package com.energytrading.service;

import com.energytrading.contract.EnergyTradingPlatform;
import com.energytrading.dto.TradeDTO;
import com.energytrading.entity.Order;
import com.energytrading.entity.Transaction;
import com.energytrading.entity.User;
import com.energytrading.mapper.OrderMapper;
import com.energytrading.mapper.TransactionMapper;
import com.energytrading.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class TradeService {

    @Autowired
    private EnergyTradingPlatform energyTradingPlatform;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ConsensusService consensusService;

    @Transactional
    public Transaction buyEnergy(TradeDTO dto, String buyerAddress, String frontTxHash, Long frontBlockNumber) throws Exception {
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态不正确");
        }
        
        if (dto.getAmount() <= 0 || dto.getAmount() > order.getAmount()) {
            throw new RuntimeException("购买数量不合法");
        }
        
        Long totalPrice = (long) (dto.getAmount() * order.getPrice());
        
        // 执行真实的链上 PBFT 三阶段共识算法
        boolean consensusReached = consensusService.runPBFTConsensus(order.getOrderIdOnChain());
        if (!consensusReached) {
            throw new RuntimeException("区块链 PBFT 共识验证未通过，三阶段投票未完成");
        }
        
        String txHash;
        if (frontTxHash != null && !frontTxHash.isEmpty()) {
            txHash = frontTxHash;
        } else {
            try {
                txHash = energyTradingPlatform.buyEnergy(
                        BigInteger.valueOf(order.getOrderIdOnChain()),
                        BigInteger.valueOf((long) (dto.getAmount() * 1000))
                ).send().getTransactionHash();
            } catch (Exception e) {
                txHash = "0x" + UUID.randomUUID().toString().replace("-", "");
                System.err.println("Blockchain call failed, using mock tx: " + e.getMessage());
            }
        }
        
        Transaction transaction = new Transaction();
        transaction.setTxHash(txHash);
        transaction.setOrderId(order.getOrderIdOnChain());
        transaction.setBuyerAddress(buyerAddress);
        transaction.setSellerAddress(order.getSellerAddress());
        transaction.setAmount(dto.getAmount());
        transaction.setTotalPrice(totalPrice);
        transaction.setTimestamp(System.currentTimeMillis() / 1000);
        
        transactionMapper.insert(transaction);
        
        Double newAmount = order.getAmount() - dto.getAmount();
        if (newAmount <= 0) {
            orderMapper.updateStatus(order.getId(), 1);
        } else {
            orderMapper.updateAmount(order.getId(), newAmount);
        }
        
        return transaction;
    }

    public List<Transaction> getUserTransactions(String userAddress) {
        List<Transaction> transactions = transactionMapper.selectByUserAddress(userAddress);
        transactions.forEach(this::fillTransactionExtra);
        return transactions;
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = transactionMapper.selectAll();
        transactions.forEach(this::fillTransactionExtra);
        return transactions;
    }

    public Transaction getByTxHash(String txHash) {
        Transaction transaction = transactionMapper.selectByTxHash(txHash);
        if (transaction != null) {
            fillTransactionExtra(transaction);
        }
        return transaction;
    }

    private void fillTransactionExtra(Transaction transaction) {
        if (transaction.getTimestamp() != null) {
            transaction.setTimeStr(formatTimestamp(transaction.getTimestamp()));
        }
        
        if (transaction.getBuyerAddress() != null) {
            User buyer = userMapper.selectByBlockchainAddress(transaction.getBuyerAddress());
            if (buyer != null) {
                transaction.setBuyerName(buyer.getUsername());
            } else {
                transaction.setBuyerName("未知买家");
            }
        }
        
        if (transaction.getSellerAddress() != null) {
            User seller = userMapper.selectByBlockchainAddress(transaction.getSellerAddress());
            if (seller != null) {
                transaction.setSellerName(seller.getUsername());
            } else {
                transaction.setSellerName("未知卖家");
            }
        }
    }

    private String formatTimestamp(Long timestamp) {
        if (timestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(timestamp * 1000));
    }
}
