package com.waidp.service;

import com.waidp.entity.Asset;
import java.math.BigDecimal;
import java.util.List;

/**
 * 资产审核服务接口
 */
public interface AssetApprovalService {
    /**
     * 审核资产（通过）
     */
    void approveAsset(Long assetId, String approverId);
    
    /**
     * 审核资产（拒绝）
     */
    void rejectAsset(Long assetId, String reason, String approverId);
    
    /**
     * 审核资产并调整价格（通过）
     * @param assetId 资产ID
     * @param hasReservePrice 是否开启保留价
     * @param startPrice 起拍价
     * @param reservePrice 保留价
     * @param remark 审核备注
     * @param approverId 审核人ID
     */
    void approveAssetWithPrice(Long assetId, Boolean hasReservePrice, BigDecimal startPrice, 
                              BigDecimal reservePrice, String remark, String approverId);
    
    /**
     * 获取待审核资产列表
     */
    List<Asset> getPendingAssets();
}