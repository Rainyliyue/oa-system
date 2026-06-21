package com.oa.common.dto;

import lombok.Data;

@Data
public class OperationLogRequest {
    private Long operatorId;
    private String operatorName;
    private String moduleName;
    private String operationType;
    private String targetType;
    private Long targetId;
    private String content;
}
