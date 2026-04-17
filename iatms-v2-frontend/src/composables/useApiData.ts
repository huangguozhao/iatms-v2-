/**
 * API 数据状态管理 Composable
 */
import { reactive, ref } from 'vue'
import { apiApi } from '@/api/modules/testing/api'
import type { ApiDetailVO } from '@/types/api'

export interface ApiData {
  // 基本信息
  id: number | null
  name: string
  description: string
  httpMethod: string
  url: string
  requestType: string
  status: string
  version: string
  // 项目/模块
  projectId: number | null
  projectName: string
  moduleId: number | null
  moduleName: string
  // 认证
  authType: string
  authConfig: any
  // 请求信息
  headers: any
  queryParams: any
  requestBody: any
  requestBodyType: string
  // 其他
  timeoutSeconds: number
  tags: string[]
  // 审计
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
  id: null,
  name: '',
  description: '',
  httpMethod: 'GET',
  url: '',
  requestType: '',
  status: 'active',
  version: '',
  projectId: null,
  projectName: '',
  moduleId: null,
  moduleName: '',
  authType: '',
  authConfig: null,
  headers: {},
  queryParams: {},
  requestBody: null,
  requestBodyType: 'json',
  timeoutSeconds: 30,
  tags: [],
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
  const loading = ref(false)
  const saving = ref(false)

  // 从后端 API 响应同步到 apiData
  const syncFromDetail = (detail: ApiDetailVO) => {
    apiData.id = detail.id
    apiData.name = detail.name || ''
    apiData.description = detail.description || ''
    apiData.httpMethod = detail.httpMethod || detail.method || 'GET'
    apiData.url = detail.url || detail.path || ''
    apiData.requestType = detail.requestType || ''
    apiData.status = detail.status || 'active'
    apiData.version = (detail as any).version || ''

    apiData.projectId = detail.projectId || null
    apiData.projectName = detail.projectName || ''
    apiData.moduleId = detail.moduleId || null
    apiData.moduleName = detail.moduleName || ''

    // 认证
    apiData.authType = (detail as any).authType || (detail as any).auth_type || ''
    apiData.authConfig = detail.authConfig || null

    // 请求信息
    apiData.requestBodyType = (detail as any).requestBodyType || (detail as any).request_body_type || 'json'

    // 解析 headers
    const headers = detail.headers
    if (typeof headers === 'string') {
      try {
        apiData.headers = JSON.parse(headers)
      } catch {
        apiData.headers = {}
      }
    } else if (typeof headers === 'object') {
      apiData.headers = headers || {}
    } else {
      apiData.headers = {}
    }

    // 解析 queryParams
    const queryParams = detail.queryParams || (detail as any).queryParams
    if (typeof queryParams === 'string') {
      try {
        apiData.queryParams = JSON.parse(queryParams)
      } catch {
        apiData.queryParams = {}
      }
    } else if (typeof queryParams === 'object') {
      apiData.queryParams = queryParams || {}
    } else {
      apiData.queryParams = {}
    }

    // 解析 requestBody
    const requestBody = detail.requestBody || (detail as any).requestBody
    if (typeof requestBody === 'string') {
      try {
        apiData.requestBody = JSON.parse(requestBody)
      } catch {
        apiData.requestBody = requestBody
      }
    } else {
      apiData.requestBody = requestBody
    }

    // 审计
    apiData.creatorName = detail.creatorName || ''
    apiData.createdAt = detail.createdAt || ''
    apiData.updatedAt = detail.updatedAt || ''

    // 同步请求参数
    syncRequestParamsFromData()
  }

