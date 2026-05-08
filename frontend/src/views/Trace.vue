<template>
    <div>
        <div class="page-header">
            <h2 class="page-title">交易溯源查证</h2>
            <p class="page-desc">验证交易真实性，追溯电力流转路径</p>
        </div>
        
        <div class="card-container">
            <div class="card-title">交易查询</div>
            <el-form :inline="true" @submit.prevent="handleSearch">
                <el-form-item label="交易哈希">
                    <el-input 
                        v-model="searchTxHash" 
                        placeholder="请输入交易哈希" 
                        style="width: 400px"
                        clearable
                    >
                        <template #prefix>
                            <el-icon><Link /></el-icon>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="handleSearch" :loading="loading">
                        <el-icon><Search /></el-icon>
                        查询验证
                    </el-button>
                </el-form-item>
            </el-form>
        </div>

        <div v-if="traceResult" class="trace-result">
            <el-row :gutter="20">
                <el-col :span="16">
                    <div class="card-container">
                        <div class="card-title">
                            <el-icon><CircleCheck /></el-icon>
                            验证结果
                            <el-tag :type="traceResult.verified ? 'success' : 'warning'" style="margin-left: 10px;">
                                {{ traceResult.verifyStatus }}
                            </el-tag>
                        </div>
                        
                        <el-descriptions :column="2" border>
                            <el-descriptions-item label="交易哈希" :span="2">
                                <el-text type="primary" class="tx-hash">
                                    {{ traceResult.txHash }}
                                </el-text>
                                <el-button link type="primary" @click="copyToClipboard(traceResult.txHash)">
                                    复制
                                </el-button>
                            </el-descriptions-item>
                            <el-descriptions-item label="验证状态">
                                <el-tag :type="traceResult.verified ? 'success' : 'danger'">
                                    {{ traceResult.verified ? '已验证' : '未验证' }}
                                </el-tag>
                            </el-descriptions-item>
                            <el-descriptions-item label="区块高度">
                                {{ traceResult.blockNumber || '未上链' }}
                            </el-descriptions-item>
                            <el-descriptions-item label="卖家地址">
                                {{ formatAddress(traceResult.sellerAddress) }}
                            </el-descriptions-item>
                            <el-descriptions-item label="买家地址">
                                {{ formatAddress(traceResult.buyerAddress) }}
                            </el-descriptions-item>
                            <el-descriptions-item label="交易电量">
                                <el-text type="warning" size="large">
                                    {{ traceResult.amount?.toFixed(2) }} kWh
                                </el-text>
                            </el-descriptions-item>
                            <el-descriptions-item label="订单编号">
                                {{ traceResult.orderId }}
                            </el-descriptions-item>
                            <el-descriptions-item label="交易时间" :span="2">
                                {{ traceResult.timeStr }}
                            </el-descriptions-item>
                            <el-descriptions-item v-if="traceResult.blockHash" label="区块哈希" :span="2">
                                <el-text size="small" class="block-hash">
                                    {{ traceResult.blockHash }}
                                </el-text>
                            </el-descriptions-item>
                        </el-descriptions>
                    </div>

                    <div class="card-container" style="margin-top: 20px;">
                        <div class="card-title">
                            <el-icon><TrendCharts /></el-icon>
                            溯源路径
                        </div>
                        <div class="trace-path" v-if="tracePath">
                            <el-timeline>
                                <el-timeline-item
                                    v-for="node in tracePath.nodes"
                                    :key="node.step"
                                    :type="getNodeType(node.type)"
                                    :hollow="node.status !== 'completed'"
                                    size="large"
                                >
                                    <div class="trace-node">
                                        <div class="node-header">
                                            <span class="node-icon">{{ getNodeIcon(node.type) }}</span>
                                            <span class="node-title">{{ node.title }}</span>
                                            <el-tag size="small" :type="node.status === 'completed' ? 'success' : 'info'">
                                                {{ node.status === 'completed' ? '已完成' : '进行中' }}
                                            </el-tag>
                                        </div>
                                        <div class="node-content">
                                            <p>{{ node.description }}</p>
                                            <p class="node-info">
                                                <el-icon><User /></el-icon>
                                                {{ node.userName }}
                                            </p>
                                            <p class="node-info">
                                                <el-icon><Clock /></el-icon>
                                                {{ node.timestamp }}
                                            </p>
                                        </div>
                                    </div>
                                </el-timeline-item>
                            </el-timeline>
                            <div class="trace-summary">
                                <el-alert type="info" :closable="false">
                                    <template #title>
                                        <strong>溯源摘要</strong>
                                    </template>
                                    {{ tracePath.summary }}
                                </el-alert>
                            </div>
                        </div>
                    </div>
                </el-col>

                <el-col :span="8">
                    <div class="card-container">
                        <div class="card-title">
                            <el-icon><Document /></el-icon>
                            交易凭证
                        </div>
                        <div class="certificate-box">
                            <pre class="certificate-content">{{ traceResult.certificate }}</pre>
                        </div>
                        <div class="certificate-actions">
                            <el-button type="primary" @click="downloadCertificate">
                                <el-icon><Download /></el-icon>
                                下载凭证
                            </el-button>
                            <el-button @click="copyCertificate">
                                <el-icon><CopyDocument /></el-icon>
                                复制凭证
                            </el-button>
                        </div>
                    </div>

                    <div class="card-container" style="margin-top: 20px;">
                        <div class="card-title">
                            <el-icon><InfoFilled /></el-icon>
                            溯源说明
                        </div>
                        <div class="info-list">
                            <div class="info-item">
                                <el-icon color="#67c23a"><CircleCheck /></el-icon>
                                <span>已验证：交易已在区块链上确认</span>
                            </div>
                            <div class="info-item">
                                <el-icon color="#e6a23c"><Warning /></el-icon>
                                <span>未验证：交易仅存在于数据库</span>
                            </div>
                            <div class="info-item">
                                <el-icon color="#409eff"><Link /></el-icon>
                                <span>区块高度：交易所在区块编号</span>
                            </div>
                            <div class="info-item">
                                <el-icon color="#909399"><Document /></el-icon>
                                <span>凭证可用于交易证明</span>
                            </div>
                        </div>
                    </div>
                </el-col>
            </el-row>
        </div>

        <div v-else class="card-container">
            <el-empty description="请输入交易哈希进行溯源查询">
                <el-button type="primary" @click="showRecentTransactions">
                    查看最近交易
                </el-button>
            </el-empty>
        </div>

        <el-dialog v-model="recentDialogVisible" title="最近交易记录" width="600px">
            <el-table :data="recentTransactions" style="width: 100%">
                <el-table-column prop="txHash" label="交易哈希" width="200">
                    <template #default="{ row }">
                        <el-button link type="primary" @click="selectTransaction(row.txHash)">
                            {{ formatHash(row.txHash) }}
                        </el-button>
                    </template>
                </el-table-column>
                <el-table-column prop="amount" label="电量" width="100">
                    <template #default="{ row }">
                        {{ row.amount?.toFixed(2) }} kWh
                    </template>
                </el-table-column>
                <el-table-column prop="timeStr" label="时间" />
            </el-table>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const searchTxHash = ref('')
