# 基于区块链的分布式点对点(P2P)电力交易系统

## 项目概述

本项目是一个基于区块链技术的分布式点对点电力交易系统，旨在实现有太阳能发电能力的"产电用户"和普通"用电用户"之间的直接电力交易。系统采用前后端分离架构，使用Spring Boot作为后端，Vue3作为前端，MySQL作为数据库，Solidity智能合约作为区块链核心。

## 技术栈

- **后端**：Spring Boot 3.2.0, Web3j 4.10.3
- **前端**：Vue 3.3.4, Element Plus 2.3.12, Axios 1.5.0
- **数据库**：MySQL 8.0+
- **区块链**：Solidity 0.8.0+, Ganache (本地测试链)

## 项目结构

```
electricity trading system/
├── backend/             # Spring Boot后端项目
├── contracts/           # Solidity智能合约
├── database/            # 数据库初始化脚本
├── frontend/            # Vue3前端项目
└── README.md            # 项目说明文档
```

## 环境搭建步骤

### 1. 安装依赖

- **Node.js**：v16.0+
- **JDK**：17+
- **MySQL**：8.0+
- **Ganache**：最新版本
- **Truffle**（可选）：用于编译和部署智能合约

### 2. 区块链环境设置

1. 启动Ganache，创建一个新的工作区
2. 记录Ganache提供的RPC URL（通常为 http://localhost:7545）
3. 记录一个账户的私钥，用于部署智能合约和发起交易

### 3. 智能合约部署

1. 编译智能合约
   ```bash
   # 使用Truffle编译
   truffle compile
   
   # 或使用Remix IDE编译
   # 打开 https://remix.ethereum.org/
   # 上传 contracts/EnergyTradingPlatform.sol
   # 编译合约
   ```

2. 部署智能合约到Ganache
   ```bash
   # 使用Truffle部署
   truffle migrate --network development
   
   # 或使用Remix IDE部署
   # 连接到Ganache（环境 -> Web3 Provider）
   # 部署合约
   ```

3. 记录部署后的合约地址

### 4. 数据库设置

1. 启动MySQL服务
2. 执行数据库初始化脚本
   ```bash
   mysql -u root -p < database/init.sql
   ```
3. 修改数据库连接配置（backend/src/main/resources/application.properties）

### 5. 后端配置

1. 修改Web3j配置（backend/src/main/resources/application.properties）
   - `web3j.url`：Ganache的RPC URL
   - `web3j.contract.address`：部署后的合约地址
   - `web3j.private.key`：用于交易的账户私钥

2. 启动后端服务
   ```bash
   cd backend
   mvn spring-boot:run
   ```

### 6. 前端配置

1. 安装依赖
   ```bash
   cd frontend
   npm install
   ```

2. 启动前端服务
   ```bash
   npm run dev
   ```

3. 访问前端页面：http://localhost:3000

## 系统功能

### 1. 交易市场
- 展示所有开放状态的卖电订单
- 支持购买电量

### 2. 我的挂单
- 展示当前用户创建的所有订单
- 支持取消未成交的订单

### 3. 发布挂单
- 发布卖电订单，设置电量和单价

### 4. 交易历史
- 展示当前用户参与的所有交易记录

## 系统演示流程

### 1. 卖家发布订单
1. 登录系统（使用默认测试账户）
2. 点击"发布挂单"
3. 输入出售电量（如10 kWh）和单价（如1000 Wei/kWh）
4. 点击"发布挂单"按钮
5. 系统提示挂单发布成功，并显示订单ID

### 2. 买家购买电量
1. 点击"交易市场"
2. 找到刚才发布的订单
3. 点击"购买"按钮
4. 输入购买电量（如5 kWh）
5. 点击"确认购买"按钮
6. 系统提示购买成功

### 3. 查看交易历史
1. 点击"交易历史"
2. 查看刚才完成的交易记录

## 注意事项

1. 本系统使用Ganache本地测试链，所有交易均为模拟交易
2. 前端默认使用固定的测试地址，实际项目中应集成MetaMask等钱包
3. 智能合约中的电量单位为kWh，价格单位为Wei/kWh
4. 系统采用后端代签的方式发起交易，简化了前端实现

## 后续优化方向

1. 集成MetaMask钱包，实现前端直接签名交易
2. 增加用户认证和授权机制
3. 优化智能合约的Gas消耗
4. 增加实时数据监控和分析功能
5. 支持更多类型的能源交易（如风能、水能等）