package com.oa.web.controller;

import com.oa.common.dto.PageQuery;
import com.oa.common.dto.UserForm;
import com.oa.common.entity.SysPermission;
import com.oa.common.entity.SysRole;
import com.oa.common.entity.SysUser;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.web.feign.AdminUserFeignClient;
import com.oa.web.feign.PermissionFeignClient;
import com.oa.web.feign.RoleFeignClient;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminUserApiController {
    private final AdminUserFeignClient userFeignClient;
    private final RoleFeignClient roleFeignClient;
    private final PermissionFeignClient permissionFeignClient;

    public AdminUserApiController(AdminUserFeignClient userFeignClient,
                                  RoleFeignClient roleFeignClient,
                                  PermissionFeignClient permissionFeignClient) {
        this.userFeignClient = userFeignClient;
        this.roleFeignClient = roleFeignClient;
        this.permissionFeignClient = permissionFeignClient;
    }

    @PostMapping("/users/page")
    public PageResult<SysUser> users(@RequestBody PageQuery query) {
        return userFeignClient.page(query);
    }

    @PostMapping("/users")
    public AjaxResult<Void> addUser(@RequestBody UserForm form) {
        return userFeignClient.add(form);
    }

    @PutMapping("/users/{id}")
    public AjaxResult<Void> updateUser(@PathVariable Long id, @RequestBody UserForm form) {
        return userFeignClient.update(id, form);
    }

    @DeleteMapping("/users/{id}")
    public AjaxResult<Void> deleteUser(@PathVariable Long id) {
        return userFeignClient.delete(id);
    }

    @PostMapping("/roles/page")
    public PageResult<SysRole> roles(@RequestBody PageQuery query) {
        return roleFeignClient.page(query);
    }

    @PostMapping("/roles")
    public AjaxResult<Void> addRole(@RequestBody SysRole role) {
        return roleFeignClient.add(role);
    }

    @PutMapping("/roles/{id}")
    public AjaxResult<Void> updateRole(@PathVariable Long id, @RequestBody SysRole role) {
        return roleFeignClient.update(id, role);
    }

    @DeleteMapping("/roles/{id}")
    public AjaxResult<Void> deleteRole(@PathVariable Long id) {
        return roleFeignClient.delete(id);
    }

    @PutMapping("/roles/{id}/permissions")
    public AjaxResult<Void> rolePermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        return roleFeignClient.permissions(id, permissionIds);
    }

    @PostMapping("/permissions/page")
    public PageResult<SysPermission> permissions(@RequestBody PageQuery query) {
        return permissionFeignClient.page(query);
    }

    @PostMapping("/permissions")
    public AjaxResult<Void> addPermission(@RequestBody SysPermission permission) {
        return permissionFeignClient.add(permission);
    }

    @PutMapping("/permissions/{id}")
    public AjaxResult<Void> updatePermission(@PathVariable Long id, @RequestBody SysPermission permission) {
        return permissionFeignClient.update(id, permission);
    }

    @DeleteMapping("/permissions/{id}")
    public AjaxResult<Void> deletePermission(@PathVariable Long id) {
        return permissionFeignClient.delete(id);
    }

    @PostMapping("/roles/all")
    public AjaxResult<List<SysRole>> allRoles() {
        return roleFeignClient.all();
    }

    @PostMapping("/permissions/all")
    public AjaxResult<List<SysPermission>> allPermissions() {
        return permissionFeignClient.all();
    }
}

