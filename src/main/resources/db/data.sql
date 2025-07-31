-- 租赁系统基础数据初始化脚本
-- 该脚本包含系统运行必需的基础数据

-- ================================
-- 1. 初始化系统权限
-- ================================

INSERT INTO permissions (name, description, type, resource, sort_order, created_at) VALUES
-- 用户管理权限
('USER_VIEW', '查看用户', 'API', '/api/users/**', 1, NOW()),
('USER_CREATE', '创建用户', 'API', '/api/users', 2, NOW()),
('USER_UPDATE', '更新用户', 'API', '/api/users/**', 3, NOW()),
('USER_DELETE', '删除用户', 'API', '/api/users/**', 4, NOW()),
('USER_ROLE_ASSIGN', '分配用户角色', 'API', '/api/users/*/roles', 5, NOW()),

-- 物品管理权限
('ITEM_VIEW', '查看物品', 'API', '/api/items/**', 10, NOW()),
('ITEM_CREATE', '创建物品', 'API', '/api/items', 11, NOW()),
('ITEM_UPDATE', '更新物品', 'API', '/api/items/**', 12, NOW()),
('ITEM_DELETE', '删除物品', 'API', '/api/items/**', 13, NOW()),
('ITEM_AUDIT', '审核物品', 'API', '/api/items/*/audit', 14, NOW()),
('ITEM_STATUS_MANAGE', '管理物品状态', 'API', '/api/items/*/status', 15, NOW()),

-- 物品分类权限
('CATEGORY_VIEW', '查看分类', 'API', '/api/categories/**', 20, NOW()),
('CATEGORY_CREATE', '创建分类', 'API', '/api/categories', 21, NOW()),
('CATEGORY_UPDATE', '更新分类', 'API', '/api/categories/**', 22, NOW()),
('CATEGORY_DELETE', '删除分类', 'API', '/api/categories/**', 23, NOW()),

-- 订单管理权限
('ORDER_VIEW', '查看订单', 'API', '/api/orders/**', 30, NOW()),
('ORDER_CREATE', '创建订单', 'API', '/api/orders', 31, NOW()),
('ORDER_UPDATE', '更新订单', 'API', '/api/orders/**', 32, NOW()),
('ORDER_CANCEL', '取消订单', 'API', '/api/orders/*/cancel', 33, NOW()),
('ORDER_AUDIT', '审核订单', 'API', '/api/orders/*/audit', 34, NOW()),
('ORDER_RETURN', '处理归还', 'API', '/api/orders/*/return', 35, NOW()),

-- 支付管理权限
('PAYMENT_VIEW', '查看支付', 'API', '/api/payments/**', 40, NOW()),
('PAYMENT_CREATE', '创建支付', 'API', '/api/payments', 41, NOW()),
('PAYMENT_PROCESS', '处理支付', 'API', '/api/payments/*/process', 42, NOW()),
('PAYMENT_REFUND', '处理退款', 'API', '/api/payments/*/refund', 43, NOW()),

-- 商家支付配置权限
('MERCHANT_PAYMENT_CONFIG', '商家支付配置', 'API', '/api/merchant/payment/**', 44, NOW()),
('MERCHANT_PAYMENT_VIEW', '查看商家支付配置', 'API', '/api/merchant/payment/config', 45, NOW()),
('MERCHANT_PAYMENT_UPDATE', '更新商家支付配置', 'API', '/api/merchant/payment/config', 46, NOW()),
('MERCHANT_PAYMENT_DELETE', '删除商家支付配置', 'API', '/api/merchant/payment/config', 47, NOW()),
('MERCHANT_PAYMENT_TEST', '测试商家支付配置', 'API', '/api/merchant/payment/config/test', 48, NOW()),

