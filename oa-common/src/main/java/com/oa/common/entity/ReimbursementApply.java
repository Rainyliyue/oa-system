package com.oa.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("oa_reimbursement_apply")
public class ReimbursementApply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String title;
    private BigDecimal amount;
    private String detail;
    private String status;
    private String auditComment;
    private LocalDateTime approveTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

