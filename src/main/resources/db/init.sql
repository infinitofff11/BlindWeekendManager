-- =====================================================
-- 不期周末 (BlindWeekend) 数据库初始化脚本
-- 版本: 1.0.1 (幂等版本 - 支持重复执行)
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
--
-- 使用方式:
--   mysql -u root -p < init.sql
--   或在MySQL客户端中: source /path/to/init.sql
-- =====================================================

CREATE DATABASE IF NOT EXISTS blind_weekend DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE blind_weekend;

-- -----------------------------------------------------
-- 1. 用户表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    password VARCHAR(100) COMMENT '密码(加密存储)',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    city VARCHAR(50) COMMENT '当前城市',
    wechat_openid VARCHAR(100) UNIQUE COMMENT '微信OpenID',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
    last_login_time DATETIME COMMENT '最后登录时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='用户表';

-- -----------------------------------------------------
-- 2. 管理员表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS admin_users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    role ENUM('super_admin', 'admin') DEFAULT 'admin' COMMENT '角色',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
    last_login_time DATETIME COMMENT '最后登录时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='管理员表';

-- -----------------------------------------------------
-- 3. 用户偏好表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    consume_level ENUM('low', 'high') DEFAULT 'low' COMMENT '消费水平: low-低消, high-高消',
    activity_radius INT DEFAULT 5 COMMENT '活动半径(km): 3/5/10/0(全城)',
    default_group_size ENUM('1', '2', '3-4') DEFAULT '1' COMMENT '默认同行人数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB COMMENT='用户偏好表';

-- -----------------------------------------------------
-- 4. 用户兴趣标签关联表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS user_interest_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    tag_name VARCHAR(30) NOT NULL COMMENT '标签名称',
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uk_user_tag (user_id, tag_name)
) ENGINE=InnoDB COMMENT='用户兴趣标签关联表';

-- -----------------------------------------------------
-- 5. 活动点表（核心数据）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS activity_spots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '活动点ID',
    name VARCHAR(100) NOT NULL COMMENT '地点名称',
    type_tags JSON COMMENT '类型标签数组, 如:["文艺","探店"]',
    consume_per_person DECIMAL(10,2) DEFAULT 0 COMMENT '人均消费',
    address VARCHAR(255) COMMENT '详细地址',
    latitude DECIMAL(10,7) COMMENT '纬度',
    longitude DECIMAL(10,7) COMMENT '经度',
    city VARCHAR(50) COMMENT '所在城市',
    district VARCHAR(50) COMMENT '所在区县',
    suggest_time_period ENUM('morning', 'afternoon', 'evening', 'all_day') DEFAULT 'all_day' COMMENT '建议时段',
    suitable_capacity ENUM('solo', 'small_group', 'both') DEFAULT 'both' COMMENT '适合容量: solo-1人, small_group-2-4人, both-都行',
    cover_image_url VARCHAR(255) COMMENT '封面图URL',
    description TEXT COMMENT '简介',
    recommend_duration INT DEFAULT 60 COMMENT '建议停留时长(分钟)',
    consume_level ENUM('low', 'high') DEFAULT 'low' COMMENT '消费水平档位',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_city_status (city, status),
    INDEX idx_type_tags ((CAST(type_tags AS CHAR(255)))),
    INDEX idx_consume_level (consume_level)
) ENGINE=InnoDB COMMENT='活动点表';

-- -----------------------------------------------------
-- 6. 方案模板表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS plan_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    name VARCHAR(100) NOT NULL COMMENT '模板名称，如"文艺半日游"',
    description TEXT COMMENT '模板描述',
    total_duration INT COMMENT '总时长(分钟)',
    consume_level ENUM('low', 'high') DEFAULT 'low' COMMENT '适配消费水平',
    theme_type VARCHAR(50) COMMENT '主题类型: 文艺/运动/探店/户外/技能',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='方案模板表';

