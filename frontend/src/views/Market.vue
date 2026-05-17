<template>
    <div>
        <div class="page-header">
            <h2 class="page-title">交易市场</h2>
            <p class="page-desc">浏览当前开放的电量出售挂单</p>
        </div>
        
        <div class="stat-cards">
            <div class="stat-card">
                <div class="stat-icon blue">
                    <el-icon><Document /></el-icon>
                </div>
                <div class="stat-value">{{ openOrders.length }}</div>
                <div class="stat-label">开放挂单</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon green">
                    <el-icon><Lightning /></el-icon>
                </div>
                <div class="stat-value">{{ totalAmount.toFixed(2) }}</div>
                <div class="stat-label">可用电量 (kWh)</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon orange">
                    <el-icon><Wallet /></el-icon>
                </div>
                <div class="stat-value">{{ walletStore.isConnected ? walletStore.balanceInEth : userStore.balance.toFixed(2) }}</div>
                <div class="stat-label">{{ walletStore.isConnected ? '钱包余额 (ETH)' : '账户余额 (ETH)' }}</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon purple">
                    <el-icon><TrendCharts /></el-icon>
                </div>
                <div class="stat-value">{{ marketAvgPrice.toFixed(4) }}</div>
                <div class="stat-label">市场均价 (ETH/kWh)</div>
            </div>
        </div>

        <el-alert 
            v-if="!walletStore.isConnected && userStore.role === 2"
            title="提示：连接MetaMask钱包后可以进行链上交易" 
            type="info" 
            :closable="false"
            style="margin-bottom: 20px;"
        >
            <template #default>
                <el-button size="small" type="primary" @click="goToWallet">前往连接</el-button>
            </template>
        </el-alert>
        
        <div class="card-container">
            <div class="card-title">开放挂单列表</div>
            
            <div class="filter-bar" style="margin-bottom: 20px; display: flex; gap: 15px; align-items: center;">
                <el-input
                    v-model="searchQuery"
                    placeholder="搜索卖家..."
                    style="width: 250px"
                    clearable
                >
                    <template #prefix>
                        <el-icon><Search /></el-icon>
                    </template>
                </el-input>
                
                <el-select v-model="sortOrder" placeholder="排序方式" style="width: 180px">
                    <el-option label="时间 (由新到旧)" value="timeDesc" />
                    <el-option label="时间 (由旧到新)" value="timeAsc" />
                    <el-option label="价格 (由低到高)" value="priceAsc" />
                    <el-option label="价格 (由高到低)" value="priceDesc" />
                    <el-option label="电量 (由多到少)" value="amountDesc" />
                    <el-option label="电量 (由少到多)" value="amountAsc" />
                </el-select>

                <el-radio-group v-model="filterRole" size="default">
                    <el-radio-button label="all">全部</el-radio-button>
                    <el-radio-button label="large">大额订单 (>50kWh)</el-radio-button>
                    <el-radio-button label="small">小额订单 (≤50kWh)</el-radio-button>
                </el-radio-group>
            </div>
            
            <el-table :data="filteredAndSortedOrders" style="width: 100%" v-loading="loading">
                <el-table-column prop="sellerName" label="卖家" width="120" />
                <el-table-column label="卖家地址" width="180">
                    <template #default="{ row }">
                        {{ formatAddress(row.sellerAddress) }}
                    </template>
                </el-table-column>
                <el-table-column prop="amount" label="电量 (kWh)" width="120">
                    <template #default="{ row }">
                        {{ row.amount.toFixed(2) }}
                    </template>
                </el-table-column>
                <el-table-column prop="price" label="单价 (Wei/kWh)" width="180">
                    <template #default="{ row }">
                        {{ formatPrice(row.price) }}
                    </template>
                </el-table-column>
                <el-table-column prop="statusName" label="状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="getStatusType(row.status)">
                            {{ row.statusName }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createdTime" label="发布时间" width="180">
                    <template #default="{ row }">
                        {{ formatTime(row.createdTime) }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" fixed="right" width="150">
                    <template #default="{ row }">
                        <el-button 
                            type="primary" 
                            size="small"
                            @click="openBuyDialog(row)"
                            :disabled="userStore.role !== 2"
                        >
                            购买
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>
        
        <el-dialog v-model="buyDialogVisible" title="购买电量" width="500px">
            <el-form :model="buyForm" :rules="buyRules" ref="buyFormRef" label-width="100px">
                <el-form-item label="卖家">
                    <el-input :value="currentOrder?.sellerName" disabled />
                </el-form-item>
                <el-form-item label="可用电量">
                    <el-input :value="currentOrder?.amount?.toFixed(2) + ' kWh'" disabled />
                </el-form-item>
                <el-form-item label="单价">
                    <el-input :value="formatPrice(currentOrder?.price)" disabled />
                </el-form-item>
                <el-form-item label="购买量" prop="amount">
                    <el-input-number 
                        v-model="buyForm.amount" 
                        :min="1" 
                        :max="currentOrder?.amount || 100"
                        :precision="0"
                        :step="1"
                        style="width: 100%"
                    />
                </el-form-item>
                <el-form-item label="总价">
                    <el-input :value="totalPriceDisplay" disabled />
                </el-form-item>
                <el-form-item label="交易方式">
                    <el-radio-group v-model="buyForm.tradeMode">
                        <el-radio :label="1">链上交易 (MetaMask)</el-radio>
                        <el-radio :label="0">链下交易 (模拟)</el-radio>
                    </el-radio-group>
                </el-form-item>
                <template v-if="buyForm.tradeMode === 1 && walletStore.isConnected">
                    <el-form-item label="支付方式">
                        <el-radio-group v-model="buyForm.paymentMode">
                            <el-radio label="wallet">钱包全额支付</el-radio>
                            <el-radio label="frozenFirst">冻结余额优先</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-alert
                        type="info"
                        :closable="false"
                        style="margin-top: 10px;"
                    >
                        <template #title>
                            支付说明
                        </template>
                        当前冻结余额：{{ frozenBalanceDisplay }} ETH；
                        本次冻结抵扣：{{ frozenUsedDisplay }} ETH；
                        钱包需支付：{{ directPaymentDisplay }} ETH
                    </el-alert>
                    <div
                        v-if="buyForm.paymentMode === 'frozenFirst'"
                        style="margin-top: 8px; color: #909399; font-size: 13px;"
                    >
                        若冻结余额不足，可先前往钱包页面执行“存款 -> 冻结余额”。
                    </div>
                </template>
                <el-alert 
                    v-if="buyForm.tradeMode === 1 && !walletStore.isConnected"
                    title="请先连接MetaMask钱包"
                    type="warning"
                    :closable="false"
                    style="margin-top: 10px;"
                />
            </el-form>
            <template #footer>
                <el-button @click="buyDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleBuy" :loading="buyLoading">
                    确认购买
                </el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useWalletStore } from '../stores/wallet'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ethers } from 'ethers'

