package com.oa.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.application.mapper.ApprovalHistoryMapper;
import com.oa.application.mapper.LeaveApplyMapper;
import com.oa.application.mapper.OperationLogMapper;
import com.oa.application.mapper.ReimbursementApplyMapper;
import com.oa.application.mapper.SystemNoticeMapper;
import com.oa.application.mapper.TripApplyMapper;
import com.oa.common.dto.ApprovalRequest;
import com.oa.common.dto.OperationLogRequest;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.ApprovalHistory;
import com.oa.common.entity.LeaveApply;
import com.oa.common.entity.OperationLog;
import com.oa.common.entity.ReimbursementApply;
import com.oa.common.entity.SystemNotice;
import com.oa.common.entity.TripApply;
import com.oa.common.enums.ApprovalStatus;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.common.util.StringUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationApplyService {
    private static final String RESULT_STEP_APPROVED = "STEP_APPROVED";

    private final LeaveApplyMapper leaveMapper;
    private final TripApplyMapper tripMapper;
    private final ReimbursementApplyMapper reimbursementMapper;
    private final ApprovalHistoryMapper approvalHistoryMapper;
    private final SystemNoticeMapper noticeMapper;
    private final OperationLogMapper operationLogMapper;

    public ApplicationApplyService(LeaveApplyMapper leaveMapper,
                                   TripApplyMapper tripMapper,
                                   ReimbursementApplyMapper reimbursementMapper,
                                   ApprovalHistoryMapper approvalHistoryMapper,
                                   SystemNoticeMapper noticeMapper,
                                   OperationLogMapper operationLogMapper) {
        this.leaveMapper = leaveMapper;
        this.tripMapper = tripMapper;
        this.reimbursementMapper = reimbursementMapper;
        this.approvalHistoryMapper = approvalHistoryMapper;
        this.noticeMapper = noticeMapper;
        this.operationLogMapper = operationLogMapper;
    }

    public PageResult<LeaveApply> leavePage(PageQuery query, boolean userOnly) {
        Page<LeaveApply> page = new Page<>(query.safePage(), query.safeLimit());
        LambdaQueryWrapper<LeaveApply> wrapper = new LambdaQueryWrapper<LeaveApply>()
                .orderByDesc(LeaveApply::getCreateTime);
        if (userOnly) {
            wrapper.eq(LeaveApply::getUserId, query.getUserId());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(LeaveApply::getStatus, query.getStatus());
        }
        if (query.getStartDate() != null) {
            wrapper.ge(LeaveApply::getStartDate, query.getStartDate());
        }
        if (query.getEndDate() != null) {
            wrapper.le(LeaveApply::getEndDate, query.getEndDate());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(LeaveApply::getUsername, query.getKeyword())
                    .or()
                    .like(LeaveApply::getReason, query.getKeyword()));
        }
        Page<LeaveApply> result = leaveMapper.selectPage(page, wrapper);
        return PageResult.success(result.getTotal(), result.getRecords());
    }

    public PageResult<TripApply> tripPage(PageQuery query, boolean userOnly) {
        Page<TripApply> page = new Page<>(query.safePage(), query.safeLimit());
        LambdaQueryWrapper<TripApply> wrapper = new LambdaQueryWrapper<TripApply>()
                .orderByDesc(TripApply::getCreateTime);
        if (userOnly) {
            wrapper.eq(TripApply::getUserId, query.getUserId());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(TripApply::getStatus, query.getStatus());
        }
        if (query.getStartDate() != null) {
            wrapper.ge(TripApply::getStartDate, query.getStartDate());
        }
        if (query.getEndDate() != null) {
            wrapper.le(TripApply::getEndDate, query.getEndDate());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(TripApply::getUsername, query.getKeyword())
                    .or()
                    .like(TripApply::getDestination, query.getKeyword())
                    .or()
                    .like(TripApply::getReason, query.getKeyword()));
        }
        Page<TripApply> result = tripMapper.selectPage(page, wrapper);
        return PageResult.success(result.getTotal(), result.getRecords());
    }

    public PageResult<ReimbursementApply> reimbursementPage(PageQuery query, boolean userOnly) {
        Page<ReimbursementApply> page = new Page<>(query.safePage(), query.safeLimit());
        LambdaQueryWrapper<ReimbursementApply> wrapper = new LambdaQueryWrapper<ReimbursementApply>()
                .orderByDesc(ReimbursementApply::getCreateTime);
        if (userOnly) {
            wrapper.eq(ReimbursementApply::getUserId, query.getUserId());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(ReimbursementApply::getStatus, query.getStatus());
        }
        if (query.getStartDate() != null) {
            wrapper.ge(ReimbursementApply::getCreateTime, query.getStartDate().atStartOfDay());
        }
        if (query.getEndDate() != null) {
            wrapper.lt(ReimbursementApply::getCreateTime, query.getEndDate().plusDays(1).atStartOfDay());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(ReimbursementApply::getUsername, query.getKeyword())
                    .or()
                    .like(ReimbursementApply::getTitle, query.getKeyword())
                    .or()
                    .like(ReimbursementApply::getDetail, query.getKeyword()));
        }
        Page<ReimbursementApply> result = reimbursementMapper.selectPage(page, wrapper);
        return PageResult.success(result.getTotal(), result.getRecords());
    }

    @Transactional
    public AjaxResult<Void> addLeave(LeaveApply apply) {
        apply.setStatus(ApprovalStatus.PENDING.name());
        apply.setCreateTime(LocalDateTime.now());
        apply.setUpdateTime(LocalDateTime.now());
        leaveMapper.insert(apply);
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> addTrip(TripApply apply) {
        apply.setStatus(ApprovalStatus.PENDING.name());
        apply.setCreateTime(LocalDateTime.now());
        apply.setUpdateTime(LocalDateTime.now());
        tripMapper.insert(apply);
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> addReimbursement(ReimbursementApply apply) {
        apply.setStatus(ApprovalStatus.PENDING.name());
        apply.setCreateTime(LocalDateTime.now());
        apply.setUpdateTime(LocalDateTime.now());
        reimbursementMapper.insert(apply);
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> updateLeaveByUser(Long id, Long userId, LeaveApply form) {
        LeaveApply old = leaveMapper.selectById(id);
        if (old == null || !old.getUserId().equals(userId)) {
            return AjaxResult.error("记录不存在");
        }
        if (!ApprovalStatus.valueOf(old.getStatus()).canUserEdit()) {
            return AjaxResult.error("该状态不能修改");
        }
        old.setReason(form.getReason());
        old.setEvidenceImage(form.getEvidenceImage());
        old.setStartDate(form.getStartDate());
        old.setEndDate(form.getEndDate());
        old.setDayCount(form.getDayCount());
        old.setUpdateTime(LocalDateTime.now());
        leaveMapper.updateById(old);
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> updateTripByUser(Long id, Long userId, TripApply form) {
        TripApply old = tripMapper.selectById(id);
        if (old == null || !old.getUserId().equals(userId)) {
            return AjaxResult.error("记录不存在");
        }
        if (!ApprovalStatus.valueOf(old.getStatus()).canUserEdit()) {
            return AjaxResult.error("该状态不能修改");
        }
        old.setDestination(form.getDestination());
        old.setReason(form.getReason());
        old.setEvidenceImage(form.getEvidenceImage());
        old.setStartDate(form.getStartDate());
        old.setEndDate(form.getEndDate());
        old.setBudget(form.getBudget());
        old.setUpdateTime(LocalDateTime.now());
        tripMapper.updateById(old);
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> updateReimbursementByUser(Long id, Long userId, ReimbursementApply form) {
        ReimbursementApply old = reimbursementMapper.selectById(id);
        if (old == null || !old.getUserId().equals(userId)) {
            return AjaxResult.error("记录不存在");
        }
        if (!ApprovalStatus.valueOf(old.getStatus()).canUserEdit()) {
            return AjaxResult.error("该状态不能修改");
        }
        old.setTitle(form.getTitle());
        old.setAmount(form.getAmount());
        old.setDetail(form.getDetail());
        old.setEvidenceImage(form.getEvidenceImage());
        old.setUpdateTime(LocalDateTime.now());
        reimbursementMapper.updateById(old);
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> deleteLeaveByUser(Long id, Long userId) {
        LeaveApply old = leaveMapper.selectById(id);
        if (old == null || !old.getUserId().equals(userId)) {
            return AjaxResult.error("记录不存在");
        }
        if (!canUserDelete(old.getStatus(), old.getEndDate())) {
            return AjaxResult.error("审批通过且尚未结束的记录不能删除");
        }
        leaveMapper.deleteById(id);
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> deleteTripByUser(Long id, Long userId) {
        TripApply old = tripMapper.selectById(id);
        if (old == null || !old.getUserId().equals(userId)) {
            return AjaxResult.error("记录不存在");
        }
        if (!canUserDelete(old.getStatus(), old.getEndDate())) {
            return AjaxResult.error("审批通过且尚未结束的记录不能删除");
        }
        tripMapper.deleteById(id);
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> deleteReimbursementByUser(Long id, Long userId) {
        ReimbursementApply old = reimbursementMapper.selectById(id);
        if (old == null || !old.getUserId().equals(userId)) {
            return AjaxResult.error("记录不存在");
        }
        reimbursementMapper.deleteById(id);
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> updateLeaveByAdmin(Long id, LeaveApply form) {
        LeaveApply old = leaveMapper.selectById(id);
        if (old == null) {
            return AjaxResult.error("记录不存在");
        }
        old.setReason(form.getReason());
        old.setEvidenceImage(form.getEvidenceImage());
        old.setStartDate(form.getStartDate());
        old.setEndDate(form.getEndDate());
        old.setDayCount(form.getDayCount());
        old.setStatus(form.getStatus());
        old.setAuditComment(form.getAuditComment());
        old.setUpdateTime(LocalDateTime.now());
        leaveMapper.updateById(old);
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> updateTripByAdmin(Long id, TripApply form) {
        TripApply old = tripMapper.selectById(id);
        if (old == null) {
            return AjaxResult.error("记录不存在");
        }
        old.setDestination(form.getDestination());
        old.setReason(form.getReason());
        old.setEvidenceImage(form.getEvidenceImage());
        old.setStartDate(form.getStartDate());
        old.setEndDate(form.getEndDate());
        old.setBudget(form.getBudget());
        old.setStatus(form.getStatus());
        old.setAuditComment(form.getAuditComment());
        old.setUpdateTime(LocalDateTime.now());
        tripMapper.updateById(old);
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> updateReimbursementByAdmin(Long id, ReimbursementApply form) {
        ReimbursementApply old = reimbursementMapper.selectById(id);
        if (old == null) {
            return AjaxResult.error("记录不存在");
        }
        old.setTitle(form.getTitle());
        old.setAmount(form.getAmount());
        old.setDetail(form.getDetail());
        old.setEvidenceImage(form.getEvidenceImage());
        old.setStatus(form.getStatus());
        old.setAuditComment(form.getAuditComment());
        old.setUpdateTime(LocalDateTime.now());
        reimbursementMapper.updateById(old);
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> deleteLeaveByAdmin(Long id) {
        leaveMapper.deleteById(id);
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> deleteTripByAdmin(Long id) {
        tripMapper.deleteById(id);
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> deleteReimbursementByAdmin(Long id) {
        reimbursementMapper.deleteById(id);
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> approveLeave(Long id, ApprovalRequest request) {
        LeaveApply old = leaveMapper.selectById(id);
        if (old == null) {
            return AjaxResult.error("记录不存在");
        }
        ApprovalDecision decision = approve("leave", id, old.getUserId(), old.getUsername(),
                "请假申请", old.getReason(), old.getStatus(), request);
        if (decision == null) {
            return AjaxResult.error("只有待审批记录可以审批");
        }
        old.setStatus(decision.status());
        old.setAuditComment(request.getAuditComment());
        if (!RESULT_STEP_APPROVED.equals(decision.result())) {
            old.setApproveTime(LocalDateTime.now());
        }
        old.setUpdateTime(LocalDateTime.now());
        leaveMapper.updateById(old);
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> approveTrip(Long id, ApprovalRequest request) {
        TripApply old = tripMapper.selectById(id);
        if (old == null) {
            return AjaxResult.error("记录不存在");
        }
        ApprovalDecision decision = approve("trip", id, old.getUserId(), old.getUsername(),
                "出差申请", old.getDestination(), old.getStatus(), request);
        if (decision == null) {
            return AjaxResult.error("只有待审批记录可以审批");
        }
        old.setStatus(decision.status());
        old.setAuditComment(request.getAuditComment());
        if (!RESULT_STEP_APPROVED.equals(decision.result())) {
            old.setApproveTime(LocalDateTime.now());
        }
        old.setUpdateTime(LocalDateTime.now());
        tripMapper.updateById(old);
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> approveReimbursement(Long id, ApprovalRequest request) {
        ReimbursementApply old = reimbursementMapper.selectById(id);
        if (old == null) {
            return AjaxResult.error("记录不存在");
        }
        ApprovalDecision decision = approve("reimbursement", id, old.getUserId(), old.getUsername(),
                "报销申请", old.getTitle(), old.getStatus(), request);
        if (decision == null) {
            return AjaxResult.error("只有待审批记录可以审批");
        }
        old.setStatus(decision.status());
        old.setAuditComment(request.getAuditComment());
        if (!RESULT_STEP_APPROVED.equals(decision.result())) {
            old.setApproveTime(LocalDateTime.now());
        }
        old.setUpdateTime(LocalDateTime.now());
        reimbursementMapper.updateById(old);
        return AjaxResult.success();
    }

    public List<ApprovalHistory> approvalHistory(String applyType, Long applyId) {
        return approvalHistoryMapper.selectList(new LambdaQueryWrapper<ApprovalHistory>()
                .eq(ApprovalHistory::getApplyType, applyType)
                .eq(ApprovalHistory::getApplyId, applyId)
                .orderByAsc(ApprovalHistory::getApprovalLevel)
                .orderByAsc(ApprovalHistory::getId));
    }

    public PageResult<SystemNotice> noticePage(PageQuery query) {
        Page<SystemNotice> page = new Page<>(query.safePage(), query.safeLimit());
        LambdaQueryWrapper<SystemNotice> wrapper = new LambdaQueryWrapper<SystemNotice>()
                .eq(SystemNotice::getUserId, query.getUserId())
                .orderByDesc(SystemNotice::getCreateTime);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SystemNotice::getTitle, query.getKeyword())
                    .or()
                    .like(SystemNotice::getContent, query.getKeyword()));
        }
        Page<SystemNotice> result = noticeMapper.selectPage(page, wrapper);
        return PageResult.success(result.getTotal(), result.getRecords());
    }

    public long unreadNoticeCount(Long userId) {
        return noticeMapper.selectCount(new LambdaQueryWrapper<SystemNotice>()
                .eq(SystemNotice::getUserId, userId)
                .eq(SystemNotice::getReadFlag, false));
    }

    @Transactional
    public AjaxResult<Void> markNoticeRead(Long id, Long userId) {
        SystemNotice notice = noticeMapper.selectById(id);
        if (notice == null || !notice.getUserId().equals(userId)) {
            return AjaxResult.error("通知不存在");
        }
        notice.setReadFlag(true);
        noticeMapper.updateById(notice);
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> markAllNoticeRead(Long userId) {
        List<SystemNotice> notices = noticeMapper.selectList(new LambdaQueryWrapper<SystemNotice>()
                .eq(SystemNotice::getUserId, userId)
                .eq(SystemNotice::getReadFlag, false));
        for (SystemNotice notice : notices) {
            notice.setReadFlag(true);
            noticeMapper.updateById(notice);
        }
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> saveOperationLog(OperationLogRequest request) {
        OperationLog log = new OperationLog();
        log.setOperatorId(request.getOperatorId());
        log.setOperatorName(request.getOperatorName());
        log.setModuleName(request.getModuleName());
        log.setOperationType(request.getOperationType());
        log.setTargetType(request.getTargetType());
        log.setTargetId(request.getTargetId());
        log.setContent(request.getContent());
        log.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(log);
        return AjaxResult.success();
    }

    public PageResult<OperationLog> operationLogPage(PageQuery query) {
        Page<OperationLog> page = new Page<>(query.safePage(), query.safeLimit());
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<OperationLog>()
                .orderByDesc(OperationLog::getCreateTime);
        if (StringUtils.hasText(query.getType())) {
            wrapper.eq(OperationLog::getOperationType, query.getType());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(OperationLog::getOperatorName, query.getKeyword())
                    .or()
                    .like(OperationLog::getModuleName, query.getKeyword())
                    .or()
                    .like(OperationLog::getContent, query.getKeyword()));
        }
        Page<OperationLog> result = operationLogMapper.selectPage(page, wrapper);
        return PageResult.success(result.getTotal(), result.getRecords());
    }

    private ApprovalDecision approve(String applyType,
                                     Long applyId,
                                     Long applicantId,
                                     String applicantName,
                                     String title,
                                     String subject,
                                     String currentStatus,
                                     ApprovalRequest request) {
        if (!ApprovalStatus.valueOf(currentStatus).canAdminApprove()) {
            return null;
        }
        boolean passed = Boolean.TRUE.equals(request.getPassed());
        boolean finalApproval = !Boolean.FALSE.equals(request.getFinalApproval());
        String result = passed ? (finalApproval ? ApprovalStatus.APPROVED.name() : RESULT_STEP_APPROVED)
                : ApprovalStatus.REJECTED.name();
        String nextStatus = passed && !finalApproval ? ApprovalStatus.PENDING.name() : result;
        int level = Math.toIntExact(approvalHistoryMapper.selectCount(new LambdaQueryWrapper<ApprovalHistory>()
                .eq(ApprovalHistory::getApplyType, applyType)
                .eq(ApprovalHistory::getApplyId, applyId)) + 1);

        ApprovalHistory history = new ApprovalHistory();
        history.setApplyType(applyType);
        history.setApplyId(applyId);
        history.setApproverId(request.getApproverId());
        history.setApproverName(request.getApproverName());
        history.setApprovalLevel(level);
        history.setResult(result);
        history.setAuditComment(request.getAuditComment());
        history.setCreateTime(LocalDateTime.now());
        approvalHistoryMapper.insert(history);

        createApprovalNotice(applicantId, title, subject, result, level);

        OperationLogRequest logRequest = new OperationLogRequest();
        logRequest.setOperatorId(request.getApproverId());
        logRequest.setOperatorName(request.getApproverName());
        logRequest.setModuleName(title.replace("申请", "管理"));
        logRequest.setOperationType("APPROVE");
        logRequest.setTargetType(applyType);
        logRequest.setTargetId(applyId);
        logRequest.setContent(approvalResultText(result) + "：" + safeSubject(subject));
        saveOperationLog(logRequest);

        return new ApprovalDecision(nextStatus, result);
    }

    private void createApprovalNotice(Long userId, String title, String subject, String result, int level) {
        if (userId == null) {
            return;
        }
        SystemNotice notice = new SystemNotice();
        notice.setUserId(userId);
        notice.setTitle(title + "审批进度");
        notice.setContent(approvalNoticeText(title, subject, result, level));
        notice.setReadFlag(false);
        notice.setCreateTime(LocalDateTime.now());
        noticeMapper.insert(notice);
    }

    private String approvalNoticeText(String title, String subject, String result, int level) {
        String item = title + (StringUtils.hasText(subject) ? "（" + safeSubject(subject) + "）" : "");
        if (RESULT_STEP_APPROVED.equals(result)) {
            return item + "已完成第 " + level + " 级审批，正在等待后续审批。";
        }
        if (ApprovalStatus.APPROVED.name().equals(result)) {
            return item + "已审批通过。";
        }
        return item + "未通过审批，请查看审批意见。";
    }

    private String approvalResultText(String result) {
        if (RESULT_STEP_APPROVED.equals(result)) {
            return "本级通过";
        }
        if (ApprovalStatus.APPROVED.name().equals(result)) {
            return "审批通过";
        }
        return "审批不通过";
    }

    private boolean canUserDelete(String statusName, LocalDate endDate) {
        ApprovalStatus status = ApprovalStatus.valueOf(statusName);
        if (status == ApprovalStatus.PENDING || status == ApprovalStatus.REJECTED || status == ApprovalStatus.FINISHED) {
            return true;
        }
        return status == ApprovalStatus.APPROVED
                && endDate != null
                && endDate.isBefore(LocalDate.now());
    }

    private String safeSubject(String subject) {
        if (!StringUtils.hasText(subject)) {
            return "无标题";
        }
        return subject.length() > 80 ? subject.substring(0, 80) : subject;
    }

    private record ApprovalDecision(String status, String result) {
    }
}
