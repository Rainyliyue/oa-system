package com.oa.application.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.application.mapper.TripApplyMapper;
import com.oa.common.dto.ApprovalRequest;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.TripApply;
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
@RequestMapping("/trip")
public class TripController {
    private final TripApplyMapper mapper;

    public TripController(TripApplyMapper mapper) {
        this.mapper = mapper;
    }

    @PostMapping("/user/page")
    public PageResult<TripApply> userPage(@RequestBody PageQuery query) {
        return page(query, true);
    }

    @PostMapping("/admin/page")
    public PageResult<TripApply> adminPage(@RequestBody PageQuery query) {
        return page(query, false);
    }

    @PostMapping
    public AjaxResult<Void> add(@RequestBody TripApply apply) {
        apply.setStatus(ApprovalStatus.PENDING.name());
        apply.setCreateTime(LocalDateTime.now());
        apply.setUpdateTime(LocalDateTime.now());
        mapper.insert(apply);
        return AjaxResult.success();
    }

    @PutMapping("/user/{id}")
    public AjaxResult<Void> userUpdate(@PathVariable Long id,
                                       @RequestParam Long userId,
                                       @RequestBody TripApply form) {
        TripApply old = mapper.selectById(id);
        if (old == null || !old.getUserId().equals(userId)) {
            return AjaxResult.error("记录不存在");
        }
        if (!ApprovalStatus.valueOf(old.getStatus()).canUserEdit()) {
            return AjaxResult.error("该状态不能修改");
        }
        old.setDestination(form.getDestination());
        old.setReason(form.getReason());
        old.setStartDate(form.getStartDate());
        old.setEndDate(form.getEndDate());
        old.setBudget(form.getBudget());
        old.setUpdateTime(LocalDateTime.now());
        mapper.updateById(old);
        return AjaxResult.success();
    }

    @DeleteMapping("/user/{id}")
    public AjaxResult<Void> userDelete(@PathVariable Long id, @RequestParam Long userId) {
        TripApply old = mapper.selectById(id);
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
    public AjaxResult<Void> adminUpdate(@PathVariable Long id, @RequestBody TripApply form) {
        TripApply old = mapper.selectById(id);
        if (old == null) {
            return AjaxResult.error("记录不存在");
        }
        old.setDestination(form.getDestination());
        old.setReason(form.getReason());
        old.setStartDate(form.getStartDate());
        old.setEndDate(form.getEndDate());
        old.setBudget(form.getBudget());
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
        TripApply old = mapper.selectById(id);
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

    private PageResult<TripApply> page(PageQuery query, boolean userOnly) {
        Page<TripApply> page = new Page<>(query.safePage(), query.safeLimit());
        LambdaQueryWrapper<TripApply> wrapper = new LambdaQueryWrapper<TripApply>()
                .orderByDesc(TripApply::getCreateTime);
        if (userOnly) {
            wrapper.eq(TripApply::getUserId, query.getUserId());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(TripApply::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(TripApply::getUsername, query.getKeyword())
                    .or()
                    .like(TripApply::getDestination, query.getKeyword())
                    .or()
                    .like(TripApply::getReason, query.getKeyword()));
        }
        Page<TripApply> result = mapper.selectPage(page, wrapper);
        return PageResult.success(result.getTotal(), result.getRecords());
    }

    private boolean canUserDelete(TripApply apply) {
        ApprovalStatus status = ApprovalStatus.valueOf(apply.getStatus());
        if (status == ApprovalStatus.PENDING || status == ApprovalStatus.REJECTED || status == ApprovalStatus.FINISHED) {
            return true;
        }
        return status == ApprovalStatus.APPROVED
                && apply.getEndDate() != null
                && apply.getEndDate().isBefore(LocalDate.now());
    }
}

