package com.blindweekend.manager.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 方案模板实体
 */
@Data
@TableName("plan_templates")
public class PlanTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模板名称 */
    private String name;

    /** 模板描述 */
    private String description;

    /** 总时长(分钟) */
    private Integer totalDuration;

    /** 适配消费水平: low/high */
    private String consumeLevel;

    /** 主题类型 */
    private String themeType;

    /** 排序权重 */
    private Integer sortOrder;

    /** 状态: 1-启用, 0-禁用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
