import { client } from '@/api/client'
import type { ApiResponse, PageResult } from '@/types/api'

export interface ApiSummaryVO {
  id: number
  name: string
  method: string
  path: string
  projectId: number
  projectName: string
  moduleId: number | null
  moduleName: string | null
  description: string
  status: string
  createdAt: string
}

export interface ApiDetailVO extends ApiSummaryVO {
  headers: Record<string, string>
  requestBody: string
  responseBody: string
  assertions: string
}

export interface CreateApiDTO {
  name: string
  projectId: number
  moduleId?: number
  method: string
  path: string
  description?: string
  headers?: Record<string, string>
  requestBody?: string
  assertions?: string
}

export interface ApiQuery {
  keyword?: string
  method?: string
  projectId?: number
  moduleId?: number
  pageNum: number
  pageSize: number
}

export const apiApi = {
  query: (query: ApiQuery) => {
    return client.get<any, ApiResponse<PageResult<ApiSummaryVO>>>('/api/apis', { params: query })
  },

  getDetail: (id: number) => {
    return client.get<any, ApiResponse<ApiDetailVO>>(`/api/apis/${id}`)
  },

  create: (data: CreateApiDTO) => {
    return client.post<any, ApiResponse<void>>('/api/apis', data)
  },

  update: (id: number, data: CreateApiDTO) => {
    return client.put<any, ApiResponse<void>>(`/api/apis/${id}`, data)
  },

  delete: (id: number) => {
    return client.delete<any, ApiResponse<void>>(`/api/apis/${id}`)
  }
}
