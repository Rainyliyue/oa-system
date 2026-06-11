package com.oa.user.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oa.common.entity.SysPermission;
import com.oa.common.entity.SysRole;
import com.oa.common.entity.SysRolePermission;
import com.oa.common.entity.SysUser;
import com.oa.common.entity.SysUserRole;
import com.oa.user.mapper.PermissionMapper;
import com.oa.user.mapper.RoleMapper;
import com.oa.user.mapper.RolePermissionMapper;
import com.oa.user.mapper.UserMapper;
import com.oa.user.mapper.UserRoleMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(UserMapper userMapper,
                           RoleMapper roleMapper,
                           PermissionMapper permissionMapper,
                           UserRoleMapper userRoleMapper,
                           RolePermissionMapper rolePermissionMapper,
                           BCryptPasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        SysRole adminRole = ensureRole("ADMIN", "管理员");
        SysRole userRole = ensureRole("USER", "普通用户");
        List<SysPermission> permissions = List.of(
                ensurePermission("USER_MANAGE", "用户管理", "/admin/users"),
                ensurePermission("ROLE_MANAGE", "角色管理", "/admin/roles"),
                ensurePermission("PERMISSION_MANAGE", "权限管理", "/admin/permissions"),
                ensurePermission("ATTENDANCE_MANAGE", "考勤管理", "/admin/attendance"),
                ensurePermission("SALARY_MANAGE", "工资管理", "/admin/salary"),
                ensurePermission("APPLY_MANAGE", "申请审批", "/admin/apply")
        );
        ensureRolePermissions(adminRole, permissions);
        ensureUser("admin", "系统管理员", "综合管理部", adminRole);
        ensureUser("user", "普通用户", "研发部", userRole);
    }

    private SysRole ensureRole(String code, String name) {
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, code));
        if (role != null) {
            return role;
        }
        role = new SysRole();
        role.setRoleCode(code);
        role.setRoleName(name);
        role.setRemark("系统初始化角色");
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.insert(role);
        return role;
    }

    private SysPermission ensurePermission(String code, String name, String path) {
        SysPermission permission = permissionMapper.selectOne(new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getPermissionCode, code));
        if (permission != null) {
            return permission;
        }
        permission = new SysPermission();
        permission.setPermissionCode(code);
        permission.setPermissionName(name);
        permission.setPath(path);
        permission.setType("MENU");
        permission.setCreateTime(LocalDateTime.now());
        permission.setUpdateTime(LocalDateTime.now());
        permissionMapper.insert(permission);
        return permission;
    }

    private void ensureRolePermissions(SysRole role, List<SysPermission> permissions) {
        for (SysPermission permission : permissions) {
            Long count = rolePermissionMapper.selectCount(new LambdaQueryWrapper<SysRolePermission>()
                    .eq(SysRolePermission::getRoleId, role.getId())
                    .eq(SysRolePermission::getPermissionId, permission.getId()));
            if (count == 0) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(role.getId());
                rp.setPermissionId(permission.getId());
                rolePermissionMapper.insert(rp);
            }
        }
    }

    private void ensureUser(String username, String realName, String department, SysRole role) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            user = new SysUser();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRealName(realName);
            user.setDepartment(department);
            user.setEnabled(true);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            userMapper.insert(user);
        }
        Long count = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, user.getId())
                .eq(SysUserRole::getRoleId, role.getId()));
        if (count == 0) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(role.getId());
            userRoleMapper.insert(userRole);
        }
    }
}
