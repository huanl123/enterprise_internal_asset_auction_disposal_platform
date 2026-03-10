package com.waidp.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拍卖数据传输对象，用于避免JPA懒加载序列化问题
 */
@Data
public class AuctionDTO {
    private Long id;
    private String name;
    private String category;
    
    // 资产信息
    private Long assetId;
    private String assetName;
    private String assetCode;
    private String assetSpecification;
    
    // 价格信息
    private BigDecimal startPrice;
    private BigDecimal currentPrice;
    private BigDecimal incrementAmount;
    private Boolean hasReservePrice;
    private BigDecimal reservePrice;
    
    // 时间信息
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 状态信息
    private String status;
    private Integer bidCount;
    
    // 其他信息
    private String description;
    private String departmentIds;
    
    // 竞拍信息
    private Long winnerId;
    private String winnerName;
    private BigDecimal finalPrice;

    // 我的竞拍信息
    private BigDecimal myBid;
    private BigDecimal myMaxBid;
    private Boolean isHighest;
    private String result;
    private String confirmStatus;
    private LocalDateTime winTime;
}