import { client } from '@/api/client'
import type { PageResult } from '@/types/api'

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
  projectId?: number | null
  moduleId?: number | null
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
  query: (query: ApiQuery): Promise<PageResult<ApiSummaryVO>> => {
    return client.get('/v1/apis', {
      params: {
        keyword: query.keyword,
        httpMethod: query.method,
        projectId: query.projectId,
        collectionId: query.moduleId,
        pageNum: query.pageNum,
        pageSize: query.pageSize
      }
    })
  },

  getDetail: (id: number): Promise<ApiDetailVO> => {
    return client.get(`/v1/apis/${id}`)
  },

  create: (data: CreateApiDTO): Promise<void> => {
    return client.post('/v1/apis', data)
  },

  update: (id: number, data: CreateApiDTO): Promise<void> => {
    return client.put(`/v1/apis/${id}`, data)
  },

  delete: (id: number): Promise<void> => {
    return client.delete(`/v1/apis/${id}`)
  }
}
