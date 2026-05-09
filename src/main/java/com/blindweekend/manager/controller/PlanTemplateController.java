package com.blindweekend.manager.controller;

import com.blindweekend.manager.common.PageResult;
import com.blindweekend.manager.common.Result;
import com.blindweekend.manager.dto.PlanTemplateDTO;
import com.blindweekend.manager.entity.PlanTemplate;
import com.blindweekend.manager.entity.TemplateSegment;
import com.blindweekend.manager.service.PlanTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 方案模板管理接口
 */
@Slf4j
@RestController
@RequestMapping("/admin/templates")
@RequiredArgsConstructor
public class PlanTemplateController {

    private final PlanTemplateService planTemplateService;

    /**
     * 分页查询模板列表
     */
    @GetMapping
    public Result<PageResult<PlanTemplate>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(planTemplateService.queryPage(pageNum, pageSize, keyword));
    }

    /**
     * 获取模板详情（含时段配置）
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        PlanTemplate template = planTemplateService.getDetailWithSegments(id);
        List<TemplateSegment> segments = planTemplateService.getSegmentsByTemplateId(id);
        return Result.success(Map.of(
                "template", template,
                "segments", segments
        ));
    }

    /**
     * 创建模板
     */
    @PostMapping
    public Result<PlanTemplate> create(@Valid @RequestBody PlanTemplateDTO dto) {
        return Result.success("创建成功", planTemplateService.create(dto));
    }

    /**
     * 更新模板
     */
    @PutMapping("/{id}")
    public Result<PlanTemplate> update(@PathVariable Long id,
                                       @Valid @RequestBody PlanTemplateDTO dto) {
        return Result.success("更新成功", planTemplateService.update(id, dto));
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        planTemplateService.delete(id);
        return Result.success("删除成功", null);
    }

    /**
     * 启用/禁用模板
     */
    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id,
                                      @RequestParam Integer status) {
        if (status != 0 && status != 1) {
            throw new IllegalArgumentException("状态值只能为0或1");
        }
        planTemplateService.toggleStatus(id, status);
        return Result.success(status == 1 ? "已启用" : "已禁用", null);
    }

    /**
     * 获取所有启用的模板（供方案生成器调用）
     */
    @GetMapping("/active")
    public Result<List<PlanTemplate>> listActive(
            @RequestParam(required = false) String consumeLevel,
            @RequestParam(required = false) String themeType) {
        return Result.success(planTemplateService.listActive(consumeLevel, themeType));
    }

    /**
     * 获取模板的时段配置
     */
    @GetMapping("/{id}/segments")
    public Result<List<TemplateSegment>> segments(@PathVariable Long id) {
        return Result.success(planTemplateService.getSegmentsByTemplateId(id));
    }
}
