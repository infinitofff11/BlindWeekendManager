package com.blindweekend.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blindweekend.manager.dto.PlanItemVO;
import com.blindweekend.manager.entity.BlindBox;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 盲盒 Mapper
 */
@Mapper
public interface BlindBoxMapper extends BaseMapper<BlindBox> {

    /**
     * 统计盲盒总数
     */
    @Select("SELECT COUNT(*) FROM blind_boxes")
    long countTotal();

    /**
     * 统计进行中的盲盒
     */
    @Select("SELECT COUNT(*) FROM blind_boxes WHERE status = 'open'")
    long countOpen();

    /**
     * 更新盲盒状态
     */
    @Update("UPDATE blind_boxes SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 浏览量原子+1（并发安全）
     */
    @Update("UPDATE blind_boxes SET view_count = IFNULL(view_count,0) + 1, updated_at = NOW() WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    /**
     * 参与人数原子+1，满员时自动变更为full状态（并发安全）
     */
    @Update("UPDATE blind_boxes SET current_count = IFNULL(current_count,0) + 1, " +
            "status = CASE WHEN (IFNULL(current_count,0) + 1) >= required_count THEN 'full' ELSE status END, " +
            "updated_at = NOW() WHERE id = #{id} AND status = 'open'")
    int incrementParticipantCount(@Param("id") Long id);

    /**
     * 根据方案ID查询环节详情（关联活动点表）
     */
    @Select("SELECT pi.item_order, pi.start_time, pi.end_time, " +
            "s.name AS spot_name, s.address AS spot_address, " +
            "s.cover_image_url AS spot_cover_image, s.recommend_duration, pi.note " +
            "FROM plan_items pi " +
            "LEFT JOIN activity_spots s ON pi.spot_id = s.id " +
            "WHERE pi.plan_id = #{planId} " +
            "ORDER BY pi.item_order")
    List<PlanItemVO> findPlanItemsByPlanId(@Param("planId") Long planId);
}
