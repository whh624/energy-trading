<template>
    <div>
        <div class="page-header">
            <h2 class="page-title">钱包管理</h2>
            <p class="page-desc">管理您的MetaMask钱包和区块链资产</p>
        </div>

        <div class="wallet-status-card" v-if="!walletStore.isConnected">
            <div class="connect-prompt">
                <div class="connect-icon">🦊</div>
                <h3>连接MetaMask钱包</h3>
                <p>连接钱包后可以进行链上交易、查看余额、管理资产</p>
                <el-button 
                    type="primary" 
                    size="large"
                    :loading="walletStore.isConnecting"
                    @click="handleConnect"
                >
                    <el-icon><Wallet /></el-icon>
                    {{ walletStore.isConnecting ? '连接中...' : '连接MetaMask' }}
                </el-button>
                <div class="connect-steps">
                    <div class="step">
                        <span class="step-num">1</span>
                        <span>安装MetaMask浏览器扩展</span>
                    </div>
                    <div class="step">
                        <span class="step-num">2</span>
                        <span>点击上方按钮连接钱包</span>
                    </div>
                    <div class="step">
                        <span class="step-num">3</span>
                        <span>在MetaMask中确认连接请求</span>
                    </div>
                </div>
            </div>
        </div>

        <template v-else>
            <div class="stat-cards">
                <div class="stat-card">
                    <div class="stat-icon blue">
                        <el-icon><Wallet /></el-icon>
                    </div>
                    <div class="stat-value">{{ walletStore.balanceInEth }}</div>
                    <div class="stat-label">钱包余额 (ETH)</div>
                </div>
                <div class="stat-card">
                    <div class="stat-icon green">
                        <el-icon><Coin /></el-icon>
                    </div>
                    <div class="stat-value">{{ contractBalance.available }}</div>
                    <div class="stat-label">合约可用余额 (ETH)</div>
                </div>
                <div class="stat-card">
                    <div class="stat-icon orange">
                        <el-icon><Lock /></el-icon>
                    </div>
                    <div class="stat-value">{{ contractBalance.frozen }}</div>
                    <div class="stat-label">冻结余额 (ETH)</div>
                </div>
                <div class="stat-card">
                    <div class="stat-icon red">
                        <el-icon><Connection /></el-icon>
                    </div>
                    <div class="stat-value">{{ networkName }}</div>
                    <div class="stat-label">当前网络</div>
                </div>
            </div>

            <div class="card-container">
                <div class="card-title">钱包信息</div>
                <el-descriptions :column="2" border>
                    <el-descriptions-item label="钱包地址" :span="2">
                        <div style="display: flex; align-items: center; gap: 10px;">
                            <code>{{ walletStore.account }}</code>
                            <el-button size="small" @click="copyAddress">复制</el-button>
                        </div>
                    </el-descriptions-item>
                    <el-descriptions-item label="链ID">
                        {{ walletStore.chainId }}
                    </el-descriptions-item>
                    <el-descriptions-item label="网络名称">
                        {{ networkName }}
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
            </div>

            <el-row :gutter="20" style="margin-top: 20px;">
                <el-col :span="12">
                    <div class="card-container">
                        <div class="card-title">存款到合约</div>
                        <el-form label-width="80px">
                            <el-form-item label="存款金额">
                                <el-input-number 
                                    v-model="depositAmount" 
                                    :min="0.001" 
                                    :max="100"
                                    :precision="4"
                                    :step="0.01"
                                    style="width: 100%"
                                />
                                <span style="margin-left: 10px; color: #909399;">ETH</span>
                            </el-form-item>
                            <el-form-item>
                                <el-button 
                                    type="primary" 
                                    :loading="depositLoading"
                                    @click="handleDeposit"
                                    style="width: 100%"
                                >
                                    存款
                                </el-button>
                            </el-form-item>
                        </el-form>
                    </div>
                </el-col>
                <el-col :span="12">
                    <div class="card-container">
                        <div class="card-title">从合约提款</div>
                        <el-form label-width="80px">
                            <el-form-item label="提款金额">
                                <el-input-number 
                                    v-model="withdrawAmount" 
                                    :min="0.001" 
                                    :max="parseFloat(contractBalance.available) || 0"
                                    :precision="4"
                                    :step="0.01"
                                    style="width: 100%"
                                />
                                <span style="margin-left: 10px; color: #909399;">ETH</span>
                            </el-form-item>
                            <el-form-item>
                                <el-button 
                                    type="warning" 
                                    :loading="withdrawLoading"
                                    @click="handleWithdraw"
                                    style="width: 100%"
                                >
                                    提款
                                </el-button>
                            </el-form-item>
                        </el-form>
                    </div>
                </el-col>
            </el-row>

            <el-row :gutter="20" style="margin-top: 20px;">
                <el-col :span="12">
                    <div class="card-container">
                        <div class="card-title">冻结合约余额</div>
                        <div style="color: #909399; font-size: 13px; margin-bottom: 16px;">
                            冻结后的余额可在购电时优先抵扣，无需每次都从钱包全额支付。
                        </div>
                        <el-form label-width="80px">
                            <el-form-item label="冻结金额">
                                <el-input-number
                                    v-model="freezeAmount"
                                    :min="0.001"
                                    :max="parseFloat(contractBalance.available) || 0"
                                    :precision="4"
                                    :step="0.01"
                                    style="width: 100%"
                                />
                                <span style="margin-left: 10px; color: #909399;">ETH</span>
                            </el-form-item>
                            <el-form-item>
                                <el-button
                                    type="info"
                                    :loading="freezeLoading"
                                    @click="handleFreeze"
                                    style="width: 100%"
                                >
                                    冻结余额
                                </el-button>
                            </el-form-item>
                        </el-form>
                    </div>
                </el-col>
                <el-col :span="12">
                    <div class="card-container">
                        <div class="card-title">解冻合约余额</div>
                        <div style="color: #909399; font-size: 13px; margin-bottom: 16px;">
                            解冻后会回到可用余额，可继续提款或再次调整冻结金额。
                        </div>
                        <el-form label-width="80px">
                            <el-form-item label="解冻金额">
                                <el-input-number
                                    v-model="unfreezeAmount"
                                    :min="0.001"
                                    :max="parseFloat(contractBalance.frozen) || 0"
                                    :precision="4"
                                    :step="0.01"
                                    style="width: 100%"
                                />
                                <span style="margin-left: 10px; color: #909399;">ETH</span>
                            </el-form-item>
                            <el-form-item>
                                <el-button
                                    type="success"
                                    :loading="unfreezeLoading"
                                    @click="handleUnfreeze"
                                    style="width: 100%"
                                >
                                    解冻余额
                                </el-button>
                            </el-form-item>
                        </el-form>
                    </div>
                </el-col>
            </el-row>

            <div class="card-container" style="margin-top: 20px;">
                <div class="card-title">交易记录</div>
                <el-table :data="recentTxs" style="width: 100%">
                    <el-table-column prop="txHash" label="交易哈希" width="200">
                        <template #default="{ row }">
                            {{ row.txHash.substring(0, 10) }}...{{ row.txHash.substring(row.txHash.length - 8) }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="type" label="类型" width="120">
                        <template #default="{ row }">
                            <el-tag :type="getTxTypeTag(row.type)" size="small">
                                {{ row.type }}
                            </el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="blockNumber" label="区块号" width="120" />
                    <el-table-column prop="gasUsed" label="Gas消耗" width="120" />
                    <el-table-column prop="time" label="时间" />
                </el-table>
                <el-empty v-if="recentTxs.length === 0" description="暂无交易记录" />
            </div>

            <div class="card-container" style="margin-top: 20px;">
                <div class="card-title">操作</div>
                <el-row :gutter="20">
                    <el-col :span="8">
                        <el-button @click="refreshBalance" style="width: 100%">
                            <el-icon><Refresh /></el-icon>
                            刷新余额
                        </el-button>
                    </el-col>
                    <el-col :span="8">
                        <el-button @click="switchNetwork" style="width: 100%">
                            <el-icon><Connection /></el-icon>
                            切换到Ganache
                        </el-button>
                    </el-col>
                    <el-col :span="8">
                        <el-button type="danger" @click="handleDisconnect" style="width: 100%">
                            <el-icon><SwitchButton /></el-icon>
                            断开钱包
                        </el-button>
                    </el-col>
                </el-row>
            </div>
        </template>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useWalletStore } from '../stores/wallet'
