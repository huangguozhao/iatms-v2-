<template>
  <div class="dashboard-container">
    <!-- 欢迎区域 -->
    <div class="welcome-section modern-card">
      <div class="welcome-content">
        <h1 class="welcome-title">欢迎回来</h1>
        <p class="welcome-subtitle">今天是测试的好日子，让我们开始吧！</p>
      </div>
      <div class="welcome-actions">
        <el-button type="primary" class="btn-gradient-primary" @click="router.push('/projects')">
          新建项目
        </el-button>
        <el-button @click="router.push('/test-cases')">用例管理</el-button>
      </div>
    </div>

    <!-- 指标卡片区域 -->
    <div class="metrics-section">
      <el-row :gutter="20">
        <el-col :span="6" v-for="(stat, index) in stats" :key="index">
          <div class="metric-card modern-card hover-lift">
            <div class="metric-icon" :style="{ backgroundColor: stat.color }">
              {{ stat.icon }}
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ stat.value }}</div>
              <div class="metric-title">{{ stat.title }}</div>
              <div class="metric-change" :class="stat.changeType">
                {{ stat.change }}
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 主内容区域 -->
    <el-row :gutter="20" class="main-content-section">
      <el-col :span="16">
        <div class="content-card modern-card">
          <div class="card-header">
            <h3>最近项目</h3>
            <el-button text type="primary" @click="router.push('/projects')">查看全部</el-button>
          </div>
          <el-table :data="recentProjects" stripe class="modern-table">
            <el-table-column prop="name" label="项目名称" min-width="120" />
            <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="120" />
          </el-table>
        </div>
      </el-col>

      <el-col :span="8">
        <div class="quick-card modern-card">
          <div class="card-header">
            <h3>快捷入口</h3>
          </div>
          <div class="quick-actions">
            <div class="quick-item hover-lift" @click="router.push('/apis')">
              <span class="quick-icon">🔗</span>
              <span class="quick-text">接口管理</span>
            </div>
            <div class="quick-item hover-lift" @click="router.push('/test-cases')">
              <span class="quick-icon">📋</span>
              <span class="quick-text">测试用例</span>
            </div>
            <div class="quick-item hover-lift" @click="router.push('/test-suites')">
              <span class="quick-icon">📚</span>
              <span class="quick-text">测试套件</span>
            </div>
            <div class="quick-item hover-lift" @click="router.push('/scheduled-tasks')">
              <span class="quick-icon">📅</span>
              <span class="quick-text">定时任务</span>
            </div>
            <div class="quick-item hover-lift" @click="router.push('/executions')">
              <span class="quick-icon">▶️</span>
              <span class="quick-text">执行记录</span>
            </div>
            <div class="quick-item hover-lift" @click="router.push('/reports')">
              <span class="quick-icon">📊</span>
              <span class="quick-text">测试报告</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 执行动态 -->
    <div class="activity-section modern-card">
      <div class="card-header">
        <h3>最近执行动态</h3>
      </div>
      <div class="activity-list scrollbar-thin">
        <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
          <div class="activity-icon" :class="activity.type">
            {{ activity.icon }}
          </div>
          <div class="activity-content">
            <div class="activity-text">{{ activity.text }}</div>
            <div class="activity-time">{{ activity.time }}</div>
          </div>
        </div>
        <el-empty v-if="!recentActivities.length" description="暂无执行动态" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { projectApi } from '@/api/modules/project/project'
import { getStatusType, getStatusText } from '@/utils/formatters'

const router = useRouter()

const stats = ref([
  { title: '项目总数', value: 12, icon: '📁', color: '#409eff', change: '+2 本周', changeType: 'up' },
  { title: '接口总数', value: 156, icon: '🔗', color: '#67c23a', change: '+15 本周', changeType: 'up' },
  { title: '用例总数', value: 892, icon: '📋', color: '#e6a23c', change: '+32 本周', changeType: 'up' },
  { title: '总执行次数', value: 3420, icon: '▶️', color: '#f56c6c', change: '+128 本周', changeType: 'up' }
])

const recentProjects = ref<any[]>([])

const recentActivities = ref([
  { id: 1, type: 'success', icon: '✅', text: '接口用例「用户登录」执行成功', time: '5分钟前' },
  { id: 2, type: 'success', icon: '✅', text: '测试套件「登录模块」执行成功', time: '15分钟前' },
  { id: 3, type: 'warning', icon: '⚠️', text: '定时任务「每日巡检」执行失败', time: '30分钟前' },
  { id: 4, type: 'success', icon: '✅', text: '项目「电商API」创建成功', time: '1小时前' },
  { id: 5, type: 'info', icon: 'ℹ️', text: '新增测试用例「订单查询」', time: '2小时前' }
])

