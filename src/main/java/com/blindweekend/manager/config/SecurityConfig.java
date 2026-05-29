package com.blindweekend.manager.config;

import com.blindweekend.manager.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

/**
 * Spring Security 配置类
 * - JWT Stateless 认证模式
 * - Double Submit Cookie CSRF 防护
 * - 静态资源公开，API 需认证
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CsrfFilter csrfFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. 禁用 Spring 内置 CSRF（使用自定义 Double Submit Cookie 模式替代）
            .csrf(AbstractHttpConfigurer::disable)

            // 2. 使用 Stateless 会话策略（不创建/使用 Session）
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 3. 保留原有 WebConfig 的 CORS 配置
            .cors(Customizer.withDefaults())

            // 4. 在用户名密码过滤器之前插入 JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // 5. 在 JWT 过滤器之后插入 CSRF 过滤器（Double Submit Cookie 模式）
            .addFilterAfter(csrfFilter, JwtAuthenticationFilter.class)

            // 5. 配置 URL 权限规则
            .authorizeHttpRequests(auth -> auth
                // 允许预检请求（CORS OPTIONS）
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()

                // 公开接口：管理员登录
                .requestMatchers(HttpMethod.POST, "/admin/auth/login").permitAll()

                // 公开接口：用户注册 + 用户登录（Android 端使用）
                .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login").permitAll()

                // 公开接口：Android 客户端只读接口（无需登录即可访问）
                .requestMatchers(
                    HttpMethod.GET,
                    "/admin/spots/active",
                    "/admin/templates/active",
                    "/admin/templates/*/segments"
                ).permitAll()

                // 公开接口：Android 客户端盲盒相关（列表、详情、发布、参与、个人记录）
                // 注意：这些接口路径为 /admin/blindboxes/*，但实际由 Android 用户端调用，
                //       后续可考虑迁移至独立的前端 API 前缀（如 /api/v1/blindboxes）
                .requestMatchers(
                    HttpMethod.GET, "/admin/blindboxes", "/admin/blindboxes/*"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/admin/blindboxes", "/admin/blindboxes/*/join").permitAll()

                // ⭐ Android 用户个人盲盒记录（/admin/blindboxes/user/{userId}/... 有多级路径，* 无法匹配）
                .requestMatchers(
                    HttpMethod.GET,
                    "/admin/blindboxes/user/*/participated",
                    "/admin/blindboxes/user/*/published",
                    "/admin/blindboxes/user/*/stats"
                ).permitAll()

                // 静态资源：公开（前端 JS 自行做 token 校验）
                .requestMatchers(
                    "/",                    // 根路径 → index.html
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico",
                    "/*.html"
                ).permitAll()

                // 公开接口：用户个人资料更新（Android 端 Bearer Token 认证，不需 ADMIN 角色）
                .requestMatchers(HttpMethod.PUT, "/auth/profile").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/profile/avatar").permitAll()

                // 文件上传公开（图片资源需可直接访问）
                .requestMatchers("/files/**").permitAll()

                // 其他所有 /admin/** 接口需要 ADMIN 角色（纯管理后台操作）
                .requestMatchers("/admin/**").hasAnyRole("ADMIN")

                // 其他所有请求需要认证
                .anyRequest().authenticated()
            )

            // 6. 自定义异常处理器（返回 JSON 格式）
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                        "{\"code\":401,\"message\":\"未登录或登录已过期，请重新登录\",\"data\":null}"
                    );
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                        "{\"code\":403,\"message\":\"权限不足，拒绝访问\",\"data\":null}"
                    );
                })
            );

        return http.build();
    }

    /**
     * BCrypt 密码编码器 Bean
     * （Spring Security 要求存在此 Bean，实际密码校验使用 Hutool BCrypt）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
