package com.waidp.service.impl;

import com.waidp.entity.Transaction;
import com.waidp.repository.AssetRepository;
import com.waidp.repository.TransactionRepository;
import com.waidp.repository.UserRepository;
import com.waidp.service.AssetHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * 中标确认超时处理（独立事务）。
 */
@Service
@RequiredArgsConstructor
public class AuctionConfirmationExpiryService {

    private final TransactionRepository transactionRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final AssetHistoryService assetHistoryService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expireInNewTransaction(Long transactionId, String reason) {
        if (transactionId == null) {
            return;
        }

        Transaction tx = transactionRepository.findById(transactionId).orElse(null);
        if (tx == null) {
            return;
        }

        String status = tx.getConfirmStatus() == null
                ? ""
                : tx.getConfirmStatus().trim().toLowerCase(Locale.ROOT);
        if (!"pending".equals(status)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        tx.setConfirmStatus("expired");
        if (tx.getPaymentStatus() != null) {
            String paymentStatus = tx.getPaymentStatus().trim().toLowerCase(Locale.ROOT);
            if ("pending".equals(paymentStatus)) {
                tx.setPaymentStatus("expired");
            }
        } else {
            tx.setPaymentStatus("expired");
        }
        if (tx.getDisposalStatus() != null) {
            String disposalStatus = tx.getDisposalStatus().trim().toLowerCase(Locale.ROOT);
            if ("pending".equals(disposalStatus)) {
                tx.setDisposalStatus("cancelled");
            }
        }
        tx.setUpdateTime(now);
        transactionRepository.save(tx);

        if (tx.getAssetId() != null) {
            assetRepository.findById(tx.getAssetId()).ifPresent(asset -> {
                asset.setStatus("待拍卖");
                asset.setUpdateTime(now);
                assetRepository.save(asset);
                assetHistoryService.addHistory(asset.getId(), "拍卖结果", reason + "，资产退回待拍卖", tx.getWinnerId());
            });
        }

        if (tx.getWinnerId() != null) {
            userRepository.findById(tx.getWinnerId()).ifPresent(user -> {
                user.setBidBanUntil(now.plusMonths(3));
                userRepository.save(user);
            });
        }
    }
}
