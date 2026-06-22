package com.oa.web.feign.fallback;

import com.oa.common.dto.PageQuery;
import com.oa.common.dto.ReimbursementSalaryRequest;
import com.oa.common.entity.Salary;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.web.feign.SalaryFeignClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class SalaryFeignFallbackFactory implements FallbackFactory<SalaryFeignClient> {
    private static final Logger log = LoggerFactory.getLogger(SalaryFeignFallbackFactory.class);

    @Override
    public SalaryFeignClient create(Throwable cause) {
        return new SalaryFeignClient() {
            @Override
            public PageResult<Salary> page(PageQuery query) {
                return FeignFallbackSupport.page("工资服务", cause, log);
            }

            @Override
            public AjaxResult<Void> add(Salary salary) {
                return FeignFallbackSupport.ajax("工资服务", cause, log);
            }

            @Override
            public AjaxResult<Void> applyReimbursementBonus(ReimbursementSalaryRequest request) {
                return FeignFallbackSupport.ajax("工资服务", cause, log);
            }

            @Override
            public AjaxResult<Void> update(Long id, Salary salary) {
                return FeignFallbackSupport.ajax("工资服务", cause, log);
            }

            @Override
            public AjaxResult<Void> delete(Long id) {
                return FeignFallbackSupport.ajax("工资服务", cause, log);
            }
        };
    }
}
