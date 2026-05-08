# 完整系统测试指南

## 📋 测试流程概览

### 阶段1：智能合约功能测试 ✅
### 阶段2：后端配置和启动
### 阶段3：前端界面测试
### 阶段4：完整系统集成测试

---

## 🎯 阶段1：智能合约功能测试（在Remix中）

### 1.1 验证部署状态
- [ ] 检查合约地址是否显示
- [ ] 确认所有函数可见
- [ ] 记录合约地址：`_________________`

### 1.2 测试创建订单
```solidity
// 函数：createOrder(uint256 _amount, uint256 _price)
// 测试参数：
_amount: 100 (kWh)
_price: 1000000000000000 (Wei, 约0.001 ETH)
```
- [ ] 调用createOrder成功
- [ ] 记录返回的订单ID：`_________________`
- [ ] 检查交易详情

### 1.3 测试查看开放订单
```solidity
// 函数：getOpenOrders()
// 预期：返回包含刚才创建订单ID的数组
```
- [ ] 调用getOpenOrders成功
- [ ] 验证返回的订单ID正确

### 1.4 测试查看订单详情
```solidity
// 函数：getOrder(uint256 _orderId)
// 参数：使用刚才创建的订单ID
```
- [ ] 调用getOrder成功
- [ ] 验证订单信息：
  - [ ] id正确
  - [ ] seller地址正确
  - [ ] amount = 100
  - [ ] price正确
  - [ ] status = 0 (OPEN)
  - [ ] createdAt有值

### 1.5 测试购买电力
```solidity
// 函数：buyEnergy(uint256 _orderId, uint256 _amount)
// 切换到不同账户后调用：
_orderId: 0
_amount: 50
Value: 50000000000000000 (50 * price)
```
- [ ] 切换到买家账户
- [ ] 调用buyEnergy成功
- [ ] 验证买家ETH减少
- [ ] 验证卖家ETH增加
- [ ] 再次查看订单，amount应该变为50

### 1.6 测试用户订单查询
```solidity
// 函数：getUserOrders(address _user)
// 参数：卖家地址
```
- [ ] 调用getUserOrders成功
- [ ] 验证返回的订单ID列表

### 1.7 测试取消订单
```solidity
// 函数：cancelOrder(uint256 _orderId)
// 1. 创建新订单
// 2. 调用cancelOrder
```
- [ ] 创建新订单成功
- [ ] 调用cancelOrder成功
- [ ] 验证订单状态变为CANCELLED

### 1.8 测试交易记录
```solidity
// 函数：getUserTransactions(address _user)
// 函数：getTransaction(uint256 _transactionId)
```
- [ ] 查询买家交易记录
- [ ] 查询卖家交易记录
- [ ] 验证交易详情正确

---

## 🔧 阶段2：后端配置和启动

### 2.1 更新application.properties

编辑 `backend/src/main/resources/application.properties`：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/energy_trading?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA配置
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Web3j配置 - 根据您的环境修改
# 如果使用Remix VM，这个配置暂时不需要
# 如果使用Ganache，配置如下：
web3j.url=http://localhost:7545
web3j.contract.address=YOUR_DEPLOYED_CONTRACT_ADDRESS
web3j.private.key=YOUR_PRIVATE_KEY
```

### 2.2 获取配置信息

#### 从Remix获取合约地址：
1. 在Remix底部找到部署的合约地址
2. 复制地址（格式：`0x...`）

#### 从Ganache获取私钥：
1. 打开Ganache GUI
2. 点击账户旁边的钥匙图标
3. 复制私钥

### 2.3 启动MySQL数据库

```bash
# Windows: 启动MySQL服务
# 或者确保MySQL正在运行
```

### 2.4 启动后端服务

```bash
cd backend
mvn spring-boot:run
```

### 2.5 验证后端启动

- [ ] 后端启动成功，无错误日志
- [ ] 数据库表自动创建
- [ ] API端点可访问（默认：http://localhost:8080）

---

## 🎨 阶段3：前端界面测试

### 3.1 启动前端开发服务器

```bash
cd frontend
npm install
npm run dev
```

### 3.2 访问前端界面

打开浏览器访问：`http://localhost:5173`

