package com.waidp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易单DTO：用于避免JPA懒加载序列化问题，并提供前端所需字段
 */
@Data
public class TransactionDTO {
    private Long id;
    private String code;

    private Long auctionId;

    private Long assetId;
    private String assetCode;
    private String assetName;

    private Long winnerId;
    private String winnerName;
    private String winnerPhone;

    private BigDecimal finalPrice;

    private String confirmStatus;
    private LocalDateTime confirmTime;
    private LocalDateTime confirmDeadline;

    private String paymentStatus;
    private LocalDateTime paymentTime;
    private String paymentVoucher;
    private String paymentRemark;

    private String disposalStatus;
    private LocalDateTime disposalTime;
    private String disposalVoucher;
    private String disposalRemark;

    private LocalDateTime paymentDeadline;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static TransactionDTO fromEntity(com.waidp.entity.Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        
        dto.setId(transaction.getId());
        dto.setCode(transaction.getCode());
        dto.setAuctionId(transaction.getAuctionId());
        dto.setAssetId(transaction.getAssetId());
        
        // 通过关联实体获取额外信息
        if (transaction.getAsset() != null) {
            dto.setAssetCode(transaction.getAsset().getCode());
            dto.setAssetName(transaction.getAsset().getName());
        }
        
        dto.setWinnerId(transaction.getWinnerId());
        if (transaction.getWinner() != null) {
            // 注意：User实体可能没有getUsername()方法，需要使用getName()或其他方法
            dto.setWinnerName(transaction.getWinner().getName());
            dto.setWinnerPhone(transaction.getWinner().getPhone());
        }
        
        dto.setFinalPrice(transaction.getFinalPrice());
        dto.setConfirmStatus(transaction.getConfirmStatus());
        dto.setConfirmTime(transaction.getConfirmTime());
        dto.setConfirmDeadline(transaction.getConfirmDeadline());
        dto.setPaymentStatus(transaction.getPaymentStatus());
        dto.setPaymentTime(transaction.getPaymentTime());
        dto.setPaymentVoucher(transaction.getPaymentVoucher());
        dto.setPaymentRemark(transaction.getPaymentRemark());
        dto.setDisposalStatus(transaction.getDisposalStatus());
        dto.setDisposalTime(transaction.getDisposalTime());
        dto.setDisposalVoucher(transaction.getDisposalVoucher());
        dto.setDisposalRemark(transaction.getDisposalRemark());
        dto.setPaymentDeadline(transaction.getPaymentDeadline());
        dto.setCreateTime(transaction.getCreateTime());
        dto.setUpdateTime(transaction.getUpdateTime());
        
        return dto;
    }
}
