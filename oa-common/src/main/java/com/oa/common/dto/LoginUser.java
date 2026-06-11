package com.oa.common.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class LoginUser implements Serializable {
    private Long id;
    private String username;
    private String realName;
    private List<String> roleCodes = new ArrayList<>();

    public boolean hasRole(String roleCode) {
        return roleCodes != null && roleCodes.contains(roleCode);
    }
}

