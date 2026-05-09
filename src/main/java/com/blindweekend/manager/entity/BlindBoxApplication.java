package com.blindweekend.manager.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 盲盒组队申请实体
 */
@Data
@TableName("blind_box_applications")
public class BlindBoxApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 盲盒ID */
    private Long blindBoxId;

    /** 申请人ID */
    private Long applicantId;

    /** 状态: pending/accepted/rejected */
    private String status;

    /** 申请留言 */
    private String message;

    /** 联系/确认时间 */
    private LocalDateTime contactedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
