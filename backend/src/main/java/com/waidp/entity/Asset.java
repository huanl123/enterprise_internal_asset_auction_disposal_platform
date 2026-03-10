package com.waidp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资产实体
 */
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "asset")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 50)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String specification;

    @Column(columnDefinition = "TEXT")
    private String images;

    @Column(name = "department_id", insertable = false, updatable = false)
    private Long departmentId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false)
    private LocalDate purchaseDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal originalValue;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal currentValue;

    @Column(name = "start_price", precision = 12, scale = 2)
    private BigDecimal startPrice;

    @Column(name = "depreciation_rule_id", insertable = false, updatable = false)
    private Long depreciationRuleId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depreciation_rule_id")
    private DepreciationRule depreciationRule;

    @Column(name = "reserve_price", precision = 12, scale = 2)
    private BigDecimal reservePrice;

    @Column(name = "has_reserve_price")
    private Boolean hasReservePrice = false;

    @Column(nullable = false, length = 20)
    private String status = "待审核";

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
