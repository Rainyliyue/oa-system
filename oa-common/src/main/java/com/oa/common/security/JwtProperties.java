package com.oa.common.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "oa.jwt")
public class JwtProperties {
    private String secret = "oa-system-course-design-secret-key-change-me";
    private Long expirationSeconds = 7200L;
}

