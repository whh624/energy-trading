package com.energytrading.mapper;

import com.energytrading.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {
    
    int insert(Order order);
    
    int update(Order order);
    
    int deleteById(Long id);
    
    Order selectById(Long id);
    
    Order selectByOrderIdOnChain(Long orderIdOnChain);
    
    List<Order> selectByStatus(Integer status);
    
    List<Order> selectBySellerAddress(String sellerAddress);
    
    List<Order> selectAll();
    
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    int updateAmount(@Param("id") Long id, @Param("amount") Double amount);
}
