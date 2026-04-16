<template>
  <div class="case-detail-panel">
    <!-- 面包屑导航 & 头部 -->
    <CaseDetailHeader
      :test-case="testCase"
      @execute="handleExecute"
      @edit="$emit('edit', testCase)"
      @copy="$emit('copy', testCase)"
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
        @view-history-detail="$emit('view-history-detail', $event)"
        @view-more-history="$emit('view-more-history')"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CaseDetailHeader from './CaseDetailHeader.vue'
import CaseDetailBasicInfo from './CaseDetailBasicInfo.vue'
import CaseDetailApiInfo from './CaseDetailApiInfo.vue'
import CaseDetailSidebar from './CaseDetailSidebar.vue'
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

defineEmits<{
  execute: []
  edit: [testCase: TestCaseDetailVO | null]
  copy: [testCase: TestCaseDetailVO | null]
  'view-history-detail': [history: ExecutionHistory]
  'view-more-history': []
}>()

// 处理执行测试
function handleExecute() {
  // 触发执行事件，由父组件处理
  // The parent component will handle opening the execute config dialog
}
</script>

<style scoped lang="scss">
.case-detail-panel {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.case-content {
  display: flex;
  gap: 16px;
  align-items: flex-start;
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
  }
}
</style>
