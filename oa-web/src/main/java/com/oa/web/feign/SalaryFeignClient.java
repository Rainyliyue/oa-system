package com.oa.web.feign;

import com.oa.common.dto.PageQuery;
import com.oa.common.dto.ReimbursementSalaryRequest;
import com.oa.common.entity.Salary;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.web.feign.fallback.SalaryFeignFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(contextId = "salaryFeignClient", value = "oa-payroll-service", path = "/salary",
        fallbackFactory = SalaryFeignFallbackFactory.class)
public interface SalaryFeignClient {
    @PostMapping("/admin/page")
    PageResult<Salary> page(@RequestBody PageQuery query);

    @PostMapping("/admin")
    AjaxResult<Void> add(@RequestBody Salary salary);

    @PostMapping("/admin/reimbursement-bonus")
    AjaxResult<Void> applyReimbursementBonus(@RequestBody ReimbursementSalaryRequest request);

    @PutMapping("/admin/{id}")
    AjaxResult<Void> update(@PathVariable("id") Long id, @RequestBody Salary salary);

    @DeleteMapping("/admin/{id}")
    AjaxResult<Void> delete(@PathVariable("id") Long id);
}
