package com.oa.attendance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@MapperScan("com.oa.attendance.mapper")
@SpringBootApplication(scanBasePackages = "com.oa")
public class OaAttendanceServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OaAttendanceServiceApplication.class, args);
    }
}

