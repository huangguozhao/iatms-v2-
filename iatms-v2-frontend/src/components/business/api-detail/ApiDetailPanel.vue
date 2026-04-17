<template>
  <div class="api-detail-panel">
    <!-- 头部 -->
    <DetailHeader
      :api-data="apiData"
      :saving="saving"
      @save="handleSave"
      @execute="handleExecute"
      @refresh="$emit('refresh')"
    />

    <!-- 标签页 -->
    <div class="detail-tabs">
      <div
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-item"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </div>
    </div>

    <!-- 内容区 -->
    <div class="detail-content">
      <ApiBasicForm
        v-show="activeTab === 'basic'"
        :api-data="apiData"
        :available-projects="availableProjects"
        :projects-loading="projectsLoading"
        :available-modules="availableModules"
        :modules-loading="modulesLoading"
        @project-change="handleProjectChange"
        @save="handleSave"
        @test="handleExecute"
        @delete="handleDelete"
      />

      <ApiParamsEditor
        v-show="activeTab === 'params'"
        :header-params="requestParams.headerParams"
        :query-params="requestParams.queryParams"
        :body-params="requestParams.bodyParams"
        :form-data-params="requestParams.formDataParams"
        :raw-body="requestParams.rawBody"
        :body-type="requestParams.bodyType"
        @update:header-params="requestParams.headerParams = $event"
        @update:query-params="requestParams.queryParams = $event"
        @update:body-params="requestParams.bodyParams = $event"
        @update:form-data-params="requestParams.formDataParams = $event"
        @update:raw-body="requestParams.rawBody = $event"
        @update:body-type="requestParams.bodyType = $event"
      />

      <ApiResponseResult
        v-show="activeTab === 'result'"
        :api="api"
        :execution-result="executionResult"
      />

      <ApiTestHistory
        v-show="activeTab === 'history'"
        :api="api"
      />

      <ApiRelatedCases
        v-show="activeTab === 'cases'"
        :api="api"
        :project-id="apiData.projectId"
        :module-id="apiData.moduleId"
        :related-cases="testCases"
        @select-case="$emit('select-case', $event)"
        @created="handleCaseCreated"
      />
    </div>

    <!-- 执行配置对话框 -->
    <ExecuteConfigDialog
      v-model="executeDialogVisible"
      :target-type="executeConfig.targetType"
      :target-id="executeConfig.targetId"
      :target-name="executeConfig.targetName"
      :case-count="executeConfig.caseCount"
      :project-id="executeConfig.projectId"
      @execute="handleExecuteFromConfig"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { ProjectTreeNode } from '@/api/modules/testing/testCase'
import { apiApi } from '@/api/modules/testing/api'
import { useApiData, useProjectModules } from '@/composables'
import ApiBasicForm from './ApiBasicForm.vue'
import ApiParamsEditor from './ApiParamsEditor.vue'
import ApiResponseResult from './ApiResponseResult.vue'
import ApiTestHistory from './ApiTestHistory.vue'
import ApiRelatedCases from './ApiRelatedCases.vue'
import DetailHeader from './DetailHeader.vue'
import { ExecuteConfigDialog } from '@/components/business/case-detail'

interface Props {
  api: ProjectTreeNode | null
}

const props = defineProps<Props>()

const emit = defineEmits<{
  execute: [api: ProjectTreeNode]
  refresh: []
  'select-case': [tc: ProjectTreeNode]
}>()

// Composables
const { apiData, requestParams, loading, saving, syncFromNode, loadApiDetail, reset, saveApi } = useApiData()
const {
  availableProjects,
  projectsLoading,
  availableModules,
  modulesLoading,
  loadProjects,
  loadModules
} = useProjectModules()

// 状态
const activeTab = ref('basic')
const executeDialogVisible = ref(false)
const executionResult = ref<any>(null)

const executeConfig = reactive({
  targetType: 'api' as 'project' | 'module' | 'api' | 'case',
  targetId: null as number | null,
  targetName: '',
  caseCount: 0,
  projectId: null as number | null
})

// 计算属性
const testCases = computed(() => props.api?.testCases || [])

// 标签页配置
const tabs = [
  { key: 'basic', label: '基本信息' },
  { key: 'params', label: '请求参数' },
  { key: 'result', label: '最近结果' },
  { key: 'history', label: '测试历史' },
  { key: 'cases', label: `相关用例 (${testCases.value.length})` }
]

// 监听 API 变化
watch(
  () => props.api,
  async (newApi) => {
    if (newApi) {
      // 先同步基本信息（从树节点）
      syncFromNode(newApi)
      // 加载完整的 API 详情（从后端）
      if (newApi.id) {
        try {
          await loadApiDetail(newApi.id)
        } catch (e) {
          console.error('加载 API 详情失败', e)
        }
      }
      // 加载项目和模块列表
      loadProjects()
      if (apiData.projectId) {
        loadModules(apiData.projectId)
      }
      // 加载最新执行结果
      loadLatestExecution()
    } else {
      reset()
      executionResult.value = null
    }
  },
  { immediate: true }
)

// 加载最新执行结果
async function loadLatestExecution() {
  if (!props.api?.id) return
  try {
    const result = await apiApi.getLatestExecution(props.api.id)
    if (result.hasResult) {
      executionResult.value = {
        status: result.status,
        responseTime: result.duration,
        testTime: result.startTime,
        statusCode: result.statusCode || '-',
        body: result.body,
        assertions: result.assertions || [],
        headers: result.headers || []
      }
    } else {
      executionResult.value = null
    }
  } catch (e) {
    console.error('加载最新执行结果失败', e)
    executionResult.value = null
  }
}

// 监听测试用例数量变化，更新标签页
watch(testCases, (cases) => {
  const casesTab = tabs.find(t => t.key === 'cases')
  if (casesTab) {
    casesTab.label = `相关用例 (${cases.length})`
  }
})

// 项目变化
function handleProjectChange(projectId: number) {
  apiData.projectId = projectId
  loadModules(projectId)
}

// 保存
async function handleSave() {
  try {
    await saveApi()
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

// 删除
async function handleDelete() {
  // TODO: 调用删除API
  ElMessage.info('删除功能开发中')
}

// 执行测试
function handleExecute() {
  if (props.api) {
    executeConfig.targetType = 'api'
    executeConfig.targetId = props.api.id
    executeConfig.targetName = props.api.name
    executeConfig.caseCount = props.api.testCases?.length || 0
    executeConfig.projectId = apiData.projectId
    executeDialogVisible.value = true
  }
}

// 从配置执行
async function handleExecuteFromConfig(config: any) {
  executeDialogVisible.value = false
  ElMessage.success('执行已提交')
}

// 用例创建成功后
function handleCaseCreated() {
  // 可以刷新列表等
}
</script>

<style scoped lang="scss">
.api-detail-panel {
  height: 100%;
  background: white;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.detail-tabs {
  display: flex;
  gap: 0;
  padding: 0 24px;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.tab-item {
  padding: 12px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s ease;

  &:hover {
    color: #303133;
  }

  &.active {
    color: #409eff;
    border-bottom-color: #409eff;
  }
}

.detail-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}
</style>
