<template>
  <div class="project-module-stats">
    <!-- 头部 -->
    <div class="stats-header">
      <div class="header-left">
        <div class="level-icon">
          <el-icon :size="32">
            <FolderOpened v-if="level === 'project'" />
            <Folder v-else />
          </el-icon>
        </div>
        <h2 class="level-title">{{ node?.name || '未命名' }}</h2>
        <el-tag :type="level === 'project' ? 'primary' : 'success'" size="large" effect="light">
          {{ level === 'project' ? '项目' : '模块' }}
        </el-tag>
      </div>
      <div class="header-actions">
        <el-button
          type="success"
          size="default"
          :icon="VideoPlay"
          @click="handleExecuteTest"
        >
          执行测试
        </el-button>
        <el-button
          v-if="level === 'project'"
          size="default"
          :icon="Setting"
          @click="handleConfigEnvironment"
        >
          环境配置
        </el-button>
        <el-button
          size="default"
          :icon="Edit"
          @click="handleEdit"
        >
          编辑
        </el-button>
        <el-button
          size="default"
          type="danger"
          :icon="Delete"
          @click="handleDelete"
        >
          删除
        </el-button>
      </div>
    </div>

    <!-- 描述 -->
    <el-card v-if="node?.description" class="description-card" shadow="hover">
      <template #header>
        <div class="section-header">
          <el-icon><Document /></el-icon>
          <span>描述</span>
        </div>
      </template>
      <p class="description-text">{{ node.description }}</p>
    </el-card>

    <!-- 统计卡片 -->
    <div class="stats-cards" v-loading="statsLoading" element-loading-text="加载中...">
      <div class="stat-card">
        <div class="stat-icon-wrapper module-icon">
          <el-icon :size="24"><Folder /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">{{ level === 'project' ? '模块数' : '接口数' }}</div>
          <div class="stat-value">{{ displayStats.moduleCount }}</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon-wrapper case-icon">
          <el-icon :size="24"><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">用例总数</div>
          <div class="stat-value">{{ displayStats.testCaseCount }}</div>
        </div>
      </div>

      <div class="stat-card success">
        <div class="stat-icon-wrapper passed-icon">
          <el-icon :size="24"><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">通过</div>
          <div class="stat-value passed">{{ displayStats.passedCount }}</div>
        </div>
      </div>

      <div class="stat-card error">
        <div class="stat-icon-wrapper failed-icon">
          <el-icon :size="24"><CircleClose /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">失败</div>
          <div class="stat-value failed">{{ displayStats.failedCount }}</div>
        </div>
      </div>

      <div class="stat-card warning">
        <div class="stat-icon-wrapper pending-icon">
          <el-icon :size="24"><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">未执行</div>
          <div class="stat-value pending">{{ displayStats.notExecutedCount }}</div>
        </div>
      </div>
    </div>

    <!-- 子项列表 -->
    <el-card class="children-card" shadow="hover">
      <template #header>
        <div class="section-header">
          <span>{{ level === 'project' ? '模块列表' : '接口列表' }}</span>
          <el-button
            type="primary"
            size="small"
            :icon="Plus"
            @click="handleAdd"
          >
            新建{{ level === 'project' ? '模块' : '接口' }}
          </el-button>
        </div>
      </template>

      <div class="list-table">
        <div class="table-header">
          <div class="col col-name">名称</div>
          <div class="col col-count" v-if="level === 'project'">接口数</div>
          <div class="col col-count">用例数</div>
          <div class="col col-status">通过率</div>
          <div class="col col-time">最后执行</div>
          <div class="col col-actions">操作</div>
        </div>

        <div class="table-body" v-if="children.length > 0">
          <div
            v-for="child in children"
            :key="child.id"
            class="table-row"
            @click="handleSelectChild(child)"
          >
            <div class="col col-name">
              <el-icon class="child-icon" :size="16">
                <Link v-if="level === 'module'" />
                <Folder v-else />
              </el-icon>
              <span class="child-name">{{ child.name }}</span>
            </div>
            <div class="col col-count" v-if="level === 'project'">
              {{ getChildApiCount(child) }}
            </div>
            <div class="col col-count">
              {{ getChildCaseCount(child) }}
            </div>
            <div class="col col-status">
              <div class="progress-bar">
                <div
                  class="progress-fill"
                  :style="{ width: getPassRate(child) + '%' }"
                  :class="getPassRateClass(child)"
                ></div>
              </div>
              <span class="progress-text">{{ getPassRate(child) }}%</span>
            </div>
            <div class="col col-time">
              {{ getLastExecutedTime(child) }}
            </div>
            <div class="col col-actions" @click.stop>
              <el-button size="small" text type="primary" @click="$emit('edit-child', child)">
                编辑
              </el-button>
              <el-button size="small" text type="danger" @click="$emit('delete-child', child)">
                删除
              </el-button>
            </div>
          </div>
        </div>

        <el-empty v-else description="暂无数据" :image-size="60" />
      </div>
    </el-card>

    <!-- 执行配置对话框 -->
    <ExecuteConfigDialog
      v-model="executeDialogVisible"
      :target-type="level"
      :target-id="node?.id"
      :target-name="node?.name || ''"
      :case-count="displayStats.testCaseCount"
      :project-id="getProjectId()"
      @execute="handleConfirmExecute"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import {
  Folder,
  FolderOpened,
  Document,
  VideoPlay,
  Setting,
  Edit,
  Delete,
  Plus,
  Link,
  CircleCheck,
  CircleClose,
  Warning
} from '@element-plus/icons-vue'
import ExecuteConfigDialog from '@/components/business/case-detail/ExecuteConfigDialog.vue'
import { projectApi } from '@/api/modules/project/project'

