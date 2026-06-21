package com.oa.web.controller;

import com.oa.common.dto.LoginUser;
import com.oa.web.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalViewModelAdvice {
    private final CurrentUser currentUser;

    public GlobalViewModelAdvice(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @ModelAttribute
    public void addLayoutAttributes(HttpServletRequest request, Model model) {
        LoginUser user = currentUser.get(request);
        model.addAttribute("user", user);
        model.addAttribute("isAdmin", user != null && user.hasRole("ADMIN"));
        model.addAttribute("activeMenu", activeMenu(request.getRequestURI()));
    }

    private String activeMenu(String path) {
        if (path == null || path.equals("/") || path.equals("/index")) {
            return "index";
        }
        return switch (path) {
            case "/user/apply/leave" -> "user-leave";
            case "/user/apply/trip" -> "user-trip";
            case "/user/apply/reimbursement" -> "user-reimbursement";
            case "/user/attendance" -> "user-attendance";
            case "/admin/users" -> "admin-users";
            case "/admin/roles" -> "admin-roles";
            case "/admin/permissions" -> "admin-permissions";
            case "/admin/attendance" -> "admin-attendance";
            case "/admin/salary" -> "admin-salary";
            case "/admin/logs" -> "admin-logs";
            case "/admin/apply/leave" -> "admin-leave";
            case "/admin/apply/trip" -> "admin-trip";
            case "/admin/apply/reimbursement" -> "admin-reimbursement";
            default -> "";
        };
    }
}
