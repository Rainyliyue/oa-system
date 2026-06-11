package com.oa.web.controller;

import com.oa.common.dto.LoginUser;
import com.oa.web.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {
    private final CurrentUser currentUser;

    public ViewController(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/index";
    }

    @GetMapping("/index")
    public String index(HttpServletRequest request, Model model) {
        LoginUser user = currentUser.get(request);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        model.addAttribute("isAdmin", user.hasRole("ADMIN"));
        return "index";
    }

    @GetMapping("/user/apply/{type}")
    public String userApply(@PathVariable String type, Model model) {
        fillApplyModel(type, false, model);
        return "apply";
    }

    @GetMapping("/admin/apply/{type}")
    public String adminApply(@PathVariable String type, Model model) {
        fillApplyModel(type, true, model);
        return "apply";
    }

    @GetMapping("/user/attendance")
    public String userAttendance() {
        return "attendance-user";
    }

    @GetMapping("/admin/users")
    public String users() {
        return "admin-users";
    }

    @GetMapping("/admin/roles")
    public String roles() {
        return "admin-roles";
    }

    @GetMapping("/admin/permissions")
    public String permissions() {
        return "admin-permissions";
    }

    @GetMapping("/admin/attendance")
    public String attendance() {
        return "admin-attendance";
    }

    @GetMapping("/admin/salary")
    public String salary() {
        return "admin-salary";
    }

    private void fillApplyModel(String type, boolean admin, Model model) {
        model.addAttribute("type", type);
        model.addAttribute("admin", admin);
        model.addAttribute("title", switch (type) {
            case "leave" -> "请假申请";
            case "trip" -> "出差申请";
            case "reimbursement" -> "报销申请";
            default -> "申请管理";
        });
    }
}

