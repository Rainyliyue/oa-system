package com.oa.common.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private String department;
}

