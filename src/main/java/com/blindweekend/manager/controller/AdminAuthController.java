package com.blindweekend.manager.controller;

import com.blindweekend.manager.common.Result;
import com.blindweekend.manager.dto.AdminLoginDTO;
import com.blindweekend.manager.service.AdminAuthService;
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
     * 响应: {code:200, message:"登录成功", data:{token:"jwt...", admin:{id:1, username:"admin", ...}}}
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid AdminLoginDTO dto) {
        log.info("收到管理员登录请求: username={}", dto.getUsername());
        Map<String, Object> data = adminAuthService.login(dto);
        return Result.success("登录成功", data);
    }
}
