package com.blindweekend.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blindweekend.manager.entity.TemplateSegment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

/**
 * 模板时段 Mapper
 */
@Mapper
public interface TemplateSegmentMapper extends BaseMapper<TemplateSegment> {

    /**
     * 删除某模板的所有时段配置
     */
    @Delete("DELETE FROM template_segments WHERE template_id = #{templateId}")
    int deleteByTemplateId(@Param("templateId") Long templateId);

    /**
     * 查询模板的所有时段（按顺序排列）
     */
    @Select("SELECT * FROM template_segments WHERE template_id = #{templateId} ORDER BY segment_order ASC")
    java.util.List<com.blindweekend.manager.entity.TemplateSegment> selectByTemplateId(@Param("templateId") Long templateId);
}
