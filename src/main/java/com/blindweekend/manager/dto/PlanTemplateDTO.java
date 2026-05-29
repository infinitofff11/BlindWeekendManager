package com.blindweekend.manager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

/**
 * 方案模板创建/更新DTO（含时段配置）
 */
@Data
public class PlanTemplateDTO {

    @NotBlank(message = "模板名称不能为空")
    private String name;

    /** 模板描述 */
    private String description;

    /** 总时长(分钟) */
    private Integer totalDuration;

    /** 消费水平: low/high */
    private String consumeLevel;

    /** 主题类型 */
    private String themeType;

    /** 排序权重 */
    private Integer sortOrder;

    /** 时段配置列表 */
    @NotEmpty(message = "时段配置不能为空")
    private List<SegmentDTO> segments;
}
