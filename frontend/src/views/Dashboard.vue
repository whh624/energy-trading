<template>
    <div class="dashboard-layout">
        <header class="dashboard-header">
            <div class="header-left">
                <div class="header-logo" aria-hidden="true">
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
                <div class="header-title">
                    <h1>电力交易系统</h1>
                    <p>基于区块链的P2P能源交易平台</p>
                </div>
            </div>
            
            <div class="header-right">
                <el-tag :type="roleTagType" effect="plain">
                    {{ userStore.roleName }}
                </el-tag>

                <WalletConnect />

                <el-dropdown @command="handleCommand">
                    <div class="user-info">
                        <el-avatar :size="40" class="user-avatar">
                            {{ userStore.username.charAt(0).toUpperCase() }}
                        </el-avatar>
                        <div class="user-details">
                            <span class="user-name">{{ userStore.username }}</span>
                            <span class="user-role">{{ formatAddress(userStore.blockchainAddress) }}</span>
                        </div>
                    </div>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item command="profile">
                                <el-icon><User /></el-icon>
                                个人中心
                            </el-dropdown-item>
                            <el-dropdown-item command="logout" divided>
                                <el-icon><SwitchButton /></el-icon>
                                退出登录
                            </el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </div>
        </header>
        
        <div class="dashboard-body">
            <aside class="dashboard-aside">
                <div class="sidebar-menu">
                    <div class="menu-title">功能导航</div>
                    <el-menu
                        :default-active="activeMenu"
                        router
                    >
                        <el-menu-item index="/dashboard">
                            <el-icon><ShoppingCart /></el-icon>
                            <span>交易市场</span>
                        </el-menu-item>
                        <el-menu-item index="/dashboard/my-orders" v-if="userStore.role === 1">
                            <el-icon><Document /></el-icon>
                            <span>我的挂单</span>
                        </el-menu-item>
                        <el-menu-item index="/dashboard/create-order" v-if="userStore.role === 1">
                            <el-icon><Plus /></el-icon>
                            <span>发布挂单</span>
                        </el-menu-item>
                        <el-menu-item index="/dashboard/history">
                            <el-icon><Clock /></el-icon>
                            <span>交易历史</span>
                        </el-menu-item>
                        <el-menu-item index="/dashboard/trace">
                            <el-icon><Link /></el-icon>
                            <span>交易溯源</span>
                        </el-menu-item>
                        <el-menu-item index="/dashboard/wallet">
                            <el-icon><Wallet /></el-icon>
                            <span>钱包管理</span>
                        </el-menu-item>
                        <el-menu-item index="/dashboard/profile">
                            <el-icon><User /></el-icon>
                            <span>个人中心</span>
                        </el-menu-item>
                        
                        <div class="menu-title" v-if="userStore.role === 3">管理后台</div>
                        
                        <el-menu-item index="/dashboard/admin/users" v-if="userStore.role === 3">
                            <el-icon><Management /></el-icon>
                            <span>用户管理</span>
                        </el-menu-item>
                        <el-menu-item index="/dashboard/admin/transactions" v-if="userStore.role === 3">
                            <el-icon><Monitor /></el-icon>
                            <span>全网监控</span>
                        </el-menu-item>
                    </el-menu>
                </div>
            </aside>
            
            <main class="dashboard-main">
                <router-view />
            </main>
        </div>
    </div>
</template>

<script setup>
import { computed, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useWalletStore } from '../stores/wallet'
import { ElMessage, ElMessageBox } from 'element-plus'
import WalletConnect from '../components/WalletConnect.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const walletStore = useWalletStore()

const activeMenu = computed(() => route.path)

const roleTagType = computed(() => {
    switch (userStore.role) {
        case 1: return 'success'
        case 2: return 'warning'
        default: return 'info'
    }
})

const formatAddress = (address) => {
    if (!address) return ''
    return `${address.substring(0, 6)}...${address.substring(address.length - 4)}`
}

const handleCommand = (command) => {
    if (command === 'logout') {
        ElMessageBox.confirm('确定要退出登录吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }).then(() => {
            walletStore.disconnectWallet()
            userStore.logout()
            ElMessage.success('已退出登录')
            router.push('/login')
        }).catch(() => {})  
    } else if (command === 'profile') {
        router.push('/dashboard/profile')
    }
}

// 组件卸载时清理
onUnmounted(() => {
    walletStore.cleanup()
})
</script>
