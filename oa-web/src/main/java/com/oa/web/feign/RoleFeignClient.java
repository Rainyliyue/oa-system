package com.oa.web.feign;

import com.oa.common.dto.PageQuery;
import com.oa.common.entity.SysRole;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "oa-user-service", path = "/admin/roles")
public interface RoleFeignClient {
    @PostMapping("/page")
    PageResult<SysRole> page(@RequestBody PageQuery query);

    @GetMapping("/all")
    AjaxResult<List<SysRole>> all();

    @PostMapping
    AjaxResult<Void> add(@RequestBody SysRole role);

    @PutMapping("/{id}")
    AjaxResult<Void> update(@PathVariable("id") Long id, @RequestBody SysRole role);

    @DeleteMapping("/{id}")
    AjaxResult<Void> delete(@PathVariable("id") Long id);

    @PutMapping("/{id}/permissions")
    AjaxResult<Void> permissions(@PathVariable("id") Long id, @RequestBody List<Long> permissionIds);
}

