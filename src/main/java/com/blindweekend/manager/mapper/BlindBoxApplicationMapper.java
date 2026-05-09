package com.blindweekend.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blindweekend.manager.entity.BlindBoxApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BlindBoxApplicationMapper extends BaseMapper<BlindBoxApplication> {

    /**
     * 查询用户参与过的盲盒ID列表
     */
    @Select("SELECT DISTINCT blind_box_id FROM blind_box_applications WHERE applicant_id = #{userId}")
    List<Long> findBlindBoxIdsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户发布的盲盒ID列表
     */
    @Select("SELECT id FROM blind_boxes WHERE publisher_id = #{userId}")
    List<Long> findPublishedBlindBoxIdsByUserId(@Param("userId") Long userId);
}
