package com.oa.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.common.dto.ApprovalRequest;
import com.oa.common.dto.LoginUser;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.ApprovalHistory;
import com.oa.common.entity.LeaveApply;
import com.oa.common.entity.ReimbursementApply;
import com.oa.common.entity.TripApply;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.web.feign.LeaveFeignClient;
import com.oa.web.feign.ReimbursementFeignClient;
import com.oa.web.feign.TripFeignClient;
import com.oa.web.feign.WorkflowFeignClient;
import com.oa.web.security.CurrentUser;
import com.oa.web.support.AdminOperationLogger;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApplicationApiController {
    private final LeaveFeignClient leaveFeignClient;
    private final TripFeignClient tripFeignClient;
    private final ReimbursementFeignClient reimbursementFeignClient;
    private final WorkflowFeignClient workflowFeignClient;
    private final ObjectMapper objectMapper;
    private final CurrentUser currentUser;
    private final AdminOperationLogger operationLogger;

    public ApplicationApiController(LeaveFeignClient leaveFeignClient,
                                    TripFeignClient tripFeignClient,
                                    ReimbursementFeignClient reimbursementFeignClient,
                                    WorkflowFeignClient workflowFeignClient,
                                    ObjectMapper objectMapper,
                                    CurrentUser currentUser,
                                    AdminOperationLogger operationLogger) {
        this.leaveFeignClient = leaveFeignClient;
        this.tripFeignClient = tripFeignClient;
        this.reimbursementFeignClient = reimbursementFeignClient;
        this.workflowFeignClient = workflowFeignClient;
        this.objectMapper = objectMapper;
        this.currentUser = currentUser;
        this.operationLogger = operationLogger;
    }

    @PostMapping("/user/applications/{type}/page")
    public PageResult<?> userPage(@PathVariable String type,
                                  @RequestBody PageQuery query,
                                  HttpServletRequest request) {
        LoginUser user = currentUser.get(request);
        query.setUserId(user.getId());
        return switch (type) {
            case "leave" -> leaveFeignClient.userPage(query);
            case "trip" -> tripFeignClient.userPage(query);
            case "reimbursement" -> reimbursementFeignClient.userPage(query);
            default -> PageResult.empty();
        };
    }

    @PostMapping("/admin/applications/{type}/page")
    public PageResult<?> adminPage(@PathVariable String type, @RequestBody PageQuery query) {
        return switch (type) {
            case "leave" -> leaveFeignClient.adminPage(query);
            case "trip" -> tripFeignClient.adminPage(query);
            case "reimbursement" -> reimbursementFeignClient.adminPage(query);
            default -> PageResult.empty();
        };
    }

    @PostMapping("/user/applications/{type}")
    public AjaxResult<Void> userAdd(@PathVariable String type,
                                    @RequestBody Map<String, Object> body,
                                    HttpServletRequest request) {
        LoginUser user = currentUser.get(request);
        body.put("userId", user.getId());
        body.put("username", user.getRealName());
        return add(type, body);
    }

    @PostMapping("/admin/applications/{type}")
    public AjaxResult<Void> adminAdd(@PathVariable String type,
                                     @RequestBody Map<String, Object> body,
                                     HttpServletRequest request) {
        AjaxResult<Void> result = add(type, body);
        operationLogger.logIfSuccess(request, result, moduleName(type), "CREATE", type, null, "新增" + moduleName(type));
        return result;
    }

    @PutMapping("/user/applications/{type}/{id}")
    public AjaxResult<Void> userUpdate(@PathVariable String type,
                                       @PathVariable Long id,
                                       @RequestBody Map<String, Object> body,
                                       HttpServletRequest request) {
        LoginUser user = currentUser.get(request);
        return switch (type) {
            case "leave" -> leaveFeignClient.userUpdate(id, user.getId(), objectMapper.convertValue(body, LeaveApply.class));
            case "trip" -> tripFeignClient.userUpdate(id, user.getId(), objectMapper.convertValue(body, TripApply.class));
            case "reimbursement" -> reimbursementFeignClient.userUpdate(id, user.getId(), objectMapper.convertValue(body, ReimbursementApply.class));
            default -> AjaxResult.error("未知申请类型");
        };
    }

    @PutMapping("/admin/applications/{type}/{id}")
    public AjaxResult<Void> adminUpdate(@PathVariable String type,
                                        @PathVariable Long id,
                                        @RequestBody Map<String, Object> body,
                                        HttpServletRequest request) {
        AjaxResult<Void> result = switch (type) {
            case "leave" -> leaveFeignClient.adminUpdate(id, objectMapper.convertValue(body, LeaveApply.class));
            case "trip" -> tripFeignClient.adminUpdate(id, objectMapper.convertValue(body, TripApply.class));
            case "reimbursement" -> reimbursementFeignClient.adminUpdate(id, objectMapper.convertValue(body, ReimbursementApply.class));
            default -> AjaxResult.error("未知申请类型");
        };
        operationLogger.logIfSuccess(request, result, moduleName(type), "UPDATE", type, id, "修改" + moduleName(type));
        return result;
    }

    @DeleteMapping("/user/applications/{type}/{id}")
    public AjaxResult<Void> userDelete(@PathVariable String type,
                                       @PathVariable Long id,
                                       HttpServletRequest request) {
        LoginUser user = currentUser.get(request);
        return switch (type) {
            case "leave" -> leaveFeignClient.userDelete(id, user.getId());
            case "trip" -> tripFeignClient.userDelete(id, user.getId());
            case "reimbursement" -> reimbursementFeignClient.userDelete(id, user.getId());
            default -> AjaxResult.error("未知申请类型");
        };
    }

    @DeleteMapping("/admin/applications/{type}/{id}")
    public AjaxResult<Void> adminDelete(@PathVariable String type,
                                        @PathVariable Long id,
                                        HttpServletRequest request) {
        AjaxResult<Void> result = switch (type) {
            case "leave" -> leaveFeignClient.adminDelete(id);
            case "trip" -> tripFeignClient.adminDelete(id);
            case "reimbursement" -> reimbursementFeignClient.adminDelete(id);
            default -> AjaxResult.error("未知申请类型");
        };
        operationLogger.logIfSuccess(request, result, moduleName(type), "DELETE", type, id, "删除" + moduleName(type));
        return result;
    }

    @PostMapping("/admin/applications/{type}/{id}/approve")
    public AjaxResult<Void> approve(@PathVariable String type,
                                    @PathVariable Long id,
                                    @RequestBody ApprovalRequest body,
                                    HttpServletRequest request) {
        LoginUser user = currentUser.get(request);
        if (user != null) {
            body.setApproverId(user.getId());
            body.setApproverName(user.getRealName());
        }
        return switch (type) {
            case "leave" -> leaveFeignClient.approve(id, body);
            case "trip" -> tripFeignClient.approve(id, body);
            case "reimbursement" -> reimbursementFeignClient.approve(id, body);
            default -> AjaxResult.error("未知申请类型");
        };
    }

    @GetMapping("/applications/{type}/{id}/history")
    public AjaxResult<List<ApprovalHistory>> approvalHistory(@PathVariable String type, @PathVariable Long id) {
        return workflowFeignClient.approvalHistory(type, id);
    }

    private AjaxResult<Void> add(String type, Map<String, Object> body) {
        return switch (type) {
            case "leave" -> leaveFeignClient.add(objectMapper.convertValue(body, LeaveApply.class));
            case "trip" -> tripFeignClient.add(objectMapper.convertValue(body, TripApply.class));
            case "reimbursement" -> reimbursementFeignClient.add(objectMapper.convertValue(body, ReimbursementApply.class));
            default -> AjaxResult.error("未知申请类型");
        };
    }

    private String moduleName(String type) {
        return switch (type) {
            case "leave" -> "请假管理";
            case "trip" -> "出差管理";
            case "reimbursement" -> "报销管理";
            default -> "申请管理";
        };
    }
}
