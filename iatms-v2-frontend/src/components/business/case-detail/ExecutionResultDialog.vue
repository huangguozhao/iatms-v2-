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

      <!-- AI智能诊断入口 - 测试失败时显示 -->
      <div class="ai-diagnosis-entrance" v-if="displayStatus === 'failed' && !showAIDiagnosis">
        <div class="ai-entrance-icon">
          <el-icon :size="32"><MagicStick /></el-icon>
        </div>
        <div class="ai-entrance-content">
          <div class="ai-entrance-title">AI 智能诊断</div>
          <div class="ai-entrance-desc">基于失败信息智能分析，提供修复建议</div>
        </div>
        <el-button
          type="primary"
          class="ai-entrance-btn"
          @click="triggerAIDiagnosis"
          :loading="aiDiagnosisLoading"
          :disabled="isDiagnosing"
        >
          开始诊断
        </el-button>
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

          <!-- AI诊断结果 -->
          <div class="ai-diagnosis-result" v-if="showAIDiagnosis">
            <!-- 加载中 -->
            <div v-if="aiDiagnosisLoading" class="ai-diagnosis-loading">
              <div class="ai-loading-animation">
                <div class="ai-loading-orb"></div>
                <div class="ai-loading-orb"></div>
                <div class="ai-loading-orb"></div>
              </div>
              <span>AI正在诊断中...</span>
            </div>

            <!-- 诊断结果 -->
            <template v-else-if="aiDiagnosisResult">
              <div class="ai-result-header">
                <div class="ai-result-icon">
                  <el-icon :size="24"><MagicStick /></el-icon>
                </div>
                <div class="ai-result-title">AI 诊断结果</div>
              </div>

              <!-- 严重程度 -->
              <div class="diagnosis-severity" v-if="aiDiagnosisResult.severity">
                <el-tag
                  :type="aiDiagnosisResult.severity === 'high' ? 'danger' : aiDiagnosisResult.severity === 'medium' ? 'warning' : 'success'"
                  size="large"
                  effect="dark"
                  round
                >
                  <el-icon :size="16"><WarningFilled /></el-icon>
                  严重程度: {{ aiDiagnosisResult.severity === 'high' ? '高' : aiDiagnosisResult.severity === 'medium' ? '中' : '低' }}
                </el-tag>
              </div>

              <!-- 完整分析报告（主要展示区域） -->
              <div class="diagnosis-analysis-section" v-if="aiDiagnosisResult.analysis">
                <div class="diagnosis-card analysis-card">
                  <div class="diagnosis-card-header">
                    <el-icon :size="18"><Document /></el-icon>
                    <span>AI 诊断分析报告</span>
                  </div>
                  <div class="diagnosis-card-content">
                    <pre class="analysis-pre">{{ aiDiagnosisResult.analysis }}</pre>
                  </div>
                </div>
              </div>

              <!-- 发现的问题（合并成连贯文本） -->
              <div class="diagnosis-issues-section" v-if="aiDiagnosisResult.issues && aiDiagnosisResult.issues.length > 0">
                <div class="diagnosis-card issues-card">
                  <div class="diagnosis-card-header">
                    <el-icon :size="18"><QuestionFilled /></el-icon>
                    <span>发现问题</span>
                  </div>
                  <div class="diagnosis-card-content">
                    <pre class="issues-pre">{{ aiDiagnosisResult.issues.map(i => i.title).filter(t => t).join('\n') }}</pre>
                  </div>
                </div>
              </div>

              <!-- 修复建议（合并成连贯文本） -->
              <div class="diagnosis-suggestions-section" v-if="aiDiagnosisResult.suggestions && aiDiagnosisResult.suggestions.length > 0">
                <div class="diagnosis-card suggestions-card">
                  <div class="diagnosis-card-header">
                    <el-icon :size="18"><Operation /></el-icon>
                    <span>修复建议</span>
                  </div>
                  <div class="diagnosis-card-content">
                    <pre class="suggestions-pre">{{ aiDiagnosisResult.suggestions.map(s => s.title + (s.content ? ': ' + s.content : '')).filter(t => t).join('\n') }}</pre>
                  </div>
                </div>
              </div>
            </template>

            <!-- 诊断失败 -->
            <div v-else class="ai-diagnosis-error">
              <el-icon :size="40"><CircleCloseFilled /></el-icon>
              <div class="ai-error-text">AI诊断失败，请稍后重试</div>
              <el-button type="primary" plain @click="triggerAIDiagnosis" :disabled="isDiagnosing">重新诊断</el-button>
            </div>
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
import { ref, computed, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
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
  Loading,
  MagicStick,
  ChatDotRound,
  QuestionFilled,
  Operation
} from '@element-plus/icons-vue'
import type { ExecutionResult } from '@/types/components'
import { aiApi, type DiagnosisResult } from '@/api/modules/ai/ai'

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

