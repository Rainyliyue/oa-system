package com.oa.web.feign;

import com.oa.common.dto.PageQuery;
import com.oa.common.dto.UserForm;
import com.oa.common.entity.SysUser;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.web.feign.fallback.AdminUserFeignFallbackFactory;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(contextId = "adminUserFeignClient", value = "oa-user-service", path = "/admin/users",
        fallbackFactory = AdminUserFeignFallbackFactory.class)
public interface AdminUserFeignClient {
    @PostMapping("/page")
    PageResult<SysUser> page(@RequestBody PageQuery query);

    @PostMapping
    AjaxResult<Void> add(@RequestBody UserForm form);

    @PutMapping("/{id}")
    AjaxResult<Void> update(@PathVariable("id") Long id, @RequestBody UserForm form);

    @DeleteMapping("/{id}")
    AjaxResult<Void> delete(@PathVariable("id") Long id);

    @PutMapping("/{id}/roles")
    AjaxResult<Void> roles(@PathVariable("id") Long id, @RequestBody List<Long> roleIds);
}
