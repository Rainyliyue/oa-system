package com.oa.common.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class UserForm {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private String department;
    private Boolean enabled = true;
    private List<Long> roleIds = new ArrayList<>();
}

