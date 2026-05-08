<template>
    <div>
        <div class="page-header">
            <h2 class="page-title">全网监控</h2>
            <p class="page-desc">实时监控区块链电力交易，自动预警异常交易</p>
        </div>

        <div class="stat-cards">
            <div class="stat-card">
                <div class="stat-icon blue">
                    <el-icon><Monitor /></el-icon>
                </div>
                <div class="stat-value">{{ transactions.length }}</div>
                <div class="stat-label">总交易笔数</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon green">
                    <el-icon><Lightning /></el-icon>
                </div>
                <div class="stat-value">{{ totalEnergy.toFixed(2) }}</div>
                <div class="stat-label">全网成交量 (kWh)</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon red">
                    <el-icon><Warning /></el-icon>
                </div>
                <div class="stat-value">{{ anomalies.length }}</div>
                <div class="stat-label">异常预警</div>
            </div>
        </div>

        <el-alert
            v-if="anomalies.length > 0"
            title="异常交易预警"
            type="warning"
            :description="`检测到 ${anomalies.length} 笔潜在异常交易（价格偏离市场均价过大或单笔电量过大）。`"
            show-icon
            style="margin-bottom: 20px;"
        />

        <div class="card-container">
            <div class="card-title">全网交易流水</div>
            
            <el-table :data="transactions" style="width: 100%" v-loading="loading">
                <el-table-column label="交易哈希" width="180">
                    <template #default="{ row }">
                        <el-tooltip :content="row.txHash" placement="top">
                            <span class="hash-link">{{ formatHash(row.txHash) }}</span>
                        </el-tooltip>
                    </template>
                </el-table-column>
                <el-table-column label="买家" width="150">
                    <template #default="{ row }">
                        <span>{{ formatAddress(row.buyerAddress) }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="卖家" width="150">
                    <template #default="{ row }">
                        <span>{{ formatAddress(row.sellerAddress) }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="amount" label="电量 (kWh)" width="120">
                    <template #default="{ row }">
                        <span :class="{ 'anomaly-text': isEnergyAnomaly(row.amount) }">
                            {{ row.amount.toFixed(2) }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column label="总价 (ETH)" width="150">
                    <template #default="{ row }">
                        <span>{{ (row.totalPrice / 1e18).toFixed(6) }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="平均单价" width="150">
                    <template #default="{ row }">
                        <span :class="{ 'anomaly-text': isPriceAnomaly(row.totalPrice / row.amount) }">
                            {{ (row.totalPrice / row.amount / 1e18).toFixed(10) }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column label="交易时间" width="180">
                    <template #default="{ row }">
                        {{ formatDate(row.timestamp) }}
                    </template>
                </el-table-column>
                <el-table-column label="风险等级" width="100">
                    <template #default="{ row }">
                        <el-tag :type="getRiskLevel(row).type">
                            {{ getRiskLevel(row).label }}
                        </el-tag>
                    </template>
                </el-table-column>
            </el-table>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const transactions = ref([])
const loading = ref(false)
const avgMarketPrice = ref(0)

const fetchTransactions = async () => {
    loading.value = true
    try {
        const response = await axios.get('/api/trade/all')
        if (response.data.code === 200) {
            transactions.value = response.data.data || []
            calculateMarketAvg()
        }
    } catch (error) {
        ElMessage.error('获取交易历史失败')
    } finally {
        loading.value = false
    }
}

const totalEnergy = computed(() => {
    return transactions.value.reduce((sum, tx) => sum + tx.amount, 0)
})

const calculateMarketAvg = () => {
    if (transactions.value.length === 0) return
    const totalP = transactions.value.reduce((sum, tx) => sum + (tx.totalPrice / tx.amount), 0)
    avgMarketPrice.value = totalP / transactions.value.length
}

const isEnergyAnomaly = (amount) => amount > 500 // 假设单笔超过500kWh为大额异常
const isPriceAnomaly = (price) => {
    if (avgMarketPrice.value === 0) return false
    return Math.abs(price - avgMarketPrice.value) / avgMarketPrice.value > 0.5 // 偏离均价50%以上
}

const anomalies = computed(() => {
    return transactions.value.filter(tx => isEnergyAnomaly(tx.amount) || isPriceAnomaly(tx.totalPrice / tx.amount))
})

const getRiskLevel = (tx) => {
    const price = tx.totalPrice / tx.amount
    if (isEnergyAnomaly(tx.amount) && isPriceAnomaly(price)) return { label: '极高', type: 'danger' }
    if (isEnergyAnomaly(tx.amount) || isPriceAnomaly(price)) return { label: '中等', type: 'warning' }
    return { label: '正常', type: 'success' }
}

const formatHash = (hash) => hash ? `${hash.slice(0, 10)}...` : ''
const formatAddress = (addr) => addr ? `${addr.slice(0, 6)}...${addr.slice(-4)}` : ''
const formatDate = (timestamp) => {
    return new Date(timestamp * 1000).toLocaleString()
}

onMounted(fetchTransactions)
</script>

<style scoped>
.page-header { margin-bottom: 24px; }
.page-title { font-size: 24px; color: #303133; margin: 0 0 8px 0; }
.page-desc { font-size: 14px; color: #909399; margin: 0; }
.stat-cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 24px; }
.stat-card { background: white; padding: 20px; border-radius: 12px; display: flex; flex-direction: column; align-items: center; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.stat-icon { font-size: 24px; width: 48px; height: 48px; border-radius: 24px; display: flex; align-items: center; justify-content: center; margin-bottom: 12px; }
.stat-icon.blue { background: #ecf5ff; color: #409eff; }
.stat-icon.green { background: #f0f9eb; color: #67c23a; }
.stat-icon.red { background: #fef0f0; color: #f56c6c; }
.stat-value { font-size: 24px; font-weight: bold; color: #303133; margin-bottom: 4px; }
.stat-label { font-size: 14px; color: #909399; }
.card-container { background: white; padding: 24px; border-radius: 12px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.card-title { font-size: 18px; font-weight: bold; color: #303133; margin-bottom: 20px; }
.hash-link { color: #409eff; cursor: pointer; font-family: monospace; }
.anomaly-text { color: #f56c6c; font-weight: bold; }
</style>
