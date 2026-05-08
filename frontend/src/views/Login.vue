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
                <h1>电力交易系统</h1>
                <p class="subtitle">基于区块链的P2P能源交易平台</p>
            </div>
            
            <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="auth-form">
                <el-form-item prop="username">
                    <el-input
                        v-model="loginForm.username"
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
                        v-model="loginForm.password"
                        type="password"
                        placeholder="密码"
                        size="large"
                        show-password
                        @keyup.enter="handleLogin"
                    >
                        <template #prefix>
                            <el-icon><Lock /></el-icon>
                        </template>
                    </el-input>
                </el-form-item>

                <el-form-item prop="role">
                    <div class="role-selector">
                        <div
                            class="role-option"
                            :class="{ active: loginForm.role === 1 }"
                            @click="loginForm.role = 1"
                        >
                            <div class="icon">☀️</div>
                            <h3>产电方</h3>
                            <p>发布并出售电量</p>
                        </div>
                        <div
                            class="role-option"
                            :class="{ active: loginForm.role === 2 }"
                            @click="loginForm.role = 2"
                        >
                            <div class="icon">🔌</div>
                            <h3>用电方</h3>
                            <p>购买并使用电量</p>
                        </div>
                        <div
                            class="role-option"
                            :class="{ active: loginForm.role === 3 }"
                            @click="loginForm.role = 3"
                        >
                            <div class="icon">🛡️</div>
                            <h3>管理方</h3>
                            <p>平台监管与审查</p>
                        </div>
                    </div>
                </el-form-item>
                
                <el-form-item>
                    <el-button
                        type="primary"
                        size="large"
                        class="auth-button"
                        :loading="loading"
                        @click="handleLogin"
                    >
                        登录
                    </el-button>
                </el-form-item>
                
                <div class="auth-footer">
                    <el-button link type="primary" @click="goToRegister">
                        注册新账户
                    </el-button>
                    <el-button link type="info" @click="demoLogin">
                        演示账户登录
                    </el-button>
                </div>
            </el-form>
            
            <div class="auth-features">
                <div class="feature-item">
                    <el-icon><CircleCheck /></el-icon>
                    <span>区块链安全</span>
                </div>
                <div class="feature-item">
                    <el-icon><Lightning /></el-icon>
                    <span>即时交易</span>
                </div>
                <div class="feature-item">
                    <el-icon><TrendCharts /></el-icon>
                    <span>透明可追溯</span>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
    username: '',
    password: '',
    role: 1
})

const rules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
    ],
    role: [
        { required: true, message: '请选择身份', trigger: 'change' }
    ]
}

const handleLogin = async () => {
    if (!loginFormRef.value) return
    
    await loginFormRef.value.validate(async (valid) => {
        if (valid) {
            loading.value = true
            const result = await userStore.login(loginForm)
            loading.value = false
            
            if (result.success) {
                ElMessage.success(result.message)
                router.push('/dashboard')
            } else {
                ElMessage.error(result.message)
            }
        }
    })
}

const goToRegister = () => {
    router.push('/register')
}

const demoLogin = () => {
    loginForm.username = 'admin'
    loginForm.password = '123456'
    loginForm.role = 3
    handleLogin()
}
</script>

<style scoped>
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
