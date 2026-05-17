-- 创建数据库
CREATE DATABASE IF NOT EXISTS energy_trading CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE energy_trading;

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  `blockchain_address` VARCHAR(100) NOT NULL,
  `created_time` DATETIME NOT NULL,
  UNIQUE KEY `uk_blockchain_address` (`blockchain_address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建订单表
CREATE TABLE IF NOT EXISTS `order` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `order_id_on_chain` BIGINT NOT NULL,
  `seller_address` VARCHAR(100) NOT NULL,
  `amount` DOUBLE NOT NULL,
  `price` BIGINT NOT NULL,
  `status` INT NOT NULL COMMENT '0-开放, 1-成交, 2-取消',
  `created_time` DATETIME NOT NULL,
  `updated_time` DATETIME NOT NULL,
  UNIQUE KEY `uk_order_id_on_chain` (`order_id_on_chain`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建交易表
CREATE TABLE IF NOT EXISTS `transaction` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `tx_hash` VARCHAR(100) NOT NULL,
  `order_id` BIGINT NOT NULL,
  `buyer_address` VARCHAR(100) NOT NULL,
  `seller_address` VARCHAR(100) NOT NULL,
  `amount` DOUBLE NOT NULL,
  `total_price` BIGINT NOT NULL,
  `timestamp` BIGINT NOT NULL,
  UNIQUE KEY `uk_tx_hash` (`tx_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
  `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) NOT NULL COMMENT '操作人姓名',
  `target_id` BIGINT NOT NULL COMMENT '被操作对象ID',
  `target_name` VARCHAR(50) NOT NULL COMMENT '被操作对象名称',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型',
  `operation_detail` TEXT COMMENT '详细描述',
  `created_time` DATETIME NOT NULL COMMENT '操作时间',
  INDEX `idx_operator_id` (`operator_id`),
  INDEX `idx_target_id` (`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作日志表';
