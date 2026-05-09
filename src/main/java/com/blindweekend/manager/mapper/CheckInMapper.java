package com.blindweekend.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blindweekend.manager.entity.CheckIn;
import com.blindweekend.manager.entity.Review;
import org.apache.ibatis.annotations.Mapper;

/**
 * 打卡记录 Mapper
 */
@Mapper
public interface CheckInMapper extends BaseMapper<CheckIn> {
}
