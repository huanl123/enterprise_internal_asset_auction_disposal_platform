package com.waidp.config;

import com.waidp.entity.User;
import com.waidp.repository.UserRepository;
import com.waidp.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RequestAttributeFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);

            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                Integer tokenVersion = jwtUtil.getTokenVersionFromToken(token);
                User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

                if (user != null && tokenVersion != null && Boolean.TRUE.equals(user.getStatus())
                        && tokenVersion.equals(user.getTokenVersion())) {
                    request.setAttribute("userId", userId);
                    request.setAttribute("username", user.getUsername());
                    request.setAttribute("role", user.getRole());
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
