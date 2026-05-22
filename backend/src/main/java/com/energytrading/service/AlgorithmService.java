package com.energytrading.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 复杂算法服务类
 * 包含电力系统中常用的图论算法、优化匹配算法以及动态定价模型
 */
@Service
public class AlgorithmService {

    /**
     * 电力调度图节点类
     */
    public static class GridNode {
        public String id;
        public String name;
        public String type; // producer, transformer, substation, consumer

        public GridNode(String id, String name, String type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }
    }

    /**
     * 电力线路类（边）
     */
    public static class PowerLine {
        public String from;
        public String to;
        public double lossRate; // 线路损耗率
        public double capacity; // 线路容量

        public PowerLine(String from, String to, double lossRate, double capacity) {
            this.from = from;
            this.to = to;
            this.lossRate = lossRate;
            this.capacity = capacity;
        }
    }

    /**
     * 算法 1: 基于 Dijkstra 的最优电力传输路径规划
     * 在复杂的电网拓扑中，寻找损耗最小的传输路径
     */
    public List<GridNode> calculateOptimalPath(String sellerAddr, String buyerAddr, String txHash) {
        // 1. 构建模拟电网拓扑（实际应用中应从数据库或 GIS 系统获取）
        // 我们根据 txHash 确定性地生成一些中间节点，模拟复杂的电网
        Map<String, GridNode> allNodes = new HashMap<>();
        List<PowerLine> lines = new ArrayList<>();
        
        // 核心节点
        allNodes.put("S", new GridNode(sellerAddr, "发电机组", "producer"));
        allNodes.put("B", new GridNode(buyerAddr, "用户终端", "consumer"));
        
        // 中间节点：变电站、配电变压器等
        allNodes.put("T1", new GridNode("T1", "110kV 变电站", "substation"));
        allNodes.put("T2", new GridNode("T2", "35kV 变电站", "substation"));
        allNodes.put("T3", new GridNode("T3", "10kV 配电所", "transformer"));
        allNodes.put("T4", new GridNode("T4", "微电网控制器", "transformer"));

        // 构建线路及损耗（权重）
        lines.add(new PowerLine("S", "T1", 0.02, 1000));
        lines.add(new PowerLine("S", "T2", 0.05, 500));
        lines.add(new PowerLine("T1", "T3", 0.01, 800));
        lines.add(new PowerLine("T2", "T3", 0.03, 400));
        lines.add(new PowerLine("T2", "T4", 0.02, 300));
        lines.add(new PowerLine("T3", "B", 0.01, 600));
        lines.add(new PowerLine("T4", "B", 0.04, 200));

        // 2. Dijkstra 算法实现
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparing(dist::get));

        for (String node : allNodes.keySet()) {
            dist.put(node, Double.MAX_VALUE);
        }
        dist.put("S", 0.0);
        pq.add("S");

        while (!pq.isEmpty()) {
            String u = pq.poll();
            if (u.equals("B")) break;

            for (PowerLine line : lines) {
                if (line.from.equals(u)) {
                    double alt = dist.get(u) + line.lossRate;
                    if (alt < dist.get(line.to)) {
                        dist.put(line.to, alt);
                        prev.put(line.to, u);
                        pq.add(line.to);
                    }
                }
            }
        }

        // 3. 回溯路径
        List<GridNode> path = new ArrayList<>();
        String curr = "B";
        while (curr != null) {
            path.add(0, allNodes.get(curr));
            curr = prev.get(curr);
        }

        return path;
    }

    /**
     * 算法 2: 多目标交易撮合优化算法 (Multi-Objective Matching)
     * 考虑价格、碳足迹、距离等多个维度进行最优匹配
     */
    public List<Map<String, Object>> optimizeMarketMatching(List<Map<String, Object>> buyOrders, List<Map<String, Object>> sellOrders) {
        // 使用加权评分模型 (WSM)
        // 评分 = w1*价格 + w2*碳排放 + w3*信誉度
        
        List<Map<String, Object>> matches = new ArrayList<>();
        
        // 按照评分对买家和卖家进行排序
        buyOrders.sort((a, b) -> Double.compare((Double)b.get("urgency"), (Double)a.get("urgency")));
        sellOrders.sort((a, b) -> Double.compare((Double)a.get("price"), (Double)b.get("price")));

        for (Map<String, Object> buyer : buyOrders) {
            double demand = (Double) buyer.get("amount");
            for (Map<String, Object> seller : sellOrders) {
                double supply = (Double) seller.get("amount");
                if (supply <= 0) continue;

                double traded = Math.min(demand, supply);
                if (traded > 0) {
                    Map<String, Object> match = new HashMap<>();
                    match.put("buyer", buyer.get("id"));
                    match.put("seller", seller.get("id"));
                    match.put("amount", traded);
                    match.put("price", seller.get("price"));
                    // 计算综合能效评分
                    match.put("efficiencyScore", calculateEfficiency(buyer, seller));
                    
                    matches.add(match);
                    
                    demand -= traded;
                    seller.put("amount", supply - traded);
                }
                if (demand <= 0) break;
            }
        }
        
        return matches;
    }

    private double calculateEfficiency(Map<String, Object> buyer, Map<String, Object> seller) {
        // 模拟复杂的能效计算公式
        // Score = 1 / (1 + distance_factor + loss_factor)
        double dist = Math.random() * 10; // 模拟距离
        double carbon = (Double) seller.getOrDefault("carbonIndex", 0.5);
        return 100 * (0.4 * (1/ (1 + dist)) + 0.6 * (1 - carbon));
    }
}
