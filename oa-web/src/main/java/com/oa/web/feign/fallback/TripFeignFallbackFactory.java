package com.oa.web.feign.fallback;

import com.oa.common.dto.ApprovalRequest;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.TripApply;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.web.feign.TripFeignClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class TripFeignFallbackFactory implements FallbackFactory<TripFeignClient> {
    private static final Logger log = LoggerFactory.getLogger(TripFeignFallbackFactory.class);

    @Override
    public TripFeignClient create(Throwable cause) {
        return new TripFeignClient() {
            @Override
            public PageResult<TripApply> userPage(PageQuery query) {
                return FeignFallbackSupport.page("出差申请服务", cause, log);
            }

            @Override
            public PageResult<TripApply> adminPage(PageQuery query) {
                return FeignFallbackSupport.page("出差申请服务", cause, log);
            }

            @Override
            public AjaxResult<Void> add(TripApply apply) {
                return FeignFallbackSupport.ajax("出差申请服务", cause, log);
            }

            @Override
            public AjaxResult<Void> userUpdate(Long id, Long userId, TripApply apply) {
                return FeignFallbackSupport.ajax("出差申请服务", cause, log);
            }

            @Override
            public AjaxResult<Void> userDelete(Long id, Long userId) {
                return FeignFallbackSupport.ajax("出差申请服务", cause, log);
            }

            @Override
            public AjaxResult<Void> adminUpdate(Long id, TripApply apply) {
                return FeignFallbackSupport.ajax("出差申请服务", cause, log);
            }

            @Override
            public AjaxResult<Void> adminDelete(Long id) {
                return FeignFallbackSupport.ajax("出差申请服务", cause, log);
            }

            @Override
            public AjaxResult<Void> approve(Long id, ApprovalRequest request) {
                return FeignFallbackSupport.ajax("出差申请服务", cause, log);
            }
        };
    }
}
