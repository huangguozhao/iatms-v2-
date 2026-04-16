<template>
  <div class="case-detail-basic-info">
    <!-- 基本信息卡片 -->
    <el-card class="info-card" shadow="hover">
      <div class="info-grid">
        <div class="info-item">
          <span class="info-label">优先级</span>
          <el-tag :type="getPriorityType(testCase?.priority)" size="small">
            {{ testCase?.priority || 'P0' }}
          </el-tag>
        </div>
        <div class="info-item">
          <span class="info-label">严重程度</span>
          <el-tag :type="getSeverityType(testCase?.severity)" size="small">
            {{ getSeverityText(testCase?.severity) }}
          </el-tag>
        </div>
        <div class="info-item">
          <span class="info-label">测试类型</span>
          <el-tag :type="getTestTypeTagType(testCase?.testType)" size="small">
            {{ getTestTypeText(testCase?.testType) }}
          </el-tag>
        </div>
        <div class="info-item">
          <span class="info-label">创建人</span>
          <span class="info-value">{{ creatorName }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">版本</span>
          <span class="info-value">{{ testCase?.version || '1.0' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">创建时间</span>
          <span class="info-value">{{ formatTime(testCase?.createdAt) }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">最后修改</span>
          <span class="info-value">{{ formatTime(testCase?.updatedAt) }}</span>
        </div>
      </div>
    </el-card>

    <!-- 用例描述 -->
    <el-card v-if="description" class="section-card" shadow="hover">
      <template #header>
        <div class="section-header">
          <el-icon><Document /></el-icon>
          <span>用例描述</span>
        </div>
      </template>
      <p class="description-text">{{ description }}</p>
    </el-card>

    <!-- 标签 -->
    <el-card v-if="displayTags.length > 0" class="section-card" shadow="hover">
      <template #header>
        <div class="section-header">
          <el-icon><PriceTag /></el-icon>
          <span>标签</span>
        </div>
      </template>
      <div class="tags-container">
        <el-tag
          v-for="(tag, index) in displayTags"
          :key="index"
          type="info"
        >
          {{ tag }}
        </el-tag>
      </div>
    </el-card>

    <!-- 测试步骤 -->
    <el-card class="section-card" shadow="hover">
      <template #header>
        <div class="section-header">
          <el-icon><List /></el-icon>
          <span>测试步骤</span>
        </div>
      </template>
      <div v-if="displaySteps.length > 0" class="steps-list">
        <div v-for="(step, index) in displaySteps" :key="index" class="step-item">
          <div class="step-number">{{ index + 1 }}</div>
          <div class="step-content">
            <div class="step-operation">{{ step.operation || step.desc || step.content }}</div>
            <div class="step-expected" v-if="step.expected">
              预期结果：{{ step.expected }}
            </div>
            <div class="step-actual" v-if="step.actual">
              实际结果：{{ step.actual }}
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无测试步骤" :image-size="60" />
    </el-card>

    <!-- 测试数据 -->
    <el-card class="section-card" shadow="hover">
      <template #header>
        <div class="section-header">
          <el-icon><Grid /></el-icon>
          <span>测试数据</span>
        </div>
      </template>
      <div v-if="displayTestData.length > 0" class="data-grid">
        <div v-for="(item, index) in displayTestData" :key="index" class="data-item">
          <span class="data-label">{{ item.label }}</span>
          <span class="data-value">{{ item.value }}</span>
        </div>
      </div>
      <el-empty v-else description="暂无测试数据" :image-size="60" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Document, PriceTag, List, Grid } from '@element-plus/icons-vue'
import type { TestCaseDetailVO } from '@/types/api'

interface TestStep {
  operation?: string
  desc?: string
  content?: string
  expected?: string
  actual?: string
}

interface Props {
  testCase: TestCaseDetailVO | null
}

const props = defineProps<Props>()

// 创建人姓名
const creatorName = computed(() => {
  if (!props.testCase) return '未知'
  // 尝试从各种可能的字段获取创建人名称
  return (props.testCase as any).creatorName ||
    (props.testCase as any).creator_name ||
    (props.testCase as any).creator?.name ||
    '未知'
})

// 用例描述
const description = computed(() => {
  return props.testCase?.description || ''
})

// 显示标签
const displayTags = computed(() => {
  const tags = (props.testCase as any)?.tags

  if (!tags) return []

  if (Array.isArray(tags)) {
    return tags
  }

  if (typeof tags === 'string') {
    try {
      const parsed = JSON.parse(tags)
      return Array.isArray(parsed) ? parsed : []
    } catch {
      return []
    }
  }

  return []
})

// 显示测试步骤
const displaySteps = computed((): TestStep[] => {
  const steps = (props.testCase as any)?.testSteps || (props.testCase as any)?.test_steps

  if (!steps) return []

  if (Array.isArray(steps)) {
    return steps
  }

  if (typeof steps === 'string') {
    try {
      const parsed = JSON.parse(steps)
      return Array.isArray(parsed) ? parsed : []
    } catch {
      return []
    }
  }

  return []
})

// 显示测试数据
const displayTestData = computed(() => {
  const data = (props.testCase as any)?.testData ||
    (props.testCase as any)?.test_data ||
    (props.testCase as any)?.preConditions ||
    (props.testCase as any)?.pre_conditions

  if (!data) return []

  if (typeof data === 'object') {
    return Object.entries(data).map(([key, value]) => ({
      label: key,
      value: typeof value === 'object' ? JSON.stringify(value) : String(value)
    }))
  }

  if (typeof data === 'string') {
    try {
      const parsed = JSON.parse(data)
      if (typeof parsed === 'object') {
        return Object.entries(parsed).map(([key, value]) => ({
          label: key,
          value: typeof value === 'object' ? JSON.stringify(value) : String(value)
        }))
      }
    } catch {
      return []
    }
  }

  return []
})

// 格式化时间
function formatTime(time: string | undefined): string {
  if (!time) return '-'

  if (typeof time === 'string' && time.includes('T')) {
    const date = new Date(time)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    }).replace(/\//g, '-')
  }

  return time
}

// 获取优先级类型
function getPriorityType(priority: string | undefined): string {
  const typeMap: Record<string, string> = {
    'P0': 'danger',
    'P1': 'danger',
    'P2': 'warning',
    'P3': 'info',
    'HIGH': 'danger',
    'MEDIUM': 'warning',
    'LOW': 'info'
  }
  return typeMap[priority || ''] || 'warning'
}

// 获取严重程度类型
function getSeverityType(severity: string | undefined): string {
  const typeMap: Record<string, string> = {
    'CRITICAL': 'danger',
    'HIGH': 'warning',
    'MEDIUM': 'info',
    'LOW': ''
  }
  return typeMap[severity || ''] || 'info'
}

// 获取严重程度文本
function getSeverityText(severity: string | undefined): string {
  const textMap: Record<string, string> = {
    'CRITICAL': '严重',
    'HIGH': '高',
    'MEDIUM': '中',
    'LOW': '低'
  }
  return textMap[severity || ''] || severity || '中'
}

// 获取测试类型标签颜色
function getTestTypeTagType(type: string | undefined): string {
  const typeMap: Record<string, string> = {
    'FUNCTIONAL': 'primary',
    'BOUNDARY': 'warning',
    'EXCEPTION': 'danger',
    'SECURITY': 'success',
    'PERFORMANCE': 'info',
    'INTEGRATION': 'primary',
    'SMOKE': 'success',
    'REGRESSION': 'warning'
  }
  return typeMap[type || ''] || ''
}

// 获取测试类型文本
function getTestTypeText(type: string | undefined): string {
  const textMap: Record<string, string> = {
    'FUNCTIONAL': '功能测试',
    'BOUNDARY': '边界测试',
    'EXCEPTION': '异常测试',
    'SECURITY': '安全测试',
    'PERFORMANCE': '性能测试',
    'INTEGRATION': '集成测试',
    'SMOKE': '冒烟测试',
    'REGRESSION': '回归测试'
  }
  return textMap[type || ''] || type || '功能测试'
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

.case-detail-basic-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-card,
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

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-label {
  font-weight: 500;
  color: $text-secondary;
  min-width: 60px;
}

.info-value {
  color: $text-primary;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: $text-primary;
}

.description-text {
  color: $text-secondary;
  line-height: 1.6;
  margin: 0;
  white-space: pre-wrap;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.steps-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.step-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border: 1px solid $border-color;
  border-radius: calc($card-radius - 4px);
  background-color: $bg-lighter;
}

.step-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff 0%, #3b82f6 100%);
  color: white;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.step-content {
  flex: 1;
}

.step-operation {
  font-weight: 500;
  color: $text-primary;
  margin-bottom: 4px;
}

.step-expected,
.step-actual {
  font-size: 14px;
  color: $text-secondary;
  margin-top: 4px;
}

.step-expected {
  color: #67c23a;
}

.step-actual {
  color: #f56c6c;
}

.data-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.data-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px;
  border: 1px solid $border-color;
  border-radius: calc($card-radius - 6px);
  background-color: $bg-lighter;
}

.data-label {
  font-weight: 500;
  color: $text-primary;
  font-size: 14px;
}

.data-value {
  color: $text-secondary;
  font-size: 13px;
  word-break: break-all;
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .data-grid {
    grid-template-columns: 1fr;
  }

  .step-item {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
