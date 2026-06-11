package com.oa.web.security;

import com.oa.common.dto.LoginUser;
import com.oa.common.security.JwtProperties;
import com.oa.common.security.JwtUtil;
import com.oa.common.security.TokenConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    private final JwtProperties jwtProperties;

    public CurrentUser(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public LoginUser get(HttpServletRequest request) {
        String token = token(request);
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            return JwtUtil.parseLoginUser(token, jwtProperties.getSecret());
        } catch (Exception ex) {
            return null;
        }
    }

    public String token(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith(TokenConstants.BEARER_PREFIX)) {
            return authorization.substring(TokenConstants.BEARER_PREFIX.length());
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (TokenConstants.COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}

