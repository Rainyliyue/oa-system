package com.oa.payroll;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@MapperScan("com.oa.payroll.mapper")
@SpringBootApplication(scanBasePackages = "com.oa")
public class OaPayrollServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OaPayrollServiceApplication.class, args);
    }
}

