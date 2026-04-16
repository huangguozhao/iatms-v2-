<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    title="测试执行结果"
    width="800px"
    :close-on-click-modal="false"
    destroy-on-close
    class="execution-result-dialog"
  >
    <div class="execution-result-container" v-if="executionResult">
      <!-- 结果状态横幅 -->
      <div class="result-banner" :class="'status-' + displayStatus">
        <div class="banner-icon-wrapper">
          <el-icon v-if="displayStatus === 'passed'" :size="48">
            <CircleCheckFilled />
          </el-icon>
          <el-icon v-else :size="48">
            <CircleCloseFilled />
          </el-icon>
        </div>
        <div class="banner-content">
          <h3 class="result-title">
            {{ displayStatus === 'passed' ? '测试通过' : '测试失败' }}
          </h3>
          <p class="result-subtitle">
            <el-tag size="small" effect="plain">
              {{ executionResult.caseName || executionResult.scopeName || '未知用例' }}
            </el-tag>
          </p>
        </div>
        <div class="banner-badge" :class="displayStatus">
          {{ displayStatus === 'passed' ? 'SUCCESS' : 'FAILED' }}
        </div>
      </div>

      <!-- 执行信息卡片 -->
      <div class="result-info-section">
        <div class="info-grid">
          <div class="info-card">
            <div class="info-card-header">
              <el-icon><Ticket /></el-icon>
              <span class="info-label">执行ID</span>
            </div>
            <div class="info-value code">{{ executionResult.recordId || executionResult.executionId }}</div>
          </div>

          <div class="info-card">
            <div class="info-card-header">
              <el-icon><Connection /></el-icon>
              <span class="info-label">响应状态</span>
            </div>
            <div class="info-value">
              <el-tag
                :type="getStatusTagType(executionResult.responseStatus)"
                size="large"
                effect="dark"
              >
                {{ executionResult.responseStatus || '-' }}
              </el-tag>
            </div>
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

          <div class="info-card assertion-card" v-if="hasAssertions">
            <div class="info-card-header">
              <el-icon><Checked /></el-icon>
              <span class="info-label">断言结果</span>
            </div>
            <div class="info-value assertion-values">
              <span class="assertion-item passed">
                <el-icon><CircleCheck /></el-icon>
                {{ executionResult.assertionsPassed || 0 }} 通过
              </span>
              <span class="assertion-divider">|</span>
              <span class="assertion-item failed">
                <el-icon><CircleClose /></el-icon>
                {{ executionResult.assertionsFailed || 0 }} 失败
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 时间信息 -->
      <div class="result-time-section">
        <div class="time-item" v-if="executionResult.startTime">
          <div class="time-icon">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="time-content">
            <span class="time-label">开始时间</span>
            <span class="time-value">{{ formatTime(executionResult.startTime) }}</span>
          </div>
        </div>
        <div class="time-item" v-if="executionResult.endTime">
          <div class="time-icon">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="time-content">
            <span class="time-label">结束时间</span>
            <span class="time-value">{{ formatTime(executionResult.endTime) }}</span>
          </div>
        </div>
        <div class="time-item" v-if="executionResult.executor || executionResult.executorInfo">
          <div class="time-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="time-content">
            <span class="time-label">执行人</span>
            <span class="time-value">{{ executionResult.executor || executionResult.executorInfo?.name || '-' }}</span>
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
          <div class="failure-message" v-if="executionResult.errorMessage || executionResult.failureReason">
            <div class="detail-label">
              <el-icon><InfoFilled /></el-icon>
              错误信息
            </div>
            <pre class="error-message">{{ executionResult.errorMessage || executionResult.failureReason }}</pre>
          </div>

          <div class="failure-type" v-if="executionResult.failureType">
            <div class="detail-label">
              <el-icon><WarningFilled /></el-icon>
              失败类型
            </div>
            <el-tag type="danger">{{ getFailureTypeText(executionResult.failureType) }}</el-tag>
          </div>
        </div>
      </div>

      <!-- 响应详情（如果有） -->
      <div class="response-section" v-if="executionResult.responseBody || executionResult.responseHeaders">
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
          type="primary"
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
  Clock,
  User,
  WarningFilled,
  InfoFilled,
  Document,
  RefreshRight
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

// 获取显示状态
const displayStatus = computed(() => {
  if (!props.executionResult?.status) return 'unknown'
  const status = props.executionResult.status.toLowerCase()
  if (status === 'passed' || status === 'success') return 'passed'
  if (status === 'failed' || status === 'error') return 'failed'
  return 'unknown'
})

// 是否有断言信息
const hasAssertions = computed(() => {
  const result = props.executionResult
  return result && (result.assertionsPassed !== undefined || result.assertionsFailed !== undefined)
})

// 获取状态标签类型
function getStatusTagType(status: number | undefined): string {
  if (!status) return 'info'
  if (status >= 200 && status < 300) return 'success'
  if (status >= 400 && status < 500) return 'warning'
  if (status >= 500) return 'danger'
  return 'info'
}

// 格式化持续时间
function formatDuration(seconds: number | undefined): string {
  if (!seconds) return '0秒'

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
  padding: 20px 24px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f0f9ff, #e0f2fe);

  &.status-passed {
    background: linear-gradient(135deg, #f0fdf4, #dcfce7);
  }

  &.status-failed {
    background: linear-gradient(135deg, #fef2f2, #fee2e2);
  }
}

.banner-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.banner-content {
  flex: 1;
}

.result-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.result-subtitle {
  margin: 8px 0 0;
}

.banner-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;

  &.status-passed {
    background: #67c23a;
    color: white;
  }

  &.status-failed {
    background: #f56c6c;
    color: white;
  }

  &.status-unknown {
    background: #909399;
    color: white;
  }
}

.result-info-section {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 16px 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.info-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
}

.info-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: #606266;
  font-size: 13px;
}

.info-value {
  font-size: 14px;
  color: #303133;

  &.code {
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 13px;
    word-break: break-all;
  }

  &.highlight {
    font-weight: 600;
    color: #409eff;
  }
}

.duration-value {
  font-size: 18px;
  font-weight: 600;
}

.assertion-values {
  display: flex;
  align-items: center;
  gap: 8px;
}

.assertion-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;

  &.passed {
    color: #67c23a;
  }

  &.failed {
    color: #f56c6c;
  }
}

.assertion-divider {
  color: #dcdfe6;
}

.result-time-section {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.time-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #ecf5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
}

.time-content {
  display: flex;
  flex-direction: column;
}

.time-label {
  font-size: 12px;
  color: #909399;
}

.time-value {
  font-size: 14px;
  color: #303133;
}

.failure-section {
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 12px;
  padding: 16px 20px;
}

.failure-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
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
  color: #606266;
  margin-bottom: 8px;
}

.error-message {
  background: white;
  border: 1px solid #fecaca;
  border-radius: 8px;
  padding: 12px 16px;
  margin: 0;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  color: #991b1b;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
}

.response-section {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 16px 20px;
}

.response-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.response-code {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px 16px;
  max-height: 300px;
  overflow: auto;

  pre {
    margin: 0;
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 12px;
    line-height: 1.4;
    color: #303133;
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
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .result-time-section {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
