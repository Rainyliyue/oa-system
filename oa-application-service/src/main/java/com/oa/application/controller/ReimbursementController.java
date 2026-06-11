package com.oa.application.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.application.mapper.ReimbursementApplyMapper;
import com.oa.common.dto.ApprovalRequest;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.ReimbursementApply;
import com.oa.common.enums.ApprovalStatus;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.common.util.StringUtils;
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
@RequestMapping("/reimbursement")
public class ReimbursementController {
    private final ReimbursementApplyMapper mapper;

    public ReimbursementController(ReimbursementApplyMapper mapper) {
        this.mapper = mapper;
    }

    @PostMapping("/user/page")
    public PageResult<ReimbursementApply> userPage(@RequestBody PageQuery query) {
        return page(query, true);
    }

    @PostMapping("/admin/page")
    public PageResult<ReimbursementApply> adminPage(@RequestBody PageQuery query) {
        return page(query, false);
    }

    @PostMapping
    public AjaxResult<Void> add(@RequestBody ReimbursementApply apply) {
        apply.setStatus(ApprovalStatus.PENDING.name());
        apply.setCreateTime(LocalDateTime.now());
        apply.setUpdateTime(LocalDateTime.now());
        mapper.insert(apply);
        return AjaxResult.success();
    }

    @PutMapping("/user/{id}")
    public AjaxResult<Void> userUpdate(@PathVariable Long id,
                                       @RequestParam Long userId,
                                       @RequestBody ReimbursementApply form) {
        ReimbursementApply old = mapper.selectById(id);
        if (old == null || !old.getUserId().equals(userId)) {
            return AjaxResult.error("记录不存在");
        }
        if (!ApprovalStatus.valueOf(old.getStatus()).canUserEdit()) {
            return AjaxResult.error("该状态不能修改");
        }
        old.setTitle(form.getTitle());
        old.setAmount(form.getAmount());
        old.setDetail(form.getDetail());
        old.setUpdateTime(LocalDateTime.now());
        mapper.updateById(old);
        return AjaxResult.success();
    }

    @DeleteMapping("/user/{id}")
    public AjaxResult<Void> userDelete(@PathVariable Long id, @RequestParam Long userId) {
        ReimbursementApply old = mapper.selectById(id);
        if (old == null || !old.getUserId().equals(userId)) {
            return AjaxResult.error("记录不存在");
        }
        mapper.deleteById(id);
        return AjaxResult.success();
    }

    @PutMapping("/admin/{id}")
    public AjaxResult<Void> adminUpdate(@PathVariable Long id, @RequestBody ReimbursementApply form) {
        ReimbursementApply old = mapper.selectById(id);
        if (old == null) {
            return AjaxResult.error("记录不存在");
        }
        old.setTitle(form.getTitle());
        old.setAmount(form.getAmount());
        old.setDetail(form.getDetail());
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
        ReimbursementApply old = mapper.selectById(id);
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

    private PageResult<ReimbursementApply> page(PageQuery query, boolean userOnly) {
        Page<ReimbursementApply> page = new Page<>(query.safePage(), query.safeLimit());
        LambdaQueryWrapper<ReimbursementApply> wrapper = new LambdaQueryWrapper<ReimbursementApply>()
                .orderByDesc(ReimbursementApply::getCreateTime);
        if (userOnly) {
            wrapper.eq(ReimbursementApply::getUserId, query.getUserId());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(ReimbursementApply::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(ReimbursementApply::getUsername, query.getKeyword())
                    .or()
                    .like(ReimbursementApply::getTitle, query.getKeyword())
                    .or()
                    .like(ReimbursementApply::getDetail, query.getKeyword()));
        }
        Page<ReimbursementApply> result = mapper.selectPage(page, wrapper);
        return PageResult.success(result.getTotal(), result.getRecords());
    }
}

