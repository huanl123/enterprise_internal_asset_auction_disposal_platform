package com.waidp.repository;

import com.waidp.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import java.math.BigDecimal;

/**
 * 交易单 Repository
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    /**
     * 根据拍卖ID查询交易单
     */
    Optional<Transaction> findByAuctionId(Long auctionId);

    /**
     * 查询超时未确认的交易单
     */
    List<Transaction> findByConfirmStatusInAndConfirmDeadlineBefore(List<String> confirmStatuses, LocalDateTime time);

    /**
     * 根据资产ID + 付款状态 + 处置状态查询交易单（用于处置确认）
     */
    Optional<Transaction> findFirstByAssetIdAndPaymentStatusAndDisposalStatus(Long assetId, String paymentStatus, String disposalStatus);

    /**
     * 根据资产ID查询最新交易单
     */
    Optional<Transaction> findFirstByAssetIdOrderByCreateTimeDesc(Long assetId);

    /**
     * 根据状态查询交易单
     */
    Page<Transaction> findByPaymentStatus(String paymentStatus, Pageable pageable);

    /**
     * 根据中标者ID查询交易单
     */
    List<Transaction> findByWinnerIdOrderByCreateTimeDesc(Long winnerId);

    /**
     * 根据中标者ID分页查询交易单
     */
    Page<Transaction> findByWinnerId(Long winnerId, Pageable pageable);

    /**
     * 搜索交易单
     */
    @Query("SELECT t FROM Transaction t WHERE " +
           "(:code IS NULL OR t.code LIKE %:code%) AND " +
           "(:confirmStatus IS NULL OR t.confirmStatus = :confirmStatus) AND " +
           "(:paymentStatus IS NULL OR t.paymentStatus = :paymentStatus)")
    Page<Transaction> searchTransactions(
            @Param("code") String code,
            @Param("confirmStatus") String confirmStatus,
            @Param("paymentStatus") String paymentStatus,
            Pageable pageable
    );

    /**
     * 统计指定时间范围内的交易单数量
     */
    long countByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计指定状态的交易单数量
     */
    long countByConfirmStatusAndCreateTimeBetween(String confirmStatus, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计指定付款状态的交易单数量
     */
    long countByPaymentStatusAndCreateTimeBetween(String paymentStatus, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 汇总指定状态和时间范围内的交易金额
     */
    @Query("SELECT COALESCE(SUM(t.finalPrice), 0) FROM Transaction t WHERE " +
           "t.paymentStatus = :paymentStatus AND " +
           "t.createTime BETWEEN :startTime AND :endTime")
    Optional<BigDecimal> sumFinalPriceByPaymentStatusAndCreateTimeBetween(
            @Param("paymentStatus") String paymentStatus,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.winnerId = :winnerId " +
           "AND LOWER(t.paymentStatus) = 'pending' AND LOWER(t.confirmStatus) = 'confirmed' " +
           "AND (t.paymentVoucher IS NULL OR TRIM(t.paymentVoucher) = '')")
    long countPendingPaymentByWinnerId(@Param("winnerId") Long winnerId);
}
