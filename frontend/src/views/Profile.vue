<template>
    <div>
        <div class="page-header">
            <h2 class="page-title">个人中心</h2>
            <p class="page-desc">管理您的账户信息</p>
        </div>
        
        <div class="card-container" style="max-width: 800px;">
            <div class="card-title">账户信息</div>
            
            <el-descriptions :column="2" border>
                <el-descriptions-item label="用户名">
                    {{ userStore.username }}
                </el-descriptions-item>
                <el-descriptions-item label="角色">
                    <el-tag :type="roleTagType">
                        {{ userStore.roleName }}
                    </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="区块链地址" :span="2">
                    <el-input :value="userStore.blockchainAddress" readonly>
                        <template #append>
                            <el-button @click="copyAddress">
                                复制
                            </el-button>
                        </template>
                    </el-input>
                </el-descriptions-item>
                <el-descriptions-item label="账户余额">
                    <span style="font-size: 20px; font-weight: 600; color: #667eea;">
                        {{ userStore.balance.toFixed(2) }} ETH
                    </span>
                </el-descriptions-item>
                <el-descriptions-item label="信用评分">
                    <el-rate
                        v-model="trustRate"
                        disabled
                        show-score
                        text-color="#ff9900"
                        score-template="{value} 分"
                    />
                </el-descriptions-item>
                <el-descriptions-item label="账户状态">
                    <el-tag :type="userStore.status === 0 ? 'success' : 'danger'">
                        {{ userStore.status === 0 ? '正常活跃' : '已被冻结' }}
                    </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="注册时间">
                    {{ formatTime(userStore.userInfo?.createdTime) }}
                </el-descriptions-item>
            </el-descriptions>
        </div>

        <div class="card-container" style="max-width: 800px; margin-top: 20px;">
            <div class="card-title">环保贡献 (低碳电力)</div>
            <div class="carbon-stats" style="display: flex; justify-content: space-around; padding: 20px 0;">
                <div class="carbon-item" style="text-align: center;">
                    <div class="carbon-icon" style="font-size: 32px; margin-bottom: 10px;">🌳</div>
                    <div class="carbon-value" style="font-size: 24px; font-weight: bold; color: #4caf50;">{{ carbonSaved.toFixed(2) }} kg</div>
                    <div class="carbon-label" style="color: #909399; font-size: 14px;">累计碳减排</div>
                </div>
                <div class="carbon-item" style="text-align: center;">
                    <div class="carbon-icon" style="font-size: 32px; margin-bottom: 10px;">🏠</div>
                    <div class="carbon-value" style="font-size: 24px; font-weight: bold; color: #2196f3;">{{ (carbonSaved / 0.1).toFixed(0) }} 天</div>
                    <div class="carbon-label" style="color: #909399; font-size: 14px;">相当于家庭用电</div>
                </div>
                <div class="carbon-item" style="text-align: center;">
                    <div class="carbon-icon" style="font-size: 32px; margin-bottom: 10px;">🌱</div>
                    <div class="carbon-value" style="font-size: 24px; font-weight: bold; color: #ff9800;">{{ (carbonSaved / 20).toFixed(1) }} 棵</div>
                    <div class="carbon-label" style="color: #909399; font-size: 14px;">相当于种植树木</div>
                </div>
            </div>
            <el-alert
                title="碳减排计算说明：基于 P2P 交易电量，每 1 kWh 绿色电力约减少 0.6kg 碳排放。"
                type="success"
                :closable="false"
                show-icon
                style="margin-top: 10px;"
            />
        </div>

        <div class="card-container" style="max-width: 800px; margin-top: 20px;">
            <div class="card-title">MetaMask钱包</div>
            <el-descriptions :column="2" border v-if="walletStore.isConnected">
                <el-descriptions-item label="钱包地址" :span="2">
                    <div style="display: flex; align-items: center; gap: 10px;">
                        <code>{{ walletStore.account }}</code>
                        <el-button size="small" @click="copyWalletAddress">复制</el-button>
                    </div>
                </el-descriptions-item>
                <el-descriptions-item label="钱包余额">
                    <span style="font-weight: 600; color: #667eea;">
                        {{ walletStore.balanceInEth }} ETH
                    </span>
                </el-descriptions-item>
                <el-descriptions-item label="连接状态">
                    <el-tag type="success">已连接</el-tag>
                </el-descriptions-item>
            </el-descriptions>
            <div v-else class="wallet-not-connected">
                <p>钱包未连接</p>
                <el-button type="primary" @click="goToWallet">前往钱包管理</el-button>
            </div>
        </div>
        
        <div class="card-container" style="max-width: 800px; margin-top: 20px;">
            <div class="card-title">功能说明</div>
            
            <el-row :gutter="20">
                <el-col :span="12">
                    <div class="feature-card" v-if="userStore.role === 1">
                        <div class="feature-icon">☀️</div>
                        <h3>产电用户</h3>
                        <ul>
                            <li>发布电量出售挂单</li>
                            <li>管理自己的挂单</li>
                            <li>查看交易历史</li>
                            <li>接收交易款项</li>
                        </ul>
                    </div>
                    <div class="feature-card" v-else-if="userStore.role === 2">
                        <div class="feature-icon">🔌</div>
                        <h3>用电用户</h3>
                        <ul>
                            <li>浏览开放的挂单</li>
                            <li>购买所需电量</li>
                            <li>查看交易历史</li>
                            <li>管理账户余额</li>
                        </ul>
                    </div>
                    <div class="feature-card" v-else>
                        <div class="feature-icon">👤</div>
                        <h3>普通用户</h3>
                        <ul>
                            <li>浏览开放的挂单</li>
                            <li>查看交易历史</li>
                            <li>升级为产电或用电用户</li>
                        </ul>
                    </div>
                </el-col>
                <el-col :span="12">
                    <div class="feature-card">
                        <div class="feature-icon">🔗</div>
                        <h3>区块链特性</h3>
                        <ul>
                            <li>交易记录不可篡改</li>
                            <li>去中心化交易</li>
                            <li>智能合约自动执行</li>
                            <li>透明可追溯</li>
                        </ul>
                    </div>
                </el-col>
            </el-row>
        </div>
    </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useWalletStore } from '../stores/wallet'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const userStore = useUserStore()
