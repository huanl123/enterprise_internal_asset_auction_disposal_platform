package com.waidp.mapper;

import com.waidp.dto.TransactionDTO;
import com.waidp.entity.Asset;
import com.waidp.entity.Transaction;
import com.waidp.entity.User;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * 交易单实体与DTO之间的映射器
 */
public class TransactionMapper {

    public static TransactionDTO toDTO(Transaction t, Asset asset, User winner) {
        if (t == null) return null;

        TransactionDTO dto = new TransactionDTO();
        dto.setId(t.getId());
        dto.setCode(t.getCode());
        dto.setAuctionId(t.getAuctionId());

        dto.setAssetId(t.getAssetId());
        dto.setAssetCode(asset != null ? asset.getCode() : null);
        dto.setAssetName(asset != null ? asset.getName() : null);

        dto.setWinnerId(t.getWinnerId());
        dto.setWinnerName(winner != null ? (winner.getName() != null ? winner.getName() : winner.getUsername()) : null);
        dto.setWinnerPhone(winner != null ? winner.getPhone() : null);

        dto.setFinalPrice(t.getFinalPrice());

        dto.setConfirmStatus(t.getConfirmStatus());
        dto.setConfirmTime(t.getConfirmTime());
        dto.setConfirmDeadline(t.getConfirmDeadline());

        dto.setPaymentStatus(resolveEffectivePaymentStatus(t));
        dto.setPaymentTime(t.getPaymentTime());
        dto.setPaymentVoucher(t.getPaymentVoucher());
        dto.setPaymentRemark(t.getPaymentRemark());

        dto.setDisposalStatus(t.getDisposalStatus());
        dto.setDisposalTime(t.getDisposalTime());
        dto.setDisposalVoucher(t.getDisposalVoucher());
        dto.setDisposalRemark(t.getDisposalRemark());

        dto.setPaymentDeadline(t.getPaymentDeadline());

        dto.setCreateTime(t.getCreateTime());
        dto.setUpdateTime(t.getUpdateTime());

        return dto;
    }

    private static String resolveEffectivePaymentStatus(Transaction t) {
        String confirmStatus = normalize(t.getConfirmStatus());
        String paymentStatus = normalize(t.getPaymentStatus());

        if ("expired".equals(confirmStatus)) {
            if (paymentStatus.isEmpty() || "pending".equals(paymentStatus)) {
                return "expired";
            }
        }

        if ("confirmed".equals(confirmStatus) && "pending".equals(paymentStatus)) {
            LocalDateTime deadline = t.getPaymentDeadline();
            if (deadline != null && deadline.isBefore(LocalDateTime.now())) {
                return "expired";
            }
        }

        return t.getPaymentStatus();
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
