package com.oa.common.config;

import com.oa.common.trace.TraceConstants;
import com.oa.common.util.StringUtils;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignTraceInterceptor {

    @Bean
    public RequestInterceptor traceIdRelayInterceptor() {
        return template -> template.header(TraceConstants.TRACE_ID_HEADER, currentTraceId());
    }

    private String currentTraceId() {
        String traceId = MDC.get(TraceConstants.TRACE_ID_MDC_KEY);
        if (StringUtils.hasText(traceId)) {
            return traceId;
        }
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            traceId = request.getHeader(TraceConstants.TRACE_ID_HEADER);
            if (StringUtils.hasText(traceId)) {
                MDC.put(TraceConstants.TRACE_ID_MDC_KEY, traceId);
                return traceId;
            }
        }
        traceId = UUID.randomUUID().toString().replace("-", "");
        MDC.put(TraceConstants.TRACE_ID_MDC_KEY, traceId);
        return traceId;
    }
}
