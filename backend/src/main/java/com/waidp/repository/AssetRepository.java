package com.waidp.repository;

import com.waidp.entity.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 资产 Repository
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {

    /**
     * 根据资产编号查询
     */
    Asset findByCode(String code);

    /**
     * 检查资产编号是否存在
     */
    boolean existsByCode(String code);

    /**
     * 根据部门ID查询资产
     */
    List<Asset> findByDepartmentId(Long departmentId);

    /**
     * 根据状态查询资产
     */
    Page<Asset> findByStatus(String status, Pageable pageable);

    /**
     * 根据状态查询资产（列表）
     */
    List<Asset> findByStatus(String status);
    
    /**
     * 根据多个状态查询资产
     */
    Page<Asset> findByStatusIn(List<String> statuses, Pageable pageable);

    /**
     * 统计使用指定折旧规则的资产数量
     */
    long countByDepreciationRuleId(Long depreciationRuleId);

    /**
     * 统计使用指定折旧规则的资产数量（原生SQL，避免映射差异）
     */
    @Query(value = "SELECT COUNT(*) FROM asset WHERE depreciation_rule_id = :ruleId", nativeQuery = true)
    long countByDepreciationRuleIdNative(@Param("ruleId") Long ruleId);

    /**
     * 搜索资产
     */
    @Query("SELECT a FROM Asset a WHERE " +
           "(:code IS NULL OR a.code LIKE %:code%) AND " +
           "(:name IS NULL OR a.name LIKE %:name%) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:departmentId IS NULL OR a.departmentId = :departmentId)")
    Page<Asset> searchAssets(
            @Param("code") String code,
            @Param("name") String name,
            @Param("status") String status,
            @Param("departmentId") Long departmentId,
            Pageable pageable
    );

    /**
     * 查询指定日期的最大资产编号
     */
    @Query(value = "SELECT MAX(code) FROM asset WHERE code LIKE CONCAT('ASSET-', :datePart, '-%')", nativeQuery = true)
    String findMaxCodeByDate(@Param("datePart") String datePart);
}