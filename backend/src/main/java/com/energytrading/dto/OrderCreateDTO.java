package com.energytrading.dto;

public class OrderCreateDTO {
    private Double amount;
    private Long price;
    private String sellerAddress;
    private String txHash;
    private Long blockNumber;

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }
    public String getSellerAddress() { return sellerAddress; }
    public void setSellerAddress(String sellerAddress) { this.sellerAddress = sellerAddress; }
    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }
    public Long getBlockNumber() { return blockNumber; }
    public void setBlockNumber(Long blockNumber) { this.blockNumber = blockNumber; }
}
