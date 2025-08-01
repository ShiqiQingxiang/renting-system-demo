-- 租赁系统数据库初始化脚本
-- 注意：数据库 rentingdb 应该已经存在，这里只创建表结构

-- ================================
-- 1. 用户相关表
-- ================================

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    status ENUM('ACTIVE', 'INACTIVE', 'LOCKED') DEFAULT 'ACTIVE' COMMENT '用户状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户资料表
CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    real_name VARCHAR(100) COMMENT '真实姓名',
    id_card VARCHAR(18) COMMENT '身份证号',
    address TEXT COMMENT '地址',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    birth_date DATE COMMENT '出生日期',
    gender ENUM('MALE', 'FEMALE', 'OTHER') COMMENT '性别',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_id_card (id_card)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资料表';

-- ================================
-- 2. 权限相关表
-- ================================

-- 权限表
CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL COMMENT '权限名称',
    description VARCHAR(255) COMMENT '权限描述',
    type ENUM('MENU', 'BUTTON', 'API') NOT NULL COMMENT '权限类型',
    resource VARCHAR(255) COMMENT '资源路径',
    parent_id BIGINT COMMENT '父权限ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (parent_id) REFERENCES permissions(id) ON DELETE SET NULL,
    INDEX idx_name (name),
    INDEX idx_type (type),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色表
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '角色描述',
    is_system BOOLEAN DEFAULT FALSE COMMENT '是否系统角色',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS user_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ================================
-- 3. 认证相关表
-- ================================

