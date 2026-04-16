<template>
  <div class="case-detail-sidebar">
    <!-- 执行历史 -->
    <el-card class="sidebar-card" shadow="hover">
      <template #header>
        <div class="sidebar-header">
          <el-icon><Clock /></el-icon>
          <span>执行历史</span>
        </div>
      </template>
      <div v-loading="executionHistoryLoading" element-loading-text="加载中..." style="min-height: 100px;">
        <div v-if="displayHistory.length > 0" class="history-list">
          <div
            v-for="(history, index) in displayHistory"
            :key="index"
            class="history-card clickable"
            @click="$emit('view-history-detail', history)"
          >
            <div class="history-header">
              <el-icon
                :color="getStatusColor(history.status)"
                :size="16"
              >
                <CircleCheckFilled v-if="history.status === 'passed' || history.status === 'success'" />
                <CircleCloseFilled v-else-if="history.status === 'failed' || history.status === 'error'" />
                <WarningFilled v-else />
              </el-icon>
              <div class="executor-info">
                <div class="executor-name">{{ history.executor || history.executorName || '未知' }}</div>
                <div class="executor-meta">
                  <span class="execution-type">{{ getExecutionTypeText(history.type || history.action) }}</span>
                  <span class="environment" v-if="history.environment">{{ history.environment }}</span>
                </div>
              </div>
            </div>
            <div class="history-body">{{ history.note || history.remark || '无备注' }}</div>
            <div class="history-footer">
              <span class="execution-time">{{ formatTime(history.executedAt || history.executed_time || history.createTime) }}</span>
              <span class="duration" v-if="getDuration(history) > 0">
                ({{ formatDuration(getDuration(history)) }})
              </span>
              <el-icon class="view-detail-icon"><View /></el-icon>
            </div>
          </div>
        </div>
        <div v-else-if="!executionHistoryLoading" class="empty-history">
          <el-empty
            :image-size="50"
            description="暂无执行记录"
          >
            <template #description>
              <p class="empty-main-text">该测试用例尚未执行</p>
              <p class="empty-tip">执行测试后将显示历史记录</p>
            </template>
          </el-empty>
        </div>

        <!-- 查看更多按钮 -->
        <div v-if="showViewMore && !executionHistoryLoading" class="view-more-section">
          <el-button
            type="primary"
            size="small"
            text
            :icon="View"
            @click="$emit('view-more-history')"
            class="view-more-btn"
          >
            查看更多执行历史 (共{{ executionHistoryTotal }}条)
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 关联信息 (预留) -->
    <el-card class="sidebar-card" shadow="hover">
      <template #header>
        <div class="sidebar-header">
          <el-icon><Link /></el-icon>
          <span>关联信息</span>
        </div>
      </template>
      <div class="placeholder-content">
        <el-empty :image-size="40" description="暂无关联信息">
          <template #image>
            <el-icon :size="40" color="#c0c4cc"><Link /></el-icon>
          </template>
        </el-empty>
      </div>
    </el-card>

    <!-- 讨论区 (预留) -->
    <el-card class="sidebar-card" shadow="hover">
      <template #header>
        <div class="sidebar-header">
          <el-icon><ChatLineSquare /></el-icon>
          <span>讨论</span>
        </div>
      </template>
      <div class="placeholder-content">
        <el-empty :image-size="40" description="暂无讨论">
          <template #image>
            <el-icon :size="40" color="#c0c4cc"><ChatLineSquare /></el-icon>
          </template>
        </el-empty>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  Clock,
  CircleCheckFilled,
  CircleCloseFilled,
  WarningFilled,
  View,
  Link,
  ChatLineSquare
} from '@element-plus/icons-vue'
import type { ExecutionHistory } from '@/types/components'

interface Props {
  displayHistory?: ExecutionHistory[]
  executionHistoryLoading?: boolean
  executionHistoryTotal?: number
}

