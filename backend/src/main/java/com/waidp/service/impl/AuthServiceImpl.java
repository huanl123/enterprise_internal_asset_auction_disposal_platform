package com.waidp.service.impl;

import com.waidp.dto.UserInfo;
import com.waidp.entity.Department;
import com.waidp.entity.User;
import com.waidp.repository.DepartmentRepository;
import com.waidp.repository.UserRepository;
import com.waidp.service.AuthService;
import com.waidp.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!user.getStatus()) {
            throw new RuntimeException("账号已被禁用");
        }

        // 使用 PasswordEncoder 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    @Transactional
    public void register(User user) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 强制验证部门ID不能为空
        if (user.getDepartmentId() == null) {
            throw new RuntimeException("部门不能为空，请选择所属部门");
        }

        // 验证部门是否存在
        Department department = departmentRepository.findById(user.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("选择的部门不存在"));

        // 设置默认角色
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("NORMAL_USER");
        }

        // 编码密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(true);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDepartment(department);

        userRepository.save(user);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public UserInfo getUserInfoByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Department department = null;
        if (user.getDepartmentId() != null) {
            department = departmentRepository.findById(user.getDepartmentId())
                    .orElse(null);
        }

        return new UserInfo(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getPhone(),
                user.getDepartmentId(),
                department != null ? department.getName() : "",
                user.getRole(),
                user.getStatus(),
                user.getAvatar(),
                user.getCreateTime()
        );
    }

    @Override
    @Transactional
    public void updateUser(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 只更新允许修改的字段
        existingUser.setName(user.getName());
        existingUser.setPhone(user.getPhone());
        existingUser.setUpdateTime(LocalDateTime.now());

        userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long userId, Boolean status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        // 使用 PasswordEncoder 编码新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    @Override
    public Long getUserIdFromToken(String token) {
        return jwtUtil.getUserIdFromToken(token);
    }
}