interface TreeNode {
  id: number
  name: string
  description?: string
  type?: string
  modules?: TreeNode[]
  apis?: any[]
  cases?: any[]
  statistics?: {
    moduleCount?: number
    apiCount?: number
    testCaseCount?: number
    passedCount?: number
    failedCount?: number
    notExecutedCount?: number
  }
  stats?: {
    moduleCount?: number
    apiCount?: number
    testCaseCount?: number
  }
}

interface Props {
  node: TreeNode | null
  level: 'project' | 'module'
}

const props = defineProps<Props>()

const emit = defineEmits<{
  edit: [node: TreeNode]
  delete: [node: TreeNode]
  add: [node: TreeNode]
  'edit-child': [child: TreeNode]
  'delete-child': [child: TreeNode]
  'select-child': [child: TreeNode]
  'config-environment': [node: TreeNode]
  execute: [config: any]
}>()

// 执行配置对话框
const executeDialogVisible = ref(false)

// 统计数据加载状态
const statsLoading = ref(false)

// 项目详情数据
const projectDetail = ref<any>(null)

// 模块详情数据
const moduleDetail = ref<any>(null)

// 获取项目ID
function getProjectId(): number | null {
  if (!props.node) return null
  return (props.node as any).projectId || (props.node as any).project_id || props.node.id
}

// 获取模块ID
function getModuleId(): number | null {
  if (!props.node) return null
  return (props.node as any).moduleId || (props.node as any).module_id || props.node.id
}

// 统计信息
interface Stats {
  moduleCount: number
  testCaseCount: number
  passedCount: number
  failedCount: number
  notExecutedCount: number
}

// 显示用的统计数据
const displayStats = computed<Stats>(() => {
  if (!props.node) {
    return { moduleCount: 0, testCaseCount: 0, passedCount: 0, failedCount: 0, notExecutedCount: 0 }
  }

  // 如果有从后端获取的项目详情数据，优先使用
  if (projectDetail.value && props.level === 'project') {
    return {
      moduleCount: projectDetail.value.totalModules || 0,
      testCaseCount: projectDetail.value.totalTestCases || 0,
      passedCount: projectDetail.value.passedCount || 0,
      failedCount: projectDetail.value.failedCount || 0,
      notExecutedCount: projectDetail.value.notExecutedCount || 0
    }
  }

  // 如果有从后端获取的模块详情数据，优先使用
  if (moduleDetail.value && props.level === 'module') {
    return {
      moduleCount: moduleDetail.value.stats?.apiCount || 0,
      testCaseCount: moduleDetail.value.stats?.testCaseCount || 0,
      passedCount: moduleDetail.value.stats?.passedCount || 0,
      failedCount: moduleDetail.value.stats?.failedCount || 0,
      notExecutedCount: moduleDetail.value.stats?.notExecutedCount || 0
    }
  }

  // 如果有内联的统计数据，优先使用
  if (props.node.stats) {
    if (props.level === 'project') {
      return {
        moduleCount: props.node.stats.moduleCount || 0,
        testCaseCount: props.node.stats.testCaseCount || 0,
        passedCount: 0,
        failedCount: 0,
        notExecutedCount: 0
      }
    } else {
      return {
        moduleCount: props.node.stats.apiCount || 0,
        testCaseCount: props.node.stats.testCaseCount || 0,
        passedCount: 0,
        failedCount: 0,
        notExecutedCount: 0
      }
    }
  }

  // 否则从本地计算
  if (props.level === 'project') {
    return {
      moduleCount: getChildCount(),
      testCaseCount: getTotalCases(),
      passedCount: getPassedCount(),
      failedCount: getFailedCount(),
      notExecutedCount: getNotExecutedCount()
    }
  } else {
    return {
      moduleCount: getChildCount(),
      testCaseCount: getTotalCases(),
      passedCount: getPassedCount(),
      failedCount: getFailedCount(),
      notExecutedCount: getNotExecutedCount()
    }
  }
})

