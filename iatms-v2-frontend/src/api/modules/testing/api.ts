import { client } from '@/api/client'
import type { ApiSummaryVO, ApiDetailVO, CreateApiDTO, ApiQuery, PageResult } from '@/types/api'

export type { ApiSummaryVO, ApiDetailVO, CreateApiDTO, ApiQuery, PageResult }

export const apiApi = {
  query: (query: ApiQuery): Promise<PageResult<ApiSummaryVO>> => {
    return client.get('/v1/apis', {
      params: {
        keyword: query.keyword,
        httpMethod: query.method,
        projectId: query.projectId,
        pageNum: query.pageNum,
        pageSize: query.pageSize
      }
    })
  },

  getDetail: (id: number): Promise<ApiDetailVO> => {
    return client.get(`/v1/apis/${id}`)
  },

  getLatestExecution: (apiId: number): Promise<{
    hasResult: boolean
    message?: string
    caseId?: number
    caseName?: string
    status?: string
    duration?: number
    startTime?: string
    endTime?: string
    failureMessage?: string
    failureType?: string
    environment?: string
    retryCount?: number
    statusCode?: number
    body?: any
    headers?: any[]
    assertions?: any[]
    requestBody?: any
  }> => {
    return client.get(`/v1/apis/${apiId}/latest-execution`)
  },

  getTestHistory: (apiId: number, period = '7days'): Promise<Array<{
    recordId: number
    executor: string
    executorInfo: { name: string }
    executionType: string
    environment: string
    startTime: string
    endTime: string
    totalCases: number
    passedCases: number
    failedCases: number
    successRate: number
    testTime: string
    status: string
    caseName: string
    duration: number
  }>> => {
    return client.get(`/v1/apis/${apiId}/test-history`, { params: { period } })
  },

  create: (data: CreateApiDTO): Promise<void> => {
    return client.post('/v1/apis', data)
  },

  update: (id: number, data: CreateApiDTO): Promise<void> => {
    return client.put(`/v1/apis/${id}`, data)
  },

  delete: (id: number): Promise<void> => {
    return client.delete(`/v1/apis/${id}`)
  }
}
