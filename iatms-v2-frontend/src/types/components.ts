/**
 * 共享组件类型定义
 */

/**
 * 执行配置
 */
export interface ExecuteConfig {
  environment: string
  baseUrl: string
  timeout: number
  async: boolean
  concurrency: number
  executionOrder: string
  priorityFilter: string[]
  tagFilter: string[]
  enabledOnly: boolean
  variables: Record<string, any>
  targetId: number | null
  targetType: 'project' | 'module' | 'api' | 'case'
  // 对话框显示用字段
  targetName?: string
  caseCount?: number
  projectId?: number | null
}

/**
 * 执行结果
 */
export interface ExecutionResult {
  recordId?: string
  executionId?: string
  caseName?: string
  scopeName?: string
  status?: string
  responseStatus?: number
  duration?: number
  durationSeconds?: number
  assertionsPassed?: number
  assertionsFailed?: number
  startTime?: string
  endTime?: string
  executor?: string
  executorInfo?: { name?: string }
  errorMessage?: string
  failureReason?: string
  failureType?: string
  responseBody?: any
  responseHeaders?: any
}

/**
 * 执行历史记录
 */
export interface ExecutionHistory {
  status?: string
  executor?: string
  executorName?: string
  type?: string
  action?: string
  environment?: string
  note?: string
  remark?: string
  executedAt?: string
  executed_time?: string
  createTime?: string
  durationSeconds?: number
  duration?: number
}
