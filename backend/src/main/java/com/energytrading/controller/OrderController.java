package com.energytrading.controller;

import com.energytrading.common.Result;
import com.energytrading.dto.OrderCreateDTO;
import com.energytrading.entity.Order;
import com.energytrading.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public Result<Order> createOrder(@RequestBody OrderCreateDTO dto) {
        try {
            Order order = orderService.createOrder(dto, dto.getTxHash(), dto.getBlockNumber());
            return Result.success("挂单创建成功", order);
        } catch (Exception e) {
            return Result.error("创建挂单失败: " + e.getMessage());
        }
    }

    @PostMapping("/cancel/{id}")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        try {
            orderService.cancelOrder(id);
            return Result.success("取消成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/open")
    public Result<List<Order>> getOpenOrders() {
        try {
            return Result.success(orderService.getOpenOrders());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/user/{address}")
    public Result<List<Order>> getUserOrders(@PathVariable String address) {
        try {
            return Result.success(orderService.getUserOrders(address));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Order> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @GetMapping("/all")
    public Result<List<Order>> getAllOrders() {
        try {
            return Result.success(orderService.getAllOrders());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
