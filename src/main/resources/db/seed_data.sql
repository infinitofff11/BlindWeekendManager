-- =====================================================
-- 不期周末 (BlindWeekend) 数据种子脚本
-- 版本: 1.0.0
--
-- ⚠️ 本脚本可安全重复执行！
-- 使用 INSERT IGNORE / REPLACE 策略，不会重复插入数据
--
-- 使用方式:
--   mysql -u root -p blind_weekend < seed_data.sql
--   或在MySQL客户端中(已选择blind_weekend数据库):
--     source D:/BlindWeekend/BlindWeekendManager/src/main/resources/db/seed_data.sql
-- =====================================================

USE blind_weekend;

-- 设置客户端字符集为 utf8mb4，确保中文JSON数据正确解析
SET NAMES utf8mb4;

-- =====================================================
-- 一、成就/称号 初始数据 (6条)
-- =====================================================

INSERT IGNORE INTO achievements (name, icon, description, condition_type, condition_value, sort_order) VALUES
('周末出逃家', '🏃', '完成首次周末出行', 'first_checkin', 1, 1),
('探店雷达', '📡', '累计打卡10次', 'checkin_count', 10, 2),
('文艺青年', '📚', '参与5次文艺类活动', 'activity_type_count', 5, 3),
('运动达人', '⚽', '参与5次运动类活动', 'activity_type_count', 5, 4),
('社交达人', '🤝', '成功组队5次', 'team_success_count', 5, 5),
('周末常客', '🌟', '累计使用APP 4周', 'weekly_active', 4, 6);

-- =====================================================
-- 二、示例活动点数据 (北京地区)
-- =====================================================

-- --- 文艺类 ---
INSERT IGNORE INTO activity_spots (id, name, type_tags, consume_per_person, address, latitude, longitude, city, district, suggest_time_period, suitable_capacity, cover_image_url, description, recommend_duration, consume_level, status) VALUES
(1, '单向空间书店', '["文艺", "探店"]', 0.00, '北京市朝阳区望京街道望京SOHO T3一层', 39.9900, 116.4700, '北京', '朝阳区', 'all_day', 'both', '', '一家有温度的独立文艺书店，书籍精选，环境安静舒适。适合独自阅读或和朋友小聚。', 90, 'low', 1),

(2, '今日美术馆', '["文艺"]', 50.00, '北京市朝阳区百子湾路32号苹果社区', 39.8980, 116.4780, '北京', '朝阳区', 'afternoon', 'both', '', '当代艺术展览空间，定期举办各类艺术展览和文化活动。建议预留1-2小时观展。', 120, 'low', 1),

(3, '郎园Vintage', '["文艺", "户外"]', 0.00, '北京市朝阳区通惠河北路10号', 39.9020, 116.4680, '北京', '朝阳区', 'afternoon', 'both', '', '老工厂改造的文创园区，集书店、咖啡馆、展览于一体，文艺气息浓厚。', 150, 'low', 1);

-- --- 探店/美食类 ---
INSERT IGNORE INTO activity_spots (id, name, type_tags, consume_per_person, address, latitude, longitude, city, district, suggest_time_period, suitable_capacity, cover_image_url, description, recommend_duration, consume_level, status) VALUES
(4, '一风堂拉面', '["一人食", "探店"]', 60.00, '北京市朝阳区三里屯太古里南区B1层', 39.9360, 116.4560, '北京', '朝阳区', 'morning,afternoon', 'solo', '', '日式拉面专门店，一人食友好，味道正宗，环境干净整洁。', 60, 'low', 1),

(5, '南锣鼓巷', '["探店", "户外"]', 30.00, '北京市东城区南锣鼓巷胡同', 39.9360, 116.4030, '北京', '东城区', 'all_day', 'both', '', '北京最著名的胡同之一，两侧布满特色小店、咖啡馆和小吃店，适合闲逛探店。', 120, 'low', 1),

(6, '%Arabica咖啡', '["探店", "文艺"]', 45.00, '北京市西城区杨梅竹斜街39号', 39.8950, 116.3950, '北京', '西城区', 'morning,afternoon', 'solo,small_group', '', '网红精品咖啡馆，手冲咖啡品质出色，适合一个人发呆或和朋友聊天。', 60, 'low', 1),

(7, '四季民福烤鸭店', '["探店", "一人食"]', 120.00, '北京市东城区王府井大街138号', 39.9140, 116.4110, '北京', '东城区', 'afternoon,evening', 'small_group', '', '北京知名烤鸭店，性价比高，环境有京味儿，适合2-4人聚餐体验。', 90, 'high', 1);