-- 管理员支付配置权限
('PAYMENT_ADMIN', '支付管理员权限', 'API', '/api/merchant/payment/admin/**', 49, NOW()),
('PAYMENT_CONFIG_AUDIT', '支付配置审核', 'API', '/api/merchant/payment/admin/configs/*/audit', 49, NOW()),
('PAYMENT_CONFIG_MANAGE', '支付配置管理', 'API', '/api/merchant/payment/admin/configs/**', 49, NOW()),

-- 合同管理权限
('CONTRACT_VIEW', '查看合同', 'API', '/api/contracts/**', 60, NOW()),
('CONTRACT_CREATE', '创建合同', 'API', '/api/contracts', 61, NOW()),
('CONTRACT_SIGN', '签署合同', 'API', '/api/contracts/*/sign', 62, NOW()),
('CONTRACT_TEMPLATE_MANAGE', '管理合同模板', 'API', '/api/contract-templates/**', 63, NOW()),

-- 财务管理权限
('FINANCE_VIEW', '查看财务', 'API', '/api/finance/**', 70, NOW()),
('FINANCE_RECORD_MANAGE', '管理财务记录', 'API', '/api/finance/records/**', 71, NOW()),
('FINANCE_REPORT_VIEW', '查看财务报表', 'API', '/api/finance/reports/**', 72, NOW()),
('FINANCE_REPORT_GENERATE', '生成财务报表', 'API', '/api/finance/reports/generate', 73, NOW()),

-- 权限管理权限
('PERMISSION_VIEW', '查看权限', 'API', '/api/permissions/**', 80, NOW()),
('PERMISSION_MANAGE', '管理权限', 'API', '/api/permissions/**', 81, NOW()),
('ROLE_VIEW', '查看角色', 'API', '/api/roles/**', 82, NOW()),
('ROLE_MANAGE', '管理角色', 'API', '/api/roles/**', 83, NOW()),

-- 通知管理权限
('NOTIFICATION_VIEW', '查看通知', 'API', '/api/notifications/**', 90, NOW()),
('NOTIFICATION_SEND', '发送通知', 'API', '/api/notifications/send', 91, NOW()),
('NOTIFICATION_MANAGE', '管理通知', 'API', '/api/notifications/**', 92, NOW()),

-- 评价与反馈管理权限
('REVIEW_VIEW', '查看评价', 'API', '/api/reviews/**', 100, NOW()),
('REVIEW_CREATE', '创建评价', 'API', '/api/reviews', 101, NOW()),
('REVIEW_REPLY', '回复评价', 'API', '/api/reviews/*/replies', 102, NOW()),
('REVIEW_MODERATE', '审核评价', 'API', '/api/reviews/*/moderate', 103, NOW()),
('REVIEW_DELETE', '删除评价', 'API', '/api/reviews/**', 104, NOW()),
('FEEDBACK_VIEW', '查看反馈', 'API', '/api/feedbacks/**', 105, NOW()),
('FEEDBACK_CREATE', '创建反馈', 'API', '/api/feedbacks', 106, NOW()),
('FEEDBACK_PROCESS', '处理反馈', 'API', '/api/feedbacks/*/process', 107, NOW()),
('FEEDBACK_ASSIGN', '分配反馈', 'API', '/api/feedbacks/*/assign', 108, NOW()),
('REVIEW_ANALYTICS', '评价分析', 'API', '/api/reviews/analytics', 109, NOW()),

-- 系统管理权限
('SYSTEM_VIEW', '查看系统信息', 'API', '/api/system/**', 110, NOW()),
('SYSTEM_CONFIG', '系统配置', 'API', '/api/system/config', 111, NOW()),
('SYSTEM_LOG_VIEW', '查看系统日志', 'API', '/api/system/logs', 112, NOW());

-- ================================
-- 2. 初始化系统角色
-- ================================

INSERT INTO roles (name, description, is_system, created_at, updated_at) VALUES
('ADMIN', '系统管理员', true, NOW(), NOW()),
('MANAGER', '业务经理', true, NOW(), NOW()),
('OPERATOR', '操作员', true, NOW(), NOW()),
('FINANCE', '财务人员', true, NOW(), NOW()),
('OWNER', '物品拥有者', true, NOW(), NOW()),
('CUSTOMER', '客户', true, NOW(), NOW());

