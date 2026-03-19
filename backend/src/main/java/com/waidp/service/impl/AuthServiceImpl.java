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
        String normalizedUsername = normalizeUsername(username);
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }

        User user = userRepository.findByUsernameNormalized(normalizedUsername)
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!user.getStatus()) {
            throw new RuntimeException("该账号已被管理员禁用，请联系管理员处理。");
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
        if (user == null) {
            throw new RuntimeException("注册参数不能为空");
        }

        String normalizedUsername = normalizeUsername(user.getUsername());
        String normalizedPassword = normalizePassword(user.getPassword());
        String normalizedName = normalizeName(user.getName());
        String normalizedPhone = normalizePhone(user.getPhone());

        // 检查用户名是否已存在
        if (userRepository.existsByUsernameNormalized(normalizedUsername)) {
            throw new RuntimeException("该工号已被使用，请更换工号或直接登录。");
        }

        // 强制验证部门ID不能为空
        if (user.getDepartmentId() == null) {
            throw new RuntimeException("部门不能为空，请选择所属部门");
        }

        // 验证部门是否存在
        Department department = departmentRepository.findById(user.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("选择的部门不存在"));

        // 注册入口固定为普通员工，禁止通过请求体越权设置角色
        user.setRole("NORMAL_USER");
        user.setUsername(normalizedUsername);
        user.setPassword(normalizedPassword);
        user.setName(normalizedName);
        user.setPhone(normalizedPhone);

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
        if (username == null) {
            return null;
        }
        return userRepository.findByUsernameNormalized(username).orElse(null);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public UserInfo getUserInfoByUsername(String username) {
        User user = userRepository.findByUsernameNormalized(username)
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

    private String normalizeUsername(String username) {
        if (username == null) {
            throw new RuntimeException("工号不能为空");
        }
        String value = username.trim();
        if (value.isEmpty()) {
            throw new RuntimeException("工号不能为空");
        }
        if (value.length() > 16) {
            throw new RuntimeException("工号长度不能超过16位");
        }
        return value;
    }

    private String normalizePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        String value = password;
        if (value.length() < 6) {
            throw new RuntimeException("密码长度至少6位");
        }
        if (value.length() > 16) {
            throw new RuntimeException("密码长度不能超过16位");
        }
        return value;
    }

    private String normalizeName(String name) {
        if (name == null) {
            throw new RuntimeException("姓名不能为空");
        }
        String value = name.trim();
        if (value.isEmpty()) {
            throw new RuntimeException("姓名不能为空");
        }
        return value;
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            throw new RuntimeException("联系方式不能为空");
        }
        String value = phone.trim();
        if (value.isEmpty()) {
            throw new RuntimeException("联系方式不能为空");
        }
        if (!value.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("请输入正确的手机号码");
        }
        return value;
    }
}
