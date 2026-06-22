package com.oa.web.feign.fallback;

import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import java.util.Collections;
import org.slf4j.Logger;

final class FeignFallbackSupport {
    private FeignFallbackSupport() {
    }

    static <T> AjaxResult<T> ajax(String serviceName, Throwable cause, Logger log) {
        log.warn("{} 调用失败，进入 Feign fallback：{}", serviceName, message(cause));
        return AjaxResult.error(serviceName + "暂不可用，请稍后重试");
    }

    static <T> AjaxResult<T> ajaxSuccess(T data, String serviceName, Throwable cause, Logger log) {
        log.warn("{} 调用失败，使用默认降级数据：{}", serviceName, message(cause));
        return AjaxResult.success(data);
    }

    static <T> PageResult<T> page(String serviceName, Throwable cause, Logger log) {
        log.warn("{} 分页查询失败，进入 Feign fallback：{}", serviceName, message(cause));
        return new PageResult<>(1, serviceName + "暂不可用，请稍后重试", 0L, Collections.emptyList());
    }

    private static String message(Throwable cause) {
        return cause == null ? "unknown" : cause.getClass().getSimpleName() + ": " + cause.getMessage();
    }
}
