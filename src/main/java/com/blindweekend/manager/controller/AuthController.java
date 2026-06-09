package com.blindweekend.manager.controller;

import com.blindweekend.manager.common.Result;
import com.blindweekend.manager.dto.LoginDTO;
import com.blindweekend.manager.dto.LoginResponse;
import com.blindweekend.manager.dto.RegisterDTO;
import com.blindweekend.manager.entity.User;
import com.blindweekend.manager.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口 - 注册/登录
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody @Valid RegisterDTO dto) {
        log.info("收到注册请求: phone={}, nickname={}", dto.getPhone(), dto.getNickname());
        User user = authService.register(dto);
        return Result.success("注册成功", user);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginDTO dto) {
        log.info("收到登录请求: phone={}", dto.getPhone());
        LoginResponse loginResponse = authService.login(dto);
        return Result.success("登录成功", loginResponse);
    }
}
