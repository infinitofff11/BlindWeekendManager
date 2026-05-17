package com.blindweekend.manager.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 盲盒实体
 */
@Data
@TableName("blind_boxes")
public class BlindBox {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发布者ID */
    private Long publisherId;

    /** 来源方案ID */
    private Long planId;

    /** 盲盒标题摘要 */
    private String title;

    /** 一句话描述 */
    private String summaryText;

    /** 心情文案 */
    private String moodText;

    /** 所在城市 */
    private String city;

    /** 大致区域 */
    private String district;

    /** 活动日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate activityDate;

    /** 时间段 */
    private String activityTimePeriod;

    /** 需求人数 */
    private Integer requiredCount;

    /** 已确认人数 */
    private Integer currentCount;

    /** 活动类型标签(JSON) */
    private String activityTypeTags;

    /** 状态: open/full/closed/cancelled */
    private String status;

    /** 浏览次数 */
    private Integer viewCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
