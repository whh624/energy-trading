package com.energytrading.controller;

import com.energytrading.common.Result;
import com.energytrading.dto.TracePathDTO;
import com.energytrading.dto.TraceResultDTO;
import com.energytrading.service.TraceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trace")
@CrossOrigin(origins = "*")
public class TraceController {

    @Autowired
    private TraceService traceService;

    @GetMapping("/verify/{txHash}")
    public Result<TraceResultDTO> verifyTransaction(@PathVariable String txHash) {
        try {
            TraceResultDTO result = traceService.verifyTransaction(txHash);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("验证失败: " + e.getMessage());
        }
    }

    @GetMapping("/path/{txHash}")
    public Result<TracePathDTO> getTracePath(@PathVariable String txHash) {
        try {
            TracePathDTO path = traceService.getTracePath(txHash);
            if (path == null) {
                return Result.error("交易记录不存在");
            }
            return Result.success(path);
        } catch (Exception e) {
            return Result.error("获取溯源路径失败: " + e.getMessage());
        }
    }

    @GetMapping("/certificate/{txHash}")
    public Result<String> getCertificate(@PathVariable String txHash) {
        try {
            TraceResultDTO trace = traceService.verifyTransaction(txHash);
            String certificate = traceService.generateCertificate(trace);
            return Result.success(certificate);
        } catch (Exception e) {
            return Result.error("生成凭证失败: " + e.getMessage());
        }
    }

    @GetMapping("/chain/{address}")
    public Result<List<Map<String, Object>>> getTransactionChain(@PathVariable String address) {
        try {
            List<Map<String, Object>> chain = traceService.getTransactionChain(address);
            return Result.success(chain);
        } catch (Exception e) {
            return Result.error("获取交易链失败: " + e.getMessage());
        }
    }
}
