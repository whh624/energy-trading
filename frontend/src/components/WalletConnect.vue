<template>
    <div class="wallet-connect">
        <div v-if="walletStore.isConnected" class="wallet-connected">
            <div class="wallet-info">
                <div class="wallet-indicator"></div>
                <span class="wallet-address">{{ walletStore.shortAddress }}</span>
                <span class="wallet-balance">{{ walletStore.balanceInEth }} ETH</span>
            </div>
            <el-dropdown @command="handleWalletCommand">
                <el-button size="small" type="primary" plain>
                    <el-icon><Wallet /></el-icon>
                    钱包
                </el-button>
                <template #dropdown>
                    <el-dropdown-menu>
                        <el-dropdown-item command="copy">
                            <el-icon><DocumentCopy /></el-icon>
                            复制地址
                        </el-dropdown-item>
                        <el-dropdown-item command="refresh">
                            <el-icon><Refresh /></el-icon>
                            刷新余额
                        </el-dropdown-item>
                        <el-dropdown-item command="network">
                            <el-icon><Connection /></el-icon>
                            切换网络
                        </el-dropdown-item>
                        <el-dropdown-item command="disconnect" divided>
                            <el-icon><SwitchButton /></el-icon>
                            断开钱包
                        </el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
        </div>
        <div v-else class="wallet-disconnected">
            <el-button 
                type="primary" 
                :loading="walletStore.isConnecting"
                @click="handleConnect"
            >
                <el-icon><Wallet /></el-icon>
                {{ walletStore.isConnecting ? '连接中...' : '连接MetaMask' }}
            </el-button>
        </div>
    </div>
</template>

<script setup>
import { useWalletStore } from '../stores/wallet'
import { ElMessage, ElMessageBox } from 'element-plus'

const walletStore = useWalletStore()

const handleConnect = async () => {
    try {
        const result = await walletStore.connectWallet()
        ElMessage.success(`钱包已连接: ${result.account.substring(0, 6)}...${result.account.substring(result.account.length - 4)}`)
    } catch (error) {
        if (error.code === 4001) {
            ElMessage.warning('用户拒绝了连接请求')
        } else {
            ElMessage.error(error.message || '连接钱包失败')
        }
    }
}

const handleWalletCommand = async (command) => {
    switch (command) {
        case 'copy':
            try {
                await navigator.clipboard.writeText(walletStore.account)
                ElMessage.success('地址已复制到剪贴板')
            } catch {
                ElMessage.error('复制失败')
            }
            break
        case 'refresh':
            await walletStore.updateBalance()
            ElMessage.success('余额已刷新')
            break
        case 'network':
            try {
                await walletStore.switchToGanache()
                ElMessage.success('已切换到Ganache网络')
            } catch (error) {
                ElMessage.error(error.message)
            }
            break
        case 'disconnect':
            try {
                await ElMessageBox.confirm('确定要断开钱包连接吗？', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                })
                walletStore.disconnectWallet()
                ElMessage.success('钱包已断开')
            } catch {}
            break
    }
}
</script>

<style scoped>
.wallet-connect {
    display: flex;
    align-items: center;
}

.wallet-connected {
    display: flex;
    align-items: center;
    gap: 12px;
}

.wallet-info {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 14px;
    background: rgba(102, 126, 234, 0.1);
    border-radius: 20px;
    font-size: 13px;
}

.wallet-indicator {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #67c23a;
    box-shadow: 0 0 6px rgba(103, 194, 58, 0.5);
}

.wallet-address {
    font-weight: 600;
    color: #303133;
}

.wallet-balance {
    color: #667eea;
    font-weight: 600;
}

.wallet-disconnected {
    display: flex;
    align-items: center;
}
</style>
