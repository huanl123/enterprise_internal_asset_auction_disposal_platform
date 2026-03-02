package com.waidp.controller;

import com.waidp.common.Result;
import com.waidp.dto.UserInfo;
import com.waidp.entity.User;
import com.waidp.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<UserLoginResponse> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.username(), request.password());
        UserInfo user = authService.getUserInfoByUsername(request.username());

        return Result.success("登录成功", new UserLoginResponse(user, token));
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody User user) {
        authService.register(user);
        return Result.success("注册成功", null);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!authService.validateToken(token)) {
            return Result.<User>error("Token 无效");
        }

        Long userId = authService.getUserIdFromToken(token);
        User user = authService.getUserById(userId);

        // 不返回密码
        user.setPassword(null);

        return Result.success(user);
    }
}

/**
 * 登录请求
 */
record LoginRequest(String username, String password) {
}

/**
 * 用户登录响应
 */
record UserLoginResponse(UserInfo user, String token) {
}
