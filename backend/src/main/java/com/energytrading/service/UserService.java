package com.energytrading.service;

import com.energytrading.dto.UserLoginDTO;
import com.energytrading.dto.UserRegisterDTO;
import com.energytrading.entity.OperationLog;
import com.energytrading.entity.User;
import com.energytrading.mapper.OperationLogMapper;
import com.energytrading.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Transactional
    public void recordOperation(Long operatorId, String operatorName, Long targetId, String targetName, String type, String detail) {
        OperationLog log = new OperationLog();
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setTargetId(targetId);
        log.setTargetName(targetName);
        log.setOperationType(type);
        log.setOperationDetail(detail);
        operationLogMapper.insert(log);
    }

    public List<OperationLog> findAllLogs() {
        return operationLogMapper.selectAll();
    }

    @Transactional
    public User register(UserRegisterDTO dto) {
        User existUser = userMapper.selectByUsername(dto.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // Only enforce blockchain address for non-admin roles (role 3 is admin)
        if (dto.getRole() != 3 && (dto.getBlockchainAddress() == null || dto.getBlockchainAddress().isBlank())) {
            throw new RuntimeException("请先连接 MetaMask 钱包并获取钱包地址");
        }

        // Only check if wallet address exists if it's provided
        if (dto.getBlockchainAddress() != null && !dto.getBlockchainAddress().isBlank()) {
            User existWalletUser = userMapper.selectByBlockchainAddress(dto.getBlockchainAddress());
            if (existWalletUser != null) {
                throw new RuntimeException("该钱包地址已绑定其他账户，请切换钱包或直接登录");
            }
        }
        
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(md5(dto.getPassword()));
        // Set blockchainAddress only if it's provided, otherwise it remains null
        user.setBlockchainAddress(dto.getBlockchainAddress());
        user.setRole(dto.getRole() != null ? dto.getRole() : 0);
        user.setBalance(1000.0);
        user.setStatus(0); // 正常
        user.setTrustScore(100); // 默认100分
        
        userMapper.insert(user);
        return user;
    }

    public Map<String, Object> login(UserLoginDTO dto) {
        User user = userMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (!user.getPassword().equals(md5(dto.getPassword()))) {
            throw new RuntimeException("密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new RuntimeException("账户已被冻结，请联系管理方");
        }

        if (dto.getRole() != null && !dto.getRole().equals(user.getRole())) {
            throw new RuntimeException("所选身份与账户角色不一致");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("blockchainAddress", user.getBlockchainAddress());
        result.put("role", user.getRole());
        result.put("roleName", getRoleName(user.getRole()));
        result.put("balance", user.getBalance());
        result.put("status", user.getStatus());
        result.put("trustScore", user.getTrustScore());
        return result;
    }

    public User findById(Long id) {
        return userMapper.selectById(id);
    }

    public User findByBlockchainAddress(String blockchainAddress) {
        return userMapper.selectByBlockchainAddress(blockchainAddress);
    }

    public User findByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Transactional
    public User updateBlockchainAddress(Long id, String blockchainAddress) {
        if (id == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        if (blockchainAddress == null || blockchainAddress.isBlank()) {
            throw new RuntimeException("钱包地址不能为空");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        User existWalletUser = userMapper.selectByBlockchainAddress(blockchainAddress);
        if (existWalletUser != null && !existWalletUser.getId().equals(id)) {
            throw new RuntimeException("该钱包地址已绑定其他账户，请切换钱包后再试");
        }

        user.setBlockchainAddress(blockchainAddress);
        userMapper.update(user);
        return user;
    }

    @Transactional
    public void updateBalance(Long id, Double balance) {
        userMapper.updateBalance(id, balance);
    }

    public java.util.List<User> findAll() {
        return userMapper.selectAll();
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        userMapper.updateStatus(id, status);
    }

    @Transactional
    public void updateTrustScore(Long id, Integer score) {
        userMapper.updateTrustScore(id, score);
    }

    // 新增：降低用户信用分
    @Transactional
    public void decreaseTrustScore(String blockchainAddress, int decreaseAmount) {
        User user = userMapper.selectByBlockchainAddress(blockchainAddress);
        if (user != null) {
            int newTrustScore = Math.max(0, user.getTrustScore() - decreaseAmount);
            userMapper.updateTrustScore(user.getId(), newTrustScore);
        }
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return input;
        }
    }

    private String getRoleName(Integer role) {
        if (role == null) return "普通用户";
        switch (role) {
            case 1: return "产电方";
            case 2: return "用电方";
            case 3: return "管理方";
            default: return "普通用户";
        }
    }
}
