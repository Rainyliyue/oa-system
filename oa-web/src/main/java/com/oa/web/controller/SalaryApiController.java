package com.oa.web.controller;

import com.oa.common.dto.PageQuery;
import com.oa.common.entity.Salary;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.web.feign.SalaryFeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/salary")
public class SalaryApiController {
    private final SalaryFeignClient salaryFeignClient;

    public SalaryApiController(SalaryFeignClient salaryFeignClient) {
        this.salaryFeignClient = salaryFeignClient;
    }

    @PostMapping("/page")
    public PageResult<Salary> page(@RequestBody PageQuery query) {
        return salaryFeignClient.page(query);
    }

    @PostMapping
    public AjaxResult<Void> add(@RequestBody Salary salary) {
        return salaryFeignClient.add(salary);
    }

    @PutMapping("/{id}")
    public AjaxResult<Void> update(@PathVariable Long id, @RequestBody Salary salary) {
        return salaryFeignClient.update(id, salary);
    }

    @DeleteMapping("/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        return salaryFeignClient.delete(id);
    }
}

