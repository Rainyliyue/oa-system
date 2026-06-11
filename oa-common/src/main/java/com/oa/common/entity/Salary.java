package com.oa.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("oa_salary")
public class Salary {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String salaryMonth;
    private BigDecimal baseSalary;
    private BigDecimal bonus;
    private BigDecimal deduction;
    private BigDecimal totalSalary;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

