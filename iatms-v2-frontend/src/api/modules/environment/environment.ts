import { client } from '@/api/client'
import type { ApiResponse } from '@/types/api'

export interface EnvironmentVO {
  envId: number
  envCode: string
  envName: string
  envType: string
  description?: string
  baseUrl?: string
  domain?: string
  protocol?: string
  port?: number
  status: string
  isDefault: boolean
  variables?: Record<string, any>
}

export const environmentApi = {
  /**
   * 获取所有激活的环境列表
   */
  list: () => {
    return client.get<any, ApiResponse<EnvironmentVO[]>>('/v1/environments')
  },

  /**
   * 根据ID获取环境详情
   */
  getById: (envId: number) => {
    return client.get<any, ApiResponse<EnvironmentVO>>(`/v1/environments/${envId}`)
  },

  /**
   * 获取默认环境
   */
  getDefault: () => {
    return client.get<any, ApiResponse<EnvironmentVO>>('/v1/environments/default')
  }
}
