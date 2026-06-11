package com.oa.application.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.application.mapper.LeaveApplyMapper;
import com.oa.common.dto.ApprovalRequest;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.LeaveApply;
import com.oa.common.enums.ApprovalStatus;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.common.util.StringUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leave")
public class LeaveController {
    private final LeaveApplyMapper mapper;

    public LeaveController(LeaveApplyMapper mapper) {
        this.mapper = mapper;
    }

    @PostMapping("/user/page")
    public PageResult<LeaveApply> userPage(@RequestBody PageQuery query) {
        query.setUserId(query.getUserId());
        return page(query, true);
    }

    @PostMapping("/admin/page")
    public PageResult<LeaveApply> adminPage(@RequestBody PageQuery query) {
        return page(query, false);
    }

    @PostMapping
    public AjaxResult<Void> add(@RequestBody LeaveApply apply) {
        apply.setStatus(ApprovalStatus.PENDING.name());
        apply.setCreateTime(LocalDateTime.now());
        apply.setUpdateTime(LocalDateTime.now());
        mapper.insert(apply);
        return AjaxResult.success();
    }

    @PutMapping("/user/{id}")
    public AjaxResult<Void> userUpdate(@PathVariable Long id,
                                       @RequestParam Long userId,
                                       @RequestBody LeaveApply form) {
        LeaveApply old = mapper.selectById(id);
        if (old == null || !old.getUserId().equals(userId)) {
            return AjaxResult.error("记录不存在");
        }
        if (!ApprovalStatus.valueOf(old.getStatus()).canUserEdit()) {
            return AjaxResult.error("该状态不能修改");
        }
        old.setReason(form.getReason());
        old.setStartDate(form.getStartDate());
        old.setEndDate(form.getEndDate());
        old.setDayCount(form.getDayCount());
        old.setUpdateTime(LocalDateTime.now());
        mapper.updateById(old);
        return AjaxResult.success();
    }

    @DeleteMapping("/user/{id}")
    public AjaxResult<Void> userDelete(@PathVariable Long id, @RequestParam Long userId) {
        LeaveApply old = mapper.selectById(id);
        if (old == null || !old.getUserId().equals(userId)) {
            return AjaxResult.error("记录不存在");
        }
        if (!canUserDelete(old)) {
            return AjaxResult.error("审批通过且尚未结束的记录不能删除");
        }
        mapper.deleteById(id);
        return AjaxResult.success();
    }

    @PutMapping("/admin/{id}")
    public AjaxResult<Void> adminUpdate(@PathVariable Long id, @RequestBody LeaveApply form) {
        LeaveApply old = mapper.selectById(id);
        if (old == null) {
            return AjaxResult.error("记录不存在");
        }
        old.setReason(form.getReason());
        old.setStartDate(form.getStartDate());
        old.setEndDate(form.getEndDate());
        old.setDayCount(form.getDayCount());
        old.setStatus(form.getStatus());
        old.setAuditComment(form.getAuditComment());
        old.setUpdateTime(LocalDateTime.now());
        mapper.updateById(old);
        return AjaxResult.success();
    }

    @DeleteMapping("/admin/{id}")
    public AjaxResult<Void> adminDelete(@PathVariable Long id) {
        mapper.deleteById(id);
        return AjaxResult.success();
    }

    @PostMapping("/admin/{id}/approve")
    public AjaxResult<Void> approve(@PathVariable Long id, @RequestBody ApprovalRequest request) {
        LeaveApply old = mapper.selectById(id);
        if (old == null) {
            return AjaxResult.error("记录不存在");
        }
        if (!ApprovalStatus.valueOf(old.getStatus()).canAdminApprove()) {
            return AjaxResult.error("只有待审批记录可以审批");
        }
        old.setStatus(Boolean.TRUE.equals(request.getPassed())
                ? ApprovalStatus.APPROVED.name()
                : ApprovalStatus.REJECTED.name());
        old.setAuditComment(request.getAuditComment());
        old.setApproveTime(LocalDateTime.now());
        old.setUpdateTime(LocalDateTime.now());
        mapper.updateById(old);
        return AjaxResult.success();
    }

    private PageResult<LeaveApply> page(PageQuery query, boolean userOnly) {
        Page<LeaveApply> page = new Page<>(query.safePage(), query.safeLimit());
        LambdaQueryWrapper<LeaveApply> wrapper = new LambdaQueryWrapper<LeaveApply>()
                .orderByDesc(LeaveApply::getCreateTime);
        if (userOnly) {
            wrapper.eq(LeaveApply::getUserId, query.getUserId());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(LeaveApply::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(LeaveApply::getUsername, query.getKeyword())
                    .or()
                    .like(LeaveApply::getReason, query.getKeyword()));
        }
        Page<LeaveApply> result = mapper.selectPage(page, wrapper);
        return PageResult.success(result.getTotal(), result.getRecords());
    }

    private boolean canUserDelete(LeaveApply apply) {
        ApprovalStatus status = ApprovalStatus.valueOf(apply.getStatus());
        if (status == ApprovalStatus.PENDING || status == ApprovalStatus.REJECTED || status == ApprovalStatus.FINISHED) {
            return true;
        }
        return status == ApprovalStatus.APPROVED
                && apply.getEndDate() != null
                && apply.getEndDate().isBefore(LocalDate.now());
    }
}

