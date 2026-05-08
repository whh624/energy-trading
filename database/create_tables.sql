-- 创建数据库
CREATE DATABASE IF NOT EXISTS energy_trading DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE energy_trading;

-- 删除已存在的表
DROP TABLE IF EXISTS transaction;
DROP TABLE IF EXISTS energy_orders;
DROP TABLE IF EXISTS user;

-- 创建用户表
CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  username VARCHAR(50) NOT NULL COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT '密码(MD5加密)',
  blockchain_address VARCHAR(100) NOT NULL COMMENT '区块链地址',
  role INT DEFAULT 0 COMMENT '角色: 0-普通用户, 1-产电用户, 2-用电用户, 3-管理方',
  balance DOUBLE DEFAULT 1000.0 COMMENT '账户余额',
  status INT DEFAULT 0 COMMENT '状态: 0-正常, 1-冻结',
  trust_score INT DEFAULT 100 COMMENT '信用分',
  created_time DATETIME NOT NULL COMMENT '创建时间',
  updated_time DATETIME NOT NULL COMMENT '更新时间',
  UNIQUE KEY uk_username (username),
  UNIQUE KEY uk_blockchain_address (blockchain_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建订单表
CREATE TABLE energy_orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
  order_id_on_chain BIGINT NOT NULL COMMENT '链上订单ID',
  seller_address VARCHAR(100) NOT NULL COMMENT '卖家区块链地址',
  amount DOUBLE NOT NULL COMMENT '电量(kWh)',
  price BIGINT NOT NULL COMMENT '单价(Wei/kWh)',
  status INT NOT NULL DEFAULT 0 COMMENT '状态: 0-开放中, 1-已完成, 2-已取消',
  created_time DATETIME NOT NULL COMMENT '创建时间',
  updated_time DATETIME NOT NULL COMMENT '更新时间',
  INDEX idx_seller_address (seller_address),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电量订单表';

-- 创建交易表
CREATE TABLE transaction (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '交易ID',
  tx_hash VARCHAR(100) NOT NULL COMMENT '交易哈希',
  order_id BIGINT NOT NULL COMMENT '订单ID',
  buyer_address VARCHAR(100) NOT NULL COMMENT '买家区块链地址',
  seller_address VARCHAR(100) NOT NULL COMMENT '卖家区块链地址',
  amount DOUBLE NOT NULL COMMENT '交易电量(kWh)',
  total_price BIGINT NOT NULL COMMENT '总价(Wei)',
  timestamp BIGINT NOT NULL COMMENT '交易时间戳',
  UNIQUE KEY uk_tx_hash (tx_hash),
  INDEX idx_buyer_address (buyer_address),
  INDEX idx_seller_address (seller_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易记录表';

-- 插入测试数据
INSERT INTO user (username, password, blockchain_address, role, balance, status, trust_score, created_time, updated_time) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '0x1111111111111111111111111111111111111111', 3, 0.0, 0, 100, NOW(), NOW()),
('producer1', 'e10adc3949ba59abbe56e057f20f883e', '0x1234567890123456789012345678901234567890', 1, 10000.0, 0, 100, NOW(), NOW()),
('consumer1', 'e10adc3949ba59abbe56e057f20f883e', '0x0987654321098765432109876543210987654321', 2, 5000.0, 0, 100, NOW(), NOW()),
('demo', 'e10adc3949ba59abbe56e057f20f883e', '0xabcdef0123456789abcdef0123456789abcdef01', 0, 1000.0, 0, 100, NOW(), NOW());

-- 插入测试订单
INSERT INTO energy_orders (order_id_on_chain, seller_address, amount, price, status, created_time, updated_time) VALUES
(1001, '0x1234567890123456789012345678901234567890', 100.0, 50000000000000, 0, NOW(), NOW()),
(1002, '0x1234567890123456789012345678901234567890', 50.0, 45000000000000, 0, NOW(), NOW());

-- 插入测试交易记录
INSERT INTO transaction (tx_hash, order_id, buyer_address, seller_address, amount, total_price, timestamp) VALUES
('0xabc123def456789', 1001, '0x0987654321098765432109876543210987654321', '0x1234567890123456789012345678901234567890', 20.0, 1000000000000000, UNIX_TIMESTAMP());
