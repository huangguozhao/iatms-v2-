import { client } from '@/api/client'
import type { ApiSummaryVO, ApiDetailVO, CreateApiDTO, ApiQuery, PageResult } from '@/types/api'

export type { ApiSummaryVO, ApiDetailVO, CreateApiDTO, ApiQuery, PageResult }

export const apiApi = {
  query: (query: ApiQuery): Promise<PageResult<ApiSummaryVO>> => {
    return client.get('/v1/apis', {
      params: {
        keyword: query.keyword,
        httpMethod: query.method,
        projectId: query.projectId,
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
