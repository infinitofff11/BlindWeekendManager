-- =====================================================
-- 管理员表 + 默认管理员账号初始化脚本
-- 版本: 1.0.0 (幂等版本 - 支持重复执行)
--
-- 使用方式:
--   mysql -u root -p blind_weekend < admin_users.sql
-- =====================================================

USE blind_weekend;

-- -----------------------------------------------------
-- 管理员表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `admin_users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname` VARCHAR(50) COMMENT '显示名称',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- -----------------------------------------------------
-- 插入默认管理员账号
-- 用户名: admin
-- 密码: admin123 (BCrypt哈希值)
-- -----------------------------------------------------
INSERT INTO `admin_users` (`username`, `password`, `nickname`, `status`) VALUES
('admin', '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.wjKwWYQRVhMSwC', '超级管理员', 1)
ON DUPLICATE KEY UPDATE `username` = VALUES(`username`);

-- =====================================================
-- ✅ 管理员表初始化完成！默认账号: admin / admin123
-- =====================================================
