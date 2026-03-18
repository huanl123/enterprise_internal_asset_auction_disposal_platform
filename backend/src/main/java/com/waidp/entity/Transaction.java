package com.waidp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易单实体类
 * 管理拍卖成交后的交易流程，包括成交确认、付款、资产处置等环节。
 * 每个成功的拍卖都会生成一个交易单，跟踪从中标到资产处置归档的全过程。
 */
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;//主键ID

    @Column(nullable = false, unique = true, length = 50)
    private String code;//交易单编号

    @Column(name = "auction_id", nullable = false, insertable = false, updatable = false)
    private Long auctionId;//拍卖活动ID

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;//拍卖活动对象

    @Column(name = "asset_id", insertable = false, updatable = false)
    private Long assetId;//资产ID

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;//资产对象

    @Column(name = "winner_id", nullable = false, insertable = false, updatable = false)
    private Long winnerId;//中标者ID

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;//中标者对象

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal finalPrice;//成交价

    @Column(name = "confirm_status", nullable = false, length = 20)
    private String confirmStatus = "PENDING";//成交确认状态

    @Column(name = "confirm_time")
    private LocalDateTime confirmTime;//成交确认时间

    @Column(name = "confirm_deadline")
    private LocalDateTime confirmDeadline;//成交确认截止时间

    @Column(name = "payment_status", nullable = false, length = 20)
    private String paymentStatus = "PENDING";//付款状态

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;//付款时间

    @Column(name = "payment_voucher")
    private String paymentVoucher;//付款凭证

    @Column(name = "payment_remark")
    private String paymentRemark;//付款备注

    @Column(name = "disposal_status", nullable = false, length = 20)
    private String disposalStatus = "PENDING";//资产处置状态

    @Column(name = "disposal_time")
    private LocalDateTime disposalTime;//资产处置时间

    @Column(name = "disposal_voucher")
    private String disposalVoucher;//资产处置凭证

    @Column(name = "disposal_remark")
    private String disposalRemark;//资产处置备注

    @Column(name = "payment_deadline", nullable = false)
    private LocalDateTime paymentDeadline;//付款截止时间

    @Column(name = "create_time")
    private LocalDateTime createTime;//创建时间

    @Column(name = "update_time")
    private LocalDateTime updateTime;//更新时间
}