const walletStore = useWalletStore()

const carbonSaved = ref(0)
const transactions = ref([])

const trustRate = computed(() => (userStore.trustScore || 100) / 20)

const fetchUserHistory = async () => {
    try {
        const response = await axios.get(`/api/trade/history/${userStore.blockchainAddress}`)
        if (response.data.code === 200) {
            transactions.value = response.data.data || []
            // 计算累计交易电量 (买入 + 卖出)
            const totalKwh = transactions.value.reduce((sum, tx) => sum + (tx.amount || 0), 0)
            // 假设每 1kWh 节约 0.6kg 碳排放
            carbonSaved.value = totalKwh * 0.6
        }
    } catch (error) {
        console.error('获取历史记录失败:', error)
    }
}

onMounted(() => {
    fetchUserHistory()
})

const roleTagType = computed(() => {
    switch (userStore.role) {
        case 1: return 'success'
        case 2: return 'warning'
        default: return 'info'
    }
})

const copyAddress = async () => {
    try {
        await navigator.clipboard.writeText(userStore.blockchainAddress)
        ElMessage.success('地址已复制到剪贴板')
    } catch (error) {
        ElMessage.error('复制失败')
    }
}

const copyWalletAddress = async () => {
    try {
        await navigator.clipboard.writeText(walletStore.account)
        ElMessage.success('钱包地址已复制到剪贴板')
    } catch (error) {
        ElMessage.error('复制失败')
    }
}

const goToWallet = () => {
    router.push('/dashboard/wallet')
}

const formatTime = (time) => {
    if (!time) return '未知'
    return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.feature-card {
    background: rgba(102, 126, 234, 0.05);
    border-radius: 12px;
    padding: 25px;
    text-align: center;
}

.feature-icon {
    font-size: 48px;
    margin-bottom: 15px;
}

.feature-card h3 {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 15px;
}

.feature-card ul {
    text-align: left;
    list-style: none;
    padding: 0;
}

.feature-card li {
    padding: 8px 0;
    color: #606266;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.feature-card li:last-child {
    border-bottom: none;
}

.wallet-not-connected {
    text-align: center;
    padding: 20px;
    color: #909399;
}

.wallet-not-connected p {
    margin-bottom: 15px;
}
</style>
