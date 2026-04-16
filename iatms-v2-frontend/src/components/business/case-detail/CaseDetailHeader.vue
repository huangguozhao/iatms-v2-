<template>
  <div class="case-detail-header">
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <span class="breadcrumb-item">测试用例</span>
      <span class="breadcrumb-separator">›</span>
      <span class="breadcrumb-item active">{{ testCase?.caseCode || testCase?.name || '未知用例' }}</span>
    </div>

    <!-- 用例标题 -->
    <div class="case-header">
      <div class="case-title-row">
        <h2 class="case-title">{{ testCase?.name || '未知用例' }}</h2>
        <el-tag v-if="testCase && testCase.status === 'DISABLED'" type="danger" size="small" class="disabled-tag">
          已禁用
        </el-tag>
      </div>
      <div class="case-actions">
        <el-button
          type="primary"
          :icon="VideoPlay"
          :disabled="!testCase || testCase.status === 'DISABLED'"
          @click="$emit('execute')"
        >
          执行测试
        </el-button>
        <el-button
          :icon="Edit"
          @click="$emit('edit')"
        >
          编辑
        </el-button>
        <el-button
          :icon="CopyDocument"
          @click="$emit('copy')"
        >
          复制
        </el-button>
        <el-dropdown @command="handleMoreAction" trigger="click">
          <el-button :icon="MoreFilled">
            更多
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="export" :icon="Download">
                导出用例
              </el-dropdown-item>
              <el-dropdown-item command="history" :icon="Clock">
                查看历史
              </el-dropdown-item>
              <el-dropdown-item command="share" :icon="Share">
                分享用例
              </el-dropdown-item>
              <el-dropdown-item
                v-if="testCase?.status === 'ENABLED'"
                divided
                command="disable"
                :icon="CircleClose"
              >
                禁用用例
              </el-dropdown-item>
              <el-dropdown-item
                v-else
                divided
                command="enable"
                :icon="CircleCheck"
              >
                启用用例
              </el-dropdown-item>
              <el-dropdown-item command="delete" :icon="Delete" style="color: #f56c6c;">
                删除用例
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  VideoPlay,
  Edit,
  CopyDocument,
  MoreFilled,
  Download,
  Clock,
  Share,
  CircleClose,
  CircleCheck,
  Delete
} from '@element-plus/icons-vue'
import type { TestCaseDetailVO } from '@/types/api'

interface Props {
  testCase: TestCaseDetailVO | null
}

defineProps<Props>()

defineEmits<{
  execute: []
  edit: []
  copy: []
  moreAction: [command: string]
}>()

function handleMoreAction(command: string) {
  // Emit the more-action event with the command
  // The parent component will handle the action
  const event = new CustomEvent('more-action', { detail: command })
  window.dispatchEvent(event)
}
</script>

<style scoped lang="scss">
.case-detail-header {
  margin-bottom: 24px;
}

.breadcrumb {
  margin-bottom: 16px;
  font-size: 14px;
  color: #666;
}

.breadcrumb-item {
  cursor: default;

  &.active {
    color: #409eff;
    font-weight: 500;
  }
}

.breadcrumb-separator {
  margin: 0 8px;
  color: #ccc;
}

.case-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.case-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.case-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.disabled-tag {
  margin-left: 8px;
}

.case-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

@media (max-width: 768px) {
  .case-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .case-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
