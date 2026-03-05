package com.waidp.service;

import com.waidp.entity.AssetHistory;
import java.util.List;

/**
 * 资产历史服务接口
 */
public interface AssetHistoryService {
    /**
     * 根据资产ID获取历史记录
     */
    List<AssetHistory> getByAssetId(Long assetId);

    /**
     * 添加历史记录
     */
    void addHistory(Long assetId, String operation, String content, Long operatorId);
}