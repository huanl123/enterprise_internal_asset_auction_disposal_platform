package com.waidp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拍卖活动实体类
 * 管理资产拍卖活动的完整生命周期，包括拍卖创建、出价、延时、成交确认等核心流程。
 * 支持保留价、部门限制、拍卖延时等业务规则。
 */
@Data
@Entity
@Table(name = "auction")
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;//主键ID

    @Column(nullable = false, length = 200)
    private String name;//拍卖名称

    @Column(name = "category", length = 50)
    private String category;//拍卖分类

    @Column(name = "asset_id", insertable = false, updatable = false)
    private Long assetId;//资产ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;//资产对象

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal startPrice;//起始价格

    @Column(name = "current_price", precision = 12, scale = 2)
    private BigDecimal currentPrice;//当前最高出价

    @Column(name = "increment_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal incrementAmount;//最小增幅

    @Column(name = "has_reserve_price")
    private Boolean hasReservePrice = false;//是否启用保留价

    @Column(name = "reserve_price", precision = 12, scale = 2)
    private BigDecimal reservePrice;//保留价

    @Column(nullable = false, length = 20)
    private LocalDateTime startTime;//开始时间

    @Column(nullable = false, length = 20)
    private LocalDateTime endTime;//结束时间

    @Column(nullable = false, length = 20)
    private String status = "not_started";//拍卖状态

    @Column(name = "bid_count")
    private Integer bidCount = 0;//出价次数

    @Column(columnDefinition = "TEXT")
    private String description;//详细说明

    @Column(name = "department_ids", columnDefinition = "TEXT")
    private String departmentIds;//允许的部门ID列表

    @Column(name = "winner_id", insertable = false, updatable = false)
    private Long winnerId;//中标用户ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;//中标用户对象

    @Column(name = "final_price", precision = 12, scale = 2)
    private BigDecimal finalPrice;//最终成交价格

    @Column(name = "create_time")
    private LocalDateTime createTime;//创建时间

    @Column(name = "update_time")
    private LocalDateTime updateTime;//更新时间
}
