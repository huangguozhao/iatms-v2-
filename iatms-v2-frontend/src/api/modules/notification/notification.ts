import request from '@/api/client'
import type { ApiResponse, PageResult } from '@/types/api'

export interface NotificationVO {
  id: number
  type: string
  title: string
  content: string
  relatedId?: number
  relatedType?: string
  read: boolean
  createdAt: string
}

export interface NotificationQueryDTO {
  keyword?: string
  type?: string
  read?: boolean
  pageNum?: number
  pageSize?: number
}

export const notificationApi = {
  query(params: NotificationQueryDTO): Promise<ApiResponse<PageResult<NotificationVO>>> {
    return request.get('/v1/notifications', { params })
  },

  getUnreadCount(): Promise<ApiResponse<number>> {
    return request.get('/v1/notifications/unread-count')
  },

  markAsRead(id: number): Promise<ApiResponse<void>> {
    return request.put(`/v1/notifications/${id}/read`)
  },

  markAllAsRead(): Promise<ApiResponse<void>> {
    return request.put('/v1/notifications/read-all')
  },

  delete(id: number): Promise<ApiResponse<void>> {
    return request.delete(`/v1/notifications/${id}`)
  },
}
