package com.blindweekend.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blindweekend.manager.common.BusinessException;
import com.blindweekend.manager.common.PageResult;
import com.blindweekend.manager.dto.BlindBoxCreateDTO;
import com.blindweekend.manager.dto.BlindBoxDetailDTO;
import com.blindweekend.manager.dto.BlindBoxUpdateDTO;
import com.blindweekend.manager.dto.PlanItemVO;
import com.blindweekend.manager.entity.BlindBox;
import com.blindweekend.manager.entity.BlindBoxApplication;
import com.blindweekend.manager.mapper.BlindBoxApplicationMapper;
import com.blindweekend.manager.mapper.BlindBoxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 盲盒管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlindBoxService {

    private final BlindBoxMapper blindBoxMapper;
    private final BlindBoxApplicationMapper applicationMapper;

    /**
     * 用户发布盲盒
     */
    public BlindBox create(BlindBoxCreateDTO dto) {
        BlindBox box = new BlindBox();
        box.setPublisherId(dto.getPublisherId());
        box.setPlanId(dto.getPlanId());
        box.setTitle(dto.getTitle());
        box.setSummaryText(dto.getSummaryText());
        box.setMoodText(dto.getMoodText());
        box.setCity(dto.getCity());
        box.setDistrict(dto.getDistrict());
        if (StringUtils.hasText(dto.getActivityDate())) {
            box.setActivityDate(parseFlexibleDate(dto.getActivityDate()));
        }
        box.setActivityTimePeriod(dto.getActivityTimePeriod());
        box.setRequiredCount(dto.getRequiredCount());
        box.setCurrentCount(1); // 发布者自己算1人
        box.setActivityTypeTags(dto.getActivityTypeTags());
        box.setStatus("open");
        box.setViewCount(0);

        blindBoxMapper.insert(box);
        log.info("盲盒发布成功: id={}, publisherId={}, title={}", box.getId(), dto.getPublisherId(), dto.getTitle());

        return box;
    }

    /**
     * 用户参与盲盒（意向响应）
     *
     * 修复：原实现只增加了 current_count 计数器，
     *       现在同时向 blind_box_applications 表写入参与记录
     */
    public void join(Long blindBoxId, Long userId) {
        BlindBox box = getById(blindBoxId);

        // 不能参与自己发布的盲盒
        if (box.getPublisherId().equals(userId)) {
            throw new BusinessException(400, "不能参与自己发布的盲盒");
        }

        // 检查状态
        if (!"open".equals(box.getStatus())) {
            throw new BusinessException(400, "该盲盒当前不可参与");
        }

        // 检查是否已满
        if (box.getCurrentCount() != null && box.getCurrentCount() >= box.getRequiredCount()) {
            throw new BusinessException(400, "该盲盒人数已满");
        }

        // 检查是否已经参与过（防重复）
        LambdaQueryWrapper<BlindBoxApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlindBoxApplication::getBlindBoxId, blindBoxId)
               .eq(BlindBoxApplication::getApplicantId, userId);
        Long existingCount = applicationMapper.selectCount(wrapper);
        if (existingCount != null && existingCount > 0) {
            throw new BusinessException(400, "您已经表达过组队意向，无需重复申请");
        }

        // ✅ 插入参与记录到 blind_box_applications 表
        BlindBoxApplication application = new BlindBoxApplication();
        application.setBlindBoxId(blindBoxId);
        application.setApplicantId(userId);
        application.setStatus("pending"); // 待发布者确认
        applicationMapper.insert(application);
        log.info("已插入参与记录: blindBoxId={}, applicantId={}", blindBoxId, userId);

        // ✅ 原子操作：current_count +1 + 满员自动变full（并发安全）
        int rows = blindBoxMapper.incrementParticipantCount(blindBoxId);
        if (rows == 0) {
            // 说明 WHERE 条件不满足（状态已不是 open 或记录被删），回滚申请记录
            throw new BusinessException(400, "该盲盒当前不可参与，请刷新重试");
        }
        int newCount = (box.getCurrentCount() == null ? 1 : box.getCurrentCount()) + 1;

        log.info("用户参与盲盒: blindBoxId={}, userId={}, currentCount={}", blindBoxId, userId, newCount);
    }

    /**
     * 分页查询盲盒列表（支持筛选）
     */
    public PageResult<BlindBox> queryPage(Integer pageNum, Integer pageSize,
                                           String keyword, String status,
                                           String city, String district,
                                           String timePeriod, String tags) {
        LambdaQueryWrapper<BlindBox> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(BlindBox::getTitle, keyword);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(BlindBox::getStatus, status);
        }
        if (StringUtils.hasText(city)) {
            wrapper.eq(BlindBox::getCity, city);
        }
        if (StringUtils.hasText(district)) {
            wrapper.eq(BlindBox::getDistrict, district);
        }
        if (StringUtils.hasText(timePeriod)) {
            wrapper.eq(BlindBox::getActivityTimePeriod, timePeriod);
        }
        if (StringUtils.hasText(tags)) {
            // tags为逗号分隔的标签列表，使用LIKE匹配JSON数组中的每个标签
            for (String tag : tags.split(",")) {
                String trimmed = tag.trim();
                if (!trimmed.isEmpty()) {
                    wrapper.like(BlindBox::getActivityTypeTags, trimmed);
                }
            }
        }

        wrapper.orderByDesc(BlindBox::getCreatedAt);

        Page<BlindBox> page = new Page<>(pageNum != null ? pageNum : 1,
                                         pageSize != null ? pageSize : 10);
        Page<BlindBox> result = blindBoxMapper.selectPage(page, wrapper);

        return new PageResult<>(result.getCurrent(), result.getSize(),
                                result.getTotal(), result.getRecords());
    }

    /**
     * 根据ID获取盲盒详情（同时增加浏览量）
     */
    public BlindBox getById(Long id) {
        BlindBox box = blindBoxMapper.selectById(id);
        if (box == null) {
            throw new BusinessException("盲盒不存在");
        }
        // 浏览量 +1
        blindBoxMapper.incrementViewCount(id);
        box.setViewCount((box.getViewCount() == null ? 0 : box.getViewCount()) + 1);
        return box;
    }

    /**
     * 获取盲盒详情（含方案环节/活动地点信息）
     * 权限控制：只有发布者或已参与（申请被接受）的用户才能看到具体活动地点
     *
     * @param blindBoxId 盲盒ID
     * @param currentUserId 当前请求用户ID，可为null（未登录）
     */
    public BlindBoxDetailDTO getDetail(Long blindBoxId, Long currentUserId) {
        BlindBox box = blindBoxMapper.selectById(blindBoxId);
        if (box == null) {
            throw new BusinessException("盲盒不存在");
        }
        // 浏览量 +1
        blindBoxMapper.incrementViewCount(blindBoxId);
        box.setViewCount((box.getViewCount() == null ? 0 : box.getViewCount()) + 1);

        BlindBoxDetailDTO dto = new BlindBoxDetailDTO(box);

        // 判断当前用户身份
        boolean isPublisher = currentUserId != null && currentUserId.equals(box.getPublisherId());
        boolean isAccepted = false;

        if (currentUserId != null && !isPublisher) {
            // 检查用户是否已参与（申请被接受）
            LambdaQueryWrapper<BlindBoxApplication> appWrapper = new LambdaQueryWrapper<>();
            appWrapper.eq(BlindBoxApplication::getBlindBoxId, blindBoxId)
                       .eq(BlindBoxApplication::getApplicantId, currentUserId)
                       .eq(BlindBoxApplication::getStatus, "accepted");
            Long acceptedCount = applicationMapper.selectCount(appWrapper);
            isAccepted = acceptedCount != null && acceptedCount > 0;
        }

        dto.setPublisher(isPublisher);
        dto.setParticipated(isAccepted);

        // 只有发布者或已参与用户才能看到具体活动地点
        if ((isPublisher || isAccepted) && box.getPlanId() != null) {
            List<PlanItemVO> planItems = blindBoxMapper.findPlanItemsByPlanId(box.getPlanId());
            dto.setPlanItems(planItems != null ? planItems : List.of());
            log.info("用户{}查看盲盒{}的活动地点，共{}个环节", currentUserId, blindBoxId, planItems != null ? planItems.size() : 0);
        }

        return dto;
    }

    /**
     * 删除盲盒（管理员操作）
     */
    public void delete(Long id) {
        BlindBox box = getById(id);
        blindBoxMapper.deleteById(id);
        log.info("删除盲盒: id={}, title={}", id, box.getTitle());
    }

    /**
     * 更新盲盒状态（关闭/取消等）
     */
    public void updateStatus(Long id, String status) {
        BlindBox box = getById(id);
        blindBoxMapper.updateStatus(id, status);
        log.info("更新盲盒状态: id={}, status={}", id, status);
    }

    /**
     * 管理员编辑盲盒信息
     */
    public BlindBox update(Long id, BlindBoxUpdateDTO dto) {
        BlindBox box = getById(id);

        // 只更新非空字段（部分更新语义）
        if (StringUtils.hasText(dto.getTitle())) {
            box.setTitle(dto.getTitle());
        }
        if (dto.getSummaryText() != null) {
            box.setSummaryText(dto.getSummaryText());
        }
        if (dto.getMoodText() != null) {
            box.setMoodText(dto.getMoodText());
        }
        if (dto.getCity() != null) {
            box.setCity(dto.getCity());
        }
        if (dto.getDistrict() != null) {
            box.setDistrict(dto.getDistrict());
        }
        if (StringUtils.hasText(dto.getActivityDate())) {
            box.setActivityDate(parseFlexibleDate(dto.getActivityDate()));
        } else if (dto.getActivityDate() != null && dto.getActivityDate().isEmpty()) {
            // 传空字符串则清空日期
            box.setActivityDate(null);
        }
        if (dto.getActivityTimePeriod() != null) {
            box.setActivityTimePeriod(dto.getActivityTimePeriod());
        }
        if (dto.getRequiredCount() != null) {
            box.setRequiredCount(dto.getRequiredCount());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            box.setStatus(dto.getStatus());
        }
        if (dto.getActivityTypeTags() != null) {
            box.setActivityTypeTags(dto.getActivityTypeTags());
        }

        blindBoxMapper.updateById(box);
        log.info("管理员编辑盲盒: id={}, title={}", id, dto.getTitle());
        return box;
    }

    /**
     * 获取盲盒统计
     */
    public java.util.Map<String, Object> getStats() {
        return java.util.Map.of(
                "total", blindBoxMapper.countTotal(),
                "open", blindBoxMapper.countOpen()
        );
    }

    // ==================== 用户参与/发布记录查询 ====================

    /**
     * 获取用户参与过的盲盒列表（作为申请人）
     */
    public List<BlindBox> getParticipatedBlindBoxes(Long userId) {
        List<Long> boxIds = applicationMapper.findBlindBoxIdsByUserId(userId);
        if (boxIds == null || boxIds.isEmpty()) {
            return List.of();
        }
        return blindBoxMapper.selectBatchIds(boxIds);
    }

    /**
     * 获取用户发布的盲盒列表
     */
    public List<BlindBox> getPublishedBlindBoxes(Long userId) {
        LambdaQueryWrapper<BlindBox> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlindBox::getPublisherId, userId)
               .orderByDesc(BlindBox::getCreatedAt);
        return blindBoxMapper.selectList(wrapper);
    }

    /**
     * 获取某盲盒的申请者列表（供发布者查看）
     */
    public List<BlindBoxApplication> getApplicationsByBlindBoxId(Long blindBoxId) {
        LambdaQueryWrapper<BlindBoxApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlindBoxApplication::getBlindBoxId, blindBoxId)
               .orderByDesc(BlindBoxApplication::getCreatedAt);
        return applicationMapper.selectList(wrapper);
    }

    /**
     * 获取用户的盲盒统计（参与数 + 发布数）
     */
    public java.util.Map<String, Object> getUserBlindBoxStats(Long userId) {
        long participatedCount = 0;
        long publishedCount = 0;

        // 参与的盲盒数量
        List<Long> participatedIds = applicationMapper.findBlindBoxIdsByUserId(userId);
        if (participatedIds != null) {
            participatedCount = participatedIds.size();
        }

        // 发布的盲盒数量
        LambdaQueryWrapper<BlindBox> pubWrapper = new LambdaQueryWrapper<>();
        pubWrapper.eq(BlindBox::getPublisherId, userId);
        publishedCount = blindBoxMapper.selectCount(pubWrapper);

        return java.util.Map.of(
                "participatedCount", participatedCount,
                "publishedCount", publishedCount
        );
    }

    /**
     * 灵活日期解析 —— 支持多种常见格式
     * 优先级：yyyy-MM-dd > yyyy.MM.dd > yyyy/MM/dd > MM.dd（补当年份）> MM/dd（补当年份）
     * 全部失败则抛出友好异常
     */
    private LocalDate parseFlexibleDate(String dateStr) {
        // 常见格式列表
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("MM.dd"),
            DateTimeFormatter.ofPattern("MM/dd")
        };

        // 先尝试带年份的四段式格式
        for (int i = 0; i < 3; i++) {
            try {
                return LocalDate.parse(dateStr, formatters[i]);
            } catch (DateTimeParseException ignored) { }
        }

        // 再尝试只有月日的短格式，自动补当年份
        for (int i = 3; i < formatters.length; i++) {
            try {
                LocalDate parsed = LocalDate.parse(dateStr, formatters[i]);
                return parsed.withYear(java.time.Year.now().getValue());
            } catch (DateTimeParseException ignored) { }
        }

        // 全部失败，返回友好错误信息
        throw new BusinessException(400,
            "日期格式错误：「" + dateStr + "」无法识别。请使用 yyyy-MM-dd 格式（如 2025-10-01）");
    }
}
