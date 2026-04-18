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

  diagnoseFailure(params: {
    caseName: string
    expected: string
    actual: string
    errorMessage: string
    httpStatus?: number
    responseBody?: string
    apiPath?: string
    method?: string
  }): Promise<DiagnosisResult> {
    return client.post('/v1/ai/diagnose-failure', params)
  }
}
