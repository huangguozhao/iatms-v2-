import { client } from '@/api/client'
import type { PageResult, TestSuiteSummaryVO, TestSuiteDetailVO, CreateTestSuiteDTO, TestSuiteQuery } from '@/types/api'

export type { TestSuiteSummaryVO, TestSuiteDetailVO, CreateTestSuiteDTO, TestSuiteQuery }

export const testSuiteApi = {
  query: (query: TestSuiteQuery) => {
    return client.get<PageResult<TestSuiteSummaryVO>, PageResult<TestSuiteSummaryVO>>('/v1/test-suites', { params: query })
  },

  getDetail: (id: number) => {
    return client.get<TestSuiteDetailVO, TestSuiteDetailVO>(`/v1/test-suites/${id}`)
  },

  create: (data: CreateTestSuiteDTO) => {
    return client.post<any, any>('/v1/test-suites', data)
  },

  update: (id: number, data: CreateTestSuiteDTO) => {
    return client.put<any, any>(`/v1/test-suites/${id}`, data)
  },

  delete: (id: number) => {
    return client.delete<any, any>(`/v1/test-suites/${id}`)
  },

  execute: (id: number) => {
    return client.post<any, any>(`/v1/test-suites/${id}/execute`)
  }
}