import { ElMessage, ElMessageBox } from 'element-plus'

const walletStore = useWalletStore()

const depositAmount = ref(0.01)
const withdrawAmount = ref(0.01)
const freezeAmount = ref(0.01)
const unfreezeAmount = ref(0.01)
const depositLoading = ref(false)
const withdrawLoading = ref(false)
const freezeLoading = ref(false)
const unfreezeLoading = ref(false)
const contractBalance = reactive({ available: '0.0000', frozen: '0.0000' })
const recentTxs = ref([])

const networkName = computed(() => {
    switch (walletStore.chainId) {
        case 1337: return 'Ganache Local'
        case 1: return 'Ethereum Mainnet'
        case 5: return 'Goerli Testnet'
        case 11155111: return 'Sepolia Testnet'
        default: return `Chain ${walletStore.chainId}`
    }
})

const handleConnect = async () => {
    try {
        const result = await walletStore.connectWallet()
        ElMessage.success(`钱包已连接: ${result.account.substring(0, 6)}...`)
    } catch (error) {
        if (error.code === 4001) {
            ElMessage.warning('用户拒绝了连接请求')
        } else {
            ElMessage.error(error.message || '连接钱包失败')
        }
    }
}

const copyAddress = async () => {
    try {
        await navigator.clipboard.writeText(walletStore.account)
        ElMessage.success('地址已复制到剪贴板')
    } catch {
        ElMessage.error('复制失败')
    }
}

