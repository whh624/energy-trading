package com.energytrading.controller;

import com.energytrading.common.Result;
import com.energytrading.dto.UserLoginDTO;
import com.energytrading.dto.UserRegisterDTO;
import com.energytrading.entity.OperationLog;
import com.energytrading.entity.User;
import com.energytrading.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/logs")
    public Result<List<OperationLog>> getLogs() {
        return Result.success(userService.findAllLogs());
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody UserRegisterDTO dto) {
        try {
            User user = userService.register(dto);
            return Result.success("注册成功", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "blockchainAddress", user.getBlockchainAddress(),
                "role", user.getRole(),
                "balance", user.getBalance()
            ));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody UserLoginDTO dto) {
        try {
            Map<String, Object> user = userService.login(dto);
            return Result.success("登录成功", user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @GetMapping("/address/{address}")
    public Result<User> getUserByAddress(@PathVariable String address) {
        User user = userService.findByBlockchainAddress(address);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @GetMapping("/list")
    public Result<java.util.List<User>> listAll() {
        java.util.List<User> users = userService.findAll();
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    @PostMapping("/status")
    public Result<String> updateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        
        // 记录日志所需信息
        Long operatorId = params.get("operatorId") != null ? Long.valueOf(params.get("operatorId").toString()) : 0L;
        String operatorName = params.get("operatorName") != null ? params.get("operatorName").toString() : "管理员";
        
        User targetUser = userService.findById(id);
        String targetName = targetUser != null ? targetUser.getUsername() : "未知用户";
        
        userService.updateStatus(id, status);
        
        try {
            String type = status == 1 ? "冻结账户" : "解冻账户";
            String detail = String.format("管理员 %s %s了用户 %s (ID: %d) 的账户", operatorName, status == 1 ? "冻结" : "解冻", targetName, id);
            userService.recordOperation(operatorId, operatorName, id, targetName, type, detail);
        } catch (Exception e) {
            System.err.println("记录操作日志失败: " + e.getMessage());
        }
        
        return Result.success("更新状态成功");
    }

    @PostMapping("/trust")
    public Result<String> updateTrustScore(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer score = Integer.valueOf(params.get("trustScore").toString());
        
        // 记录日志所需信息
        Long operatorId = params.get("operatorId") != null ? Long.valueOf(params.get("operatorId").toString()) : 0L;
        String operatorName = params.get("operatorName") != null ? params.get("operatorName").toString() : "管理员";
        
        User targetUser = userService.findById(id);
        String targetName = targetUser != null ? targetUser.getUsername() : "未知用户";
        
        userService.updateTrustScore(id, score);
        
        try {
            String detail = String.format("管理员 %s 将用户 %s (ID: %d) 的信用分修改为 %d", operatorName, targetName, id, score);
            userService.recordOperation(operatorId, operatorName, id, targetName, "修改评分", detail);
        } catch (Exception e) {
            System.err.println("记录操作日志失败: " + e.getMessage());
        }
        
        return Result.success("更新信用分成功");
    }

    @PostMapping("/wallet-address")
    public Result<Map<String, Object>> updateWalletAddress(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            String blockchainAddress = params.get("blockchainAddress").toString();
            User user = userService.updateBlockchainAddress(id, blockchainAddress);
            return Result.success("钱包地址更新成功", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "blockchainAddress", user.getBlockchainAddress(),
                    "role", user.getRole(),
                    "balance", user.getBalance(),
                    "status", user.getStatus(),
                    "trustScore", user.getTrustScore()
            ));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
