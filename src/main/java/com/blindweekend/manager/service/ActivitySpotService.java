package com.blindweekend.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blindweekend.manager.common.BusinessException;
import com.blindweekend.manager.common.PageResult;
import com.blindweekend.manager.dto.ActivitySpotDTO;
import com.blindweekend.manager.dto.ActivitySpotQuery;
import com.blindweekend.manager.entity.ActivitySpot;
import com.blindweekend.manager.mapper.ActivitySpotMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动点管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivitySpotService {

    private final ActivitySpotMapper activitySpotMapper;

    /**
     * 分页查询活动点
     */
    public PageResult<ActivitySpot> queryPage(ActivitySpotQuery query) {
        // 构建查询条件
        LambdaQueryWrapper<ActivitySpot> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(ActivitySpot::getName, query.getKeyword())
                    .or().like(ActivitySpot::getAddress, query.getKeyword()));
        }
        if (StringUtils.hasText(query.getTypeTag())) {
            wrapper.like(ActivitySpot::getTypeTags, query.getTypeTag());
        }
        if (StringUtils.hasText(query.getConsumeLevel())) {
            wrapper.eq(ActivitySpot::getConsumeLevel, query.getConsumeLevel());
        }
        if (StringUtils.hasText(query.getCity())) {
            wrapper.eq(ActivitySpot::getCity, query.getCity());
        }
        if (query.getStatus() != null) {
            wrapper.eq(ActivitySpot::getStatus, query.getStatus());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(ActivitySpot::getCreatedAt);

        // 分页查询
        Page<ActivitySpot> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<ActivitySpot> result = activitySpotMapper.selectPage(page, wrapper);

        return new PageResult<>(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getRecords()
        );
    }

    /**
     * 查询所有启用的活动点（供方案生成使用）
     */
    public List<ActivitySpot> listAllActive(String city) {
        LambdaQueryWrapper<ActivitySpot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivitySpot::getStatus, 1);
        if (StringUtils.hasText(city)) {
            wrapper.eq(ActivitySpot::getCity, city);
        }
        wrapper.orderByAsc(ActivitySpot::getConsumePerPerson);
        return activitySpotMapper.selectList(wrapper);
    }

    /**
     * 根据ID获取活动点详情
     */
    public ActivitySpot getById(Long id) {
        ActivitySpot spot = activitySpotMapper.selectById(id);
        if (spot == null) {
            throw new BusinessException("活动点不存在");
        }
        return spot;
    }

    /**
     * 创建活动点
     */
    @Transactional(rollbackFor = Exception.class)
    public ActivitySpot create(ActivitySpotDTO dto) {
        ActivitySpot spot = new ActivitySpot();
        BeanUtils.copyProperties(dto, spot);

        // 设置默认值
        if (spot.getRecommendDuration() == null) {
            spot.setRecommendDuration(60);
        }
        if (!StringUtils.hasText(spot.getSuggestTimePeriod())) {
            spot.setSuggestTimePeriod("all_day");
        }
        if (!StringUtils.hasText(spot.getSuitableCapacity())) {
            spot.setSuitableCapacity("both");
        }
        if (!StringUtils.hasText(spot.getConsumeLevel())) {
            // 根据消费金额自动判断
            if (spot.getConsumePerPerson() != null && spot.getConsumePerPerson().compareTo(new BigDecimal("100")) > 0) {
                spot.setConsumeLevel("high");
            } else {
                spot.setConsumeLevel("low");
            }
        }
        spot.setStatus(1);
        spot.setCreatedAt(LocalDateTime.now());

        activitySpotMapper.insert(spot);
        log.info("创建活动点成功: id={}, name={}", spot.getId(), spot.getName());
        return spot;
    }

    /**
     * 更新活动点
     */
    @Transactional(rollbackFor = Exception.class)
    public ActivitySpot update(Long id, ActivitySpotDTO dto) {
        ActivitySpot existing = getById(id);

        BeanUtils.copyProperties(dto, existing);
        existing.setId(id);  // 确保ID不被覆盖
        existing.setUpdatedAt(LocalDateTime.now());

        activitySpotMapper.updateById(existing);
        log.info("更新活动点成功: id={}", id);
        return existing;
    }

    /**
     * 删除活动点（逻辑删除/物理删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ActivitySpot spot = getById(id);
        activitySpotMapper.deleteById(id);
        log.info("删除活动点成功: id={}, name={}", id, spot.getName());
    }

    /**
     * 启用/禁用活动点
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleStatus(Long id, Integer status) {
        ActivitySpot spot = getById(id);
        spot.setStatus(status);
        spot.setUpdatedAt(LocalDateTime.now());
        activitySpotMapper.updateById(spot);
        log.info("更新活动点状态: id={}, status={}", id, status);
    }

    /**
     * 批量导入活动点（从Excel数据）
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchImport(List<ActivitySpotDTO> spots) {
        int count = 0;
        for (ActivitySpotDTO dto : spots) {
            try {
                ActivitySpot spot = new ActivitySpot();
                BeanUtils.copyProperties(dto, spot);
                if (spot.getRecommendDuration() == null) {
                    spot.setRecommendDuration(60);
                }
                if (!StringUtils.hasText(spot.getConsumeLevel())) {
                    spot.setConsumeLevel("low");
                }
                if (!StringUtils.hasText(spot.getSuggestTimePeriod())) {
                    spot.setSuggestTimePeriod("all_day");
                }
                spot.setStatus(1);
                spot.setCreatedAt(LocalDateTime.now());
                activitySpotMapper.insert(spot);
                count++;
            } catch (Exception e) {
                log.warn("导入活动点失败: name={}, error={}", dto.getName(), e.getMessage());
            }
        }
        log.info("批量导入活动点完成: 成功{}条", count);
        return count;
    }

    /**
     * 获取活动点统计信息
     */
    public long countByStatus(Integer status) {
        return activitySpotMapper.selectCount(
                new LambdaQueryWrapper<ActivitySpot>().eq(ActivitySpot::getStatus, status)
        );
    }
}
