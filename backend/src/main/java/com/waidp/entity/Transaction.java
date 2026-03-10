package com.waidp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易单实体
 */
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "auction_id", nullable = false, insertable = false, updatable = false)
    private Long auctionId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    @Column(name = "asset_id", insertable = false, updatable = false)
    private Long assetId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @Column(name = "winner_id", nullable = false, insertable = false, updatable = false)
    private Long winnerId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal finalPrice;

    @Column(name = "confirm_status", nullable = false, length = 20)
    private String confirmStatus = "PENDING";

    @Column(name = "confirm_time")
    private LocalDateTime confirmTime;

    @Column(name = "confirm_deadline")
    private LocalDateTime confirmDeadline;

    @Column(name = "payment_status", nullable = false, length = 20)
    private String paymentStatus = "PENDING";

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    @Column(name = "payment_voucher")
    private String paymentVoucher;

    @Column(name = "payment_remark")
    private String paymentRemark;

    @Column(name = "disposal_status", nullable = false, length = 20)
    private String disposalStatus = "PENDING";

    @Column(name = "disposal_time")
    private LocalDateTime disposalTime;

    @Column(name = "disposal_voucher")
    private String disposalVoucher;

    @Column(name = "disposal_remark")
    private String disposalRemark;

    @Column(name = "payment_deadline", nullable = false)
    private LocalDateTime paymentDeadline;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}