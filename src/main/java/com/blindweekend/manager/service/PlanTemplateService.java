package com.blindweekend.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blindweekend.manager.common.BusinessException;
import com.blindweekend.manager.common.PageResult;
import com.blindweekend.manager.dto.PlanTemplateDTO;
import com.blindweekend.manager.dto.SegmentDTO;
import com.blindweekend.manager.entity.PlanTemplate;
import com.blindweekend.manager.entity.TemplateSegment;
import com.blindweekend.manager.mapper.PlanTemplateMapper;
import com.blindweekend.manager.mapper.TemplateSegmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 方案模板管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanTemplateService {

    private final PlanTemplateMapper planTemplateMapper;
    private final TemplateSegmentMapper templateSegmentMapper;

    /**
     * 分页查询模板列表
     */
    public PageResult<PlanTemplate> queryPage(Integer pageNum, Integer pageSize, String keyword) {
        LambdaQueryWrapper<PlanTemplate> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(PlanTemplate::getName, keyword)
                    .or().like(PlanTemplate::getThemeType, keyword);
        }

        wrapper.orderByDesc(PlanTemplate::getSortOrder)
               .orderByDesc(PlanTemplate::getCreatedAt);

        Page<PlanTemplate> page = new Page<>(pageNum != null ? pageNum : 1,
                                             pageSize != null ? pageSize : 10);
        Page<PlanTemplate> result = planTemplateMapper.selectPage(page, wrapper);

        // 填充每个模板的时段数量
        for (PlanTemplate tpl : result.getRecords()) {
            Long count = templateSegmentMapper.countByTemplateId(tpl.getId());
            tpl.set_segmentCount(count != null ? count.intValue() : 0);
        }

        return new PageResult<>(result.getCurrent(), result.getSize(),
                                result.getTotal(), result.getRecords());
    }

    /**
     * 获取模板详情（含时段配置）
     */
    public PlanTemplate getDetailWithSegments(Long id) {
        PlanTemplate template = planTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }
        // 查询时段配置
        List<TemplateSegment> segments = templateSegmentMapper.selectByTemplateId(id);
        // 可以将segments存到扩展字段或单独返回
        log.debug("模板详情: id={}, 时段数={}", id, segments.size());
        return template;
    }

    /**
     * 获取模板的时段配置
     */
    public List<TemplateSegment> getSegmentsByTemplateId(Long templateId) {
        return templateSegmentMapper.selectByTemplateId(templateId);
    }

    /**
     * 创建模板（含时段）
     */
    @Transactional(rollbackFor = Exception.class)
    public PlanTemplate create(PlanTemplateDTO dto) {
        // 创建主表
        PlanTemplate template = new PlanTemplate();
        template.setName(dto.getName());
        template.setDescription(dto.getDescription());
        template.setTotalDuration(dto.getTotalDuration());
        template.setConsumeLevel(dto.getConsumeLevel());
        template.setThemeType(dto.getThemeType());
        template.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        template.setStatus(1);
        template.setCreatedAt(LocalDateTime.now());

        planTemplateMapper.insert(template);
        log.info("创建方案模板: id={}, name={}", template.getId(), template.getName());

        // 创建时段配置
        saveSegments(template.getId(), dto.getSegments());

        return template;
    }

    /**
     * 更新模板（含时段）
     */
    @Transactional(rollbackFor = Exception.class)
    public PlanTemplate update(Long id, PlanTemplateDTO dto) {
        PlanTemplate existing = planTemplateMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("模板不存在");
        }

        // 更新主表
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setTotalDuration(dto.getTotalDuration());
        existing.setConsumeLevel(dto.getConsumeLevel());
        existing.setThemeType(dto.getThemeType());
        existing.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : existing.getSortOrder());
        existing.setUpdatedAt(LocalDateTime.now());

        planTemplateMapper.updateById(existing);

        // 删除旧时段，重新创建
        templateSegmentMapper.deleteByTemplateId(id);
        saveSegments(id, dto.getSegments());

        log.info("更新方案模板: id={}", id);
        return existing;
    }

    /**
     * 删除模板
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        PlanTemplate template = planTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }
        // 先删除关联的时段
        templateSegmentMapper.deleteByTemplateId(id);
        planTemplateMapper.deleteById(id);
        log.info("删除方案模板: id={}", id);
    }

    /**
     * 启用/禁用模板
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleStatus(Long id, Integer status) {
        PlanTemplate template = planTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }
        template.setStatus(status);
        template.setUpdatedAt(LocalDateTime.now());
        planTemplateMapper.updateById(template);
    }

    /**
     * 获取所有启用的模板（供方案生成使用）
     */
    public List<PlanTemplate> listActive(String consumeLevel, String themeType) {
        LambdaQueryWrapper<PlanTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlanTemplate::getStatus, 1);

        if (StringUtils.hasText(consumeLevel)) {
            wrapper.eq(PlanTemplate::getConsumeLevel, consumeLevel);
        }
        if (StringUtils.hasText(themeType)) {
            wrapper.eq(PlanTemplate::getThemeType, themeType);
        }

        wrapper.orderByAsc(PlanTemplate::getSortOrder);
        return planTemplateMapper.selectList(wrapper);
    }

    /**
     * 保存时段配置
     */
    private void saveSegments(Long templateId, List<SegmentDTO> segmentDTOs) {
        if (segmentDTOs == null || segmentDTOs.isEmpty()) {
            return;
        }

        List<TemplateSegment> segments = new ArrayList<>();
        for (SegmentDTO dto : segmentDTOs) {
            TemplateSegment seg = new TemplateSegment();
            seg.setTemplateId(templateId);
            seg.setSegmentOrder(dto.getSegmentOrder());
            seg.setStartTime(dto.getStartTime());
            seg.setEndTime(dto.getEndTime());
            seg.setActivityTypes(dto.getActivityTypes());
            seg.setSegmentName(dto.getSegmentName());
            seg.setCreatedAt(LocalDateTime.now());
            segments.add(seg);
        }

        for (TemplateSegment seg : segments) {
            templateSegmentMapper.insert(seg);
        }
        log.info("保存时段配置: templateId={}, count={}", templateId, segments.size());
    }
}
