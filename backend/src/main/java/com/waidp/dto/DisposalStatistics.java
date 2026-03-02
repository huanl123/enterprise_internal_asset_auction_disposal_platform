package com.waidp.dto;

/**
 * 资产处置统计 DTO
 */
public record DisposalStatistics(
        Long totalAssets,          // 总处置数量
        java.math.BigDecimal totalAmount,   // 总成交金额
        double failureRate,        // 流拍率
        Long succeededCount,       // 成功成交数量
        Long failedCount           // 流拍数量
) {
}
