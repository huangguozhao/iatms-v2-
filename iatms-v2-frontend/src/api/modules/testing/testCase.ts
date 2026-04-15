import { client } from '@/api/client'
import type { ApiResponse, PageResult } from '@/types/api'

export interface TestCaseSummaryVO {
  id: number
  name: string
  apiId: number
  apiName: string
  projectId: number
  projectName: string
  priority: string
  status: string
  createdAt: string
}

export interface TestCaseDetailVO extends TestCaseSummaryVO {
  headers: Record<string, string>
  requestBody: string
  assertions: string
  extractors: string
  description: string
}

export interface CreateTestCaseDTO {
  name: string
  apiId: number
  priority: string
  headers?: Record<string, string>
  requestBody?: string
  assertions?: string
  extractors?: string
  description?: string
}

export interface TestCaseQuery {
  keyword?: string
  priority?: string
  apiId?: number
  projectId?: number
  pageNum: number
  pageSize: number
}

export const testCaseApi = {
  query: (query: TestCaseQuery) => {
    return client.get<any, ApiResponse<PageResult<TestCaseSummaryVO>>>('/test-cases', { params: query })
  },

  getDetail: (id: number) => {
    return client.get<any, ApiResponse<TestCaseDetailVO>>(`/test-cases/${id}`)
  },

  create: (data: CreateTestCaseDTO) => {
    return client.post<any, ApiResponse<void>>('/test-cases', data)
  },

  update: (id: number, data: CreateTestCaseDTO) => {
    return client.put<any, ApiResponse<void>>(`/test-cases/${id}`, data)
  },

  delete: (id: number) => {
    return client.delete<any, ApiResponse<void>>(`/test-cases/${id}`)
  },

  execute: (id: number) => {
    return client.post<any, ApiResponse<void>>(`/test-cases/${id}/execute`)
  }
}
