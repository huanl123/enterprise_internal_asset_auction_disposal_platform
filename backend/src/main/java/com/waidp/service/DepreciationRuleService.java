package com.waidp.service;

import com.waidp.entity.DepreciationRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 折旧规则服务接口
 */
public interface DepreciationRuleService {

    /**
     * 获取折旧规则列表
     */
    Page<DepreciationRule> getRules(String name, Boolean status, Pageable pageable);

    /**
     * 根据ID获取折旧规则
     */
    DepreciationRule getRuleById(Long id);

    /**
     * 创建折旧规则
     */
    void createRule(DepreciationRule rule);

    /**
     * 更新折旧规则
     */
    void updateRule(DepreciationRule rule);

    /**
     * 启用/停用折旧规则
     */
    void toggleRuleStatus(Long ruleId, Boolean status);

    /**
     * 删除折旧规则
     */
    void deleteRule(Long id);

    /**
     * 获取所有启用的折旧规则
     */
    List<DepreciationRule> getActiveRules();
}
