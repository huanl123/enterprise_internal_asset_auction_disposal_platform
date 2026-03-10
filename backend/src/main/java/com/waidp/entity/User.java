package com.waidp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
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

    @Column(name = "bid_ban_until")
    private LocalDateTime bidBanUntil;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Transient
    private String departmentName;

    // 为了向后兼容，添加 getName/setName 方法，映射到 realName
    public String getName() {
        return realName;
    }

    public void setName(String name) {
        this.realName = name;
    }

    // 获取中文角色名
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
