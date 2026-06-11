package com.oa.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("oa_leave_apply")
public class LeaveApply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String reason;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal dayCount;
    private String status;
    private String auditComment;
    private LocalDateTime approveTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

