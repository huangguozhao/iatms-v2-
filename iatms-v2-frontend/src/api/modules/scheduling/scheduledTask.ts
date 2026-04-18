import { client } from '@/api/client'
import type { PageResult, ScheduledTaskSummaryVO, ScheduledTaskDetailVO, ScheduledTaskQuery } from '@/types/api'

export type { ScheduledTaskSummaryVO, ScheduledTaskDetailVO }

export interface CreateScheduledTaskDTO {
  name: string
  type: string
  targetId: number
  cron: string
  strategy: string
  notifyOn: string[]
  description?: string
  triggerType?: string
  cronExpression?: string
  dailyHour?: number
  dailyMinute?: number
  weeklyDays?: string
  monthlyDay?: number
  simpleRepeatInterval?: number
  simpleRepeatCount?: number
  timeoutSeconds?: number
  retryEnabled?: boolean
  maxRetryAttempts?: number
  notifyOnSuccess?: boolean
  notifyOnFailure?: boolean
  caseIds?: number[]
}

export { type ScheduledTaskQuery }

export const scheduledTaskApi = {
  query: (query: ScheduledTaskQuery) => {
    return client.get<PageResult<ScheduledTaskSummaryVO>, PageResult<ScheduledTaskSummaryVO>>('/v1/scheduled-tasks', { params: query })
  },

  getDetail: (id: number) => {
    return client.get<ScheduledTaskDetailVO, ScheduledTaskDetailVO>(`/v1/scheduled-tasks/${id}`)
  },

  create: (data: CreateScheduledTaskDTO) => {
    return client.post<any, any>('/v1/scheduled-tasks', data)
  },

  update: (id: number, data: CreateScheduledTaskDTO) => {
    return client.put<any, any>(`/v1/scheduled-tasks/${id}`, data)
  },

  delete: (id: number) => {
    return client.delete<any, any>(`/v1/scheduled-tasks/${id}`)
  },

  pause: (id: number) => {
    return client.post<any, any>(`/v1/scheduled-tasks/${id}/pause`)
  },

  resume: (id: number) => {
    return client.post<any, any>(`/v1/scheduled-tasks/${id}/resume`)
  },

  execute: (id: number) => {
    return client.post<any, any>(`/v1/scheduled-tasks/${id}/run`)
  }
}
