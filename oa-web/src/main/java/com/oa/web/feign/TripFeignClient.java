package com.oa.web.feign;

import com.oa.common.dto.ApprovalRequest;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.TripApply;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "oa-application-service", path = "/trip")
public interface TripFeignClient {
    @PostMapping("/user/page")
    PageResult<TripApply> userPage(@RequestBody PageQuery query);

    @PostMapping("/admin/page")
    PageResult<TripApply> adminPage(@RequestBody PageQuery query);

    @PostMapping
    AjaxResult<Void> add(@RequestBody TripApply apply);

    @PutMapping("/user/{id}")
    AjaxResult<Void> userUpdate(@PathVariable("id") Long id, @RequestParam("userId") Long userId,
                                @RequestBody TripApply apply);

    @DeleteMapping("/user/{id}")
    AjaxResult<Void> userDelete(@PathVariable("id") Long id, @RequestParam("userId") Long userId);

    @PutMapping("/admin/{id}")
    AjaxResult<Void> adminUpdate(@PathVariable("id") Long id, @RequestBody TripApply apply);

    @DeleteMapping("/admin/{id}")
    AjaxResult<Void> adminDelete(@PathVariable("id") Long id);

    @PostMapping("/admin/{id}/approve")
    AjaxResult<Void> approve(@PathVariable("id") Long id, @RequestBody ApprovalRequest request);
}

