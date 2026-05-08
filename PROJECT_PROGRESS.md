# 微电网P2P电力交易系统 - 项目开发进度文档

> **最后更新时间**: 2026-03-30
> **项目状态**: 基础功能已完成，交易溯源功能已添加

---

## 一、项目概述

### 1.1 项目名称
微电网分布式点对点（P2P）电力交易平台

### 1.2 项目目标
构建一个基于区块链的电力交易平台，支持产电用户和用电用户之间的电量交易，实现：
- 用户注册登录与角色管理
- 电量挂单发布与购买
- 区块链交易记录
- **交易溯源查证（新增）**
- 交易历史查询

### 1.3 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 前端 | Vue 3 | 3.3.4 |
| 前端框架 | Element Plus | 2.4.4 |
| 状态管理 | Pinia | 2.1.7 |
| 路由 | Vue Router | 4.2.5 |
| HTTP客户端 | Axios | 1.5.0 |
| 构建工具 | Vite | 4.4.5 |
| 后端 | Spring Boot | 3.2.0 |
| ORM | MyBatis | 3.0.3 |
| 数据库 | MySQL | 8.0 |
| 区块链交互 | Web3j | 4.10.3 |
| 区块链网络 | Ganache | 本地测试网络 |
| 智能合约 | Solidity | 0.8.0 |

---

## 二、项目结构

```
electricity trading system/
├── backend/                          # Spring Boot后端
│   ├── src/main/java/com/energytrading/
│   │   ├── common/                   # 通用类
│   │   │   └── Result.java           # 统一响应格式
│   │   ├── config/                   # 配置类
│   │   │   └── Web3jConfig.java      # Web3j区块链配置
│   │   ├── contract/                 # 智能合约Java包装
│   │   │   └── EnergyTradingPlatform.java
│   │   ├── controller/               # 控制器层
│   │   │   ├── UserController.java   # 用户接口
│   │   │   ├── OrderController.java  # 订单接口
│   │   │   ├── TradeController.java  # 交易接口
│   │   │   └── TraceController.java  # 溯源接口（新增）
│   │   ├── dto/                      # 数据传输对象
│   │   │   ├── UserLoginDTO.java
│   │   │   ├── UserRegisterDTO.java
│   │   │   ├── OrderCreateDTO.java
│   │   │   ├── TradeDTO.java
│   │   │   ├── TraceResultDTO.java   # 溯源结果（新增）
│   │   │   └── TracePathDTO.java     # 溯源路径（新增）
│   │   ├── entity/                   # 实体类
│   │   │   ├── User.java
│   │   │   ├── Order.java
│   │   │   └── Transaction.java
│   │   ├── mapper/                   # MyBatis Mapper接口
│   │   │   ├── UserMapper.java
│   │   │   ├── OrderMapper.java
│   │   │   └── TransactionMapper.java
│   │   ├── service/                  # 服务层
│   │   │   ├── UserService.java
│   │   │   ├── OrderService.java
│   │   │   ├── TradeService.java
│   │   │   └── TraceService.java     # 溯源服务（新增）
│   │   └── BackendApplication.java   # 启动类
│   ├── src/main/resources/
│   │   ├── mapper/                   # MyBatis XML映射文件
│   │   │   ├── UserMapper.xml
│   │   │   ├── OrderMapper.xml
│   │   │   └── TransactionMapper.xml
│   │   └── application.properties    # 配置文件
│   └── pom.xml                       # Maven依赖配置
│
├── frontend/                         # Vue 3前端
│   ├── src/
│   │   ├── router/                   # 路由配置
│   │   │   └── index.js
│   │   ├── stores/                   # Pinia状态管理
│   │   │   └── user.js
│   │   ├── styles/                   # 样式文件
│   │   │   └── main.css
│   │   ├── views/                    # 页面组件
│   │   │   ├── Login.vue             # 登录页
│   │   │   ├── Register.vue          # 注册页
│   │   │   ├── Dashboard.vue         # 主框架
│   │   │   ├── Market.vue            # 交易市场
│   │   │   ├── MyOrders.vue          # 我的挂单
│   │   │   ├── CreateOrder.vue       # 发布挂单
│   │   │   ├── History.vue           # 交易历史
│   │   │   ├── Trace.vue             # 交易溯源（新增）
│   │   │   └── Profile.vue           # 个人中心
│   │   ├── App.vue                   # 根组件
│   │   └── main.js                   # 入口文件
│   ├── index.html
│   ├── vite.config.js
│   └── package.json
│
├── contracts/                        # 智能合约
│   └── EnergyTradingPlatform.sol
│
├── database/                         # 数据库脚本
│   └── create_tables.sql
│
└── PROJECT_PROGRESS.md               # 本文档
```

---

## 三、数据库设计

