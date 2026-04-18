<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    title="测试执行结果"
    :width="dialogWidth"
    :close-on-click-modal="false"
    destroy-on-close
    class="execution-result-dialog"
  >
    <div class="execution-result-container" v-if="executionResult">
      <!-- 结果状态横幅 -->
      <div class="result-banner" :class="'status-' + displayStatus">
        <div class="banner-icon-wrapper">
          <el-icon v-if="displayStatus === 'passed'" :size="56">
            <CircleCheckFilled />
          </el-icon>
          <el-icon v-else-if="displayStatus === 'failed'" :size="56">
            <CircleCloseFilled />
          </el-icon>
          <el-icon v-else :size="56">
            <Loading />
          </el-icon>
        </div>
        <div class="banner-content">
          <h3 class="result-title">
            {{ getStatusTitle(displayStatus) }}
          </h3>
          <p class="result-subtitle">
            <el-tag size="small" effect="plain">
              {{ executionResult.caseName || executionResult.scopeName || '未知用例' }}
            </el-tag>
          </p>
        </div>
        <div class="banner-badge" :class="displayStatus">
          {{ getStatusBadge(displayStatus) }}
        </div>
      </div>

      <!-- 执行信息卡片 -->
      <div class="result-info-section">
        <div class="info-grid" :class="{ 'has-assertions': hasAssertions }">
          <div class="info-card">
            <div class="info-card-header">
              <el-icon><Ticket /></el-icon>
              <span class="info-label">执行ID</span>
            </div>
            <div class="info-value code">{{ executionResult.executionId || executionResult.recordId || '-' }}</div>
          </div>

          <div class="info-card">
            <div class="info-card-header">
              <el-icon><User /></el-icon>
              <span class="info-label">执行人</span>
            </div>
            <div class="info-value">{{ executionResult.executor || '-' }}</div>
          </div>

          <div class="info-card">
            <div class="info-card-header">
              <el-icon><Timer /></el-icon>
              <span class="info-label">执行耗时</span>
            </div>
            <div class="info-value highlight">
              <span class="duration-value">{{ formatDuration(executionResult.duration || executionResult.durationSeconds) }}</span>
            </div>
          </div>

          <div class="info-card" v-if="hasAssertions">
            <div class="info-card-header">
              <el-icon><Checked /></el-icon>
              <span class="info-label">断言结果</span>
            </div>
            <div class="info-value assertion-values">
              <span class="assertion-item passed">
                <el-icon><CircleCheck /></el-icon>
                {{ executionResult.assertionsPassed ?? 0 }} 通过
              </span>
              <span class="assertion-divider">/</span>
              <span class="assertion-item failed">
                <el-icon><CircleClose /></el-icon>
                {{ executionResult.assertionsFailed ?? 0 }} 失败
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 时间信息 -->
      <div class="result-time-section">
        <div class="time-item" v-if="executionResult.startTime">
          <div class="time-icon">
            <el-icon><VideoPlay /></el-icon>
          </div>
          <div class="time-content">
            <span class="time-label">开始时间</span>
            <span class="time-value">{{ formatTime(executionResult.startTime) }}</span>
          </div>
        </div>
        <div class="time-item" v-if="executionResult.endTime">
          <div class="time-icon">
            <el-icon><VideoPause /></el-icon>
          </div>
          <div class="time-content">
            <span class="time-label">结束时间</span>
            <span class="time-value">{{ formatTime(executionResult.endTime) }}</span>
          </div>
        </div>
        <div class="time-item" v-if="executionResult.environment">
          <div class="time-icon">
            <el-icon><Connection /></el-icon>
          </div>
          <div class="time-content">
            <span class="time-label">执行环境</span>
            <span class="time-value">
              <el-tag size="small" type="info">{{ executionResult.environment }}</el-tag>
            </span>
          </div>
        </div>
      </div>

      <!-- 失败详情区域 -->
      <div class="failure-section" v-if="displayStatus === 'failed'">
        <div class="failure-header">
          <el-icon><WarningFilled /></el-icon>
          <span>失败详情</span>
        </div>

        <div class="failure-content">
          <div class="failure-message" v-if="effectiveErrorMessage">
            <div class="detail-label">
              <el-icon><InfoFilled /></el-icon>
              错误信息
            </div>
            <pre class="error-message">{{ effectiveErrorMessage }}</pre>
          </div>

          <div class="failure-type" v-if="executionResult.failureType">
            <div class="detail-label">
              <el-icon><Warning /></el-icon>
              失败类型
            </div>
            <el-tag type="danger">{{ getFailureTypeText(executionResult.failureType) }}</el-tag>
          </div>
        </div>
      </div>

      <!-- 响应详情（如果有） -->
      <div class="response-section" v-if="hasResponseDetails">
        <div class="response-header">
          <el-icon><Document /></el-icon>
          <span>响应详情</span>
        </div>

        <div class="response-tabs">
          <el-tabs v-model="activeResponseTab">
            <el-tab-pane label="响应体" name="body" v-if="executionResult.responseBody">
              <div class="response-code">
                <pre>{{ formatResponseBody() }}</pre>
              </div>
            </el-tab-pane>
            <el-tab-pane label="响应头" name="headers" v-if="executionResult.responseHeaders">
              <div class="response-code">
                <pre>{{ formatResponseHeaders() }}</pre>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-result">
      <el-empty description="暂无执行结果" />
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="$emit('update:visible', false)">关闭</el-button>
        <el-button
          v-if="displayStatus === 'failed'"
          type="warning"
          @click="$emit('retry')"
        >
          <el-icon><RefreshRight /></el-icon>
          重新执行
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  CircleCheckFilled,
  CircleCloseFilled,
  Ticket,
  Connection,
  Timer,
  Checked,
  CircleCheck,
  CircleClose,
  User,
  WarningFilled,
  InfoFilled,
  Warning,
  Document,
  RefreshRight,
  VideoPlay,
  VideoPause,
  Loading
} from '@element-plus/icons-vue'
import type { ExecutionResult } from '@/types/components'

