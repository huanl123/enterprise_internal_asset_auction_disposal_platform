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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
        return new JwtAuthenticationFilter(jwtUtil, userRepository, userDetailsService());
    }

    @Bean
    public RequestAttributeFilter requestAttributeFilter() {
        return new RequestAttributeFilter(jwtUtil, userRepository);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService(userRepository);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/users/current",
                                "/api/statistics/dashboard",
                                "/",
                                "/error",
                                "/actuator/**",
                                "/api/department/list"
                        ).permitAll()
                        .requestMatchers("/api/files/**", "/uploads/**").permitAll()
                        .requestMatchers("/api/asset-images/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users/{id}").authenticated()
                        .requestMatchers("/api/users/profile", "/api/users/password").authenticated()
                        .requestMatchers("/api/statistics/**").authenticated()
                        .requestMatchers("/api/auction", "/api/auction/**").authenticated()
                        .requestMatchers("/api/users", "/api/users/**").hasAnyRole(
                                "ADMIN", "admin",
                                "SYSTEM_ADMIN", "system_admin",
                                "系统管理员"
                        )
                        .requestMatchers("/api/auction/create", "/api/auction/{id}").hasAnyRole(
                                "ADMIN", "admin",
                                "SYSTEM_ADMIN", "system_admin",
                                "系统管理员",
                                "ASSET_SPECIALIST", "asset_specialist",
                                "资产专员"
                        )
                        .requestMatchers("/api/auction/{id}/bid", "/api/auction/{id}/quick-bid", "/api/auction/{id}/withdraw")
                        .authenticated()
                        .requestMatchers("/api/finance/**").hasAnyRole(
                                "FINANCE_SPECIALIST", "finance_specialist",
                                "财务专员",
                                "ADMIN", "admin",
                                "SYSTEM_ADMIN", "system_admin",
                                "系统管理员"
                        )
                        .requestMatchers("/api/assets/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(requestAttributeFilter(), JwtAuthenticationFilter.class);

        return http.build();
    }
}
