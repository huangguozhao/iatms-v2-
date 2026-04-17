<template>
  <div v-if="visible" class="tab-content cases-content">
    <div class="cases-card">
      <!-- 工具栏 -->
      <div class="cases-toolbar">
        <div class="toolbar-left">
          <el-select v-model="filter.type" placeholder="测试类型" size="small" clearable>
            <el-option v-for="t in testTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
          <el-select v-model="filter.priority" placeholder="优先级" size="small" clearable>
            <el-option v-for="p in priorityOptions" :key="p.value" :label="p.label" :value="p.value" />
          </el-select>
          <el-select v-model="filter.sortBy" size="small">
            <el-option v-for="s in sortOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </div>
        <div class="toolbar-right">
          <el-button type="primary" size="small" @click="showAddDialog">
            + 添加测试用例
          </el-button>
          <el-input v-model="searchText" placeholder="搜索..." size="small" style="width: 180px" clearable />
        </div>
      </div>

      <!-- 用例表格 -->
      <el-table :data="filteredCases" @row-click="handleRowClick">
        <el-table-column label="用例名称" min-width="180">
          <template #default="{ row }">
            <div class="case-name-cell">
              <el-icon class="case-icon"><Document /></el-icon>
              <span class="case-link" @click.stop="$emit('select-case', row)">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getTestTypeTagType(row.testType)" size="small">
              {{ getTestTypeText(row.testType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="优先级" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getPriorityTagType(row.priority)" size="small">
              {{ row.priority }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="测试数据" min-width="200">
          <template #default="{ row }">
            <el-tooltip :content="formatTestDataFull(row)" placement="top">
              <span class="data-preview">{{ truncateText(formatTestData(row), 40) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column label="预期结果" min-width="150">
          <template #default="{ row }">
            <el-tooltip :content="formatExpectedResultFull(row)" placement="top">
              <span class="data-preview">{{ truncateText(formatExpectedResult(row), 30) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click.stop="handleRun(row)">运行</el-button>
            <el-button size="small" text type="primary" @click.stop="$emit('select-case', row)">查看</el-button>
            <el-button size="small" text :type="row.isEnabled ? 'warning' : 'success'" @click.stop="toggleStatus(row)">
              {{ row.isEnabled ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination">
        <span class="pagination-info">显示 1-{{ Math.min(6, total) }} / {{ total }}</span>
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :total="total"
          layout="prev, pager, next"
        />
      </div>
    </div>

    <!-- 添加用例对话框 -->
    <TestCaseFormDialog v-model="addDialogVisible" @success="handleCaseCreated" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { ProjectTreeNode } from '@/api/modules/testing/testCase'
import { truncateText } from '@/utils/formatters'
import TestCaseFormDialog from './TestCaseFormDialog.vue'

const props = defineProps({
  visible: { type: Boolean, default: true },
  api: { type: Object as () => ProjectTreeNode | null, default: null },
  relatedCases: { type: Array as () => ProjectTreeNode[], default: () => [] }
})

const emit = defineEmits<{
  'select-case': [tc: ProjectTreeNode]
  'created': []
}>()

// 状态
const searchText = ref('')
const addDialogVisible = ref(false)

const filter = reactive({
  type: '',
  priority: '',
  sortBy: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10
})

// 选项
const testTypeOptions = [
  { label: '功能测试', value: 'functional' },
  { label: '性能测试', value: 'performance' },
  { label: '安全测试', value: 'security' },
  { label: '集成测试', value: 'integration' }
]

const priorityOptions = [
  { label: 'P0', value: 'P0' },
  { label: 'P1', value: 'P1' },
  { label: 'P2', value: 'P2' },
  { label: 'P3', value: 'P3' }
]

const sortOptions = [
  { label: '默认排序', value: '' },
  { label: '按名称', value: 'name' },
  { label: '按优先级', value: 'priority' },
  { label: '按创建时间', value: 'createdAt' }
]

// 计算
const filteredCases = computed(() => {
  let cases = props.relatedCases || []

  if (filter.priority) {
    cases = cases.filter((c: any) => c.priority === filter.priority)
  }
  if (searchText.value) {
    const search = searchText.value.toLowerCase()
    cases = cases.filter((c: any) => c.name?.toLowerCase().includes(search))
  }

  return cases
})

const total = computed(() => filteredCases.value.length)

// 方法
function getTestTypeTagType(type?: string): string {
  const map: Record<string, string> = {
    functional: '',
    performance: 'warning',
    security: 'danger',
    integration: 'success'
  }
  return map[type || ''] || 'info'
}

function getTestTypeText(type?: string): string {
  const map: Record<string, string> = {
    functional: '功能',
    performance: '性能',
    security: '安全',
    integration: '集成'
  }
  return map[type || ''] || type || '-'
}

function getPriorityTagType(priority?: string): string {
  const map: Record<string, string> = { P0: 'danger', P1: 'danger', P2: 'warning', P3: 'info' }
  return map[priority || ''] || 'warning'
}

function formatTestData(row: any): string {
  const data = row.requestBody || row.testData
  if (!data) return '-'
  return typeof data === 'object' ? JSON.stringify(data).slice(0, 50) : String(data).slice(0, 50)
}

function formatTestDataFull(row: any): string {
  const data = row.requestBody || row.testData
  if (!data) return '-'
  return typeof data === 'object' ? JSON.stringify(data, null, 2) : String(data)
}

function formatExpectedResult(row: any): string {
  const result = row.expectedResponse || row.expectedResult
  if (!result) return '-'
  return typeof result === 'object' ? JSON.stringify(result).slice(0, 40) : String(result).slice(0, 40)
}

function formatExpectedResultFull(row: any): string {
  const result = row.expectedResponse || row.expectedResult
  if (!result) return '-'
  return typeof result === 'object' ? JSON.stringify(result, null, 2) : String(result)
}

function handleRowClick(row: any) {
  // 可以选中行
}

function showAddDialog() {
  addDialogVisible.value = true
}

function handleCaseCreated() {
  emit('created')
}

function handleRun(row: any) {
  ElMessage.info('执行功能开发中')
}

function toggleStatus(row: any) {
  ElMessage.info('切换状态功能开发中')
}
</script>

<style scoped lang="scss">
.cases-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 6px 20px rgba(16, 24, 40, 0.06);
}

.cases-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.case-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.case-icon {
  color: #909399;
}

.case-link {
  color: #409eff;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

.data-preview {
  font-size: 13px;
  color: #606266;
  cursor: default;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}

.pagination-info {
  font-size: 13px;
  color: #909399;
}
</style>
