package com.blindweekend.manager.config;

import com.blindweekend.manager.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT 认证过滤器
 * 优先从 HttpOnly Cookie 读取 Token（管理端），其次从 Authorization Header 读取（Android端）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String JWT_COOKIE_NAME = "admin_token";

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. 提取 Token（优先 Cookie，其次 Authorization Header）
            String token = extractToken(request);

            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                // 2. 解析 Token 获取管理员信息
                Long adminId = jwtUtil.getAdminId(token);
                String username = jwtUtil.getUsername(token);

                // 3. 创建 Authentication 对象（无需密码，已通过JWT验证）
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(adminId, null, authorities);

                // 4. 设置到 SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT认证成功: adminId={}, username={}", adminId, username);
            }
        } catch (Exception e) {
            log.warn("JWT认证失败: {}", e.getMessage());
            // 不抛出异常，让 SecurityConfig 的权限规则处理未认证请求
        }

        // 5. 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 提取 JWT Token
     * 优先级：HttpOnly Cookie > Authorization Header
     * Cookie 用于管理端浏览器（防XSS），Header 用于 Android 客户端
     */
    private String extractToken(HttpServletRequest request) {
        // 1. 尝试从 Cookie 读取（管理端浏览器自动携带）
        String cookieToken = extractCookieValue(request, JWT_COOKIE_NAME);
        if (StringUtils.hasText(cookieToken)) {
            return cookieToken;
        }

        // 2. 尝试从 Authorization Header 读取（Android 客户端）
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    /**
     * 从请求中提取指定 Cookie 的值
     */
    private String extractCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
