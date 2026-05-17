<template>
    <div class="auth-container">
        <div class="auth-card">
            <div class="auth-header">
                <div class="logo-icon" aria-hidden="true">
                    <svg viewBox="0 0 64 64" fill="none">
                        <rect x="8" y="8" width="48" height="48" rx="14" fill="#F7F6F0" stroke="#243126" stroke-width="2"/>
                        <path d="M24 19V27" stroke="#243126" stroke-width="3.2" stroke-linecap="round"/>
                        <path d="M40 19V27" stroke="#243126" stroke-width="3.2" stroke-linecap="round"/>
                        <path d="M22 18H42" stroke="#243126" stroke-width="3" stroke-linecap="round"/>
                        <path d="M32 27V32" stroke="#243126" stroke-width="3" stroke-linecap="round"/>
                        <path d="M23 27H41C41.55 27 42 27.45 42 28V35C42 39.97 37.97 44 33 44H31C26.03 44 22 39.97 22 35V28C22 27.45 22.45 27 23 27Z" fill="#FDFCF7" stroke="#243126" stroke-width="2.8"/>
                        <path d="M35 29L29.8 35.2H33.1L29 41L35.3 34.6H31.8L35 29Z" fill="#D7A63D" stroke="#243126" stroke-width="1.4" stroke-linejoin="round"/>
                        <path d="M24 46H40" stroke="#5E9B68" stroke-width="3" stroke-linecap="round"/>
                        <circle cx="24" cy="46" r="2.2" fill="#5E9B68"/>
                        <circle cx="40" cy="46" r="2.2" fill="#5E9B68"/>
                    </svg>
                </div>
                <h1>注册新账户</h1>
                <p class="subtitle">加入P2P电力交易网络</p>
            </div>
            
            <el-form :model="registerForm" :rules="rules" ref="registerFormRef" class="auth-form">
                <el-form-item prop="username">
                    <el-input
                        v-model="registerForm.username"
                        placeholder="用户名"
                        size="large"
                    >
                        <template #prefix>
                            <el-icon><User /></el-icon>
                        </template>
                    </el-input>
                </el-form-item>
                
                <el-form-item prop="password">
                    <el-input
                        v-model="registerForm.password"
                        type="password"
                        placeholder="密码"
                        size="large"
                        show-password
                    >
                        <template #prefix>
                            <el-icon><Lock /></el-icon>
                        </template>
                    </el-input>
                </el-form-item>
                
                <el-form-item prop="confirmPassword">
                    <el-input
                        v-model="registerForm.confirmPassword"
                        type="password"
                        placeholder="确认密码"
                        size="large"
                        show-password
                    >
                        <template #prefix>
                            <el-icon><Lock /></el-icon>
                        </template>
                    </el-input>
                </el-form-item>

                <el-form-item>
                    <div class="wallet-panel">
                        <div class="wallet-panel__header">钱包地址</div>
                        <div class="wallet-panel__address">
                            {{ walletStore.isConnected ? walletStore.account : '请先连接 MetaMask 钱包' }}
                        </div>
                        <el-button
                            type="primary"
                            plain
                            :loading="walletStore.isConnecting"
                            @click="handleConnectWallet"
                        >
                            {{ walletStore.isConnected ? '重新连接钱包' : '连接 MetaMask' }}
                        </el-button>
                    </div>
                </el-form-item>
                
                <el-form-item prop="role">
                    <div class="role-selector">
                        <div 
                            class="role-option" 
                            :class="{ active: registerForm.role === 1 }"
                            @click="registerForm.role = 1"
                        >
                            <div class="icon">☀️</div>
                            <h3>产电方</h3>
                            <p>出售多余电量</p>
                        </div>
                        <div 
                            class="role-option" 
                            :class="{ active: registerForm.role === 2 }"
                            @click="registerForm.role = 2"
                        >
                            <div class="icon">🔌</div>
                            <h3>用电方</h3>
                            <p>购买所需电量</p>
                        </div>
                        <div 
                            class="role-option" 
                            :class="{ active: registerForm.role === 3 }"
                            @click="registerForm.role = 3"
                        >
                            <div class="icon">🛡️</div>
                            <h3>管理方</h3>
                            <p>平台监管与运维</p>
                        </div>
                    </div>
                </el-form-item>
                
                <el-form-item>
                    <el-button
                        type="primary"
                        size="large"
                        class="auth-button"
                        :loading="loading"
                        @click="handleRegister"
                    >
                        注册
                    </el-button>
                </el-form-item>
                
                <div class="auth-footer">
                    <el-button link type="primary" @click="goToLogin">
                        返回登录
                    </el-button>
                </div>
            </el-form>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useWalletStore } from '../stores/wallet'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const walletStore = useWalletStore()

