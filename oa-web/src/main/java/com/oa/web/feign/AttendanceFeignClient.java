package com.oa.web.feign;

import com.oa.common.dto.PageQuery;
import com.oa.common.entity.Attendance;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(contextId = "attendanceFeignClient", value = "oa-attendance-service", path = "/attendance")
public interface AttendanceFeignClient {
    @GetMapping("/user/today")
    AjaxResult<Attendance> today(@RequestParam("userId") Long userId);

    @PostMapping("/user/clock-in")
    AjaxResult<Attendance> clockIn(@RequestBody Attendance attendance);

    @PostMapping("/user/clock-out")
    AjaxResult<Attendance> clockOut(@RequestBody Attendance attendance);

    @PostMapping("/admin/page")
    PageResult<Attendance> adminPage(@RequestBody PageQuery query);

    @PutMapping("/admin/{id}")
    AjaxResult<Void> update(@PathVariable("id") Long id, @RequestBody Attendance attendance);

    @DeleteMapping("/admin/{id}")
    AjaxResult<Void> delete(@PathVariable("id") Long id);
}
