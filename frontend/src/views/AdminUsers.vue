<template>
    <div>
        <div class="page-header">
            <h2 class="page-title">用户管理</h2>
            <p class="page-desc">全网用户状态监控、角色管理与信用评级</p>
        </div>

        <div class="stat-cards">
            <div class="stat-card">
                <div class="stat-icon blue">
                    <el-icon><User /></el-icon>
                </div>
                <div class="stat-value">{{ users.length }}</div>
                <div class="stat-label">总用户数</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon green">
                    <el-icon><CircleCheck /></el-icon>
                </div>
                <div class="stat-value">{{ users.filter(u => u.status === 0).length }}</div>
                <div class="stat-label">正常用户</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon red">
                    <el-icon><CircleClose /></el-icon>
                </div>
                <div class="stat-value">{{ users.filter(u => u.status === 1).length }}</div>
                <div class="stat-label">已冻结用户</div>
            </div>
        </div>

        <div class="card-container">
            <div class="card-title">用户列表</div>
            
            <div class="filter-bar" style="margin-bottom: 20px; display: flex; gap: 15px;">
                <el-input v-model="searchQuery" placeholder="搜索用户名/地址" style="width: 250px" clearable>
                    <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
                <el-select v-model="roleFilter" placeholder="角色筛选" style="width: 150px" clearable>
                    <el-option label="产电方" :value="1" />
                    <el-option label="用电方" :value="2" />
                    <el-option label="管理方" :value="3" />
                </el-select>
            </div>

            <el-table :data="filteredUsers" style="width: 100%" v-loading="loading">
                <el-table-column prop="username" label="用户名" width="120" />
                <el-table-column label="身份" width="120">
                    <template #default="{ row }">
                        <el-tag :type="getRoleType(row.role)">{{ getRoleName(row.role) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="区块链地址" width="180">
                    <template #default="{ row }">
                        <el-tooltip :content="row.blockchainAddress" placement="top">
                            <span>{{ formatAddress(row.blockchainAddress) }}</span>
                        </el-tooltip>
                    </template>
                </el-table-column>
                <el-table-column prop="trustScore" label="信用分" width="100">
                    <template #default="{ row }">
                        <span :style="{ color: getTrustColor(row.trustScore), fontWeight: 'bold' }">
                            {{ row.trustScore }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column label="账户状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 0 ? 'success' : 'danger'">
                            {{ row.status === 0 ? '正常' : '已冻结' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作">
                    <template #default="{ row }">
                        <el-button 
                            size="small" 
                            :type="row.status === 0 ? 'danger' : 'success'"
                            @click="toggleStatus(row)"
                            v-if="row.role !== 3"
                        >
                            {{ row.status === 0 ? '冻结' : '解冻' }}
                        </el-button>
                        <el-button 
                            size="small" 
                            type="primary" 
                            @click="editTrust(row)"
                            v-if="row.role !== 3"
                        >
                            评分
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>

        <!-- 信用评分弹窗 -->
        <el-dialog v-model="trustDialogVisible" title="修改用户信用分" width="30%">
            <el-form label-width="80px">
                <el-form-item label="用户名">
                    <span>{{ currentUser?.username }}</span>
                </el-form-item>
                <el-form-item label="信用分">
                    <el-input-number v-model="newTrustScore" :min="0" :max="100" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="trustDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitTrust">确定</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref([])
const loading = ref(false)
const searchQuery = ref('')
const roleFilter = ref(null)

const trustDialogVisible = ref(false)
const currentUser = ref(null)
const newTrustScore = ref(100)

const fetchUsers = async () => {
    loading.value = true
    try {
        const response = await axios.get('/api/user/list')
        if (response.data.code === 200) {
            users.value = response.data.data || []
        }
    } catch (error) {
        ElMessage.error('获取用户列表失败')
    } finally {
        loading.value = false
    }
}

const filteredUsers = computed(() => {
    return users.value.filter(u => {
        const matchSearch = u.username.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
                          u.blockchainAddress.toLowerCase().includes(searchQuery.value.toLowerCase())
        const matchRole = roleFilter.value ? u.role === roleFilter.value : true
        return matchSearch && matchRole
    })
})

const getRoleName = (role) => {
    const roles = { 1: '产电方', 2: '用电方', 3: '管理方' }
    return roles[role] || '未知'
}

const getRoleType = (role) => {
    const types = { 1: 'success', 2: 'warning', 3: 'danger' }
    return types[role] || 'info'
}

const getTrustColor = (score) => {
    if (score >= 90) return '#67C23A'
    if (score >= 60) return '#E6A23C'
    return '#F56C6C'
}

const formatAddress = (addr) => {
    return addr ? `${addr.slice(0, 6)}...${addr.slice(-4)}` : ''
}

const toggleStatus = (user) => {
    const action = user.status === 0 ? '冻结' : '解冻'
    ElMessageBox.confirm(`确定要${action}该账户吗？`, '提示', {
        type: 'warning'
    }).then(async () => {
        try {
            const response = await axios.post('/api/user/status', {
                id: user.id,
                status: user.status === 0 ? 1 : 0
            })
            if (response.data.code === 200) {
                ElMessage.success(`${action}成功`)
                fetchUsers()
            }
        } catch (error) {
            ElMessage.error(`${action}失败`)
        }
    })
}

const editTrust = (user) => {
    currentUser.value = user
    newTrustScore.value = user.trustScore
    trustDialogVisible.value = true
}

const submitTrust = async () => {
    try {
        const response = await axios.post('/api/user/trust', {
            id: currentUser.value.id,
            trustScore: newTrustScore.value
        })
        if (response.data.code === 200) {
            ElMessage.success('信用分修改成功')
            trustDialogVisible.value = false
            fetchUsers()
        }
    } catch (error) {
        ElMessage.error('修改失败')
    }
}

onMounted(fetchUsers)
</script>

<style scoped>
.page-header { margin-bottom: 24px; }
.page-title { font-size: 24px; color: #303133; margin: 0 0 8px 0; }
.page-desc { font-size: 14px; color: #909399; margin: 0; }
.stat-cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 24px; }
.stat-card { background: white; padding: 20px; border-radius: 12px; display: flex; flex-direction: column; align-items: center; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.stat-icon { font-size: 24px; width: 48px; height: 48px; border-radius: 24px; display: flex; align-items: center; justify-content: center; margin-bottom: 12px; }
.stat-icon.blue { background: #ecf5ff; color: #409eff; }
.stat-icon.green { background: #f0f9eb; color: #67c23a; }
.stat-icon.red { background: #fef0f0; color: #f56c6c; }
.stat-value { font-size: 24px; font-weight: bold; color: #303133; margin-bottom: 4px; }
.stat-label { font-size: 14px; color: #909399; }
.card-container { background: white; padding: 24px; border-radius: 12px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.card-title { font-size: 18px; font-weight: bold; color: #303133; margin-bottom: 20px; }
</style>
