package com.oa.attendance.controller;

import com.oa.attendance.service.AttendanceService;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.Attendance;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    private final AttendanceService service;

    public AttendanceController(AttendanceService service) {
        this.service = service;
    }

    @GetMapping("/user/today")
    public AjaxResult<Attendance> today(@RequestParam Long userId) {
        return service.today(userId);
    }

    @PostMapping("/user/clock-in")
    public AjaxResult<Attendance> clockIn(@RequestBody Attendance form) {
        return service.clockIn(form);
    }

    @PostMapping("/user/clock-out")
    public AjaxResult<Attendance> clockOut(@RequestBody Attendance form) {
        return service.clockOut(form);
    }

    @PostMapping("/admin/page")
    public PageResult<Attendance> adminPage(@RequestBody PageQuery query) {
        return service.page(query);
    }

    @PutMapping("/admin/{id}")
    public AjaxResult<Void> update(@PathVariable Long id, @RequestBody Attendance form) {
        return service.update(id, form);
    }

    @DeleteMapping("/admin/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}
