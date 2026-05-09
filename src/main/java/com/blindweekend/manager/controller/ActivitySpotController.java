package com.blindweekend.manager.controller;

import com.blindweekend.manager.common.PageResult;
import com.blindweekend.manager.common.Result;
import com.blindweekend.manager.dto.ActivitySpotDTO;
import com.blindweekend.manager.dto.ActivitySpotQuery;
import com.blindweekend.manager.entity.ActivitySpot;
import com.blindweekend.manager.service.ActivitySpotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 活动点管理接口
 */
@Slf4j
@RestController
@RequestMapping("/admin/spots")
@RequiredArgsConstructor
public class ActivitySpotController {

    private final ActivitySpotService activitySpotService;

    /**
     * 分页查询活动点列表
     */
    @GetMapping
    public Result<PageResult<ActivitySpot>> list(ActivitySpotQuery query) {
        return Result.success(activitySpotService.queryPage(query));
    }

    /**
     * 获取活动点详情
     */
    @GetMapping("/{id}")
    public Result<ActivitySpot> detail(@PathVariable Long id) {
        return Result.success(activitySpotService.getById(id));
    }

    /**
     * 创建活动点
     */
    @PostMapping
    public Result<ActivitySpot> create(@Valid @RequestBody ActivitySpotDTO dto) {
        return Result.success("创建成功", activitySpotService.create(dto));
    }

    /**
     * 更新活动点
     */
    @PutMapping("/{id}")
    public Result<ActivitySpot> update(@PathVariable Long id, @Valid @RequestBody ActivitySpotDTO dto) {
        return Result.success("更新成功", activitySpotService.update(id, dto));
    }

    /**
     * 删除活动点
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        activitySpotService.delete(id);
        return Result.success("删除成功", null);
    }

    /**
     * 启用/禁用活动点
     */
    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        if (status != 0 && status != 1) {
            throw new IllegalArgumentException("状态值只能为0或1");
        }
        activitySpotService.toggleStatus(id, status);
        String msg = status == 1 ? "已启用" : "已禁用";
        return Result.success(msg, null);
    }

    /**
     * 批量导入（Excel文件上传）
     */
    @PostMapping("/import")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) {
        // TODO: 实现Excel解析导入逻辑
        // 使用EasyExcel解析文件，调用service.batchImport()
        return Result.success("导入功能开发中", Map.of("message", "请使用API接口批量创建"));
    }

    /**
     * 获取所有启用的活动点（供方案生成器调用）
     */
    @GetMapping("/active")
    public Result<List<ActivitySpot>> listActive(@RequestParam(required = false) String city) {
        return Result.success(activitySpotService.listAllActive(city));
    }

    /**
     * 统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> stats = Map.of(
                "total", activitySpotService.countByStatus(null),
                "active", activitySpotService.countByStatus(1),
                "disabled", activitySpotService.countByStatus(0)
        );
        return Result.success(stats);
    }
}
