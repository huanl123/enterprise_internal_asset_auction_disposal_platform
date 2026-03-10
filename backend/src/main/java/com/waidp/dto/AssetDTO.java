package com.waidp.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资产数据传输对象，用于避免JPA懒加载序列化问题
 */
@Data
public class AssetDTO {
    private Long id;
    private String code;
    private String name;
    private String category;
    private String specification;
    private String images;
    
    private Long departmentId;
    private String departmentName;
    
    private LocalDate purchaseDate;
    private BigDecimal originalValue;
    private BigDecimal currentValue;
    private BigDecimal startPrice;
    private BigDecimal reservePrice;
    private Boolean hasReservePrice;
    
    private Long depreciationRuleId;
    private String depreciationRuleName;
    
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}