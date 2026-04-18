<template>
  <div class="task-detail-page">
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading" size="32"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <div v-else-if="task" class="page-content">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="breadcrumb">
          <span class="breadcrumb-item" @click="$router.push('/scheduled-tasks')">任务安排</span>
          <span class="breadcrumb-separator">›</span>
          <span class="breadcrumb-item active">{{ task.name }}</span>
        </div>
        <div class="header-actions">
          <el-button @click="$router.go(-1)">
            <el-icon><ArrowLeft /></el-icon>
            返回列表
          </el-button>
          <el-button
            :type="isEnabled(task.status) ? 'warning' : 'success'"
            @click="handleToggleStatus"
          >
            <el-icon><VideoPause v-if="isEnabled(task.status)" /><VideoPlay v-else /></el-icon>
            {{ isEnabled(task.status) ? '禁用' : '启用' }}
          </el-button>
          <el-button type="primary" @click="handleExecute">
            <el-icon><RefreshRight /></el-icon>
            立即执行
          </el-button>
          <el-button type="primary" @click="handleEdit">
            <el-icon><Edit /></el-icon>
            编辑任务
          </el-button>
          <el-button type="danger" @click="handleDelete">
            <el-icon><Delete /></el-icon>
            删除任务
          </el-button>
        </div>
      </div>

      <!-- 任务详情内容 -->
      <div class="task-detail-content">
        <!-- 任务摘要 -->
        <div class="task-summary">
          <h2>{{ task.name }}</h2>
          <p class="summary-desc">{{ task.description || '暂无描述' }}</p>
          <p class="summary-execution">{{ getFrequencyText(task.frequency || task.triggerType) }} {{ getExecutionTime() }} 自动执行</p>
          <div class="task-status-row">
            <el-tag
              :type="isEnabled(task.status) ? 'success' : 'info'"
              size="large"
            >
              {{ isEnabled(task.status) ? '启用' : '禁用' }}
            </el-tag>
            <el-tag
              v-if="task.lastExecutionStatus"
              :type="getExecutionStatusTagType(task.lastExecutionStatus)"
              size="large"
            >
              {{ getExecutionStatusText(task.lastExecutionStatus) }}
            </el-tag>
          </div>
        </div>

        <!-- 基本信息 -->
        <div class="info-section">
          <h3 class="section-title">基本信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">任务类型</span>
              <span class="info-value">{{ getTaskTypeText(task.type) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">目标名称</span>
              <span class="info-value">{{ task.targetName || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">执行环境</span>
              <span class="info-value">{{ task.executionEnvironment || 'test' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">创建时间</span>
              <span class="info-value">{{ formatDateTime(task.createdAt) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">最后执行</span>
              <span class="info-value">{{ formatDateTime(task.lastRunTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">下次执行</span>
              <span class="info-value">{{ formatDateTime(task.nextRunTime) }}</span>
            </div>
          </div>
        </div>

        <!-- 执行统计 -->
        <div class="info-section">
          <h3 class="section-title">执行统计</h3>
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-value">{{ task.totalExecutions || 0 }}</div>
              <div class="stat-label">总执行次数</div>
            </div>
            <div class="stat-card success">
              <div class="stat-value">{{ task.successfulExecutions || 0 }}</div>
              <div class="stat-label">成功次数</div>
            </div>
            <div class="stat-card danger">
              <div class="stat-value">{{ task.failedExecutions || 0 }}</div>
              <div class="stat-label">失败次数</div>
            </div>
            <div class="stat-card info">
              <div class="stat-value">{{ getSuccessRate() }}</div>
              <div class="stat-label">成功率</div>
            </div>
          </div>
        </div>

        <!-- 执行计划 -->
        <div class="info-section">
          <h3 class="section-title">执行计划</h3>
          <div class="plan-info">
            <div class="plan-item">
              <span class="plan-label">执行频率</span>
              <span class="plan-value">{{ getFrequencyText(task.frequency || task.triggerType) }}</span>
            </div>
            <div class="plan-item">
              <span class="plan-label">执行时间</span>
              <span class="plan-value">{{ getExecutionTime() }}</span>
            </div>
            <div class="plan-item">
              <span class="plan-label">超时设置</span>
              <span class="plan-value">{{ getTimeoutText() }}</span>
            </div>
            <div class="plan-item">
              <span class="plan-label">重试设置</span>
              <span class="plan-value">{{ task.retryEnabled ? `启用 (最多${task.maxRetryAttempts}次)` : '禁用' }}</span>
            </div>
            <div class="plan-item">
              <span class="plan-label">通知设置</span>
              <span class="plan-value">
                {{ task.notifyOnSuccess ? '成功通知 ' : '' }}
                {{ task.notifyOnFailure ? '失败通知' : '' }}
                {{ !task.notifyOnSuccess && !task.notifyOnFailure ? '无' : '' }}
              </span>
            </div>
          </div>
        </div>

        <!-- 最近执行记录 -->
        <div class="info-section">
          <h3 class="section-title">最近执行记录</h3>
          <TaskExecutionHistory :task-id="taskId" />
        </div>
      </div>
    </div>

    <div v-else class="error-container">
      <div class="error-icon">!</div>
      <div class="error-title">任务不存在</div>
      <div class="error-message">找不到ID为 {{ taskId }} 的任务</div>
      <el-button @click="$router.push('/scheduled-tasks')">返回任务列表</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Edit,
  Delete,
  VideoPlay,
  VideoPause,
  RefreshRight,
  ArrowLeft,
  Loading
} from '@element-plus/icons-vue'
import TaskExecutionHistory from '@/components/scheduled-task/TaskExecutionHistory.vue'
import { scheduledTaskApi } from '@/api/modules/scheduling/scheduledTask'
import type { ScheduledTaskDetailVO } from '@/types/api'

const route = useRoute()
const router = useRouter()

const taskId = computed(() => Number(route.params.id))
const loading = ref(false)
const task = ref<ScheduledTaskDetailVO | null>(null)

async function loadTask() {
  loading.value = true
  try {
    const res = await scheduledTaskApi.getDetail(taskId.value)
    if (res) {
      task.value = res
    } else {
      task.value = null
    }
  } catch {
    ElMessage.error('加载任务失败')
    task.value = null
  } finally {
    loading.value = false
  }
}

function isEnabled(status: string): boolean {
  return status === 'ENABLED' || status === 'enabled'
}

function getTaskTypeText(type: string): string {
  const map: Record<string, string> = {
    SINGLE_CASE: '单个用例',
    MODULE: '模块',
    PROJECT: '项目',
    TEST_SUITE: '测试套件',
    API: 'API'
  }
  return map[type] || type || '-'
}

function getFrequencyText(frequency: string): string {
  const map: Record<string, string> = {
    DAILY: '每日执行',
    WEEKLY: '每周执行',
    MONTHLY: '每月执行',
    CRON: 'Cron表达式',
    SIMPLE: '简单重复',
    ONCE: '一次性执行'
  }
  return map[frequency] || frequency || '-'
}

function getExecutionTime(): string {
  if (!task.value) return '-'
  if (task.value.dailyHour !== undefined) {
    const hour = String(task.value.dailyHour).padStart(2, '0')
    const minute = String(task.value.dailyMinute || 0).padStart(2, '0')
    return `${hour}:${minute}`
  }
  return '-'
}

function getTimeoutText(): string {
  if (!task.value) return '-'
  const seconds = task.value.timeoutSeconds
  if (!seconds) return '-'
  return `${Math.ceil(seconds / 60)}分钟`
}

function getSuccessRate(): string {
  if (!task.value) return '0%'
  const total = task.value.totalExecutions || 0
  if (total === 0) return '0%'
  const success = task.value.successfulExecutions || 0
  return ((success / total) * 100).toFixed(1) + '%'
}

function getExecutionStatusTagType(status: string): string {
  const map: Record<string, string> = {
    SUCCESS: 'success',
    FAILURE: 'danger',
    RUNNING: 'warning'
  }
  return map[status] || 'info'
}

function getExecutionStatusText(status: string): string {
  const map: Record<string, string> = {
    SUCCESS: '上次执行成功',
    FAILURE: '上次执行失败',
    RUNNING: '运行中'
  }
  return map[status] || status
}

function formatDateTime(time: string | null | undefined): string {
  if (!time) return '-'
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

async function handleToggleStatus() {
  if (!task.value) return
  try {
    const action = isEnabled(task.value.status) ? '禁用' : '启用'
    await ElMessageBox.confirm(`确定要${action}任务"${task.value.name}"吗？`, `${action}确认`, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    if (isEnabled(task.value.status)) {
      await scheduledTaskApi.disable(task.value.id)
      ElMessage.success('任务已禁用')
    } else {
      await scheduledTaskApi.enable(task.value.id)
      ElMessage.success('任务已启用')
    }
    loadTask()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

async function handleExecute() {
  if (!task.value) return
  try {
    await ElMessageBox.confirm(`确定要立即执行任务"${task.value.name}"吗？`, '执行确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await scheduledTaskApi.execute(task.value.id)
    ElMessage.success('任务已提交执行')
    loadTask()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('执行失败')
    }
  }
}

function handleEdit() {
  if (!task.value) return
  router.push(`/scheduled-tasks/${task.value.id}/edit`)
}

async function handleDelete() {
  if (!task.value) return
  try {
    await ElMessageBox.confirm(`确定要删除任务"${task.value.name}"吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await scheduledTaskApi.delete(task.value.id)
    ElMessage.success('删除成功')
    router.push('/scheduled-tasks')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadTask()
})
</script>

<style scoped lang="scss">
.task-detail-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.page-content {
  padding: 20px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  gap: 12px;
  color: #8c92a4;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.breadcrumb {
  font-size: 14px;
  color: #666;
  cursor: pointer;
}

.breadcrumb-item {
  &:hover {
    color: #409eff;
  }

  &.active {
    color: #409eff;
    font-weight: 500;
  }
}

.breadcrumb-separator {
  margin: 0 8px;
  color: #ccc;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.task-detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.task-summary {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  text-align: center;

  h2 {
    margin: 0 0 8px 0;
    font-size: 28px;
    font-weight: 600;
    color: #303133;
  }
}

.summary-desc {
  margin: 0 0 8px 0;
  color: #606266;
  font-size: 16px;
}

.summary-execution {
  margin: 0 0 16px 0;
  color: #909399;
  font-size: 14px;
}

.task-status-row {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;

  &.success {
    background: #f0f9ff;
    border: 1px solid #bae7ff;
  }

  &.danger {
    background: #fff1f0;
    border: 1px solid #ffccc7;
  }

  &.info {
    background: #f9f9f9;
    border: 1px solid #e8e8e8;
  }
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;

  .success & {
    color: #67c23a;
  }

  .danger & {
    color: #f56c6c;
  }
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.info-section {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.section-title {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-label {
  font-weight: 500;
  color: #606266;
  font-size: 14px;
}

.info-value {
  color: #303133;
  font-size: 14px;
}

.plan-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.plan-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.plan-label {
  font-weight: 500;
  color: #606266;
  min-width: 80px;
}

.plan-value {
  color: #303133;
  font-weight: 500;
}

.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  text-align: center;
}

.error-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #f56c6c;
  color: white;
  font-size: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.error-title {
  font-size: 24px;
  font-weight: bold;
  color: #666;
  margin-bottom: 8px;
}

.error-message {
  color: #999;
  margin-bottom: 24px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .header-actions {
    flex-wrap: wrap;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .plan-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
