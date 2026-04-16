<template>
  <div class="case-detail-api-info">
    <!-- 预期响应 -->
    <el-card class="section-card" shadow="hover">
      <template #header>
        <div class="section-header">
          <el-icon><Connection /></el-icon>
          <span>预期响应</span>
        </div>
      </template>
      <div class="expected-response-section">
        <div class="response-item">
          <span class="response-label">状态码</span>
          <el-tag
            :type="getStatusCodeType(displayHttpStatus)"
            size="small"
          >
            {{ displayHttpStatus || 200 }}
          </el-tag>
        </div>
        <div class="response-item">
          <span class="response-label">响应时间</span>
          <span class="response-value">&lt; {{ displayResponseTime || '2秒' }}</span>
        </div>
        <div class="response-item full-width" v-if="displayValidationRules.length > 0">
          <span class="response-label">验证规则</span>
          <div class="validation-rules">
            <el-tag
              v-for="(rule, index) in displayValidationRules"
              :key="index"
              type="info"
              size="small"
            >
              {{ rule }}
            </el-tag>
          </div>
        </div>
        <div class="response-item full-width">
          <span class="response-label">响应体</span>
          <div class="response-code">
            <el-button
              class="copy-btn"
              size="small"
              type="primary"
              link
              :icon="DocumentCopy"
              @click="handleCopyExpectedResponse"
            >
              复制
            </el-button>
            <pre>{{ formatExpectedResponse() }}</pre>
          </div>
        </div>
        <div class="response-item full-width" v-if="hasExpectedResponseSchema">
          <span class="response-label">响应Schema</span>
          <div class="response-code">
            <el-button
              class="copy-btn"
              size="small"
              type="primary"
              link
              :icon="DocumentCopy"
              @click="handleCopyExpectedResponseSchema"
            >
              复制
            </el-button>
            <pre>{{ formatExpectedResponseSchema() }}</pre>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 响应提取规则 -->
    <el-card v-if="displayExtractors.length > 0" class="section-card" shadow="hover">
      <template #header>
        <div class="section-header">
          <el-icon><Collection /></el-icon>
          <span>响应提取规则</span>
        </div>
      </template>
      <div class="extractors-list">
        <div
          v-for="(extractor, index) in displayExtractors"
          :key="index"
          class="extractor-item"
        >
          <div class="extractor-header">
            <span class="extractor-name">{{ extractor.name }}</span>
            <el-tag size="small" type="success">提取变量</el-tag>
          </div>
          <div class="extractor-expression">
            <span class="expression-label">表达式:</span>
            <code class="expression-code">{{ extractor.expression }}</code>
          </div>
          <div class="extractor-description" v-if="extractor.description">
            {{ extractor.description }}
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { DocumentCopy, Connection, Collection } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { TestCaseDetailVO } from '@/types/api'

interface Extractor {
  name: string
  expression: string
  description?: string
}

interface Props {
  testCase: TestCaseDetailVO | null
}

const props = defineProps<Props>()

// 显示 HTTP 状态码
const displayHttpStatus = computed(() => {
  return (props.testCase as any)?.expectedHttpStatus ||
    (props.testCase as any)?.expected_http_status ||
    (props.testCase as any)?.expectedResponseHttpStatus ||
    200
})

// 显示响应时间
const displayResponseTime = computed(() => {
  return (props.testCase as any)?.expectedResponseTime ||
    (props.testCase as any)?.expected_response_time ||
    '2秒'
})

// 显示验证规则
const displayValidationRules = computed((): string[] => {
  const rules = (props.testCase as any)?.validationRules ||
    (props.testCase as any)?.validation_rules

  if (!rules) return []

  if (Array.isArray(rules)) {
    return rules
  }

  if (typeof rules === 'string') {
    try {
      const parsed = JSON.parse(rules)
      return Array.isArray(parsed) ? parsed : []
    } catch {
      return []
    }
  }

  return []
})

// 显示提取器
const displayExtractors = computed((): Extractor[] => {
  const extractors = (props.testCase as any)?.extractors

  if (!extractors) return []

  if (Array.isArray(extractors)) {
    return extractors
  }

  if (typeof extractors === 'string') {
    try {
      const parsed = JSON.parse(extractors)
      return Array.isArray(parsed) ? parsed : []
    } catch {
      return []
    }
  }

  return []
})

