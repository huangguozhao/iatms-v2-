<template>
  <el-container class="layout-container">
    <!-- 顶部标题栏 -->
    <el-header class="header glass-card">
      <div class="header-left">
        <h1 class="system-title text-gradient-primary">接口自动化管理</h1>
        <p class="system-subtitle">API AutoOps</p>
      </div>
      <div class="header-right">
        <el-link class="nav-link hover-scale" @click.prevent="router.push('/dashboard')">首页</el-link>
        <div class="search-wrapper">
          <el-input
            v-model="searchText"
            placeholder="搜索..."
            class="search-input modern-input"
            :prefix-icon="Search"
          />
        </div>
        <div
          class="notification-wrapper glass-btn hover-lift"
          @click="handleNotificationClick"
          role="button"
          aria-label="Open message center"
          tabindex="0"
          @keydown.enter.prevent="handleNotificationClick"
        >
          <el-badge :value="notificationStore.unreadCount" class="notification-badge" :hidden="notificationStore.unreadCount === 0">
            <el-icon class="notification-icon">
              <Bell />
            </el-icon>
          </el-badge>
        </div>
        <div class="user-info glass-btn hover-lift clickable" @click="handleUserClick">
          <el-avatar :size="40" class="user-avatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <div class="user-details">
            <div class="user-name">{{ userStore.userInfo?.displayName || userStore.userInfo?.username || '用户' }}</div>
            <div class="user-team">{{ userStore.userInfo?.email || '测试团队' }}</div>
          </div>
          <el-icon class="dropdown-icon">
            <ArrowDown />
          </el-icon>
        </div>
      </div>
    </el-header>

    <el-container>
      <!-- 左侧菜单栏 -->
      <el-aside :width="sidebarCollapsed ? '60px' : '200px'" class="sidebar" :class="{ collapsed: sidebarCollapsed }">
        <div class="sidebar-menu">
          <div
            v-for="item in menuItems"
            :key="item.index"
            class="menu-item"
            :class="{ 'is-active': activeMenu === item.index }"
            @click="handleMenuSelect(item.index)"
          >
            <el-icon class="menu-icon"><component :is="item.icon" /></el-icon>
            <span class="menu-label" v-if="!sidebarCollapsed">{{ item.label }}</span>
          </div>
        </div>
        <div class="collapse-nav">
          <button class="collapse-btn" @click="toggleSidebar">
            <span class="collapse-icon">{{ sidebarCollapsed ? '›' : '‹' }}</span>
            <span v-if="!sidebarCollapsed">收起导航</span>
            <span v-else>展开导航</span>
          </button>
        </div>
      </el-aside>

      <!-- 右侧内容区域 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  Search,
  Bell,
  User,
  ArrowDown,
  Home,
  Folder,
  Link,
  Document,
  Collection,
  VideoPlay,
  Calendar,
  DataAnalysis,
  Robot,
  Setting
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useNotificationStore } from '@/stores/notification'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const notificationStore = useNotificationStore()

const searchText = ref('')
const sidebarCollapsed = ref(false)

// 根据用户角色获取菜单项
const menuItems = computed(() => {
  const items = [
    { index: 'dashboard', label: '首页', icon: Home, path: '/dashboard' },
    { index: 'projects', label: '项目管理', icon: Folder, path: '/projects' },
    { index: 'apis', label: '接口管理', icon: Link, path: '/apis' },
    { index: 'test-cases', label: '测试用例', icon: Document, path: '/test-cases' },
    { index: 'test-suites', label: '测试套件', icon: Collection, path: '/test-suites' },
    { index: 'executions', label: '执行记录', icon: VideoPlay, path: '/executions' },
    { index: 'scheduled-tasks', label: '定时任务', icon: Calendar, path: '/scheduled-tasks' },
    { index: 'reports', label: '测试报告', icon: DataAnalysis, path: '/reports' },
    { index: 'ai-diagnosis', label: 'AI 诊断', icon: Robot, path: '/ai-diagnosis' },
    { index: 'settings', label: '系统设置', icon: Setting, path: '/settings' }
  ]
  return items
})

// 根据当前路由设置活动菜单
const activeMenu = computed(() => {
  const path = route.path
  const item = menuItems.value.find(item => path.startsWith(item.path))
  return item ? item.index : 'dashboard'
})

// 切换侧边栏
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const handleMenuSelect = (index) => {
  const item = menuItems.value.find(item => item.index === index)
  if (item && item.path) {
    router.push(item.path)
  }
}

const handleNotificationClick = () => {
  router.push('/notifications')
}

const handleUserClick = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '退出确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    userStore.logout()
    router.push('/login')
  } catch {
    // 用户取消退出
  }
}

