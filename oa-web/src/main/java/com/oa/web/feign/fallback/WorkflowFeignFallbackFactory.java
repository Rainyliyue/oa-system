package com.oa.web.feign.fallback;

import com.oa.common.dto.OperationLogRequest;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.ApprovalHistory;
import com.oa.common.entity.OperationLog;
import com.oa.common.entity.SystemNotice;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.web.feign.WorkflowFeignClient;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class WorkflowFeignFallbackFactory implements FallbackFactory<WorkflowFeignClient> {
    private static final Logger log = LoggerFactory.getLogger(WorkflowFeignFallbackFactory.class);

    @Override
    public WorkflowFeignClient create(Throwable cause) {
        return new WorkflowFeignClient() {
            @Override
            public AjaxResult<List<ApprovalHistory>> approvalHistory(String type, Long id) {
                return FeignFallbackSupport.ajaxSuccess(Collections.emptyList(), "工作流服务", cause, log);
            }

            @Override
            public PageResult<SystemNotice> notices(PageQuery query) {
                return FeignFallbackSupport.page("工作流服务", cause, log);
            }

            @Override
            public AjaxResult<Long> unreadNoticeCount(Long userId) {
                return FeignFallbackSupport.ajaxSuccess(0L, "工作流服务", cause, log);
            }

            @Override
            public AjaxResult<Void> markNoticeRead(Long id, Long userId) {
                return FeignFallbackSupport.ajax("工作流服务", cause, log);
            }

            @Override
            public AjaxResult<Void> markAllNoticeRead(Long userId) {
                return FeignFallbackSupport.ajax("工作流服务", cause, log);
            }

            @Override
            public AjaxResult<Void> saveOperationLog(OperationLogRequest request) {
                return FeignFallbackSupport.ajax("工作流服务", cause, log);
            }

            @Override
            public PageResult<OperationLog> operationLogs(PageQuery query) {
                return FeignFallbackSupport.page("工作流服务", cause, log);
            }
        };
    }
}
