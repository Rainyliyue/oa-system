package com.oa.web.feign;

import com.oa.common.dto.OperationLogRequest;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.ApprovalHistory;
import com.oa.common.entity.OperationLog;
import com.oa.common.entity.SystemNotice;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(contextId = "workflowFeignClient", value = "oa-application-service", path = "/workflow")
public interface WorkflowFeignClient {
    @GetMapping("/approval-history/{type}/{id}")
    AjaxResult<List<ApprovalHistory>> approvalHistory(@PathVariable("type") String type,
                                                      @PathVariable("id") Long id);

    @PostMapping("/notices/page")
    PageResult<SystemNotice> notices(@RequestBody PageQuery query);

    @GetMapping("/notices/unread-count")
    AjaxResult<Long> unreadNoticeCount(@RequestParam("userId") Long userId);

    @PostMapping("/notices/{id}/read")
    AjaxResult<Void> markNoticeRead(@PathVariable("id") Long id, @RequestParam("userId") Long userId);

    @PostMapping("/notices/read-all")
    AjaxResult<Void> markAllNoticeRead(@RequestParam("userId") Long userId);

    @PostMapping("/operation-logs")
    AjaxResult<Void> saveOperationLog(@RequestBody OperationLogRequest request);

    @PostMapping("/operation-logs/page")
    PageResult<OperationLog> operationLogs(@RequestBody PageQuery query);
}
