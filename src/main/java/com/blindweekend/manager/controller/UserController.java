package com.blindweekend.manager.controller;

import com.blindweekend.manager.common.PageResult;
import com.blindweekend.manager.common.Result;
import com.blindweekend.manager.entity.User;
import com.blindweekend.manager.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理接口
 */
@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户列表
     */
    @GetMapping
    public Result<PageResult<User>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(userService.queryPage(pageNum, pageSize, keyword, status));
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /**
     * 禁用/启用用户
     */
    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id,
                                      @RequestParam Integer status) {
        if (status != 0 && status != 1) {
            throw new IllegalArgumentException("状态值只能为0或1");
        }
        userService.toggleStatus(id, status);
        String msg = status == 1 ? "已启用" : "已禁用";
        return Result.success(msg, null);
    }

    /**
     * 用户统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(userService.getStats());
    }
}
