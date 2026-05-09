package com.blindweekend.manager.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 模板时段配置实体
 */
@Data
@TableName("template_segments")
public class TemplateSegment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属模板ID */
    private Long templateId;

    /** 时段顺序 */
    private Integer segmentOrder;

    /** 开始时间 */
    private String startTime;

    /** 结束时间 */
    private String endTime;

    /** 可选活动类型标签(JSON) */
    private String activityTypes;

    /** 时段名称 */
    private String segmentName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
