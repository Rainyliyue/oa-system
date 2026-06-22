package com.oa.web.feign.fallback;

import com.oa.common.dto.ApprovalRequest;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.LeaveApply;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.web.feign.LeaveFeignClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class LeaveFeignFallbackFactory implements FallbackFactory<LeaveFeignClient> {
    private static final Logger log = LoggerFactory.getLogger(LeaveFeignFallbackFactory.class);

    @Override
    public LeaveFeignClient create(Throwable cause) {
        return new LeaveFeignClient() {
            @Override
            public PageResult<LeaveApply> userPage(PageQuery query) {
                return FeignFallbackSupport.page("请假申请服务", cause, log);
            }

            @Override
            public PageResult<LeaveApply> adminPage(PageQuery query) {
                return FeignFallbackSupport.page("请假申请服务", cause, log);
            }

            @Override
            public AjaxResult<Void> add(LeaveApply apply) {
                return FeignFallbackSupport.ajax("请假申请服务", cause, log);
            }

            @Override
            public AjaxResult<Void> userUpdate(Long id, Long userId, LeaveApply apply) {
                return FeignFallbackSupport.ajax("请假申请服务", cause, log);
            }

            @Override
            public AjaxResult<Void> userDelete(Long id, Long userId) {
                return FeignFallbackSupport.ajax("请假申请服务", cause, log);
            }

            @Override
            public AjaxResult<Void> adminUpdate(Long id, LeaveApply apply) {
                return FeignFallbackSupport.ajax("请假申请服务", cause, log);
            }

            @Override
            public AjaxResult<Void> adminDelete(Long id) {
                return FeignFallbackSupport.ajax("请假申请服务", cause, log);
            }

            @Override
            public AjaxResult<Void> approve(Long id, ApprovalRequest request) {
                return FeignFallbackSupport.ajax("请假申请服务", cause, log);
            }
        };
    }
}
