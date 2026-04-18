import { client } from '@/api/client'
import type { PageResult } from '@/types/api'

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
  query(params: NotificationQueryDTO): Promise<PageResult<NotificationVO>> {
    return client.get('/v1/notifications', { params })
  },

  getUnreadCount(): Promise<number> {
    return client.get('/v1/notifications/unread-count')
  },

  markAsRead(id: number): Promise<void> {
    return client.put(`/v1/notifications/${id}/read`)
  },

  markAllAsRead(): Promise<void> {
    return client.put('/v1/notifications/read-all')
  },

  delete(id: number): Promise<void> {
    return client.delete(`/v1/notifications/${id}`)
  },
}
