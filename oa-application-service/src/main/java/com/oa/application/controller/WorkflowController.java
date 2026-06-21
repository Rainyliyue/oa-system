package com.oa.application.controller;

import com.oa.application.service.ApplicationApplyService;
import com.oa.common.dto.OperationLogRequest;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.ApprovalHistory;
import com.oa.common.entity.OperationLog;
import com.oa.common.entity.SystemNotice;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workflow")
public class WorkflowController {
    private final ApplicationApplyService service;

    public WorkflowController(ApplicationApplyService service) {
        this.service = service;
    }

    @GetMapping("/approval-history/{type}/{id}")
    public AjaxResult<List<ApprovalHistory>> approvalHistory(@PathVariable String type, @PathVariable Long id) {
        return AjaxResult.success(service.approvalHistory(type, id));
    }

    @PostMapping("/notices/page")
    public PageResult<SystemNotice> notices(@RequestBody PageQuery query) {
        return service.noticePage(query);
    }

    @GetMapping("/notices/unread-count")
    public AjaxResult<Long> unreadNoticeCount(@RequestParam Long userId) {
        return AjaxResult.success(service.unreadNoticeCount(userId));
    }

    @PostMapping("/notices/{id}/read")
    public AjaxResult<Void> markNoticeRead(@PathVariable Long id, @RequestParam Long userId) {
        return service.markNoticeRead(id, userId);
    }

    @PostMapping("/notices/read-all")
    public AjaxResult<Void> markAllNoticeRead(@RequestParam Long userId) {
        return service.markAllNoticeRead(userId);
    }

    @PostMapping("/operation-logs")
    public AjaxResult<Void> saveOperationLog(@RequestBody OperationLogRequest request) {
        return service.saveOperationLog(request);
    }

    @PostMapping("/operation-logs/page")
    public PageResult<OperationLog> operationLogs(@RequestBody PageQuery query) {
        return service.operationLogPage(query);
    }
}