function getStatusType(status: string) {
  const map: Record<string, string> = {
    NOT_STARTED: 'info',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success',
    ARCHIVED: 'warning'
  }
  return map[status] || 'info'
}

function getStatusText(status: string) {
  const map: Record<string, string> = {
    NOT_STARTED: '未开始',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    ARCHIVED: '已归档'
  }
  return map[status] || status
}

onMounted(async () => {
  try {
    const data = await projectApi.getRecent(5)
    if (data && Array.isArray(data)) {
      recentProjects.value = data
    }
  } catch (error) {
    console.error('获取最近项目失败:', error)
  }
})
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 欢迎区域 */
.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  border: none;

  .welcome-title {
    font-size: 28px;
    font-weight: 600;
    color: #fff;
    margin: 0 0 8px 0;
  }

  .welcome-subtitle {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
    margin: 0;
  }

  .welcome-actions {
    display: flex;
    gap: 12px;

    .el-button {
      border-radius: 12px;
      padding: 12px 24px;
      font-weight: 500;
    }

    .el-button:first-child {
      background: rgba(255, 255, 255, 0.9);
      color: #1890ff;
      border: none;

      &:hover {
        background: #fff;
      }
    }

    .el-button:last-child {
      background: rgba(255, 255, 255, 0.2);
      color: #fff;
      border: 1px solid rgba(255, 255, 255, 0.3);

      &:hover {
        background: rgba(255, 255, 255, 0.3);
      }
    }
  }
}

/* 指标卡片 */
.metrics-section {
  margin-bottom: 20px;
}

.metric-card {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;

  .metric-icon {
    width: 56px;
    height: 56px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    flex-shrink: 0;
  }

  .metric-info {
    flex: 1;
    min-width: 0;
  }

  .metric-value {
    font-size: 28px;
    font-weight: 700;
    color: #303133;
    line-height: 1.2;
  }

  .metric-title {
    font-size: 14px;
    color: #909399;
    margin-top: 4px;
  }

  .metric-change {
    font-size: 12px;
    margin-top: 4px;

    &.up { color: #67c23a; }
    &.down { color: #f56c6c; }
    &.neutral { color: #909399; }
  }
}

/* 主内容区域 */
.main-content-section {
  margin-bottom: 20px;
}

.content-card,
.quick-card {
  padding: 20px;
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin: 0;
  }
}

/* 现代化表格 */
.modern-table {
  :deep(.el-table__header th) {
    background: #f5f7fa;
    color: #606266;
    font-weight: 600;
  }

  :deep(.el-table__row) {
    transition: background 0.2s;
  }

  :deep(.el-table__row:hover) {
    background: #f5f7fa;
  }
}

/* 快捷入口 */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 12px;
  background: rgba(24, 144, 255, 0.04);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: rgba(24, 144, 255, 0.08);
    transform: translateY(-2px);
  }

  .quick-icon {
    font-size: 24px;
  }

  .quick-text {
    font-size: 13px;
    color: #606266;
    text-align: center;
  }
}

/* 执行动态 */
.activity-section {
  padding: 20px;
}

.activity-list {
  max-height: 300px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid rgba(228, 231, 237, 0.5);

  &:last-child {
    border-bottom: none;
  }
}

.activity-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;

  &.success { background: rgba(103, 194, 58, 0.1); }
  &.warning { background: rgba(230, 162, 60, 0.1); }
  &.error { background: rgba(245, 108, 108, 0.1); }
  &.info { background: rgba(64, 158, 255, 0.1); }
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-text {
  font-size: 14px;
  color: #303133;
  line-height: 1.4;
}

.activity-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 渐变按钮 */
.btn-gradient-primary {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%) !important;
  border: none !important;
  color: #fff !important;
}

/* 响应式 */
@media (max-width: 1200px) {
  .main-content-section {
    .el-col-16 {
      span: 24;
    }
    .el-col-8 {
      span: 24;
    }
  }
}

@media (max-width: 768px) {
  .welcome-section {
    flex-direction: column;
    text-align: center;
    gap: 16px;

    .welcome-actions {
      flex-direction: column;
      width: 100%;
    }
  }

  .metrics-section {
    .el-col {
      span: 12;
    }
  }

  .quick-actions {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
