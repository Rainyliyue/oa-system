package com.oa.common.enums;

public enum ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    FINISHED;

    public boolean canUserEdit() {
        return this == PENDING || this == REJECTED;
    }

    public boolean canAdminApprove() {
        return this == PENDING;
    }
}

