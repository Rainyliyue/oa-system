package com.oa.web.feign.fallback;

import com.oa.common.dto.AuthToken;
import com.oa.common.dto.LoginRequest;
import com.oa.common.dto.LoginUser;
import com.oa.common.dto.RegisterRequest;
import com.oa.common.result.AjaxResult;
import com.oa.web.feign.AuthFeignClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class AuthFeignFallbackFactory implements FallbackFactory<AuthFeignClient> {
    private static final Logger log = LoggerFactory.getLogger(AuthFeignFallbackFactory.class);

    @Override
    public AuthFeignClient create(Throwable cause) {
        return new AuthFeignClient() {
            @Override
            public AjaxResult<AuthToken> login(LoginRequest request) {
                return FeignFallbackSupport.ajax("用户认证服务", cause, log);
            }

            @Override
            public AjaxResult<Void> register(RegisterRequest request) {
                return FeignFallbackSupport.ajax("用户认证服务", cause, log);
            }

            @Override
            public AjaxResult<LoginUser> me() {
                return FeignFallbackSupport.ajax("用户认证服务", cause, log);
            }

            @Override
            public AjaxResult<Void> logout() {
                return FeignFallbackSupport.ajax("用户认证服务", cause, log);
            }
        };
    }
}
