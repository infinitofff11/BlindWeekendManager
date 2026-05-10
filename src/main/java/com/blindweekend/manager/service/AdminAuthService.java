package com.blindweekend.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blindweekend.manager.common.BusinessException;
import com.blindweekend.manager.dto.AdminLoginDTO;
import com.blindweekend.manager.entity.AdminUser;
import com.blindweekend.manager.mapper.AdminMapper;
import com.blindweekend.manager.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminMapper adminMapper;
    private final JwtUtil jwtUtil;

    /**
     * 管理员登录
     *
     * @return Map {token, admin}
     */
    public Map<String, Object> login(AdminLoginDTO dto) {
        // 1. 根据用户名查找管理员
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminUser::getUsername, dto.getUsername());
        AdminUser admin = adminMapper.selectOne(wrapper);

        if (admin == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 2. 验证密码 (使用 Hutool BCrypt)
        if (!cn.hutool.crypto.digest.BCrypt.checkpw(dto.getPassword(), admin.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 3. 检查账号状态
        if (admin.getStatus() == null || admin.getStatus() != 1) {
            throw new BusinessException(401, "账号已被禁用");
        }

        // 4. 更新最后登录时间
        AdminUser update = new AdminUser();
        update.setId(admin.getId());
        update.setLastLoginTime(LocalDateTime.now());
        adminMapper.updateById(update);

        // 5. 生成 JWT Token
        String token = jwtUtil.generateToken(admin.getId(), admin.getUsername());

        log.info("管理员登录成功: username={}, id={}", dto.getUsername(), admin.getId());

        // 6. 构造返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("admin", maskSensitiveInfo(admin));

        return result;
    }

    /**
     * 根据 ID 查询管理员信息（脱敏）
     */
    public AdminUser getAdminById(Long id) {
        AdminUser admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(401, "管理员不存在");
        }
        return maskSensitiveInfo(admin);
    }

    /**
     * 脱敏处理（移除密码字段）
     */
    private AdminUser maskSensitiveInfo(AdminUser admin) {
        admin.setPassword(null);
        return admin;
    }
}
