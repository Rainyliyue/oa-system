package com.oa.web.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.oa.common.dto.AuthToken;
import com.oa.common.dto.LoginRequest;
import com.oa.common.dto.RegisterRequest;
import com.oa.common.result.AjaxResult;
import com.oa.common.security.TokenConstants;
import com.oa.web.feign.AuthFeignClient;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthPageController {
    private final AuthFeignClient authFeignClient;

    public AuthPageController(AuthFeignClient authFeignClient) {
        this.authFeignClient = authFeignClient;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/doLogin")
    @ResponseBody
    @SentinelResource(value = "auth:login", blockHandler = "doLoginBlocked", fallback = "doLoginFallback")
    public AjaxResult<AuthToken> doLogin(@RequestBody LoginRequest request, HttpServletResponse response) {
        AjaxResult<AuthToken> result = authFeignClient.login(request);
        if (result.ok() && result.getData() != null) {
            Cookie cookie = new Cookie(TokenConstants.COOKIE_NAME, result.getData().getToken());
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(result.getData().getExpiresIn().intValue());
            response.addCookie(cookie);
        }
        return result;
    }

    public AjaxResult<AuthToken> doLoginBlocked(LoginRequest request,
                                                HttpServletResponse response,
                                                BlockException exception) {
        return AjaxResult.error("登录请求过于频繁，请稍后再试");
    }

    public AjaxResult<AuthToken> doLoginFallback(LoginRequest request,
                                                 HttpServletResponse response,
                                                 Throwable throwable) {
        return AjaxResult.error("登录服务暂不可用，请稍后重试");
    }

    @PostMapping("/doRegister")
    @ResponseBody
    public AjaxResult<Void> doRegister(@RequestBody RegisterRequest request) {
        return authFeignClient.register(request);
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        try {
            authFeignClient.logout();
        } catch (Exception ignored) {
            // Cookie cleanup should still happen when the backend is unavailable.
        }
        Cookie cookie = new Cookie(TokenConstants.COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/login";
    }
}
