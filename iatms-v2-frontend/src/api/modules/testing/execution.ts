import { client } from '@/api/client'
import type { ApiResponse, PageResult, ExecutionSummaryVO, ExecutionDetailVO, TestResultVO, AssertionResult, ExecutionProgressVO } from '@/types/api'

export type { ExecutionSummaryVO, ExecutionDetailVO, TestResultVO, AssertionResult, ExecutionProgressVO }

export interface ExecutionQuery {
  type?: string
  status?: string
  targetId?: number
  startTime?: string
  endTime?: string
  pageNum: number
  pageSize: number
}

export const executionApi = {
  query: (query: ExecutionQuery) => {
    return client.get<any, ApiResponse<PageResult<ExecutionSummaryVO>>>('/v1/executions', { params: query })
  },

  getDetail: (id: number) => {
    return client.get<any, ApiResponse<ExecutionDetailVO>>(`/v1/executions/${id}`)
  },

  getProgress: (id: number) => {
    return client.get<any, ApiResponse<ExecutionProgressVO>>(`/v1/executions/${id}/progress`)
  },

  start: (type: string, targetId: number) => {
    return client.post<any, ApiResponse<{ executionId: number }>>('/v1/executions/start', { type, targetId })
  },

  cancel: (id: number) => {
    return client.post<any, ApiResponse<void>>(`/v1/executions/${id}/cancel`)
  }
}