-- ================================
-- 3. 分配角色权限
-- ================================

-- 管理员拥有所有权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;

-- 业务经理权限（除系统管理外的大部分权限，包括支付管理）
INSERT INTO role_permissions (role_id, permission_id)
SELECT 2, id FROM permissions
WHERE name IN (
    'USER_VIEW', 'USER_CREATE', 'USER_UPDATE', 'USER_ROLE_ASSIGN',
    'ITEM_VIEW', 'ITEM_CREATE', 'ITEM_UPDATE', 'ITEM_AUDIT', 'ITEM_STATUS_MANAGE',
    'CATEGORY_VIEW', 'CATEGORY_CREATE', 'CATEGORY_UPDATE',
    'ORDER_VIEW', 'ORDER_CREATE', 'ORDER_UPDATE', 'ORDER_AUDIT', 'ORDER_RETURN',
    'PAYMENT_VIEW', 'PAYMENT_PROCESS', 'PAYMENT_REFUND',
    'CONTRACT_VIEW', 'CONTRACT_CREATE', 'CONTRACT_TEMPLATE_MANAGE',
    'FINANCE_VIEW', 'FINANCE_REPORT_VIEW',
    'NOTIFICATION_VIEW', 'NOTIFICATION_SEND',
    'REVIEW_VIEW', 'REVIEW_MODERATE', 'REVIEW_ANALYTICS',
    'FEEDBACK_VIEW', 'FEEDBACK_PROCESS', 'FEEDBACK_ASSIGN',
    'PAYMENT_ADMIN', 'PAYMENT_CONFIG_AUDIT', 'PAYMENT_CONFIG_MANAGE'
);

-- 操作员权限（基础操作权限）
INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, id FROM permissions
WHERE name IN (
    'USER_VIEW',
    'ITEM_VIEW', 'ITEM_AUDIT', 'ITEM_STATUS_MANAGE',
    'CATEGORY_VIEW',
    'ORDER_VIEW', 'ORDER_AUDIT', 'ORDER_RETURN',
    'PAYMENT_VIEW',
    'CONTRACT_VIEW',
    'NOTIFICATION_VIEW',
    'REVIEW_VIEW', 'REVIEW_MODERATE',
    'FEEDBACK_VIEW', 'FEEDBACK_PROCESS'
);

-- 财务人员权限（财务相关权限）
INSERT INTO role_permissions (role_id, permission_id)
SELECT 4, id FROM permissions
WHERE name IN (
    'ORDER_VIEW',
    'PAYMENT_VIEW', 'PAYMENT_PROCESS', 'PAYMENT_REFUND',
    'FINANCE_VIEW', 'FINANCE_RECORD_MANAGE', 'FINANCE_REPORT_VIEW', 'FINANCE_REPORT_GENERATE'
);

-- 物品拥有者权限（管理自己的物品和评价回复，以及商家支付配置）
INSERT INTO role_permissions (role_id, permission_id)
SELECT 5, id FROM permissions
WHERE name IN (
    'ITEM_VIEW', 'ITEM_CREATE', 'ITEM_UPDATE',
    'CATEGORY_VIEW',
    'ORDER_VIEW',
    'CONTRACT_VIEW', 'CONTRACT_SIGN',
    'NOTIFICATION_VIEW',
    'REVIEW_VIEW', 'REVIEW_REPLY',
    'MERCHANT_PAYMENT_CONFIG', 'MERCHANT_PAYMENT_VIEW', 'MERCHANT_PAYMENT_UPDATE',
    'MERCHANT_PAYMENT_DELETE', 'MERCHANT_PAYMENT_TEST'
);

