package com.oa.gateway.filter;

import com.oa.common.dto.LoginUser;
import com.oa.common.security.JwtProperties;
import com.oa.common.security.JwtUtil;
import com.oa.common.security.TokenConstants;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/login", "/register", "/doLogin", "/doRegister", "/auth/",
            "/layui/", "/css/", "/js/", "/images/", "/favicon.ico", "/error"
    );

    private final ReactiveStringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    public AuthGlobalFilter(ReactiveStringRedisTemplate redisTemplate, JwtProperties jwtProperties) {
        this.redisTemplate = redisTemplate;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String token = resolveToken(exchange.getRequest());
        if (token == null || token.isBlank()) {
            return unauthorized(exchange, "请先登录");
        }

        LoginUser user;
        try {
            user = JwtUtil.parseLoginUser(token, jwtProperties.getSecret());
        } catch (Exception ex) {
            return unauthorized(exchange, "登录状态已失效");
        }

        if (isAdminPath(path) && !user.hasRole("ADMIN")) {
            return forbidden(exchange, "没有管理员权限");
        }

        return redisTemplate.hasKey(TokenConstants.redisKey(token))
                .flatMap(exists -> {
                    if (Boolean.FALSE.equals(exists)) {
                        return unauthorized(exchange, "登录状态已过期");
                    }
                    ServerHttpRequest request = exchange.getRequest().mutate()
                            .header(HttpHeaders.AUTHORIZATION, TokenConstants.BEARER_PREFIX + token)
                            .header("X-User-Id", String.valueOf(user.getId()))
                            .header("X-Username", user.getUsername())
                            .build();
                    return chain.filter(exchange.mutate().request(request).build());
                });
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublic(String path) {
        return PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private boolean isAdminPath(String path) {
        return path.startsWith("/admin") || path.startsWith("/api/admin");
    }

    private String resolveToken(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith(TokenConstants.BEARER_PREFIX)) {
            return authorization.substring(TokenConstants.BEARER_PREFIX.length());
        }
        HttpCookie cookie = request.getCookies().getFirst(TokenConstants.COOKIE_NAME);
        return cookie == null ? null : cookie.getValue();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return write(exchange, "{\"code\":401,\"msg\":\"" + message + "\"}");
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return write(exchange, "{\"code\":403,\"msg\":\"" + message + "\"}");
    }

    private Mono<Void> write(ServerWebExchange exchange, String body) {
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}

