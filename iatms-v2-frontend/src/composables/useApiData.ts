/**
 * API 数据状态管理 Composable
 */
import { reactive, ref, watch } from 'vue'
import type { ProjectTreeNode } from '@/api/modules/testing/testCase'

export interface ApiData {
  project: string
  projectId: number | null
  module: string
  moduleId: number | null
  apiCode: string
  name: string
  path: string
  method: string
  baseUrl: string
  description: string
  precondition: string
  tags: string[]
  requestParameters: any[]
  pathParameters: any[]
  requestHeaders: any[]
  requestBody: any
  requestBodyType: string
  responseBodyType: string
  status: string
  version: string
  authType: string
  authConfig: any
  examples: any[]
  timeoutSeconds: number
  creatorName: string
  createdAt: string
  updatedAt: string
}

export interface RequestParams {
  headerParams: any[]
  queryParams: any[]
  bodyParams: any[]
  formDataParams: any[]
  rawBody: string
  bodyType: string
}

const defaultApiData = (): ApiData => ({
  project: '',
  projectId: null,
  module: '',
  moduleId: null,
  apiCode: '',
  name: '',
  path: '',
  method: 'GET',
  baseUrl: '',
  description: '',
  precondition: '',
  tags: [],
  requestParameters: [],
  pathParameters: [],
  requestHeaders: [],
  requestBody: null,
  requestBodyType: 'json',
  responseBodyType: '',
  status: 'active',
  version: '',
  authType: '',
  authConfig: null,
  examples: [],
  timeoutSeconds: 30,
  creatorName: '',
  createdAt: '',
  updatedAt: ''
})

const defaultRequestParams = (): RequestParams => ({
  headerParams: [],
  queryParams: [],
  bodyParams: [],
  formDataParams: [],
  rawBody: '',
  bodyType: 'json'
})

export function useApiData() {
  const apiData = reactive<ApiData>(defaultApiData())
  const requestParams = reactive<RequestParams>(defaultRequestParams())

  const saving = ref(false)

  // 从 ProjectTreeNode 同步到 apiData
  const syncFromNode = (node: ProjectTreeNode) => {
    apiData.project = node.projectName || (node as any).project_name || '-'
    apiData.projectId = node.projectId || (node as any).project_id
    apiData.module = (node as any).moduleName || (node as any).module_name || '-'
    apiData.moduleId = (node as any).moduleId || (node as any).module_id
    apiData.apiCode = node.code || (node as any).apiCode || (node as any).api_code || ''
    apiData.name = node.name || ''
    apiData.path = node.path || node.url || ''
    apiData.method = (node as any).method || node.httpMethod || 'GET'
    apiData.baseUrl = (node as any).baseUrl || (node as any).base_url || ''
    apiData.description = node.description || ''
    apiData.precondition = (node as any).precondition || (node as any).pre_condition || ''
    apiData.tags = Array.isArray(node.tags) ? node.tags : []
    apiData.requestBodyType = (node as any).requestBodyType || (node as any).request_body_type || 'json'
    apiData.responseBodyType = (node as any).responseBodyType || (node as any).response_body_type || ''
    apiData.status = node.status || 'active'
    apiData.version = (node as any).version || ''
    apiData.authType = (node as any).authType || (node as any).auth_type || ''
    apiData.authConfig = (node as any).authConfig || (node as any).auth_config
    apiData.timeoutSeconds = (node as any).timeoutSeconds || (node as any).timeout_seconds || 30
    apiData.creatorName = (node as any).creatorName || (node as any).creator_name || ''
    apiData.createdAt = node.createdAt || (node as any).created_time || ''
    apiData.updatedAt = node.updatedAt || (node as any).updated_time || ''

    // 同步请求参数
    syncRequestParams(node)
  }

  // 同步请求参数
  const syncRequestParams = (node: ProjectTreeNode) => {
    // Headers
    const headers = (node as any).requestHeaders || (node as any).request_headers || (node as any).headers
    if (Array.isArray(headers)) {
      requestParams.headerParams = headers.map((h: any) => {
        if (typeof h === 'object' && h.name !== undefined) {
          return { name: h.name, value: h.value || '', description: h.description || '' }
        }
        return { name: String(h), value: '', description: '' }
      })
    } else {
      requestParams.headerParams = []
    }

    // Query Params
    const reqParams = (node as any).requestParameters || (node as any).queryParams
    if (Array.isArray(reqParams)) {
      requestParams.queryParams = reqParams.map((p: any) => {
        if (typeof p === 'object' && p.name !== undefined) {
          return { name: p.name, value: p.value || '', description: p.description || '' }
        }
        return { name: String(p), value: '', description: '' }
      })
    } else {
      requestParams.queryParams = []
    }

    // Body
    const reqBodyType = (node as any).requestBodyType || (node as any).request_body_type || 'json'
    requestParams.bodyType = reqBodyType

    const reqBody = (node as any).requestBody || (node as any).request_body
    if (typeof reqBody === 'string') {
      try {
        const parsed = JSON.parse(reqBody)
        if (Array.isArray(parsed)) {
          requestParams.bodyParams = parsed.map((p: any) => {
            if (typeof p === 'object' && p.name !== undefined) {
              return { name: p.name, value: p.value || '', description: p.description || '' }
            }
            return { name: String(p), value: '', description: '' }
          })
        } else {
          requestParams.rawBody = reqBody
        }
      } catch {
        requestParams.rawBody = reqBody
      }
    } else if (Array.isArray(reqBody)) {
      requestParams.bodyParams = reqBody.map((p: any) => {
        if (typeof p === 'object' && p.name !== undefined) {
          return { name: p.name, value: p.value || '', description: p.description || '' }
        }
        return { name: String(p), value: '', description: '' }
      })
    } else {
      requestParams.bodyParams = []
      requestParams.rawBody = ''
    }

    requestParams.formDataParams = []
  }

  // 重置
  const reset = () => {
    Object.assign(apiData, defaultApiData())
    Object.assign(requestParams, defaultRequestParams())
  }

  // 保存（需要外部传入API）
  const save = async (updateApi: (id: number, data: any) => Promise<any>) => {
    if (!apiData.projectId) return
    saving.value = true
    try {
      await updateApi(apiData.projectId, {
        name: apiData.name,
        path: apiData.path,
        method: apiData.method,
        description: apiData.description,
        // ... 其他字段
      })
    } finally {
      saving.value = false
    }
  }

  return {
    apiData,
    requestParams,
    saving,
    syncFromNode,
    syncRequestParams,
    reset,
    save
  }
}
