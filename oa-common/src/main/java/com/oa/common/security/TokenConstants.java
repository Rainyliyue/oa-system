package com.oa.common.security;

public final class TokenConstants {
    public static final String COOKIE_NAME = "OA_TOKEN";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String REDIS_TOKEN_PREFIX = "oa:token:";

    private TokenConstants() {
    }

    public static String redisKey(String token) {
        return REDIS_TOKEN_PREFIX + token;
    }
}