-- 客户权限（基础用户权限）
INSERT INTO role_permissions (role_id, permission_id)
SELECT 6, id FROM permissions
WHERE name IN (
    'ITEM_VIEW',
    'CATEGORY_VIEW',
    'ORDER_VIEW', 'ORDER_CREATE', 'ORDER_CANCEL',
    'PAYMENT_VIEW',
    'CONTRACT_VIEW', 'CONTRACT_SIGN',
    'NOTIFICATION_VIEW',
    'REVIEW_VIEW', 'REVIEW_CREATE',
    'FEEDBACK_CREATE'
);

-- ================================
-- 4. 创建默认管理员用户
-- ================================

-- 创建管理员用户（密码：123456，正确的BCrypt加密）
INSERT INTO users (username, password, email, status, created_at, updated_at) VALUES
('admin', '$2a$10$wvq6qQYJWV/avr//Bkq2oe5H3VPY0UO4.EfrILAE43M6mc6ruFfGu', 'admin@rental.com', 'ACTIVE', NOW(), NOW()),
('manager', '$2a$10$wvq6qQYJWV/avr//Bkq2oe5H3VPY0UO4.EfrILAE43M6mc6ruFfGu', 'manager@rental.com', 'ACTIVE', NOW(), NOW()),
('finance', '$2a$10$wvq6qQYJWV/avr//Bkq2oe5H3VPY0UO4.EfrILAE43M6mc6ruFfGu', 'finance@rental.com', 'ACTIVE', NOW(), NOW());

-- 分配用户角色
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- admin -> ADMIN
(2, 2), -- manager -> MANAGER
(3, 4); -- finance -> FINANCE

-- 创建用户资料
INSERT INTO user_profiles (user_id, real_name, gender, created_at, updated_at) VALUES
(1, '系统管理员', 'OTHER', NOW(), NOW()),
(2, '业务经理', 'OTHER', NOW(), NOW()),
(3, '财务人员', 'OTHER', NOW(), NOW());

-- ================================
-- 5. 初始化物品分类
-- ================================

INSERT INTO item_categories (name, description, sort_order, created_at, updated_at) VALUES
('电子设备', '各类电子设备租赁', 1, NOW(), NOW()),
('家具家电', '家具和家电设备', 2, NOW(), NOW()),
('工具设备', '各类工具和设备', 3, NOW(), NOW()),
('运动器材', '体育运动相关器材', 4, NOW(), NOW()),
('交通工具', '各类交通工具', 5, NOW(), NOW()),
('服装配饰', '服装和配饰用品', 6, NOW(), NOW()),
('摄影器材', '摄影摄像设备', 7, NOW(), NOW()),
('音响设备', '音响和音乐设备', 8, NOW(), NOW());

-- 添加子分类
INSERT INTO item_categories (name, description, parent_id, sort_order, created_at, updated_at) VALUES
-- 电子设备子分类
('笔记本电脑', '各品牌笔记本电脑', 1, 1, NOW(), NOW()),
('平板电脑', '平板电脑设备', 1, 2, NOW(), NOW()),
('智能手机', '智能手机设备', 1, 3, NOW(), NOW()),
('游戏设备', '游戏机和相关设备', 1, 4, NOW(), NOW()),

-- 家具家电子分类
('办公家具', '办公桌椅等办公家具', 2, 1, NOW(), NOW()),
('家用电器', '冰箱、洗衣机等家电', 2, 2, NOW(), NOW()),
('家居用品', '床、沙发等家居用品', 2, 3, NOW(), NOW()),

-- 工具设备子分类
('电动工具', '电钻、切割机等电动工具', 3, 1, NOW(), NOW()),
('手动工具', '扳手、螺丝刀等手动工具', 3, 2, NOW(), NOW()),
('测量设备', '测量仪器和设备', 3, 3, NOW(), NOW()),

-- 运动器材子分类
('健身器材', '跑步机、哑铃等健身设备', 4, 1, NOW(), NOW()),
('户外运动', '登山、露营等户外设备', 4, 2, NOW(), NOW()),
('球类运动', '足球、篮球等球类设备', 4, 3, NOW(), NOW());

