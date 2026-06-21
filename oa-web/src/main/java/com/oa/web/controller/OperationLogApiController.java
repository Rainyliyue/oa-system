package com.oa.web.controller;

import com.oa.common.dto.PageQuery;
import com.oa.common.entity.OperationLog;
import com.oa.common.result.PageResult;
import com.oa.web.feign.WorkflowFeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/operation-logs")
public class OperationLogApiController {
    private final WorkflowFeignClient workflowFeignClient;

    public OperationLogApiController(WorkflowFeignClient workflowFeignClient) {
        this.workflowFeignClient = workflowFeignClient;
    }

    @PostMapping("/page")
    public PageResult<OperationLog> page(@RequestBody PageQuery query) {
        return workflowFeignClient.operationLogs(query);
    }
}
