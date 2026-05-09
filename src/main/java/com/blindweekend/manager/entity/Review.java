package com.blindweekend.manager.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评价实体
 */
@Data
@TableName("reviews")
public class Review {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联打卡记录ID */
    private Long checkInId;

    /** 评价的活动点ID */
    private Long spotId;

    /** 评价内容 */
    private String content;

    /** 评分1-5 */
    private Integer rating;

    /** 状态: 1-正常, 0-屏蔽 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
