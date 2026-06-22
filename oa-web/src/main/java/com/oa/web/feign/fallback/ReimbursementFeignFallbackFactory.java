package com.oa.web.feign.fallback;

import com.oa.common.dto.ApprovalRequest;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.ReimbursementApply;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.web.feign.ReimbursementFeignClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class ReimbursementFeignFallbackFactory implements FallbackFactory<ReimbursementFeignClient> {
    private static final Logger log = LoggerFactory.getLogger(ReimbursementFeignFallbackFactory.class);

    @Override
    public ReimbursementFeignClient create(Throwable cause) {
        return new ReimbursementFeignClient() {
            @Override
            public PageResult<ReimbursementApply> userPage(PageQuery query) {
                return FeignFallbackSupport.page("报销申请服务", cause, log);
            }

            @Override
            public PageResult<ReimbursementApply> adminPage(PageQuery query) {
                return FeignFallbackSupport.page("报销申请服务", cause, log);
            }

            @Override
            public AjaxResult<ReimbursementApply> adminDetail(Long id) {
                return FeignFallbackSupport.ajax("报销申请服务", cause, log);
            }

            @Override
            public AjaxResult<Void> add(ReimbursementApply apply) {
                return FeignFallbackSupport.ajax("报销申请服务", cause, log);
            }

            @Override
            public AjaxResult<Void> userUpdate(Long id, Long userId, ReimbursementApply apply) {
                return FeignFallbackSupport.ajax("报销申请服务", cause, log);
            }

            @Override
            public AjaxResult<Void> userDelete(Long id, Long userId) {
                return FeignFallbackSupport.ajax("报销申请服务", cause, log);
            }

            @Override
            public AjaxResult<Void> adminUpdate(Long id, ReimbursementApply apply) {
                return FeignFallbackSupport.ajax("报销申请服务", cause, log);
            }

            @Override
            public AjaxResult<Void> adminDelete(Long id) {
                return FeignFallbackSupport.ajax("报销申请服务", cause, log);
            }

            @Override
            public AjaxResult<Void> approve(Long id, ApprovalRequest request) {
                return FeignFallbackSupport.ajax("报销申请服务", cause, log);
            }
        };
    }
}
