package com.blindweekend.manager.controller;

import com.blindweekend.manager.common.PageResult;
import com.blindweekend.manager.common.Result;
import com.blindweekend.manager.dto.BlindBoxCreateDTO;
import com.blindweekend.manager.dto.BlindBoxUpdateDTO;
import com.blindweekend.manager.entity.BlindBox;
import com.blindweekend.manager.service.BlindBoxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 盲盒管理接口
 */
@Slf4j
@RestController
@RequestMapping("/admin/blindboxes")
@RequiredArgsConstructor
public class BlindBoxController {

    private final BlindBoxService blindBoxService;

    /**
     * 分页查询盲盒列表
     */
    @GetMapping
    public Result<PageResult<BlindBox>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String timePeriod,
            @RequestParam(required = false) String tags) {
        return Result.success(blindBoxService.queryPage(pageNum, pageSize, keyword, status, city, district, timePeriod, tags));
    }

    /**
     * 用户发布盲盒（从生成的方案发布）
     */
    @PostMapping
    public Result<BlindBox> create(@RequestBody @Valid BlindBoxCreateDTO dto) {
        log.info("用户发布盲盒: publisherId={}, title={}", dto.getPublisherId(), dto.getTitle());
        BlindBox box = blindBoxService.create(dto);
        return Result.success("盲盒发布成功", box);
    }

    /**
     * 获取盲盒详情
     */
    @GetMapping("/{id}")
    public Result<BlindBox> detail(@PathVariable Long id) {
        return Result.success(blindBoxService.getById(id));
    }

    /**
     * 管理员编辑盲盒
     */
    @PutMapping("/{id}")
    public Result<BlindBox> update(@PathVariable Long id,
                                    @RequestBody @Valid BlindBoxUpdateDTO dto) {
        log.info("管理员编辑盲盒: id={}, title={}", id, dto.getTitle());
        BlindBox box = blindBoxService.update(id, dto);
        return Result.success("盲盒已更新", box);
    }

    /**
     * 删除盲盒（管理员操作）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        blindBoxService.delete(id);
        return Result.success("删除成功", null);
    }

    /**
     * 意向响应（参与盲盒/想组队）
     */
    @PostMapping("/{id}/join")
    public Result<Void> join(@PathVariable Long id,
                              @RequestParam Long userId) {
        log.info("用户参与盲盒: userId={}, blindBoxId={}", userId, id);
        blindBoxService.join(id, userId);
        return Result.success("已向发起者表达组队意向！", null);
    }

    /**
     * 更新盲盒状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                      @RequestParam String status) {
        blindBoxService.updateStatus(id, status);
        return Result.success("状态已更新", null);
    }

    /**
     * 盲盒统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(blindBoxService.getStats());
    }

    // ==================== 用户参与/发布记录 API ====================

    /**
     * 获取用户参与的盲盒列表
     */
    @GetMapping("/user/{userId}/participated")
    public Result<List<BlindBox>> getParticipated(@PathVariable Long userId) {
        List<BlindBox> boxes = blindBoxService.getParticipatedBlindBoxes(userId);
        return Result.success(boxes);
    }

    /**
     * 获取用户发布的盲盒列表
     */
    @GetMapping("/user/{userId}/published")
    public Result<List<BlindBox>> getPublished(@PathVariable Long userId) {
        List<BlindBox> boxes = blindBoxService.getPublishedBlindBoxes(userId);
        return Result.success(boxes);
    }

    /**
     * 获取用户盲盒统计（参与数 + 发布数）
     */
    @GetMapping("/user/{userId}/stats")
    public Result<Map<String, Object>> getUserStats(@PathVariable Long userId) {
        Map<String, Object> stats = blindBoxService.getUserBlindBoxStats(userId);
        return Result.success(stats);
    }
}
