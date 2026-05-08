<template>
    <div>
        <div class="page-header">
            <h2 class="page-title">交易历史</h2>
            <p class="page-desc">查看您的所有交易记录</p>
        </div>
        
        <div class="stat-cards">
            <div class="stat-card">
                <div class="stat-icon blue">
                    <el-icon><Tickets /></el-icon>
                </div>
                <div class="stat-value">{{ transactions.length }}</div>
                <div class="stat-label">总交易数</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon green">
                    <el-icon><ShoppingCart /></el-icon>
                </div>
                <div class="stat-value">{{ buyCount }}</div>
                <div class="stat-label">买入次数</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon orange">
                    <el-icon><Sell /></el-icon>
                </div>
                <div class="stat-value">{{ sellCount }}</div>
                <div class="stat-label">卖出次数</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon red">
                    <el-icon><Coin /></el-icon>
                </div>
                <div class="stat-value">{{ totalVolume.toFixed(2) }}</div>
                <div class="stat-label">总交易量 (kWh)</div>
            </div>
        </div>
        
        <div class="card-container">
            <div class="card-title">交易记录</div>
            
            <el-table :data="transactions" style="width: 100%" v-loading="loading">
                <el-table-column prop="txHash" label="交易哈希" width="200">
                    <template #default="{ row }">
                        <el-tooltip :content="row.txHash" placement="top">
                            <span>{{ formatHash(row.txHash) }}</span>
                        </el-tooltip>
                    </template>
                </el-table-column>
                <el-table-column label="类型" width="80">
                    <template #default="{ row }">
                        <el-tag :type="currentAddress && row.buyerAddress === currentAddress ? 'warning' : 'success'" size="small">
                            {{ currentAddress && row.buyerAddress === currentAddress ? '买入' : '卖出' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="sellerName" label="卖家" width="100" />
                <el-table-column prop="buyerName" label="买家" width="100" />
                <el-table-column prop="amount" label="电量 (kWh)" width="120">
                    <template #default="{ row }">
                        {{ row.amount.toFixed(2) }}
                    </template>
                </el-table-column>
                <el-table-column prop="totalPrice" label="总价" width="150">
                    <template #default="{ row }">
                        {{ formatPrice(row.totalPrice) }}
                    </template>
                </el-table-column>
                <el-table-column prop="timeStr" label="交易时间" width="180" />
                <el-table-column label="操作" fixed="right" width="100">
                    <template #default="{ row }">
                        <el-button type="primary" link size="small" @click="showDetail(row)">
                            详情
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>
        
        <el-dialog v-model="detailDialogVisible" title="交易详情" width="500px">
            <el-descriptions :column="1" border>
                <el-descriptions-item label="交易哈希">
                    {{ currentTransaction?.txHash }}
                </el-descriptions-item>
                <el-descriptions-item label="订单ID">
                    {{ currentTransaction?.orderId }}
                </el-descriptions-item>
                <el-descriptions-item label="卖家">
                    {{ currentTransaction?.sellerName }} ({{ formatAddress(currentTransaction?.sellerAddress) }})
                </el-descriptions-item>
                <el-descriptions-item label="买家">
                    {{ currentTransaction?.buyerName }} ({{ formatAddress(currentTransaction?.buyerAddress) }})
                </el-descriptions-item>
                <el-descriptions-item label="电量">
                    {{ currentTransaction?.amount?.toFixed(2) }} kWh
                </el-descriptions-item>
                <el-descriptions-item label="总价">
                    {{ formatPrice(currentTransaction?.totalPrice) }}
                </el-descriptions-item>
                <el-descriptions-item label="交易时间">
                    {{ currentTransaction?.timeStr }}
                </el-descriptions-item>
            </el-descriptions>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { useWalletStore } from '../stores/wallet'
import axios from 'axios'

const userStore = useUserStore()
const walletStore = useWalletStore()

const currentAddress = computed(() => 
    walletStore.isConnected ? walletStore.account : userStore.blockchainAddress
)

const loading = ref(false)
const transactions = ref([])
const detailDialogVisible = ref(false)
const currentTransaction = ref(null)

const buyCount = computed(() => 
    transactions.value.filter(t => t.buyerAddress === currentAddress.value).length
)

const sellCount = computed(() => 
    transactions.value.filter(t => t.sellerAddress === currentAddress.value).length
)

const totalVolume = computed(() => 
    transactions.value.reduce((sum, t) => sum + (t.amount || 0), 0)
)

const fetchTransactions = async () => {
    loading.value = true
    try {
        const response = await axios.get(`/api/trade/history/${currentAddress.value}`)
        if (response.data.code === 200) {
            transactions.value = response.data.data || []
        }
    } catch (error) {
        console.error('获取交易记录失败:', error)
    } finally {
        loading.value = false
    }
}

const showDetail = (transaction) => {
    currentTransaction.value = transaction
    detailDialogVisible.value = true
}

const formatHash = (hash) => {
    if (!hash) return ''
    return `${hash.substring(0, 10)}...${hash.substring(hash.length - 8)}`
}

const formatAddress = (address) => {
    if (!address) return ''
    return `${address.substring(0, 6)}...${address.substring(address.length - 4)}`
}

const formatPrice = (price) => {
    if (!price) return '0'
    return (price / 1e18).toFixed(6) + ' ETH'
}

onMounted(() => {
    fetchTransactions()
})
</script>
