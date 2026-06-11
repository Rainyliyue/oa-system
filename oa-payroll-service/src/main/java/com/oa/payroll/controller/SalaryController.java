package com.oa.payroll.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.Salary;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.common.util.StringUtils;
import com.oa.payroll.mapper.SalaryMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final SalaryMapper mapper;

    public SalaryController(SalaryMapper mapper) {
        this.mapper = mapper;
    }

    @PostMapping("/admin/page")
    public PageResult<Salary> page(@RequestBody PageQuery query) {
        Page<Salary> page = new Page<>(query.safePage(), query.safeLimit());
        LambdaQueryWrapper<Salary> wrapper = new LambdaQueryWrapper<Salary>()
                .orderByDesc(Salary::getSalaryMonth)
                .orderByDesc(Salary::getId);
        if (query.getUserId() != null) {
            wrapper.eq(Salary::getUserId, query.getUserId());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Salary::getUsername, query.getKeyword())
                    .or()
                    .like(Salary::getSalaryMonth, query.getKeyword()));
        }
        Page<Salary> result = mapper.selectPage(page, wrapper);
        return PageResult.success(result.getTotal(), result.getRecords());
    }

    @PostMapping("/admin")
    public AjaxResult<Void> add(@RequestBody Salary salary) {
        salary.setTotalSalary(total(salary));
        salary.setCreateTime(LocalDateTime.now());
        salary.setUpdateTime(LocalDateTime.now());
        mapper.insert(salary);
        return AjaxResult.success();
    }

    @PutMapping("/admin/{id}")
    public AjaxResult<Void> update(@PathVariable Long id, @RequestBody Salary salary) {
        Salary old = mapper.selectById(id);
        if (old == null) {
            return AjaxResult.error("工资记录不存在");
        }
        old.setUserId(salary.getUserId());
        old.setUsername(salary.getUsername());
        old.setSalaryMonth(salary.getSalaryMonth());
        old.setBaseSalary(salary.getBaseSalary());
        old.setBonus(salary.getBonus());
        old.setDeduction(salary.getDeduction());
        old.setTotalSalary(total(salary));
        old.setRemark(salary.getRemark());
        old.setUpdateTime(LocalDateTime.now());
        mapper.updateById(old);
        return AjaxResult.success();
    }

    @DeleteMapping("/admin/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        mapper.deleteById(id);
        return AjaxResult.success();
    }

    private BigDecimal total(Salary salary) {
        return value(salary.getBaseSalary()).add(value(salary.getBonus())).subtract(value(salary.getDeduction()));
    }

    private BigDecimal value(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}

