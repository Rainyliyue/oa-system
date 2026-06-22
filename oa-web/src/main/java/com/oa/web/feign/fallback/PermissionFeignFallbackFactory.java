package com.oa.web.feign.fallback;

import com.oa.common.dto.PageQuery;
import com.oa.common.entity.SysPermission;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.web.feign.PermissionFeignClient;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class PermissionFeignFallbackFactory implements FallbackFactory<PermissionFeignClient> {
    private static final Logger log = LoggerFactory.getLogger(PermissionFeignFallbackFactory.class);

    @Override
    public PermissionFeignClient create(Throwable cause) {
        return new PermissionFeignClient() {
            @Override
            public PageResult<SysPermission> page(PageQuery query) {
                return FeignFallbackSupport.page("权限资源服务", cause, log);
            }

            @Override
            public AjaxResult<List<SysPermission>> all() {
                return FeignFallbackSupport.ajaxSuccess(Collections.emptyList(), "权限资源服务", cause, log);
            }

            @Override
            public AjaxResult<Void> add(SysPermission permission) {
                return FeignFallbackSupport.ajax("权限资源服务", cause, log);
            }

            @Override
            public AjaxResult<Void> update(Long id, SysPermission permission) {
                return FeignFallbackSupport.ajax("权限资源服务", cause, log);
            }

            @Override
            public AjaxResult<Void> delete(Long id) {
                return FeignFallbackSupport.ajax("权限资源服务", cause, log);
            }
        };
    }
}