const router = useRouter()
const userStore = useUserStore()
const walletStore = useWalletStore()

const loading = ref(false)
const openOrders = ref([])
const buyDialogVisible = ref(false)
const buyLoading = ref(false)
const currentOrder = ref(null)
const buyFormRef = ref(null)

// 搜索、筛选和排序
const searchQuery = ref('')
const sortOrder = ref('timeDesc')
const filterRole = ref('all')

const filteredAndSortedOrders = computed(() => {
    let result = [...openOrders.value]
    
    // 搜索
    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        result = result.filter(order => 
            (order.sellerName && order.sellerName.toLowerCase().includes(query)) ||
            (order.sellerAddress && order.sellerAddress.toLowerCase().includes(query))
        )
    }
    
    // 筛选
    if (filterRole.value === 'large') {
        result = result.filter(order => order.amount > 50)
    } else if (filterRole.value === 'small') {
        result = result.filter(order => order.amount <= 50)
    }
    
    // 排序
    result.sort((a, b) => {
        switch (sortOrder.value) {
            case 'timeDesc': return new Date(b.createdTime) - new Date(a.createdTime)
            case 'timeAsc': return new Date(a.createdTime) - new Date(b.createdTime)
            case 'priceAsc': return a.price - b.price
            case 'priceDesc': return b.price - a.price
            case 'amountDesc': return b.amount - a.amount
            case 'amountAsc': return a.amount - b.amount
            default: return 0
        }
    })
    
    return result
})

const buyForm = reactive({
    amount: 1,
    tradeMode: walletStore.isConnected ? 1 : 0,
    paymentMode: 'wallet'
})

const buyRules = {
    amount: [
        { required: true, message: '请输入购买量', trigger: 'blur' }
    ]
}

const contractBalanceWei = reactive({
    available: '0',
    frozen: '0'
})

const totalPriceWei = computed(() => {
    if (!currentOrder.value || !buyForm.amount) {
        return ethers.BigNumber.from(0)
    }

    return ethers.BigNumber.from(currentOrder.value.price.toString()).mul(buyForm.amount)
})

const totalPriceDisplay = computed(() => {
    return `${parseFloat(ethers.utils.formatEther(totalPriceWei.value)).toFixed(6)} ETH`
})

const frozenUsedWei = computed(() => {
    if (buyForm.tradeMode !== 1 || buyForm.paymentMode !== 'frozenFirst') {
        return ethers.BigNumber.from(0)
    }

    const frozenWei = ethers.BigNumber.from(contractBalanceWei.frozen)
    return frozenWei.gte(totalPriceWei.value) ? totalPriceWei.value : frozenWei
})

const directPaymentWei = computed(() => {
    return totalPriceWei.value.sub(frozenUsedWei.value)
})

