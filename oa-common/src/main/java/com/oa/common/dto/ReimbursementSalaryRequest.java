package com.oa.common.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ReimbursementSalaryRequest {
    private Long userId;
    private String username;
    private String salaryMonth;
    private BigDecimal amount;
    private Long reimbursementId;
    private String reimbursementTitle;
    private String remark;
}
