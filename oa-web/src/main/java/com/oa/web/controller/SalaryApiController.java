package com.oa.web.controller;

import com.oa.common.dto.PageQuery;
import com.oa.common.entity.Salary;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.web.feign.SalaryFeignClient;
import com.oa.web.support.AdminOperationLogger;
import jakarta.servlet.http.HttpServletRequest;
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
    private final AdminOperationLogger operationLogger;

    public SalaryApiController(SalaryFeignClient salaryFeignClient, AdminOperationLogger operationLogger) {
        this.salaryFeignClient = salaryFeignClient;
        this.operationLogger = operationLogger;
    }

    @PostMapping("/page")
    public PageResult<Salary> page(@RequestBody PageQuery query) {
        return salaryFeignClient.page(query);
    }

    @PostMapping
    public AjaxResult<Void> add(@RequestBody Salary salary, HttpServletRequest request) {
        AjaxResult<Void> result = salaryFeignClient.add(salary);
        operationLogger.logIfSuccess(request, result, "工资管理", "CREATE", "oa_salary", null, "新增工资：" + salary.getUsername());
        return result;
    }

    @PutMapping("/{id}")
    public AjaxResult<Void> update(@PathVariable Long id, @RequestBody Salary salary, HttpServletRequest request) {
        AjaxResult<Void> result = salaryFeignClient.update(id, salary);
        operationLogger.logIfSuccess(request, result, "工资管理", "UPDATE", "oa_salary", id, "修改工资：" + id);
        return result;
    }

    @DeleteMapping("/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        AjaxResult<Void> result = salaryFeignClient.delete(id);
        operationLogger.logIfSuccess(request, result, "工资管理", "DELETE", "oa_salary", id, "删除工资：" + id);
        return result;
    }
}
