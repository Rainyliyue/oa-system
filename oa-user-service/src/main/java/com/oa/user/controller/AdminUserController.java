package com.oa.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.common.dto.PageQuery;
import com.oa.common.dto.UserForm;
import com.oa.common.entity.SysRole;
import com.oa.common.entity.SysUser;
import com.oa.common.entity.SysUserRole;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.common.util.StringUtils;
import com.oa.user.mapper.RoleMapper;
import com.oa.user.mapper.UserMapper;
import com.oa.user.mapper.UserRoleMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminUserController(UserMapper userMapper,
                               RoleMapper roleMapper,
                               UserRoleMapper userRoleMapper,
                               BCryptPasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/page")
    public PageResult<SysUser> page(@RequestBody PageQuery query) {
        Page<SysUser> page = new Page<>(query.safePage(), query.safeLimit());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .orderByDesc(SysUser::getCreateTime);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SysUser::getUsername, query.getKeyword())
                    .or()
                    .like(SysUser::getRealName, query.getKeyword())
                    .or()
                    .like(SysUser::getDepartment, query.getKeyword()));
        }
        Page<SysUser> result = userMapper.selectPage(page, wrapper);
        result.getRecords().forEach(this::fillUserSafeFields);
        return PageResult.success(result.getTotal(), result.getRecords());
    }

    @GetMapping("/{id}")
    public AjaxResult<SysUser> get(@PathVariable Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            return AjaxResult.error("用户不存在");
        }
        fillUserSafeFields(user);
        return AjaxResult.success(user);
    }

    @PostMapping
    public AjaxResult<Void> add(@RequestBody UserForm form) {
        if (!StringUtils.hasText(form.getUsername()) || !StringUtils.hasText(form.getPassword())) {
            return AjaxResult.error("用户名和密码不能为空");
        }
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, form.getUsername()));
        if (count > 0) {
            return AjaxResult.error("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(form.getUsername().trim());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRealName(form.getRealName());
        user.setPhone(form.getPhone());
        user.setEmail(form.getEmail());
        user.setDepartment(form.getDepartment());
        user.setEnabled(form.getEnabled() == null || form.getEnabled());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        assignRoles(user.getId(), form.getRoleIds());
        return AjaxResult.success();
    }

    @PutMapping("/{id}")
    public AjaxResult<Void> update(@PathVariable Long id, @RequestBody UserForm form) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            return AjaxResult.error("用户不存在");
        }
        user.setRealName(form.getRealName());
        user.setPhone(form.getPhone());
        user.setEmail(form.getEmail());
        user.setDepartment(form.getDepartment());
        user.setEnabled(form.getEnabled() == null || form.getEnabled());
        if (StringUtils.hasText(form.getPassword())) {
            user.setPassword(passwordEncoder.encode(form.getPassword()));
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        assignRoles(id, form.getRoleIds());
        return AjaxResult.success();
    }

    @DeleteMapping("/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            return AjaxResult.error("用户不存在");
        }
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            return AjaxResult.error("默认管理员不能删除");
        }
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        userMapper.deleteById(id);
        return AjaxResult.success();
    }

    @PutMapping("/{id}/roles")
    public AjaxResult<Void> roles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        if (userMapper.selectById(id) == null) {
            return AjaxResult.error("用户不存在");
        }
        assignRoles(id, roleIds);
        return AjaxResult.success();
    }

    private void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds == null) {
            return;
        }
        for (Long roleId : roleIds) {
            if (roleMapper.selectById(roleId) == null) {
                continue;
            }
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }

    private void fillUserSafeFields(SysUser user) {
        user.setPassword(null);
        List<SysUserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, user.getId()));
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        user.setRoleIds(roleIds);
        if (!roleIds.isEmpty()) {
            List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
            user.setRoleCodes(roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList()));
        }
    }
}

