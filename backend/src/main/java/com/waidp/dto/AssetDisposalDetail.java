package com.waidp.dto;

import com.waidp.entity.Asset;
import com.waidp.entity.AssetHistory;
import com.waidp.entity.Bid;
import com.waidp.entity.Transaction;

import java.util.List;

/**
 * 资产处置详情 DTO
 */
public record AssetDisposalDetail(
        Asset asset,
        Transaction transaction,
        List<AssetHistory> history,
        List<Bid> bids,
        String winnerName,
        String winnerPhone,
        String winnerDepartment,
        java.time.LocalDateTime winTime
) {
}
