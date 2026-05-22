package com.energytrading.controller;

import com.energytrading.common.Result;
import com.energytrading.service.AlgorithmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/algorithm")
public class AlgorithmController {

    @Autowired
    private AlgorithmService algorithmService;

    /**
     * 演示市场匹配优化算法
     */
    @GetMapping("/optimize-matching")
    public Result optimizeMatching() {
        // 1. 模拟市场上的买家订单
        List<Map<String, Object>> buyOrders = new ArrayList<>();
        buyOrders.add(createOrder("B1", 100.0, 0.0, 0.8)); // ID, 电量, 价格(买家不限价), 紧急程度
        buyOrders.add(createOrder("B2", 150.0, 0.0, 0.4));
        buyOrders.add(createOrder("B3", 80.0, 0.0, 0.9));

        // 2. 模拟市场上的卖家订单
        List<Map<String, Object>> sellOrders = new ArrayList<>();
        sellOrders.add(createOrder("S1", 120.0, 0.55, 0.2)); // ID, 电量, 价格, 碳排放指数
        sellOrders.add(createOrder("S2", 200.0, 0.60, 0.1));
        sellOrders.add(createOrder("S3", 50.0, 0.50, 0.5));

        // 3. 执行复杂匹配算法
        List<Map<String, Object>> matches = algorithmService.optimizeMarketMatching(buyOrders, sellOrders);

        Map<String, Object> response = new HashMap<>();
        response.put("matches", matches);
        response.put("totalMatchedAmount", matches.stream().mapToDouble(m -> (Double)m.get("amount")).sum());
        response.put("averageEfficiency", matches.stream().mapToDouble(m -> (Double)m.get("efficiencyScore")).average().orElse(0.0));

        return Result.success(response);
    }

    private Map<String, Object> createOrder(String id, double amount, double price, double factor) {
        Map<String, Object> order = new HashMap<>();
        order.put("id", id);
        order.put("amount", amount);
        order.put("price", price);
        if (id.startsWith("B")) {
            order.put("urgency", factor);
        } else {
            order.put("carbonIndex", factor);
        }
        return order;
    }
}
