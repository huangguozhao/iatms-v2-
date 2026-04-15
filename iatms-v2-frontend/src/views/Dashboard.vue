<template>
  <div class="dashboard page-container">
    <h2 class="page-title">仪表盘</h2>

    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in stats" :key="stat.title">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" :style="{ backgroundColor: stat.color }">
              <el-icon :size="24"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-title">{{ stat.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>最近项目</span>
          </template>
          <el-table :data="recentProjects" style="width: 100%">
            <el-table-column prop="name" label="项目名称" />
            <el-table-column prop="code" label="项目编号" width="150" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ row.statusText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="180" />
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <span>快捷入口</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="router.push('/projects')">新建项目</el-button>
            <el-button type="success" @click="router.push('/apis')">接口管理</el-button>
            <el-button type="warning" @click="router.push('/test-cases')">用例管理</el-button>
            <el-button type="danger" @click="router.push('/scheduled-tasks')">定时任务</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { projectApi } from '@/api/modules/project/project'
import { Folder, Connection, Document, Clock } from '@element-plus/icons-vue'

const router = useRouter()

const stats = ref([
  { title: '项目总数', value: 12, icon: Folder, color: '#409EFF' },
  { title: '接口总数', value: 156, icon: Connection, color: '#67C23A' },
  { title: '用例总数', value: 892, icon: Document, color: '#E6A23C' },
  { title: '定时任务', value: 24, icon: Clock, color: '#F56C6C' }
])

const recentProjects = ref<any[]>([])

function getStatusType(status: string) {
  const map: Record<string, string> = {
    NOT_STARTED: 'info',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success',
    ARCHIVED: 'warning'
  }
  return map[status] || 'info'
}

onMounted(async () => {
  try {
    recentProjects.value = await projectApi.getRecent(5)
  } catch (error) {
    console.error('获取最近项目失败:', error)
  }
})
</script>

<style scoped lang="scss">
.dashboard {
  .page-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 600;
  }
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 15px;

  .stat-icon {
    width: 60px;
    height: 60px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }

  .stat-info {
    .stat-value {
      font-size: 28px;
      font-weight: bold;
      color: #333;
    }

    .stat-title {
      font-size: 14px;
      color: #999;
      margin-top: 5px;
    }
  }
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;

  .el-button {
    width: 100%;
  }
}
</style>
