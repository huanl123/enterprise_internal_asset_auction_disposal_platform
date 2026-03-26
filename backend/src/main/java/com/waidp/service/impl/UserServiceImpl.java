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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("该工号已被使用，请更换工号或直接登录。");
        }

        if (user.getDepartmentId() == null) {
            throw new RuntimeException("部门不能为空，请选择所属部门");
        }

        Department department = departmentRepository.findById(user.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("选择的部门不存在"));

        validatePassword(user.getPassword());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(normalizeRoleCode(user.getRole()));
        user.setStatus(true);
        user.setTokenVersion(0);
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

        existing.setName(user.getName());
        existing.setPhone(user.getPhone());

        String newRole = normalizeRoleCode(user.getRole());
        if (!Objects.equals(existing.getRole(), newRole)) {
            existing.setTokenVersion(nextTokenVersion(existing));
        }
        existing.setRole(newRole);

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

        if ("ROLE_admin".equals(user.getRole()) || "ROLE_system_admin".equals(user.getRole())) {
            throw new RuntimeException("不允许删除系统管理员账号");
        }

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

        if (!status && ("ROLE_admin".equals(user.getRole()) || "ROLE_system_admin".equals(user.getRole()))) {
            throw new RuntimeException("不允许禁用系统管理员账号");
        }

        user.setStatus(status);
        user.setTokenVersion(nextTokenVersion(user));
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        validatePassword(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenVersion(nextTokenVersion(user));
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUserProfile(User user) {
        User existing = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

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

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        validatePassword(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenVersion(nextTokenVersion(user));
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
    }

    private boolean userHasActiveTransactions(Long userId) {
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

    private int nextTokenVersion(User user) {
        return (user.getTokenVersion() == null ? 0 : user.getTokenVersion()) + 1;
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        if (password.length() < 6) {
            throw new RuntimeException("密码长度至少6位");
        }
        if (password.length() > 16) {
            throw new RuntimeException("密码长度不能超过16位");
        }
    }
}
