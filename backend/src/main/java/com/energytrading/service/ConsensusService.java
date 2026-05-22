package com.energytrading.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 共识算法服务
 * 模拟 PBFT (Practical Byzantine Fault Tolerance) 实用拜占庭容错算法
 */
@Service
public class ConsensusService {

    private static final int TOTAL_NODES = 4; // 模拟 4 个验证节点
    private static final int MAX_FAULTY_NODES = (TOTAL_NODES - 1) / 3; // 容错数 f = 1

    /**
     * 模拟执行 PBFT 共识过程
     * 包含三个阶段：Pre-prepare, Prepare, Commit
     */
    public boolean runPBFTConsensus(String transactionData) {
        System.out.println("=== 启动 PBFT 共识流程 ===");
        System.out.println("交易数据摘要: " + transactionData.substring(0, Math.min(20, transactionData.length())) + "...");

        try {
            // 1. Pre-prepare 阶段 (主节点广播)
            System.out.println("[Phase 1: Pre-prepare] 主节点 Node 0 发起提案...");
            TimeUnit.MILLISECONDS.sleep(300);

            // 2. Prepare 阶段 (节点间广播并验证)
            System.out.println("[Phase 2: Prepare] 各验证节点进行交叉验证...");
            int prepareVotes = simulateNodeVoting("Prepare");
            if (prepareVotes < (2 * MAX_FAULTY_NODES + 1)) {
                System.out.println("Prepare 阶段共识失败，投票数: " + prepareVotes);
                return false;
            }
            System.out.println("Prepare 阶段共识达成，获得投票: " + prepareVotes);
            TimeUnit.MILLISECONDS.sleep(300);

            // 3. Commit 阶段 (确认提交)
            System.out.println("[Phase 3: Commit] 节点进入确认提交状态...");
            int commitVotes = simulateNodeVoting("Commit");
            if (commitVotes < (2 * MAX_FAULTY_NODES + 1)) {
                System.out.println("Commit 阶段共识失败，投票数: " + commitVotes);
                return false;
            }
            System.out.println("Commit 阶段共识达成，交易准备上链。");
            
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 模拟节点投票行为
     */
    private int simulateNodeVoting(String phase) {
        int votes = 0;
        Random random = new Random();
        for (int i = 0; i < TOTAL_NODES; i++) {
            // 模拟 90% 的概率节点是正常的并投赞成票
            if (random.nextDouble() > 0.1) {
                votes++;
                // System.out.println("  > 节点 Node " + i + " 已完成 " + phase);
            } else {
                System.out.println("  > 节点 Node " + i + " 响应超时或由于拜占庭故障拒绝投票");
            }
        }
        return votes;
    }

    /**
     * 异步获取共识状态日志（用于前端展示）
     */
    public List<String> getConsensusLogs(String txId) {
        List<String> logs = new ArrayList<>();
        logs.add("PBFT 共识启动 - 节点总数: 4, 容错上限: 1");
        logs.add("阶段 1: Pre-prepare 完成 - 主节点已广播提案");
        logs.add("阶段 2: Prepare 完成 - 收到 3 个验证节点的确认信息");
        logs.add("阶段 3: Commit 完成 - 节点达成一致状态");
        logs.add("共识结论: 交易合法，允许上链");
        return logs;
    }
}
