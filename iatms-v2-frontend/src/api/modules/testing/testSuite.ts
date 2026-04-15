import { client } from '@/api/client'
import type { ApiResponse, PageResult } from '@/types/api'

export interface TestSuiteSummaryVO {
  id: number
  name: string
  projectId: number
  projectName: string
  caseCount: number
  executionStrategy: string
  failStrategy: string
  status: string
  createdAt: string
}

export interface TestSuiteDetailVO extends TestSuiteSummaryVO {
  caseIds: number[]
  description: string
}

export interface CreateTestSuiteDTO {
  name: string
  projectId: number
  caseIds: number[]
  executionStrategy: string
  failStrategy: string
  description?: string
}

export interface TestSuiteQuery {
  keyword?: string
  projectId?: number
  pageNum: number
  pageSize: number
}

export const testSuiteApi = {
  query: (query: TestSuiteQuery) => {
    return client.get<any, ApiResponse<PageResult<TestSuiteSummaryVO>>>('/test-suites', { params: query })
  },

  getDetail: (id: number) => {
    return client.get<any, ApiResponse<TestSuiteDetailVO>>(`/test-suites/${id}`)
  },

  create: (data: CreateTestSuiteDTO) => {
    return client.post<any, ApiResponse<void>>('/test-suites', data)
  },

  update: (id: number, data: CreateTestSuiteDTO) => {
    return client.put<any, ApiResponse<void>>(`/test-suites/${id}`, data)
  },

  delete: (id: number) => {
    return client.delete<any, ApiResponse<void>>(`/test-suites/${id}`)
  },

  execute: (id: number) => {
    return client.post<any, ApiResponse<void>>(`/test-suites/${id}/execute`)
  }
}
