package com.oa.web.feign;

import com.oa.common.dto.AuthToken;
import com.oa.common.dto.LoginRequest;
import com.oa.common.dto.LoginUser;
import com.oa.common.dto.RegisterRequest;
import com.oa.common.result.AjaxResult;
import com.oa.web.feign.fallback.AuthFeignFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(contextId = "authFeignClient", value = "oa-user-service", path = "/auth",
        fallbackFactory = AuthFeignFallbackFactory.class)
public interface AuthFeignClient {
    @PostMapping("/login")
    AjaxResult<AuthToken> login(@RequestBody LoginRequest request);

    @PostMapping("/register")
    AjaxResult<Void> register(@RequestBody RegisterRequest request);

    @GetMapping("/me")
    AjaxResult<LoginUser> me();

    @PostMapping("/logout")
    AjaxResult<Void> logout();
}
