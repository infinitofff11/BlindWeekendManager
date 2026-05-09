package com.blindweekend.manager.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动点实体
 */
@Data
@TableName("activity_spots")
public class ActivitySpot {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 地点名称 */
    private String name;

    /** 类型标签(JSON数组) */
    private String typeTags;

    /** 人均消费 */
    private BigDecimal consumePerPerson;

    /** 详细地址 */
    private String address;

    /** 纬度 */
    private BigDecimal latitude;

    /** 经度 */
    private BigDecimal longitude;

    /** 所在城市 */
    private String city;

    /** 所在区县 */
    private String district;

    /** 建议时段: morning/afternoon/evening/all_day */
    private String suggestTimePeriod;

    /** 适合容量: solo/small_group/both */
    private String suitableCapacity;

    /** 封面图URL */
    private String coverImageUrl;

    /** 简介 */
    private String description;

    /** 建议停留时长(分钟) */
    private Integer recommendDuration;

    /** 消费水平档位: low/high */
    private String consumeLevel;

    /** 状态: 1-启用, 0-禁用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