### 3.3 测试登录功能

- [ ] 打开登录页面
- [ ] 输入用户名和密码
- [ ] 点击登录按钮
- [ ] 验证登录成功

### 3.4 测试MetaMask连接

- [ ] 点击"连接钱包"按钮
- [ ] 在MetaMask中确认连接
- [ ] 验证钱包地址显示正确
- [ ] 验证ETH余额显示

### 3.5 测试市场界面

- [ ] 查看电力订单列表
- [ ] 验证订单信息显示正确
- [ ] 测试创建新订单
- [ ] 测试购买电力
- [ ] 测试取消订单

### 3.6 测试响应式设计

- [ ] 在不同屏幕尺寸下测试
- [ ] 验证布局自适应
- [ ] 验证移动端显示

---

## 🚀 阶段4：完整系统集成测试

### 4.1 端到端测试流程

#### 测试场景1：完整交易流程
1. 用户A登录并连接MetaMask
2. 用户A创建电力订单（100 kWh, 0.001 ETH/kWh）
3. 用户B登录并连接MetaMask
4. 用户B查看开放订单
5. 用户B购买50 kWh电力
6. 验证交易完成
7. 验证区块链记录
8. 验证数据库记录

#### 测试场景2：订单管理
1. 用户创建多个订单
2. 查看所有订单
3. 取消部分订单
4. 验证订单状态更新

#### 测试场景3：错误处理
1. 尝试购买超过订单数量的电力
2. 尝试取消已完成的订单
3. 尝试用不足的ETH购买
4. 验证错误提示正确

### 4.2 性能测试

- [ ] 测试多个并发订单
- [ ] 测试大量数据加载
- [ ] 验证响应时间

### 4.3 安全测试

- [ ] 验证用户权限
- [ ] 验证交易签名
- [ ] 验证数据加密

---

## 📊 测试检查清单

### 智能合约测试
- [ ] createOrder功能正常
- [ ] buyEnergy功能正常
- [ ] cancelOrder功能正常
- [ ] getOrder功能正常
- [ ] getOpenOrders功能正常
- [ ] getUserOrders功能正常
- [ ] getUserTransactions功能正常
- [ ] getTransaction功能正常

### 后端测试
- [ ] API端点可访问
- [ ] 数据库连接正常
- [ ] 区块链集成正常
- [ ] 错误处理正确

### 前端测试
- [ ] 界面显示正常
- [ ] 用户交互流畅
- [ ] MetaMask集成正常
- [ ] 响应式设计正常

### 系统集成测试
- [ ] 端到端流程正常
- [ ] 数据一致性正确
- [ ] 性能满足要求
- [ ] 安全性符合标准

---

## 🐛 常见问题和解决方案

### 问题1：后端启动失败
**解决方案：**
- 检查MySQL是否运行
- 验证数据库连接配置
- 检查端口是否被占用

### 问题2：前端无法连接后端
**解决方案：**
- 检查后端是否启动
- 验证API端点URL
- 检查CORS配置

### 问题3：MetaMask连接失败
**解决方案：**
- 确保MetaMask已安装
- 检查网络配置
- 验证账户是否解锁

### 问题4：区块链交易失败
**解决方案：**
- 检查账户ETH余额
- 验证合约地址正确
- 检查gas费用

---

## 📝 测试报告模板

### 测试环境
- 操作系统：_________________
- 浏览器：_________________
- Node.js版本：_________________
- Java版本：_________________
- MySQL版本：_________________

### 测试结果
- 智能合约测试：通过/失败
- 后端测试：通过/失败
- 前端测试：通过/失败
- 系统集成测试：通过/失败

### 发现的问题
1. _________________________
2. _________________________
3. _________________________

### 改进建议
1. _________________________
2. _________________________
3. _________________________

---

## 🎯 下一步行动

测试完成后：
1. 修复发现的问题
2. 优化性能
3. 完善文档
4. 准备部署

---

**祝测试顺利！** 🚀