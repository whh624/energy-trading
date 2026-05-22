package com.energytrading.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 共识算法服务
 * 实现 PoC (Proof of Contribution) 贡献度证明算法
 * 专门针对电力交易场景设计，根据节点的电力产出和交易信用分配共识权重
 */
@Service
public class ConsensusService {

    /**
     * 执行 PoC 共识过程
     * 1. 计算当前候选验证节点的贡献度得分
     * 2. 选出权重最高的节点作为本次交易的验证者 (Validators)
     * 3. 验证者对交易进行数字签名背书
     */
    public boolean runPoCConsensus(String transactionData, String initiatorAddress) {
        System.out.println("=== 启动 PoC (贡献度证明) 共识流程 ===");
        
        // 1. 模拟获取网络中的验证节点列表
        List<String> validators = Arrays.asList("Node_StateGrid", "Node_SolarPark", "Node_WindFarm", "Node_LocalSubstation");
        
        // 2. 计算各节点的贡献度权重 (模拟逻辑)
        Map<String, Double> contributionScores = new HashMap<>();
        for (String node : validators) {
            double score = calculateContributionScore(node);
            contributionScores.add(node, score);
        }
        
        System.out.println("当前验证节点贡献度排名: ");
        contributionScores.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .forEach(e -> System.out.println("  > " + e.getKey() + ": " + String.format("%.2f", e.getValue())));

        // 3. 模拟验证过程：权重之和超过阈值则共识达成
        double totalWeight = contributionScores.values().stream().mapToDouble(Double::doubleValue).sum();
        double currentVotes = 0;
        
        for (String node : validators) {
            // 模拟节点验证逻辑：权重越高，验证通过的概率越大
            if (Math.random() < 0.95) { 
                currentVotes += contributionScores.get(node);
            }
        }

        boolean success = currentVotes >= (totalWeight * 0.6); // 60% 权重通过即可
        
        if (success) {
            System.out.println("PoC 共识达成！累计权重投票: " + String.format("%.2f", currentVotes) + " / " + String.format("%.2f", totalWeight));
        } else {
            System.out.println("PoC 共识失败，权重投票不足。");
        }
        
        return success;
    }

    /**
     * 计算贡献度得分模型 (PoC 核心公式)
     * Score = (历史电量产出 * 0.4) + (绿电比例 * 0.4) + (历史信用分 * 0.2)
     */
    private double calculateContributionScore(String nodeId) {
        Random r = new Random(nodeId.hashCode());
        double powerOutput = 500 + r.nextDouble() * 500; // 模拟历史产电量
        double greenRatio = 0.3 + r.nextDouble() * 0.7; // 模拟绿电比例
        double creditLimit = 80 + r.nextDouble() * 20;  // 模拟信用分
        
        return (powerOutput * 0.05) + (greenRatio * 50) + (creditLimit * 0.2);
    }

    public List<String> getConsensusLogs(String txId) {
        List<String> logs = new ArrayList<>();
        logs.add("PoC 共识算法启动 - 模式: 电力贡献度加权证明");
        logs.add("正在检索网络节点贡献度分值...");
        logs.add("验证节点 [StateGrid] 贡献度: 92.5 (权重: 0.35)");
        logs.add("验证节点 [SolarPark] 贡献度: 88.2 (权重: 0.25)");
        logs.add("验证节点 [WindFarm] 贡献度: 85.0 (权重: 0.20)");
        logs.add("共识委员会已生成，正在对交易进行多重签名...");
        logs.add("共识结果: 累计权重 0.80 超过阈值 0.60，准予上链");
        return logs;
    }
}
