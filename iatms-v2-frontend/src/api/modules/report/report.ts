import { client } from '@/api/client'
import type { ApiResponse, PageResult } from '@/types/api'

export interface ReportSummaryVO {
  id: number
  executionId: number
  projectId: number
  projectName: string
  type: string
  status: string
  total: number
  passed: number
  failed: number
  skipped: number
  passRate: number
  duration: number
  createdAt: string
}

export interface ReportDetailVO extends ReportSummaryVO {
  results: ReportResult[]
  summary: string
}

export interface ReportResult {
  id: number
  name: string
  method: string
  path: string
  status: string
  duration: number
  errorMessage: string
  requestData: string
  responseData: string
  assertions: AssertionResult[]
  logs: string[]
}

export interface AssertionResult {
  expression: string
  expected: string
  actual: string
  passed: boolean
}

export interface ReportQuery {
  startDate?: string
  endDate?: string
  projectId?: number
  status?: string
  pageNum: number
  pageSize: number
}

export const reportApi = {
  query: (query: ReportQuery) => {
    return client.get<any, ApiResponse<PageResult<ReportSummaryVO>>>('/v1/reports', { params: query })
  },

  getDetail: (id: number) => {
    return client.get<any, ApiResponse<ReportDetailVO>>(`/v1/reports/${id}`)
  },

  download: (id: number, format: 'HTML' | 'PDF' | 'JSON') => {
    return client.get<any, ApiResponse<Blob>>(`/v1/reports/${id}/download`, {
      params: { format },
      responseType: 'blob'
    })
  }
}
