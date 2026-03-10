package com.waidp.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门实体
 */
@Data
@Entity
@Table(name = "department")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String leader;

    @Column(length = 20)
    private String leaderPhone;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<User> users;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Transient
    private Long employeeCount;

    @Transient
    private List<String> employeeNames;
}