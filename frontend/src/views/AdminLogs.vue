<template>
    <div>
        <div class="page-header">
            <h2 class="page-title">系统操作日志</h2>
            <p class="page-desc">管理员对用户状态、信用分等关键信息的修改记录</p>
        </div>

        <div class="card-container">
            <div class="card-title">日志记录</div>
            
            <el-table :data="logs" v-loading="loading" style="width: 100%">
                <el-table-column prop="createdTime" label="操作时间" width="180">
                    <template #default="{ row }">
                        {{ formatTime(row.createdTime) }}
                    </template>
                </el-table-column>
                <el-table-column prop="operatorName" label="管理员" width="120">
                    <template #default="{ row }">
                        <el-tag size="small">{{ row.operatorName }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="operationType" label="操作类型" width="120">
                    <template #default="{ row }">
                        <el-tag :type="getTypeTag(row.operationType)" size="small">
                            {{ row.operationType }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="targetName" label="目标用户" width="120" />
                <el-table-column prop="operationDetail" label="详情描述" />
            </el-table>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const logs = ref([])
const loading = ref(false)

const fetchLogs = async () => {
    loading.value = true
    try {
        const response = await axios.get('/api/user/logs')
        if (response.data.code === 200) {
            logs.value = response.data.data
        }
    } catch (error) {
        ElMessage.error('获取日志失败')
    } finally {
        loading.value = false
    }
}

const formatTime = (timeStr) => {
    if (!timeStr) return ''
    const date = new Date(timeStr)
    return date.toLocaleString('zh-CN')
}

const getTypeTag = (type) => {
    switch (type) {
        case '冻结账户': return 'danger'
        case '解冻账户': return 'success'
        case '修改评分': return 'warning'
        default: return 'info'
    }
}

onMounted(() => {
    fetchLogs()
})
</script>

<style scoped>
.page-header {
    margin-bottom: 24px;
}

.page-title {
    font-size: 24px;
    font-weight: 600;
    color: #1a1a1a;
    margin: 0 0 8px 0;
}

.page-desc {
    font-size: 14px;
    color: #606266;
    margin: 0;
}

.card-container {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

.card-title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 20px;
    color: #303133;
}
</style>
