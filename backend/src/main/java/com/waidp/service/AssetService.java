package com.waidp.service;

import com.waidp.dto.AssetDTO;
import com.waidp.entity.Asset;
import com.waidp.entity.AssetHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 资产服务接口
 */
public interface AssetService {

    /**
     * 分页查询资产
     */
    Page<Asset> getAssets(String code, String name, String status, Long departmentId, Pageable pageable);

    /**
     * 分页查询资产 - 返回DTO，避免懒加载问题
     */
    Page<AssetDTO> getAssetsDTO(String code, String name, String status, Long departmentId, Pageable pageable);

    /**
     * 根据ID获取资产
     */
    Asset getAssetById(Long id);

    /**
     * 根据ID获取资产 - 返回DTO，避免懒加载问题
     */
    AssetDTO getAssetDTOById(Long id);

    /**
     * 获取资产历史记录
     */
    List<AssetHistory> getAssetHistory(Long assetId);

    /**
     * 创建资产
     */
    void createAsset(Asset asset, Long operatorId);

    /**
     * 更新资产
     */
    void updateAsset(Asset asset);

    /**
     * 删除资产
     */
    void deleteAsset(Long id);

    /**
     * 重新计算资产价值
     */
    void recalculateValue(Long id);
    
    /**
     * 生成唯一的资产编号
     */
    String generateAssetCode();
}