-- --- 运动类 ---
INSERT IGNORE INTO activity_spots (id, name, type_tags, consume_per_person, address, latitude, longitude, city, district, suggest_time_period, suitable_capacity, cover_image_url, description, recommend_duration, consume_level, status) VALUES
(8, '奥林匹克森林公园', '["运动", "户外"]', 0.00, '北京市朝阳区北辰东路15号', 40.0090, 116.3890, '北京', '朝阳区', 'morning,afternoon', 'both', '', '北京最大的城市公园之一，南北园合计约680公顷。跑步、骑行、飞盘、徒步皆宜。免费入场！', 180, 'low', 1),

(9, '朝阳公园', '["运动", "户外"]', 5.00, '北京市朝阳区农展南路1号', 39.9410, 116.4720, '北京', '朝阳区', 'all_day', 'both', '', '四环内最大城市公园，有湖泊、草坪和健身步道。适合晨跑、放风筝、野餐露营。', 120, 'low', 1),

(10, '回龙观骑行道', '["运动", "户外"]', 0.00, '北京市昌平区回龙观地区', 40.0750, 116.3370, '北京', '昌平区', 'morning,afternoon', 'small_group', '', '北京首条自行车专用道，全长约6.5公里，骑行体验极佳，沿途风景优美。', 90, 'low', 1);

-- --- 户外类 ---
INSERT IGNORE INTO activity_spots (id, name, type_tags, consume_per_person, address, latitude, longitude, city, district, suggest_time_period, suitable_capacity, cover_image_url, description, recommend_duration, consume_level, status) VALUES
(11, '玉渊潭公园', '["户外"]', 10.00, '北京市海淀区西三环中路10号', 39.9180, 116.2830, '北京', '海淀区', 'all_day', 'both', '', '春季赏樱胜地，平时也是散步休闲的好去处。湖面开阔，可划船游览。', 150, 'low', 1),

(12, '温榆河公园', '["户外", "运动"]', 0.00, '北京市朝阳区孙河乡', 39.9980, 116.5520, '北京', '朝阳区', 'all_day', 'both', '', '超大生态公园，有露营区、儿童游乐设施、湿地景观。周末露营热门地点。', 180, 'low', 1),

(13, '香山公园', '["户外", "运动"]', 15.00, '北京市海淀区买卖街40号', 39.9940, 116.1950, '北京', '海淀区', 'morning,afternoon', 'both', '', '北京著名登山胜地，秋季红叶闻名全国。登山强度适中，俯瞰京城美景。', 240, 'low', 1);

-- --- 技能/手工类 ---
INSERT IGNORE INTO activity_spots (id, name, type_tags, consume_per_person, address, latitude, longitude, city, district, suggest_time_period, suitable_capacity, cover_image_url, description, recommend_duration, consume_level, status) VALUES
(14, '陶然亭手作工坊', '["技能"]', 88.00, '北京市西城区太平街19号陶然亭公园内', 39.8720, 116.3800, '北京', '西城区', 'afternoon', 'solo,small_group', '', '提供陶艺、绘画、花艺等多种DIY体验课程。新手友好，作品可带走。', 120, 'low', 1),

(15, 'UCCA尤伦斯当代艺术中心工作坊', '["文艺", "技能"]', 150.00, '北京市朝阳区酒仙桥路4号798艺术区', 9820, 116.4950, '北京', '朝阳区', 'afternoon', 'small_group', '', '定期举办艺术工作坊：版画制作、摄影、艺术导览等。需提前预约。', 150, 'high', 1);

-- --- 甜品/休闲类 ---
INSERT IGNORE INTO activity_spots (id, name, type_tags, consume_per_person, address, latitude, longitude, city, district, suggest_time_period, suitable_capacity, cover_image_url, description, recommend_duration, consume_level, status) VALUES
(16, '好利来(旗舰店)', '["甜品", "探店"]', 35.00, '北京市朝阳区建国路87号SKP B1层', 39.9080, 116.4620, '北京', '朝阳区', 'all_day', 'solo', '', '网红蛋糕品牌旗舰店，黑天鹅系列、半熟芝士必尝。适合下午茶歇脚。', 45, 'low', 1),

(17, '铃木食堂', '["探店", "甜品"]', 70.00, '北京市东鼓楼东大街103号', 39.9370, 116.4020, '北京', '东城区', 'afternoon', 'small_group', '', '日式家庭料理小店，氛围温馨治愈，甜品和咖喱饭是招牌。', 75, 'low', 1);

-- =====================================================
-- 三、方案模板示例数据 (含时段配置)
-- =====================================================

-- 模板1: 文艺半日游
INSERT IGNORE INTO plan_templates (id, name, description, total_duration, consume_level, theme_type, sort_order, status) VALUES
(1, '文艺半日游', '适合喜欢安静阅读、看展的文艺青年，半天时间沉浸式体验。', 240, 'low', '文艺', 1, 1);

