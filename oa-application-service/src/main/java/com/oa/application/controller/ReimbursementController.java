package com.oa.application.controller;

import com.oa.application.service.ApplicationApplyService;
import com.oa.common.dto.ApprovalRequest;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.ReimbursementApply;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final ApplicationApplyService service;

    public ReimbursementController(ApplicationApplyService service) {
        this.service = service;
    }

    @PostMapping("/user/page")
    public PageResult<ReimbursementApply> userPage(@RequestBody PageQuery query) {
        return service.reimbursementPage(query, true);
    }

    @PostMapping("/admin/page")
    public PageResult<ReimbursementApply> adminPage(@RequestBody PageQuery query) {
        return service.reimbursementPage(query, false);
    }

    @GetMapping("/admin/{id}")
    public AjaxResult<ReimbursementApply> adminDetail(@PathVariable Long id) {
        return service.reimbursementDetail(id);
    }

    @PostMapping
    public AjaxResult<Void> add(@RequestBody ReimbursementApply apply) {
        return service.addReimbursement(apply);
    }

    @PutMapping("/user/{id}")
    public AjaxResult<Void> userUpdate(@PathVariable Long id,
                                       @RequestParam Long userId,
                                       @RequestBody ReimbursementApply form) {
        return service.updateReimbursementByUser(id, userId, form);
    }

    @DeleteMapping("/user/{id}")
    public AjaxResult<Void> userDelete(@PathVariable Long id, @RequestParam Long userId) {
        return service.deleteReimbursementByUser(id, userId);
    }

    @PutMapping("/admin/{id}")
    public AjaxResult<Void> adminUpdate(@PathVariable Long id, @RequestBody ReimbursementApply form) {
        return service.updateReimbursementByAdmin(id, form);
    }

    @DeleteMapping("/admin/{id}")
    public AjaxResult<Void> adminDelete(@PathVariable Long id) {
        return service.deleteReimbursementByAdmin(id);
    }

    @PostMapping("/admin/{id}/approve")
    public AjaxResult<Void> approve(@PathVariable Long id, @RequestBody ApprovalRequest request) {
        return service.approveReimbursement(id, request);
    }
}
