package com.oa.web.feign;

import com.oa.common.dto.ApprovalRequest;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.LeaveApply;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "oa-application-service", path = "/leave")
public interface LeaveFeignClient {
    @PostMapping("/user/page")
    PageResult<LeaveApply> userPage(@RequestBody PageQuery query);

    @PostMapping("/admin/page")
    PageResult<LeaveApply> adminPage(@RequestBody PageQuery query);

    @PostMapping
    AjaxResult<Void> add(@RequestBody LeaveApply apply);

    @PutMapping("/user/{id}")
    AjaxResult<Void> userUpdate(@PathVariable("id") Long id, @RequestParam("userId") Long userId,
                                @RequestBody LeaveApply apply);

    @DeleteMapping("/user/{id}")
    AjaxResult<Void> userDelete(@PathVariable("id") Long id, @RequestParam("userId") Long userId);

    @PutMapping("/admin/{id}")
    AjaxResult<Void> adminUpdate(@PathVariable("id") Long id, @RequestBody LeaveApply apply);

    @DeleteMapping("/admin/{id}")
    AjaxResult<Void> adminDelete(@PathVariable("id") Long id);

    @PostMapping("/admin/{id}/approve")
    AjaxResult<Void> approve(@PathVariable("id") Long id, @RequestBody ApprovalRequest request);
}