const props = withDefaults(defineProps<Props>(), {
  displayHistory: () => [],
  executionHistoryLoading: false,
  executionHistoryTotal: 0
})

defineEmits<{
  'view-history-detail': [history: ExecutionHistory]
  'view-more-history': []
}>()

// 是否显示"查看更多"按钮
const showViewMore = computed(() => {
  return props.executionHistoryTotal > 3
})

// 获取状态颜色
function getStatusColor(status: string | undefined): string {
  if (!status) return '#909399'
  const statusLower = status.toLowerCase()
  if (statusLower === 'passed' || statusLower === 'success') return '#67c23a'
  if (statusLower === 'failed' || statusLower === 'error') return '#f56c6c'
  if (statusLower === 'pending' || statusLower === 'running') return '#e6a23c'
  return '#909399'
}

// 获取执行类型文本
function getExecutionTypeText(type: string | undefined): string {
  if (!type) return '执行'
  const typeMap: Record<string, string> = {
    'manual': '手动执行',
    'auto': '自动执行',
    'scheduled': '定时执行',
    'api': 'API触发'
  }
  return typeMap[type.toLowerCase()] || type
}

// 获取执行时长（秒）
function getDuration(history: ExecutionHistory): number {
  return history.durationSeconds || history.duration || 0
}

// 格式化持续时间
function formatDuration(seconds: number): string {
  if (!seconds || seconds === 0) return '0秒'

  if (seconds < 60) {
    return `${seconds}秒`
  } else if (seconds < 3600) {
    const minutes = Math.floor(seconds / 60)
    const remainingSeconds = seconds % 60
    return remainingSeconds > 0 ? `${minutes}分${remainingSeconds}秒` : `${minutes}分钟`
  } else {
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    const remainingSeconds = seconds % 60

    let result = `${hours}小时`
    if (minutes > 0) {
      result += `${minutes}分钟`
    }
    if (remainingSeconds > 0) {
      result += `${remainingSeconds}秒`
    }
    return result
  }
}

// 格式化时间
function formatTime(time: string | undefined): string {
  if (!time) return '-'

  if (typeof time === 'string' && time.includes('T')) {
    const date = new Date(time)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    }).replace(/\//g, '-')
  }

  return time
}
</script>

<style scoped lang="scss">
.case-detail-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 320px;
  flex-shrink: 0;
}

.sidebar-card {
  :deep(.el-card__header) {
    padding: 12px 20px;
    background: #f5f7fa;
    border-radius: 8px 8px 0 0;
  }
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-card {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background-color: #fafafa;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    border-color: #c0c4cc;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}

.history-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.executor-info {
  flex: 1;
  min-width: 0;
}

.executor-name {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
  margin-bottom: 2px;
}

.executor-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}

.execution-type {
  background-color: #ecf5ff;
  color: #409eff;
  padding: 2px 6px;
  border-radius: 3px;
}

.environment {
  background-color: #f0f9ff;
  color: #0ea5e9;
  padding: 2px 6px;
  border-radius: 3px;
}

.history-body {
  color: #606266;
  font-size: 13px;
  line-height: 1.4;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.history-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.execution-time {
  flex: 1;
}

.duration {
  color: #67c23a;
  margin-right: 8px;
}

.view-detail-icon {
  color: #c0c4cc;
  transition: color 0.2s ease;
}

.history-card:hover .view-detail-icon {
  color: #409eff;
}

.empty-history {
  text-align: center;
  padding: 20px;
}

.empty-main-text {
  color: #606266;
  margin: 0;
}

.empty-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.view-more-section {
  margin-top: 16px;
  text-align: center;
}

.view-more-btn {
  width: 100%;
}

.placeholder-content {
  min-height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 1024px) {
  .case-detail-sidebar {
    width: 100%;
    order: -1;
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .sidebar-card {
    :deep(.el-card__header) {
      padding: 10px 16px;
    }
  }

  .history-card {
    padding: 10px;
  }
}
</style>
