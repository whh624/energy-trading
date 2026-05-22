# P2P电力交易系统 - 项目总结报告 (算法与架构增强版)

## 📋 项目概述

**项目名称**：基于区块链的去中心化 P2P 电力交易与溯源系统
**核心定位**：解决微电网环境下，产消者（Prosumer）之间电能交易的信任、效率与物理流转透明度问题。
**技术突破**：在传统的区块链交易基础上，引入了 **PBFT 共识算法**、**PoC 贡献度模型**以及 **Dijkstra 调度算法**。

---

## �️ 核心技术架构 (分层设计)

1.  **物理感知层 (Physical Layer)**：
    *   利用 **Dijkstra 最短路径算法** 模拟真实电网拓扑。
    *   功能：计算电力从卖家到买家的损耗最小传输路径。
2.  **区块链共识层 (Consensus Layer)**：
    *   **PBFT 协议**：实现链上三阶段提交（Pre-prepare, Prepare, Commit），确保分布式节点对交易达成强一致性。
    *   **PoC 模型**：基于节点贡献度（产电量、绿电占比、信用分）分配投票权重，体现电力行业特色。
3.  **智能合约层 (Contract Layer)**：
    *   基于 Solidity 0.8.0 开发，负责资金冻结、电力资产交割与共识状态维护。
4.  **应用服务层 (Application Layer)**：
    *   Spring Boot + Web3j + Vue3 + Element Plus。

---

## 🎯 核心功能模块与技术亮点

### 1. 强一致性 PBFT 共识模块
*   **创新点**：将 PBFT 算法逻辑下沉至智能合约，实现了“真共识”而非“代码模拟”。
*   **流程**：交易必须通过 $2f+1$ 个验证节点的链上背书方可生效，有效防止了拜占庭节点的恶意篡改。

### 2. 电力调度图论算法
*   **技术实现**：在 `AlgorithmService` 中构建动态图模型。
*   **应用**：在“交易溯源”功能中，系统会根据电网实时拓扑，利用 Dijkstra 算法动态生成电力流转路径图（包含变电站、变压器等物理节点）。

### 3. 多目标市场撮合优化
*   **策略**：综合考虑价格、碳足迹指数与地理距离，利用加权评分模型（WSM）实现买卖双方的最优匹配。

---

## 🔧 技术配置汇总

| 配置项 | 详细信息 |
| :--- | :--- |
| **区块链平台** | Ganache (Local Blockchain) |
| **智能合约** | Solidity 0.8.0, PBFT State Machine |
| **共识算法** | PoC (Proof of Contribution) + PBFT |
| **后端框架** | Spring Boot 3.2, Web3j, JPA |
| **前端技术** | Vue 3, Vite, Element Plus, MetaMask |
| **图论算法** | Dijkstra (电力传输路径优化) |

---

## 📈 项目开发历程与问题解决

### 1. 从“存证”到“共识”的演进
*   **初期**：系统仅使用区块链做数据存证。
*   **后期**：重写了智能合约，引入了 PBFT 状态机，使每一笔交易都必须经过链上多节点投票，极大地提升了系统的技术深度。

### 2. 物理与逻辑的解耦
*   **挑战**：如何体现电力系统的物理特性？
*   **对策**：引入 Dijkstra 算法模拟电网拓扑，使虚拟的交易数据与物理的电力流转路径在“溯源模块”中得到了统一。

---

## 📊 毕业设计核心卖点

1.  **真实区块链交互**：完整集成了 MetaMask 钱包与链上三阶段共识。
2.  **多学科交叉**：结合了计算机分布式共识算法与电力系统图论分析。
3.  **完善的追溯体系**：从买卖双方的身份验证到物理传输路径的动态计算，实现了全流程闭环。

---

## � 关键代码分布

- **共识逻辑**：[ConsensusService.java](file:///d:/Trae-app/bishe2/electricity%20trading%20system/backend/src/main/java/com/energytrading/service/ConsensusService.java)
- **图论算法**：[AlgorithmService.java](file:///d:/Trae-app/bishe2/electricity%20trading%20system/backend/src/main/java/com/energytrading/service/AlgorithmService.java)
- **智能合约**：[EnergyTradingPlatform.sol](file:///d:/Trae-app/bishe2/electricity%20trading%20system/contracts/contracts/EnergyTradingPlatform.sol)
- **前端溯源**：[Trace.vue](file:///d:/Trae-app/bishe2/electricity%20trading%20system/frontend/src/views/Trace.vue)
