package com.waidp.config;

import com.waidp.repository.UserRepository;
import com.waidp.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService());
    }

    @Bean
    public RequestAttributeFilter requestAttributeFilter() {
        return new RequestAttributeFilter(jwtUtil);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService(userRepository);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/users/current", "/api/statistics/dashboard", "/", "/error", "/actuator/**", "/api/department/list").permitAll()
                        // 允许所有用户访问文件资源（包括图片）
                        .requestMatchers("/api/files/**", "/uploads/**").permitAll()
                        // 允许认证用户上传资产图片
                        .requestMatchers("/api/asset-images/**").authenticated()
                        // 允许认证用户访问自己的用户信息
                        .requestMatchers(HttpMethod.GET, "/api/users/{id}").authenticated()
                        // 允许认证用户修改个人信息/密码
                        .requestMatchers("/api/users/profile", "/api/users/password").authenticated()
                        // 允许认证用户访问统计功能
                        .requestMatchers("/api/statistics/**").authenticated()
                        // 允许认证用户访问拍卖功能
                        .requestMatchers("/api/auction").authenticated()
                        .requestMatchers("/api/auction/**").authenticated()
                        // 管理员功能 - 兼容不同角色命名（权限实际为 ROLE_xxx）
                        .requestMatchers("/api/users").hasAnyRole("ADMIN", "admin", "SYSTEM_ADMIN", "system_admin", "系统管理员")
                        .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "admin", "SYSTEM_ADMIN", "system_admin", "系统管理员")
                        .requestMatchers("/api/auction/create", "/api/auction/{id}").hasAnyRole(
                                "ADMIN", "admin", "SYSTEM_ADMIN", "system_admin", "系统管理员",
                                "ASSET_SPECIALIST", "asset_specialist", "资产专员"
                        )
                        .requestMatchers("/api/auction/{id}/bid", "/api/auction/{id}/quick-bid").authenticated()
                        // 财务功能 - 财务专员及以上角色可以访问
                        .requestMatchers("/api/finance/**").hasAnyRole(
                                "FINANCE_SPECIALIST", "finance_specialist", "财务专员",
                                "ADMIN", "admin", "SYSTEM_ADMIN", "system_admin", "系统管理员"
                        )
                        // 资产功能 - 资产专员及以上角色可以访问
                        .requestMatchers("/api/assets/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(requestAttributeFilter(), JwtAuthenticationFilter.class);

        return http.build();
    }
}
