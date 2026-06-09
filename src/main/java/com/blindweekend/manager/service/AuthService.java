package com.blindweekend.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blindweekend.manager.common.BusinessException;
import com.blindweekend.manager.dto.LoginDTO;
import com.blindweekend.manager.dto.LoginResponse;
import com.blindweekend.manager.dto.RegisterDTO;
import com.blindweekend.manager.entity.User;
import com.blindweekend.manager.mapper.UserMapper;
import com.blindweekend.manager.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证服务 - 注册/登录
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    public User register(RegisterDTO dto) {
        // 1. 检查手机号是否已注册
        LambdaQueryWrapper<User> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(User::getPhone, dto.getPhone());
        Long count = userMapper.selectCount(checkWrapper);
        if (count > 0) {
            throw new BusinessException(400, "该手机号已注册");
        }

        // 2. 创建用户（密码 BCrypt 加密）
        User user = new User();
        user.setPhone(dto.getPhone());
        user.setPassword(cn.hutool.crypto.digest.BCrypt.hashpw(dto.getPassword(), cn.hutool.crypto.digest.BCrypt.gensalt()));
        user.setNickname(dto.getNickname());
        user.setStatus(1); // 默认正常状态
        user.setLastLoginTime(LocalDateTime.now());

        userMapper.insert(user);
        log.info("用户注册成功: phone={}, nickname={}, id={}", dto.getPhone(), dto.getNickname(), user.getId());

        // 3. 返回用户信息（脱敏）
        user.setPassword(null);
        return user;
    }

    /**
     * 用户登录
     */
    public LoginResponse login(LoginDTO dto) {
        // 1. 根据手机号查找用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(400, "手机号或密码错误");
        }

        // 2. 验证密码
        if (!cn.hutool.crypto.digest.BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "手机号或密码错误");
        }

        // 3. 检查账号状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(400, "账号已被禁用，请联系管理员");
        }

        // 4. 更新最后登录时间
        User update = new User();
        update.setId(user.getId());
        update.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(update);

        log.info("用户登录成功: phone={}, id={}", dto.getPhone(), user.getId());

        // 5. 返回用户信息（脱敏）
        user.setPassword(null);
        String token = jwtUtil.generateToken(user.getId(), user.getPhone());

        return new LoginResponse(token, user);
    }
}
