package com.oa.web.feign;

import com.oa.common.dto.PageQuery;
import com.oa.common.entity.SysPermission;
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

@FeignClient(value = "oa-user-service", path = "/admin/permissions")
public interface PermissionFeignClient {
    @PostMapping("/page")
    PageResult<SysPermission> page(@RequestBody PageQuery query);

    @GetMapping("/all")
    AjaxResult<List<SysPermission>> all();

    @PostMapping
    AjaxResult<Void> add(@RequestBody SysPermission permission);

    @PutMapping("/{id}")
    AjaxResult<Void> update(@PathVariable("id") Long id, @RequestBody SysPermission permission);

    @DeleteMapping("/{id}")
    AjaxResult<Void> delete(@PathVariable("id") Long id);
}