const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
    username: '',
    password: '',
    confirmPassword: '',
    role: 1
})

const validateConfirmPassword = (rule, value, callback) => {
    if (value === '') {
        callback(new Error('请再次输入密码'))
    } else if (value !== registerForm.password) {
        callback(new Error('两次输入密码不一致'))
    } else {
        callback()
    }
}

const rules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
    ],
    confirmPassword: [
        { required: true, validator: validateConfirmPassword, trigger: 'blur' }
    ],
    role: [
        { required: true, message: '请选择角色', trigger: 'change' }
    ]
}

const handleConnectWallet = async () => {
    try {
        await walletStore.connectWallet()
        ElMessage.success('钱包连接成功')
    } catch (error) {
        ElMessage.error(error.message || '钱包连接失败')
    }
}

const handleRegister = async () => {
    if (!registerFormRef.value) return
    
    await registerFormRef.value.validate(async (valid) => {
        if (valid) {
            loading.value = true

            let blockchainAddress = null; // Default to null

            if (registerForm.role === 3) { // 管理方
                // For admin, blockchainAddress is optional. If connected, use it.
                if (walletStore.isConnected && walletStore.account) {
                    blockchainAddress = walletStore.account;
                }
                // If not connected, blockchainAddress remains null, which is allowed for admin.
            } else { // 产电方 or 用电方
                // For other roles, blockchainAddress is mandatory.
                if (!walletStore.isConnected || !walletStore.account) {
                    loading.value = false;
                    ElMessage.error('请先连接 MetaMask 钱包后再注册');
                    return;
                }
                blockchainAddress = walletStore.account;
            }

            const result = await userStore.register({
                username: registerForm.username,
                password: registerForm.password,
                role: registerForm.role,
                blockchainAddress: blockchainAddress // Pass the determined address
            })
            
            loading.value = false
            
            if (result.success) {
                ElMessage.success(result.message + '，请登录')
                router.push('/login')
            } else {
                ElMessage.error(result.message)
            }
        }
    })
}

const goToLogin = () => {
    router.push('/login')
}
</script>

<style scoped>
.wallet-panel {
    width: 100%;
    padding: 14px 16px;
    border: 1px solid #e4e7ed;
    border-radius: 12px;
    background: #fafafa;
}

.wallet-panel__header {
    margin-bottom: 8px;
    font-size: 13px;
    color: #909399;
}

.wallet-panel__address {
    margin-bottom: 12px;
    color: #303133;
    font-size: 14px;
    word-break: break-all;
}

.role-selector {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
    width: 100%;
}

.role-option {
    padding: 16px 8px;
    border: 2px solid #e4e7ed;
    border-radius: 12px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s ease;
    background: #fff;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
}

.role-option:hover {
    border-color: #667eea;
    transform: translateY(-2px);
}

.role-option.active {
    border-color: #667eea;
    background: #f5f7ff;
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.15);
}

.role-option .icon {
    font-size: 24px;
    margin-bottom: 8px;
}

.role-option h3 {
    margin: 0 0 4px;
    font-size: 14px;
    color: #303133;
    white-space: nowrap;
}

.role-option p {
    margin: 0;
    font-size: 11px;
    color: #909399;
    line-height: 1.2;
    transform: scale(0.9);
}
</style>
