package com.oa.common.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class PageQuery {
    private Integer page = 1;
    private Integer limit = 10;
    private String keyword;
    private String status;
    private Long userId;
    private Boolean enabled;
    private String type;
    private String salaryMonth;
    private LocalDate startDate;
    private LocalDate endDate;

    public long offset() {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeLimit = limit == null || limit < 1 ? 10 : limit;
        return (long) (safePage - 1) * safeLimit;
    }

    public int safePage() {
        return page == null || page < 1 ? 1 : page;
    }

    public int safeLimit() {
        return limit == null || limit < 1 ? 10 : limit;
    }
}
