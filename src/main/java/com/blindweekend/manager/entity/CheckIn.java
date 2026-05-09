package com.blindweekend.manager.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 打卡记录实体
 */
@Data
@TableName("check_ins")
public class CheckIn {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 目标类型: plan/blind_box */
    private String targetType;

    /** 目标ID */
    private Long targetId;

    /** 照片URL数组(JSON) */
    private String photoUrls;

    /** 短评 */
    private String shortReview;

    /** 评分1-5 */
    private Integer rating;

    /** 打卡时间 */
    private LocalDateTime checkInTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
