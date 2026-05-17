import { defineStore } from 'pinia'
import { ref, computed, markRaw, onUnmounted } from 'vue'
import { ethers } from 'ethers'

const CONTRACT_ABI = [
    {
        "anonymous": false,
        "inputs": [
            {"indexed": false, "name": "orderId", "type": "uint256"},
            {"indexed": false, "name": "seller", "type": "address"},
            {"indexed": false, "name": "amount", "type": "uint256"},
            {"indexed": false, "name": "price", "type": "uint256"}
        ],
        "name": "OrderCreated",
        "type": "event"
    },
    {
        "inputs": [],
        "name": "deposit",
        "outputs": [],
        "stateMutability": "payable",
        "type": "function"
    },
    {
        "inputs": [{"name": "amount", "type": "uint256"}],
        "name": "freezeBalance",
        "outputs": [],
        "stateMutability": "nonpayable",
        "type": "function"
    },
    {
        "inputs": [{"name": "amount", "type": "uint256"}],
        "name": "unfreezeBalance",
        "outputs": [],
        "stateMutability": "nonpayable",
        "type": "function"
    },
    {
        "inputs": [{"name": "amount", "type": "uint256"}],
        "name": "withdraw",
        "outputs": [],
        "stateMutability": "nonpayable",
        "type": "function"
    },
    {
        "inputs": [
            {"name": "_amount", "type": "uint256"},
            {"name": "_price", "type": "uint256"}
        ],
        "name": "createOrder",
        "outputs": [{"name": "", "type": "uint256"}],
        "stateMutability": "nonpayable",
        "type": "function"
    },
    {
        "inputs": [
            {"name": "_orderId", "type": "uint256"},
            {"name": "_amount", "type": "uint256"}
        ],
        "name": "buyEnergy",
        "outputs": [],
        "stateMutability": "payable",
        "type": "function"
    },
    {
        "inputs": [{"name": "_orderId", "type": "uint256"}],
        "name": "cancelOrder",
        "outputs": [],
        "stateMutability": "nonpayable",
        "type": "function"
    },
    {
        "inputs": [{"name": "_orderId", "type": "uint256"}],
        "name": "getOrder",
        "outputs": [
            {"name": "id", "type": "uint256"},
            {"name": "seller", "type": "address"},
            {"name": "amount", "type": "uint256"},
            {"name": "price", "type": "uint256"},
            {"name": "status", "type": "uint8"},
            {"name": "createdAt", "type": "uint256"}
        ],
        "stateMutability": "view",
        "type": "function"
    },
    {
        "inputs": [],
        "name": "getOpenOrders",
        "outputs": [{"name": "", "type": "uint256[]"}],
        "stateMutability": "view",
        "type": "function"
    },
    {
        "inputs": [{"name": "_user", "type": "address"}],
        "name": "getBalance",
        "outputs": [
            {"name": "available", "type": "uint256"},
            {"name": "frozen", "type": "uint256"}
        ],
        "stateMutability": "view",
        "type": "function"
    }
]

const CONTRACT_ADDRESS = '0x3e85B795BD87d0E10d9aac9a910Ecee2609Af476'
const GANACHE_RPC_URL = 'http://localhost:7545'

