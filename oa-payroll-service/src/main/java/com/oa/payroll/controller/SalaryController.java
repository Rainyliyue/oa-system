package com.oa.payroll.controller;

import com.oa.common.dto.PageQuery;
import com.oa.common.dto.ReimbursementSalaryRequest;
import com.oa.common.entity.Salary;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.payroll.service.SalaryService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/salary")
public class SalaryController {
    private final SalaryService service;

    public SalaryController(SalaryService service) {
        this.service = service;
    }

    @PostMapping("/admin/page")
    public PageResult<Salary> page(@RequestBody PageQuery query) {
        return service.page(query);
    }

    @PostMapping("/admin")
    public AjaxResult<Void> add(@RequestBody Salary salary) {
        return service.add(salary);
    }

    @PostMapping("/admin/reimbursement-bonus")
    public AjaxResult<Void> applyReimbursementBonus(@RequestBody ReimbursementSalaryRequest request) {
        return service.applyReimbursementBonus(request);
    }

    @PutMapping("/admin/{id}")
    public AjaxResult<Void> update(@PathVariable Long id, @RequestBody Salary salary) {
        return service.update(id, salary);
    }

    @DeleteMapping("/admin/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}
