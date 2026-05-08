package com.energytrading.mapper;

import com.energytrading.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    
    int insert(User user);
    
    int update(User user);
    
    int deleteById(Long id);
    
    User selectById(Long id);
    
    User selectByUsername(String username);
    
    User selectByBlockchainAddress(String blockchainAddress);
    
    List<User> selectAll();
    
    User selectByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
    
    int updateBalance(@Param("id") Long id, @Param("balance") Double balance);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updateTrustScore(@Param("id") Long id, @Param("trustScore") Integer trustScore);
}