export const useWalletStore = defineStore('wallet', () => {
    const account = ref('')
    const chainId = ref(null)
    const balance = ref('0')
    const isConnecting = ref(false)
    const isConnected = computed(() => !!account.value)
    const contract = ref(null)
    const provider = ref(null)
    const signer = ref(null)

    const shortAddress = computed(() => {
        if (!account.value) return ''
        return `${account.value.substring(0, 6)}...${account.value.substring(account.value.length - 4)}`
    })

    const balanceInEth = computed(() => {
        return parseFloat(ethers.utils.formatEther(balance.value)).toFixed(4)
    })

    async function connectWallet() {
        if (typeof window.ethereum === 'undefined') {
            throw new Error('请先安装MetaMask钱包扩展')
        }

        isConnecting.value = true
        try {
            const accounts = await window.ethereum.request({
                method: 'eth_requestAccounts'
            })

            if (accounts.length === 0) {
                throw new Error('未获取到账户信息')
            }

            account.value = accounts[0]

            provider.value = markRaw(new ethers.providers.Web3Provider(window.ethereum))
            signer.value = markRaw(provider.value.getSigner())

            const network = await provider.value.getNetwork()
            chainId.value = network.chainId

            await updateBalance()

            contract.value = markRaw(new ethers.Contract(
                CONTRACT_ADDRESS,
                CONTRACT_ABI,
                signer.value
            ))

            window.ethereum.on('accountsChanged', handleAccountsChanged)
            window.ethereum.on('chainChanged', handleChainChanged)

            return { success: true, account: accounts[0] }
        } catch (error) {
            throw error
        } finally {
            isConnecting.value = false
        }
    }

    async function switchToGanache() {
        if (!window.ethereum) return

        const ganacheChainId = '0x539'

        try {
            await window.ethereum.request({
                method: 'wallet_switchEthereumChain',
                params: [{ chainId: ganacheChainId }]
            })
        } catch (switchError) {
            if (switchError.code === 4902) {
                try {
                    await window.ethereum.request({
                        method: 'wallet_addEthereumChain',
                        params: [{
                            chainId: ganacheChainId,
                            chainName: 'Ganache Local',
                            nativeCurrency: {
                                name: 'ETH',
                                symbol: 'ETH',
                                decimals: 18
                            },
                            rpcUrls: [GANACHE_RPC_URL]
                        }]
                    })
                } catch (addError) {
                    throw new Error('添加Ganache网络失败: ' + addError.message)
                }
            } else {
                throw new Error('切换网络失败: ' + switchError.message)
            }
        }
    }

    function handleAccountsChanged(accounts) {
        if (accounts.length === 0) {
            disconnectWallet()
        } else {
            account.value = accounts[0]
            updateBalance()
        }
    }

    function handleChainChanged() {
        window.location.reload()
    }

    async function updateBalance() {
        if (!provider.value || !account.value) return
        try {
            const rawBalance = await provider.value.getBalance(account.value)
            balance.value = rawBalance.toString()
        } catch (error) {
            console.error('获取余额失败:', error)
        }
    }

    function disconnectWallet() {
        account.value = ''
        chainId.value = null
        balance.value = '0'
        contract.value = null
        provider.value = null
        signer.value = null

        if (window.ethereum) {
            window.ethereum.removeListener('accountsChanged', handleAccountsChanged)
            window.ethereum.removeListener('chainChanged', handleChainChanged)
        }
    }

    async function createOrderOnChain(amount, priceWei) {
        if (!contract.value) throw new Error('钱包未连接')

        try {
            const tx = await contract.value.createOrder(
                ethers.utils.parseUnits(amount.toString(), 0),
                priceWei,
                {
                    gasLimit: 3000000
                }
            )
            const receipt = await tx.wait(1)
            const orderCreatedEvent = receipt.events?.find((event) => event.event === 'OrderCreated')
            const orderIdOnChain = orderCreatedEvent?.args?.orderId?.toString()

            if (!orderIdOnChain) {
                throw new Error('未能从交易回执中解析链上订单ID')
            }

            return {
                orderIdOnChain,
                txHash: receipt.transactionHash,
                blockNumber: receipt.blockNumber,
                gasUsed: receipt.gasUsed.toString()
            }
        } catch (error) {
            if (error.code === 4001 || error.code === 'ACTION_REJECTED') {
                throw new Error('用户取消了交易')
            } else if (error.code === -32002) {
                throw new Error('交易已在处理中，请稍候')
            } else {
                throw new Error('链上交易失败: ' + (error.reason || error.message))
            }
        }
    }

    async function buyEnergyOnChain(orderId, amount, priceWei, directPaymentWei = priceWei) {
        if (!contract.value) throw new Error('钱包未连接')

        try {
            const tx = await contract.value.buyEnergy(
                orderId,
                ethers.utils.parseUnits(amount.toString(), 0),
                {
                    value: directPaymentWei,
                    gasLimit: 3000000
                }
            )
            const receipt = await tx.wait(1)
            return {
                txHash: receipt.transactionHash,
                blockNumber: receipt.blockNumber,
                gasUsed: receipt.gasUsed.toString()
            }
        } catch (error) {
            if (error.code === 4001 || error.code === 'ACTION_REJECTED') {
                throw new Error('用户取消了交易')
            } else if (error.code === -32002) {
                throw new Error('交易已在处理中，请稍候')
            } else if (error.message && error.message.includes('insufficient funds')) {
                throw new Error('账户余额不足')
            } else if ((error.reason || error.message || '').includes('Insufficient frozen balance')) {
                throw new Error('冻结余额不足，请先在钱包页面冻结足够金额')
            } else {
                throw new Error('链上交易失败: ' + (error.reason || error.message))
            }
        }
    }

    async function cancelOrderOnChain(orderId) {
        if (!contract.value) throw new Error('钱包未连接')

        try {
            const tx = await contract.value.cancelOrder(orderId, {
                gasLimit: 3000000
            })
            const receipt = await tx.wait(1)
            return {
                txHash: receipt.transactionHash,
                blockNumber: receipt.blockNumber,
                gasUsed: receipt.gasUsed.toString()
            }
        } catch (error) {
            if (error.code === 4001 || error.code === 'ACTION_REJECTED') {
                throw new Error('用户取消了交易')
            } else if (error.code === -32002) {
                throw new Error('交易已在处理中，请稍候')
            } else {
                throw new Error('链上交易失败: ' + (error.reason || error.message))
            }
        }
    }

    async function depositOnChain(amountEth) {
        if (!contract.value) throw new Error('钱包未连接')

        try {
            const tx = await contract.value.deposit({
                value: ethers.utils.parseEther(amountEth.toString()),
                gasLimit: 3000000
            })
            const receipt = await tx.wait(1)
            return {
                txHash: receipt.transactionHash,
                blockNumber: receipt.blockNumber,
                gasUsed: receipt.gasUsed.toString()
            }
        } catch (error) {
            if (error.code === 4001 || error.code === 'ACTION_REJECTED') {
                throw new Error('用户取消了交易')
            } else if (error.code === -32002) {
                throw new Error('交易已在处理中，请稍候')
            } else if (error.message && error.message.includes('insufficient funds')) {
                throw new Error('账户余额不足')
            } else {
                throw new Error('链上交易失败: ' + (error.reason || error.message))
            }
        }
    }

    async function freezeBalanceOnChain(amountEth) {
        if (!contract.value) throw new Error('钱包未连接')

        try {
            const amountWei = ethers.utils.parseEther(amountEth.toString())
            const tx = await contract.value.freezeBalance(amountWei, {
                gasLimit: 3000000
            })
            const receipt = await tx.wait(1)
            return {
                txHash: receipt.transactionHash,
                blockNumber: receipt.blockNumber,
                gasUsed: receipt.gasUsed.toString()
            }
        } catch (error) {
            if (error.code === 4001 || error.code === 'ACTION_REJECTED') {
                throw new Error('用户取消了交易')
            } else if (error.code === -32002) {
                throw new Error('交易已在处理中，请稍候')
            } else if ((error.reason || '').includes('Insufficient available balance')) {
                throw new Error('合约可用余额不足')
            } else {
                throw new Error('链上交易失败: ' + (error.reason || error.message))
            }
        }
    }

    async function unfreezeBalanceOnChain(amountEth) {
        if (!contract.value) throw new Error('钱包未连接')

        try {
            const amountWei = ethers.utils.parseEther(amountEth.toString())
            const tx = await contract.value.unfreezeBalance(amountWei, {
                gasLimit: 3000000
            })
            const receipt = await tx.wait(1)
            return {
                txHash: receipt.transactionHash,
                blockNumber: receipt.blockNumber,
                gasUsed: receipt.gasUsed.toString()
            }
        } catch (error) {
            if (error.code === 4001 || error.code === 'ACTION_REJECTED') {
                throw new Error('用户取消了交易')
            } else if (error.code === -32002) {
                throw new Error('交易已在处理中，请稍候')
            } else if ((error.reason || '').includes('Insufficient frozen balance')) {
                throw new Error('冻结余额不足')
            } else {
                throw new Error('链上交易失败: ' + (error.reason || error.message))
            }
        }
    }

    async function getContractBalanceRaw(userAddress) {
        if (!contract.value) throw new Error('钱包未连接')

        try {
            const [available, frozen] = await contract.value.getBalance(userAddress)
            return {
                available,
                frozen
            }
        } catch (error) {
            console.error('获取合约原始余额失败:', error)
            throw new Error('获取合约余额失败: ' + (error.message || '未知错误'))
        }
    }

    async function withdrawOnChain(amountEth) {
        if (!contract.value) throw new Error('钱包未连接')

        try {
            const amountWei = ethers.utils.parseEther(amountEth.toString())
            const tx = await contract.value.withdraw(amountWei, {
                gasLimit: 3000000
            })
            const receipt = await tx.wait(1)
            return {
                txHash: receipt.transactionHash,
                blockNumber: receipt.blockNumber,
                gasUsed: receipt.gasUsed.toString()
            }
        } catch (error) {
            if (error.code === 4001 || error.code === 'ACTION_REJECTED') {
                throw new Error('用户取消了交易')
            } else if (error.code === -32002) {
                throw new Error('交易已在处理中，请稍候')
            } else if (error.reason && error.reason.includes('Insufficient available balance')) {
                throw new Error('合约余额不足')
            } else {
                throw new Error('链上交易失败: ' + (error.reason || error.message))
            }
        }
    }

    async function getContractBalance(userAddress) {
        if (!contract.value) throw new Error('钱包未连接')

        try {
            const [available, frozen] = await contract.value.getBalance(userAddress)
            return {
                available: ethers.utils.formatEther(available),
                frozen: ethers.utils.formatEther(frozen)
            }
        } catch (error) {
            console.error('获取合约余额失败:', error)
            throw new Error('获取合约余额失败: ' + (error.message || '未知错误'))
        }
    }

    async function signMessage(message) {
        if (!signer.value) throw new Error('钱包未连接')
        try {
            const signature = await signer.value.signMessage(message)
            return signature
        } catch (error) {
            if (error.code === 4001 || error.code === 'ACTION_REJECTED') {
                throw new Error('用户取消了签名')
            } else {
                throw new Error('签名失败: ' + (error.message || '未知错误'))
            }
        }
    }

    // 清理函数
    function cleanup() {
        disconnectWallet()
    }

    return {
        account,
        chainId,
        balance,
        isConnecting,
        isConnected,
        contract,
        shortAddress,
        balanceInEth,
        connectWallet,
        disconnectWallet,
        switchToGanache,
        updateBalance,
        createOrderOnChain,
        buyEnergyOnChain,
        cancelOrderOnChain,
        depositOnChain,
        freezeBalanceOnChain,
        unfreezeBalanceOnChain,
        withdrawOnChain,
        getContractBalance,
        getContractBalanceRaw,
        signMessage,
        cleanup
    }
})
