package com.waidp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 管理系统用户的基本信息和权限，包括员工、管理员、财务专员、资产专员等角色。
 * 支持用户状态管理、竞拍权限控制等功能。
 */
@Data
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;//主键ID

    @Column(nullable = false, unique = true, length = 50)
    private String username;//用户名

    @Column(nullable = false)
    private String password;//密码

    @Column(length = 100)
    private String email;//邮箱

    @Column(length = 20)
    private String phone;//电话号码

    @Column(name = "real_name", length = 50)
    private String realName;//真实姓名

    @Column(name = "avatar", length = 255)
    private String avatar;//头像

    @Column(name = "department_id", insertable = false, updatable = false)
    private Long departmentId;//所属部门ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonBackReference
    private Department department;//所属部门对象

    @Column(nullable = false, length = 20)
    private String role;//用户角色

    @Column(nullable = false)
    private Boolean status = true;//用户状态

    @Column(name = "bid_ban_until")
    private LocalDateTime bidBanUntil;//竞拍禁用截止时间

    @Column(name = "create_time")
    private LocalDateTime createTime;//创建时间

    @Column(name = "update_time")
    private LocalDateTime updateTime;//更新时间

    @Transient
    private String departmentName;//部门名称

    // 为了向后兼容，添加 getName/setName 方法，映射到 realName
    public String getName() {
        return realName;
    }

    public void setName(String name) {
        this.realName = name;
    }

    /**
     * 获取中文角色名
     * @return 角色的中文显示名称
     */
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
