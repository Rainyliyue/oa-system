package com.oa.user.controller;

import com.oa.common.dto.PageQuery;
import com.oa.common.dto.UserForm;
import com.oa.common.entity.SysUser;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.user.service.AdminUserService;
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
@RequestMapping("/admin/users")
public class AdminUserController {
    private final AdminUserService service;

    public AdminUserController(AdminUserService service) {
        this.service = service;
    }

    @PostMapping("/page")
    public PageResult<SysUser> page(@RequestBody PageQuery query) {
        return service.page(query);
    }

    @GetMapping("/{id}")
    public AjaxResult<SysUser> get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    public AjaxResult<Void> add(@RequestBody UserForm form) {
        return service.add(form);
    }

    @PutMapping("/{id}")
    public AjaxResult<Void> update(@PathVariable Long id, @RequestBody UserForm form) {
        return service.update(id, form);
    }

    @DeleteMapping("/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        return service.delete(id);
    }

    @PutMapping("/{id}/roles")
    public AjaxResult<Void> roles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        return service.roles(id, roleIds);
    }
}
