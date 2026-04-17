<template>
  <div class="case-detail-panel">
    <!-- 面包屑导航 & 头部 -->
    <CaseDetailHeader
      :test-case="testCase"
      @execute="handleExecute"
      @edit="handleEdit"
      @copy="$emit('copy', testCase)"
      @more-action="$emit('more-action', $event)"
    />

    <!-- 主要内容区域 -->
    <div class="case-content">
      <!-- 左侧主要信息 -->
      <div class="case-main">
        <!-- 基本信息 -->
        <CaseDetailBasicInfo :test-case="testCase" />

        <!-- API信息 -->
        <CaseDetailApiInfo :test-case="testCase" />
      </div>

      <!-- 右侧辅助信息 -->
      <CaseDetailSidebar
        :display-history="displayHistory"
        :execution-history-loading="executionHistoryLoading"
        :execution-history-total="executionHistoryTotal"
        :case-id="testCase?.id"
        @view-history-detail="$emit('view-history-detail', $event)"
        @view-more-history="$emit('view-more-history', $event)"
      />
    </div>

    <!-- 编辑用例对话框 -->
    <TestCaseFormDialog
      v-model="editDialogVisible"
      mode="edit"
      :case-id="testCase?.id"
      :api-id="(testCase as any)?.apiId"
      :project-id="(testCase as any)?.projectId"
      :module-id="(testCase as any)?.moduleId"
      @success="handleEditSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CaseDetailHeader from './CaseDetailHeader.vue'
import CaseDetailBasicInfo from './CaseDetailBasicInfo.vue'
import CaseDetailApiInfo from './CaseDetailApiInfo.vue'
import CaseDetailSidebar from './CaseDetailSidebar.vue'
import TestCaseFormDialog from '@/components/business/api-detail/TestCaseFormDialog.vue'
import type { TestCaseDetailVO } from '@/types/api'

interface ExecutionHistory {
  status?: string
  executor?: string
  executorName?: string
  type?: string
  action?: string
  environment?: string
  note?: string
  remark?: string
  executedAt?: string
  executed_time?: string
  createTime?: string
  durationSeconds?: number
  duration?: number
}

interface Props {
  testCase: TestCaseDetailVO | null
  displayHistory?: ExecutionHistory[]
  executionHistoryLoading?: boolean
  executionHistoryTotal?: number
}

const props = withDefaults(defineProps<Props>(), {
  displayHistory: () => [],
  executionHistoryLoading: false,
  executionHistoryTotal: 0
})

const emit = defineEmits<{
  execute: []
  edit: [testCase: TestCaseDetailVO | null]
  copy: [testCase: TestCaseDetailVO | null]
  'view-history-detail': [history: ExecutionHistory]
  'view-more-history': [caseId: number | null]
  'more-action': [command: string]
}>()

const editDialogVisible = ref(false)

// 处理执行测试 - 传递给父组件
function handleExecute() {
  emit('execute')
}

// 处理编辑用例
function handleEdit() {
  editDialogVisible.value = true
}

// 编辑成功后
function handleEditSuccess() {
  editDialogVisible.value = false
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

.case-detail-panel {
  height: 100%;
  background: white;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.case-content {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  padding: 16px 24px;
  overflow-y: auto;
  flex: 1;
}

.case-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

@media (max-width: 1024px) {
  .case-content {
    flex-direction: column;
    padding: 16px;
  }
}
</style>
