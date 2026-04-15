import { client } from '@/api/client'
import type { ApiResponse, PageResult } from '@/types/api'

export interface ExecutionSummaryVO {
  id: number
  type: string
  targetId: number
  targetName: string
  status: string
  statusText: string
  total: number
  passed: number
  failed: number
  skipped: number
  duration: number
  executor: string
  createdAt: string
}

export interface ExecutionDetailVO extends ExecutionSummaryVO {
  results: TestResultVO[]
}

export interface TestResultVO {
  id: number
  name: string
  status: string
  duration: number
  errorMessage: string
  requestData: string
  responseData: string
  assertions: AssertionResult[]
}

export interface AssertionResult {
  expression: string
  expected: string
  actual: string
  passed: boolean
}

export interface ExecutionQuery {
  type?: string
  status?: string
  targetId?: number
  startTime?: string
  endTime?: string
  pageNum: number
  pageSize: number
}

export interface ExecutionProgressVO {
  executionId: number
  status: string
  total: number
  passed: number
  failed: number
  current: number
  currentName: string
}

export const executionApi = {
  query: (query: ExecutionQuery) => {
    return client.get<any, ApiResponse<PageResult<ExecutionSummaryVO>>>('/executions', { params: query })
  },

  getDetail: (id: number) => {
    return client.get<any, ApiResponse<ExecutionDetailVO>>(`/executions/${id}`)
  },

  getProgress: (id: number) => {
    return client.get<any, ApiResponse<ExecutionProgressVO>>(`/executions/${id}/progress`)
  },

  start: (type: string, targetId: number) => {
    return client.post<any, ApiResponse<{ executionId: number }>>('/executions/start', { type, targetId })
  },

  cancel: (id: number) => {
    return client.post<any, ApiResponse<void>>(`/executions/${id}/cancel`)
  }
}
