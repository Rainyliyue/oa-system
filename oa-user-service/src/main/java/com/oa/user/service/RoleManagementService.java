package com.oa.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.SysRole;
import com.oa.common.entity.SysRolePermission;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.common.util.StringUtils;
import com.oa.user.mapper.RoleMapper;
import com.oa.user.mapper.RolePermissionMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleManagementService {
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;

    public RoleManagementService(RoleMapper roleMapper, RolePermissionMapper rolePermissionMapper) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    public PageResult<SysRole> page(PageQuery query) {
        Page<SysRole> page = new Page<>(query.safePage(), query.safeLimit());
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .orderByDesc(SysRole::getCreateTime);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SysRole::getRoleCode, query.getKeyword())
                    .or()
                    .like(SysRole::getRoleName, query.getKeyword())
                    .or()
                    .like(SysRole::getRemark, query.getKeyword()));
        }
        Page<SysRole> result = roleMapper.selectPage(page, wrapper);
        result.getRecords().forEach(this::fillPermissionIds);
        return PageResult.success(result.getTotal(), result.getRecords());
    }

    public AjaxResult<List<SysRole>> all() {
        return AjaxResult.success(roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .orderByAsc(SysRole::getId)));
    }

    @Transactional
    public AjaxResult<Void> add(SysRole role) {
        if (!StringUtils.hasText(role.getRoleCode()) || !StringUtils.hasText(role.getRoleName())) {
            return AjaxResult.error("角色编码和名称不能为空");
        }
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.insert(role);
        assignPermissions(role.getId(), role.getPermissionIds());
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> update(Long id, SysRole role) {
        SysRole old = roleMapper.selectById(id);
        if (old == null) {
            return AjaxResult.error("角色不存在");
        }
        old.setRoleName(role.getRoleName());
        old.setRemark(role.getRemark());
        old.setUpdateTime(LocalDateTime.now());
        roleMapper.updateById(old);
        assignPermissions(id, role.getPermissionIds());
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> delete(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            return AjaxResult.error("角色不存在");
        }
        if ("ADMIN".equals(role.getRoleCode()) || "USER".equals(role.getRoleCode())) {
            return AjaxResult.error("系统默认角色不能删除");
        }
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id));
        roleMapper.deleteById(id);
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> permissions(Long id, List<Long> permissionIds) {
        if (roleMapper.selectById(id) == null) {
            return AjaxResult.error("角色不存在");
        }
        assignPermissions(id, permissionIds);
        return AjaxResult.success();
    }

    private void assignPermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId));
        if (permissionIds == null) {
            return;
        }
        for (Long permissionId : permissionIds) {
            SysRolePermission rp = new SysRolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permissionId);
            rolePermissionMapper.insert(rp);
        }
    }

    private void fillPermissionIds(SysRole role) {
        List<SysRolePermission> list = rolePermissionMapper.selectList(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, role.getId()));
        role.setPermissionIds(list.stream().map(SysRolePermission::getPermissionId).collect(Collectors.toList()));
    }
}
