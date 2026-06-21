package com.oa.common.dto;

import lombok.Data;

@Data
public class ApprovalRequest {
    private Long id;
    private Boolean passed;
    private Boolean finalApproval = true;
    private String auditComment;
    private Long approverId;
    private String approverName;
}
