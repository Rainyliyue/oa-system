package com.oa.web.controller;

import com.oa.common.dto.DashboardStats;
import com.oa.common.dto.LoginUser;
import com.oa.common.dto.NoticeItem;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
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
    public PageResult<NoticeItem> notices(@RequestBody PageQuery query, HttpServletRequest request) {
        LoginUser user = currentUser.get(request);
        if (user == null) {
            return PageResult.empty();
        }
        query.setUserId(user.getId());
        List<NoticeItem> notices = new ArrayList<>();
        if (isAdmin(user)) {
            addPendingNotice(notices, "请假待审批", safeCount(() -> leaveFeignClient.adminPage(pendingQuery())), "/admin/apply/leave");
            addPendingNotice(notices, "出差待审批", safeCount(() -> tripFeignClient.adminPage(pendingQuery())), "/admin/apply/trip");
            addPendingNotice(notices, "报销待审批", safeCount(() -> reimbursementFeignClient.adminPage(pendingQuery())), "/admin/apply/reimbursement");
        }
        PageResult<SystemNotice> userNotices = safeNoticePage(query);
        if (userNotices != null && userNotices.getData() != null) {
            userNotices.getData().forEach(notice -> notices.add(toNoticeItem(notice, null)));
        }
        int page = query.safePage();
        int limit = query.safeLimit();
        int from = Math.min((page - 1) * limit, notices.size());
        int to = Math.min(from + limit, notices.size());
        return PageResult.success(notices.size(), notices.subList(from, to));
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
        return safeCount(() -> leaveFeignClient.adminPage(pendingQuery()))
                + safeCount(() -> tripFeignClient.adminPage(pendingQuery()))
                + safeCount(() -> reimbursementFeignClient.adminPage(pendingQuery()));
    }

    private long userApplicationStatusCount(Long userId, String status) {
        PageQuery query = oneRowQuery();
        query.setUserId(userId);
        query.setStatus(status);
        return safeCount(() -> leaveFeignClient.userPage(query))
                + safeCount(() -> tripFeignClient.userPage(query))
                + safeCount(() -> reimbursementFeignClient.userPage(query));
    }

    private long monthlyApplicationCount(Long userId) {
        PageQuery query = currentMonthQuery(userId);
        return safeCount(() -> leaveFeignClient.userPage(query))
                + safeCount(() -> tripFeignClient.userPage(query))
                + safeCount(() -> reimbursementFeignClient.userPage(query));
    }

    private long monthlyAttendanceCount(Long userId) {
        return safeCount(() -> attendanceFeignClient.adminPage(currentMonthQuery(userId)));
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

    private PageQuery pendingQuery() {
        PageQuery query = oneRowQuery();
        query.setStatus("PENDING");
        return query;
    }

    private void addPendingNotice(List<NoticeItem> notices, String title, Long count, String targetUrl) {
        long safeCount = count == null ? 0 : count;
        if (safeCount <= 0) {
            return;
        }
        NoticeItem notice = new NoticeItem();
        notice.setTitle(title);
        notice.setContent("当前有 " + safeCount + " 条记录等待处理。");
        notice.setReadFlag(false);
        notice.setCreateTime(LocalDateTime.now());
        notice.setTargetUrl(targetUrl);
        notices.add(notice);
    }

    private long safeCount(Supplier<PageResult<?>> supplier) {
        try {
            PageResult<?> result = supplier.get();
            return result == null || result.getCount() == null ? 0 : result.getCount();
        } catch (Exception exception) {
            return 0;
        }
    }

    private PageResult<SystemNotice> safeNoticePage(PageQuery query) {
        try {
            return workflowFeignClient.notices(query);
        } catch (Exception exception) {
            return PageResult.empty();
        }
    }

    private NoticeItem toNoticeItem(SystemNotice notice, String targetUrl) {
        NoticeItem item = new NoticeItem();
        item.setId(notice.getId());
        item.setTitle(notice.getTitle());
        item.setContent(notice.getContent());
        item.setReadFlag(notice.getReadFlag());
        item.setCreateTime(notice.getCreateTime());
        item.setTargetUrl(targetUrl);
        return item;
    }

    private long value(AjaxResult<Long> result) {
        return result == null || result.getData() == null ? 0 : result.getData();
    }

    private boolean isAdmin(LoginUser user) {
        List<String> roles = user.getRoleCodes();
        return roles != null && roles.contains("ADMIN");
    }
}
