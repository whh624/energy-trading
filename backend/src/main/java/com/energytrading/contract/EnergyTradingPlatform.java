package com.energytrading.contract;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.crypto.Credentials;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.DynamicArray;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EnergyTradingPlatform extends Contract {

    private static final String BINARY = "";

    private static final String CREATE_ORDER_FUNCTION = "createOrder(uint256,uint256)";
    private static final String CANCEL_ORDER_FUNCTION = "cancelOrder(uint256)";
    private static final String BUY_ENERGY_FUNCTION = "buyEnergy(uint256,uint256)";
    private static final String GET_ORDER_FUNCTION = "getOrder(uint256)";
    private static final String GET_USER_TRANSACTIONS_FUNCTION = "getUserTransactions(address)";
    private static final String GET_TRANSACTION_FUNCTION = "getTransaction(uint256)";
    private static final String GET_OPEN_ORDERS_FUNCTION = "getOpenOrders()";
    private static final String GET_USER_ORDERS_FUNCTION = "getUserOrders(address)";

    protected EnergyTradingPlatform(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, gasProvider);
    }

    public RemoteCall<TransactionReceipt> createOrder(BigInteger amount, BigInteger price) {
        Function function = new Function(
                CREATE_ORDER_FUNCTION,
                Arrays.asList(new Uint256(amount), new Uint256(price)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> cancelOrder(BigInteger orderId) {
        Function function = new Function(
                CANCEL_ORDER_FUNCTION,
                Arrays.asList(new Uint256(orderId)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> buyEnergy(BigInteger orderId, BigInteger amount) {
        Function function = new Function(
                BUY_ENERGY_FUNCTION,
                Arrays.asList(new Uint256(orderId), new Uint256(amount)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    @SuppressWarnings("unchecked")
    public RemoteCall<List<Type>> getOrder(BigInteger orderId) {
        Function function = new Function(
                GET_ORDER_FUNCTION,
                Arrays.asList(new Uint256(orderId)),
                Arrays.asList(
                        new TypeReference<Uint256>() {},
                        new TypeReference<Address>() {},
                        new TypeReference<Uint256>() {},
                        new TypeReference<Uint256>() {},
                        new TypeReference<Uint8>() {},
                        new TypeReference<Uint256>() {}));
        return executeRemoteCallMultipleValueReturn(function);
    }

    @SuppressWarnings("unchecked")
    public RemoteCall<List<Type>> getUserTransactions(String user) {
        Function function = new Function(
                GET_USER_TRANSACTIONS_FUNCTION,
                Arrays.asList(new Address(user)),
                Arrays.asList(new TypeReference<DynamicArray<Uint256>>() {}));
        return executeRemoteCallMultipleValueReturn(function);
    }

    @SuppressWarnings("unchecked")
    public RemoteCall<List<Type>> getTransaction(BigInteger transactionId) {
        Function function = new Function(
                GET_TRANSACTION_FUNCTION,
                Arrays.asList(new Uint256(transactionId)),
                Arrays.asList(
                        new TypeReference<Uint256>() {},
                        new TypeReference<Uint256>() {},
                        new TypeReference<Address>() {},
                        new TypeReference<Address>() {},
                        new TypeReference<Uint256>() {},
                        new TypeReference<Uint256>() {},
                        new TypeReference<Uint256>() {}));
        return executeRemoteCallMultipleValueReturn(function);
    }

    @SuppressWarnings("unchecked")
    public RemoteCall<List<Type>> getOpenOrders() {
        Function function = new Function(
                GET_OPEN_ORDERS_FUNCTION,
                Collections.emptyList(),
                Arrays.asList(new TypeReference<DynamicArray<Uint256>>() {}));
        return executeRemoteCallMultipleValueReturn(function);
    }

    @SuppressWarnings("unchecked")
    public RemoteCall<List<Type>> getUserOrders(String user) {
        Function function = new Function(
                GET_USER_ORDERS_FUNCTION,
                Arrays.asList(new Address(user)),
                Arrays.asList(new TypeReference<DynamicArray<Uint256>>() {}));
        return executeRemoteCallMultipleValueReturn(function);
    }

    public static EnergyTradingPlatform load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return new EnergyTradingPlatform(
                contractAddress,
                web3j,
                new ReadonlyTransactionManager(web3j, credentials.getAddress()),
                gasProvider);
    }

    public static EnergyTradingPlatform load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        return new EnergyTradingPlatform(
                contractAddress,
                web3j,
                transactionManager,
                gasProvider);
    }
}