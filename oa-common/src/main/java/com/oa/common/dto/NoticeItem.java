package com.oa.common.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NoticeItem {
    private Long id;
    private String title;
    private String content;
    private Boolean readFlag;
    private LocalDateTime createTime;
    private String targetUrl;
}
