package com.blindweekend.manager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 活动点创建/更新请求DTO
 */
@Data
public class ActivitySpotDTO {

    @NotBlank(message = "地点名称不能为空")
    private String name;

    /** 类型标签，逗号分隔或JSON数组 */
    private String typeTags;

    /** 人均消费 */
    private BigDecimal consumePerPerson;

    /** 详细地址 */
    @NotBlank(message = "地址不能为空")
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
}
