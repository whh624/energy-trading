<template>
    <div>
        <div class="page-header">
            <h2 class="page-title">发布挂单</h2>
            <p class="page-desc">创建新的电量出售挂单</p>
        </div>

        <el-alert 
            v-if="!walletStore.isConnected"
            title="提示：连接MetaMask钱包后可以将挂单同步到区块链" 
            type="info" 
            :closable="false"
            style="margin-bottom: 20px;"
        >
            <template #default>
                <el-button size="small" type="primary" @click="goToWallet">前往连接</el-button>
            </template>
        </el-alert>
        
        <div class="card-container" style="max-width: 600px;">
            <el-form :model="orderForm" :rules="rules" ref="orderFormRef" label-width="100px">
                <el-form-item label="出售电量" prop="amount">
                    <el-input-number 
                        v-model="orderForm.amount" 
                        :min="0.1" 
                        :max="10000"
                        :precision="2"
                        style="width: 100%"
                    />
                    <span style="margin-left: 10px; color: #909399;">kWh</span>
                </el-form-item>
                
                <el-form-item label="单价" prop="priceEth">
                    <el-input-number 
                        v-model="orderForm.priceEth" 
                        :min="0.000001" 
                        :max="100"
                        :precision="6"
                        :step="0.001"
                        style="width: 100%"
                    />
                    <span style="margin-left: 10px; color: #909399;">ETH/kWh</span>
                </el-form-item>
                
                <el-form-item label="预计总价">
                    <span style="font-size: 18px; font-weight: 600; color: #667eea;">
                        {{ (orderForm.amount * orderForm.priceEth).toFixed(6) }} ETH
                    </span>
                </el-form-item>
                
                <el-form-item label="区块链地址">
                    <el-input :value="walletStore.isConnected ? walletStore.account : userStore.blockchainAddress" disabled />
                </el-form-item>

                <el-form-item label="交易方式">
                    <el-radio-group v-model="orderForm.tradeMode">
                        <el-radio :label="1">链上交易 (MetaMask)</el-radio>
                        <el-radio :label="0">链下交易 (模拟)</el-radio>
                    </el-radio-group>
                </el-form-item>
                
                <el-form-item>
                    <el-button 
                        type="primary" 
                        size="large"
                        :loading="loading"
                        @click="handleSubmit"
                        style="width: 100%;"
                    >
                        发布挂单
                    </el-button>
                </el-form-item>
            </el-form>
        </div>
        
        <div class="card-container" style="max-width: 600px; margin-top: 20px;">
            <div class="card-title">注意事项</div>
            <el-alert type="info" :closable="false">
                <ul style="margin: 0; padding-left: 20px;">
                    <li>链上交易将通过MetaMask签名并提交到区块链</li>
                    <li>链下交易仅在数据库中记录，不实际上链</li>
                    <li>用电用户可以购买您的挂单</li>
                    <li>您可以随时取消未成交的挂单</li>
                    <li>交易完成后资金将自动转入您的账户</li>
                </ul>
            </el-alert>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useWalletStore } from '../stores/wallet'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { ethers } from 'ethers'

const router = useRouter()
const userStore = useUserStore()
const walletStore = useWalletStore()

const orderFormRef = ref(null)
const loading = ref(false)

const orderForm = reactive({
    amount: 10,
    priceEth: 0.001,
    tradeMode: walletStore.isConnected ? 1 : 0
})

const rules = {
    amount: [
        { required: true, message: '请输入出售电量', trigger: 'blur' }
    ],
    priceEth: [
        { required: true, message: '请输入单价', trigger: 'blur' }
    ]
}

const handleSubmit = async () => {
    if (!orderFormRef.value) return
    
    await orderFormRef.value.validate(async (valid) => {
        if (valid) {
            if (orderForm.tradeMode === 1) {
                await handleOnChainCreate()
            } else {
                await handleOffChainCreate()
            }
        }
    })
}

const handleOnChainCreate = async () => {
    if (!walletStore.isConnected) {
        ElMessage.warning('请先连接MetaMask钱包')
        return
    }

    loading.value = true
    try {
        const priceWei = Math.floor(orderForm.priceEth * 1e18)

        const chainResult = await walletStore.createOrderOnChain(
            orderForm.amount,
            priceWei
        )

        const response = await axios.post('/api/order/create', {
            amount: orderForm.amount,
            price: priceWei,
            sellerAddress: walletStore.account,
            txHash: chainResult.txHash,
            blockNumber: chainResult.blockNumber
        })

        if (response.data.code === 200) {
            ElMessage.success(`挂单发布成功！交易哈希: ${chainResult.txHash.substring(0, 10)}...`)
            orderForm.amount = 10
            orderForm.priceEth = 0.001
        } else {
            ElMessage.error(response.data.message || '发布失败')
        }
    } catch (error) {
        if (error.code === 4001 || error.code === 'ACTION_REJECTED') {
            ElMessage.warning('用户取消了交易')
        } else {
            ElMessage.error('链上交易失败: ' + (error.reason || error.message))
        }
    } finally {
        loading.value = false
    }
}

const handleOffChainCreate = async () => {
    loading.value = true
    try {
        const priceWei = Math.floor(orderForm.priceEth * 1e18)
        
        const response = await axios.post('/api/order/create', {
            amount: orderForm.amount,
            price: priceWei,
            sellerAddress: userStore.blockchainAddress
        })
        
        if (response.data.code === 200) {
            ElMessage.success('挂单发布成功！')
            orderForm.amount = 10
            orderForm.priceEth = 0.001
        } else {
            ElMessage.error(response.data.message || '发布失败')
        }
    } catch (error) {
        ElMessage.error(error.response?.data?.message || '发布失败')
    } finally {
        loading.value = false
    }
}

const goToWallet = () => {
    router.push('/dashboard/wallet')
}
</script>
