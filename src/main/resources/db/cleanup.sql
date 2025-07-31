-- 数据库清理脚本
-- 用于解决表结构不一致和外键约束冲突问题

USE rentingdb;

-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 删除所有可能存在的表（包括旧的表）
DROP TABLE IF EXISTS rental_orders;
DROP TABLE IF EXISTS review_helpfulness;
DROP TABLE IF EXISTS review_replies;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS feedbacks;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS user_sessions;
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS user_profiles;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS payment_records;
DROP TABLE IF EXISTS merchant_payment_configs;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS finance_records;
DROP TABLE IF EXISTS finance_reports;
DROP TABLE IF EXISTS contracts;
DROP TABLE IF EXISTS contract_templates;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS item_categories;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;