-- 用户会话表
CREATE TABLE IF NOT EXISTS user_sessions (
    session_id VARCHAR(64) PRIMARY KEY COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    device_info VARCHAR(255) COMMENT '设备信息',
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    last_access_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后访问时间',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    active BOOLEAN DEFAULT TRUE COMMENT '是否活跃',
    expires_at TIMESTAMP COMMENT '过期时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会话表';

-- 刷新令牌表
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    token VARCHAR(255) NOT NULL UNIQUE COMMENT '刷新令牌',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    expiry_date TIMESTAMP NOT NULL COMMENT '过期时间',
    revoked BOOLEAN DEFAULT FALSE COMMENT '是否撤销',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_token (token),
    INDEX idx_expiry_date (expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='刷新令牌表';

-- ================================
-- 4. 物品相关表
-- ================================

-- 物品分类表
CREATE TABLE IF NOT EXISTS item_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    description TEXT COMMENT '分类描述',
    parent_id BIGINT COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (parent_id) REFERENCES item_categories(id) ON DELETE SET NULL,
    INDEX idx_parent_id (parent_id),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品分类表';

-- 物品表
CREATE TABLE IF NOT EXISTS items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '物品名称',
    description TEXT COMMENT '物品描述',
    category_id BIGINT COMMENT '分类ID',
    owner_id BIGINT NOT NULL COMMENT '物品所有者ID',
    price_per_day DECIMAL(10, 2) NOT NULL COMMENT '每日租金',
    deposit DECIMAL(10, 2) DEFAULT 0 COMMENT '押金',
    status ENUM('PENDING', 'AVAILABLE', 'RENTED', 'MAINTENANCE', 'UNAVAILABLE', 'OFFLINE') DEFAULT 'PENDING' COMMENT '物品状态',
    location VARCHAR(255) COMMENT '物品位置',
    images TEXT COMMENT '物品图片',
    brand VARCHAR(100) COMMENT '品牌',
    model VARCHAR(100) COMMENT '型号',
    color VARCHAR(50) COMMENT '颜色',
    size VARCHAR(100) COMMENT '尺寸',
    weight VARCHAR(50) COMMENT '重量',
    material VARCHAR(100) COMMENT '材质',
    item_condition VARCHAR(100) COMMENT '成色',
    features TEXT COMMENT '特性描述',
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' COMMENT '审核状态',
    approval_comment TEXT COMMENT '审核评论',
    approved_by BIGINT COMMENT '审核人ID',
    approved_at TIMESTAMP NULL COMMENT '审核时间',
    average_rating DECIMAL(3, 2) DEFAULT 0.00 COMMENT '平均评分',
    rating_count INT DEFAULT 0 COMMENT '评价数量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (category_id) REFERENCES item_categories(id) ON DELETE SET NULL,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_name (name),
    INDEX idx_category_id (category_id),
    INDEX idx_owner_id (owner_id),
    INDEX idx_status (status),
    INDEX idx_approval_status (approval_status),
    INDEX idx_price (price_per_day),
    INDEX idx_average_rating (average_rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品表';

-- ================================
-- 5. 订单相关表
-- ================================

-- 订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) UNIQUE NOT NULL COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(12, 2) NOT NULL COMMENT '总金额',
    deposit_amount DECIMAL(12, 2) DEFAULT 0 COMMENT '押金金额',
    status ENUM('PENDING', 'CONFIRMED', 'PAID', 'IN_USE', 'RETURNED', 'CANCELLED') DEFAULT 'PENDING' COMMENT '订单状态',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    actual_return_date DATE COMMENT '实际归还日期',
    remark TEXT COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单项表
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    item_id BIGINT NOT NULL COMMENT '物品ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    price_per_day DECIMAL(10, 2) NOT NULL COMMENT '每日价格',
    total_amount DECIMAL(12, 2) NOT NULL COMMENT '总金额',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE RESTRICT,
    INDEX idx_order_id (order_id),
    INDEX idx_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单项表';

-- ================================
-- 6. 支付相关表
-- ================================

-- 支付表
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_no VARCHAR(64) UNIQUE NOT NULL COMMENT '支付编号',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    merchant_id BIGINT NOT NULL COMMENT '商家ID（对应users表中的商家用户）',
    amount DECIMAL(12, 2) NOT NULL COMMENT '支付金额',
    payment_method ENUM('ALIPAY', 'WECHAT', 'CASH', 'BANK_TRANSFER') NOT NULL COMMENT '支付方式',
    payment_type ENUM('RENTAL', 'DEPOSIT', 'REFUND') NOT NULL COMMENT '支付类型',
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED') DEFAULT 'PENDING' COMMENT '支付状态',
    third_party_transaction_id VARCHAR(255) COMMENT '第三方交易ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE RESTRICT,
    FOREIGN KEY (merchant_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_payment_no (payment_no),
    INDEX idx_order_id (order_id),
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_merchant_status (merchant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付表';

-- 支付记录表
CREATE TABLE IF NOT EXISTS payment_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT NOT NULL COMMENT '支付ID',
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED') NOT NULL COMMENT '支付状态',
    response_data JSON COMMENT '响应数据',
    error_message TEXT COMMENT '错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE,
    INDEX idx_payment_id (payment_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- 商家支付配置表
CREATE TABLE IF NOT EXISTS merchant_payment_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL COMMENT '商家ID（对应users表中的商家用户）',
    alipay_app_id VARCHAR(32) NOT NULL COMMENT '支付宝应用ID',
    encrypted_private_key TEXT NOT NULL COMMENT '加密存储的商家私钥',
    alipay_public_key TEXT NOT NULL COMMENT '支付宝公钥',
    alipay_account VARCHAR(100) NOT NULL COMMENT '支付宝收款账户',
    notify_url VARCHAR(255) NOT NULL COMMENT '异步通知地址',
    return_url VARCHAR(255) NOT NULL COMMENT '同步返回地址',
    status ENUM('ACTIVE', 'INACTIVE', 'PENDING_REVIEW') DEFAULT 'PENDING_REVIEW' COMMENT '配置状态',
    remark VARCHAR(500) COMMENT '备注信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 外键约束
    FOREIGN KEY (merchant_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- 索引
    UNIQUE KEY uk_merchant_id (merchant_id) COMMENT '商家ID唯一索引',
    UNIQUE KEY uk_alipay_app_id (alipay_app_id) COMMENT '支付宝应用ID唯一索引',
    INDEX idx_status (status) COMMENT '状态索引',
    INDEX idx_created_at (created_at) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家支付配置表';

-- ================================
-- 7. 合同相关表
-- ================================

-- 合同模板表
CREATE TABLE IF NOT EXISTS contract_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '模板名称',
    content TEXT NOT NULL COMMENT '模板内容',
    version VARCHAR(20) DEFAULT '1.0' COMMENT '版本号',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同模板表';

-- 合同表
CREATE TABLE IF NOT EXISTS contracts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_no VARCHAR(64) UNIQUE NOT NULL COMMENT '合同编号',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    template_id BIGINT COMMENT '模板ID',
    content TEXT NOT NULL COMMENT '合同内容',
    status ENUM('DRAFT', 'SIGNED', 'EXPIRED', 'TERMINATED') DEFAULT 'DRAFT' COMMENT '合同状态',
    signed_at TIMESTAMP NULL COMMENT '签署时间',
    expires_at TIMESTAMP NULL COMMENT '过期时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE RESTRICT,
    FOREIGN KEY (template_id) REFERENCES contract_templates(id) ON DELETE SET NULL,
    INDEX idx_contract_no (contract_no),
    INDEX idx_order_id (order_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同表';

-- ================================
-- 8. 财务相关表
-- ================================

-- 财务记录表
CREATE TABLE IF NOT EXISTS finance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_no VARCHAR(64) UNIQUE NOT NULL COMMENT '记录编号',
    order_id BIGINT COMMENT '订单ID',
    payment_id BIGINT COMMENT '支付ID',
    type ENUM('INCOME', 'EXPENSE', 'REFUND') NOT NULL COMMENT '记录类型',
    category VARCHAR(100) NOT NULL COMMENT '财务分类',
    amount DECIMAL(12, 2) NOT NULL COMMENT '金额',
    description TEXT COMMENT '描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL,
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE SET NULL,
    INDEX idx_record_no (record_no),
    INDEX idx_type (type),
    INDEX idx_category (category),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务记录表';

-- 财务报表表
CREATE TABLE IF NOT EXISTS finance_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_type ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY') NOT NULL COMMENT '报表类型',
    period_start DATE NOT NULL COMMENT '期间开始',
    period_end DATE NOT NULL COMMENT '期间结束',
    total_income DECIMAL(15, 2) DEFAULT 0 COMMENT '总收入',
    total_expense DECIMAL(15, 2) DEFAULT 0 COMMENT '总支出',
    net_profit DECIMAL(15, 2) DEFAULT 0 COMMENT '净利润',
    report_data JSON COMMENT '报表数据',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_report_type (report_type),
    INDEX idx_period (period_start, period_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务报表表';

-- ================================
-- 9. 评价与反馈相关表
-- ================================

-- 评价表
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_no VARCHAR(64) UNIQUE NOT NULL COMMENT '评价编号',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    item_id BIGINT NOT NULL COMMENT '物品ID',
    reviewer_id BIGINT NOT NULL COMMENT '评价者ID',
    owner_id BIGINT NOT NULL COMMENT '物品拥有者ID',
    rating INT NOT NULL COMMENT '评分(1-5)',
    quality_rating INT COMMENT '物品质量评分(1-5)',
    service_rating INT COMMENT '服务评分(1-5)',
    delivery_rating INT COMMENT '配送评分(1-5)',
    title VARCHAR(200) COMMENT '评价标题',
    content TEXT COMMENT '评价内容',
    images TEXT COMMENT '评价图片',
    review_type ENUM('ORDER', 'ITEM', 'SERVICE') DEFAULT 'ORDER' COMMENT '评价类型',
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'HIDDEN') DEFAULT 'PENDING' COMMENT '审核状态',
    is_anonymous BOOLEAN DEFAULT FALSE COMMENT '是否匿名',
    helpful_count INT DEFAULT 0 COMMENT '有用数',
    reply_count INT DEFAULT 0 COMMENT '回复数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE RESTRICT,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE RESTRICT,
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_review_no (review_no),
    INDEX idx_order_id (order_id),
    INDEX idx_item_id (item_id),
    INDEX idx_reviewer_id (reviewer_id),
    INDEX idx_owner_id (owner_id),
    INDEX idx_rating (rating),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 评价回复表
CREATE TABLE IF NOT EXISTS review_replies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_id BIGINT NOT NULL COMMENT '评价ID',
    replier_id BIGINT NOT NULL COMMENT '回复者ID',
    content TEXT NOT NULL COMMENT '回复内容',
    reply_type ENUM('OWNER', 'ADMIN', 'CUSTOMER_SERVICE') DEFAULT 'OWNER' COMMENT '回复类型',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE,
    FOREIGN KEY (replier_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_review_id (review_id),
    INDEX idx_replier_id (replier_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价回复表';

-- 反馈表
CREATE TABLE IF NOT EXISTS feedbacks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    feedback_no VARCHAR(64) UNIQUE NOT NULL COMMENT '反馈编号',
    user_id BIGINT NOT NULL COMMENT '反馈用户ID',
    type ENUM('COMPLAINT', 'SUGGESTION', 'BUG_REPORT', 'FEATURE_REQUEST') NOT NULL COMMENT '反馈类型',
    category VARCHAR(100) COMMENT '反馈分类',
    title VARCHAR(200) NOT NULL COMMENT '反馈标题',
    content TEXT NOT NULL COMMENT '反馈内容',
    attachments TEXT COMMENT '附件',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM' COMMENT '优先级',
    status ENUM('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') DEFAULT 'OPEN' COMMENT '处理状态',
    assigned_to BIGINT COMMENT '分配给客服ID',
    resolution TEXT COMMENT '处理结果',
    resolved_at TIMESTAMP NULL COMMENT '解决时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_feedback_no (feedback_no),
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_assigned_to (assigned_to),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='反馈表';

-- 评价有用性表
CREATE TABLE IF NOT EXISTS review_helpfulness (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_id BIGINT NOT NULL COMMENT '评价ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    is_helpful BOOLEAN NOT NULL COMMENT '是否有用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_review_user (review_id, user_id),
    INDEX idx_review_id (review_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价有用性表';

-- ================================
-- 10. 通知相关表
-- ================================

-- 通知表
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type ENUM('EMAIL', 'SMS', 'SYSTEM') NOT NULL COMMENT '通知类型',
    title VARCHAR(255) NOT NULL COMMENT '通知标题',
    content TEXT NOT NULL COMMENT '通知内容',
    status ENUM('PENDING', 'SENT', 'FAILED', 'READ') DEFAULT 'PENDING' COMMENT '通知状态',
    sent_at TIMESTAMP NULL COMMENT '发送时间',
    read_at TIMESTAMP NULL COMMENT '阅读时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- ================================
-- 11. 索引优化和触发器
-- ================================

-- 创建全文搜索索引
ALTER TABLE items ADD FULLTEXT(name, description, features);
ALTER TABLE reviews ADD FULLTEXT(title, content);
ALTER TABLE feedbacks ADD FULLTEXT(title, content);

-- ================================
-- 12. 文件管理相关表
-- ================================

-- 文件分类表
CREATE TABLE IF NOT EXISTS file_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    code VARCHAR(20) NOT NULL UNIQUE COMMENT '分类代码',
    description TEXT COMMENT '分类描述',
    allowed_extensions TEXT COMMENT '允许的扩展名(逗号分隔)',
    max_file_size BIGINT DEFAULT 10485760 COMMENT '最大文件大小(字节)',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_code (code),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件分类表';

-- 文件信息表
CREATE TABLE IF NOT EXISTS file_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_name VARCHAR(255) NOT NULL COMMENT '存储文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT NOT NULL COMMENT '文件大小(字节)',
    content_type VARCHAR(100) NOT NULL COMMENT '文件类型',
    file_extension VARCHAR(10) COMMENT '文件扩展名',
    file_hash VARCHAR(64) COMMENT '文件哈希值(MD5)',
    category_id BIGINT COMMENT '分类ID',
    uploader_id BIGINT NOT NULL COMMENT '上传者ID',
    related_entity_type VARCHAR(50) COMMENT '关联实体类型(ITEM/USER/CONTRACT等)',
    related_entity_id BIGINT COMMENT '关联实体ID',
    access_level VARCHAR(20) DEFAULT 'PRIVATE' COMMENT '访问级别(PUBLIC/PRIVATE)',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否有效',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (category_id) REFERENCES file_category(id),
    FOREIGN KEY (uploader_id) REFERENCES users(id),
    INDEX idx_uploader (uploader_id),
    INDEX idx_category (category_id),
    INDEX idx_hash (file_hash),
    INDEX idx_entity (related_entity_type, related_entity_id),
    INDEX idx_path (file_path),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';