-- ================================
-- 6. 创建示例物品
-- ================================

INSERT INTO items (name, description, category_id, owner_id, price_per_day, deposit, status, location, approval_status, created_at, updated_at) VALUES
-- 使用正确的category_id: 9=笔记本电脑, 7=摄影器材, 11=智能手机, 17=健身器材, 13=办公家具, 12=游戏设备
-- owner_id=1 假设第一个管理员用户作为物品所有者
('MacBook Pro 16英寸', '苹果MacBook Pro 16英寸，M1 Pro芯片，16GB内存，512GB存储', 9, 1, 200.00, 8000.00, 'AVAILABLE', '北京市朝阳区', 'APPROVED', NOW(), NOW()),
('佳能EOS R5', '佳能全画幅无反相机，4500万像素，支持8K视频录制', 7, 1, 150.00, 12000.00, 'AVAILABLE', '上海市浦东新区', 'APPROVED', NOW(), NOW()),
('iPhone 14 Pro', '苹果iPhone 14 Pro，256GB存储，深空黑色', 11, 1, 80.00, 3000.00, 'AVAILABLE', '广州市天河区', 'APPROVED', NOW(), NOW()),
('跑步机NordicTrack', '商用级跑步机，可折叠设计，多种运动模式', 17, 1, 50.00, 2000.00, 'AVAILABLE', '深圳市南山区', 'APPROVED', NOW(), NOW()),
('办公桌椅套装', '人体工学办公桌椅，升降式设计，适合长时间办公', 13, 1, 30.00, 500.00, 'AVAILABLE', '杭州市西湖区', 'APPROVED', NOW(), NOW()),
('大疆无人机Mini 3', 'DJI Mini 3无人机，4K摄像，30分钟续航', 7, 1, 120.00, 2500.00, 'AVAILABLE', '成都市锦江区', 'APPROVED', NOW(), NOW()),
('索尼PlayStation 5', '索尼PS5游戏机，825GB SSD，包含手柄', 12, 1, 60.00, 2000.00, 'AVAILABLE', '重庆市渝中区', 'APPROVED', NOW(), NOW());

-- ================================
-- 7. 创建合同模板
-- ================================

INSERT INTO contract_templates (name, content, version, is_active, created_at, updated_at) VALUES
('标准租赁合同模板',
'租赁合同

甲方（出租方）：租赁平台
乙方（承租方）：{{用户姓名}}

根据《中华人民共和国合同法》等相关法律法规，甲乙双方在平等、自愿、协商一致的基础上，就租赁事宜达成如下协议：

第一条 租赁物品
租赁物品名称：{{物品名称}}
租赁物品描述：{{物品描述}}
租赁数量：{{租赁数量}}

第二条 租赁期限
租赁期限：从{{开始日期}}至{{结束日期}}
租赁天数：{{租赁天数}}天

第三条 租金及押金
日租金：{{日租金}}元/天
总租金：{{总租金}}元
押金：{{押金}}元

第四条 乙方义务
1. 按时支付租金和押金
2. 妥善保管租赁物品，不得损坏或丢失
3. 按时归还租赁物品
4. 不得将租赁物品转租给第三方

第五条 甲方义务
1. 保证租赁物品的合法性和可用性
2. 提供技术支持和使用指导
3. 及时处理租赁过程中的问题

第六条 违约责任
1. 乙方逾期归还的，每日按租金的10%支付违约金
2. 物品损坏或丢失的，乙方应赔偿相应损失
3. 其他违约情况按相关法律法规处理

第七条 争议解决
本合同履行过程中发生争议，双方应协商解决；协商不成的，可向合同签订地人民法院起诉。

第八条 其他约定
本合同自双方签字（确认）之日起生效。

甲方（签章）：租赁平台    乙方（签字）：{{用户姓名}}
签约日期：{{签约日期}}',
'1.0', true, NOW(), NOW());

