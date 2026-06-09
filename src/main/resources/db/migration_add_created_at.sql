-- =============================================
-- 数据库迁移: 补充缺失的 created_at 列
-- 原因: Entity 实体类有 @TableField(fill=INSERT) 的 createdAt 字段,
--       MyBatis-Plus MetaObjectHandler 会自动填充该字段并写入 DB,
--       但以下表缺少对应列导致 INSERT 报 "Unknown column"
--
-- 执行方式: 在 MySQL 中运行此脚本即可
-- =============================================

-- 1. template_segments 表添加 created_at 列
ALTER TABLE template_segments
    ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
    AFTER segment_name;