### 3.1 用户表 (user)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR(100) | 密码（MD5加密） |
| blockchain_address | VARCHAR(100) | 区块链地址，唯一 |
| role | INT | 角色：0-普通用户，1-产电用户，2-用电用户 |
| balance | DOUBLE | 账户余额 |
| created_time | DATETIME | 创建时间 |
| updated_time | DATETIME | 更新时间 |

### 3.2 订单表 (energy_orders)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| order_id_on_chain | BIGINT | 链上订单ID |
| seller_address | VARCHAR(100) | 卖家区块链地址 |
| amount | DOUBLE | 电量(kWh) |
| price | BIGINT | 单价(Wei/kWh) |
| status | INT | 状态：0-开放中，1-已完成，2-已取消 |
| created_time | DATETIME | 创建时间 |
| updated_time | DATETIME | 更新时间 |

### 3.3 交易表 (transaction)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| tx_hash | VARCHAR(100) | 交易哈希，唯一 |
| order_id | BIGINT | 订单ID |
| buyer_address | VARCHAR(100) | 买家区块链地址 |
| seller_address | VARCHAR(100) | 卖家区块链地址 |
| amount | DOUBLE | 交易电量(kWh) |
| total_price | BIGINT | 总价(Wei) |
| timestamp | BIGINT | 交易时间戳 |

---

## 四、API接口设计

### 4.1 用户模块 `/api/user`

| 方法 | 路径 | 说明 | 请求体 |
|------|------|------|--------|
| POST | /register | 用户注册 | `{username, password, blockchainAddress, role}` |
| POST | /login | 用户登录 | `{username, password}` |
| GET | /{id} | 获取用户信息 | - |
| GET | /address/{address} | 根据区块链地址获取用户 | - |

### 4.2 订单模块 `/api/order`

| 方法 | 路径 | 说明 | 请求体 |
|------|------|------|--------|
| POST | /create | 创建挂单 | `{amount, price, sellerAddress}` |
| POST | /cancel/{id} | 取消挂单 | - |
| GET | /open | 获取开放挂单列表 | - |
| GET | /user/{address} | 获取用户挂单 | - |
| GET | /{id} | 获取订单详情 | - |
| GET | /all | 获取所有订单 | - |

### 4.3 交易模块 `/api/trade`

| 方法 | 路径 | 说明 | 请求体 |
|------|------|------|--------|
| POST | /buy | 购买电量 | `{orderId, amount}` + `?buyerAddress=xxx` |
| GET | /history/{address} | 获取用户交易历史 | - |
| GET | /all | 获取所有交易记录 | - |
| GET | /tx/{txHash} | 根据交易哈希获取详情 | - |

### 4.4 溯源模块 `/api/trace` （新增）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /verify/{txHash} | 验证交易真实性 |
| GET | /path/{txHash} | 获取溯源路径 |
| GET | /certificate/{txHash} | 生成交易凭证 |
| GET | /chain/{address} | 获取用户交易链 |

### 4.5 统一响应格式

```json
{
    "code": 200,
    "message": "success",
    "data": { ... }
}
```

---

## 五、智能合约功能

### 5.1 主要功能

| 功能 | 方法 | 说明 |
|------|------|------|
| 存款 | deposit() | 向合约存入ETH |
| 取款 | withdraw(amount) | 从合约取出ETH |
| 创建订单 | createOrder(amount, price) | 发布电量出售挂单 |
| 购买电量 | buyEnergy(orderId, amount) | 购买电量 |
| 取消订单 | cancelOrder(orderId) | 取消挂单 |
| 查询余额 | getBalance(address) | 查询可用/冻结余额 |

### 5.2 资金冻结机制
- 用户可存款到合约
- 购买时可使用冻结资金或直接转账
- 卖家收款自动到账

---

## 六、交易溯源功能（新增）

### 6.1 功能概述

交易溯源功能提供以下能力：

| 功能 | 说明 |
|------|------|
| 交易验证 | 验证交易是否真实存在于区块链 |
| 溯源路径 | 展示电力从生产到消费的完整流转路径 |
| 交易凭证 | 生成带数字签名的交易证明文件 |
| 交易链查询 | 查询用户的所有交易记录链 |

### 6.2 溯源路径节点

1. **电力生产** - 产电用户发布电量出售挂单
2. **挂单发布** - 电量挂单进入交易市场
3. **交易匹配** - 用电用户发起购买请求
4. **链上记录** - 交易数据写入区块链
5. **电量交割** - 电量从卖家转移至买家

### 6.3 交易凭证格式

```
========== 电力交易溯源凭证 ==========
凭证编号: 0xabc123def456789...
验证状态: 已上链验证
验证结果: 已验证
--------------------------------------
卖家地址: 0x1234...5678
买家地址: 0x8765...4321
交易电量: 20.00 kWh
订单编号: 1001
交易时间: 2026-03-30 16:30:00
区块高度: 12345
区块哈希: 0xdef456...
--------------------------------------
数字签名: ABCD1234EFGH5678...
生成时间: 2026-03-30 17:00:00
======================================
```

