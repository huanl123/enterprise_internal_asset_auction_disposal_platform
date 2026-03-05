package com.waidp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拍卖实体
 */
@Data
@Entity
@Table(name = "auction")
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "asset_id", insertable = false, updatable = false)
    private Long assetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal startPrice;

    @Column(name = "current_price", precision = 12, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "increment_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal incrementAmount;

    @Column(name = "has_reserve_price")
    private Boolean hasReservePrice = false;

    @Column(name = "reserve_price", precision = 12, scale = 2)
    private BigDecimal reservePrice;

    @Column(nullable = false, length = 20)
    private LocalDateTime startTime;

    @Column(nullable = false, length = 20)
    private LocalDateTime endTime;

    @Column(nullable = false, length = 20)
    private String status = "not_started";

    @Column(name = "bid_count")
    private Integer bidCount = 0;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "department_ids", columnDefinition = "TEXT")
    private String departmentIds;

    @Column(name = "winner_id", insertable = false, updatable = false)
    private Long winnerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    @Column(name = "final_price", precision = 12, scale = 2)
    private BigDecimal finalPrice;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