-- -----------------------------------------------------
-- 7. 模板时段配置表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS template_segments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '时段ID',
    template_id BIGINT NOT NULL COMMENT '所属模板ID',
    segment_order INT NOT NULL COMMENT '时段顺序',
    start_time VARCHAR(10) COMMENT '开始时间,如"09:00"',
    end_time VARCHAR(10) COMMENT '结束时间,如"11:00"',
    activity_types JSON COMMENT '可选活动类型标签,如:["文艺","探店"]',
    segment_name VARCHAR(50) COMMENT '时段名称,如"上午文艺时光"',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (template_id) REFERENCES plan_templates(id) ON DELETE CASCADE,
    INDEX idx_template_order (template_id, segment_order)
) ENGINE=InnoDB COMMENT='模板时段配置表';

-- -----------------------------------------------------
-- 8. 用户生成的方案表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS user_plans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '方案ID',
    user_id BIGINT NOT NULL COMMENT '生成者ID',
    template_id BIGINT COMMENT '使用的模板ID',
    plan_name VARCHAR(100) COMMENT '方案名称',
    plan_date DATE COMMENT '计划日期',
    is_favorited TINYINT DEFAULT 0 COMMENT '是否收藏',
    status ENUM('draft', 'completed', 'cancelled') DEFAULT 'draft' COMMENT '状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (template_id) REFERENCES plan_templates(id),
    INDEX idx_user_created (user_id, created_at DESC)
) ENGINE=InnoDB COMMENT='用户方案表';

-- -----------------------------------------------------
-- 9. 方案环节明细表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS plan_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NOT NULL COMMENT '方案ID',
    spot_id BIGINT NOT NULL COMMENT '活动点ID',
    item_order INT NOT NULL COMMENT '环节顺序',
    start_time VARCHAR(10) COMMENT '预计开始时间',
    end_time VARCHAR(10) COMMENT '预计结束时间',
    note VARCHAR(255) COMMENT '备注',
    FOREIGN KEY (plan_id) REFERENCES user_plans(id) ON DELETE CASCADE,
    FOREIGN KEY (spot_id) REFERENCES activity_spots(id),
    INDEX idx_plan_order (plan_id, item_order)
) ENGINE=InnoDB COMMENT='方案环节明细表';

-- -----------------------------------------------------
-- 10. 盲盒表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS blind_boxes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '盲盒ID',
    publisher_id BIGINT NOT NULL COMMENT '发布者ID',
    plan_id BIGINT COMMENT '来源方案ID',
    title VARCHAR(100) NOT NULL COMMENT '盲盒标题摘要',
    summary_text VARCHAR(200) COMMENT '一句话描述',
    mood_text VARCHAR(200) COMMENT '心情文案',
    district VARCHAR(50) COMMENT '大致区域,如"朝阳区"',
    activity_date DATE COMMENT '活动日期',
    activity_time_period VARCHAR(20) COMMENT '时间段,如"周六下午"',
    required_count INT NOT NULL DEFAULT 1 COMMENT '需求人数',
    current_count INT DEFAULT 0 COMMENT '已确认人数',
    activity_type_tags JSON COMMENT '活动类型标签',
    status ENUM('open', 'full', 'closed', 'cancelled') DEFAULT 'open' COMMENT '状态',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (publisher_id) REFERENCES users(id),
    FOREIGN KEY (plan_id) REFERENCES user_plans(id),
    INDEX idx_status_created (status, created_at DESC),
    INDEX idx_publisher (publisher_id)
) ENGINE=InnoDB COMMENT='盲盒表';

-- -----------------------------------------------------
-- 11. 盲盒组队申请表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS blind_box_applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    blind_box_id BIGINT NOT NULL COMMENT '盲盒ID',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    status ENUM('pending', 'accepted', 'rejected') DEFAULT 'pending' COMMENT '状态',
    message VARCHAR(200) COMMENT '申请留言',
    contacted_at DATETIME COMMENT '联系/确认时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (blind_box_id) REFERENCES blind_boxes(id) ON DELETE CASCADE,
    FOREIGN KEY (applicant_id) REFERENCES users(id),
    UNIQUE KEY uk_box_applicant (blind_box_id, applicant_id),
    INDEX idx_blind_box (blind_box_id, status)
) ENGINE=InnoDB COMMENT='盲盒组队申请表';

-- =====================================================
-- ✅ 表结构初始化完成！共11张表
-- 初始数据请运行: seed_data.sql
-- =====================================================
