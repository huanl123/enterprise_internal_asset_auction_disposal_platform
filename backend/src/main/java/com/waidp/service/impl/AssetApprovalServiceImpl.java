package com.waidp.service.impl;

import com.waidp.entity.Asset;
import com.waidp.entity.AssetHistory;
import com.waidp.repository.AssetRepository;
import com.waidp.service.AssetApprovalService;
import com.waidp.service.AssetHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 资产审核服务实现
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AssetApprovalServiceImpl implements AssetApprovalService {

    private final AssetRepository assetRepository;
    private final AssetHistoryService assetHistoryService;

    @Override
    public void approveAsset(Long assetId, String approverId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("资产不存在"));
        
        // 只能审核待审核状态的资产
        if (!"待审核".equals(asset.getStatus())) {
            throw new RuntimeException("只有待审核状态的资产可以被审核");
        }
        
        // 更新状态为待拍卖
        asset.setStatus("待拍卖");
        asset.setUpdateTime(LocalDateTime.now());
        assetRepository.save(asset);
        
        // 添加审核记录
        assetHistoryService.addHistory(
            assetId, 
            "审核通过", 
            "资产审核通过，状态变更为待拍卖", 
            approverId != null ? Long.parseLong(approverId) : null
        );
    }
    
    @Override
    @Transactional
    public void approveAssetWithPrice(Long assetId, Boolean hasReservePrice, BigDecimal startPrice, 
                                     BigDecimal reservePrice, String remark, String approverId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("资产不存在"));
        
        // 只能审核待审核状态的资产
        if (!"待审核".equals(asset.getStatus())) {
            throw new RuntimeException("只有待审核状态的资产可以被审核");
        }
        
        // 更新资产价格和状态
        asset.setStartPrice(startPrice);
        asset.setHasReservePrice(hasReservePrice);
        if (hasReservePrice && reservePrice != null) {
            asset.setReservePrice(reservePrice);
        }
        asset.setStatus("待拍卖");
        asset.setUpdateTime(LocalDateTime.now());
        assetRepository.save(asset);
        
        // 构建审核记录内容
        StringBuilder historyContent = new StringBuilder("资产审核通过，状态变更为待拍卖。");
        historyContent.append("起拍价调整为：¥").append(startPrice);
        if (hasReservePrice && reservePrice != null) {
            historyContent.append("，保留价设置为：¥").append(reservePrice);
        }
        if (remark != null && !remark.trim().isEmpty()) {
            historyContent.append("。备注：").append(remark);
        }
        
        // 添加审核记录
        assetHistoryService.addHistory(
            assetId, 
            "审核通过", 
            historyContent.toString(), 
            approverId != null ? Long.parseLong(approverId) : null
        );
    }

    @Override
    public void rejectAsset(Long assetId, String reason, String approverId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("资产不存在"));
        
        // 只能审核待审核状态的资产
        if (!"待审核".equals(asset.getStatus())) {
            throw new RuntimeException("只有待审核状态的资产可以被审核");
        }
        
        // 更新状态为已拒绝（或保持待审核，根据需求）
        asset.setStatus("已拒绝");
        asset.setUpdateTime(LocalDateTime.now());
        assetRepository.save(asset);
        
        // 添加审核记录
        assetHistoryService.addHistory(
            assetId, 
            "审核拒绝", 
            "资产审核拒绝，原因：" + reason, 
            approverId != null ? Long.parseLong(approverId) : null
        );
    }

    @Override
    public List<Asset> getPendingAssets() {
        // 使用searchAssets方法获取所有待审核资产
        return assetRepository.searchAssets(null, null, "待审核", null, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }
}