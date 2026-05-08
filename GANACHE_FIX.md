# Ganache升级指南

## 问题说明
当前使用的Ganache 2.7.1版本与Solidity 0.8.0不完全兼容，导致"invalid opcode"错误。

## 解决方案

### 方案1：使用Ganache CLI（推荐）

#### 步骤1：安装最新版Ganache CLI
```bash
npm install -g ganache
```

#### 步骤2：启动Ganache
```bash
ganache
```

#### 步骤3：在Remix中连接
1. 在Remix的"Deploy & Run Transactions"面板中
2. 在"Environment"下拉菜单中选择"Custom"
3. 输入RPC URL：`http://127.0.0.1:8545`（注意：CLI版本默认端口是8545）
4. 点击连接

### 方案2：使用Hardhat（现代化替代方案）

#### 步骤1：安装Hardhat
```bash
npm install --save-dev hardhat
```

#### 步骤2：初始化Hardhat项目
```bash
npx hardhat init
```

#### 步骤3：启动本地网络
```bash
npx hardhat node
```

#### 步骤4：在Remix中连接
1. 在Remix的"Deploy & Run Transactions"面板中
2. 在"Environment"下拉菜单中选择"Custom"
3. 输入RPC URL：`http://127.0.0.1:8545`
4. 点击连接

### 方案3：降级Solidity版本（不推荐）

如果必须使用旧版Ganache，需要修改智能合约：

#### 修改合约版本
将 `EnergyTradingPlatform.sol` 第一行改为：
```solidity
// SPDX-License-Identifier: MIT
pragma solidity ^0.7.0;
```

#### 注意事项
- ⚠️ 需要修改合约代码以兼容0.7.0语法
- ⚠️ 缺少Solidity 0.8.0的安全特性
- ⚠️ 不推荐用于生产环境

## 推荐方案对比

| 方案 | 优点 | 缺点 | 推荐度 |
|------|------|------|--------|
| Remix VM | 最简单，无需安装 | 仅用于测试 | ⭐⭐⭐⭐⭐ |
| Ganache CLI | 功能完整，支持0.8.0 | 需要Node.js | ⭐⭐⭐⭐ |
| Hardhat | 现代化，功能强大 | 学习曲线 | ⭐⭐⭐⭐ |
| 降级Solidity | 兼容旧版Ganache | 失去新特性 | ⭐⭐ |

## 快速测试步骤

### 使用Remix VM测试（推荐）
1. 打开 https://remix.ethereum.org/
2. 粘贴您的智能合约代码
3. 编译合约（选择0.8.0+版本）
4. 选择"Remix VM (Cancun)"环境
5. 点击Deploy

### 验证部署成功
部署成功后，您应该看到：
- 合约地址显示在底部
- "Deployed Contracts"部分出现合约函数
- 可以调用createOrder、buyEnergy等函数

## 下一步

部署成功后：
1. 测试合约的所有功能
2. 记录合约地址
3. 更新后端配置文件
4. 启动后端服务
5. 进行完整的端到端测试