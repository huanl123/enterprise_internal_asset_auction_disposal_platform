package com.waidp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 折旧规则实体类
 * 定义资产折旧的计算规则，通过不同的规则自动计算资产现值。
 * 折旧计算公式：现值 = 原值 - (原值 - 残值) * (已使用月数 / 使用年限月数)
 */
@Data
@Entity
@Table(name = "depreciation_rule")
public class DepreciationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;//主键ID

    @Column(nullable = false, length = 100)
    private String name;//规则名称

    @Column(nullable = false)
    private Integer usefulLife;//使用年限（年）

    @Column(nullable = false)
    private Double salvageRate;//残值率（百分比）

    @Column(columnDefinition = "TEXT")
    private String description;//规则描述

    @Column(nullable = false)
    private Boolean status = true;//规则状态

    @Column(name = "asset_count")
    private Integer assetCount = 0;//使用该规则的资产数量

    @Column(name = "create_time")
    private LocalDateTime createTime;//创建时间

    @Column(name = "update_time")
    private LocalDateTime updateTime;//更新时间
}
