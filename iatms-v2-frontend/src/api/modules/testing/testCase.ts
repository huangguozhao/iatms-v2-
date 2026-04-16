import { client } from '@/api/client'
import type { ApiResponse, PageResult } from '@/types/api'

// Re-export common types for convenience
export type { PageResult }

/**
 * 测试用例概要VO
 */
export interface TestCaseSummaryVO {
  id: number
  caseCode: string
  name: string
  priority: string
  status: string
  projectId: number
  moduleId: number
  apiId: number | null
  createdAt: string
  createdBy: string
}

/**
 * 测试用例详情VO
 */
export interface TestCaseDetailVO extends TestCaseSummaryVO {
  description: string
  testType: string
  preconditions: string
  testSteps: string
  testData: string
  headers: Record<string, string> | string
  requestParams: string
  requestBody: string
  assertions: string
  expectedResponse: string
  extractors: string
  updatedAt: string
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

/**
 * 项目树节点
 */
export interface ProjectTreeNode {
  id: number
  name: string
  type: 'project' | 'module' | 'api' | 'testcase'
  projectId?: number
  moduleId?: number
  apiId?: number
  parentId?: number
  code?: string
  description?: string
  status?: string
  httpMethod?: string
  path?: string
  priority?: string
  children?: ProjectTreeNode[]
  apis?: ProjectTreeNode[]
  testCases?: ProjectTreeNode[]
  stats?: {
    moduleCount?: number
    apiCount?: number
    testCaseCount?: number
  }
}

export const testCaseApi = {
  query: (query: TestCaseQuery) => {
    return client.get<any, PageResult<TestCaseSummaryVO>>('/v1/test-cases', { params: query })
  },

  getDetail: (id: number) => {
    return client.get<any, TestCaseDetailVO>(`/v1/test-cases/${id}`)
  },

  create: (data: CreateTestCaseDTO) => {
    return client.post<any, void>('/v1/test-cases', data)
  },

  update: (id: number, data: CreateTestCaseDTO) => {
    return client.put<any, void>(`/v1/test-cases/${id}`, data)
  },

  delete: (id: number) => {
    return client.delete<any, void>(`/v1/test-cases/${id}`)
  },

  execute: (id: number) => {
    return client.post<any, void>(`/v1/test-cases/${id}/execute`)
  },

  /**
   * 获取项目树形结构
   */
  getTree: (projectId?: number) => {
    const params = projectId ? { projectId } : {}
    return client.get<any, ProjectTreeNode[]>('/v1/test-cases/tree', { params })
  }
}
