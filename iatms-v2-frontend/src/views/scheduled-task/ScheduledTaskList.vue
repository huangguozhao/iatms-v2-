<template>
  <div class="tasks-container">
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <span class="breadcrumb-item" @click="$router.push('/')">首页</span>
      <span class="breadcrumb-separator">></span>
      <span class="breadcrumb-item">用例管理</span>
      <span class="breadcrumb-separator">></span>
      <span class="breadcrumb-item active">任务安排</span>
    </div>

    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2 class="page-title">任务安排</h2>
      <el-button type="primary" @click="$router.push('/scheduled-tasks/create')">
        创建定时任务
      </el-button>
    </div>

    <!-- 筛选区域 -->
    <TaskFilters v-model="filters" @change="handleFilterChange" />

    <!-- 主内容区域 -->
    <div class="main-content">
      <div class="task-list-container">
        <!-- 表格 -->
        <el-table
          :data="tasks"
          v-loading="loading"
          class="task-table"
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" />
          
          <el-table-column prop="name" label="任务名称" min-width="180">
            <template #default="{ row }">
              <router-link :to="`/scheduled-tasks/${row.id}`" class="task-link">
                <div class="task-name-cell">
                  <span class="task-name">{{ row.name }}</span>
                  <span v-if="row.targetName" class="task-target">{{ row.targetName }}</span>
                </div>
              </router-link>
            </template>
          </el-table-column>

          <el-table-column prop="taskType" label="任务类型" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="getTaskTypeTagType(row.taskType)">
                {{ getTaskTypeText(row.taskType) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="triggerType" label="执行频率" width="100">
            <template #default="{ row }">
              {{ getFrequencyText(row.triggerType) }}
            </template>
          </el-table-column>

          <el-table-column prop="lastRunTime" label="最近执行" width="160">
            <template #default="{ row }">
              <div class="execution-time-cell">
                <span>{{ row.lastRunTime ? formatTime(row.lastRunTime) : '-' }}</span>
                <el-tag v-if="row.lastExecutionStatus" size="small" :type="getExecutionStatusType(row.lastExecutionStatus)">
                  {{ getExecutionStatusText(row.lastExecutionStatus) }}
                </el-tag>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="nextRunTime" label="下次执行" width="160">
            <template #default="{ row }">
              {{ row.nextRunTime ? formatTime(row.nextRunTime) : '-' }}
            </template>
          </el-table-column>

          <el-table-column prop="totalRuns" label="执行次数" width="90">
            <template #default="{ row }">
              <div class="execution-count-cell">
                <span class="success-count">{{ row.successRuns || 0 }}</span> /
                <span class="total-count">{{ row.totalRuns || 0 }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="executionEnvironment" label="执行环境" width="90">
            <template #default="{ row }">
              <el-tag v-if="row.executionEnvironment" size="small" type="info">
                {{ row.executionEnvironment }}
              </el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>

          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag
                :class="getStatusClass(row.status)"
                effect="dark"
                size="small"
              >
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-tooltip :content="getToggleStatusText(row.status)" placement="top">
                  <el-button
                    circle
                    size="small"
                    :type="getToggleStatusBtnType(row.status)"
                    @click.stop="handleToggleStatus(row)"
                  >
                    <el-icon><VideoPause v-if="isEnabled(row.status)" /><VideoPlay v-else /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="立即执行" placement="top">
                  <el-button
                    circle
                    size="small"
                    type="primary"
                    @click.stop="handleExecute(row)"
                  >
                    <el-icon><Refresh /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="编辑" placement="top">
                  <el-button
                    circle
                    size="small"
                    @click.stop="handleEdit(row)"
                  >
                    <el-icon><Edit /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="删除" placement="top">
                  <el-button
                    circle
                    size="small"
                    type="danger"
                    @click.stop="handleDelete(row)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <div class="pagination-info">
            共 {{ pagination.total }} 条记录，当前显示 {{ getStartIndex() }}-{{ getEndIndex() }} 条
          </div>
          <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="sizes, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Edit,
  Delete,
  VideoPlay,
  VideoPause,
  Refresh
} from '@element-plus/icons-vue'
import TaskFilters from '@/components/scheduled-task/TaskFilters.vue'
import type { TaskFilters as TaskFiltersType } from '@/components/scheduled-task/TaskFilters.vue'
import { scheduledTaskApi } from '@/api/modules/scheduling/scheduledTask'
import type { ScheduledTaskSummaryVO } from '@/types/api'

const router = useRouter()
const loading = ref(false)

const filters = reactive<TaskFiltersType>({
  frequency: 'all',
  dateRange: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tasks = ref<ScheduledTaskSummaryVO[]>([])

function handleFilterChange() {
  pagination.pageNum = 1
  loadTasks()
}

function handleSizeChange() {
  pagination.pageNum = 1
  loadTasks()
}

function handlePageChange() {
  loadTasks()
}

function getStartIndex(): number {
  return (pagination.pageNum - 1) * pagination.pageSize + 1
}

function getEndIndex(): number {
  return Math.min(pagination.pageNum * pagination.pageSize, pagination.total)
}

async function loadTasks() {
  loading.value = true
  try {
    const params = {
      keyword: '',
      status: filters.status === 'enabled' ? 'ACTIVE' : (filters.status === 'disabled' ? 'PAUSED' : undefined),
      type: filters.frequency !== 'all' ? filters.frequency : undefined,
      projectId: filters.projectId,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await scheduledTaskApi.query(params)
    if (res) {
      tasks.value = res.records || []
      pagination.total = res.total || 0
    } else {
      tasks.value = []
      pagination.total = 0
    }
  } catch {
    ElMessage.error('加载任务失败')
    tasks.value = []
  } finally {
    loading.value = false
  }
}

function isEnabled(status: string): boolean {
  return status === 'ACTIVE' || status === 'active' || status === 'ENABLED' || status === 'enabled'
}

function getStatusText(status: string): string {
  return isEnabled(status) ? '启用' : '禁用'
}

function getStatusClass(status: string): string {
  return isEnabled(status) ? 'status-enabled' : 'status-disabled'
}

function getToggleStatusText(status: string): string {
  return isEnabled(status) ? '禁用' : '启用'
}

function getToggleStatusBtnType(status: string): string {
  return isEnabled(status) ? 'warning' : 'success'
}

function getTaskTypeText(type: string): string {
  if (!type) return '-'
  const upperType = type.toUpperCase().replace(/-/g, '_')
  const map: Record<string, string> = {
    'SINGLE_CASE': '单个用例',
    'MODULE': '模块',
    'PROJECT': '项目',
    'TEST_SUITE': '测试套件',
    'API': 'API'
  }
  return map[upperType] || type
}

function getTaskTypeTagType(type: string): string {
  if (!type) return ''
  const upperType = type.toUpperCase().replace(/-/g, '_')
  const map: Record<string, string> = {
    'SINGLE_CASE': '',
    'MODULE': 'success',
    'PROJECT': 'warning',
    'TEST_SUITE': 'danger',
    'API': 'info'
  }
  return map[upperType] || ''
}

function getFrequencyText(frequency: string): string {
  if (!frequency) return '-'
  const upperFreq = frequency.toUpperCase()
  const map: Record<string, string> = {
    'DAILY': '每日执行',
    'WEEKLY': '每周执行',
    'MONTHLY': '每月执行',
    'CRON': 'Cron表达式',
    'SIMPLE': '简单重复',
    'ONCE': '一次性执行'
  }
  return map[upperFreq] || frequency
}

function getExecutionStatusType(status: string): string {
  const map: Record<string, string> = {
    SUCCESS: 'success',
    FAILURE: 'danger',
    RUNNING: 'warning',
    SKIPPED: 'info'
  }
  return map[status] || 'info'
}

function getExecutionStatusText(status: string): string {
  const map: Record<string, string> = {
    SUCCESS: '成功',
    FAILURE: '失败',
    RUNNING: '运行中',
    SKIPPED: '已跳过'
  }
  return map[status] || status
}

function formatTime(time: string | null): string {
  if (!time) return '-'
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

async function handleToggleStatus(row: ScheduledTaskSummaryVO) {
  try {
    const action = isEnabled(row.status) ? '暂停' : '恢复'
    await ElMessageBox.confirm(`确定要${action}任务"${row.name}"吗？`, `${action}确认`, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    if (isEnabled(row.status)) {
      await scheduledTaskApi.pause(row.id)
      ElMessage.success('任务已暂停')
    } else {
      await scheduledTaskApi.resume(row.id)
      ElMessage.success('任务已恢复')
    }
    loadTasks()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败，请稍后重试')
    }
  }
}

async function handleExecute(row: ScheduledTaskSummaryVO) {
  try {
    await ElMessageBox.confirm(`确定要立即执行任务"${row.name}"吗？`, '执行确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await scheduledTaskApi.execute(row.id)
    ElMessage.success('任务已提交执行')
    loadTasks()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('执行失败，请稍后重试')
    }
  }
}

function handleEdit(row: ScheduledTaskSummaryVO) {
  router.push(`/scheduled-tasks/${row.id}/edit`)
}

async function handleDelete(row: ScheduledTaskSummaryVO) {
  try {
    await ElMessageBox.confirm(`确定要删除任务"${row.name}"吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await scheduledTaskApi.delete(row.id)
    ElMessage.success('删除成功')
    loadTasks()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadTasks()
})
</script>

<style scoped lang="scss">
.tasks-container {
  padding: 24px;
  background: linear-gradient(135deg, #f0f2f5 0%, #e8eaed 100%);
  min-height: 100%;
  position: relative;
}

.tasks-container::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.08) 0%, transparent 70%);
  pointer-events: none;
}

.tasks-container::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: -10%;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(103, 194, 58, 0.06) 0%, transparent 70%);
  pointer-events: none;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  font-size: 14px;
  position: relative;
  z-index: 1;
}

.breadcrumb-item {
  color: #8c92a4;
  cursor: pointer;
  transition: color 0.2s;

  &:hover:not(.active) {
    color: #1890ff;
  }

  &.active {
    color: #303133;
    font-weight: 500;
  }
}

.breadcrumb-separator {
  color: #c0c4cc;
  font-size: 12px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  position: relative;
  z-index: 1;
}

.page-title {
  font-size: 26px;
  font-weight: 600;
  background: linear-gradient(135deg, #303133 0%, #606266 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.main-content {
  position: relative;
  z-index: 1;
}

.task-list-container {
  flex: 1;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  overflow: hidden;
  box-shadow:
    0 4px 20px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  transition: all 0.3s ease;

  &:hover {
    box-shadow:
      0 8px 30px rgba(0, 0, 0, 0.1),
      inset 0 1px 0 rgba(255, 255, 255, 0.5);
  }
}

.task-table {
  font-size: 14px;
}

:deep(.el-table tr) {
  transition: all 0.2s ease;
}

:deep(.el-table tr:hover > td) {
  background: rgba(64, 158, 255, 0.04) !important;
}

:deep(.el-table th) {
  background: linear-gradient(135deg, #f8f9fb 0%, #f0f2f5 100%) !important;
  color: #606266;
  font-weight: 600;
  font-size: 13px;
  border-bottom: 2px solid #e4e7ed !important;
}

:deep(.el-table td) {
  padding: 14px 0;
  border-bottom: 1px solid #f0f2f5 !important;
}

:deep(.el-table .el-table__row:last-child td) {
  border-bottom: none !important;
}

.task-link {
  color: inherit;
  text-decoration: none;

  &:hover .task-name {
    color: #1890ff;
  }
}

.task-name-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.task-name {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
  transition: color 0.2s;
}

.task-target {
  font-size: 12px;
  color: #8c92a4;
  display: flex;
  align-items: center;
  gap: 4px;

  &::before {
    content: '';
    width: 6px;
    height: 6px;
    background: linear-gradient(135deg, #1890ff, #67c23a);
    border-radius: 50%;
  }
}

.execution-time-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;

  > span {
    color: #606266;
    font-size: 13px;
  }
}

.execution-count-cell {
  font-size: 13px;
  font-weight: 500;
}

.success-count {
  color: #67c23a;
  font-weight: 600;
}

.total-count {
  color: #909399;
}

.status-enabled {
  background: linear-gradient(135deg, #67c23a, #85ce61) !important;
  color: white !important;
  border: none !important;
  font-weight: 500;
}

.status-disabled {
  background: linear-gradient(135deg, #c0c4cc, #909399) !important;
  color: white !important;
  border: none !important;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 4px;
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: linear-gradient(135deg, #fafbfc 0%, #f5f7fa 100%);
  border-top: 1px solid #f0f2f5;
}

.pagination-info {
  font-size: 13px;
  color: #8c92a4;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.task-list-container {
  animation: fadeInUp 0.4s ease-out;
}

@media (max-width: 1200px) {
  .filter-left {
    gap: 12px;
  }

  .filter-item {
    width: 140px;
  }

  .date-picker {
    width: 240px;
  }
}

@media (max-width: 768px) {
  .tasks-container {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .filter-left {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-item,
  .date-picker {
    width: 100%;
  }
}
</style>
