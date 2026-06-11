package com.oa.user.controller;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/roles")
public class RoleController {
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;

    public RoleController(RoleMapper roleMapper, RolePermissionMapper rolePermissionMapper) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    @PostMapping("/page")
    public PageResult<SysRole> page(@RequestBody PageQuery query) {
        Page<SysRole> page = new Page<>(query.safePage(), query.safeLimit());
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .orderByDesc(SysRole::getCreateTime);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SysRole::getRoleCode, query.getKeyword())
                    .or()
                    .like(SysRole::getRoleName, query.getKeyword()));
        }
        Page<SysRole> result = roleMapper.selectPage(page, wrapper);
        result.getRecords().forEach(this::fillPermissionIds);
        return PageResult.success(result.getTotal(), result.getRecords());
    }

    @GetMapping("/all")
    public AjaxResult<List<SysRole>> all() {
        return AjaxResult.success(roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .orderByAsc(SysRole::getId)));
    }

    @PostMapping
    public AjaxResult<Void> add(@RequestBody SysRole role) {
        if (!StringUtils.hasText(role.getRoleCode()) || !StringUtils.hasText(role.getRoleName())) {
            return AjaxResult.error("角色编码和名称不能为空");
        }
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.insert(role);
        assignPermissions(role.getId(), role.getPermissionIds());
        return AjaxResult.success();
    }

    @PutMapping("/{id}")
    public AjaxResult<Void> update(@PathVariable Long id, @RequestBody SysRole role) {
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

    @DeleteMapping("/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
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

    @PutMapping("/{id}/permissions")
    public AjaxResult<Void> permissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
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

