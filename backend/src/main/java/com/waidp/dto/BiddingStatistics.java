package com.waidp.dto;

import java.util.List;

/**
 * 竞拍统计信息 DTO
 */
public record BiddingStatistics(
        Long userId,
        String username,
        String name,
        String department,
        Long auctionCount,        // 参与竞拍场次
        Long winCount,            // 中标次数
        Long defaultCount,         // 违约次数
        java.math.BigDecimal totalAmount,  // 总成交金额
        List<AssetSummary> assets  // 成交资产明细
) {
}
