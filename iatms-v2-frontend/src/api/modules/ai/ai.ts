import { client } from '@/api/client'

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

export interface DiagnosisContext {
  executionId: string
  executionScope: string
  scopeName: string
  environment: string
  status: string
  totalCases: number
  passedCases: number
  failedCases: number
}

// 获取API基础路径
function getApiBasePath(): string {
  // 使用后端配置的 context-path: /api
  return '/api'
}

export const aiApi = {
  generateTestCases(apiDescription: string, testType: string = 'FUNCTIONAL'): Promise<string> {
    return client.post('/v1/ai/generate-test-cases', null, {
      params: { apiDescription, testType }
    })
  },

  completeDescriptions(parameters: Record<string, string>): Promise<Record<string, string>> {
    return client.post('/v1/ai/complete-descriptions', parameters)
  },

  generateMockData(fieldType: string, constraints?: string): Promise<string> {
    return client.post('/v1/ai/generate-mock-data', null, {
      params: { fieldType, constraints }
    })
  },

  /**
   * 诊断测试失败 - SSE版本（推荐）
   * 返回 EventSource 用于前端监听
   */
  diagnoseFailureSSE(executionId: string): EventSource {
    const basePath = getApiBasePath()
    const token = localStorage.getItem('token') || ''
    const url = `${basePath}/v1/ai/diagnose-failure/sse?executionId=${executionId}&token=${encodeURIComponent(token)}`
    console.log('SSE连接URL:', url)
    return new EventSource(url)
  },

  /**
   * 诊断测试失败 - 同步版本（简单场景）
   */
  diagnoseFailure(params: {
    executionId?: string
    caseName?: string
    expected?: string
    actual?: string
    errorMessage?: string
    httpStatus?: number
    responseBody?: string
    apiPath?: string
    method?: string
  }): Promise<DiagnosisResult> {
    return client.post('/v1/ai/diagnose-failure', params)
  }
}
