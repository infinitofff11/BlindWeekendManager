package com.blindweekend.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blindweekend.manager.entity.ActivitySpot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 活动点 Mapper
 */
@Mapper
public interface ActivitySpotMapper extends BaseMapper<ActivitySpot> {

    /**
     * 按关键词和条件查询活动点总数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM activity_spots " +
            "WHERE 1=1 " +
            "<if test='query.keyword != null and query.keyword != \"\"'>" +
            " AND (name LIKE CONCAT('%', #{query.keyword}, '%') OR address LIKE CONCAT('%', #{query.keyword}, '%'))" +
            "</if>" +
            "<if test='query.typeTag != null and query.typeTag != \"\"'>" +
            " AND type_tags LIKE CONCAT('%', #{query.typeTag}, '%')" +
            "</if>" +
            "<if test='query.consumeLevel != null and query.consumeLevel != \"\"'>" +
            " AND consume_level = #{query.consumeLevel}" +
            "</if>" +
            "<if test='query.city != null and query.city != \"\"'>" +
            " AND city = #{query.city}" +
            "</if>" +
            "<if test='query.status != null'>" +
            " AND status = #{query.status}" +
            "</if>" +
            "</script>")
    long countByQuery(@Param("query") com.blindweekend.manager.dto.ActivitySpotQuery query);
}
