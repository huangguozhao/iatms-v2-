import { client } from '@/api/client'
import type { ApiResponse, PageResult, ReportSummaryVO, ReportDetailVO, ReportResult, AssertionResult } from '@/types/api'

export type { ReportSummaryVO, ReportDetailVO, ReportResult, AssertionResult }

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
