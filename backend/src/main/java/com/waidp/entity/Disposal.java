package com.waidp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 处置实体
 */
@Data
@Entity
@Table(name = "disposal")
public class Disposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_id", nullable = false, insertable = false, updatable = false)
    private Long assetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DisposalMethod method;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private DisposalStatus status;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    public enum DisposalMethod {
        AUCTION, DIRECT_SALE, SCRAP
    }

    public enum DisposalStatus {
        PENDING, APPROVED, COMPLETED, CANCELLED
    }
}