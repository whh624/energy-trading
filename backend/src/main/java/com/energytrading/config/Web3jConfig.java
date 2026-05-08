package com.energytrading.config;

import com.energytrading.contract.EnergyTradingPlatform;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;

@Configuration
public class Web3jConfig {

    @Value("${web3j.url}")
    private String web3jUrl;

    @Value("${web3j.contract.address}")
    private String contractAddress;

    @Value("${web3j.private.key}")
    private String privateKey;

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(web3jUrl));
    }

    @Bean
    public Credentials credentials() {
        return Credentials.create(privateKey);
    }

    @Bean
    public StaticGasProvider gasProvider() {
        return new StaticGasProvider(
                BigInteger.valueOf(20000000000L), // Gas price
                BigInteger.valueOf(6721975L)       // Gas limit
        );
    }

    @Bean
    public String contractAddress() {
        return contractAddress;
    }

    @Bean
    public EnergyTradingPlatform energyTradingPlatform(Web3j web3j, Credentials credentials, StaticGasProvider gasProvider) {
        return EnergyTradingPlatform.load(
                contractAddress,
                web3j,
                credentials,
                gasProvider
        );
    }
}