onMounted(() => {
  if (userStore.token && !userStore.userInfo) {
    userStore.fetchUserInfo()
  }
  if (userStore.token) {
    notificationStore.fetchUnreadCount()
  }
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  overflow: hidden;
}

.header {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-bottom: 1px solid rgba(228, 231, 237, 0.5);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow:
    0 2px 8px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  height: 64px;
  flex-shrink: 0;
  position: relative;
  z-index: 100;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.system-title {
  font-family: '楷体', 'KaiTi', serif;
  font-size: 28px;
  font-weight: bold;
  margin: 0;
  line-height: 1.2;
  letter-spacing: 1px;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.system-subtitle {
  font-size: 13px;
  color: #909399;
  margin: 0;
  font-weight: 400;
  letter-spacing: 0.5px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-link {
  font-size: 15px;
  color: #606266;
  text-decoration: none;
  padding: 6px 12px;
  border-radius: 8px;
  transition: all 0.2s ease;
  font-weight: 500;
}

.nav-link:hover {
  color: #1890ff;
  background: rgba(24, 144, 255, 0.08);
}

.search-wrapper {
  position: relative;
}

.search-wrapper :deep(.el-input) {
  width: 240px;
}

.search-wrapper :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  border: 1px solid rgba(228, 231, 237, 0.8);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;
  padding: 8px 12px;
}

.search-wrapper :deep(.el-input__wrapper:hover) {
  border-color: #1890ff;
  box-shadow: 0 2px 12px rgba(24, 144, 255, 0.15);
}

.search-wrapper :deep(.el-input.is-focus .el-input__wrapper) {
  border-color: #1890ff;
  box-shadow:
    0 0 0 3px rgba(24, 144, 255, 0.1),
    0 2px 12px rgba(24, 144, 255, 0.15);
  background: rgba(255, 255, 255, 0.9);
}

.search-wrapper :deep(.el-input__inner) {
  font-size: 14px;
}

.notification-wrapper {
  padding: 8px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
  height: 40px;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(228, 231, 237, 0.5);
  transition: all 0.2s ease;
}

.notification-wrapper:hover {
  background: rgba(255, 255, 255, 0.8);
  border-color: #1890ff;
}

.notification-badge {
  cursor: pointer;
}

.notification-icon {
  font-size: 20px;
  color: #606266;
  transition: color 0.2s ease;
}

.notification-wrapper:hover .notification-icon {
  color: #1890ff;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 12px 6px 6px;
  border-radius: 12px;
  cursor: pointer;
  min-width: 160px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(228, 231, 237, 0.5);
  transition: all 0.2s ease;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.8);
  border-color: #1890ff;
}

.user-avatar {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
  flex-shrink: 0;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-team {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-icon {
  font-size: 12px;
  color: #c0c4cc;
  transition: transform 0.2s ease, color 0.2s ease;
  flex-shrink: 0;
}

.user-info:hover .dropdown-icon {
  color: #606266;
  transform: translateY(1px);
}

.sidebar {
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  height: calc(100vh - 64px);
  overflow: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
}

.sidebar.collapsed {
  width: 60px !important;
}

.sidebar-menu {
  flex: 1;
  padding: 8px 0;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-menu::-webkit-scrollbar {
  width: 6px;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.sidebar-menu::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.sidebar-menu::-webkit-scrollbar-track {
  background: transparent;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  cursor: pointer;
  transition: all 0.2s;
  color: #606266;
  margin: 2px 8px;
  border-radius: 8px;
  font-size: 15px;
}

.menu-item:hover {
  background: rgba(24, 144, 255, 0.08);
  color: #1890ff;
}

.menu-item.is-active {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  color: white;
  box-shadow: 0 2px 12px rgba(24, 144, 255, 0.3);
}

.menu-icon {
  font-size: 18px;
  flex-shrink: 0;
  width: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-label {
  font-size: 14px;
  flex: 1;
  white-space: nowrap;
}

.sidebar.collapsed .menu-item {
  justify-content: center;
  padding: 12px;
}

.collapse-nav {
  padding: 16px;
  border-top: 1px solid #e4e7ed;
}

.sidebar.collapsed .collapse-nav {
  padding: 16px 0;
  text-align: center;
}

.collapse-btn {
  width: 100%;
  padding: 8px 12px;
  background: transparent;
  border: none;
  color: #909399;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
  border-radius: 4px;
}

.collapse-btn:hover {
  color: #606266;
  background: #f5f7fa;
}

.collapse-icon {
  font-size: 18px;
}

.main-content {
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
  overflow-x: hidden;
  height: calc(100vh - 64px);
}

.main-content::-webkit-scrollbar {
  width: 8px;
}

.main-content::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 4px;
}

.main-content::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.main-content::-webkit-scrollbar-track {
  background: #f5f7fa;
}

/* Glass card effect */
.glass-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

/* Glass button effect */
.glass-btn {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  border: 1px solid rgba(228, 231, 237, 0.5);
}

/* Text gradient */
.text-gradient-primary {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Hover scale effect */
.hover-scale {
  transition: transform 0.2s ease;
}

.hover-scale:hover {
  transform: scale(1.05);
}

/* Hover lift effect */
.hover-lift {
  transition: all 0.2s ease;
}

.hover-lift:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* Responsive design */
@media (max-width: 768px) {
  .header {
    padding: 0 16px;
    height: 56px;
  }

  .system-title {
    font-size: 20px;
  }

  .system-subtitle {
    font-size: 11px;
  }

  .header-right {
    gap: 8px;
  }

  .nav-link {
    display: none;
  }

  .search-wrapper :deep(.el-input) {
    width: 140px;
  }

  .user-info {
    min-width: auto;
    padding: 4px 8px 4px 4px;
    gap: 8px;
  }

  .user-details {
    display: none;
  }

  .dropdown-icon {
    display: none;
  }
}

@media (max-width: 480px) {
  .search-wrapper {
    display: none;
  }

  .notification-wrapper {
    min-width: 36px;
    height: 36px;
    padding: 6px;
  }
}
</style>
