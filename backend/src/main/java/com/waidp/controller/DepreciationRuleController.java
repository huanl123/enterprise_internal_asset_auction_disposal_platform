package com.waidp.controller;

import com.waidp.common.PageResult;
import com.waidp.common.Result;
import com.waidp.entity.DepreciationRule;
import com.waidp.service.DepreciationRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 折旧规则管理控制器
 */
@RestController
@RequestMapping("/api/depreciation-rules")
@RequiredArgsConstructor
public class DepreciationRuleController {

    private final DepreciationRuleService depreciationRuleService;

    /**
     * 获取折旧规则列表（分页）
     */
    @GetMapping
    public Result<PageResult<DepreciationRule>> getRules(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
        Page<DepreciationRule> rulePage = depreciationRuleService.getRules(name, status, pageable);
        return Result.success(PageResult.of(rulePage));
    }

    /**
     * 获取折旧规则详情
     */
    @GetMapping("/{id}")
    public Result<DepreciationRule> getRuleById(@PathVariable Long id) {
        return Result.success(depreciationRuleService.getRuleById(id));
    }

    /**
     * 创建折旧规则（资产专员）
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ASSET_SPECIALIST','asset_specialist','资产专员','ADMIN','admin','SYSTEM_ADMIN','system_admin','系统管理员')")
    public Result<Void> createRule(@RequestBody DepreciationRule rule) {
        depreciationRuleService.createRule(rule);
        return Result.success("创建折旧规则成功", null);
    }

    /**
     * 更新折旧规则（资产专员）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ASSET_SPECIALIST','asset_specialist','资产专员','ADMIN','admin','SYSTEM_ADMIN','system_admin','系统管理员')")
    public Result<Void> updateRule(@PathVariable Long id, @RequestBody DepreciationRule rule) {
        rule.setId(id);
        depreciationRuleService.updateRule(rule);
        return Result.success("更新折旧规则成功", null);
    }

    /**
     * 启用/停用折旧规则（资产专员）
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ASSET_SPECIALIST','asset_specialist','资产专员','ADMIN','admin','SYSTEM_ADMIN','system_admin','系统管理员')")
    public Result<Void> toggleRuleStatus(@PathVariable Long id, @RequestBody Boolean status) {
        depreciationRuleService.toggleRuleStatus(id, status);
        return Result.success(status ? "启用折旧规则成功" : "停用折旧规则成功", null);
    }

    /**
     * 删除折旧规则（资产专员，只能删除未被使用的规则）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ASSET_SPECIALIST','asset_specialist','资产专员','ADMIN','admin','SYSTEM_ADMIN','system_admin','系统管理员')")
    public Result<Void> deleteRule(@PathVariable Long id) {
        depreciationRuleService.deleteRule(id);
        return Result.success("删除折旧规则成功", null);
    }

    /**
     * 获取所有启用的折旧规则（用于下拉选择）
     */
    @GetMapping("/active")
    public Result<?> getActiveRules() {
        return Result.success(depreciationRuleService.getActiveRules());
    }
}