package com.oa.payroll.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.common.dto.PageQuery;
import com.oa.common.dto.ReimbursementSalaryRequest;
import com.oa.common.entity.Salary;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import com.oa.common.util.StringUtils;
import com.oa.payroll.mapper.SalaryMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalaryService {
    private static final DateTimeFormatter SALARY_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final SalaryMapper mapper;

    public SalaryService(SalaryMapper mapper) {
        this.mapper = mapper;
    }

    public PageResult<Salary> page(PageQuery query) {
        Page<Salary> page = new Page<>(query.safePage(), query.safeLimit());
        LambdaQueryWrapper<Salary> wrapper = new LambdaQueryWrapper<Salary>()
                .orderByDesc(Salary::getSalaryMonth)
                .orderByDesc(Salary::getId);
        if (query.getUserId() != null) {
            wrapper.eq(Salary::getUserId, query.getUserId());
        }
        if (StringUtils.hasText(query.getSalaryMonth())) {
            wrapper.eq(Salary::getSalaryMonth, query.getSalaryMonth());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Salary::getUsername, query.getKeyword())
                    .or()
                    .like(Salary::getSalaryMonth, query.getKeyword())
                    .or()
                    .like(Salary::getRemark, query.getKeyword()));
        }
        Page<Salary> result = mapper.selectPage(page, wrapper);
        return PageResult.success(result.getTotal(), result.getRecords());
    }

    @Transactional
    public AjaxResult<Void> add(Salary salary) {
        salary.setTotalSalary(total(salary));
        salary.setCreateTime(LocalDateTime.now());
        salary.setUpdateTime(LocalDateTime.now());
        mapper.insert(salary);
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> update(Long id, Salary salary) {
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

    @Transactional
    public AjaxResult<Void> delete(Long id) {
        mapper.deleteById(id);
        return AjaxResult.success();
    }

    @Transactional
    public AjaxResult<Void> applyReimbursementBonus(ReimbursementSalaryRequest request) {
        if (request.getUserId() == null) {
            return AjaxResult.error("用户不能为空");
        }
        BigDecimal amount = value(request.getAmount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return AjaxResult.error("报销金额必须大于 0");
        }
        String salaryMonth = StringUtils.hasText(request.getSalaryMonth())
                ? request.getSalaryMonth()
                : SALARY_MONTH_FORMATTER.format(LocalDate.now());

        Salary salary = mapper.selectOne(new LambdaQueryWrapper<Salary>()
                .eq(Salary::getUserId, request.getUserId())
                .eq(Salary::getSalaryMonth, salaryMonth)
                .last("LIMIT 1"));
        if (salary == null) {
            salary = new Salary();
            salary.setUserId(request.getUserId());
            salary.setUsername(request.getUsername());
            salary.setSalaryMonth(salaryMonth);
            salary.setBaseSalary(BigDecimal.ZERO);
            salary.setBonus(amount);
            salary.setDeduction(BigDecimal.ZERO);
            salary.setRemark(reimbursementRemark(request));
            salary.setTotalSalary(total(salary));
            salary.setCreateTime(LocalDateTime.now());
            salary.setUpdateTime(LocalDateTime.now());
            mapper.insert(salary);
            return AjaxResult.success();
        }

        salary.setUsername(StringUtils.hasText(request.getUsername()) ? request.getUsername() : salary.getUsername());
        salary.setBonus(value(salary.getBonus()).add(amount));
        salary.setRemark(appendRemark(salary.getRemark(), reimbursementRemark(request)));
        salary.setTotalSalary(total(salary));
        salary.setUpdateTime(LocalDateTime.now());
        mapper.updateById(salary);
        return AjaxResult.success();
    }

    private BigDecimal total(Salary salary) {
        return value(salary.getBaseSalary()).add(value(salary.getBonus())).subtract(value(salary.getDeduction()));
    }

    private BigDecimal value(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String reimbursementRemark(ReimbursementSalaryRequest request) {
        String title = StringUtils.hasText(request.getReimbursementTitle())
                ? request.getReimbursementTitle()
                : "报销申请";
        String prefix = request.getReimbursementId() == null
                ? title
                : "报销#" + request.getReimbursementId() + " " + title;
        String remark = StringUtils.hasText(request.getRemark()) ? request.getRemark() : prefix;
        return remark.length() > 120 ? remark.substring(0, 120) : remark;
    }

    private String appendRemark(String oldRemark, String newRemark) {
        if (!StringUtils.hasText(oldRemark)) {
            return newRemark;
        }
        String merged = oldRemark + "；" + newRemark;
        return merged.length() > 255 ? merged.substring(0, 255) : merged;
    }
}