  // 从 apiData 同步到 requestParams
  const syncRequestParamsFromData = () => {
    // Headers - 兼容数组格式 [{name, value, description}] 和对象格式 {key: value}
    if (apiData.headers) {
      if (Array.isArray(apiData.headers)) {
        requestParams.headerParams = apiData.headers.map(item => ({
          name: item.name || '',
          value: item.value || '',
          description: item.description || ''
        }))
      } else if (typeof apiData.headers === 'object') {
        requestParams.headerParams = Object.entries(apiData.headers).map(([name, value]) => ({
          name,
          value: String(value),
          description: ''
        }))
      } else {
        requestParams.headerParams = []
      }
    } else {
      requestParams.headerParams = []
    }

    // Query Params - 兼容数组格式 [{name, value, description}] 和对象格式 {key: value}
    if (apiData.queryParams) {
      if (Array.isArray(apiData.queryParams)) {
        requestParams.queryParams = apiData.queryParams.map(item => ({
          name: item.name || '',
          value: item.value || '',
          description: item.description || ''
        }))
      } else if (typeof apiData.queryParams === 'object') {
        requestParams.queryParams = Object.entries(apiData.queryParams).map(([name, value]) => ({
          name,
          value: String(value),
          description: ''
        }))
      } else {
        requestParams.queryParams = []
      }
    } else {
      requestParams.queryParams = []
    }

    // Body
    if (apiData.requestBody) {
      if (typeof apiData.requestBody === 'object') {
        requestParams.bodyParams = Object.entries(apiData.requestBody).map(([name, value]) => ({
          name,
          value: String(value),
          description: ''
        }))
        requestParams.rawBody = JSON.stringify(apiData.requestBody, null, 2)
      } else {
        requestParams.bodyParams = []
        requestParams.rawBody = String(apiData.requestBody)
      }
    } else {
      requestParams.bodyParams = []
      requestParams.rawBody = ''
    }

    requestParams.bodyType = apiData.requestBodyType || 'json'
    requestParams.formDataParams = []
  }

  // 从 ProjectTreeNode 同步（用于树节点点击时）
  const syncFromNode = (node: any) => {
    apiData.id = node.id
    apiData.name = node.name || ''
    apiData.description = node.description || ''
    apiData.httpMethod = node.httpMethod || node.method || 'GET'
    apiData.url = node.path || node.url || ''
    apiData.status = node.status || 'active'

    apiData.projectId = node.projectId || null
    apiData.projectName = node.projectName || ''
    apiData.moduleId = node.moduleId || null
    apiData.moduleName = node.moduleName || ''

    apiData.requestBodyType = node.requestBodyType || node.request_body_type || 'json'
    apiData.creatorName = node.creatorName || node.creator_name || ''
    apiData.createdAt = node.createdAt || node.created_time || ''
    apiData.updatedAt = node.updatedAt || node.updated_time || ''

    // 从 node 同步 headers/queryParams/requestBody
    const headers = node.headers || node.requestHeaders || node.request_headers
    if (typeof headers === 'string') {
      try {
        apiData.headers = JSON.parse(headers)
      } catch {
        apiData.headers = {}
      }
    } else if (typeof headers === 'object') {
      apiData.headers = headers || {}
    } else {
      apiData.headers = {}
    }

    const queryParams = node.queryParams || node.requestParams || node.request_parameters
    if (typeof queryParams === 'string') {
      try {
        apiData.queryParams = JSON.parse(queryParams)
      } catch {
        apiData.queryParams = {}
      }
    } else if (typeof queryParams === 'object') {
      apiData.queryParams = queryParams || {}
    } else {
      apiData.queryParams = {}
    }

    const requestBody = node.requestBody || node.request_body
    if (typeof requestBody === 'string') {
      try {
        apiData.requestBody = JSON.parse(requestBody)
      } catch {
        apiData.requestBody = requestBody
      }
    } else {
      apiData.requestBody = requestBody
    }

    syncRequestParamsFromData()
  }

  // 加载 API 详情
  const loadApiDetail = async (apiId: number) => {
    loading.value = true
    try {
      const detail = await apiApi.getDetail(apiId)
      syncFromDetail(detail)
    } catch (e) {
      console.error('加载 API 详情失败', e)
      throw e
    } finally {
      loading.value = false
    }
  }

  // 保存 API
  const saveApi = async () => {
    if (!apiData.id) return

    saving.value = true
    try {
      // 构建请求数据
      const data: any = {
        name: apiData.name,
        description: apiData.description,
        httpMethod: apiData.httpMethod,
        url: apiData.url,
        requestBody: typeof apiData.requestBody === 'object' ? JSON.stringify(apiData.requestBody) : apiData.requestBody,
        headers: typeof apiData.headers === 'object' ? JSON.stringify(apiData.headers) : apiData.headers,
        queryParams: typeof apiData.queryParams === 'object' ? JSON.stringify(apiData.queryParams) : apiData.queryParams
      }

      await apiApi.update(apiData.id, data)
    } finally {
      saving.value = false
    }
  }

  // 重置
  const reset = () => {
    Object.assign(apiData, defaultApiData())
    Object.assign(requestParams, defaultRequestParams())
  }

  return {
    apiData,
    requestParams,
    loading,
    saving,
    syncFromDetail,
    syncFromNode,
    syncRequestParamsFromData,
    loadApiDetail,
    saveApi,
    reset
  }
}