// 是否有预期响应Schema
const hasExpectedResponseSchema = computed(() => {
  return !!((props.testCase as any)?.expectedResponseSchema ||
    (props.testCase as any)?.expected_response_schema)
})

// 获取状态码标签类型
function getStatusCodeType(code: number | string | undefined): string {
  const numCode = typeof code === 'string' ? parseInt(code, 10) : code
  if (!numCode) return 'success'
  if (numCode >= 200 && numCode < 300) return 'success'
  if (numCode >= 300 && numCode < 400) return 'warning'
  if (numCode >= 400 && numCode < 500) return 'warning'
  if (numCode >= 500) return 'danger'
  return 'info'
}

// 格式化预期响应
function formatExpectedResponse(): string {
  const response = (props.testCase as any)?.expectedResponse ||
    (props.testCase as any)?.expected_response

  if (!response) return '{}'

  if (typeof response === 'object') {
    return JSON.stringify(response, null, 2)
  }

  if (typeof response === 'string') {
    try {
      const parsed = JSON.parse(response)
      return JSON.stringify(parsed, null, 2)
    } catch {
      return response
    }
  }

  return String(response)
}

// 格式化预期响应Schema
function formatExpectedResponseSchema(): string {
  const schema = (props.testCase as any)?.expectedResponseSchema ||
    (props.testCase as any)?.expected_response_schema

  if (!schema) return '{}'

  if (typeof schema === 'object') {
    return JSON.stringify(schema, null, 2)
  }

  if (typeof schema === 'string') {
    try {
      const parsed = JSON.parse(schema)
      return JSON.stringify(parsed, null, 2)
    } catch {
      return schema
    }
  }

  return String(schema)
}

// 复制预期响应到剪贴板
async function handleCopyExpectedResponse() {
  try {
    await navigator.clipboard.writeText(formatExpectedResponse())
    ElMessage.success('响应体已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}

// 复制预期响应Schema到剪贴板
async function handleCopyExpectedResponseSchema() {
  try {
    await navigator.clipboard.writeText(formatExpectedResponseSchema())
    ElMessage.success('响应Schema已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
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

.case-detail-api-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-card {
  border-radius: $card-radius;
  box-shadow: $card-shadow;
  transition: $card-transition;

  &:hover {
    box-shadow: $card-shadow-hover;
  }

  :deep(.el-card__header) {
    padding: 12px 20px;
    background: $bg-light;
    border-radius: $card-radius $card-radius 0 0;
    border-bottom: 1px solid $border-color;
  }

  :deep(.el-card__body) {
    padding: 16px 20px;
  }
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: $text-primary;
}

.expected-response-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.response-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.response-item.full-width {
  grid-column: 1 / -1;
}

.response-label {
  font-weight: 500;
  color: $text-secondary;
  font-size: 14px;
}

.response-value {
  color: $text-primary;
  font-size: 14px;
}

.response-code {
  position: relative;
  border: 1px solid $border-color;
  border-radius: calc($card-radius - 6px);
  background-color: $bg-light;
  max-height: 300px;
  overflow: auto;
}

.response-code pre {
  margin: 0;
  padding: 12px 40px 12px 12px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.4;
  color: $text-primary;
  white-space: pre-wrap;
  word-break: break-all;
}

.copy-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 10;
}

.validation-rules {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.extractors-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.extractor-item {
  padding: 16px;
  border: 1px solid $border-color;
  border-radius: calc($card-radius - 4px);
  background-color: $bg-lighter;
}

.extractor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.extractor-name {
  font-weight: 600;
  color: $text-primary;
  font-size: 14px;
}

.extractor-expression {
  margin-bottom: 8px;
}

.expression-label {
  font-weight: 500;
  color: $text-secondary;
  font-size: 13px;
  margin-right: 8px;
}

.expression-code {
  background-color: $bg-light;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  color: #f56c6c;
}

.extractor-description {
  color: $text-placeholder;
  font-size: 13px;
  line-height: 1.4;
}

@media (max-width: 768px) {
  .expected-response-section {
    grid-template-columns: 1fr;
  }

  .response-item.full-width {
    grid-column: auto;
  }
}
</style>
