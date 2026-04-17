<template>
  <div v-if="visible" class="tab-content history-content">
    <div class="history-card">
      <!-- 筛选工具栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="filter.period" size="small" style="width: 110px">
            <el-option label="最近7天" value="7days" />
            <el-option label="最近30天" value="30days" />
            <el-option label="最近90天" value="90days" />
            <el-option label="全部" value="all" />
          </el-select>
          <el-select v-model="filter.status" size="small" style="width: 100px" clearable>
            <el-option label="全部" value="" />
            <el-option label="成功" value="passed" />
            <el-option label="失败" value="failed" />
          </el-select>
        </div>
        <div class="toolbar-right">
          <el-button size="small" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
          <el-input v-model="searchText" placeholder="搜索" size="small" style="width: 180px" clearable />
        </div>
      </div>

      <!-- 历史表格 -->
      <el-table :data="filteredRecords" v-loading="loading" stripe>
        <el-table-column label="测试时间" width="170" prop="testTime" />

        <el-table-column label="执行人" width="140">
          <template #default="{ row }">
            <div class="executor-cell">
              <el-avatar :size="24" class="avatar">{{ row.executor?.charAt(0) || '?' }}</el-avatar>
              <span>{{ row.executor }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.executionType === 'manual' ? 'primary' : 'info'" size="small">
              {{ row.executionType === 'manual' ? '手动' : '自动' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="环境" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getEnvTagType(row.environment)" size="small">
              {{ getEnvText(row.environment) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="响应时间" width="100" align="center" prop="responseTime" />

        <el-table-column label="结果" width="90" align="center">
          <template #default="{ row }">
            <div class="result-cell">
              <el-icon :color="row.status === 'passed' ? '#67c23a' : '#f56c6c'" :size="16">
                <CircleCheckFilled v-if="row.status === 'passed'" />
                <CircleCloseFilled v-else />
              </el-icon>
              <span :class="row.status">{{ row.status === 'passed' ? '通过' : '失败' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="统计" width="180" align="center">
          <template #default="{ row }">
            <span class="stat">总计: {{ row.totalCases }}</span>
            <span class="stat success">通过: {{ row.passedCases }}</span>
            <span class="stat danger">失败: {{ row.failedCases }}</span>
          </template>
        </el-table-column>

        <el-table-column label="成功率" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getRateTagType(row.successRate)" size="small">
              {{ (row.successRate * 100).toFixed(0) }}%
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button size="small" text type="success" @click="handleRetest(row)">重测</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <span class="pagination-info">{{ paginationInfo }}</span>
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="prev, pager, next"
        />
      </div>

      <!-- 详情对话框 -->
      <el-dialog v-model="detailVisible" title="执行详情" width="700px">
        <div v-if="currentRecord" class="detail-content">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="执行ID">{{ currentRecord.recordId }}</el-descriptions-item>
            <el-descriptions-item label="执行人">{{ currentRecord.executorInfo?.name || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="环境">{{ currentRecord.environment }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="currentRecord.status === 'completed' ? 'success' : 'danger'" size="small">
                {{ currentRecord.status }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="开始时间">{{ formatDateTime(currentRecord.startTime) }}</el-descriptions-item>
            <el-descriptions-item label="结束时间">{{ formatDateTime(currentRecord.endTime) }}</el-descriptions-item>
          </el-descriptions>

          <div v-if="currentRecord.totalCases" class="stats-section">
            <h4>执行统计</h4>
            <el-descriptions :column="3" border>
              <el-descriptions-item label="总用例数">{{ currentRecord.totalCases }}</el-descriptions-item>
              <el-descriptions-item label="通过数">
                <span class="text-success">{{ currentRecord.passedCases }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="失败数">
                <span class="text-danger">{{ currentRecord.failedCases }}</span>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { Download } from '@element-plus/icons-vue'
import { CircleCheckFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { formatDateTime } from '@/utils/formatters'
import type { ProjectTreeNode } from '@/api/modules/testing/testCase'

const props = defineProps({
  visible: { type: Boolean, default: true },
  api: { type: Object as () => ProjectTreeNode | null, default: null }
})

// 状态
const loading = ref(false)
const searchText = ref('')
const detailVisible = ref(false)
const currentRecord = ref<any>(null)

const filter = reactive({
  period: '7days',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10
})

// TODO: 从API获取真实数据
const records = ref<any[]>([])

const filteredRecords = computed(() => {
  let result = records.value

  if (filter.status) {
    result = result.filter(r => r.status === filter.status)
  }
  if (searchText.value) {
    const search = searchText.value.toLowerCase()
    result = result.filter(r =>
      r.recordId?.toLowerCase().includes(search) ||
      r.executor?.toLowerCase().includes(search)
    )
  }

  return result
})

const total = computed(() => filteredRecords.value.length)

const paginationInfo = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize + 1
  const end = Math.min(pagination.currentPage * pagination.pageSize, total.value)
  return `${start}-${end} / ${total.value}`
})

// 方法
function getEnvTagType(env?: string): string {
  const map: Record<string, string> = { dev: '', test: 'success', staging: 'warning', prod: 'danger' }
  return map[env || ''] || 'info'
}

function getEnvText(env?: string): string {
  const map: Record<string, string> = { dev: '开发', test: '测试', staging: '预发', prod: '生产' }
  return map[env || ''] || env || '-'
}

function getRateTagType(rate: number): string {
  if (rate >= 0.9) return 'success'
  if (rate >= 0.6) return 'warning'
  return 'danger'
}

function viewDetail(row: any) {
  currentRecord.value = row
  detailVisible.value = true
}

function handleRetest(row: any) {
  ElMessage.info('重新测试功能开发中')
}

function handleExport() {
  ElMessage.info('导出功能开发中')
}
</script>

<style scoped lang="scss">
.history-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 6px 20px rgba(16, 24, 40, 0.06);
}

.toolbar {
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

.executor-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar {
  background: #409eff;
  color: white;
  font-size: 12px;
}

.result-cell {
  display: flex;
  align-items: center;
  gap: 4px;

  .passed { color: #67c23a; }
  .failed { color: #f56c6c; }
}

.stat {
  font-size: 12px;
  color: #606266;
  margin-right: 8px;

  &.success { color: #67c23a; }
  &.danger { color: #f56c6c; }
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

.detail-content {
  max-height: 60vh;
  overflow-y: auto;
}

.stats-section {
  margin-top: 20px;

  h4 {
    margin: 0 0 12px 0;
    font-size: 14px;
    color: #303133;
  }
}

.text-success {
  color: #67c23a;
  font-weight: 600;
}

.text-danger {
  color: #f56c6c;
  font-weight: 600;
}
</style>
