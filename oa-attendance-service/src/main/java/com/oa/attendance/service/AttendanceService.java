package com.oa.attendance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.attendance.mapper.AttendanceMapper;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.Attendance;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.common.util.StringUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceService {
    private final AttendanceMapper mapper;

    public AttendanceService(AttendanceMapper mapper) {
        this.mapper = mapper;
    }

    public AjaxResult<Attendance> today(Long userId) {
        return AjaxResult.success(todayRecord(userId));
    }

    @Transactional
    public AjaxResult<Attendance> clockIn(Attendance form) {
        Attendance attendance = todayRecord(form.getUserId());
        if (attendance != null && attendance.getClockInTime() != null) {
            return AjaxResult.error("今天已经完成上班打卡");
        }
        LocalDateTime now = LocalDateTime.now();
        if (attendance == null) {
            attendance = new Attendance();
            attendance.setUserId(form.getUserId());
            attendance.setUsername(form.getUsername());
            attendance.setWorkDate(LocalDate.now());
            attendance.setStatus("NORMAL");
            attendance.setCreateTime(now);
        }
        attendance.setClockInTime(now);
        attendance.setUpdateTime(now);
        if (attendance.getId() == null) {
            mapper.insert(attendance);
        } else {
            mapper.updateById(attendance);
        }
        return AjaxResult.success(attendance);
    }

    @Transactional
    public AjaxResult<Attendance> clockOut(Attendance form) {
        Attendance attendance = todayRecord(form.getUserId());
        if (attendance == null || attendance.getClockInTime() == null) {
            return AjaxResult.error("请先完成上班打卡");
        }
        if (attendance.getClockOutTime() != null) {
            return AjaxResult.error("今天已经完成下班打卡");
        }
        attendance.setClockOutTime(LocalDateTime.now());
        attendance.setUpdateTime(LocalDateTime.now());
        mapper.updateById(attendance);
        return AjaxResult.success(attendance);
    }

    public PageResult<Attendance> page(PageQuery query) {
        Page<Attendance> page = new Page<>(query.safePage(), query.safeLimit());
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<Attendance>()
                .orderByDesc(Attendance::getWorkDate)
                .orderByDesc(Attendance::getId);
        if (query.getUserId() != null) {
            wrapper.eq(Attendance::getUserId, query.getUserId());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Attendance::getStatus, query.getStatus());
        }
        if (query.getStartDate() != null && query.getEndDate() != null) {
            wrapper.between(Attendance::getWorkDate, query.getStartDate(), query.getEndDate());
        } else if (query.getStartDate() != null) {
            wrapper.eq(Attendance::getWorkDate, query.getStartDate());
        } else if (query.getEndDate() != null) {
            wrapper.le(Attendance::getWorkDate, query.getEndDate());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Attendance::getUsername, query.getKeyword())
                    .or()
                    .like(Attendance::getRemark, query.getKeyword()));
        }
        Page<Attendance> result = mapper.selectPage(page, wrapper);
        return PageResult.success(result.getTotal(), result.getRecords());
    }

    @Transactional
    public AjaxResult<Void> update(Long id, Attendance form) {
        Attendance old = mapper.selectById(id);
        if (old == null) {
            return AjaxResult.error("考勤记录不存在");
        }
        old.setWorkDate(form.getWorkDate());
        old.setClockInTime(form.getClockInTime());
        old.setClockOutTime(form.getClockOutTime());
        old.setStatus(form.getStatus());
        old.setRemark(form.getRemark());
        old.setUpdateTime(LocalDateTime.now());
        mapper.updateById(old);
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> delete(Long id) {
        mapper.deleteById(id);
        return AjaxResult.success();
    }

    private Attendance todayRecord(Long userId) {
        return mapper.selectOne(new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getUserId, userId)
                .eq(Attendance::getWorkDate, LocalDate.now()));
    }
}
