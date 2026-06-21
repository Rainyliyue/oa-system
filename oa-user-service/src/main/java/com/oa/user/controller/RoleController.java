package com.oa.user.controller;

import com.oa.common.dto.PageQuery;
import com.oa.common.entity.SysRole;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.user.service.RoleManagementService;
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
@RequestMapping("/admin/roles")
public class RoleController {
    private final RoleManagementService service;

    public RoleController(RoleManagementService service) {
        this.service = service;
    }

    @PostMapping("/page")
    public PageResult<SysRole> page(@RequestBody PageQuery query) {
        return service.page(query);
    }

    @GetMapping("/all")
    public AjaxResult<List<SysRole>> all() {
        return service.all();
    }

    @PostMapping
    public AjaxResult<Void> add(@RequestBody SysRole role) {
        return service.add(role);
    }

    @PutMapping("/{id}")
    public AjaxResult<Void> update(@PathVariable Long id, @RequestBody SysRole role) {
        return service.update(id, role);
    }

    @DeleteMapping("/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        return service.delete(id);
    }

    @PutMapping("/{id}/permissions")
    public AjaxResult<Void> permissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        return service.permissions(id, permissionIds);
    }
}
