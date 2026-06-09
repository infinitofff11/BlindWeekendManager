-- =====================================================
-- 默认管理员账号初始化脚本
-- 版本: 1.0.0 (幂等版本 - 支持重复执行)
--
-- 使用方式:
--   mysql -u root -p blind_weekend < admin_users.sql
-- =====================================================

USE blind_weekend;

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