-- ================================
-- 8. 创建示例通知
-- ================================

INSERT INTO notifications (user_id, type, title, content, status, created_at) VALUES
(1, 'SYSTEM', '欢迎使用租赁系统', '欢迎您使用我们的租赁系统！系统已为您准备了丰富的物品资源，祝您使用愉快！', 'SENT', NOW()),
(2, 'SYSTEM', '管理员权限说明', '您已被分配为业务经理角色，拥有物品审核、订单管理等权限，请合理使用。', 'SENT', NOW()),
(3, 'SYSTEM', '财务系统使用指南', '作为财务人员，您可以查看和管理所有支付记录、生成财务报表等。', 'SENT', NOW());

-- ================================
-- 9. 初始化系统配置
-- ================================

-- 创建一些示例财务记录
INSERT INTO finance_records (record_no, type, category, amount, description, created_at) VALUES
('FIN001', 'INCOME', '系统初始化', 0.00, '系统初始化记录', NOW()),
('FIN002', 'EXPENSE', '系统维护', 0.00, '系统维护成本记录', NOW());

-- ================================
-- 10. 文件管理模块初始化数据
-- ================================

-- 初始化文件分类
INSERT INTO file_category (name, code, description, allowed_extensions, max_file_size, sort_order) VALUES
('用户头像', 'AVATAR', '用户头像图片', 'jpg,jpeg,png,gif', 5242880, 1),
('物品图片', 'ITEM_IMAGE', '物品展示图片', 'jpg,jpeg,png,gif', 10485760, 2),
('合同文件', 'CONTRACT', '合同相关文档', 'pdf,doc,docx', 20971520, 3),
('一般文档', 'DOCUMENT', '一般文档资料', 'pdf,doc,docx,xls,xlsx,txt', 20971520, 4);

-- 文件管理相关权限
INSERT INTO permissions (name, description, type, resource, sort_order, created_at) VALUES
('FILE_UPLOAD', '文件上传', 'API', '/api/files/upload', 80, NOW()),
('FILE_DOWNLOAD', '文件下载', 'API', '/api/files/download/**', 81, NOW()),
('FILE_MANAGE', '文件管理', 'API', '/api/files/**', 82, NOW()),
('FILE_DELETE', '文件删除', 'API', '/api/files/*/delete', 83, NOW()),
('FILE_VIEW_ALL', '查看所有文件', 'API', '/api/files/all', 84, NOW());

-- ================================
-- 11. 分配文件管理权限到角色
-- ================================

-- 管理员拥有所有文件权限（通过前面的所有权限分配已经包含）

-- 业务经理拥有文件管理权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 2, id FROM permissions
WHERE name IN (
    'FILE_UPLOAD', 'FILE_DOWNLOAD', 'FILE_MANAGE', 'FILE_DELETE', 'FILE_VIEW_ALL'
);

-- 操作员拥有基础文件权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, id FROM permissions
WHERE name IN (
    'FILE_UPLOAD', 'FILE_DOWNLOAD', 'FILE_MANAGE'
);

-- 财务人员拥有文件查看和下载权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 4, id FROM permissions
WHERE name IN (
    'FILE_DOWNLOAD', 'FILE_MANAGE'
);

-- 物品拥有者拥有文件上传、下载、管理权限（管理自己的文件）
INSERT INTO role_permissions (role_id, permission_id)
SELECT 5, id FROM permissions
WHERE name IN (
    'FILE_UPLOAD', 'FILE_DOWNLOAD', 'FILE_MANAGE', 'FILE_DELETE'
);

-- 客户拥有基础文件权限（上传和下载自己的文件）
INSERT INTO role_permissions (role_id, permission_id)
SELECT 6, id FROM permissions
WHERE name IN (
    'FILE_UPLOAD', 'FILE_DOWNLOAD'
);