INSERT IGNORE INTO template_segments (template_id, segment_order, start_time, end_time, activity_types, segment_name) VALUES
(1, 1, '09:30', '11:00', '["文艺"]', '上午文艺时光'),
(1, 2, '11:30', '13:00', '["一人食","探店"]', '午餐时光'),
(1, 3, '14:00', '16:00', '["文艺","探店","户外"]', '下午悠闲时光');

-- 模板2: 运动活力一日游
INSERT IGNORE INTO plan_templates (id, name, description, total_duration, consume_level, theme_type, sort_order, status) VALUES
(2, '运动活力一日游', '用一整天释放活力！从晨跑到户外运动再到休闲放松。', 420, 'low', '运动', 2, 1);

INSERT IGNORE INTO template_segments (template_id, segment_order, start_time, end_time, activity_types, segment_name) VALUES
(2, 1, '08:00', '10:00', '["运动","户外"]', '晨间有氧'),
(2, 2, '10:30', '12:00', '["运动"]', '运动挑战'),
(2, 3, '12:30', '14:00', '["一人食","探店"]', '能量午餐'),
(2, 4, '15:00', '17:00', '["户外"]', '自然放松');

-- 模板3: 探店吃货半日游
INSERT IGNORE INTO plan_templates (id, name, description, total_duration, consume_level, theme_type, sort_order, status) VALUES
(3, '探店吃货半日游', '穿梭于城市角落，发现隐藏的美味小店和特色咖啡馆。', 300, 'low', '探店', 3, 1);

INSERT IGNORE INTO template_segments (template_id, segment_order, start_time, end_time, activity_types, segment_name) VALUES
(3, 1, '10:00', '11:30', '["探店","甜品"]', '早茶探店'),
(3, 2, '12:00', '13:30', '["探店","一人食"]', '正餐打卡'),
(3, 3, '14:00', '15:30', '["甜品","探店"]', '下午茶时光');

-- 模板4: 户外轻徒步
INSERT IGNORE INTO plan_templates (id, name, description, total_duration, consume_level, theme_type, sort_order, status) VALUES
(4, '户外轻徒步', '亲近自然的轻度户外路线，适合初次体验者，不累但有趣。', 360, 'low', '户外', 4, 1);

INSERT IGNORE INTO template_segments (template_id, segment_order, start_time, end_time, activity_types, segment_name) VALUES
(4, 1, '09:00', '11:30', '["户外","运动"]', '徒步探索'),
(4, 2, '12:00', '13:30', '["一人食","探店"]', '山脚野餐'),
(4, 3, '14:00', '16:00', '["户外"]', '湖边休憩');

-- 模板5: 技能体验半日
INSERT IGNORE INTO plan_templates (id, name, description, total_duration, consume_level, theme_type, sort_order, status) VALUES
(5, '技能体验半日', '学习一门新技能！陶艺/绘画/花艺等DIY体验。', 240, 'low', '技能', 5, 1);

INSERT IGNORE INTO template_segments (template_id, segment_order, start_time, end_time, activity_types, segment_name) VALUES
(5, 1, '10:00', '12:00', '["技能"]', '动手创作'),
(5, 2, '13:00', '14:30', '["探店","一人食"]', '午餐补给'),
(5, 3, '15:00', '16:30', '["文艺","探店"]', '成果分享');

-- 模板6: 周末精致一日游 (高消)
INSERT IGNORE INTO plan_templates (id, name, description, total_duration, consume_level, theme_type, sort_order, status) VALUES
(6, '周末精致一日游', '高品质周末方案：精品展览+精致餐饮+付费体验活动。', 480, 'high', '文艺', 1, 1);

INSERT IGNORE INTO template_segments (template_id, segment_order, start_time, end_time, activity_types, segment_name) VALUES
(6, 1, '10:00', '12:00', '["文艺"]', '精品展览'),
(6, 2, '12:30', '14:30', '["探店"]', '精致午餐'),
(6, 3, '15:00', '17:00', '["技能","文艺"]', '付费体验');

-- =====================================================
-- 三-2、惠州地区活动点数据 (10条)
-- =====================================================

