<template>
    <div>
        <div class="page-header">
            <h2 class="page-title">我的挂单</h2>
            <p class="page-desc">管理您发布的电量出售挂单</p>
        </div>
        
        <div class="stat-cards">
            <div class="stat-card">
                <div class="stat-icon blue">
                    <el-icon><Document /></el-icon>
                </div>
                <div class="stat-value">{{ myOrders.length }}</div>
                <div class="stat-label">总挂单数</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon green">
                    <el-icon><CircleCheck /></el-icon>
                </div>
                <div class="stat-value">{{ openCount }}</div>
                <div class="stat-label">开放中</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon orange">
                    <el-icon><Finished /></el-icon>
                </div>
                <div class="stat-value">{{ completedCount }}</div>
                <div class="stat-label">已完成</div>
            </div>
        </div>
        
        <div class="card-container">
            <div class="card-title">挂单列表</div>
            
            <el-table :data="myOrders" style="width: 100%" v-loading="loading">
                <el-table-column prop="id" label="ID" width="80" />
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
                <el-table-column prop="createdTime" label="创建时间" width="180">
                    <template #default="{ row }">
                        {{ formatTime(row.createdTime) }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" fixed="right" width="200">
                    <template #default="{ row }">
                        <el-button 
                            type="danger" 
                            size="small"
                            @click="handleCancel(row)"
                            :disabled="row.status !== 0"
                        >
                            取消挂单
                        </el-button>
                        <el-button 
                            v-if="walletStore.isConnected && row.status === 0"
                            type="warning" 
                            size="small"
                            @click="handleOnChainCancel(row)"
                        >
                            链上取消
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { useWalletStore } from '../stores/wallet'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const walletStore = useWalletStore()

const loading = ref(false)
const myOrders = ref([])

const openCount = computed(() => myOrders.value.filter(o => o.status === 0).length)
const completedCount = computed(() => myOrders.value.filter(o => o.status === 1).length)

const fetchMyOrders = async () => {
    loading.value = true
    try {
        const address = walletStore.isConnected ? walletStore.account : userStore.blockchainAddress
        const response = await axios.get(`/api/order/user/${address}`)
        if (response.data.code === 200) {
            myOrders.value = response.data.data || []
        }
    } catch (error) {
        console.error('获取挂单失败:', error)
    } finally {
        loading.value = false
    }
}

const handleCancel = async (order) => {
    try {
        await ElMessageBox.confirm('确定要取消这个挂单吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        
        const response = await axios.post(`/api/order/cancel/${order.id}`)
        if (response.data.code === 200) {
            ElMessage.success('取消成功')
            fetchMyOrders()
        } else {
            ElMessage.error(response.data.message || '取消失败')
        }
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error(error.response?.data?.message || '取消失败')
        }
    }
}

const handleOnChainCancel = async (order) => {
    if (!walletStore.isConnected) {
        ElMessage.warning('请先连接MetaMask钱包')
        return
    }

    try {
        await ElMessageBox.confirm('确定要在链上取消这个挂单吗？这将通过MetaMask发送交易。', '链上取消', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })

        const result = await walletStore.cancelOrderOnChain(order.orderIdOnChain)
        ElMessage.success(`链上取消成功！交易哈希: ${result.txHash.substring(0, 10)}...`)

        const response = await axios.post(`/api/order/cancel/${order.id}`)
        if (response.data.code === 200) {
            fetchMyOrders()
        }
    } catch (error) {
        if (error.code === 4001 || error.code === 'ACTION_REJECTED') {
            ElMessage.warning('用户取消了交易')
        } else if (error !== 'cancel') {
            ElMessage.error('链上取消失败: ' + (error.reason || error.message))
        }
    }
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
    fetchMyOrders()
})
</script>
