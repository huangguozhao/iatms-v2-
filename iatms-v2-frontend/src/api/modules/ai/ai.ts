import request from '@/api/client'
import type { ApiResponse } from '@/types/api'

export interface DiagnosisResult {
  severity: 'high' | 'medium' | 'low'
  rootCause: string
  issues: Array<{
    title: string
    severity: string
    description: string
  }>
  suggestions: Array<{
    title: string
    content: string
    priority: string
  }>
  analysis: string
}

export const aiApi = {
  /**
   * 生成测试用例
   */
  generateTestCases(apiDescription: string, testType: string = 'FUNCTIONAL'): Promise<ApiResponse<string>> {
    return request.post('/v1/ai/generate-test-cases', null, {
      params: { apiDescription, testType }
    })
  },

  /**
   * 补全参数描述
   */
  completeDescriptions(parameters: Record<string, string>): Promise<ApiResponse<Record<string, string>>> {
    return request.post('/v1/ai/complete-descriptions', parameters)
  },

  /**
   * 生成模拟数据
   */
  generateMockData(fieldType: string, constraints?: string): Promise<ApiResponse<string>> {
    return request.post('/v1/ai/generate-mock-data', null, {
      params: { fieldType, constraints }
    })
  },

  /**
   * 诊断测试失败原因
   */
  diagnoseFailure(params: {
    caseName: string
    expected: string
    actual: string
    errorMessage: string
    httpStatus?: number
    responseBody?: string
    apiPath?: string
    method?: string
  }): Promise<ApiResponse<DiagnosisResult>> {
    return request.post('/v1/ai/diagnose-failure', params)
  }
}
