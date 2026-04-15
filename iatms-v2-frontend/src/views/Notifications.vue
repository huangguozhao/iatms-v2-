<template>
  <div class="notifications-container page-container">
    <div class="page-header">
      <h2 class="title">消息中心</h2>
      <el-button v-if="notificationStore.hasUnread" type="primary" @click="handleMarkAllRead">
        全部已读
      </el-button>
    </div>

    <el-card>
      <div v-if="notificationStore.notifications.length === 0 && !notificationStore.loading">
        <el-empty description="暂无消息" />
      </div>
      <div v-else v-loading="notificationStore.loading" class="notification-list">
        <div
          v-for="item in notificationStore.notifications"
          :key="item.id"
          class="notification-item"
          :class="{ unread: !item.read }"
          @click="handleItemClick(item)"
        >
          <div class="notification-icon-wrapper">
            <el-icon class="notification-type-icon">
              <Bell v-if="item.type === 'SYSTEM'" />
              <SuccessFilled v-else-if="item.type === 'SUCCESS'" />
              <CircleCloseFilled v-else-if="item.type === 'FAILURE'" />
              <Warning v-else />
            </el-icon>
          </div>
          <div class="notification-content">
            <div class="notification-title">{{ item.title }}</div>
            <div class="notification-text">{{ item.content }}</div>
            <div class="notification-time">{{ item.createdAt }}</div>
          </div>
          <div class="notification-actions">
            <el-button
              v-if="!item.read"
              link
              type="primary"
              size="small"
              @click.stop="handleMarkRead(item.id)"
            >
              标记已读
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              @click.stop="handleDelete(item.id)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>

      <el-pagination
        v-if="notificationStore.notifications.length > 0"
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="notificationStore.pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="loadNotifications"
        @current-change="loadNotifications"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Bell,
  SuccessFilled,
  CircleCloseFilled,
  Warning
} from '@element-plus/icons-vue'
import { useNotificationStore, type NotificationVO } from '@/stores/notification'

const notificationStore = useNotificationStore()

const pagination = reactive({
  pageNum: 1,
  pageSize: 10
})

async function loadNotifications() {
  await notificationStore.fetchNotifications({
    pageNum: pagination.pageNum,
    pageSize: pagination.pageSize
  })
}

async function handleMarkRead(id: number) {
  try {
    await notificationStore.markAsRead(id)
    ElMessage.success('已标记为已读')
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleMarkAllRead() {
  try {
    await notificationStore.markAllAsRead()
    ElMessage.success('已全部标记为已读')
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(id: number) {
  try {
    await notificationStore.deleteNotification(id)
    ElMessage.success('删除成功')
  } catch {
    ElMessage.error('删除失败')
  }
}

function handleItemClick(item: NotificationVO) {
  if (!item.read) {
    handleMarkRead(item.id)
  }
  // 可以根据 relatedType 跳转到相关页面
  if (item.relatedId && item.relatedType) {
    // 路由跳转逻辑
  }
}

// 初始化加载
loadNotifications()
</script>

<style scoped lang="scss">
.notifications-container {
  max-width: 800px;
  margin: 0 auto;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  background: #f5f7fa;
  transition: all 0.2s;
  cursor: pointer;

  &:hover {
    background: #ecf5ff;
  }

  &.unread {
    background: linear-gradient(135deg, rgba(24, 144, 255, 0.08) 0%, rgba(64, 169, 255, 0.08) 100%);
    border-left: 3px solid #1890ff;
  }
}

.notification-icon-wrapper {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-type-icon {
  font-size: 18px;
  color: #fff;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.notification-text {
  font-size: 14px;
  color: #606266;
  margin-bottom: 6px;
  line-height: 1.5;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.notification-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
</style>
