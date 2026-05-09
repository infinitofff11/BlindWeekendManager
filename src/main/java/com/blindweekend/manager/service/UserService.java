package com.blindweekend.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blindweekend.manager.common.BusinessException;
import com.blindweekend.manager.common.PageResult;
import com.blindweekend.manager.entity.User;
import com.blindweekend.manager.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    /**
     * 分页查询用户列表
     */
    public PageResult<User> queryPage(Integer pageNum, Integer pageSize,
                                       String keyword, Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }

        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> page = new Page<>(pageNum != null ? pageNum : 1,
                                      pageSize != null ? pageSize : 10);
        Page<User> result = userMapper.selectPage(page, wrapper);

        return new PageResult<>(result.getCurrent(), result.getSize(),
                                result.getTotal(), result.getRecords());
    }

    /**
     * 根据ID获取用户详情
     */
    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 隐藏敏感信息
        user.setPassword(null);
        return user;
    }

    /**
     * 禁用/启用用户
     */
    public void toggleStatus(Long id, Integer status) {
        User user = getById(id);
        if (status != 0 && status != 1) {
            throw new IllegalArgumentException("状态值只能为0或1");
        }
        userMapper.updateStatus(id, status);
        log.info("更新用户状态: id={}, status={}", id, status);
    }

    /**
     * 获取用户统计
     */
    public java.util.Map<String, Object> getStats() {
        return java.util.Map.of(
                "total", userMapper.countTotal(),
                "todayNew", userMapper.countTodayNew()
        );
    }
}
