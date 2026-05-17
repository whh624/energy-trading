package com.energytrading.service;

import com.energytrading.contract.EnergyTradingPlatform;
import com.energytrading.dto.OrderCreateDTO;
import com.energytrading.entity.Order;
import com.energytrading.entity.User;
import com.energytrading.mapper.OrderMapper;
import com.energytrading.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private EnergyTradingPlatform energyTradingPlatform;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private Web3j web3j;

    @Autowired
    private UserService userService; // 注入 UserService

    private static final Event ORDER_CREATED_EVENT = new Event(
            "OrderCreated",
            Arrays.asList(
                    new TypeReference<Uint256>() {},
                    new TypeReference<org.web3j.abi.datatypes.Address>() {},
                    new TypeReference<Uint256>() {},
                    new TypeReference<Uint256>() {}
            )
    );

    @Transactional
    public Order createOrder(OrderCreateDTO dto, String frontTxHash, Long frontBlockNumber) throws Exception {
        User seller = userMapper.selectByBlockchainAddress(dto.getSellerAddress());
        if (seller == null) {
            throw new RuntimeException("当前卖家地址未注册，请先绑定并使用已注册的钱包地址");
        }

        Long chainOrderId = dto.getOrderIdOnChain();

        if (frontTxHash == null || frontTxHash.isEmpty()) {
            try {
                TransactionReceipt receipt = energyTradingPlatform.createOrder(
                        BigInteger.valueOf((long) (dto.getAmount() * 1000)),
                        BigInteger.valueOf(dto.getPrice())
                ).send();
                chainOrderId = extractOrderIdFromReceipt(receipt)
                        .orElseThrow(() -> new RuntimeException("未能从链上回执中解析订单ID"));
            } catch (Exception e) {
                throw new RuntimeException("链上创建订单失败: " + e.getMessage(), e);
            }
        } else if (chainOrderId == null) {
            chainOrderId = extractOrderIdFromFrontTx(frontTxHash)
                    .orElseThrow(() -> new RuntimeException("未能根据交易哈希解析链上订单ID"));
        }

        Order order = new Order();
        order.setOrderIdOnChain(chainOrderId);
        order.setSellerAddress(dto.getSellerAddress());
        order.setAmount(dto.getAmount());
        order.setPrice(dto.getPrice());
        order.setStatus(0);
        
        orderMapper.insert(order);
        // 调用价格偏离度监控
        monitorPriceBias(order);
        return order;
    }

    // 自动化异常监控算法
    private void monitorPriceBias(Order order) {
        try {
            Double avgPrice = orderMapper.selectAveragePrice();
            // 如果没有其他开放订单，则无法计算平均价格，跳过监控
            if (avgPrice == null || avgPrice == 0) {
                log.info("当前没有其他开放订单，跳过价格偏离度监控。订单ID: {}", order.getId());
                return;
            }

            double currentPrice = order.getPrice();
            double bias = Math.abs(currentPrice - avgPrice) / avgPrice; // 计算偏离度

            if (bias > 0.5) { // 偏离度阈值设为 0.5 (50%)
                order.setIsAbnormal(true); // 标记为异常订单
                orderMapper.updateIsAbnormal(order.getId(), true); // 更新数据库
                log.warn("检测到价格异常订单，ID: {}, 当前价格: {}, 市场均价: {}, 偏离度: {}",
                         order.getId(), currentPrice, avgPrice, String.format("%.2f", bias));
                // 触发信用分扣减逻辑
                userService.decreaseTrustScore(order.getSellerAddress(), 10); // 扣减10点信用分
            } else {
                order.setIsAbnormal(false);
                orderMapper.updateIsAbnormal(order.getId(), false); // 确保数据库状态一致
            }
        } catch (Exception e) {
            log.error("价格偏离度监控异常，订单ID: {}. 错误信息: {}", order.getId(), e.getMessage());
        }
    }

    private Optional<Long> extractOrderIdFromFrontTx(String txHash) {
        try {
            Optional<TransactionReceipt> receiptOptional = web3j.ethGetTransactionReceipt(txHash).send().getTransactionReceipt();
            if (receiptOptional.isEmpty()) {
                return Optional.empty();
            }
            return extractOrderIdFromReceipt(receiptOptional.get());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<Long> extractOrderIdFromReceipt(TransactionReceipt receipt) {
        String eventSignature = EventEncoder.encode(ORDER_CREATED_EVENT);

        for (Log log : receipt.getLogs()) {
            if (log.getTopics() == null || log.getTopics().isEmpty()) {
                continue;
            }
            if (!eventSignature.equals(log.getTopics().get(0))) {
                continue;
            }

            List<Type> decodedValues = FunctionReturnDecoder.decode(log.getData(), ORDER_CREATED_EVENT.getNonIndexedParameters());
            if (decodedValues.isEmpty()) {
                continue;
            }

            BigInteger orderId = (BigInteger) decodedValues.get(0).getValue();
            return Optional.of(orderId.longValue());
        }

        return Optional.empty();
    }

    @Transactional
    public void cancelOrder(Long orderId) throws Exception {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (order.getStatus() != 0) {
            throw new RuntimeException("只能取消开放状态的订单");
        }
        
        try {
            energyTradingPlatform.cancelOrder(BigInteger.valueOf(order.getOrderIdOnChain())).send();
        } catch (Exception e) {
            System.err.println("Blockchain call failed: " + e.getMessage());
        }
        
        orderMapper.updateStatus(orderId, 2);
    }

    public List<Order> getOpenOrders() {
        List<Order> orders = orderMapper.selectByStatus(0);
        orders.forEach(this::fillOrderExtra);
        return orders;
    }

    public List<Order> getUserOrders(String sellerAddress) {
        List<Order> orders = orderMapper.selectBySellerAddress(sellerAddress);
        orders.forEach(this::fillOrderExtra);
        return orders;
    }

    public Order getOrderById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order != null) {
            fillOrderExtra(order);
        }
        return order;
    }

    public List<Order> getAllOrders() {
        List<Order> orders = orderMapper.selectAll();
        orders.forEach(this::fillOrderExtra);
        return orders;
    }

    private void fillOrderExtra(Order order) {
        if (order.getStatus() != null) {
            order.setStatusName(getStatusName(order.getStatus()));
        }
        if (order.getSellerName() != null && !order.getSellerName().isBlank()) {
            return;
        }
        if (order.getSellerAddress() != null) {
            User seller = userMapper.selectByBlockchainAddress(order.getSellerAddress());
            if (seller != null) {
                order.setSellerName(seller.getUsername());
            } else {
                order.setSellerName("未知卖家");
            }
        }
    }

    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "开放中";
            case 1: return "已完成";
            case 2: return "已取消";
            default: return "未知";
        }
    }
}
