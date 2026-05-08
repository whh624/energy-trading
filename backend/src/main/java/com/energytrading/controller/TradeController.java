package com.energytrading.controller;

import com.energytrading.common.Result;
import com.energytrading.dto.TradeDTO;
import com.energytrading.entity.Transaction;
import com.energytrading.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trade")
@CrossOrigin(origins = "*")
public class TradeController {

    @Autowired
    private TradeService tradeService;

    @PostMapping("/buy")
    public Result<Transaction> buyEnergy(
            @RequestBody TradeDTO dto,
            @RequestParam String buyerAddress,
            @RequestParam(required = false) String txHash,
            @RequestParam(required = false) Long blockNumber) {
        try {
            Transaction transaction = tradeService.buyEnergy(dto, buyerAddress, txHash, blockNumber);
            return Result.success("购买成功", transaction);
        } catch (Exception e) {
            return Result.error("购买失败: " + e.getMessage());
        }
    }

    @GetMapping("/history/{address}")
    public Result<List<Transaction>> getUserTransactions(@PathVariable String address) {
        try {
            return Result.success(tradeService.getUserTransactions(address));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/all")
    public Result<List<Transaction>> getAllTransactions() {
        try {
            return Result.success(tradeService.getAllTransactions());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/tx/{txHash}")
    public Result<Transaction> getByTxHash(@PathVariable String txHash) {
        Transaction transaction = tradeService.getByTxHash(txHash);
        if (transaction == null) {
            return Result.error("交易记录不存在");
        }
        return Result.success(transaction);
    }
}
