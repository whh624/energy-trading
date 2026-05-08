# 微电网P2P电力交易系统 - 启动指南

## 项目概述

本项目是一个基于区块链的分布式点对点（P2P）电力交易平台，支持产电用户和用电用户之间的电量交易。

## 技术栈

### 后端
- Spring Boot 3.2.0
- MyBatis 3.0.3
- MySQL 8.0
- Web3j 4.10.3（区块链交互）

### 前端
- Vue 3.3.4
- Vue Router 4.2.5
- Pinia 2.1.7
- Element Plus 2.4.4
- Axios 1.5.0
- Vite 4.4.5

### 区块链
- Ganache（本地以太坊测试网络）
- Solidity 0.8.0

## 系统角色

1. **产电用户（role=1）**：可以发布电量出售挂单
2. **用电用户（role=2）**：可以购买电量
3. **普通用户（role=0）**：基础用户角色

## 启动步骤

### 1. 启动MySQL数据库

```bash
# 使用Navicat或其他工具执行数据库脚本
# 脚本位置：database/create_tables.sql
```

### 2. 启动Ganache区块链

```bash
# 安装Ganache CLI（如果未安装）
npm install -g ganache

# 启动本地区块链网络
ganache -p 8545

# 或者使用Ganache GUI应用启动
```

### 3. 部署智能合约

```bash
# 进入contracts目录
cd contracts

# 使用Truffle或Remix部署合约
# 部署后更新application.properties中的合约地址
```

### 4. 启动后端服务

```bash
# 进入backend目录
cd backend

# 编译项目
mvn clean install

# 启动服务
mvn spring-boot:run

# 后端服务运行在 http://localhost:8080
```

### 5. 启动前端服务

```bash
# 进入frontend目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 前端服务运行在 http://localhost:3000
```

## 测试账户

系统预置了以下测试账户（密码均为：123456）：

| 用户名 | 角色 | 区块链地址 |
|--------|------|------------|
| producer1 | 产电用户 | 0x1234567890123456789012345678901234567890 |
| consumer1 | 用电用户 | 0x0987654321098765432109876543210987654321 |
| demo | 普通用户 | 0xabcdef0123456789abcdef0123456789abcdef01 |

## API接口

### 用户模块 `/api/user`
- `POST /register` - 用户注册
- `POST /login` - 用户登录
- `GET /{id}` - 获取用户信息
- `GET /address/{address}` - 根据区块链地址获取用户

### 订单模块 `/api/order`
- `POST /create` - 创建挂单
- `POST /cancel/{id}` - 取消挂单
- `GET /open` - 获取开放挂单列表
- `GET /user/{address}` - 获取用户挂单
- `GET /{id}` - 获取订单详情
- `GET /all` - 获取所有订单

### 交易模块 `/api/trade`
- `POST /buy` - 购买电量
- `GET /history/{address}` - 获取用户交易历史
- `GET /all` - 获取所有交易记录
- `GET /tx/{txHash}` - 根据交易哈希获取详情

## 功能特性

### 产电用户
- 发布电量出售挂单
- 管理自己的挂单（查看、取消）
- 查看交易历史
- 接收交易款项

### 用电用户
- 浏览开放的挂单
- 购买所需电量
- 查看交易历史
- 管理账户余额

### 区块链特性
- 交易记录不可篡改
- 智能合约自动执行
- 资金冻结与解冻机制
- 透明可追溯

## 项目结构

```
electricity trading system/
├── backend/                    # 后端项目
│   ├── src/main/java/
│   │   └── com/energytrading/
│   │       ├── common/         # 通用类（统一响应）
│   │       ├── config/         # 配置类
│   │       ├── controller/     # 控制器
│   │       ├── dto/            # 数据传输对象
│   │       ├── entity/         # 实体类
│   │       ├── mapper/         # MyBatis Mapper
│   │       └── service/        # 服务层
│   └── src/main/resources/
│       ├── mapper/             # MyBatis XML
│       └── application.properties
├── frontend/                   # 前端项目
│   └── src/
│       ├── router/             # 路由配置
│       ├── stores/             # Pinia状态管理
│       ├── styles/             # 样式文件
│       └── views/              # 页面组件
├── contracts/                  # 智能合约
│   └── EnergyTradingPlatform.sol
└── database/                   # 数据库脚本
    └── create_tables.sql
```

## 注意事项

1. 确保MySQL服务已启动，且数据库配置正确
2. 确保Ganache已启动，端口为8545
3. 首次运行需要执行数据库初始化脚本
4. 智能合约部署后需要更新配置文件中的合约地址
5. 测试账户密码为MD5加密存储

## 开发环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+
- Ganache
