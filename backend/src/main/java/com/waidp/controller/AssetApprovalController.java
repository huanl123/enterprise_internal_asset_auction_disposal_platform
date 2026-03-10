package com.waidp.controller;

import com.waidp.common.Result;
import com.waidp.entity.Asset;
import com.waidp.service.AssetApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 资产审核控制器
 */
@RestController
@RequestMapping("/api/asset-approval")
@RequiredArgsConstructor
public class AssetApprovalController {

    private final AssetApprovalService assetApprovalService;

    /**
     * 获取待审核资产列表（资产专员、财务专员、系统管理员）
     */
    @GetMapping("/pending")
    public Result<List<Asset>> getPendingAssets() {
        return Result.success(assetApprovalService.getPendingAssets());
    }

    /**
     * 审核通过资产（资产专员、财务专员、系统管理员）
     */
    @PostMapping("/{assetId}/approve")
    public Result<Void> approveAsset(
            @PathVariable Long assetId,
            @RequestParam(required = false) String approverId) {
        
        assetApprovalService.approveAsset(assetId, approverId);
        return Result.success();
    }
    
    /**
     * 审核通过资产并调整价格（资产专员、财务专员、系统管理员）
     * @param assetId 资产ID
     * @param hasReservePrice 是否开启保留价
     * @param startPrice 起拍价
     * @param reservePrice 保留价
     * @param remark 审核备注
     * @param approverId 审核人ID
     */
    @PostMapping("/{assetId}/approve-with-price")
    public Result<Void> approveAssetWithPrice(
            @PathVariable Long assetId,
            @RequestParam Boolean hasReservePrice,
            @RequestParam BigDecimal startPrice,
            @RequestParam(required = false) BigDecimal reservePrice,
            @RequestParam(required = false) String remark,
            @RequestParam(required = false) String approverId) {
        
        assetApprovalService.approveAssetWithPrice(
            assetId, hasReservePrice, startPrice, reservePrice, remark, approverId);
        return Result.success();
    }

    /**
     * 审核拒绝资产（资产专员、财务专员、系统管理员）
     */
    @PostMapping("/{assetId}/reject")
    public Result<Void> rejectAsset(
            @PathVariable Long assetId,
            @RequestParam String reason,
            @RequestParam(required = false) String approverId) {
        
        assetApprovalService.rejectAsset(assetId, reason, approverId);
        return Result.success();
    }
}