const handleDeposit = async () => {
    if (depositAmount.value <= 0) {
        ElMessage.warning('请输入有效的存款金额')
        return
    }
    depositLoading.value = true
    try {
        const result = await walletStore.depositOnChain(depositAmount.value)
        ElMessage.success(`存款成功！交易哈希: ${result.txHash.substring(0, 10)}...`)
        recentTxs.value.unshift({
            txHash: result.txHash,
            type: '存款',
            blockNumber: result.blockNumber,
            gasUsed: result.gasUsed,
            time: new Date().toLocaleString('zh-CN')
        })
        await refreshBalance()
    } catch (error) {
        ElMessage.error('存款失败: ' + (error.reason || error.message))
    } finally {
        depositLoading.value = false
    }
}

const handleWithdraw = async () => {
    if (withdrawAmount.value <= 0) {
        ElMessage.warning('请输入有效的提款金额')
        return
    }
    withdrawLoading.value = true
    try {
        const result = await walletStore.withdrawOnChain(withdrawAmount.value)
        ElMessage.success(`提款成功！交易哈希: ${result.txHash.substring(0, 10)}...`)
        recentTxs.value.unshift({
            txHash: result.txHash,
            type: '提款',
            blockNumber: result.blockNumber,
            gasUsed: result.gasUsed,
            time: new Date().toLocaleString('zh-CN')
        })
        await refreshBalance()
    } catch (error) {
        ElMessage.error('提款失败: ' + (error.reason || error.message))
    } finally {
        withdrawLoading.value = false
    }
}

const handleFreeze = async () => {
    if (freezeAmount.value <= 0) {
        ElMessage.warning('请输入有效的冻结金额')
        return
    }
    freezeLoading.value = true
    try {
        const result = await walletStore.freezeBalanceOnChain(freezeAmount.value)
        ElMessage.success(`冻结成功！交易哈希: ${result.txHash.substring(0, 10)}...`)
        recentTxs.value.unshift({
            txHash: result.txHash,
            type: '冻结',
            blockNumber: result.blockNumber,
            gasUsed: result.gasUsed,
            time: new Date().toLocaleString('zh-CN')
        })
        await refreshBalance()
    } catch (error) {
        ElMessage.error('冻结失败: ' + (error.reason || error.message))
    } finally {
        freezeLoading.value = false
    }
}

const handleUnfreeze = async () => {
    if (unfreezeAmount.value <= 0) {
        ElMessage.warning('请输入有效的解冻金额')
        return
    }
    unfreezeLoading.value = true
    try {
        const result = await walletStore.unfreezeBalanceOnChain(unfreezeAmount.value)
        ElMessage.success(`解冻成功！交易哈希: ${result.txHash.substring(0, 10)}...`)
        recentTxs.value.unshift({
            txHash: result.txHash,
            type: '解冻',
            blockNumber: result.blockNumber,
            gasUsed: result.gasUsed,
            time: new Date().toLocaleString('zh-CN')
        })
        await refreshBalance()
    } catch (error) {
        ElMessage.error('解冻失败: ' + (error.reason || error.message))
    } finally {
        unfreezeLoading.value = false
    }
}

const refreshBalance = async () => {
    await walletStore.updateBalance()
    try {
        const balance = await walletStore.getContractBalance(walletStore.account)
        contractBalance.available = parseFloat(balance.available).toFixed(4)
        contractBalance.frozen = parseFloat(balance.frozen).toFixed(4)
    } catch (error) {
        console.error('获取合约余额失败:', error)
    }
}

const switchNetwork = async () => {
    try {
        await walletStore.switchToGanache()
        ElMessage.success('已切换到Ganache网络')
    } catch (error) {
        ElMessage.error(error.message)
    }
}

const handleDisconnect = async () => {
    try {
        await ElMessageBox.confirm('确定要断开钱包连接吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        walletStore.disconnectWallet()
        ElMessage.success('钱包已断开')
    } catch {}
}

const getTxTypeTag = (type) => {
    switch (type) {
        case '存款': return 'success'
        case '冻结': return 'info'
        case '解冻': return 'success'
        case '提款': return 'warning'
        case '创建挂单': return 'primary'
        case '购买电量': return 'success'
        case '取消挂单': return 'danger'
        default: return 'info'
    }
}

watch(() => walletStore.isConnected, async (connected) => {
    if (connected) {
        await refreshBalance()
    }
})

onMounted(async () => {
    if (walletStore.isConnected) {
        await refreshBalance()
    }
})
</script>

<style scoped>
.wallet-status-card {
    background: white;
    border-radius: 16px;
    padding: 40px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
    text-align: center;
}

.connect-prompt {
    max-width: 500px;
    margin: 0 auto;
}

.connect-icon {
    font-size: 80px;
    margin-bottom: 20px;
}

.connect-prompt h3 {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 10px;
}

.connect-prompt p {
    font-size: 14px;
    color: #909399;
    margin-bottom: 30px;
}

.connect-steps {
    display: flex;
    justify-content: center;
    gap: 30px;
    margin-top: 30px;
    padding-top: 25px;
    border-top: 1px solid #ebeef5;
}

.step {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
}

.step-num {
    width: 30px;
    height: 30px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    font-size: 14px;
}

.step span:last-child {
    font-size: 12px;
    color: #606266;
}
</style>
