import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { notificationApi, type NotificationVO, type NotificationQueryDTO } from '@/api/modules/notification/notification'

export type { NotificationVO, NotificationQueryDTO }

export const useNotificationStore = defineStore('notification', () => {
  // 状态
  const notifications = ref<NotificationVO[]>([])
  const unreadCount = ref(0)
  const loading = ref(false)
  const pagination = ref({
    pageNum: 1,
    pageSize: 10,
    total: 0
  })

  // 计算属性
  const hasUnread = computed(() => unreadCount.value > 0)

  const unreadNotifications = computed(() =>
    notifications.value.filter(n => !n.read)
  )

  // Actions
  async function fetchNotifications(params?: NotificationQueryDTO) {
    loading.value = true
    try {
      const result = await notificationApi.query({
        pageNum: params?.pageNum ?? pagination.value.pageNum,
        pageSize: params?.pageSize ?? pagination.value.pageSize,
        keyword: params?.keyword,
        type: params?.type,
        read: params?.read
      })
      notifications.value = result?.records || []
      pagination.value.total = result?.total || 0
      return result
    } finally {
      loading.value = false
    }
  }

  async function fetchUnreadCount() {
    try {
      const result = await notificationApi.getUnreadCount()
      unreadCount.value = result || 0
      return result
    } catch (error) {
      console.error('获取未读数量失败:', error)
      return 0
    }
  }

  async function markAsRead(id: number) {
    try {
      await notificationApi.markAsRead(id)
      const notification = notifications.value.find(n => n.id === id)
      if (notification && !notification.read) {
        notification.read = true
        unreadCount.value = Math.max(0, unreadCount.value - 1)
      }
    } catch (error) {
      console.error('标记已读失败:', error)
      throw error
    }
  }

  async function markAllAsRead() {
    try {
      await notificationApi.markAllAsRead()
      notifications.value.forEach(n => { n.read = true })
      unreadCount.value = 0
    } catch (error) {
      console.error('标记全部已读失败:', error)
      throw error
    }
  }

  async function deleteNotification(id: number) {
    try {
      await notificationApi.delete(id)
      const index = notifications.value.findIndex(n => n.id === id)
      if (index > -1) {
        const notification = notifications.value[index]
        if (!notification.read) {
          unreadCount.value = Math.max(0, unreadCount.value - 1)
        }
        notifications.value.splice(index, 1)
      }
    } catch (error) {
      console.error('删除通知失败:', error)
      throw error
    }
  }

  function clearNotifications() {
    notifications.value = []
    unreadCount.value = 0
  }

  return {
    // 状态
    notifications,
    unreadCount,
    loading,
    pagination,
    // 计算属性
    hasUnread,
    unreadNotifications,
    // Actions
    fetchNotifications,
    fetchUnreadCount,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    clearNotifications
  }
})
