package com.oa.application.controller;

import com.oa.application.service.ApplicationApplyService;
import com.oa.common.dto.ApprovalRequest;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.LeaveApply;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
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
    private final ApplicationApplyService service;

    public LeaveController(ApplicationApplyService service) {
        this.service = service;
    }

    @PostMapping("/user/page")
    public PageResult<LeaveApply> userPage(@RequestBody PageQuery query) {
        return service.leavePage(query, true);
    }

    @PostMapping("/admin/page")
    public PageResult<LeaveApply> adminPage(@RequestBody PageQuery query) {
        return service.leavePage(query, false);
    }

    @PostMapping
    public AjaxResult<Void> add(@RequestBody LeaveApply apply) {
        return service.addLeave(apply);
    }

    @PutMapping("/user/{id}")
    public AjaxResult<Void> userUpdate(@PathVariable Long id,
                                       @RequestParam Long userId,
                                       @RequestBody LeaveApply form) {
        return service.updateLeaveByUser(id, userId, form);
    }

    @DeleteMapping("/user/{id}")
    public AjaxResult<Void> userDelete(@PathVariable Long id, @RequestParam Long userId) {
        return service.deleteLeaveByUser(id, userId);
    }

    @PutMapping("/admin/{id}")
    public AjaxResult<Void> adminUpdate(@PathVariable Long id, @RequestBody LeaveApply form) {
        return service.updateLeaveByAdmin(id, form);
    }

    @DeleteMapping("/admin/{id}")
    public AjaxResult<Void> adminDelete(@PathVariable Long id) {
        return service.deleteLeaveByAdmin(id);
    }

    @PostMapping("/admin/{id}/approve")
    public AjaxResult<Void> approve(@PathVariable Long id, @RequestBody ApprovalRequest request) {
        return service.approveLeave(id, request);
    }
}
