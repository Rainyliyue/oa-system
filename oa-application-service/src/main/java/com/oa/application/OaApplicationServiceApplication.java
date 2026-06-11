package com.oa.application;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@MapperScan("com.oa.application.mapper")
@SpringBootApplication(scanBasePackages = "com.oa")
public class OaApplicationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OaApplicationServiceApplication.class, args);
    }
}

