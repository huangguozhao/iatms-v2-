<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="800px"
    :close-on-click-modal="false"
    destroy-on-close
    class="execute-config-dialog"
    @close="handleClose"
  >
    <div class="execute-dialog-content">
      <!-- 执行目标信息卡片 -->
      <el-card class="target-info-card" shadow="never">
        <div class="target-header">
          <div class="target-icon">
            <el-icon :size="24"><VideoPlay /></el-icon>
          </div>
          <div class="target-details">
            <div class="target-title">{{ targetTypeLabel }}</div>
            <div class="target-meta">
              <el-tag size="small" type="primary">{{ targetName || '未选择目标' }}</el-tag>
              <span class="target-desc">将执行 {{ caseCount }} 个测试用例</span>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 环境配置区域 -->
      <div class="config-section">
        <div class="section-header">
          <el-icon><Connection /></el-icon>
          <span>执行环境</span>
        </div>
        <el-card class="config-card" shadow="never">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="执行环境" required>
                <el-select v-model="formData.environment" placeholder="请选择执行环境" style="width: 100%">
                  <el-option v-for="env in environmentList" :key="env.value" :label="env.label" :value="env.value">
                    <div class="option-content">
                      <span class="option-dot" :class="env.value"></span>
                      <span>{{ env.label }}</span>
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="超时时间">
                <el-input-number
                  v-model="formData.timeout"
                  :min="1"
                  :max="300"
                  :step="10"
                  placeholder="30"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="Base URL">
            <el-input
              v-model="formData.baseUrl"
              placeholder="留空则使用环境默认URL"
              clearable
            >
              <template #prefix>
                <el-icon><Link /></el-icon>
              </template>
            </el-input>
            <div class="url-preview" v-if="formData.baseUrl">
              <span class="preview-label">预览：</span>
              <span class="preview-url">{{ formData.baseUrl }}</span>
            </div>
          </el-form-item>
        </el-card>
      </div>

      <!-- 测试用例筛选 -->
      <div class="config-section">
        <div class="section-header">
          <el-icon><Filter /></el-icon>
          <span>测试用例筛选</span>
          <el-tag size="small" type="warning">高级选项</el-tag>
        </div>
        <el-card class="config-card advanced-card" shadow="never">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="并发执行数">
                <el-input-number
                  v-model="formData.concurrency"
                  :min="1"
                  :max="10"
                  placeholder="3"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="执行顺序">
                <el-select v-model="formData.executionOrder" placeholder="选择执行顺序" style="width: 100%">
                  <el-option label="依赖顺序执行（推荐）" value="dependency" />
                  <el-option label="优先级降序" value="priority_desc" />
                  <el-option label="优先级升序" value="priority_asc" />
                  <el-option label="名称升序" value="name_asc" />
                  <el-option label="名称降序" value="name_desc" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="优先级过滤">
                <el-select
                  v-model="formData.priorityFilter"
                  multiple
                  placeholder="全部优先级"
                  style="width: 100%"
                >
                  <el-option label="P0（最高优先级）" value="P0" />
                  <el-option label="P1（高优先级）" value="P1" />
                  <el-option label="P2（中等优先级）" value="P2" />
                  <el-option label="P3（低优先级）" value="P3" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="标签过滤">
                <el-select
                  v-model="formData.tagFilter"
                  multiple
                  filterable
                  allow-create
                  placeholder="全部标签"
                  style="width: 100%"
                >
                  <el-option v-for="tag in availableTags" :key="tag" :label="tag" :value="tag" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item>
            <el-checkbox v-model="formData.enabledOnly">
              <span>仅执行已启用的测试用例</span>
            </el-checkbox>
          </el-form-item>
        </el-card>
      </div>

      <!-- 执行模式配置 -->
      <div class="config-section">
        <div class="section-header">
          <el-icon><Timer /></el-icon>
          <span>执行模式与变量</span>
        </div>
        <el-card class="config-card" shadow="never">
          <el-form-item label="执行方式">
            <el-radio-group v-model="formData.async" class="execution-mode-group">
              <el-radio :value="false" class="mode-option">
                <div class="mode-content">
                  <div class="mode-title">同步执行</div>
                  <div class="mode-desc">等待测试完成并返回详细结果</div>
                </div>
              </el-radio>
              <el-radio :value="true" class="mode-option">
                <div class="mode-content">
                  <div class="mode-title">异步执行</div>
                  <div class="mode-desc">立即返回任务ID，后台执行</div>
                </div>
              </el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="执行变量">
            <div class="variables-header">
              <el-input
                v-model="formData.variables"
                type="textarea"
                :rows="3"
                placeholder='{"username": "testuser", "token": "your-token"}'
                class="variables-textarea"
              />
              <div class="variable-actions">
                <el-button size="small" text @click="insertVariableTemplate('user')">
                  用户变量
                </el-button>
                <el-button size="small" text @click="insertVariableTemplate('token')">
                  Token
                </el-button>
                <el-button size="small" text @click="insertVariableTemplate('custom')">
                  自定义
                </el-button>
              </div>
            </div>
          </el-form-item>
        </el-card>
      </div>

      <!-- 执行预览摘要 -->
      <el-card class="summary-card" shadow="never">
        <template #header>
          <div class="summary-header">
            <el-icon><List /></el-icon>
            <span>执行预览</span>
          </div>
        </template>
        <div class="summary-content">
          <div class="summary-item">
            <span class="summary-label">执行目标：</span>
            <span class="summary-value">{{ targetTypeLabel }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">执行环境：</span>
            <el-tag size="small" :type="getEnvironmentTagType(formData.environment)">
              {{ formData.environment }}
            </el-tag>
          </div>
          <div class="summary-item">
            <span class="summary-label">超时设置：</span>
            <span class="summary-value">{{ formData.timeout }} 秒</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">执行方式：</span>
            <span class="summary-value">{{ formData.async ? '异步执行' : '同步执行' }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose" size="large">取消</el-button>
        <el-button
          type="primary"
          @click="handleExecute"
          :loading="executing"
          size="large"
          class="execute-btn"
        >
          <el-icon v-if="!executing"><VideoPlay /></el-icon>
          {{ executing ? '执行中...' : '开始执行' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { VideoPlay, Connection, Link, Filter, Timer, List } from '@element-plus/icons-vue'

interface Environment {
  label: string
  value: string
}

interface Props {
  modelValue: boolean
  targetType?: 'project' | 'module' | 'case'
  targetId?: number | null
  targetName?: string
  caseCount?: number
  projectId?: number | null
}

const props = withDefaults(defineProps<Props>(), {
  targetType: 'case',
  targetId: null,
  targetName: '',
  caseCount: 0,
  projectId: null
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  execute: [config: ExecuteConfig]
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const dialogTitle = computed(() => {
  if (props.targetType === 'project') return '执行项目测试'
  if (props.targetType === 'module') return '执行模块测试'
  return '执行测试'
})

const targetTypeLabel = computed(() => {
  if (props.targetType === 'project') return '项目级测试'
  if (props.targetType === 'module') return '模块级测试'
  return '用例测试'
})

const executing = ref(false)
const environmentList = ref<Environment[]>([
  { label: '开发环境', value: 'dev' },
  { label: '测试环境', value: 'test' },
  { label: '预发布环境', value: 'staging' },
  { label: '生产环境', value: 'prod' }
])
const availableTags = ref<string[]>(['冒烟测试', '回归测试', '功能测试', '缺陷验证', '性能测试'])

const formData = ref({
  environment: 'test',
  baseUrl: '',
  timeout: 30,
  async: false,
  concurrency: 3,
  executionOrder: 'dependency',
  priorityFilter: [] as string[],
  tagFilter: [] as string[],
  enabledOnly: true,
  variables: ''
})

export interface ExecuteConfig {
  environment: string
  baseUrl: string
  timeout: number
  async: boolean
  concurrency: number
  executionOrder: string
  priorityFilter: string[]
  tagFilter: string[]
  enabledOnly: boolean
  variables: Record<string, any>
  targetId: number | null
  targetType: string
}

function getEnvironmentTagType(env: string): string {
  const types: Record<string, string> = {
    dev: 'success',
    test: 'warning',
    staging: 'danger',
    prod: 'info'
  }
  return types[env] || 'info'
}

function insertVariableTemplate(type: string) {
  const templates: Record<string, string> = {
    user: '{"username": "testuser", "password": "Test@123"}',
    token: '{"token": "your-auth-token-here"}',
    custom: '{"key": "value"}'
  }
  formData.value.variables = templates[type] || ''
}

function handleClose() {
  visible.value = false
}

function handleExecute() {
  executing.value = true

  let parsedVariables: Record<string, any> = {}
  if (formData.value.variables) {
    try {
      parsedVariables = JSON.parse(formData.value.variables)
    } catch (e) {
      ElMessage.warning('执行变量格式错误，将被视为空')
      parsedVariables = {}
    }
  }

  const config: ExecuteConfig = {
    environment: formData.value.environment,
    baseUrl: formData.value.baseUrl,
    timeout: formData.value.timeout,
    async: formData.value.async,
    concurrency: formData.value.concurrency,
    executionOrder: formData.value.executionOrder,
    priorityFilter: formData.value.priorityFilter,
    tagFilter: formData.value.tagFilter,
    enabledOnly: formData.value.enabledOnly,
    variables: parsedVariables,
    targetId: props.targetId,
    targetType: props.targetType
  }

  emit('execute', config)

  // 模拟执行完成
  setTimeout(() => {
    executing.value = false
  }, 500)
}

watch(visible, (val) => {
  if (!val) {
    executing.value = false
  }
})
</script>

<style scoped lang="scss">
.execute-dialog-content {
  padding: 0;
}

.target-info-card {
  margin-bottom: 16px;
  :deep(.el-card__body) {
    padding: 16px 20px;
  }
}

.target-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.target-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #409eff, #67c23a);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.target-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.target-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 4px;
}

.target-desc {
  font-size: 13px;
  color: #909399;
}

.config-section {
  margin-bottom: 16px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.config-card {
  border: 1px solid #e4e7ed;
  :deep(.el-card__body) {
    padding: 16px 20px;
  }
}

.advanced-card {
  background: #fafafa;
}

.option-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.option-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;

  &.dev { background: #67c23a; }
  &.test { background: #e6a23c; }
  &.staging { background: #f56c6c; }
  &.prod { background: #909399; }
}

.url-preview {
  margin-top: 8px;
  font-size: 12px;
}

.preview-label {
  color: #909399;
}

.preview-url {
  color: #409eff;
}

.execution-mode-group {
  display: flex;
  gap: 16px;
}

.mode-option {
  display: flex;
  align-items: flex-start;
  padding: 12px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  margin-right: 0;

  &.is-checked {
    border-color: #409eff;
    background: #ecf5ff;
  }
}

.mode-content {
  margin-left: 8px;
}

.mode-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.mode-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.variables-header {
  width: 100%;
}

.variables-textarea {
  font-family: 'Monaco', 'Menlo', monospace;
}

.variable-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.summary-card {
  border: 1px solid #e4e7ed;
  background: #f5f7fa;
  :deep(.el-card__header) {
    padding: 12px 20px;
    background: #f0f0f0;
  }
  :deep(.el-card__body) {
    padding: 16px 20px;
  }
}

.summary-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
}

.summary-content {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.summary-label {
  color: #909399;
  font-size: 13px;
}

.summary-value {
  color: #303133;
  font-size: 13px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.execute-btn {
  display: flex;
  align-items: center;
  gap: 6px;
}

@media (max-width: 768px) {
  .summary-content {
    grid-template-columns: 1fr;
  }

  .execution-mode-group {
    flex-direction: column;
  }
}
</style>
