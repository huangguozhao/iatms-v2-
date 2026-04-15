import { client } from '@/api/client'
import type { ApiResponse, PageResult } from '@/types/api'

export interface ScheduledTaskSummaryVO {
  id: number
  name: string
  type: string
  targetId: number
  targetName: string
  cron: string
  status: string
  nextRunTime: string
  lastRunTime: string | null
  createdAt: string
}

export interface ScheduledTaskDetailVO extends ScheduledTaskSummaryVO {
  notifyOn: string[]
  description: string
}

export interface CreateScheduledTaskDTO {
  name: string
  type: string
  targetId: number
  cron: string
  strategy: string
  notifyOn: string[]
  description?: string
}

export interface ScheduledTaskQuery {
  keyword?: string
  type?: string
  status?: string
  pageNum: number
  pageSize: number
}

export const scheduledTaskApi = {
  query: (query: ScheduledTaskQuery) => {
    return client.get<any, ApiResponse<PageResult<ScheduledTaskSummaryVO>>>('/scheduled-tasks', { params: query })
  },

  getDetail: (id: number) => {
    return client.get<any, ApiResponse<ScheduledTaskDetailVO>>(`/scheduled-tasks/${id}`)
  },

  create: (data: CreateScheduledTaskDTO) => {
    return client.post<any, ApiResponse<void>>('/scheduled-tasks', data)
  },

  update: (id: number, data: CreateScheduledTaskDTO) => {
    return client.put<any, ApiResponse<void>>(`/scheduled-tasks/${id}`, data)
  },

  delete: (id: number) => {
    return client.delete<any, ApiResponse<void>>(`/scheduled-tasks/${id}`)
  },

  enable: (id: number) => {
    return client.post<any, ApiResponse<void>>(`/scheduled-tasks/${id}/enable`)
  },

  disable: (id: number) => {
    return client.post<any, ApiResponse<void>>(`/scheduled-tasks/${id}/disable`)
  },

  execute: (id: number) => {
    return client.post<any, ApiResponse<void>>(`/scheduled-tasks/${id}/execute`)
  }
}
