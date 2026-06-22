package com.oa.web.feign.fallback;

import com.oa.common.dto.PageQuery;
import com.oa.common.dto.UserForm;
import com.oa.common.entity.SysUser;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.web.feign.AdminUserFeignClient;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class AdminUserFeignFallbackFactory implements FallbackFactory<AdminUserFeignClient> {
    private static final Logger log = LoggerFactory.getLogger(AdminUserFeignFallbackFactory.class);

    @Override
    public AdminUserFeignClient create(Throwable cause) {
        return new AdminUserFeignClient() {
            @Override
            public PageResult<SysUser> page(PageQuery query) {
                return FeignFallbackSupport.page("用户管理服务", cause, log);
            }

            @Override
            public AjaxResult<Void> add(UserForm form) {
                return FeignFallbackSupport.ajax("用户管理服务", cause, log);
            }

            @Override
            public AjaxResult<Void> update(Long id, UserForm form) {
                return FeignFallbackSupport.ajax("用户管理服务", cause, log);
            }

            @Override
            public AjaxResult<Void> delete(Long id) {
                return FeignFallbackSupport.ajax("用户管理服务", cause, log);
            }

            @Override
            public AjaxResult<Void> roles(Long id, List<Long> roleIds) {
                return FeignFallbackSupport.ajax("用户管理服务", cause, log);
            }
        };
    }
}
