package com.waidp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资产实体类
 * 存储企业废旧资产的基本信息，包括资产编码、名称、类别、规格、价值等。
 * 支持资产折旧计算、拍卖流转等功能。
 */
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "asset")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;//主键ID

    @Column(nullable = false, unique = true, length = 50)
    private String code;//资产编码

    @Column(nullable = false, length = 200)
    private String name;//资产名称

    @Column(length = 50)
    private String category;//资产分类

    @Column(columnDefinition = "TEXT")
    private String specification;//规格参数

    @Column(columnDefinition = "TEXT")
    private String images;//图片URL

    @Column(name = "department_id", insertable = false, updatable = false)
    private Long departmentId;//部门ID

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;//部门对象

    @Column(nullable = false)
    private LocalDate purchaseDate;//购置时间

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal originalValue;//原始价格

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal currentValue;//当前价值

    @Column(name = "start_price", precision = 12, scale = 2)
    private BigDecimal startPrice;//起始价格

    @Column(name = "depreciation_rule_id", insertable = false, updatable = false)
    private Long depreciationRuleId;//折旧规则ID

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depreciation_rule_id")
    private DepreciationRule depreciationRule;//折旧规则对象

    @Column(name = "reserve_price", precision = 12, scale = 2)
    private BigDecimal reservePrice;//保留价

    @Column(name = "has_reserve_price")
    private Boolean hasReservePrice = false;//是否启用保留价

    @Column(nullable = false, length = 20)
    private String status = "待审核";//资产状态

    @Column(name = "create_time")
    private LocalDateTime createTime;//创建时间

    @Column(name = "update_time")
    private LocalDateTime updateTime;//更新时间
}
