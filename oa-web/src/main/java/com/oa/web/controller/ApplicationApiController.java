package com.oa.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.common.dto.ApprovalRequest;
import com.oa.common.dto.LoginUser;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.LeaveApply;
import com.oa.common.entity.ReimbursementApply;
import com.oa.common.entity.TripApply;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.web.feign.LeaveFeignClient;
import com.oa.web.feign.ReimbursementFeignClient;
import com.oa.web.feign.TripFeignClient;
import com.oa.web.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final ObjectMapper objectMapper;
    private final CurrentUser currentUser;

    public ApplicationApiController(LeaveFeignClient leaveFeignClient,
                                    TripFeignClient tripFeignClient,
                                    ReimbursementFeignClient reimbursementFeignClient,
                                    ObjectMapper objectMapper,
                                    CurrentUser currentUser) {
        this.leaveFeignClient = leaveFeignClient;
        this.tripFeignClient = tripFeignClient;
        this.reimbursementFeignClient = reimbursementFeignClient;
        this.objectMapper = objectMapper;
        this.currentUser = currentUser;
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
    public AjaxResult<Void> adminAdd(@PathVariable String type, @RequestBody Map<String, Object> body) {
        return add(type, body);
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
                                        @RequestBody Map<String, Object> body) {
        return switch (type) {
            case "leave" -> leaveFeignClient.adminUpdate(id, objectMapper.convertValue(body, LeaveApply.class));
            case "trip" -> tripFeignClient.adminUpdate(id, objectMapper.convertValue(body, TripApply.class));
            case "reimbursement" -> reimbursementFeignClient.adminUpdate(id, objectMapper.convertValue(body, ReimbursementApply.class));
            default -> AjaxResult.error("未知申请类型");
        };
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
    public AjaxResult<Void> adminDelete(@PathVariable String type, @PathVariable Long id) {
        return switch (type) {
            case "leave" -> leaveFeignClient.adminDelete(id);
            case "trip" -> tripFeignClient.adminDelete(id);
            case "reimbursement" -> reimbursementFeignClient.adminDelete(id);
            default -> AjaxResult.error("未知申请类型");
        };
    }

    @PostMapping("/admin/applications/{type}/{id}/approve")
    public AjaxResult<Void> approve(@PathVariable String type,
                                    @PathVariable Long id,
                                    @RequestBody ApprovalRequest body) {
        return switch (type) {
            case "leave" -> leaveFeignClient.approve(id, body);
            case "trip" -> tripFeignClient.approve(id, body);
            case "reimbursement" -> reimbursementFeignClient.approve(id, body);
            default -> AjaxResult.error("未知申请类型");
        };
    }

    private AjaxResult<Void> add(String type, Map<String, Object> body) {
        return switch (type) {
            case "leave" -> leaveFeignClient.add(objectMapper.convertValue(body, LeaveApply.class));
            case "trip" -> tripFeignClient.add(objectMapper.convertValue(body, TripApply.class));
            case "reimbursement" -> reimbursementFeignClient.add(objectMapper.convertValue(body, ReimbursementApply.class));
            default -> AjaxResult.error("未知申请类型");
        };
    }
}

