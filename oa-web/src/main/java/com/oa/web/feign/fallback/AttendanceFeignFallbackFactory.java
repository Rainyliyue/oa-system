package com.oa.web.feign.fallback;

import com.oa.common.dto.PageQuery;
import com.oa.common.entity.Attendance;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.web.feign.AttendanceFeignClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class AttendanceFeignFallbackFactory implements FallbackFactory<AttendanceFeignClient> {
    private static final Logger log = LoggerFactory.getLogger(AttendanceFeignFallbackFactory.class);

    @Override
    public AttendanceFeignClient create(Throwable cause) {
        return new AttendanceFeignClient() {
            @Override
            public AjaxResult<Attendance> today(Long userId) {
                return FeignFallbackSupport.ajax("考勤服务", cause, log);
            }

            @Override
            public AjaxResult<Attendance> clockIn(Attendance attendance) {
                return FeignFallbackSupport.ajax("考勤服务", cause, log);
            }

            @Override
            public AjaxResult<Attendance> clockOut(Attendance attendance) {
                return FeignFallbackSupport.ajax("考勤服务", cause, log);
            }

            @Override
            public PageResult<Attendance> adminPage(PageQuery query) {
                return FeignFallbackSupport.page("考勤服务", cause, log);
            }

            @Override
            public AjaxResult<Void> update(Long id, Attendance attendance) {
                return FeignFallbackSupport.ajax("考勤服务", cause, log);
            }

            @Override
            public AjaxResult<Void> delete(Long id) {
                return FeignFallbackSupport.ajax("考勤服务", cause, log);
            }
        };
    }
}
