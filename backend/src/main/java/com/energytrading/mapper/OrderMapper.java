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

    // 新增：获取开放订单的平均价格
    Double selectAveragePrice();

    // 新增：更新订单的异常状态
    int updateIsAbnormal(@Param("id") Long id, @Param("isAbnormal") Boolean isAbnormal);
}