const frozenBalanceDisplay = computed(() => {
    return parseFloat(ethers.utils.formatEther(contractBalanceWei.frozen)).toFixed(6)
})

const frozenUsedDisplay = computed(() => {
    return parseFloat(ethers.utils.formatEther(frozenUsedWei.value)).toFixed(6)
})

const directPaymentDisplay = computed(() => {
    return parseFloat(ethers.utils.formatEther(directPaymentWei.value)).toFixed(6)
})

const totalAmount = computed(() => {
    return openOrders.value.reduce((sum, order) => sum + (order.amount || 0), 0)
})

const marketAvgPrice = computed(() => {
    if (openOrders.value.length === 0) return 0
    const totalP = openOrders.value.reduce((sum, order) => sum + (order.price / 1e18), 0)
    return totalP / openOrders.value.length
})

const fetchOpenOrders = async () => {
    loading.value = true
    try {
        const response = await axios.get('/api/order/open')
        if (response.data.code === 200) {
            openOrders.value = response.data.data || []
        }
    } catch (error) {
        console.error('获取挂单失败:', error)
    } finally {
        loading.value = false
    }
}

const refreshContractFunding = async () => {
    if (!walletStore.isConnected) {
        contractBalanceWei.available = '0'
        contractBalanceWei.frozen = '0'
        return
    }

    const balance = await walletStore.getContractBalanceRaw(walletStore.account)
    contractBalanceWei.available = balance.available.toString()
    contractBalanceWei.frozen = balance.frozen.toString()
}

const openBuyDialog = async (order) => {
    currentOrder.value = order
    buyForm.amount = 1
    buyForm.tradeMode = walletStore.isConnected ? 1 : 0
    buyForm.paymentMode = 'wallet'
    if (walletStore.isConnected) {
        await refreshContractFunding()
    }
    buyDialogVisible.value = true
}

const handleBuy = async () => {
    if (!buyFormRef.value) return
    
    await buyFormRef.value.validate(async (valid) => {
        if (valid) {
            if (buyForm.tradeMode === 1) {
                await handleOnChainBuy()
            } else {
                await handleOffChainBuy()
            }
        }
    })
}

const handleOnChainBuy = async () => {
    if (!walletStore.isConnected) {
        ElMessage.warning('请先连接MetaMask钱包')
        return
    }

    if (!currentOrder.value?.orderIdOnChain) {
        ElMessage.error('当前订单缺少链上订单号，无法发起链上交易')
        return
    }

    buyLoading.value = true
    try {
        const result = await walletStore.buyEnergyOnChain(
            currentOrder.value.orderIdOnChain,
            buyForm.amount,
            totalPriceWei.value.toString(),
            directPaymentWei.value.toString()
        )

        const response = await axios.post('/api/trade/buy', {
            orderId: currentOrder.value.id,
            amount: buyForm.amount
        }, {
            params: {
                buyerAddress: walletStore.account,
                txHash: result.txHash,
                blockNumber: result.blockNumber
            }
        })

        if (response.data.code === 200) {
            ElMessage.success(`购买成功！交易哈希: ${result.txHash.substring(0, 10)}...`)
            buyDialogVisible.value = false
            await refreshContractFunding()
            fetchOpenOrders()
        } else {
            ElMessage.error(response.data.message || '购买失败')
        }
    } catch (error) {
        if (error.code === 4001 || error.code === 'ACTION_REJECTED') {
            ElMessage.warning('用户取消了交易')
        } else {
            ElMessage.error('链上交易失败: ' + (error.reason || error.message))
        }
    } finally {
        buyLoading.value = false
    }
}

const handleOffChainBuy = async () => {
    buyLoading.value = true
    try {
        const response = await axios.post('/api/trade/buy', {
            orderId: currentOrder.value.id,
            amount: buyForm.amount
        }, {
            params: {
                buyerAddress: userStore.blockchainAddress
            }
        })
        
        if (response.data.code === 200) {
            ElMessage.success('购买成功！')
            buyDialogVisible.value = false
            fetchOpenOrders()
        } else {
            ElMessage.error(response.data.message || '购买失败')
        }
    } catch (error) {
        ElMessage.error(error.response?.data?.message || '购买失败')
    } finally {
        buyLoading.value = false
    }
}

const goToWallet = () => {
    router.push('/dashboard/wallet')
}

const formatAddress = (address) => {
    if (!address) return ''
    return `${address.substring(0, 6)}...${address.substring(address.length - 4)}`
}

const formatPrice = (price) => {
    if (!price) return '0'
    return (price / 1e18).toFixed(6) + ' ETH'
}

const formatTime = (time) => {
    if (!time) return ''
    return new Date(time).toLocaleString('zh-CN')
}

const getStatusType = (status) => {
    switch (status) {
        case 0: return 'success'
        case 1: return 'info'
        case 2: return 'danger'
        default: return 'info'
    }
}

onMounted(() => {
    fetchOpenOrders()
})
</script>
