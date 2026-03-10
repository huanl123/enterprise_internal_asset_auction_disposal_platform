package com.waidp.controller;

import com.waidp.common.PageResult;
import com.waidp.common.Result;
import com.waidp.dto.UserInfo;
import com.waidp.entity.User;
import com.waidp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','admin','SYSTEM_ADMIN','system_admin','系统管理员')")
    public Result<PageResult<User>> getUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
        Page<User> userPage = userService.getUsers(username, name, role, departmentId, status, pageable);
        return Result.success(PageResult.of(userPage));
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId == null) {
            return Result.error(401, "未登录");
        }
        if (!currentUserId.equals(id)) {
            String currentRole = (String) request.getAttribute("role");
            boolean isAdmin = "admin".equals(currentRole)
                    || "ADMIN".equals(currentRole)
                    || "SYSTEM_ADMIN".equals(currentRole)
                    || "system_admin".equals(currentRole)
                    || "系统管理员".equals(currentRole);
            if (!isAdmin) {
                return Result.error(403, "没有权限访问该用户信息");
            }
        }

        User user = userService.getUserById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','admin','SYSTEM_ADMIN','system_admin','系统管理员')")
    public Result<Void> createUser(@RequestBody User user) {
        userService.createUser(user);
        return Result.success("创建用户成功", null);
    }

    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody User user, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId == null) {
            return Result.error(401, "未登录");
        }
        if (!currentUserId.equals(id)) {
            String currentRole = (String) request.getAttribute("role");
            boolean isAdmin = "admin".equals(currentRole)
                    || "ADMIN".equals(currentRole)
                    || "SYSTEM_ADMIN".equals(currentRole)
                    || "system_admin".equals(currentRole)
                    || "系统管理员".equals(currentRole);
            if (!isAdmin) {
                return Result.error(403, "没有权限更新该用户信息");
            }
        }

        user.setId(id);
        userService.updateUser(user);
        return Result.success("更新用户成功", null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','admin','SYSTEM_ADMIN','system_admin','系统管理员')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除用户成功", null);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','admin','SYSTEM_ADMIN','system_admin','系统管理员')")
    public Result<Void> toggleUserStatus(@PathVariable Long id, @RequestBody Boolean status) {
        userService.toggleUserStatus(id, status);
        return Result.success(status != null && status ? "启用用户成功" : "禁用用户成功", null);
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasAnyRole('ADMIN','admin','SYSTEM_ADMIN','system_admin','系统管理员')")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request.newPassword());
        return Result.success("重置密码成功", null);
    }

    @GetMapping("/current")
    public Result<UserInfo> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserInfo userInfo = userService.getUserInfoById(userId);
        return Result.success(userInfo);
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(HttpServletRequest request, @RequestBody User user) {
        Long userId = (Long) request.getAttribute("userId");
        user.setId(userId);
        userService.updateUserProfile(user);
        return Result.success("更新个人信息成功", null);
    }

    @PutMapping("/password")
    public Result<Void> changePassword(HttpServletRequest request, @RequestBody ChangePasswordRequest passwordRequest) {
        Long userId = (Long) request.getAttribute("userId");
        userService.changePassword(userId, passwordRequest.oldPassword(), passwordRequest.newPassword());
        return Result.success("修改密码成功", null);
    }
}

record ChangePasswordRequest(String oldPassword, String newPassword) {
}

record ResetPasswordRequest(String newPassword) {
}

