package com.oa.web.controller;

import com.oa.common.dto.DashboardStats;
import com.oa.common.dto.LoginUser;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.SystemNotice;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.web.feign.AttendanceFeignClient;
import com.oa.web.feign.LeaveFeignClient;
import com.oa.web.feign.ReimbursementFeignClient;
import com.oa.web.feign.TripFeignClient;
import com.oa.web.feign.WorkflowFeignClient;
import com.oa.web.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DashboardApiController {
    private final LeaveFeignClient leaveFeignClient;
    private final TripFeignClient tripFeignClient;
    private final ReimbursementFeignClient reimbursementFeignClient;
    private final AttendanceFeignClient attendanceFeignClient;
    private final WorkflowFeignClient workflowFeignClient;
    private final CurrentUser currentUser;

    public DashboardApiController(LeaveFeignClient leaveFeignClient,
                                  TripFeignClient tripFeignClient,
                                  ReimbursementFeignClient reimbursementFeignClient,
                                  AttendanceFeignClient attendanceFeignClient,
                                  WorkflowFeignClient workflowFeignClient,
                                  CurrentUser currentUser) {
        this.leaveFeignClient = leaveFeignClient;
        this.tripFeignClient = tripFeignClient;
        this.reimbursementFeignClient = reimbursementFeignClient;
        this.attendanceFeignClient = attendanceFeignClient;
        this.workflowFeignClient = workflowFeignClient;
        this.currentUser = currentUser;
    }

    @GetMapping("/dashboard/stats")
    public AjaxResult<DashboardStats> stats(HttpServletRequest request) {
        LoginUser user = currentUser.get(request);
        if (user == null) {
            return AjaxResult.error("请先登录");
        }

        DashboardStats stats = new DashboardStats();
        stats.setUnreadNoticeCount(value(workflowFeignClient.unreadNoticeCount(user.getId())));
        stats.setMonthlyAttendanceCount(monthlyAttendanceCount(user.getId()));
        stats.setMonthlyApplicationCount(monthlyApplicationCount(user.getId()));
        stats.setMyPendingCount(userApplicationStatusCount(user.getId(), "PENDING"));
        stats.setMyApprovedCount(userApplicationStatusCount(user.getId(), "APPROVED"));
        stats.setMyRejectedCount(userApplicationStatusCount(user.getId(), "REJECTED"));

        if (isAdmin(user)) {
            stats.setPendingApprovalCount(adminPendingApprovalCount());
        }
        return AjaxResult.success(stats);
    }

    @PostMapping("/notices/page")
    public PageResult<SystemNotice> notices(@RequestBody PageQuery query, HttpServletRequest request) {
        LoginUser user = currentUser.get(request);
        query.setUserId(user.getId());
        return workflowFeignClient.notices(query);
    }

    @PostMapping("/notices/{id}/read")
    public AjaxResult<Void> markNoticeRead(@PathVariable Long id, HttpServletRequest request) {
        LoginUser user = currentUser.get(request);
        return workflowFeignClient.markNoticeRead(id, user.getId());
    }

    @PostMapping("/notices/read-all")
    public AjaxResult<Void> markAllNoticeRead(HttpServletRequest request) {
        LoginUser user = currentUser.get(request);
        return workflowFeignClient.markAllNoticeRead(user.getId());
    }

    private long adminPendingApprovalCount() {
        PageQuery query = oneRowQuery();
        query.setStatus("PENDING");
        return safeCount(leaveFeignClient.adminPage(query))
                + safeCount(tripFeignClient.adminPage(query))
                + safeCount(reimbursementFeignClient.adminPage(query));
    }

    private long userApplicationStatusCount(Long userId, String status) {
        PageQuery query = oneRowQuery();
        query.setUserId(userId);
        query.setStatus(status);
        return safeCount(leaveFeignClient.userPage(query))
                + safeCount(tripFeignClient.userPage(query))
                + safeCount(reimbursementFeignClient.userPage(query));
    }

    private long monthlyApplicationCount(Long userId) {
        PageQuery query = currentMonthQuery(userId);
        return safeCount(leaveFeignClient.userPage(query))
                + safeCount(tripFeignClient.userPage(query))
                + safeCount(reimbursementFeignClient.userPage(query));
    }

    private long monthlyAttendanceCount(Long userId) {
        return safeCount(attendanceFeignClient.adminPage(currentMonthQuery(userId)));
    }

    private PageQuery currentMonthQuery(Long userId) {
        LocalDate now = LocalDate.now();
        PageQuery query = oneRowQuery();
        query.setUserId(userId);
        query.setStartDate(now.withDayOfMonth(1));
        query.setEndDate(now);
        return query;
    }

    private PageQuery oneRowQuery() {
        PageQuery query = new PageQuery();
        query.setPage(1);
        query.setLimit(1);
        return query;
    }

    private long safeCount(PageResult<?> result) {
        return result == null || result.getCount() == null ? 0 : result.getCount();
    }

    private long value(AjaxResult<Long> result) {
        return result == null || result.getData() == null ? 0 : result.getData();
    }

    private boolean isAdmin(LoginUser user) {
        List<String> roles = user.getRoleCodes();
        return roles != null && roles.contains("ADMIN");
    }
}
