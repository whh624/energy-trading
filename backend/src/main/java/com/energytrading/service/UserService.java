package com.energytrading.service;

import com.energytrading.dto.UserLoginDTO;
import com.energytrading.dto.UserRegisterDTO;
import com.energytrading.entity.User;
import com.energytrading.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Transactional
    public User register(UserRegisterDTO dto) {
        User existUser = userMapper.selectByUsername(dto.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(md5(dto.getPassword()));
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
