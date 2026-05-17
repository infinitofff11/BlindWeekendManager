package com.blindweekend.manager.controller;

import com.blindweekend.manager.common.Result;
import com.blindweekend.manager.config.CsrfFilter;
import com.blindweekend.manager.dto.AdminLoginDTO;
import com.blindweekend.manager.service.AdminAuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员认证接口
 * 与 AuthController（用户认证 /auth/*）完全独立
 */
@Slf4j
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    /**
     * 管理员登录
     * POST /admin/auth/login
     * 请求体: {"username": "admin", "password": "admin123"}
     * 响应: {code:200, message:"登录成功", data:{token:"jwt...", admin:{...}, csrfToken:"..."}}
     *
     * 安全增强：
     * - JWT Token 通过 HttpOnly Cookie 设置，前端 JS 无法读取，防 XSS 窃取
     * - CSRF Token 通过 SameSite=Strict Cookie 设置，写操作需附加 X-XSRF-TOKEN Header
     * - 响应体中仍返回 token（供 Android 客户端使用 Authorization Header）
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid AdminLoginDTO dto,
                                              HttpServletResponse response) {
        log.info("收到管理员登录请求: username={}", dto.getUsername());
        Map<String, Object> data = adminAuthService.login(dto);
        String jwtToken = (String) data.get("token");

        // 1. 设置 JWT HttpOnly Cookie（前端 JS 无法读取，防 XSS 窃取）
        String jwtCookie = String.format(
                "admin_token=%s; Path=/; HttpOnly; SameSite=Strict; Max-Age=86400",
                jwtToken);
        response.addHeader("Set-Cookie", jwtCookie);

        // 2. 设置认证状态标记 Cookie（非 HttpOnly，前端 JS 可读取用于 auth guard）
        String statusCookie = "admin_auth_status=1; Path=/; SameSite=Strict; Max-Age=86400";
        response.addHeader("Set-Cookie", statusCookie);

        // 3. 设置 CSRF Cookie（Double Submit Cookie 模式）
        String csrfToken = CsrfFilter.generateToken();
        String csrfCookie = String.format("%s=%s; Path=/; SameSite=Strict; Max-Age=86400",
                CsrfFilter.CSRF_COOKIE_NAME, csrfToken);
        response.addHeader("Set-Cookie", csrfCookie);

        // 将 CSRF Token 返回到响应体（前端可备用）
        data.put("csrfToken", csrfToken);

        return Result.success("登录成功", data);
    }

    /**
     * 管理员登出
     * POST /admin/auth/logout
     * 清除所有认证 Cookie
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletResponse response) {
        // 清除 JWT Cookie
        String clearJwt = "admin_token=; Path=/; HttpOnly; SameSite=Strict; Max-Age=0";
        response.addHeader("Set-Cookie", clearJwt);

        // 清除认证状态 Cookie
        String clearStatus = "admin_auth_status=; Path=/; SameSite=Strict; Max-Age=0";
        response.addHeader("Set-Cookie", clearStatus);

        // 清除 CSRF Cookie
        String clearCsrf = String.format("%s=; Path=/; SameSite=Strict; Max-Age=0",
                CsrfFilter.CSRF_COOKIE_NAME);
        response.addHeader("Set-Cookie", clearCsrf);

        return Result.success("已退出登录", null);
    }
}
