package com.waidp.service.impl;

import com.waidp.entity.DepreciationRule;
import com.waidp.repository.AssetRepository;
import com.waidp.repository.DepreciationRuleRepository;
import com.waidp.service.DepreciationRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 折旧规则服务实现
 */
@Service
@RequiredArgsConstructor
public class DepreciationRuleServiceImpl implements DepreciationRuleService {

    private final DepreciationRuleRepository depreciationRuleRepository;
    private final AssetRepository assetRepository;

    @Override
    public Page<DepreciationRule> getRules(String name, Boolean status, Pageable pageable) {
        Page<DepreciationRule> page = depreciationRuleRepository.searchRules(name, status, pageable);
        page.getContent().forEach(rule ->
                rule.setAssetCount((int) assetRepository.countByDepreciationRuleIdNative(rule.getId()))
        );
        return page;
    }

    @Override
    public DepreciationRule getRuleById(Long id) {
        DepreciationRule rule = depreciationRuleRepository.findById(id).orElse(null);
        if (rule != null) {
            rule.setAssetCount((int) assetRepository.countByDepreciationRuleIdNative(rule.getId()));
        }
        return rule;
    }

    @Override
    @Transactional
    public void createRule(DepreciationRule rule) {
        // 检查规则名称是否已存在
        if (depreciationRuleRepository.existsByName(rule.getName())) {
            throw new RuntimeException("规则名称已存在");
        }

        // 验证参数
        if (rule.getUsefulLife() <= 0) {
            throw new RuntimeException("预计使用年限必须大于0");
        }
        if (rule.getSalvageRate() < 0 || rule.getSalvageRate() > 100) {
            throw new RuntimeException("残值率必须在0-100之间");
        }

        rule.setStatus(true);
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());

        depreciationRuleRepository.save(rule);
    }

    @Override
    @Transactional
    public void updateRule(DepreciationRule rule) {
        DepreciationRule existing = depreciationRuleRepository.findById(rule.getId())
                .orElseThrow(() -> new RuntimeException("折旧规则不存在"));

        // 检查规则名称是否与其他规则重复
        if (!existing.getName().equals(rule.getName()) &&
            depreciationRuleRepository.existsByNameAndIdNot(rule.getName(), rule.getId())) {
            throw new RuntimeException("规则名称已存在");
        }

        // 验证参数
        if (rule.getUsefulLife() <= 0) {
            throw new RuntimeException("预计使用年限必须大于0");
        }
        if (rule.getSalvageRate() < 0 || rule.getSalvageRate() > 100) {
            throw new RuntimeException("残值率必须在0-100之间");
        }

        long boundCount = assetRepository.countByDepreciationRuleIdNative(existing.getId());
        if (boundCount > 0) {
            throw new RuntimeException("规则已被资产绑定，只能停用不能修改或删除");
        }

        // 未绑定的规则允许修改
        existing.setName(rule.getName());
        existing.setUsefulLife(rule.getUsefulLife());
        existing.setSalvageRate(rule.getSalvageRate());
        existing.setDescription(rule.getDescription());

        existing.setUpdateTime(LocalDateTime.now());
        depreciationRuleRepository.save(existing);
    }

    @Override
    @Transactional
    public void toggleRuleStatus(Long ruleId, Boolean status) {
        DepreciationRule rule = depreciationRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("折旧规则不存在"));

        rule.setStatus(status);
        rule.setUpdateTime(LocalDateTime.now());
        depreciationRuleRepository.save(rule);
    }

    @Override
    @Transactional
    public void deleteRule(Long id) {
        depreciationRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("折旧规则不存在"));

        // 检查是否有资产使用该规则
        if (assetRepository.countByDepreciationRuleIdNative(id) > 0) {
            throw new RuntimeException("规则已被资产使用，只能停用不能删除");
        }

        depreciationRuleRepository.deleteById(id);
    }

    @Override
    public List<DepreciationRule> getActiveRules() {
        return depreciationRuleRepository.findByStatus(true);
    }
}
