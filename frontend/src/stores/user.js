import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
    
    const isLoggedIn = computed(() => !!userInfo.value)
    const username = computed(() => userInfo.value?.username || '')
    const role = computed(() => userInfo.value?.role || 0)
    const roleName = computed(() => {
        const roles = { 0: '普通用户', 1: '产电方', 2: '用电方', 3: '管理方' }
        return roles[userInfo.value?.role] || '未知'
    })
    const blockchainAddress = computed(() => userInfo.value?.blockchainAddress || '')
    const balance = computed(() => userInfo.value?.balance || 0)
    const status = computed(() => userInfo.value?.status ?? 0)
    const trustScore = computed(() => userInfo.value?.trustScore ?? 100)
    
    async function login(loginData) {
        try {
            const response = await axios.post('/api/user/login', loginData)
            if (response.data.code === 200) {
                userInfo.value = response.data.data
                localStorage.setItem('userInfo', JSON.stringify(response.data.data))
                return { success: true, message: '登录成功' }
            }
            return { success: false, message: response.data.message }
        } catch (error) {
            return { success: false, message: error.response?.data?.message || error.message }
        }
    }
    
    async function register(registerData) {
        try {
            const response = await axios.post('/api/user/register', registerData)
            if (response.data.code === 200) {
                return { success: true, message: '注册成功', data: response.data.data }
            }
            return { success: false, message: response.data.message }
        } catch (error) {
            return { success: false, message: error.response?.data?.message || error.message }
        }
    }

    async function updateWalletAddress(id, blockchainAddress) {
        try {
            const response = await axios.post('/api/user/wallet-address', {
                id,
                blockchainAddress
            })
            if (response.data.code === 200) {
                if (userInfo.value) {
                    userInfo.value = {
                        ...userInfo.value,
                        ...response.data.data
                    }
                    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
                }
                return { success: true, message: response.data.message, data: response.data.data }
            }
            return { success: false, message: response.data.message }
        } catch (error) {
            return { success: false, message: error.response?.data?.message || error.message }
        }
    }
    
    function logout() {
        token.value = ''
        userInfo.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
    }
    
    function updateBalance(newBalance) {
        if (userInfo.value) {
            userInfo.value.balance = newBalance
            localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        }
    }
    
    return {
        token,
        userInfo,
        isLoggedIn,
        username,
        role,
        roleName,
        blockchainAddress,
        balance,
        status,
        trustScore,
        login,
        register,
        updateWalletAddress,
        logout,
        updateBalance
    }
})
