package com.blindweekend.manager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 盲盒创建请求 DTO
 */
@Data
public class BlindBoxCreateDTO {

    /** 发布者用户ID */
    @NotNull(message = "发布者ID不能为空")
    private Long publisherId;

    /** 来源方案ID（可选） */
    private Long planId;

    /** 盲盒标题 */
    @NotBlank(message = "标题不能为空")
    @Size(max = 50, message = "标题最长50个字符")
    private String title;

    /** 一句话描述 */
    @Size(max = 100, message = "描述最长100个字符")
    private String summaryText;

    /** 心情文案 */
    @Size(max = 200, message = "心情文案最长200个字符")
    private String moodText;

    /** 所在城市 */
    @NotBlank(message = "城市不能为空")
    @Size(max = 20, message = "城市最长20个字符")
    private String city;

    /** 大致区域 */
    @Size(max = 20, message = "区域最长20个字符")
    private String district;

    /** 活动日期 (yyyy-MM-dd) */
    private String activityDate;

    /** 时间段 (morning/afternoon/evening/all_day) */
    private String activityTimePeriod;

    /** 需求人数 */
    @NotNull(message = "需求人数不能为空")
    private Integer requiredCount;

    /** 活动类型标签(JSON数组) */
    private String activityTypeTags;
}
