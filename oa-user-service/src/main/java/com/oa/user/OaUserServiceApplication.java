package com.oa.user;

import com.oa.common.security.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@MapperScan("com.oa.user.mapper")
@SpringBootApplication(scanBasePackages = "com.oa")
@EnableConfigurationProperties(JwtProperties.class)
public class OaUserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OaUserServiceApplication.class, args);
    }
}

