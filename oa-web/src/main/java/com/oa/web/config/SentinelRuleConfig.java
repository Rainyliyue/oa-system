package com.oa.web.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SentinelRuleConfig {

    @Bean
    @ConditionalOnMissingBean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    @PostConstruct
    public void initRules() {
        FlowRuleManager.loadRules(flowRules());
        DegradeRuleManager.loadRules(degradeRules());
    }

    private List<FlowRule> flowRules() {
        List<FlowRule> rules = new ArrayList<>();
        rules.add(qpsRule("auth:login", 5));
        rules.add(qpsRule("file:upload:image", 3));
        rules.add(qpsRule("application:approve", 10));
        return rules;
    }

    private FlowRule qpsRule(String resource, double count) {
        FlowRule rule = new FlowRule();
        rule.setResource(resource);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(count);
        return rule;
    }

    private List<DegradeRule> degradeRules() {
        List<DegradeRule> rules = new ArrayList<>();
        DegradeRule approveRule = new DegradeRule();
        approveRule.setResource("application:approve");
        approveRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        approveRule.setCount(0.5);
        approveRule.setMinRequestAmount(5);
        approveRule.setStatIntervalMs(60_000);
        approveRule.setTimeWindow(10);
        rules.add(approveRule);
        return rules;
    }
}