INSERT IGNORE INTO activity_spots (id, name, type_tags, consume_per_person, address, latitude, longitude, city, district, suggest_time_period, suitable_capacity, cover_image_url, description, recommend_duration, consume_level, status) VALUES
(18, '惠州西湖', '["文艺", "户外"]', 0.00, '惠州市惠城区环城西路', 23.0830, 114.4120, '惠州', '惠城区', 'all_day', 'both', '', '惠州西湖风景名胜区，国家5A级景区，有"大中国西湖三十六，唯惠州足并杭州"的美誉。湖光山色，漫步苏堤，适合文艺拍照和户外散步。', 180, 'low', 1),
(19, '祝屋巷', '["文艺", "探店"]', 35.00, '惠州市惠城区祝屋巷', 23.0860, 114.4100, '惠州', '惠城区', 'afternoon', 'both', '', '西湖畔的文艺小巷，咖啡馆、手作店、独立书店林立，惠州版"鼓浪屿"。适合半日闲逛探店。', 120, 'low', 1),
(20, '水东街', '["探店", "文艺"]', 50.00, '惠州市惠城区水东街', 23.0890, 114.4160, '惠州', '惠城区', 'afternoon', 'both', '', '百年骑楼老街改造而成的文创街区，集茶馆、民谣酒吧、手工体验于一体，古韵新潮并存。', 150, 'low', 1),
(21, '红花湖景区', '["运动", "户外"]', 0.00, '惠州市惠城区龙丰街道', 23.0700, 114.3800, '惠州', '惠城区', 'morning', 'both', '', '18公里环湖绿道是骑行和跑步爱好者的天堂，山水相依，空气清新。免费入园！', 180, 'low', 1),
(22, '高榜山', '["运动", "户外"]', 0.00, '惠州市惠城区高榜山', 23.0720, 114.3700, '惠州', '惠城区', 'morning', 'both', '', '惠城区最高峰，登山步道完善，约40分钟登顶可俯瞰全城美景，是市民周末登高首选。', 120, 'low', 1),
(23, '罗浮山', '["户外", "运动"]', 54.00, '惠州市博罗县长宁镇', 23.2800, 114.0000, '惠州', '博罗县', 'all_day', 'both', '', '中国道教名山，5A级景区。山势雄伟，飞瀑鸣泉，登山+祈福+品素斋，一日游佳选。', 360, 'low', 1),
(24, '惠东巽寮湾', '["户外", "探店"]', 0.00, '惠州市惠东县巽寮湾', 22.9300, 114.6000, '惠州', '惠东县', 'all_day', 'both', '', '粤东最洁净的海湾之一，碧海银沙。可出海捕鱼、沙滩BBQ、浮潜看珊瑚，周末海岛度假首选。', 360, 'low', 1),
(25, '惠州老街茶馆', '["探店", "文艺"]', 45.00, '惠州市惠城区桥东街道', 23.0880, 114.4180, '惠州', '惠城区', 'afternoon', 'small_group', '', '藏在骑楼里的老茶馆，功夫茶和潮式点心是招牌，氛围怀旧，适合悠闲下午茶。', 90, 'low', 1),
(26, '惠阳陶艺工坊', '["技能"]', 98.00, '惠州市惠阳区淡水街道', 22.7900, 114.4700, '惠州', '惠阳区', 'afternoon', 'solo,small_group', '', '专业陶艺体验工坊，拉坯、修坯、上釉一条龙。作品烧制后可邮寄到家，适合周末手工体验。', 150, 'low', 1),
(27, '东坡祠', '["文艺"]', 0.00, '惠州市惠城区桥东街道东坡路', 23.0880, 114.4200, '惠州', '惠城区', 'morning', 'both', '', '苏东坡被贬惠州时的居所遗址重建，了解东坡文化和惠州历史的好去处。环境清幽，免费参观。', 90, 'low', 1);

-- =====================================================
-- 四、惠州盲盒示例数据 (2条)
-- =====================================================

INSERT IGNORE INTO blind_boxes (id, publisher_id, title, summary_text, mood_text, district, city, activity_date, activity_time_period, required_count, current_count, activity_type_tags, status, view_count) VALUES
(1, 100, '惠州西湖文艺半日行', '逛祝屋巷咖啡店+漫步苏堤+水东街探店', '想在西湖边找个同频的人一起走走停停☕', '惠城区', '惠州', '2026-05-24', 'afternoon', 3, 1, '["文艺", "探店", "户外"]', 'open', 0),
(2, 101, '红花湖骑行局🚴', '环湖18km骑行+山脚下午茶', '一个人骑车太无聊了，来个搭子吧！', '惠城区', '惠州', '2026-05-25', 'morning', 2, 1, '["运动", "户外"]', 'open', 0);

-- =====================================================
-- ✅ 数据初始化完成！统计如下：
-- - 成就称号: 6条
-- - 示例活动点: 27条 (北京17 + 惠州10, 覆盖文艺/运动/探店/户外/技能/甜品)
-- - 方案模板: 6套 (含18个时段配置)
-- - 示例盲盒: 2条 (惠州)
-- =====================================================

-- 验证查询（执行后可查看结果）
-- SELECT COUNT(*) as '成就数' FROM achievements;
-- SELECT COUNT(*) as '活动点数' FROM activity_spots WHERE status = 1;
-- SELECT COUNT(*) as '模板数' FROM plan_templates WHERE status = 1;
