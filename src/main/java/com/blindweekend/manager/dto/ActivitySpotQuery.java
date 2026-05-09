package com.blindweekend.manager.dto;

import lombok.Data;

/**
 * 活动点查询条件
 */
@Data
public class ActivitySpotQuery {

    /** 关键词搜索(名称/地址) */
    private String keyword;

    /** 类型标签筛选 */
    private String typeTag;

    /** 消费水平: low/high */
    private String consumeLevel;

    /** 所在城市 */
    private String city;

    /** 状态: 1-启用, 0-禁用 */
    private Integer status;

    /** 页码 */
    private Integer pageNum = 1;

    /** 每页大小 */
    private Integer pageSize = 10;
}
