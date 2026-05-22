package com.energytrading.service;

import com.energytrading.contract.EnergyTradingPlatform;
import com.energytrading.dto.TracePathDTO;
import com.energytrading.dto.TraceResultDTO;
import com.energytrading.entity.Transaction;
import com.energytrading.mapper.TransactionMapper;
import com.energytrading.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TraceService {

    @Autowired
    private Web3j web3j;

    @Autowired
    private EnergyTradingPlatform energyTradingPlatform;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AlgorithmService algorithmService;

    public TraceResultDTO verifyTransaction(String txHash) {
        TraceResultDTO result = new TraceResultDTO();
        result.setTxHash(txHash);

        Transaction dbTx = transactionMapper.selectByTxHash(txHash);
        if (dbTx == null) {
            result.setVerified(false);
            result.setVerifyStatus("交易记录不存在");
            return result;
        }

        result.setAmount(dbTx.getAmount());
        result.setSellerAddress(dbTx.getSellerAddress());
        result.setBuyerAddress(dbTx.getBuyerAddress());
        result.setOrderId(String.valueOf(dbTx.getOrderId()));
        result.setTimestamp(dbTx.getTimestamp());
        result.setTimeStr(formatTimestamp(dbTx.getTimestamp()));

        try {
            EthGetTransactionReceipt receiptResponse = web3j.ethGetTransactionReceipt(txHash).send();
            if (receiptResponse.getTransactionReceipt().isPresent()) {
                TransactionReceipt receipt = receiptResponse.getTransactionReceipt().get();
                result.setVerified(true);
                result.setVerifyStatus("已上链验证");
                result.setBlockNumber(receipt.getBlockNumber().toString());
                result.setBlockHash(receipt.getBlockHash());
                
                EthBlock blockResponse = web3j.ethGetBlockByHash(receipt.getBlockHash(), false).send();
                if (blockResponse.getBlock() != null) {
                    long blockTimestamp = blockResponse.getBlock().getTimestamp().longValue();
                    result.setTimeStr(formatTimestamp(blockTimestamp));
                }
            } else {
                result.setVerified(false);
                result.setVerifyStatus("交易未上链（数据库记录存在）");
            }
        } catch (Exception e) {
            result.setVerified(false);
            result.setVerifyStatus("区块链连接失败，使用数据库记录");
        }

        String certificate = generateCertificate(result);
        result.setCertificate(certificate);

        return result;
    }

    public TracePathDTO getTracePath(String txHash) {
        Transaction tx = transactionMapper.selectByTxHash(txHash);
        if (tx == null) {
            return null;
        }

        TracePathDTO path = new TracePathDTO();
        path.setTxHash(txHash);

        List<TracePathDTO.TraceNode> nodes = new ArrayList<>();

        // 使用复杂算法计算电力传输的最优路径
        List<AlgorithmService.GridNode> gridPath = algorithmService.calculateOptimalPath(
                tx.getSellerAddress(), tx.getBuyerAddress(), txHash);

        for (int i = 0; i < gridPath.size(); i++) {
            AlgorithmService.GridNode gn = gridPath.get(i);
            TracePathDTO.TraceNode node = new TracePathDTO.TraceNode();
            node.setStep(i + 1);
            node.setType(gn.type);
            node.setTitle(gn.name);
            node.setAddress(gn.id);
            node.setStatus("completed");
            
            // 补充一些描述信息
            if ("producer".equals(gn.type)) {
                node.setDescription("清洁能源发电机组并网点");
                node.setUserName(tx.getSellerName());
                node.setTimestamp(formatTimestamp(tx.getTimestamp() - 3600));
            } else if ("consumer".equals(gn.type)) {
                node.setDescription("终端用户智能电表接入");
                node.setUserName(tx.getBuyerName());
                node.setTimestamp(formatTimestamp(tx.getTimestamp()));
            } else {
                node.setDescription("电力调度中心自动路由节点");
                node.setUserName("电网节点");
                node.setTimestamp(formatTimestamp(tx.getTimestamp() - 1800 + i * 300));
            }
            nodes.add(node);
        }

        path.setNodes(nodes);

        String summary = String.format(
            "本次交易共 %.2f kWh 电力，从 %s 转移至 %s，交易哈希: %s",
            tx.getAmount(),
            tx.getSellerName() != null ? tx.getSellerName() : "未知卖家",
            tx.getBuyerName() != null ? tx.getBuyerName() : "未知买家",
            txHash.substring(0, 10) + "..."
        );
        path.setSummary(summary);

        return path;
    }

    public String generateCertificate(TraceResultDTO trace) {
        StringBuilder sb = new StringBuilder();
        sb.append("========== 电力交易溯源凭证 ==========\n");
        sb.append("凭证编号: ").append(trace.getTxHash()).append("\n");
        sb.append("验证状态: ").append(trace.getVerifyStatus()).append("\n");
        sb.append("验证结果: ").append(trace.getVerified() ? "已验证" : "未验证").append("\n");
        sb.append("--------------------------------------\n");
        sb.append("卖家地址: ").append(trace.getSellerAddress()).append("\n");
        sb.append("买家地址: ").append(trace.getBuyerAddress()).append("\n");
        sb.append("交易电量: ").append(String.format("%.2f", trace.getAmount())).append(" kWh\n");
        sb.append("订单编号: ").append(trace.getOrderId()).append("\n");
        sb.append("交易时间: ").append(trace.getTimeStr()).append("\n");
        if (trace.getBlockNumber() != null) {
            sb.append("区块高度: ").append(trace.getBlockNumber()).append("\n");
            sb.append("区块哈希: ").append(trace.getBlockHash()).append("\n");
        }
        sb.append("--------------------------------------\n");
        sb.append("数字签名: ").append(generateSignature(trace)).append("\n");
        sb.append("生成时间: ").append(formatTimestamp(System.currentTimeMillis() / 1000)).append("\n");
        sb.append("======================================");
        return sb.toString();
    }

    private String generateSignature(TraceResultDTO trace) {
        try {
            String data = trace.getTxHash() + trace.getAmount() + trace.getTimestamp();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().substring(0, 32).toUpperCase();
        } catch (Exception e) {
            return UUID.randomUUID().toString().replace("-", "").substring(0, 32).toUpperCase();
        }
    }

    public List<Map<String, Object>> getTransactionChain(String userAddress) {
        List<Transaction> transactions = transactionMapper.selectByUserAddress(userAddress);
        List<Map<String, Object>> chain = new ArrayList<>();

        for (Transaction tx : transactions) {
            Map<String, Object> node = new HashMap<>();
            node.put("txHash", tx.getTxHash());
            node.put("amount", tx.getAmount());
            node.put("timestamp", tx.getTimestamp());
            node.put("timeStr", formatTimestamp(tx.getTimestamp()));
            node.put("type", tx.getBuyerAddress().equals(userAddress) ? "buy" : "sell");
            node.put("counterparty", tx.getBuyerAddress().equals(userAddress) ? 
                tx.getSellerName() : tx.getBuyerName());
            chain.add(node);
        }

        return chain;
    }

    private String formatTimestamp(Long timestamp) {
        if (timestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(timestamp * 1000));
    }
}
