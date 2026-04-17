<template>
  <div v-if="visible" class="tab-content result-content">
    <div class="result-card">
      <!-- 状态 Banner -->
      <div class="status-banner" :class="'status-' + testStatus">
        <el-icon :size="40" :color="statusColor">
          <CircleCloseFilled v-if="testStatus === 'failed'" />
          <CircleCheckFilled v-else-if="testStatus === 'passed'" />
          <InfoFilled v-else />
        </el-icon>
        <div class="status-info">
          <div class="status-title">{{ statusText }}</div>
          <div class="status-meta">
            <span>状态码：<strong>{{ actualResponse.statusCode }}</strong></span>
            <span>响应时间：<strong>{{ responseTime }}</strong></span>
            <span>测试时间：<strong>{{ testTime }}</strong></span>
          </div>
        </div>
      </div>

      <!-- 子标签 -->
      <div class="result-tabs">
        <div
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-item"
          :class="{ active: resultTab === tab.key }"
          @click="resultTab = tab.key"
        >
          {{ tab.label }}
        </div>
      </div>

      <!-- 响应体 -->
      <div v-if="resultTab === 'response'" class="tab-content">
        <div class="response-toolbar">
          <div class="toolbar-left">
            <el-button size="small" @click="copyResponse">复制</el-button>
            <el-button size="small" @click="formatResponseBody">格式化</el-button>
          </div>
          <el-input v-model="searchText" placeholder="搜索" size="small" style="width: 180px" clearable />
        </div>
        <div class="response-body">
          <pre v-html="highlightedResponse"></pre>
        </div>
      </div>

      <!-- 断言结果 -->
      <div v-if="resultTab === 'assertions'" class="tab-content">
        <el-table :data="assertionResults" border>
          <el-table-column label="断言项" min-width="180">
            <template #default="{ row }">
              <div class="assertion-field">
                <el-icon :color="row.passed ? '#67c23a' : '#f56c6c'" :size="16">
                  <CircleCheckFilled v-if="row.passed" />
                  <CircleCloseFilled v-else />
                </el-icon>
                <span>{{ row.field }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="预期值" width="150" prop="expected" />
          <el-table-column label="实际值" width="150" prop="actual" />
          <el-table-column label="结果" min-width="150">
            <template #default="{ row }">
              <span :class="row.passed ? 'text-success' : 'text-danger'">
                {{ row.passed ? '✓ 通过' : row.message }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 响应头 -->
      <div v-if="resultTab === 'headers'" class="tab-content">
        <el-table :data="responseHeaders" border>
          <el-table-column label="名称" width="250" prop="name" />
          <el-table-column label="值" min-width="400" prop="value" />
        </el-table>
      </div>

      <!-- 操作按钮 -->
      <div class="result-actions">
        <el-button type="success" @click="$emit('retest')">
          <el-icon><Refresh /></el-icon>
          重新测试
        </el-button>
        <el-button @click="exportReport">
          <el-icon><Download /></el-icon>
          导出报告
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { Refresh, Download } from '@element-plus/icons-vue'
import { CircleCheckFilled, CircleCloseFilled, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { formatDateTime } from '@/utils/formatters'
import type { ProjectTreeNode } from '@/api/modules/testing/testCase'

const props = defineProps({
  visible: { type: Boolean, default: true },
  api: { type: Object as () => ProjectTreeNode | null, default: null },
  executionResult: { type: Object, default: null }
})

defineEmits(['retest'])

const tabs = [
  { key: 'response', label: '响应体' },
  { key: 'assertions', label: '断言结果' },
  { key: 'headers', label: '响应头' }
]

const resultTab = ref('response')
const searchText = ref('')

const testStatus = ref<'passed' | 'failed' | 'not_executed'>('not_executed')
const responseTime = ref('-')
const testTime = ref('-')

const actualResponse = reactive({
  statusCode: '-',
  responseCode: '-',
  body: null as any
})

const assertionResults = ref<any[]>([])
const responseHeaders = ref<any[]>([])

const statusColor = computed(() => {
  return testStatus.value === 'passed' ? '#67c23a' : testStatus.value === 'failed' ? '#f56c6c' : '#909399'
})

const statusText = computed(() => {
  return testStatus.value === 'passed' ? '测试通过' : testStatus.value === 'failed' ? '测试失败' : '未执行'
})

const formattedResponse = computed(() => {
  if (!actualResponse.body) return '{}'
  if (typeof actualResponse.body === 'object') {
    return JSON.stringify(actualResponse.body, null, 2)
  }
  return String(actualResponse.body)
})

const highlightedResponse = computed(() => {
  let content = formattedResponse.value
  if (searchText.value) {
    const regex = new RegExp(`(${searchText.value})`, 'gi')
    content = content.replace(regex, '<mark>$1</mark>')
  }
  return content
})

watch(() => props.executionResult, (result) => {
  if (result) {
    testStatus.value = result.status === 'passed' ? 'passed' : result.status === 'failed' ? 'failed' : 'not_executed'
    responseTime.value = result.responseTime || '-'
    testTime.value = formatDateTime(result.testTime)
    actualResponse.statusCode = result.statusCode || '-'
    actualResponse.responseCode = result.responseCode || '-'
    actualResponse.body = result.body
    assertionResults.value = result.assertions || []
    responseHeaders.value = result.headers || []
  }
}, { immediate: true })

async function copyResponse() {
  try {
    await navigator.clipboard.writeText(formattedResponse.value)
    ElMessage.success('已复制')
  } catch {
    ElMessage.error('复制失败')
  }
}

function formatResponseBody() {
  // 格式化处理
}

function exportReport() {
  ElMessage.info('导出功能开发中')
}
</script>

<style scoped lang="scss">
.result-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 6px 20px rgba(16, 24, 40, 0.06);
}

.status-banner {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;

  &.status-passed {
    background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
    border: 1px solid #c2e7b0;
  }

  &.status-failed {
    background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
    border: 1px solid #fbbcbc;
  }

  &.status-not_executed {
    background: linear-gradient(135deg, #f4f4f5 0%, #e4e7ed 100%);
    border: 1px solid #d3d3d3;
  }
}

.status-info {
  flex: 1;
}

.status-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.status-meta {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #606266;

  strong {
    color: #303133;
  }
}

.result-tabs {
  display: flex;
  gap: 0;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 16px;
}

.tab-item {
  padding: 10px 20px;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;

  &:hover {
    color: #409eff;
  }

  &.active {
    color: #409eff;
    border-bottom-color: #409eff;
  }
}

.response-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.toolbar-left {
  display: flex;
  gap: 8px;
}

.response-body {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  max-height: 400px;
  overflow: auto;

  pre {
    margin: 0;
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 13px;
    line-height: 1.5;
    white-space: pre-wrap;
    word-break: break-all;

    :deep(mark) {
      background: #fef0e7;
      color: #e6a23c;
      padding: 2px 4px;
      border-radius: 2px;
    }
  }
}

.assertion-field {
  display: flex;
  align-items: center;
  gap: 8px;
}

.text-success {
  color: #67c23a;
}

.text-danger {
  color: #f56c6c;
}

.result-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}
</style>