// 加载项目详情
async function loadProjectDetail() {
  if (!props.node) return

  statsLoading.value = true
  try {
    if (props.level === 'project') {
      const id = getProjectId()
      console.log('[DEBUG] Loading project detail, id:', id)
      if (id) {
        projectDetail.value = await projectApi.getDetail(id)
        console.log('[DEBUG] projectDetail loaded:', projectDetail.value)
      }
    } else if (props.level === 'module') {
      const id = getModuleId()
      console.log('[DEBUG] Loading module detail, id:', id, 'level:', props.level)
      if (id) {
        moduleDetail.value = await projectApi.getModuleDetail(id)
        console.log('[DEBUG] moduleDetail loaded:', moduleDetail.value)
      } else {
        console.log('[DEBUG] module id is null, node:', props.node)
      }
    }
  } catch (error) {
    console.error('加载详情失败:', error)
  } finally {
    statsLoading.value = false
  }
}

// 监听节点变化，重新加载数据
watch(() => props.node?.id, (newId, oldId) => {
  console.log('[DEBUG] watch triggered: node.id changed from', oldId, 'to', newId, 'level:', props.level)
  projectDetail.value = null
  moduleDetail.value = null
  loadProjectDetail()
}, { immediate: true })

// 调试用：监听 moduleDetail 变化
watch(() => moduleDetail.value, (newVal) => {
  console.log('[DEBUG] moduleDetail changed:', newVal)
  console.log('[DEBUG] moduleDetail.stats:', newVal?.stats)
}, { deep: true })

// 打开执行配置对话框
function handleExecuteTest() {
  executeDialogVisible.value = true
}

// 确认执行
function handleConfirmExecute(config: any) {
  executeDialogVisible.value = false
  emit('execute', config)
}

