package com.oa.user.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminUserService(UserMapper userMapper,
                            RoleMapper roleMapper,
                            UserRoleMapper userRoleMapper,
                            BCryptPasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResult<SysUser> page(PageQuery query) {
        Page<SysUser> page = new Page<>(query.safePage(), query.safeLimit());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .orderByDesc(SysUser::getCreateTime);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SysUser::getUsername, query.getKeyword())
                    .or()
                    .like(SysUser::getRealName, query.getKeyword())
                    .or()
                    .like(SysUser::getDepartment, query.getKeyword())
                    .or()
                    .like(SysUser::getPhone, query.getKeyword())
                    .or()
                    .like(SysUser::getEmail, query.getKeyword()));
        }
        if (query.getEnabled() != null) {
            wrapper.eq(SysUser::getEnabled, query.getEnabled());
        }
        Page<SysUser> result = userMapper.selectPage(page, wrapper);
        result.getRecords().forEach(this::fillUserSafeFields);
        return PageResult.success(result.getTotal(), result.getRecords());
    }

    public AjaxResult<SysUser> get(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            return AjaxResult.error("用户不存在");
        }
        fillUserSafeFields(user);
        return AjaxResult.success(user);
    }

    @Transactional
    public AjaxResult<Void> add(UserForm form) {
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

    @Transactional
    public AjaxResult<Void> update(Long id, UserForm form) {
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

    @Transactional
    public AjaxResult<Void> delete(Long id) {
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

    @Transactional
    public AjaxResult<Void> roles(Long id, List<Long> roleIds) {
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
