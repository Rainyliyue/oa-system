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
import com.oa.web.support.AdminOperationLogger;
import jakarta.servlet.http.HttpServletRequest;
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
    private final AdminOperationLogger operationLogger;

    public AdminUserApiController(AdminUserFeignClient userFeignClient,
                                  RoleFeignClient roleFeignClient,
                                  PermissionFeignClient permissionFeignClient,
                                  AdminOperationLogger operationLogger) {
        this.userFeignClient = userFeignClient;
        this.roleFeignClient = roleFeignClient;
        this.permissionFeignClient = permissionFeignClient;
        this.operationLogger = operationLogger;
    }

    @PostMapping("/users/page")
    public PageResult<SysUser> users(@RequestBody PageQuery query) {
        return userFeignClient.page(query);
    }

    @PostMapping("/users")
    public AjaxResult<Void> addUser(@RequestBody UserForm form, HttpServletRequest request) {
        AjaxResult<Void> result = userFeignClient.add(form);
        operationLogger.logIfSuccess(request, result, "用户管理", "CREATE", "sys_user", null, "新增用户：" + form.getUsername());
        return result;
    }

    @PutMapping("/users/{id}")
    public AjaxResult<Void> updateUser(@PathVariable Long id, @RequestBody UserForm form, HttpServletRequest request) {
        AjaxResult<Void> result = userFeignClient.update(id, form);
        operationLogger.logIfSuccess(request, result, "用户管理", "UPDATE", "sys_user", id, "修改用户：" + id);
        return result;
    }

    @DeleteMapping("/users/{id}")
    public AjaxResult<Void> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        AjaxResult<Void> result = userFeignClient.delete(id);
        operationLogger.logIfSuccess(request, result, "用户管理", "DELETE", "sys_user", id, "删除用户：" + id);
        return result;
    }

    @PostMapping("/roles/page")
    public PageResult<SysRole> roles(@RequestBody PageQuery query) {
        return roleFeignClient.page(query);
    }

    @PostMapping("/roles")
    public AjaxResult<Void> addRole(@RequestBody SysRole role, HttpServletRequest request) {
        AjaxResult<Void> result = roleFeignClient.add(role);
        operationLogger.logIfSuccess(request, result, "角色管理", "CREATE", "sys_role", null, "新增角色：" + role.getRoleCode());
        return result;
    }

    @PutMapping("/roles/{id}")
    public AjaxResult<Void> updateRole(@PathVariable Long id, @RequestBody SysRole role, HttpServletRequest request) {
        AjaxResult<Void> result = roleFeignClient.update(id, role);
        operationLogger.logIfSuccess(request, result, "角色管理", "UPDATE", "sys_role", id, "修改角色：" + id);
        return result;
    }

    @DeleteMapping("/roles/{id}")
    public AjaxResult<Void> deleteRole(@PathVariable Long id, HttpServletRequest request) {
        AjaxResult<Void> result = roleFeignClient.delete(id);
        operationLogger.logIfSuccess(request, result, "角色管理", "DELETE", "sys_role", id, "删除角色：" + id);
        return result;
    }

    @PutMapping("/roles/{id}/permissions")
    public AjaxResult<Void> rolePermissions(@PathVariable Long id,
                                            @RequestBody List<Long> permissionIds,
                                            HttpServletRequest request) {
        AjaxResult<Void> result = roleFeignClient.permissions(id, permissionIds);
        operationLogger.logIfSuccess(request, result, "角色管理", "ASSIGN_PERMISSION", "sys_role", id, "分配角色权限：" + id);
        return result;
    }

    @PostMapping("/permissions/page")
    public PageResult<SysPermission> permissions(@RequestBody PageQuery query) {
        return permissionFeignClient.page(query);
    }

    @PostMapping("/permissions")
    public AjaxResult<Void> addPermission(@RequestBody SysPermission permission, HttpServletRequest request) {
        AjaxResult<Void> result = permissionFeignClient.add(permission);
        operationLogger.logIfSuccess(request, result, "权限资源", "CREATE", "sys_permission", null, "新增权限：" + permission.getPermissionCode());
        return result;
    }

    @PutMapping("/permissions/{id}")
    public AjaxResult<Void> updatePermission(@PathVariable Long id,
                                             @RequestBody SysPermission permission,
                                             HttpServletRequest request) {
        AjaxResult<Void> result = permissionFeignClient.update(id, permission);
        operationLogger.logIfSuccess(request, result, "权限资源", "UPDATE", "sys_permission", id, "修改权限：" + id);
        return result;
    }

    @DeleteMapping("/permissions/{id}")
    public AjaxResult<Void> deletePermission(@PathVariable Long id, HttpServletRequest request) {
        AjaxResult<Void> result = permissionFeignClient.delete(id);
        operationLogger.logIfSuccess(request, result, "权限资源", "DELETE", "sys_permission", id, "删除权限：" + id);
        return result;
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
