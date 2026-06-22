package com.oa.web.service;

import com.oa.common.dto.ApprovalRequest;
import com.oa.common.dto.ReimbursementSalaryRequest;
import com.oa.common.entity.ReimbursementApply;
import com.oa.common.result.AjaxResult;
import com.oa.web.feign.LeaveFeignClient;
import com.oa.web.feign.ReimbursementFeignClient;
import com.oa.web.feign.SalaryFeignClient;
import com.oa.web.feign.TripFeignClient;
import io.seata.spring.annotation.GlobalTransactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ApprovalOrchestrationService {
    private static final DateTimeFormatter SALARY_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final LeaveFeignClient leaveFeignClient;
    private final TripFeignClient tripFeignClient;
    private final ReimbursementFeignClient reimbursementFeignClient;
    private final SalaryFeignClient salaryFeignClient;

    @Value("${seata.enabled:false}")
    private boolean seataEnabled;

    public ApprovalOrchestrationService(LeaveFeignClient leaveFeignClient,
                                        TripFeignClient tripFeignClient,
                                        ReimbursementFeignClient reimbursementFeignClient,
                                        SalaryFeignClient salaryFeignClient) {
        this.leaveFeignClient = leaveFeignClient;
        this.tripFeignClient = tripFeignClient;
        this.reimbursementFeignClient = reimbursementFeignClient;
        this.salaryFeignClient = salaryFeignClient;
    }

    @GlobalTransactional(name = "oa-application-approval", rollbackFor = Exception.class)
    public AjaxResult<Void> approve(String type, Long id, ApprovalRequest request) {
        ReimbursementApply reimbursement = reimbursementForSalaryLink(type, id, request);
        AjaxResult<Void> result = switch (type) {
            case "leave" -> leaveFeignClient.approve(id, request);
            case "trip" -> tripFeignClient.approve(id, request);
            case "reimbursement" -> reimbursementFeignClient.approve(id, request);
            default -> AjaxResult.error("未知申请类型");
        };
        if (!result.ok()) {
            return result;
        }
        if (reimbursement != null) {
            AjaxResult<Void> salaryResult = salaryFeignClient.applyReimbursementBonus(toSalaryRequest(reimbursement));
            if (!salaryResult.ok()) {
                throw new IllegalStateException("工资联动失败：" + salaryResult.getMsg());
            }
        }
        return result;
    }

    private ReimbursementApply reimbursementForSalaryLink(String type, Long id, ApprovalRequest request) {
        if (!seataEnabled
                || !"reimbursement".equals(type)
                || !Boolean.TRUE.equals(request.getPassed())
                || Boolean.FALSE.equals(request.getFinalApproval())) {
            return null;
        }
        AjaxResult<ReimbursementApply> detail = reimbursementFeignClient.adminDetail(id);
        if (!detail.ok() || detail.getData() == null) {
            throw new IllegalStateException("读取报销明细失败：" + detail.getMsg());
        }
        return detail.getData();
    }

    private ReimbursementSalaryRequest toSalaryRequest(ReimbursementApply reimbursement) {
        ReimbursementSalaryRequest request = new ReimbursementSalaryRequest();
        request.setUserId(reimbursement.getUserId());
        request.setUsername(reimbursement.getUsername());
        request.setSalaryMonth(SALARY_MONTH_FORMATTER.format(LocalDate.now()));
        request.setAmount(reimbursement.getAmount());
        request.setReimbursementId(reimbursement.getId());
        request.setReimbursementTitle(reimbursement.getTitle());
        request.setRemark("报销审批通过自动计入当月奖金");
        return request;
    }
}
