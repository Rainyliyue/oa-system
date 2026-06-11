package com.oa.web.controller;

import com.oa.common.dto.LoginUser;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.Attendance;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.web.feign.AttendanceFeignClient;
import com.oa.web.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AttendanceApiController {
    private final AttendanceFeignClient attendanceFeignClient;
    private final CurrentUser currentUser;

    public AttendanceApiController(AttendanceFeignClient attendanceFeignClient, CurrentUser currentUser) {
        this.attendanceFeignClient = attendanceFeignClient;
        this.currentUser = currentUser;
    }

    @GetMapping("/user/attendance/today")
    public AjaxResult<Attendance> today(HttpServletRequest request) {
        return attendanceFeignClient.today(currentUser.get(request).getId());
    }

    @PostMapping("/user/attendance/clock-in")
    public AjaxResult<Attendance> clockIn(HttpServletRequest request) {
        LoginUser user = currentUser.get(request);
        Attendance attendance = new Attendance();
        attendance.setUserId(user.getId());
        attendance.setUsername(user.getRealName());
        return attendanceFeignClient.clockIn(attendance);
    }

    @PostMapping("/user/attendance/clock-out")
    public AjaxResult<Attendance> clockOut(HttpServletRequest request) {
        LoginUser user = currentUser.get(request);
        Attendance attendance = new Attendance();
        attendance.setUserId(user.getId());
        attendance.setUsername(user.getRealName());
        return attendanceFeignClient.clockOut(attendance);
    }

    @PostMapping("/admin/attendance/page")
    public PageResult<Attendance> page(@RequestBody PageQuery query) {
        return attendanceFeignClient.adminPage(query);
    }

    @PutMapping("/admin/attendance/{id}")
    public AjaxResult<Void> update(@PathVariable Long id, @RequestBody Attendance attendance) {
        return attendanceFeignClient.update(id, attendance);
    }

    @DeleteMapping("/admin/attendance/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        return attendanceFeignClient.delete(id);
    }
}

