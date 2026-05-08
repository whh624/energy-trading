package com.energytrading.service;

import com.energytrading.contract.EnergyTradingPlatform;
import com.energytrading.dto.OrderCreateDTO;
import com.energytrading.entity.Order;
import com.energytrading.entity.User;
import com.energytrading.mapper.OrderMapper;
import com.energytrading.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private EnergyTradingPlatform energyTradingPlatform;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Transactional
    public Order createOrder(OrderCreateDTO dto, String frontTxHash, Long frontBlockNumber) throws Exception {
        Long chainOrderId = System.currentTimeMillis();
        
        if (frontTxHash == null || frontTxHash.isEmpty()) {
            try {
                energyTradingPlatform.createOrder(
                        BigInteger.valueOf((long) (dto.getAmount() * 1000)),
                        BigInteger.valueOf(dto.getPrice())
                ).send();
            } catch (Exception e) {
                System.err.println("Blockchain call failed: " + e.getMessage());
            }
        }

        Order order = new Order();
        order.setOrderIdOnChain(chainOrderId);
        order.setSellerAddress(dto.getSellerAddress());
        order.setAmount(dto.getAmount());
        order.setPrice(dto.getPrice());
        order.setStatus(0);
        
        orderMapper.insert(order);
        return order;
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
