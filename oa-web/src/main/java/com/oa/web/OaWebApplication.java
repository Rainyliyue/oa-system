package com.oa.web;

import com.oa.common.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.oa.web.feign")
@SpringBootApplication(scanBasePackages = "com.oa")
@EnableConfigurationProperties(JwtProperties.class)
public class OaWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(OaWebApplication.class, args);
    }
}

