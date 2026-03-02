package com.waidp.dto;

import com.waidp.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户信息响应
 */
@Data
@AllArgsConstructor
public class UserInfo {
    private Long id;
    private String username;
    private String name;
    private String phone;
    private Long departmentId;
    private String departmentName;
    private String role;
    private Boolean status;
    private String avatar;
    private java.time.LocalDateTime createTime;

    public UserInfo(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.phone = user.getPhone();
        this.departmentId = user.getDepartmentId();
        this.departmentName = user.getDepartment() != null ? user.getDepartment().getName() : "";
        // 转换英文角色名为中文
        this.role = convertRoleName(user.getRole());
        this.status = user.getStatus();
        this.avatar = user.getAvatar();
        this.createTime = user.getCreateTime();
    }

    /**
     * 将英文角色名转换为中文角色名
     */
    private String convertRoleName(String role) {
        if (role == null) {
            return null;
        }
        return switch (role.toUpperCase()) {
            case "ADMIN", "SYSTEM_ADMIN" -> "系统管理员";
            case "ASSET_SPECIALIST" -> "资产专员";
            case "FINANCE_SPECIALIST" -> "财务专员";
            case "NORMAL_USER", "EMPLOYEE" -> "普通员工";
            default -> role;
        };
    }
}