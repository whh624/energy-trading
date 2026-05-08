# 智能合约部署指南

## 前置准备

### 1. 启动Ganache（可选）
如果您想使用本地区块链测试：

#### 使用Ganache GUI：
- 下载并安装Ganache：https://trufflesuite.com/ganache/
- 启动Ganache应用
- 默认RPC URL：http://127.0.0.1:7545

#### 使用Ganache CLI：
```bash
npm install -g ganache
ganache
```

### 2. 打开Remix IDE
访问：https://remix.ethereum.org/

## 部署步骤

### 步骤1：创建合约文件
1. 在左侧文件浏览器中，点击 `contracts` 文件夹
2. 创建新文件 `EnergyTradingPlatform.sol`
3. 将您的智能合约代码粘贴进去

### 步骤2：编译合约
1. 点击左侧的 **"Solidity Compiler"** 图标
2. 选择编译器版本：`0.8.0` 或更高版本
3. 点击 **"Compile EnergyTradingPlatform.sol"** 按钮
4. 确保编译成功，没有错误

### 步骤3：部署合约

#### 选项A：使用Remix VM（推荐用于测试）
1. 点击左侧的 **"Deploy & Run Transactions"** 图标
2. 在 **"Environment"** 下拉菜单中选择 **"Remix VM (Cancun)"**
3. 在 **"Contract"** 下拉菜单中选择 `EnergyTradingPlatform`
4. 点击 **"Deploy"** 按钮
5. 部署成功后，合约地址会显示在底部

#### 选项B：连接到Ganache
1. 点击左侧的 **"Deploy & Run Transactions"** 图标
2. 在 **"Environment"** 下拉菜单中选择 **"Custom"**
3. 输入RPC URL：`http://127.0.0.1:7545`
4. 在 **"Account"** 下拉菜单中选择一个账户
5. 在 **"Contract"** 下拉菜单中选择 `EnergyTradingPlatform`
6. 点击 **"Deploy"** 按钮

#### 选项C：使用MetaMask连接到测试网
1. 在MetaMask中添加Sepolia测试网
2. 获取测试ETH：https://sepoliafaucet.com/
3. 在Remix的 **"Environment"** 中选择 **"Injected Provider - MetaMask"**
4. 在 **"Contract"** 中选择 `EnergyTradingPlatform`
5. 点击 **"Deploy"** 按钮
6. 在MetaMask中确认交易

## 部署后配置

### 1. 记录合约地址
部署成功后，复制合约地址（格式：`0x...`）

### 2. 更新后端配置
编辑 `backend/src/main/resources/application.properties`：

```properties
# 区块链配置
blockchain.rpc-url=http://127.0.0.1:7545
blockchain.contract-address=YOUR_DEPLOYED_CONTRACT_ADDRESS
blockchain.private-key=YOUR_PRIVATE_KEY
```

### 3. 测试合约功能
在Remix的 **"Deployed Contracts"** 部分测试合约功能：
- `createOrder`: 创建电力订单
- `buyEnergy`: 购买电力
- `getOpenOrders`: 查看开放订单
- `getUserOrders`: 查看用户订单

## 常见问题

### Q: 找不到"Web3 Provider"选项？
A: 新版Remix使用"Environment"下拉菜单，选择"Custom"或"Injected Provider - MetaMask"

### Q: 编译失败怎么办？
A: 检查Solidity版本，确保与合约中的`pragma solidity ^0.8.0;`匹配

### Q: 部署时gas不足？
A: 确保账户有足够的ETH，在测试网可以使用faucet获取测试币

### Q: 如何连接到Sepolia测试网？
A: 
1. 在MetaMask中添加Sepolia网络
2. 获取测试ETH
3. 在Remix中选择"Injected Provider - MetaMask"

## 下一步

部署成功后：
1. 测试合约的所有功能
2. 更新后端配置文件
3. 启动后端服务
4. 测试前端界面
5. 进行完整的端到端测试