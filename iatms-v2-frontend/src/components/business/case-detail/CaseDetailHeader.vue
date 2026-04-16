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
// 复用旧前端样式变量
$card-radius: 12px;
$card-shadow: 0 10px 30px rgba(16, 24, 40, 0.06);
$card-shadow-hover: 0 18px 40px rgba(16, 24, 40, 0.08);
$card-transition: transform 0.18s cubic-bezier(0.2, 0.8, 0.2, 1), box-shadow 0.18s cubic-bezier(0.2, 0.8, 0.2, 1);
$border-color: #e4e7ed;
$text-primary: #303133;
$text-secondary: #606266;
$text-placeholder: #c0c4cc;

.case-detail-header {
  flex-shrink: 0;
  border-bottom: 1px solid $border-color;
}

/* 面包屑导航 */
.breadcrumb {
  padding: 16px 24px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: 1px solid $border-color;
}

.breadcrumb-item {
  font-size: 14px;
  color: $text-secondary;
  cursor: default;

  &.active {
    color: $text-primary;
    font-weight: 500;
  }
}

.breadcrumb-separator {
  color: $text-placeholder;
  font-size: 14px;
}

/* 用例标题 */
.case-header {
  padding: 20px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
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
  color: $text-primary;
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
  .breadcrumb {
    padding: 12px 16px;
  }

  .case-header {
    padding: 16px;
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
