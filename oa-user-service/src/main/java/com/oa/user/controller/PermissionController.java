package com.oa.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.SysPermission;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.common.util.StringUtils;
import com.oa.user.mapper.PermissionMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/permissions")
public class PermissionController {
    private final PermissionMapper permissionMapper;

    public PermissionController(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    @PostMapping("/page")
    public PageResult<SysPermission> page(@RequestBody PageQuery query) {
        Page<SysPermission> page = new Page<>(query.safePage(), query.safeLimit());
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<SysPermission>()
                .orderByDesc(SysPermission::getCreateTime);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SysPermission::getPermissionCode, query.getKeyword())
                    .or()
                    .like(SysPermission::getPermissionName, query.getKeyword())
                    .or()
                    .like(SysPermission::getPath, query.getKeyword()));
        }
        Page<SysPermission> result = permissionMapper.selectPage(page, wrapper);
        return PageResult.success(result.getTotal(), result.getRecords());
    }

    @GetMapping("/all")
    public AjaxResult<List<SysPermission>> all() {
        return AjaxResult.success(permissionMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                .orderByAsc(SysPermission::getId)));
    }

    @PostMapping
    public AjaxResult<Void> add(@RequestBody SysPermission permission) {
        if (!StringUtils.hasText(permission.getPermissionCode())
                || !StringUtils.hasText(permission.getPermissionName())) {
            return AjaxResult.error("权限编码和名称不能为空");
        }
        permission.setCreateTime(LocalDateTime.now());
        permission.setUpdateTime(LocalDateTime.now());
        permissionMapper.insert(permission);
        return AjaxResult.success();
    }

    @PutMapping("/{id}")
    public AjaxResult<Void> update(@PathVariable Long id, @RequestBody SysPermission permission) {
        SysPermission old = permissionMapper.selectById(id);
        if (old == null) {
            return AjaxResult.error("权限不存在");
        }
        old.setPermissionName(permission.getPermissionName());
        old.setPath(permission.getPath());
        old.setType(permission.getType());
        old.setUpdateTime(LocalDateTime.now());
        permissionMapper.updateById(old);
        return AjaxResult.success();
    }

    @DeleteMapping("/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        permissionMapper.deleteById(id);
        return AjaxResult.success();
    }
}

