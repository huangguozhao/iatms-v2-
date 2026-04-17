<template>
  <div class="detail-header">
    <div class="header-left">
      <h2 class="api-title">
        {{ apiData.name || '未知接口' }}
      </h2>

      <div v-if="apiData.description" class="api-description">
        {{ apiData.description }}
      </div>

      <div v-if="apiData.tags && apiData.tags.length > 0" class="api-tags">
        <el-tag
          v-for="(tag, index) in apiData.tags"
          :key="index"
          size="small"
          class="api-tag-item"
        >
          {{ tag }}
        </el-tag>
      </div>

      <div class="api-info-line">
        <code class="api-path">{{ apiData.url || '-' }}</code>
        <el-tooltip content="复制路径" placement="top">
          <button class="copy-path-btn" @click.stop="copyPath">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path d="M16 1H4a2 2 0 0 0-2 2v12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              <rect x="8" y="5" width="13" height="13" rx="2" stroke="currentColor" stroke-width="1.5"/>
            </svg>
          </button>
        </el-tooltip>
        <span class="method-tag" :class="'method-' + (apiData.httpMethod || '').toLowerCase()">
          {{ apiData.httpMethod || '-' }}
        </span>
        <el-tag :type="getApiStatusTagType(apiData.status)" size="small">
          {{ getApiStatusText(apiData.status) }}
        </el-tag>
        <span v-if="apiData.version" class="version-tag">v{{ apiData.version }}</span>
      </div>

      <div class="api-meta">
        <span class="meta-item">
          <span class="meta-label">认证方式：</span>
          <span class="meta-value">{{ getAuthTypeText(apiData.authType) }}</span>
        </span>
        <span class="meta-item">
          <span class="meta-label">超时时间：</span>
          <span class="meta-value">{{ apiData.timeoutSeconds || 30 }}秒</span>
        </span>
        <span class="meta-item">
          <span class="meta-label">请求体类型：</span>
          <span class="meta-value">{{ apiData.requestBodyType || '-' }}</span>
        </span>
      </div>
    </div>

    <div class="header-right">
      <div class="detail-actions">
        <el-button size="small" type="success" @click="$emit('save')" :loading="saving">
          <el-icon><Check /></el-icon>
          保存修改
        </el-button>
        <el-button size="small" type="primary" @click="$emit('execute')">
          <el-icon><CaretRight /></el-icon>
          执行测试
        </el-button>
        <el-button size="small" @click="$emit('refresh')">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <div v-if="apiData.creatorName" class="creator-info">
        <el-avatar :size="32" class="creator-avatar">
          {{ apiData.creatorName?.charAt(0) || '?' }}
        </el-avatar>
        <div class="creator-details">
          <div class="creator-name">{{ apiData.creatorName }}</div>
          <div class="creator-label">创建人</div>
        </div>
      </div>

      <div class="time-info-group">
        <span class="time-info">创建时间：{{ formatDateTime(apiData.createdAt) }}</span>
        <span class="time-info">更新时间：{{ formatDateTime(apiData.updatedAt) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Check, Refresh, CaretRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getApiStatusTagType,
  getApiStatusText,
  getAuthTypeText,
  formatDateTime
} from '@/utils/formatters'
import type { ApiData } from '@/composables/useApiData'

defineProps({
  apiData: {
    type: Object as () => ApiData,
    required: true
  },
  saving: {
    type: Boolean,
    default: false
  }
})

defineEmits(['save', 'execute', 'refresh'])

async function copyPath() {
  try {
    await navigator.clipboard.writeText(apiData.url || '')
    ElMessage.success('路径已复制')
  } catch {
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped lang="scss">
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px 24px;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.header-left {
  flex: 1;
}

.api-title {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.api-description {
  color: #606266;
  font-size: 14px;
  margin-bottom: 12px;
}

.api-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.api-tag-item {
  background: #ecf5ff;
  color: #409eff;
  border: none;
}

.api-info-line {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.api-path {
  background: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  color: #303133;
}

.copy-path-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  background: #f5f7fa;
  border-radius: 4px;
  cursor: pointer;
  color: #606266;
  transition: all 0.2s;

  &:hover {
    background: #dcdfe6;
    color: #303133;
  }
}

.method-tag {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;

  &.method-get { background: #e1f3d8; color: #67c23a; }
  &.method-post { background: #fef0e7; color: #e6a23c; }
  &.method-put { background: #d9ecff; color: #409eff; }
  &.method-delete { background: #fde2e2; color: #f56c6c; }
  &.method-patch { background: #f4f4f5; color: #909399; }
}

.version-tag {
  background: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
}

.api-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  font-size: 13px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-label {
  color: #c0c4cc;
}

.meta-value {
  color: #606266;
}

.header-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.detail-actions {
  display: flex;
  gap: 8px;
}

.creator-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.creator-avatar {
  background: #409eff;
  color: white;
}

.creator-details {
  .creator-name {
    font-size: 13px;
    font-weight: 500;
    color: #303133;
  }
  .creator-label {
    font-size: 11px;
    color: #c0c4cc;
  }
}

.time-info-group {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #c0c4cc;
}

@media (max-width: 768px) {
  .detail-header {
    flex-direction: column;
    gap: 16px;
  }
  .header-right {
    align-items: flex-start;
  }
  .api-meta {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
