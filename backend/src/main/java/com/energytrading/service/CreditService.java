package com.energytrading.service;

import com.energytrading.entity.User;
import com.energytrading.entity.Transaction;
import com.energytrading.entity.Order;
import com.energytrading.mapper.UserMapper;
import com.energytrading.mapper.TransactionMapper;
import com.energytrading.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 自动化信用评分服务
 * 基于用户的历史挂单和交易行为自动计算信用分
 */
@Service
public class CreditService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private OrderMapper orderMapper;

    private static final int BASE_SCORE = 100;
    private static final int MAX_SCORE = 200;
    private static final int MIN_SCORE = 0;

    /**
     * 重新计算并更新用户的信用分
     * @param blockchainAddress 用户的区块链地址
     */
    @Transactional
    public void updateAutomatedTrustScore(String blockchainAddress) {
        User user = userMapper.selectByBlockchainAddress(blockchainAddress);
        if (user == null) return;

        // 1. 获取交易历史
        List<Transaction> buyTxs = transactionMapper.selectByBuyerAddress(blockchainAddress);
        List<Transaction> sellTxs = transactionMapper.selectBySellerAddress(blockchainAddress);
        int totalTransactions = buyTxs.size() + sellTxs.size();

        // 2. 获取累计交易电量
        double totalVolume = 0;
        for (Transaction tx : buyTxs) totalVolume += tx.getAmount();
        for (Transaction tx : sellTxs) totalVolume += tx.getAmount();

        // 3. 获取历史挂单数量
        List<Order> historyOrders = orderMapper.selectBySellerAddress(blockchainAddress);
        int totalOrders = historyOrders.size();

        // 4. 自动评分算法模型
        // 基础分 100
        // 每笔成交交易 +2 分
        // 每 100 kWh 交易量 +1 分
        // 每次积极挂单 (无论成交与否) +1 分 (体现市场活跃度)
        int calculatedScore = BASE_SCORE 
                            + (totalTransactions * 2) 
                            + (int)(totalVolume / 100) 
                            + (totalOrders * 1);

        // 限制最高分和最低分
        if (calculatedScore > MAX_SCORE) calculatedScore = MAX_SCORE;
        if (calculatedScore < MIN_SCORE) calculatedScore = MIN_SCORE;

        // 更新数据库
        userMapper.updateTrustScore(user.getId(), calculatedScore);
        System.out.println("User " + user.getUsername() + " trust score updated to: " + calculatedScore);
    }

    /**
     * 批量更新所有用户的信用分 (可由定时任务调用)
     */
    @Transactional
    public void updateAllUserScores() {
        List<User> users = userMapper.selectAll();
        for (User user : users) {
            updateAutomatedTrustScore(user.getBlockchainAddress());
        }
    }
}
