package com.oa.common.dto;

import lombok.Data;

@Data
public class DashboardStats {
    private long pendingApprovalCount;
    private long unreadNoticeCount;
    private long monthlyAttendanceCount;
    private long monthlyApplicationCount;
    private long myPendingCount;
    private long myApprovedCount;
    private long myRejectedCount;
}
