package com.waidp.service;

import com.waidp.entity.Department;
import com.waidp.dto.UserInfo;
import com.waidp.entity.User;
import com.waidp.util.JwtUtil;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    String login(String username, String password);

    /**
     * 用户注册
     */
    void register(User user);

    /**
     * 根据用户名获取用户
     */
    User getUserByUsername(String username);

    /**
     * 获取用户信息（包含部门信息）
     */
    UserInfo getUserInfoByUsername(String username);

    /**
     * 根据ID获取用户
     */
    User getUserById(Long id);

    /**
     * 更新用户信息
     */
    void updateUser(User user);

    /**
     * 启用/禁用用户
     */
    void toggleUserStatus(Long userId, Boolean status);

    /**
     * 重置用户密码
     */
    void resetPassword(Long userId, String newPassword);

    /**
     * 验证 Token
     */
    boolean validateToken(String token);

    /**
     * 从 Token 中获取用户ID
     */
    Long getUserIdFromToken(String token);
}
