package com.blindweekend.manager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 模板时段DTO
 */
@Data
public class SegmentDTO {

    /** 时段顺序 */
    @NotNull(message = "时段顺序不能为空")
    private Integer segmentOrder;

    /** 开始时间，如"09:00" */
    @NotBlank(message = "开始时间不能为空")
    private String startTime;

    /** 结束时间 */
    @NotBlank(message = "结束时间不能为空")
    private String endTime;

    /** 可选活动类型标签（逗号分隔） */
    private String activityTypes;

    /** 时段名称 */
    @NotBlank(message = "时段名称不能为空")
    private String segmentName;
}
