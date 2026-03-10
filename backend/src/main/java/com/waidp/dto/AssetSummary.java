package com.waidp.dto;

/**
 * 资产摘要 DTO
 */
public record AssetSummary(
        Long assetId,
        String assetName,
        java.math.BigDecimal finalPrice,
        String transactionDate
) {
}
