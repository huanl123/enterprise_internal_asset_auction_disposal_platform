package com.waidp.dto;

/**
 * 财务统计 DTO
 */
public record FinanceStatistics(
        long totalTransactions,
        java.math.BigDecimal totalAmount,
        long confirmedTransactions,
        long pendingTransactions,
        long rejectedTransactions
) {
}