interface Props {
  visible: boolean
  executionResult: ExecutionResult | null
}

const props = defineProps<Props>()

defineEmits<{
  'update:visible': [value: boolean]
  retry: []
}>()

const activeResponseTab = ref('body')

// 对话框宽度
const dialogWidth = computed(() => {
  return '800px'
})

// 处理字段名映射（兼容不同的字段名）
const effectiveErrorMessage = computed(() => {
  return props.executionResult?.errorMessage || props.executionResult?.failureMessage || ''
})

// 是否有响应详情
const hasResponseDetails = computed(() => {
  return props.executionResult?.responseBody || props.executionResult?.responseHeaders
})

// 获取显示状态
const displayStatus = computed(() => {
  if (!props.executionResult?.status) return 'unknown'
  const status = props.executionResult.status.toLowerCase()
  if (status === 'passed' || status === 'success' || status === 'completed') return 'passed'
  if (status === 'failed' || status === 'error') return 'failed'
  if (status === 'running') return 'running'
  return 'unknown'
})

// 是否有断言信息
const hasAssertions = computed(() => {
  const result = props.executionResult
  return result && (result.assertionsPassed !== undefined || result.assertionsFailed !== undefined)
})

// 获取状态标题
function getStatusTitle(status: string): string {
  const titleMap: Record<string, string> = {
    passed: '测试通过',
    failed: '测试失败',
    running: '执行中...',
    unknown: '状态未知'
  }
  return titleMap[status] || '状态未知'
}

// 获取状态徽章文本
function getStatusBadge(status: string): string {
  const badgeMap: Record<string, string> = {
    passed: 'SUCCESS',
    failed: 'FAILED',
    running: 'RUNNING',
    unknown: 'UNKNOWN'
  }
  return badgeMap[status] || 'UNKNOWN'
}

