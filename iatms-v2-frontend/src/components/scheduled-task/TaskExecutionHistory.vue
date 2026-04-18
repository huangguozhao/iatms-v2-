<template>
  <div class="execution-history">
    <div v-if="loading" class="loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
    <div v-else-if="!executions.length" class="empty">
      <el-empty description="暂无执行记录" />
    </div>
    <div v-else class="history-list">
      <div
        v-for="item in executions"
        :key="item.executionId"
        class="execution-item"
      >
        <div class="execution-header">
          <div class="execution-time">
            <el-icon><Clock /></el-icon>
            <span>{{ formatTime(item.scheduledTime) }}</span>
          </div>
          <div class="execution-duration" v-if="item.durationSeconds">
            耗时: {{ formatDuration(item.durationSeconds) }}
          </div>
        </div>
        <div class="execution-stats">
          <div class="stat-item success">
            <el-icon><SuccessFilled /></el-icon>
            <span>通过 {{ item.passedCases }}</span>
          </div>
          <div class="stat-item failed" v-if="item.failedCases > 0">
            <el-icon><CircleCloseFilled /></el-icon>
            <span>失败 {{ item.failedCases }}</span>
          </div>
          <div class="stat-item skipped" v-if="item.skippedCases > 0">
            <el-icon><Warning /></el-icon>
            <span>跳过 {{ item.skippedCases }}</span>
          </div>
        </div>
        <div class="execution-status">
          <el-tag :type="getStatusType(item.status)" size="small">
            {{ getStatusText(item.status) }}
          </el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  Clock,
  SuccessFilled,
  CircleCloseFilled,
  Warning
} from '@element-plus/icons-vue'

const props = defineProps<{
  taskId: number
}>()

const loading = ref(false)
const executions = ref<any[]>([])

async function loadExecutions() {
  loading.value = false
  // 后端暂未实现执行历史接口，暂不加载
  executions.value = []
}

function formatTime(time: string): string {
  if (!time) return '-'
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

function formatDuration(seconds: number): string {
  if (!seconds) return '-'
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  return minutes > 0 ? `${minutes}分${secs}秒` : `${secs}秒`
}

function getStatusType(status: string): string {
  const map: Record<string, string> = {
    SUCCESS: 'success',
    FAILURE: 'danger',
    RUNNING: 'warning',
    PENDING: 'info',
    SKIPPED: 'warning',
    TIMEOUT: 'danger'
  }
  return map[status] || 'info'
}

function getStatusText(status: string): string {
  const map: Record<string, string> = {
    SUCCESS: '成功',
    FAILURE: '失败',
    RUNNING: '运行中',
    PENDING: '等待中',
    SKIPPED: '已跳过',
    TIMEOUT: '超时'
  }
  return map[status] || status
}

onMounted(() => {
  // 后端暂未实现执行历史接口，暂不加载
})
</script>

<style scoped lang="scss">
.execution-history {
  .loading {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 20px;
    color: #8c92a4;
  }

  .empty {
    padding: 20px;
  }

  .history-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .execution-item {
    padding: 16px;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    background: #fafafa;
    transition: all 0.2s;

    &:hover {
      background: #f5f7fa;
      border-color: #c0c4cc;
    }
  }

  .execution-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .execution-time {
    display: flex;
    align-items: center;
    gap: 6px;
    font-weight: 500;
    color: #303133;
  }

  .execution-duration {
    color: #909399;
    font-size: 13px;
  }

  .execution-stats {
    display: flex;
    gap: 16px;
    margin-bottom: 8px;
  }

  .stat-item {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    font-weight: 500;

    &.success {
      color: #67c23a;
    }

    &.failed {
      color: #f56c6c;
    }

    &.skipped {
      color: #e6a23c;
    }
  }

  .execution-status {
    display: flex;
    justify-content: flex-start;
  }
}
</style>
