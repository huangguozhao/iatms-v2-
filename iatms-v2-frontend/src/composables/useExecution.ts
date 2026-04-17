/**
 * 测试执行 Composable
 * 处理执行配置、执行、轮询、结果展示的完整流程
 */
import { ref, shallowRef } from 'vue'
import { ElMessage } from 'element-plus'
import { testCaseApi } from '@/api/modules/testing/testCase'
import { executionApi } from '@/api/modules/testing/execution'
import type { ExecuteConfig, ExecutionResult } from '@/types/components'

// 轮询间隔 (ms)
const POLLING_INTERVAL = 2000
// 最大轮询次数
const MAX_POLLING_ATTEMPTS = 60

export interface UseExecutionOptions {
  onSuccess?: (result: ExecutionResult) => void
  onError?: (error: any) => void
  onComplete?: () => void
}

export function useExecution(options: UseExecutionOptions = {}) {
  // 状态
  const executing = ref(false)
  const executionResult = shallowRef<ExecutionResult | null>(null)
  const executionId = ref<string | null>(null)
  const currentStatus = ref<string>('')
  const progress = ref(0)

  // 对话框状态
  const configDialogVisible = ref(false)
  const resultDialogVisible = ref(false)

  // 执行配置
  const executeConfig = ref<ExecuteConfig>({
    targetType: 'case',
    targetId: null,
    targetName: '',
    caseCount: 0,
    projectId: null,
    environment: 'test',
    timeout: 30,
    async: false
  })

  // 当前正在执行的用例信息
  const currentCaseInfo = ref<{
    id: number
    name: string
  } | null>(null)

  // 是否正在轮询
  const isPolling = ref(false)
  let pollingTimer: ReturnType<typeof setTimeout> | null = null
  let pollingAttempts = 0

  /**
   * 打开执行配置对话框
   */
  function openConfigDialog(caseInfo: { id: number; name: string }) {
    currentCaseInfo.value = caseInfo
    executeConfig.value = {
      targetType: 'case',
      targetId: caseInfo.id,
      targetName: caseInfo.name,
      caseCount: 1,
      projectId: null,
      environment: 'test',
      timeout: 30,
      async: false
    }
    configDialogVisible.value = true
  }

  /**
   * 执行测试
   */
  async function execute(config: ExecuteConfig) {
    if (!config.targetId) {
      ElMessage.error('请选择要执行的用例')
      return
    }

    executing.value = true
    configDialogVisible.value = false

    try {
      if (config.async === false) {
        // 同步执行：等待后端执行完成，直接返回结果
        ElMessage.info('执行中，请稍候...')
        const result = await testCaseApi.execute(config.targetId, false) as any
        executionResult.value = result
        executionId.value = result.executionId

        // 计算 passedCases 如果没返回
        if (result.assertionsPassed === undefined && result.status === 'passed') {
          result.assertionsPassed = 1
          result.assertionsFailed = 0
        }

        resultDialogVisible.value = true

        if (result.status === 'passed' || result.status === 'completed') {
          ElMessage.success('执行成功')
          options.onSuccess?.(result)
        } else {
          ElMessage.error(result.errorMessage || '执行失败')
          options.onError?.(result)
        }
      } else {
        // 异步执行：后端立即返回 executionId，需要轮询
        const execId = await testCaseApi.execute(config.targetId, true) as string
        executionId.value = execId

        // 初始化结果对象
        executionResult.value = {
          recordId: execId,
          caseName: config.targetName,
          status: 'running',
          responseStatus: 0,
          duration: 0,
          startTime: new Date().toISOString(),
          endTime: '',
          executor: '当前用户',
          assertionsPassed: 0,
          assertionsFailed: 0
        }

        // 显示结果对话框，开始轮询
        resultDialogVisible.value = true
        ElMessage.success('执行已开始')
        startPolling(execId)
      }
    } catch (error: any) {
      ElMessage.error(error.message || '执行失败')
      resultDialogVisible.value = false
      options.onError?.(error)
    } finally {
      executing.value = false
    }
  }

  /**
   * 开始轮询执行状态
   */
  function startPolling(execId: string) {
    if (isPolling.value) return

    isPolling.value = true
    pollingAttempts = 0
    currentStatus.value = 'running'
    progress.value = 0

    pollExecutionStatus(execId)
  }

  /**
   * 轮询执行状态
   */
  async function pollExecutionStatus(execId: string) {
    if (pollingAttempts >= MAX_POLLING_ATTEMPTS) {
      ElMessage.warning('执行超时，请稍后查看执行结果')
      stopPolling()
      executionResult.value = {
        ...executionResult.value!,
        status: 'timeout'
      }
      resultDialogVisible.value = true
      options.onComplete?.()
      return
    }

    try {
      // 调用状态查询接口
      const status = await getExecutionStatus(execId)

      if (status) {
        currentStatus.value = status.status || 'running'
        progress.value = status.progress || 0

        // 更新结果
        if (executionResult.value) {
          executionResult.value = {
            ...executionResult.value,
            status: mapStatus(status.status),
            duration: status.duration || 0,
            assertionsPassed: status.passedCases || 0,
            assertionsFailed: status.failedCases || 0
          }
        }

        // 检查是否完成
        if (isTerminalStatus(status.status)) {
          stopPolling()

          // 填充完整结果
          executionResult.value = {
            recordId: execId,
            caseName: currentCaseInfo.value?.name || '未知用例',
            status: mapStatus(status.status),
            responseStatus: status.status === 'completed' ? 200 : 500,
            duration: status.duration || 0,
            startTime: status.startedAt || new Date().toISOString(),
            endTime: new Date().toISOString(),
            executor: '当前用户',
            assertionsPassed: status.passedCases || 0,
            assertionsFailed: status.failedCases || 0
          }

          resultDialogVisible.value = true

          if (status.status === 'completed' || status.status === 'passed') {
            options.onSuccess?.(executionResult.value)
          } else {
            options.onError?.({ message: '执行失败' })
          }

          options.onComplete?.()
          return
        }
      }

      pollingAttempts++
      pollingTimer = setTimeout(() => pollExecutionStatus(execId), POLLING_INTERVAL)
    } catch (error: any) {
      console.error('轮询执行状态失败:', error)
      pollingAttempts++

      // 如果是最后几次尝试，继续轮询
      if (pollingAttempts < MAX_POLLING_ATTEMPTS) {
        pollingTimer = setTimeout(() => pollExecutionStatus(execId), POLLING_INTERVAL)
      } else {
        stopPolling()
        ElMessage.error('获取执行状态失败')
        options.onComplete?.()
      }
    }
  }

  /**
   * 停止轮询
   */
  function stopPolling() {
    isPolling.value = false
    if (pollingTimer) {
      clearTimeout(pollingTimer)
      pollingTimer = null
    }
  }

  /**
   * 获取执行状态
   */
  async function getExecutionStatus(execId: string): Promise<any> {
    try {
      // 调用后端接口 GET /v1/executions/{executionId}/status
      const response = await executionApi.getStatus(execId)
      return response
    } catch (error: any) {
      console.error('获取执行状态失败:', error)
      throw error
    }
  }

  /**
   * 映射后端状态到前端状态
   */
  function mapStatus(status: string | undefined): 'passed' | 'failed' | 'running' | 'timeout' {
    if (!status) return 'running'
    const s = status.toLowerCase()
    if (s === 'completed' || s === 'passed' || s === 'success') return 'passed'
    if (s === 'failed' || s === 'failure' || s === 'error') return 'failed'
    if (s === 'timeout') return 'timeout'
    return 'running'
  }

  /**
   * 判断是否为终态
   */
  function isTerminalStatus(status: string | undefined): boolean {
    if (!status) return false
    const s = status.toLowerCase()
    return ['completed', 'passed', 'failed', 'success', 'failure', 'error', 'cancelled', 'timeout'].includes(s)
  }

  /**
   * 关闭结果对话框
   */
  function closeResultDialog() {
    resultDialogVisible.value = false
    executionResult.value = null
  }

  /**
   * 重新执行
   */
  function retry() {
    resultDialogVisible.value = false
    if (currentCaseInfo.value) {
      openConfigDialog(currentCaseInfo.value)
    }
  }

  // 组件销毁时清理
  function cleanup() {
    stopPolling()
  }

  return {
    // 状态
    executing,
    executionResult,
    executionId,
    currentStatus,
    progress,
    isPolling,

    // 对话框
    configDialogVisible,
    resultDialogVisible,
    executeConfig,

    // 方法
    openConfigDialog,
    execute,
    startPolling,
    stopPolling,
    closeResultDialog,
    retry,
    cleanup
  }
}

/**
 * 格式化执行时长
 */
export function formatDuration(seconds: number | undefined): string {
  if (!seconds) return '-'

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

/**
 * 格式化时间
 */
export function formatTime(time: string | undefined): string {
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