// ==================== AI 诊断相关 ====================
const showAIDiagnosis = ref(false)
const aiDiagnosisLoading = ref(false)
const aiDiagnosisResult = ref<DiagnosisResult | null>(null)
const aiDiagnosisStreamContent = ref('')  // 流式内容
let eventSource: EventSource | null = null
const isDiagnosing = ref(false)  // 防止重复请求
let currentDiagnosisId: string | null = null  // 当前诊断ID，用于防止处理过期响应

// 触发 AI 诊断 (使用 SSE)
const triggerAIDiagnosis = () => {
  if (!props.executionResult) return

  // 防止重复请求
  if (isDiagnosing.value) {
    console.log('AI诊断正在进行中，忽略重复请求, isDiagnosing=', isDiagnosing.value)
    return
  }

  const executionId = props.executionResult.executionId || props.executionResult.recordId
  if (!executionId) {
    ElMessage.error('无法获取执行ID')
    return
  }

  // 生成唯一诊断ID，用于追踪
  const diagnosisId = `diag_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  currentDiagnosisId = diagnosisId

  showAIDiagnosis.value = true
  aiDiagnosisLoading.value = true
  aiDiagnosisResult.value = null
  aiDiagnosisStreamContent.value = ''
  isDiagnosing.value = true

  console.log('AI诊断开始: executionId=', executionId, ', diagnosisId=', diagnosisId)

  // 清理之前的连接
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }

  // 创建 SSE 连接
  eventSource = aiApi.diagnoseFailureSSE(executionId)

  eventSource.onopen = () => {
    console.log('SSE连接已打开')
  }

  // 接收上下文信息
  eventSource.addEventListener('context', (event) => {
    console.log('收到上下文:', event.data)
    try {
      const context = JSON.parse(event.data)
      // 可以用context更新UI
    } catch (e) {
      console.error('解析上下文失败:', e)
    }
  })

  // 接收流式内容
  eventSource.addEventListener('chunk', (event) => {
    // 检查是否是最新的诊断请求
    if (currentDiagnosisId !== diagnosisId) {
      console.log('收到旧请求的chunk，忽略: current=', currentDiagnosisId, ', this=', diagnosisId)
      return
    }
    console.log('收到chunk:', event.data, ', diagnosisId=', diagnosisId)
    aiDiagnosisStreamContent.value += event.data
  })

  // 接收错误 - 区分后端错误和连接断开
  eventSource.addEventListener('error', (event) => {
    console.log('SSE error事件, readyState=', eventSource.readyState, ', diagnosisId=', diagnosisId, ', currentDiagnosisId=', currentDiagnosisId)

    // 如果currentDiagnosisId已被清除，说明诊断已完成，忽略所有错误
    if (currentDiagnosisId === null) {
      console.log('currentDiagnosisId为null，忽略错误')
      return
    }

    // 检查是否是最新的诊断请求
    if (currentDiagnosisId !== diagnosisId) {
      console.log('收到旧请求的错误响应，忽略: current=', currentDiagnosisId, ', this=', diagnosisId)
      return
    }

    // EventSource 连接断开时会触发 error 事件
    // readyState: 0=CONNECTING, 1=OPEN, 2=CLOSED
    if (eventSource.readyState === EventSource.CLOSED) {
      // 连接正常关闭，不需要显示错误
      console.log('SSE连接已正常关闭, diagnosisId=', diagnosisId)
      return
    }

    console.error('SSE错误:', event, ', diagnosisId=', diagnosisId)
    aiDiagnosisLoading.value = false
    isDiagnosing.value = false
    ElMessage.error('AI诊断失败，请稍后重试')
  })

  // 接收完成
  eventSource.addEventListener('done', (event) => {
    // 检查是否是最新的诊断请求
    if (currentDiagnosisId !== diagnosisId) {
      console.log('收到旧请求的完成响应，忽略: current=', currentDiagnosisId, ', this=', diagnosisId)
      return
    }

    console.log('AI诊断完成:', event.data, ', diagnosisId=', diagnosisId)
    aiDiagnosisLoading.value = false
    isDiagnosing.value = false
    currentDiagnosisId = null  // 清除诊断ID

    // 立即关闭EventSource防止重连
    if (eventSource) {
      console.log('done事件后立即关闭EventSource')
      eventSource.close()
      eventSource = null
    }

    try {
      const result = JSON.parse(event.data)
      aiDiagnosisResult.value = result
      if (result && result.analysis) {
        ElMessage.success('AI诊断完成')
      }
    } catch (e) {
      console.error('解析诊断结果失败:', e)
      ElMessage.error('解析诊断结果失败')
    }
  })

  // 超时处理
  setTimeout(() => {
    // 检查是否是最新的诊断请求
    if (currentDiagnosisId !== diagnosisId) {
      console.log('超时回调，但是已经是旧请求: current=', currentDiagnosisId, ', this=', diagnosisId)
      return
    }
    if (aiDiagnosisLoading.value) {
      console.warn('AI诊断超时, diagnosisId=', diagnosisId)
      if (eventSource) {
        eventSource.close()
        eventSource = null
      }
      aiDiagnosisLoading.value = false
      isDiagnosing.value = false
      ElMessage.warning('AI诊断超时')
    }
  }, 5 * 60 * 1000) // 5分钟超时
}

// 清理 SSE 连接
const cleanupSSE = () => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
}

// 组件卸载时清理
onUnmounted(() => {
  cleanupSSE()
})
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

// AI诊断入口卡片样式
.ai-diagnosis-entrance {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  border: 1px solid #93c5fd;
  border-radius: 12px;
  transition: all 0.2s ease;

  &:hover {
    border-color: #3b82f6;
    background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
  }
}

.ai-entrance-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.ai-entrance-content {
  flex: 1;
}

.ai-entrance-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e40af;
  margin-bottom: 2px;
}

.ai-entrance-desc {
  font-size: 13px;
  color: #6b7280;
}

.ai-entrance-btn {
  padding: 8px 20px;
  background: #3b82f6;
  border: none;
  border-radius: 8px;
  color: white;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.2s ease;

  &:hover {
    background: #2563eb;
  }
}

// AI诊断结果区域
.ai-diagnosis-result {
  margin-top: 16px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
}

.ai-diagnosis-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 40px;
  background: #f8fafc;

  span {
    font-size: 14px;
    font-weight: 500;
    color: #374151;
  }
}

.ai-loading-animation {
  display: flex;
  gap: 6px;
}

.ai-loading-orb {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #3b82f6;
  animation: bounce 1.4s ease-in-out infinite;

  &:nth-child(1) { animation-delay: 0s; }
  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0.7);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.ai-result-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #3b82f6;
  color: white;
}

.ai-result-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-result-title {
  font-size: 15px;
  font-weight: 600;
}

.diagnosis-severity {
  padding: 12px 16px;
  background: white;
  border-bottom: 1px solid #f3f4f6;
}

.diagnosis-card {
  background: white;
  border-bottom: 1px solid #f3f4f6;

  &:last-child {
    border-bottom: none;
  }
}

.diagnosis-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  background: #f9fafb;
  border-bottom: 1px solid #f3f4f6;
}

.diagnosis-card-content {
  padding: 12px 16px;
  font-size: 13px;
  line-height: 1.6;
  color: #374151;
}

.root-cause-card {
  .diagnosis-card-header {
    color: #dc2626;
    background: #fef2f2;
    border-bottom-color: #fecaca;
  }

  .diagnosis-card-content {
    background: #fef2f2;
    color: #991b1b;
  }
}

.issues-list {
  padding: 10px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.issue-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;

  &.issue-high {
    border-left: 3px solid #ef4444;
  }

  &.issue-medium {
    border-left: 3px solid #f59e0b;
  }

  .el-tag {
    flex-shrink: 0;
    margin-top: 2px;
  }

  .issue-content {
    flex: 1;
    min-width: 0;
  }

  .issue-title {
    font-weight: 600;
    color: #1f2937;
    font-size: 13px;
    margin-bottom: 2px;
  }

  .issue-desc {
    font-size: 12px;
    color: #6b7280;
    line-height: 1.4;
  }
}

.suggestions-timeline {
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
}

.suggestion-item {
  display: flex;
  gap: 12px;

  .suggestion-timeline-marker {
    display: flex;
    flex-direction: column;
    align-items: center;
    flex-shrink: 0;
  }

  .suggestion-timeline-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #3b82f6;
  }

  .suggestion-timeline-line {
    width: 2px;
    flex: 1;
    min-height: 16px;
    background: #e5e7eb;
    margin: 3px 0;
  }

  .suggestion-content {
    flex: 1;
    padding-bottom: 16px;
  }

  .suggestion-title {
    font-weight: 600;
    color: #1f2937;
    font-size: 13px;
    margin-bottom: 2px;
  }

  .suggestion-desc {
    font-size: 12px;
    color: #6b7280;
    line-height: 1.5;
  }

  &:last-child .suggestion-content {
    padding-bottom: 0;
  }
}

// 摘要卡片
.summary-card {
  .diagnosis-card-header {
    color: #2563eb;
    background: #eff6ff;
    border-bottom-color: #bfdbfe;
  }

  .diagnosis-card-content {
    background: #eff6ff;
    color: #1e40af;
    font-size: 14px;
    line-height: 1.7;
  }
}

// 关键发现列表
.key-findings-list {
  padding: 10px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.key-finding-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 8px 12px;
  background: #fff;
  border-radius: 6px;
  border: 1px solid #e5e7eb;

  .finding-tag {
    flex-shrink: 0;
  }

  .finding-text {
    font-size: 13px;
    color: #374151;
    line-height: 1.5;
  }
}

// 修复建议列表（新样式）
.suggestions-list {
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;

  .suggestion-item {
    display: flex;
    gap: 12px;
    align-items: flex-start;

    .suggestion-number {
      width: 22px;
      height: 22px;
      border-radius: 50%;
      background: linear-gradient(135deg, #3b82f6, #2563eb);
      color: #fff;
      font-size: 12px;
      font-weight: 600;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .suggestion-content {
      flex: 1;
    }

    .suggestion-title {
      font-weight: 600;
      color: #1f2937;
      font-size: 13px;
      margin-bottom: 2px;
    }

    .suggestion-desc {
      font-size: 12px;
      color: #6b7280;
      line-height: 1.5;
    }
  }
}

// 完整分析报告区域
.diagnosis-analysis-section {
  margin-top: 12px;
}

.analysis-card {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e5e7eb;

  .diagnosis-card-header {
    color: #2563eb;
    background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
    border-bottom: 1px solid #bfdbfe;
    font-size: 14px;
    font-weight: 600;
  }

  .diagnosis-card-content {
    background: #fff;
    padding: 0;
  }
}

.analysis-pre {
  padding: 16px 20px;
  margin: 0;
  font-size: 14px;
  line-height: 1.9;
  color: #374151;
  white-space: pre-wrap;
  word-break: break-word;
  background: #fff;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

// 发现问题区域
.diagnosis-issues-section {
  margin-top: 12px;
}

.issues-card {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e5e7eb;

  .diagnosis-card-header {
    color: #dc2626;
    background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
    border-bottom: 1px solid #fecaca;
    font-size: 14px;
    font-weight: 600;
  }

  .diagnosis-card-content {
    background: #fff;
    padding: 0;
  }
}

.issues-pre {
  padding: 14px 16px;
  margin: 0;
  font-size: 13px;
  line-height: 1.8;
  color: #374151;
  white-space: pre-wrap;
  word-break: break-word;
  background: #fff;
  font-family: inherit;
}

// 修复建议区域
.diagnosis-suggestions-section {
  margin-top: 12px;
}

.suggestions-card {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e5e7eb;

  .diagnosis-card-header {
    color: #059669;
    background: linear-gradient(135deg, #ecfdf5 0%, #d1fae5 100%);
    border-bottom: 1px solid #a7f3d0;
    font-size: 14px;
    font-weight: 600;
  }

  .diagnosis-card-content {
    background: #fff;
    padding: 0;
  }
}

.suggestions-pre {
  padding: 14px 16px;
  margin: 0;
  font-size: 13px;
  line-height: 1.8;
  color: #374151;
  white-space: pre-wrap;
  word-break: break-word;
  background: #fff;
  font-family: inherit;
}

.ai-diagnosis-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 32px;
  background: #fef2f2;

  .el-icon {
    font-size: 36px;
    color: #ef4444;
  }

  .ai-error-text {
    font-size: 14px;
    font-weight: 500;
    color: #991b1b;
  }
}
</style>
