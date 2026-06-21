package com.oa.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("oa_approval_history")
public class ApprovalHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String applyType;
    private Long applyId;
    private Long approverId;
    private String approverName;
    private Integer approvalLevel;
    private String result;
    private String auditComment;
    private LocalDateTime createTime;
}