// 格式化持续时间
function formatDuration(seconds: number | undefined): string {
  if (!seconds && seconds !== 0) return '-'

  if (seconds < 1) {
    return `${Math.round(seconds * 1000)}ms`
  }
  if (seconds < 60) {
    return `${seconds.toFixed(2)}s`
  }
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}m ${remainingSeconds.toFixed(0)}s`
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
      minute: '2-digit',
      second: '2-digit'
    }).replace(/\//g, '-')
  }

  return time
}

// 获取失败类型文本
function getFailureTypeText(type: string | undefined): string {
  const typeMap: Record<string, string> = {
    'assertion': '断言失败',
    'timeout': '执行超时',
    'network': '网络错误',
    'system': '系统错误',
    'auth': '认证错误',
    'validation': '验证错误',
    'unknown': '未知错误'
  }
  return type ? (typeMap[type.toLowerCase()] || type) : '未知错误'
}

// 格式化响应体
function formatResponseBody(): string {
  const body = props.executionResult?.responseBody
  if (!body) return ''

  if (typeof body === 'object') {
    return JSON.stringify(body, null, 2)
  }

  if (typeof body === 'string') {
    try {
      return JSON.stringify(JSON.parse(body), null, 2)
    } catch {
      return body
    }
  }

  return String(body)
}

// 格式化响应头
function formatResponseHeaders(): string {
  const headers = props.executionResult?.responseHeaders
  if (!headers) return ''

  if (typeof headers === 'object') {
    return JSON.stringify(headers, null, 2)
  }

  if (typeof headers === 'string') {
    try {
      return JSON.stringify(JSON.parse(headers), null, 2)
    } catch {
      return headers
    }
  }

  return String(headers)
}
</script>

<style scoped lang="scss">
.execution-result-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.result-banner {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 2px solid transparent;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
  }

  &.status-passed {
    background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
    border-color: #86efac;

    &::before {
      background: linear-gradient(90deg, #22c55e, #86efac);
    }
  }

  &.status-failed {
    background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
    border-color: #fca5a5;

    &::before {
      background: linear-gradient(90deg, #ef4444, #fca5a5);
    }
  }

  &.status-running {
    background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
    border-color: #93c5fd;

    &::before {
      background: linear-gradient(90deg, #3b82f6, #93c5fd);
    }
  }

  &.status-unknown {
    background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
    border-color: #d1d5db;

    &::before {
      background: linear-gradient(90deg, #6b7280, #d1d5db);
    }
  }
}

.banner-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;

  .status-passed & {
    color: #22c55e;
  }

  .status-failed & {
    color: #ef4444;
  }

  .status-running & {
    color: #3b82f6;
  }

  .status-unknown & {
    color: #6b7280;
  }
}

.banner-content {
  flex: 1;
  min-width: 0;
}

.result-title {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: #1f2937;
  letter-spacing: 1px;
}

.result-subtitle {
  margin: 8px 0 0;
}

.banner-badge {
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 1px;
  flex-shrink: 0;

  &.status-passed {
    background: #22c55e;
    color: white;
  }

  &.status-failed {
    background: #ef4444;
    color: white;
  }

  &.status-running {
    background: #3b82f6;
    color: white;
  }

  &.status-unknown {
    background: #6b7280;
    color: white;
  }
}

.result-info-section {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e5e7eb;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;

  &.has-assertions {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: repeat(2, 1fr);

    &.has-assertions {
      grid-template-columns: 1fr;
    }
  }
}

.info-card {
  background: white;
  border-radius: 10px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }
}

.info-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: #6b7280;
  font-size: 13px;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #374151;
  font-weight: 500;

  &.code {
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 12px;
    word-break: break-all;
  }

  &.highlight {
    font-weight: 700;
  }
}

.duration-value {
  font-size: 20px;
  font-weight: 700;
  color: #3b82f6;
}

.assertion-values {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.assertion-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 600;

  &.passed {
    color: #22c55e;
  }

  &.failed {
    color: #ef4444;
  }
}

.assertion-divider {
  color: #d1d5db;
}

.result-time-section {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f9fafb 0%, #ffffff 100%);
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.time-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.time-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3b82f6;
  flex-shrink: 0;
}

.time-content {
  display: flex;
  flex-direction: column;
}

.time-label {
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 2px;
}

.time-value {
  font-size: 14px;
  color: #374151;
  font-weight: 600;
}

.failure-section {
  background: linear-gradient(135deg, #fef2f2 0%, #fff5f5 100%);
  border: 1px solid #fecaca;
  border-radius: 12px;
  padding: 20px;
}

.failure-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #dc2626;
  margin-bottom: 16px;
}

.failure-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  margin-bottom: 8px;
}

.error-message {
  background: #1f2937;
  border: 1px solid #374151;
  border-radius: 8px;
  padding: 14px 16px;
  margin: 0;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  color: #fbbf24;
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.6;
  max-height: 200px;
  overflow-y: auto;
}

.response-section {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e5e7eb;
}

.response-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 16px;
}

.response-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 12px;
  }
}

.response-code {
  background: #1f2937;
  border: 1px solid #374151;
  border-radius: 8px;
  padding: 12px 16px;
  max-height: 300px;
  overflow: auto;

  pre {
    margin: 0;
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 12px;
    line-height: 1.5;
    color: #e5e7eb;
    white-space: pre-wrap;
    word-break: break-all;
  }
}

.empty-result {
  padding: 40px 0;
  text-align: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .result-banner {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .result-time-section {
    flex-direction: column;
    gap: 16px;
  }
}
</style>
