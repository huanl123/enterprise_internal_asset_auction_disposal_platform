package com.waidp.repository;

import com.waidp.entity.DepreciationRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 折旧规则 Repository
 */
@Repository
public interface DepreciationRuleRepository extends JpaRepository<DepreciationRule, Long> {

    /**
     * 查询所有启用的折旧规则
     */
    List<DepreciationRule> findByStatusTrueOrderByCreateTimeDesc();

    /**
     * 根据状态查询折旧规则
     */
    List<DepreciationRule> findByStatus(Boolean status);

    /**
     * 查询所有折旧规则
     */
    List<DepreciationRule> findAllByOrderByCreateTimeDesc();

    /**
     * 检查规则名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 检查规则名称是否存在（排除指定ID）
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * 多条件搜索折旧规则
     */
    @Query("SELECT r FROM DepreciationRule r WHERE " +
           "(:name IS NULL OR r.name LIKE %:name%) AND " +
           "(:status IS NULL OR r.status = :status)")
    Page<DepreciationRule> searchRules(@Param("name") String name,
                                       @Param("status") Boolean status,
                                       Pageable pageable);
}
