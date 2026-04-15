<template>
  <div class="ai-diagnosis page-container">
    <div class="page-header">
      <h2 class="title">AI 智能诊断</h2>
    </div>

    <el-card class="diagnosis-card glass-card">
      <template #header>
        <div class="card-header">
          <span>失败原因分析</span>
        </div>
      </template>

      <el-form ref="formRef" :model="form" label-width="120px" class="diagnosis-form">
        <el-form-item label="用例名称" prop="caseName">
          <el-input v-model="form.caseName" placeholder="请输入测试用例名称" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="请求方法" prop="method">
              <el-select v-model="form.method" placeholder="选择方法" style="width: 100%">
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
                <el-option label="PUT" value="PUT" />
                <el-option label="DELETE" value="DELETE" />
                <el-option label="PATCH" value="PATCH" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="API 路径" prop="apiPath">
              <el-input v-model="form.apiPath" placeholder="/api/xxx" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="HTTP 状态码" prop="httpStatus">
              <el-input-number v-model="form.httpStatus" :min="100" :max="599" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预期结果" prop="expected">
              <el-input v-model="form.expected" placeholder="预期返回的内容或状态" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="实际结果" prop="actual">
          <el-input v-model="form.actual" type="textarea" rows="3" placeholder="实际返回的内容或错误信息" />
        </el-form-item>

        <el-form-item label="错误信息" prop="errorMessage">
          <el-input v-model="form.errorMessage" type="textarea" rows="2" placeholder="详细的错误信息" />
        </el-form-item>

        <el-form-item label="响应体" prop="responseBody">
          <el-input v-model="form.responseBody" type="textarea" rows="4" placeholder="完整的响应体内容（JSON格式）" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleDiagnose" class="btn-gradient-primary">
            <el-icon v-if="!loading"><MagicStick /></el-icon>
            开始诊断
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 诊断结果 -->
    <el-card v-if="result" class="result-card glass-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>诊断结果</span>
          <el-tag :type="getSeverityType(result.severity)" size="large">
            严重程度: {{ getSeverityText(result.severity) }}
          </el-tag>
        </div>
      </template>

      <!-- 根本原因 -->
      <div class="result-section">
        <h3 class="section-title">
          <el-icon><Warning /></el-icon>
          根本原因分析
        </h3>
        <div class="root-cause-box">
          {{ result.rootCause }}
        </div>
      </div>

      <!-- 问题列表 -->
      <div class="result-section" v-if="result.issues && result.issues.length">
        <h3 class="section-title">
          <el-icon><CircleClose /></el-icon>
          发现的问题
        </h3>
        <el-timeline>
          <el-timeline-item
            v-for="(issue, index) in result.issues"
            :key="index"
            :type="getIssueSeverityType(issue.severity)"
            :icon="getIssueIcon(issue.severity)"
          >
            <div class="issue-item">
              <h4>{{ issue.title }}</h4>
              <p>{{ issue.description }}</p>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>

      <!-- 修复建议 -->
      <div class="result-section" v-if="result.suggestions && result.suggestions.length">
        <h3 class="section-title">
          <el-icon><Guide /></el-icon>
          修复建议
        </h3>
        <el-collapse>
          <el-collapse-item
            v-for="(suggestion, index) in result.suggestions"
            :key="index"
            :title="suggestion.title"
          >
            <template #title>
              <span class="suggestion-title">
                {{ suggestion.title }}
                <el-tag size="small" :type="getPriorityType(suggestion.priority)">
                  {{ getPriorityText(suggestion.priority) }}
                </el-tag>
              </span>
            </template>
            <div class="suggestion-content">
              {{ suggestion.content }}
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>

      <!-- 详细分析 -->
      <div class="result-section" v-if="result.analysis">
        <h3 class="section-title">
          <el-icon><Document /></el-icon>
          详细分析
        </h3>
        <div class="analysis-box">
          {{ result.analysis }}
        </div>
      </div>
    </el-card>

    <!-- 历史诊断 -->
    <el-card class="history-card glass-card">
      <template #header>
        <div class="card-header">
          <span>诊断历史</span>
          <el-button link type="primary" @click="loadHistory">刷新</el-button>
        </div>
      </template>

      <el-table :data="history" style="width: 100%" v-loading="historyLoading">
        <el-table-column prop="caseName" label="用例名称" min-width="150" />
        <el-table-column prop="errorMessage" label="错误信息" min-width="200" show-overflow-tooltip />
        <el-table-column prop="severity" label="严重程度" width="100">
          <template #default="{ row }">
            <el-tag :type="getSeverityType(row.severity)" size="small">
              {{ getSeverityText(row.severity) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="诊断时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewHistory(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
  MagicStick,
  Warning,
  CircleClose,
  Guide,
  Document
} from '@element-plus/icons-vue'
import { aiApi, type DiagnosisResult } from '@/api/modules/ai/ai'

const loading = ref(false)
const historyLoading = ref(false)
const result = ref<DiagnosisResult | null>(null)
const history = ref<any[]>([])

const form = reactive({
  caseName: '',
  method: 'GET',
  apiPath: '',
  httpStatus: 500,
  expected: '',
  actual: '',
  errorMessage: '',
  responseBody: ''
})

const formRef = ref()

async function handleDiagnose() {
  if (!form.caseName) {
    ElMessage.warning('请输入用例名称')
    return
  }

  loading.value = true
  result.value = null

  try {
    const res = await aiApi.diagnoseFailure({
      caseName: form.caseName,
      method: form.method,
      apiPath: form.apiPath,
      httpStatus: form.httpStatus,
      expected: form.expected,
      actual: form.actual,
      errorMessage: form.errorMessage,
      responseBody: form.responseBody
    })

    if (res.data) {
      result.value = res.data
      ElMessage.success('诊断完成')
      loadHistory()
    }
  } catch (error: any) {
    ElMessage.error(error.message || '诊断失败')
  } finally {
    loading.value = false
  }
}

function handleReset() {
  form.caseName = ''
  form.method = 'GET'
  form.apiPath = ''
  form.httpStatus = 500
  form.expected = ''
  form.actual = ''
  form.errorMessage = ''
  form.responseBody = ''
  result.value = null
}

async function loadHistory() {
  // 从 localStorage 加载历史记录
  const stored = localStorage.getItem('ai_diagnosis_history')
  if (stored) {
    try {
      history.value = JSON.parse(stored)
    } catch {
      history.value = []
    }
  }
}

function viewHistory(item: any) {
  // 填充表单
  form.caseName = item.caseName
  form.method = item.method || 'GET'
  form.apiPath = item.apiPath || ''
  form.httpStatus = item.httpStatus || 500
  form.expected = item.expected || ''
  form.actual = item.actual || ''
  form.errorMessage = item.errorMessage || ''
  form.responseBody = item.responseBody || ''

  // 显示结果
  result.value = item.result
}

function getSeverityType(severity: string) {
  switch (severity) {
    case 'high': return 'danger'
    case 'medium': return 'warning'
    case 'low': return 'success'
    default: return 'info'
  }
}

function getSeverityText(severity: string) {
  switch (severity) {
    case 'high': return '高'
    case 'medium': return '中'
    case 'low': return '低'
    default: return '未知'
  }
}

function getIssueSeverityType(severity: string) {
  switch (severity) {
    case 'high': return 'danger'
    case 'medium': return 'warning'
    case 'low': return 'info'
    default: return 'info'
  }
}

function getIssueIcon(severity: string) {
  switch (severity) {
    case 'high': return 'CircleClose'
    case 'medium': return 'Warning'
    default: return 'InfoFilled'
  }
}

function getPriorityType(priority: string) {
  switch (priority) {
    case 'high': return 'danger'
    case 'medium': return 'warning'
    case 'low': return 'info'
    default: return 'info'
  }
}

function getPriorityText(priority: string) {
  switch (priority) {
    case 'high': return '高优先级'
    case 'medium': return '中优先级'
    case 'low': return '低优先级'
    default: return '普通'
  }
}

// 初始化
loadHistory()
</script>

<style scoped lang="scss">
.ai-diagnosis {
  max-width: 1200px;
  margin: 0 auto;
}

.diagnosis-card,
.result-card,
.history-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.diagnosis-form {
  max-width: 800px;
}

.result-section {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;

  .el-icon {
    color: #409eff;
  }
}

.root-cause-box {
  background: linear-gradient(135deg, rgba(24, 144, 255, 0.08) 0%, rgba(64, 169, 255, 0.08) 100%);
  border-left: 4px solid #1890ff;
  border-radius: 0 8px 8px 0;
  padding: 16px 20px;
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
}

.issue-item {
  h4 {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 4px;
  }

  p {
    font-size: 13px;
    color: #606266;
    line-height: 1.6;
  }
}

.suggestion-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.suggestion-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  padding: 8px 0;
}

.analysis-box {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px 20px;
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}
</style>
