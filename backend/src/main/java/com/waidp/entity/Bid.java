package com.waidp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 竞价记录实体类
 * 记录用户在拍卖活动中的出价信息，包括出价人、出价金额、出价时间等。
 * 支持最高出价标识，用于判断当前领先者。
 */
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "bid")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;//主键ID

    @Column(name = "auction_id", nullable = false, insertable = false, updatable = false)
    private Long auctionId;//拍卖活动ID

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;//拍卖活动对象

    @Column(name = "bidder_id", nullable = false, insertable = false, updatable = false)
    private Long bidderId;//出价用户ID

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidder_id")
    private User bidder;//出价用户对象

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;//出价金额

    @Column(name = "is_highest")
    private Boolean isHighest = false;//是否为最高出价

    @Column(name = "bid_time", nullable = false)
    private LocalDateTime bidTime;//出价时间

    @Column(name = "create_time")
    private LocalDateTime createTime;//创建时间
}
