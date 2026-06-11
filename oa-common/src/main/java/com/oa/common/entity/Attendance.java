package com.oa.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("oa_attendance")
public class Attendance {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private LocalDate workDate;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