---

## 七、已完成功能

### 7.1 后端功能 ✅
- [x] Spring Boot项目搭建
- [x] MyBatis配置与动态SQL
- [x] 统一响应格式Result类
- [x] 用户注册登录（MD5密码加密）
- [x] 用户角色管理（产电/用电用户）
- [x] 订单CRUD操作
- [x] 交易流程实现
- [x] Web3j区块链集成
- [x] 跨域配置
- [x] **交易溯源服务（新增）**
- [x] **区块链交易验证（新增）**
- [x] **交易凭证生成（新增）**

### 7.2 前端功能 ✅
- [x] Vue 3 + Vite项目搭建
- [x] Vue Router路由配置
- [x] Pinia状态管理
- [x] Element Plus UI集成
- [x] 登录页面
- [x] 注册页面（含角色选择）
- [x] 交易市场页面
- [x] 我的挂单页面
- [x] 发布挂单页面
- [x] 交易历史页面
- [x] **交易溯源页面（新增）**
- [x] **溯源路径可视化（新增）**
- [x] 个人中心页面
- [x] Axios HTTP请求封装

### 7.3 数据库 ✅
- [x] 数据库创建
- [x] 表结构设计
- [x] 测试数据初始化

---

## 八、测试账户

| 用户名 | 密码 | 角色 | 区块链地址 |
|--------|------|------|------------|
| producer1 | 123456 | 产电用户 | 0x1234567890123456789012345678901234567890 |
| consumer1 | 123456 | 用电用户 | 0x0987654321098765432109876543210987654321 |
| demo | 123456 | 普通用户 | 0xabcdef0123456789abcdef0123456789abcdef01 |

---

## 九、启动步骤

### 9.1 启动MySQL
```bash
# 确保MySQL服务运行在3306端口
# 执行数据库脚本：database/create_tables.sql
```

### 9.2 启动Ganache（可选，用于区块链功能）
```bash
ganache -p 8545
```

### 9.3 启动后端
```bash
cd "electricity trading system/backend"
mvn spring-boot:run
# 后端运行在 http://localhost:8080
```

### 9.4 启动前端
```bash
cd "electricity trading system/frontend"
npm install  # 首次运行需要安装依赖
npm run dev
# 前端运行在 http://localhost:3000
```

---

## 十、配置文件

### 10.1 后端配置 (application.properties)
```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/energy_trading
spring.datasource.username=root
spring.datasource.password=123456

# MyBatis配置
mybatis.mapper-locations=classpath:mapper/*.xml
mybatis.type-aliases-package=com.energytrading.entity

# Web3j配置
web3j.url=http://127.0.0.1:8545
web3j.contract.address=0xf999bA376BFADA777303774980265Bc94335a806
web3j.private.key=0xacd355c2200677244c4dbd5da41b0dfa6309c0a65c766f752dd7e50d2720ba23
```

### 10.2 前端配置 (vite.config.js)
```javascript
export default defineConfig({
    plugins: [vue()],
    server: {
        port: 3000,
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true
            }
        }
    }
})
```

---

## 十一、待完成/优化事项

### 11.1 高优先级
- [ ] 完善区块链交易功能（当前使用模拟交易）
- [ ] 添加JWT Token认证
- [ ] 添加用户权限验证

### 11.2 中优先级
- [ ] 添加订单搜索功能
- [ ] 添加数据统计图表
- [ ] 优化前端错误处理
- [ ] 添加国际化支持

### 11.3 低优先级
- [ ] 添加用户头像上传
- [ ] 添加消息通知功能
- [ ] 添加交易评价功能
- [ ] 移动端适配优化

---

## 十二、已知问题

1. **Java版本兼容性**: 当前使用Java 25，与Lombok有兼容问题，已移除Lombok改用手动getter/setter
2. **区块链连接**: 如果Ganache未启动，系统会使用模拟交易哈希
3. **密码加密**: 当前使用简单MD5，生产环境建议使用BCrypt

---

## 十三、开发日志

### 2026-03-30
- 完成项目基础架构搭建
- 实现用户注册登录功能
- 实现角色选择功能（产电/用电用户）
- 实现订单发布与购买流程
- 实现交易历史查询
- 完成前端页面开发
- 解决Lombok兼容性问题
- 系统成功运行测试
- **新增交易溯源查证功能**
- **新增区块链交易验证**
- **新增交易凭证生成**
- **新增溯源路径可视化**

---

## 十四、联系方式

如有问题，请参考本文档重新了解项目结构。继续开发时：
1. 阅读本文档了解项目整体架构
2. 查看对应模块的代码实现
3. 根据待完成事项继续开发

---

*文档结束*
