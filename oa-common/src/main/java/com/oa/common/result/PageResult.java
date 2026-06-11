package com.oa.common.result;

import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private Integer code;
    private String msg;
    private Long count;
    private List<T> data;

    public static <T> PageResult<T> success(long count, List<T> data) {
        return new PageResult<>(0, "查询成功", count, data);
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>(0, "查询成功", 0L, Collections.emptyList());
    }
}

