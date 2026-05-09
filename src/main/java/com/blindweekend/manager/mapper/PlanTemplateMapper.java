package com.blindweekend.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blindweekend.manager.entity.PlanTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

/**
 * 方案模板 Mapper
 */
@Mapper
public interface PlanTemplateMapper extends BaseMapper<PlanTemplate> {

    /**
     * 查询启用的模板数量
     */
    @Select("SELECT COUNT(*) FROM plan_templates WHERE status = 1")
    long countActive();
}
