package com.waidp.service.impl;

import com.waidp.dto.UserInfo;
import com.waidp.entity.Department;
import com.waidp.entity.User;
import com.waidp.repository.DepartmentRepository;
import com.waidp.repository.UserRepository;
import com.waidp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<User> getUsers(String username, String name, String role, Long departmentId,
                                Boolean status, Pageable pageable) {
        String normalizedRole = (role == null || role.trim().isEmpty()) ? null : normalizeRoleCode(role);
        Page<User> userPage = userRepository.searchUsers(username, name, normalizedRole, departmentId, status, pageable);
        List<User> users = userPage.getContent();
        Set<Long> departmentIds = users.stream()
                .map(User::getDepartmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!departmentIds.isEmpty()) {
            Map<Long, String> departmentMap = departmentRepository.findAllById(departmentIds)
                    .stream()
                    .collect(Collectors.toMap(Department::getId, Department::getName));
            for (User user : users) {
                Long deptId = user.getDepartmentId();
                if (deptId != null) {
                    user.setDepartmentName(departmentMap.getOrDefault(deptId, ""));
                }
            }
        }
        return userPage;
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public UserInfo getUserInfoById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Department department = departmentRepository.findById(user.getDepartmentId())
                .orElse(null);

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
    public void createUser(User user) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("该工号已被使用，请更换工号或直接登录。");
        }

        // 强制验证部门ID不能为空
        if (user.getDepartmentId() == null) {
            throw new RuntimeException("部门不能为空，请选择所属部门");
        }

        // 验证部门是否存在
        Department department = departmentRepository.findById(user.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("选择的部门不存在"));

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(normalizeRoleCode(user.getRole()));
        user.setStatus(true);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDepartment(department);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUser(User user) {
        User existing = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 只更新允许修改的字段
        existing.setName(user.getName());
        existing.setPhone(user.getPhone());
        existing.setRole(normalizeRoleCode(user.getRole()));

        // 如果部门ID改变，更新部门
        if (user.getDepartmentId() != null && !user.getDepartmentId().equals(existing.getDepartmentId())) {
            Department department = departmentRepository.findById(user.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("部门不存在"));
            existing.setDepartment(department);
            existing.setDepartmentId(user.getDepartmentId());
        }

        existing.setUpdateTime(LocalDateTime.now());
        userRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 不允许删除系统管理员
        if ("ROLE_admin".equals(user.getRole()) || "ROLE_system_admin".equals(user.getRole())) {
            throw new RuntimeException("不允许删除系统管理员账号");
        }

        // 检查用户是否有未完成的交易
        if (userHasActiveTransactions(id)) {
            throw new RuntimeException("该用户有未完成的交易，无法删除");
        }

        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long userId, Boolean status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 不允许禁用系统管理员
        if (!status && ("ROLE_admin".equals(user.getRole()) || "ROLE_system_admin".equals(user.getRole()))) {
            throw new RuntimeException("不允许禁用系统管理员账号");
        }

        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUserProfile(User user) {
        User existing = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 普通用户只能修改姓名、电话、头像
        existing.setName(user.getName());
        existing.setPhone(user.getPhone());
        if (user.getAvatar() != null) {
            existing.setAvatar(user.getAvatar());
        }
        existing.setUpdateTime(LocalDateTime.now());

        userRepository.save(existing);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * 检查用户是否有未完成的交易
     */
    private boolean userHasActiveTransactions(Long userId) {
        // 这里需要实现检查逻辑
        // 可以查询Transaction表中该用户是否有状态为pending或confirmed的交易
        return false;
    }

    private String normalizeRoleCode(String role) {
        if (role == null || role.trim().isEmpty()) {
            return "NORMAL_USER";
        }
        String normalized = role.trim().replaceFirst("(?i)^ROLE_", "");
        String key = normalized.toLowerCase(Locale.ROOT);
        return switch (key) {
            case "admin", "system_admin", "系统管理员" -> "SYSTEM_ADMIN";
            case "asset_specialist", "资产专员" -> "ASSET_SPECIALIST";
            case "finance_specialist", "财务专员" -> "FINANCE_SPECIALIST";
            case "employee", "normal_user", "user", "普通员工" -> "NORMAL_USER";
            default -> throw new RuntimeException("无效角色: " + role);
        };
    }
}
