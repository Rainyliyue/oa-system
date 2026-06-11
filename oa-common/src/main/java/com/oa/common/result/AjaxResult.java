package com.oa.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AjaxResult<T> {
    private Integer code;
    private String msg;
    private T data;

    public static <T> AjaxResult<T> success() {
        return new AjaxResult<>(0, "操作成功", null);
    }

    public static <T> AjaxResult<T> success(T data) {
        return new AjaxResult<>(0, "操作成功", data);
    }

    public static <T> AjaxResult<T> success(String msg, T data) {
        return new AjaxResult<>(0, msg, data);
    }

    public static <T> AjaxResult<T> error(String msg) {
        return new AjaxResult<>(1, msg, null);
    }

    public boolean ok() {
        return code != null && code == 0;
    }
}

