package com.waidp.config;

import com.waidp.entity.User;
import com.waidp.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * 自定义 UserDetailsService
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        // 检查密码格式，如果是纯BCrypt格式（以$2a$开头），添加{bcrypt}前缀
        String password = user.getPassword();
        if (password != null && !password.startsWith("{") && password.startsWith("$2a$")) {
            password = "{bcrypt}" + password;
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(password)
                .authorities(getAuthorities(user))
                .accountLocked(!user.getStatus())
                .disabled(false)
                .build();
    }

    private java.util.Collection<SimpleGrantedAuthority> getAuthorities(User user) {
        java.util.List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        // 使用数据库中的原始角色名，不进行大小写转换
        String role = user.getRole();
        if (role != null) {
            // 直接使用原始角色名，用于Spring Security配置
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            
            // 为了调试，打印一下角色信息
            System.out.println("User: " + user.getUsername() + ", Original Role: " + role + ", Authority: ROLE_" + role);
        }
        
        return authorities;
    }
}