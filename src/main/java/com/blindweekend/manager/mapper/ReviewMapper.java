package com.blindweekend.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blindweekend.manager.entity.Review;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评价 Mapper
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
}
