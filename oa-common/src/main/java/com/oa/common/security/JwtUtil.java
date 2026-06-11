package com.oa.common.security;

import com.oa.common.dto.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public final class JwtUtil {
    private JwtUtil() {
    }

    public static String generate(LoginUser user, String secret, long expirationSeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim("realName", user.getRealName())
                .claim("roles", String.join(",", user.getRoleCodes()))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(key(secret), SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims parse(String token, String secret) {
        return Jwts.parserBuilder()
                .setSigningKey(key(secret))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static LoginUser parseLoginUser(String token, String secret) {
        Claims claims = parse(token, secret);
        LoginUser user = new LoginUser();
        Object userId = claims.get("userId");
        if (userId instanceof Number number) {
            user.setId(number.longValue());
        } else if (userId != null) {
            user.setId(Long.valueOf(userId.toString()));
        }
        user.setUsername(claims.getSubject());
        user.setRealName((String) claims.get("realName"));
        String roles = (String) claims.get("roles");
        if (roles != null && !roles.isBlank()) {
            List<String> roleCodes = Arrays.stream(roles.split(","))
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toList());
            user.setRoleCodes(roleCodes);
        }
        return user;
    }

    private static Key key(String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}

