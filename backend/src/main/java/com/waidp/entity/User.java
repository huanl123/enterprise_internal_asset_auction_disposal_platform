package com.waidp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "real_name", length = 50)
    private String realName;

    @Column(name = "avatar", length = 255)
    private String avatar;

    @Column(name = "department_id", insertable = false, updatable = false)
    private Long departmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonBackReference
    private Department department;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(nullable = false)
    private Boolean status = true;

    @Column(name = "token_version", nullable = false)
    private Integer tokenVersion = 0;

    @Column(name = "bid_ban_until")
    private LocalDateTime bidBanUntil;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Transient
    private String departmentName;

    public String getName() {
        return realName;
    }

    public void setName(String name) {
        this.realName = name;
    }

    public String getRoleDisplayName() {
        return switch (role) {
            case "ADMIN", "SYSTEM_ADMIN" -> "系统管理员";
            case "ASSET_SPECIALIST" -> "资产专员";
            case "FINANCE_SPECIALIST" -> "财务专员";
            case "NORMAL_USER", "EMPLOYEE" -> "普通员工";
            default -> role;
        };
    }
}
