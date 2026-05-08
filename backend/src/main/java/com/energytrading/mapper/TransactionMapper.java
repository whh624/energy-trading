package com.energytrading.mapper;

import com.energytrading.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TransactionMapper {
    
    int insert(Transaction transaction);
    
    int deleteById(Long id);
    
    Transaction selectById(Long id);
    
    Transaction selectByTxHash(String txHash);
    
    List<Transaction> selectByBuyerAddress(String buyerAddress);
    
    List<Transaction> selectBySellerAddress(String sellerAddress);
    
    List<Transaction> selectByUserAddress(String userAddress);
    
    List<Transaction> selectAll();
}
