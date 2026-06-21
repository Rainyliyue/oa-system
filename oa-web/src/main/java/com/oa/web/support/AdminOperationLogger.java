package com.oa.web.support;

import com.oa.common.dto.LoginUser;
import com.oa.common.dto.OperationLogRequest;
import com.oa.common.result.AjaxResult;
import com.oa.web.feign.WorkflowFeignClient;
import com.oa.web.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class AdminOperationLogger {
    private final WorkflowFeignClient workflowFeignClient;
    private final CurrentUser currentUser;

    public AdminOperationLogger(WorkflowFeignClient workflowFeignClient, CurrentUser currentUser) {
        this.workflowFeignClient = workflowFeignClient;
        this.currentUser = currentUser;
    }

    public void logIfSuccess(HttpServletRequest request,
                             AjaxResult<?> result,
                             String moduleName,
                             String operationType,
                             String targetType,
                             Long targetId,
                             String content) {
        if (result == null || !result.ok()) {
            return;
        }
        try {
            LoginUser user = currentUser.get(request);
            OperationLogRequest log = new OperationLogRequest();
            if (user != null) {
                log.setOperatorId(user.getId());
                log.setOperatorName(user.getRealName());
            }
            log.setModuleName(moduleName);
            log.setOperationType(operationType);
            log.setTargetType(targetType);
            log.setTargetId(targetId);
            log.setContent(content);
            workflowFeignClient.saveOperationLog(log);
        } catch (Exception ignored) {
            // 日志是审计增强能力，不能影响主业务操作。
        }
    }
}
