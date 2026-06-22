package com.oa.web.feign.fallback;

import com.oa.common.dto.PageQuery;
import com.oa.common.entity.SysRole;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.web.feign.RoleFeignClient;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class RoleFeignFallbackFactory implements FallbackFactory<RoleFeignClient> {
    private static final Logger log = LoggerFactory.getLogger(RoleFeignFallbackFactory.class);

    @Override
    public RoleFeignClient create(Throwable cause) {
        return new RoleFeignClient() {
            @Override
            public PageResult<SysRole> page(PageQuery query) {
                return FeignFallbackSupport.page("角色权限服务", cause, log);
            }

            @Override
            public AjaxResult<List<SysRole>> all() {
                return FeignFallbackSupport.ajaxSuccess(Collections.emptyList(), "角色权限服务", cause, log);
            }

            @Override
            public AjaxResult<Void> add(SysRole role) {
                return FeignFallbackSupport.ajax("角色权限服务", cause, log);
            }

            @Override
            public AjaxResult<Void> update(Long id, SysRole role) {
                return FeignFallbackSupport.ajax("角色权限服务", cause, log);
            }

            @Override
            public AjaxResult<Void> delete(Long id) {
                return FeignFallbackSupport.ajax("角色权限服务", cause, log);
            }

            @Override
            public AjaxResult<Void> permissions(Long id, List<Long> permissionIds) {
                return FeignFallbackSupport.ajax("角色权限服务", cause, log);
            }
        };
    }
}
