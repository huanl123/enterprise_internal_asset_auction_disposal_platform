package com.waidp.repository;

import com.waidp.entity.AssetHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 资产历史记录 Repository
 */
@Repository
public interface AssetHistoryRepository extends JpaRepository<AssetHistory, Long> {

    /**
     * 按操作类型查询历史记录（按时间倒序）
     */
    List<AssetHistory> findByOperationOrderByCreateTimeDesc(String operation);

    /**
     * 查询资产的所有历史记录
     */
    List<AssetHistory> findByAssetIdOrderByCreateTimeDesc(Long assetId);

    /**
     * 查询指定资产ID列表的历史记录（按时间倒序）
     */
    List<AssetHistory> findByAssetIdInOrderByCreateTimeDesc(List<Long> assetIds);

    /**
     * 查询指定资产ID列表中包含特定操作的历史记录
     */
    @Query("SELECT h FROM AssetHistory h WHERE h.assetId IN :assetIds AND h.content LIKE %:operation%")
    List<AssetHistory> findByAssetIdInAndOperationContains(@Param("assetIds") List<Long> assetIds,
                                                          @Param("operation") String operation);
}
