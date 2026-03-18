package com.waidp.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门实体类
 * 管理企业的组织架构，每个用户归属于一个部门。
 * 部门信息用于拍卖活动的参与限制等功能。
 */
@Data
@Entity
@Table(name = "department")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;//主键ID

    @Column(nullable = false, length = 50)
    private String name;//部门名称

    @Column(length = 20)
    private String leader;//部门负责人

    @Column(length = 20)
    private String leaderPhone;//负责人电话

    @Column(columnDefinition = "TEXT")
    private String description;//部门描述

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<User> users;//部门用户列表

    @Column(name = "create_time")
    private LocalDateTime createTime;//创建时间

    @Column(name = "update_time")
    private LocalDateTime updateTime;//更新时间

    @Transient
    private Long employeeCount;//员工数量

    @Transient
    private List<String> employeeNames;//员工姓名列表
}