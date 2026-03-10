package com.waidp.service.impl;

import com.waidp.entity.AssetHistory;
import com.waidp.repository.AssetHistoryRepository;
import com.waidp.service.AssetHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 资产历史服务实现
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AssetHistoryServiceImpl implements AssetHistoryService {

    private final AssetHistoryRepository assetHistoryRepository;

    @Override
    public List<AssetHistory> getByAssetId(Long assetId) {
        return assetHistoryRepository.findByAssetIdOrderByCreateTimeDesc(assetId);
    }

    @Override
    public void addHistory(Long assetId, String operation, String content, Long operatorId) {
        AssetHistory history = new AssetHistory();
        history.setAssetId(assetId);
        history.setOperation(operation);
        history.setContent(content);
        history.setOperatorId(operatorId);
        history.setCreateTime(LocalDateTime.now());
        assetHistoryRepository.save(history);
    }
}