const loading = ref(false)
const traceResult = ref(null)
const tracePath = ref(null)
const recentDialogVisible = ref(false)
const recentTransactions = ref([])

const handleSearch = async () => {
    if (!searchTxHash.value) {
        ElMessage.warning('请输入交易哈希')
        return
    }

    loading.value = true
    try {
        const [verifyRes, pathRes] = await Promise.all([
            axios.get(`/api/trace/verify/${searchTxHash.value}`),
            axios.get(`/api/trace/path/${searchTxHash.value}`)
        ])

        if (verifyRes.data.code === 200) {
            traceResult.value = verifyRes.data.data
        } else {
            ElMessage.error(verifyRes.data.message)
        }

        if (pathRes.data.code === 200) {
            tracePath.value = pathRes.data.data
        }
    } catch (error) {
        ElMessage.error('查询失败: ' + error.message)
    } finally {
        loading.value = false
    }
}

const showRecentTransactions = async () => {
    try {
        const response = await axios.get(`/api/trade/history/${userStore.blockchainAddress}`)
        if (response.data.code === 200) {
            recentTransactions.value = response.data.data || []
            recentDialogVisible.value = true
        }
    } catch (error) {
        ElMessage.error('获取交易记录失败')
    }
}

const selectTransaction = (txHash) => {
    searchTxHash.value = txHash
    recentDialogVisible.value = false
    handleSearch()
}

const copyToClipboard = async (text) => {
    try {
        await navigator.clipboard.writeText(text)
        ElMessage.success('已复制到剪贴板')
    } catch (error) {
        ElMessage.error('复制失败')
    }
}

const copyCertificate = async () => {
    if (traceResult.value?.certificate) {
        await copyToClipboard(traceResult.value.certificate)
    }
}

const downloadCertificate = () => {
    if (!traceResult.value?.certificate) return
    
    const blob = new Blob([traceResult.value.certificate], { type: 'text/plain' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `交易凭证_${traceResult.value.txHash.substring(0, 10)}.txt`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    ElMessage.success('凭证已下载')
}

const formatAddress = (address) => {
    if (!address) return ''
    return `${address.substring(0, 6)}...${address.substring(address.length - 4)}`
}

const formatHash = (hash) => {
    if (!hash) return ''
    return `${hash.substring(0, 10)}...${hash.substring(hash.length - 8)}`
}

const getNodeType = (type) => {
    const types = {
        producer: 'success',
        listing: 'primary',
        trade: 'warning',
        blockchain: 'danger',
        delivery: 'info'
    }
    return types[type] || 'primary'
}

const getNodeIcon = (type) => {
    const icons = {
        producer: '☀️',
        listing: '📋',
        trade: '🤝',
        blockchain: '🔗',
        delivery: '⚡'
    }
    return icons[type] || '📍'
}
</script>

<style scoped>
.trace-result {
    margin-top: 20px;
}

.tx-hash {
    font-family: monospace;
    font-size: 12px;
}

.block-hash {
    font-family: monospace;
    font-size: 11px;
    color: #909399;
}

.certificate-box {
    background: #f5f7fa;
    border-radius: 8px;
    padding: 15px;
    margin-bottom: 15px;
}

.certificate-content {
    font-family: monospace;
    font-size: 12px;
    line-height: 1.6;
    white-space: pre-wrap;
    margin: 0;
    color: #303133;
}

.certificate-actions {
    display: flex;
    gap: 10px;
}

.info-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.info-item {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: #606266;
}

.trace-path {
    padding: 10px 0;
}

.trace-node {
    padding: 5px 0;
}

.node-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 8px;
}

.node-icon {
    font-size: 20px;
}

.node-title {
    font-weight: 600;
    font-size: 15px;
    color: #303133;
}

.node-content {
    padding-left: 30px;
}

.node-content p {
    margin: 5px 0;
    font-size: 13px;
    color: #606266;
}

.node-info {
    display: flex;
    align-items: center;
    gap: 5px;
    color: #909399;
}

.trace-summary {
    margin-top: 20px;
}
</style>
