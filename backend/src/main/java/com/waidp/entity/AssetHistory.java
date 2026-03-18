package com.waidp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 资产历史记录实体类
 * 记录资产的完整操作历史，便于追踪资产状态变化和操作日志。
 * 记录的操作类型包括：创建、修改、折旧计算、拍卖创建、拍卖结果、处置等。
 */
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "asset_history")
public class AssetHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;//主键ID

    @Column(name = "asset_id", nullable = false)
    private Long assetId;//资产ID

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", insertable = false, updatable = false)
    private Asset asset;//资产对象

    @Column(name = "operator_id")
    private Long operatorId;//操作人ID

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", insertable = false, updatable = false)
    private User operator;//操作人对象

    @Column(nullable = false, length = 50)
    private String operation;//操作类型

    @Column(columnDefinition = "TEXT")
    private String content;//操作内容

    @Column(name = "create_time")
    private LocalDateTime createTime;//记录时间

    @Transient
    private String operatorName;//操作人姓名
}