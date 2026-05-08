package com.energytrading.controller;

import com.energytrading.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "*")
public class WalletController {

    private final ConcurrentHashMap<String, String> nonceStore = new ConcurrentHashMap<>();

    @GetMapping("/nonce/{address}")
    public Result<Map<String, Object>> getNonce(@PathVariable String address) {
        String nonce = "energy_trading_" + System.currentTimeMillis();
        nonceStore.put(address.toLowerCase(), nonce);
        
        Map<String, Object> data = new HashMap<>();
        data.put("nonce", nonce);
        data.put("message", "欢迎访问微电网P2P电力交易平台！\n\n请签名以验证您的身份。\n\nNonce: " + nonce);
        return Result.success(data);
    }

    @PostMapping("/verify")
    public Result<Map<String, Object>> verifySignature(@RequestBody Map<String, String> request) {
        String address = request.get("address");
        String signature = request.get("signature");
        String message = request.get("message");

        if (address == null || signature == null || message == null) {
            return Result.error("参数不完整");
        }

        String storedNonce = nonceStore.get(address.toLowerCase());
        if (storedNonce != null && message.contains(storedNonce)) {
            nonceStore.remove(address.toLowerCase());
            
            Map<String, Object> data = new HashMap<>();
            data.put("verified", true);
            data.put("address", address);
            return Result.success("签名验证成功", data);
        }

        return Result.error("签名验证失败");
    }

    @GetMapping("/contract-info")
    public Result<Map<String, Object>> getContractInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("address", "0xf999bA376BFADA777303774980265Bc94335a806");
        info.put("network", "Ganache Local");
        info.put("chainId", 1337);
        info.put("rpcUrl", "http://127.0.0.1:8545");
        return Result.success(info);
    }
}
