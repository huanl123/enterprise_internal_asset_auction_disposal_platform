package com.waidp.service;

import com.waidp.dto.UserInfo;
import com.waidp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 获取用户列表
     */
    Page<User> getUsers(String username, String name, String role, Long departmentId,
                        Boolean status, Pageable pageable);

    /**
     * 根据ID获取用户
     */
    User getUserById(Long id);

    /**
     * 获取用户信息（包含部门信息）
     */
    UserInfo getUserInfoById(Long id);

    /**
     * 创建用户
     */
    void createUser(User user);

    /**
     * 更新用户信息
     */
    void updateUser(User user);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 启用/禁用用户
     */
    void toggleUserStatus(Long userId, Boolean status);

    /**
     * 重置密码
     */
    void resetPassword(Long userId, String newPassword);

    /**
     * 更新个人信息
     */
    void updateUserProfile(User user);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);
}