// 删除确认
function handleDelete() {
  if (!props.node) return

  ElMessageBox.confirm(
    `确定删除 ${props.level === 'project' ? '项目' : '模块'} "${props.node.name}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      emit('delete', props.node!)
    })
    .catch(() => {})
}

// 编辑
function handleEdit() {
  if (!props.node) return
  emit('edit', props.node)
}

// 环境配置
function handleConfigEnvironment() {
  if (!props.node) return
  emit('config-environment', props.node)
}

// 添加子节点
function handleAdd() {
  if (!props.node) return
  emit('add', props.node)
}

// 子项列表
const children = computed(() => {
  console.log('[DEBUG] children computed, node:', props.node?.id, 'level:', props.level)
  if (!props.node) return []
  if (props.level === 'project') {
    // 项目下的模块在 children 字段中
    console.log('[DEBUG] project children:', props.node.children)
    return props.node.children || []
  } else {
    // 模块下的接口在 apis 字段中
    return props.node.apis || []
  }
})

// 获取子项数量
function getChildCount(): number {
  if (!props.node) return 0

  // 优先使用内联统计数据
  if (props.node.statistics) {
    if (props.level === 'project' && props.node.statistics.moduleCount !== undefined) {
      return props.node.statistics.moduleCount
    }
    if (props.level === 'module' && props.node.statistics.apiCount !== undefined) {
      return props.node.statistics.apiCount
    }
  }

  return children.value.length
}

// 获取用例总数
function getTotalCases(): number {
  if (!props.node) return 0

  // 优先使用内联统计数据
  if (props.node.statistics?.testCaseCount !== undefined) {
    return props.node.statistics.testCaseCount
  }

  // 本地计算
  let total = 0
  if (props.level === 'project') {
    props.node.modules?.forEach((module: any) => {
      module.apis?.forEach((api: any) => {
        total += api.cases?.length || 0
      })
    })
  } else {
    props.node.apis?.forEach((api: any) => {
      total += api.cases?.length || 0
    })
  }
  return total
}

// 获取通过数量
function getPassedCount(): number {
  if (!props.node) return 0

  if (props.node.statistics?.passedCount !== undefined) {
    return props.node.statistics.passedCount
  }

  let count = 0
  if (props.level === 'project') {
    props.node.modules?.forEach((module: any) => {
      module.apis?.forEach((api: any) => {
        api.cases?.forEach((c: any) => {
          if (c.status === 'passed') count++
        })
      })
    })
  } else {
    props.node.apis?.forEach((api: any) => {
      api.cases?.forEach((c: any) => {
        if (c.status === 'passed') count++
      })
    })
  }
  return count
}

// 获取失败数量
function getFailedCount(): number {
  if (!props.node) return 0

  if (props.node.statistics?.failedCount !== undefined) {
    return props.node.statistics.failedCount
  }

  let count = 0
  if (props.level === 'project') {
    props.node.modules?.forEach((module: any) => {
      module.apis?.forEach((api: any) => {
        api.cases?.forEach((c: any) => {
          if (c.status === 'failed') count++
        })
      })
    })
  } else {
    props.node.apis?.forEach((api: any) => {
      api.cases?.forEach((c: any) => {
        if (c.status === 'failed') count++
      })
    })
  }
  return count
}

// 获取未执行数量
function getNotExecutedCount(): number {
  if (!props.node) return 0

  if (props.node.statistics?.notExecutedCount !== undefined) {
    return props.node.statistics.notExecutedCount
  }

  let count = 0
  if (props.level === 'project') {
    props.node.modules?.forEach((module: any) => {
      module.apis?.forEach((api: any) => {
        api.cases?.forEach((c: any) => {
          if (c.status === 'not_executed') count++
        })
      })
    })
  } else {
    props.node.apis?.forEach((api: any) => {
      api.cases?.forEach((c: any) => {
        if (c.status === 'not_executed') count++
      })
    })
  }
  return count
}

// 获取子项的接口数
function getChildApiCount(child: any): number {
  return child.apis?.length || 0
}

// 获取子项的用例数
function getChildCaseCount(child: any): number {
  if (props.level === 'project') {
    let total = 0
    child.apis?.forEach((api: any) => {
      total += api.testCases?.length || 0
    })
    return total
  }
  return child.testCases?.length || 0
}

// 获取通过率
function getPassRate(child: any): number {
  const cases = getAllCases(child)
  if (cases.length === 0) return 0
  const passed = cases.filter((c: any) => c.status === 'passed').length
  return Math.round((passed / cases.length) * 100)
}

// 获取通过率样式类
function getPassRateClass(child: any): string {
  const rate = getPassRate(child)
  if (rate >= 80) return 'rate-high'
  if (rate >= 60) return 'rate-medium'
  return 'rate-low'
}

// 获取所有用例
function getAllCases(child: any): any[] {
  const cases: any[] = []
  if (props.level === 'project') {
    child.apis?.forEach((api: any) => {
      cases.push(...(api.cases || []))
    })
  } else {
    cases.push(...(child.cases || []))
  }
  return cases
}

// 获取最后执行时间
function getLastExecutedTime(child: any): string {
  const cases = getAllCases(child)
  if (cases.length === 0) return '-'

  const times = cases
    .map((c: any) => c.lastExecutedTime || c.last_executed_time || c.executedAt || c.executed_at)
    .filter(Boolean)
    .sort()
    .reverse()

  if (!times[0]) return '-'

  const time = times[0]
  if (typeof time === 'string' && time.includes('T')) {
    return new Date(time).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    }).replace(/\//g, '-')
  }
  return time
}

// 选择子项
function handleSelectChild(child: any) {
  emit('select-child', child)
}
</script>

<style scoped lang="scss">
// 复用旧前端样式变量
$card-radius: 12px;
$card-shadow: 0 10px 30px rgba(16, 24, 40, 0.06);
$card-shadow-hover: 0 18px 40px rgba(16, 24, 40, 0.08);
$card-transition: transform 0.18s cubic-bezier(0.2, 0.8, 0.2, 1), box-shadow 0.18s cubic-bezier(0.2, 0.8, 0.2, 1);
$border-color: #e4e7ed;
$text-primary: #303133;
$text-secondary: #606266;
$text-placeholder: #c0c4cc;
$bg-light: #f5f7fa;
$bg-lighter: #fafafa;

.project-module-stats {
  padding: 24px;
  height: 100%;
  overflow-y: auto;
  background: white;
}

// 头部
.stats-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid $border-color;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.level-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: $card-radius;
  background: linear-gradient(135deg, #409eff, #67c23a);
  color: white;
}

.level-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: $text-primary;
}

.header-actions {
  display: flex;
  gap: 8px;
}

// 描述卡片
.description-card {
  margin-bottom: 24px;
  padding: 16px;
  background: $bg-light;
  border-radius: 4px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: $text-primary;
}

.description-text {
  margin: 0;
  color: $text-secondary;
  line-height: 1.6;
}

// 统计卡片
.stats-cards {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: $card-radius;
  box-shadow: $card-shadow;
  transition: $card-transition;

  &:hover {
    box-shadow: $card-shadow-hover;
    transform: translateY(-2px);
  }

  &.success {
    background: linear-gradient(135deg, #e6f4ff, #dcfce7);
    border: 1px solid #b3d8ff;
  }

  &.error {
    background: linear-gradient(135deg, #fef0f0, #fee2e2);
    border: 1px solid #fbc4c4;
  }

  &.warning {
    background: linear-gradient(135deg, #fdf6ec, #fef0e6);
    border: 1px solid #f5dab1;
  }
}

.stat-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: $card-radius;
  background: $bg-light;

  &.module-icon {
    background: #ecf5ff;
    color: #409eff;
  }

  &.case-icon {
    background: #f0f9ff;
    color: #0ea5e9;
  }

  &.passed-icon {
    background: #e6f4ff;
    color: #67c23a;
  }

  &.failed-icon {
    background: #fef0f0;
    color: #f56c6c;
  }

  &.pending-icon {
    background: #fdf6ec;
    color: #e6a23c;
  }
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: $text-placeholder;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: $text-primary;

  &.passed {
    color: #67c23a;
  }

  &.failed {
    color: #f56c6c;
  }

  &.pending {
    color: #e6a23c;
  }
}

// 子项列表卡片
.children-card {
  border-radius: $card-radius;
  overflow: hidden;
  border: 1px solid $border-color;

  :deep(.el-card__header) {
    padding: 12px 16px;
    background: $bg-lighter;
    border-bottom: 1px solid $border-color;
  }

  :deep(.el-card__body) {
    padding: 0;
  }
}

.list-table {
  border-radius: calc($card-radius - 8px);
  overflow: hidden;
}

.table-header {
  display: flex;
  background: $bg-lighter;
  border-bottom: 1px solid $border-color;
  padding: 12px 16px;
  font-size: 13px;
  font-weight: 600;
  color: $text-placeholder;
}

.table-body {
  background: white;
}

.table-row {
  display: flex;
  padding: 14px 16px;
  border-bottom: 1px solid $border-color;
  cursor: pointer;
  transition: background 0.2s ease;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: $bg-light;
  }
}

.col {
  display: flex;
  align-items: center;
  padding: 0 8px;
}

.col-name {
  flex: 2;
  gap: 8px;
}

.child-icon {
  color: $text-placeholder;
}

.child-name {
  color: $text-primary;
  font-size: 14px;
}

.col-count {
  flex: 0 0 80px;
  justify-content: center;
  font-size: 14px;
  color: $text-secondary;
}

.col-status {
  flex: 1;
  gap: 8px;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: $border-color;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  transition: width 0.3s ease;
  border-radius: 4px;

  &.rate-high {
    background: linear-gradient(to right, #67c23a, #95d475);
  }

  &.rate-medium {
    background: linear-gradient(to right, #e6a23c, #f0b775);
  }

  &.rate-low {
    background: linear-gradient(to right, #f56c6c, #f78989);
  }
}

.progress-text {
  font-size: 13px;
  color: $text-secondary;
  min-width: 40px;
}

.col-time {
  flex: 0 0 150px;
  font-size: 13px;
  color: $text-placeholder;
}

.col-actions {
  flex: 0 0 120px;
  justify-content: flex-end;
  gap: 4px;
}

// 响应式
@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .col-time {
    display: none;
  }
}
</style>
