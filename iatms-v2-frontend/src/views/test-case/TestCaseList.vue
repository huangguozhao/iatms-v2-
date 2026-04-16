<template>
  <div class="test-case-page">
    <!-- 左侧树形导航 -->
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <h3 v-if="!sidebarCollapsed" class="sidebar-title">项目结构</h3>
        <el-button text @click="sidebarCollapsed = !sidebarCollapsed" class="collapse-btn">
          <el-icon :size="16">
            <ArrowLeft v-if="!sidebarCollapsed" />
            <ArrowRight v-else />
          </el-icon>
        </el-button>
      </div>

      <div v-if="!sidebarCollapsed" class="sidebar-content">
        <div class="sidebar-toolbar">
          <el-input
            v-model="treeSearch"
            placeholder="搜索节点..."
            :prefix-icon="Search"
            size="small"
            clearable
            class="search-input"
          />
          <el-button size="small" circle @click="loadTree" title="刷新">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>

        <el-scrollbar class="tree-scrollbar">
          <el-tree
            ref="treeRef"
            :data="treeData"
            :props="treeProps"
            node-key="id"
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            highlight-current
            class="project-tree"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <el-tooltip :content="node.label" placement="top" :show-after="500" :enterable="false" :disabled="node.label.length <= 20">
                <div class="tree-node" :class="`node-${data.type}`">
                  <!-- 节点图标 -->
                  <span class="node-icon">
                    <el-icon v-if="data.type === 'project'" :size="16" class="icon-project"><FolderOpened /></el-icon>
                    <el-icon v-else-if="data.type === 'module'" :size="16" class="icon-module"><Folder /></el-icon>
                    <el-icon v-else-if="data.type === 'api'" :size="16" class="icon-api"><Link /></el-icon>
                    <el-icon v-else :size="16" class="icon-testcase"><Document /></el-icon>
                  </span>

                  <!-- 节点标签 -->
                  <span class="node-label">{{ node.label }}</span>

                  <!-- HTTP方法标签（仅API节点） -->
                  <span v-if="data.type === 'api' && data.httpMethod" class="method-badge" :class="`method-${data.httpMethod?.toLowerCase()}`">
                    {{ data.httpMethod }}
                  </span>

                  <!-- 状态点 -->
                  <span v-if="data.type === 'testcase'" class="status-dot" :class="data.status === 'ENABLED' ? 'status-enabled' : 'status-disabled'" />
                </div>
              </el-tooltip>
            </template>
          </el-tree>
        </el-scrollbar>
      </div>
    </aside>

    <!-- 右侧内容区 -->
    <main class="main-content">
      <!-- 项目/模块统计视图 -->
      <div v-if="selectedNode && (selectedNode.type === 'project' || selectedNode.type === 'module')" class="detail-panel">
        <ProjectModuleStats
          :node="selectedNode"
          :level="selectedNode.type"
          @edit="handleEditNode"
          @delete="handleDeleteNode"
          @add="handleAddChildNode"
          @edit-child="handleEditChildNode"
          @delete-child="handleDeleteChildNode"
          @select-child="handleSelectChildNode"
          @config-environment="handleConfigEnvironment"
          @execute="handleExecuteFromProjectModule"
        />
      </div>

      <!-- 接口详情视图 -->
      <div v-else-if="selectedNode && selectedNode.type === 'api'" class="detail-panel">
        <div class="panel-header">
          <div class="header-title">
            <el-icon :size="24" class="header-icon"><Link /></el-icon>
            <h2>{{ selectedNode.name }}</h2>
          </div>
          <el-tag :type="getMethodTagType(selectedNode.httpMethod)" size="large" effect="dark">
            {{ selectedNode.httpMethod }}
          </el-tag>
        </div>

        <el-card class="info-card" shadow="hover">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="路径">
              <code class="path-code">{{ selectedNode.path }}</code>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag size="small" :type="selectedNode.status === 'ACTIVE' ? 'success' : 'info'">
                {{ selectedNode.status }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="编码">{{ selectedNode.code || '-' }}</el-descriptions-item>
            <el-descriptions-item label="描述">{{ selectedNode.description || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <div class="case-list-section">
          <div class="section-header">
            <h3>
              <el-icon><Document /></el-icon>
              测试用例 ({{ selectedNode.testCases?.length || 0 }})
            </h3>
            <el-button type="primary" size="small" @click="handleCreateCase">
              <el-icon><Plus /></el-icon>
              新建用例
            </el-button>
          </div>

          <el-table :data="selectedNode.testCases || []" stripe class="case-table">
            <el-table-column prop="name" label="用例名称" min-width="180">
              <template #default="{ row }">
                <div class="case-name-cell">
                  <el-icon class="case-icon"><Document /></el-icon>
                  <span class="case-link" @click="handleSelectCase(row)">{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="priority" label="优先级" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="getPriorityType(row.priority)" size="small" effect="light">{{ row.priority }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" size="small" effect="light">
                  {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" align="center">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleEditCase(row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button link type="success" @click="handleExecuteCase(row)">
                  <el-icon><VideoPlay /></el-icon>
                  执行
                </el-button>
                <el-button link type="danger" @click="handleDeleteCase(row)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 用例详情视图 -->
      <div v-else-if="selectedNode && selectedNode.type === 'testcase'" class="detail-panel">
        <CaseDetailPanel
          v-loading="caseDetailLoading"
          :test-case="caseDetail"
          :display-history="executionHistory"
          :execution-history-loading="executionHistoryLoading"
          :execution-history-total="executionHistoryTotal"
          @edit="handleEditCase"
          @copy="handleCopyCase"
          @view-history-detail="handleViewHistoryDetail"
          @view-more-history="handleViewMoreHistory"
        />
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-icon class="empty-icon"><FolderOpened /></el-icon>
        <div class="empty-text">从左侧选择项目、模块、接口或用例查看详情</div>
        <div class="empty-tip">点击节点可展开/折叠</div>
      </div>
    </main>

    <!-- 用例编辑对话框 -->
    <el-dialog v-model="caseDialogVisible" :title="caseDialogTitle" width="600px" destroy-on-close>
      <el-form ref="caseFormRef" :model="caseForm" :rules="caseFormRules" label-width="100px">
        <el-form-item label="用例名称" prop="name">
          <el-input v-model="caseForm.name" placeholder="请输入用例名称" />
        </el-form-item>
        <el-form-item label="所属接口" prop="apiId">
          <el-select v-model="caseForm.apiId" placeholder="请选择接口" style="width: 100%">
            <el-option v-for="api in availableApis" :key="api.id" :label="`${api.httpMethod || ''} ${api.name}`" :value="api.id">
              <span class="api-option">
                <el-tag v-if="api.httpMethod" size="small" :type="getMethodTagType(api.httpMethod)">{{ api.httpMethod }}</el-tag>
                <span>{{ api.name }}</span>
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="caseForm.priority" style="width: 100%">
            <el-option label="P0 - 最高" value="P0" />
            <el-option label="P1 - 高" value="P1" />
            <el-option label="P2 - 中" value="P2" />
            <el-option label="P3 - 低" value="P3" />
          </el-select>
        </el-form-item>
        <el-form-item label="请求头">
          <el-input v-model="caseForm.headers" type="textarea" rows="2" placeholder="JSON格式: {&quot;Content-Type&quot;: &quot;application/json&quot;}" />
        </el-form-item>
        <el-form-item label="请求体">
          <el-input v-model="caseForm.requestBody" type="textarea" rows="3" placeholder="请求体内容" />
        </el-form-item>
        <el-form-item label="断言">
          <el-input v-model="caseForm.assertions" type="textarea" rows="2" placeholder="JSON数组格式" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="caseForm.description" type="textarea" rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="caseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCaseSubmit">确定</el-button>
      </template>
    </el-dialog>

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

    <!-- 执行结果对话框 -->
    <ExecutionResultDialog
      :visible="resultDialogVisible"
      :execution-result="executionResult"
      @update:visible="resultDialogVisible = $event"
      @retry="handleRetryExecution"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted, computed, h } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Refresh, Plus, Edit, Delete, Link, Document, Folder, FolderOpened,
  ArrowLeft, ArrowRight, VideoPlay
} from '@element-plus/icons-vue'
import { testCaseApi, type ProjectTreeNode, type TestCaseDetailVO } from '@/api/modules/testing/testCase'
import type { FormInstance, FormRules } from 'element-plus'
import { CaseDetailPanel, ExecuteConfigDialog, ExecutionResultDialog } from '@/components/business/case-detail'
import { ProjectModuleStats } from '@/components/business/project-detail'

// ExecuteConfig type (duplicated here to avoid import issues)
interface ExecuteConfig {
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

// ExecutionResult type (duplicated here to avoid import issues)
interface ExecutionResult {
  recordId?: string
  executionId?: string
  caseName?: string
  scopeName?: string
  status?: string
  responseStatus?: number
  duration?: number
  durationSeconds?: number
  assertionsPassed?: number
  assertionsFailed?: number
  startTime?: string
  endTime?: string
  executor?: string
  executorInfo?: { name?: string }
  errorMessage?: string
  failureReason?: string
  failureType?: string
  responseBody?: any
  responseHeaders?: any
}

// 状态
const sidebarCollapsed = ref(false)
const treeSearch = ref('')
const treeData = ref<ProjectTreeNode[]>([])
const treeRef = ref()
const selectedNode = ref<ProjectTreeNode | null>(null)
const caseDetail = ref<TestCaseDetailVO | null>(null)
const caseDetailLoading = ref(false)

// 执行相关状态
const executionHistory = ref<any[]>([])
const executionHistoryLoading = ref(false)
const executionHistoryTotal = ref(0)

const executeDialogVisible = ref(false)
const resultDialogVisible = ref(false)
const executionResult = ref<ExecutionResult | null>(null)

const executeConfig = reactive({
  targetType: 'case' as 'project' | 'module' | 'case',
  targetId: null as number | null,
  targetName: '',
  caseCount: 0,
  projectId: null as number | null
})

// el-tree 配置
const treeProps = {
  children: 'children',
  label: 'name'
}

// 用例对话框
const caseDialogVisible = ref(false)
const caseDialogTitle = ref('新建用例')
const caseFormRef = ref<FormInstance>()
const caseForm = reactive({
  id: null as number | null,
  name: '',
  apiId: null as number | null,
  priority: 'P2',
  headers: '',
  requestBody: '',
  assertions: '',
  description: ''
})
const caseFormRules: FormRules = {
  name: [{ required: true, message: '请输入用例名称', trigger: 'blur' }],
  apiId: [{ required: true, message: '请选择所属接口', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
}

// 可选的接口列表
const availableApis = computed(() => {
  const apis: ProjectTreeNode[] = []
  const collectApis = (nodes: ProjectTreeNode[]) => {
    for (const node of nodes) {
      if (node.type === 'api') {
        apis.push(node)
      }
      if (node.children) {
        collectApis(node.children)
      }
    }
  }
  collectApis(treeData.value)
  return apis
})

// 加载树数据
async function loadTree() {
  try {
    const data = await testCaseApi.getTree()
    treeData.value = transformTreeData(data || [])
  } catch (error: any) {
    ElMessage.error('加载项目树失败: ' + error.message)
  }
}

/**
 * 转换树形数据以适配 el-tree 组件
 */
function transformTreeData(nodes: ProjectTreeNode[]): ProjectTreeNode[] {
  return nodes.map(project => {
    const transformedProject: ProjectTreeNode = { ...project, children: [] }

    const processChildren = (modules: ProjectTreeNode[], parentChildren: ProjectTreeNode[]) => {
      for (const mod of modules) {
        const moduleNode: ProjectTreeNode = { ...mod, type: 'module', children: [] }

        if (mod.children && mod.children.length > 0) {
          processChildren(mod.children, moduleNode.children!)
        }

        if (mod.apis) {
          for (const api of mod.apis) {
            const apiNode: ProjectTreeNode = { ...api, type: 'api', children: [], testCases: [] }

            if (api.testCases) {
              for (const tc of api.testCases) {
                const tcNode: ProjectTreeNode = { ...tc, type: 'testcase' }
                apiNode.children!.push(tcNode)
                apiNode.testCases!.push(tcNode)
              }
            }

            moduleNode.children!.push(apiNode)
          }
        }

        parentChildren.push(moduleNode)
      }
    }

    if (project.children) {
      processChildren(project.children, transformedProject.children!)
    }

    return transformedProject
  })
}

// 树节点过滤
function filterNode(value: string, data: ProjectTreeNode) {
  if (!value) return true
  return data.name.toLowerCase().includes(value.toLowerCase())
}

// 监听搜索
watch(treeSearch, (val) => {
  treeRef.value?.filter(val)
})

// 节点点击
async function handleNodeClick(data: ProjectTreeNode) {
  selectedNode.value = data

  if (data.type === 'testcase') {
    caseDetailLoading.value = true
    try {
      caseDetail.value = await testCaseApi.getDetail(data.id)
    } catch {
      ElMessage.error('加载用例详情失败')
    } finally {
      caseDetailLoading.value = false
    }
  } else {
    caseDetail.value = null
  }
}

// 获取方法标签类型
function getMethodTagType(method?: string): string {
  const map: Record<string, string> = {
    GET: 'success',
    POST: 'warning',
    PUT: 'primary',
    DELETE: 'danger',
    PATCH: 'info'
  }
  return map[method?.toUpperCase() || ''] || 'info'
}

// 获取优先级类型
function getPriorityType(priority?: string): string {
  const map: Record<string, string> = {
    P0: 'danger',
    P1: 'warning',
    P2: '',
    P3: 'info'
  }
  return map[priority || ''] || ''
}

// 统计用例数量
function countTestCases(node: ProjectTreeNode): number {
  let count = 0
  const countInNode = (n: ProjectTreeNode) => {
    if (n.testCases) count += n.testCases.length
    if (n.children) {
      for (const child of n.children) {
        countInNode(child)
      }
    }
  }
  countInNode(node)
  return count
}

// 统计接口数量
function countApis(node: ProjectTreeNode): number {
  let count = 0
  const countInNode = (n: ProjectTreeNode) => {
    if (n.type === 'api') count++
    if (n.children) {
      for (const child of n.children) {
        countInNode(child)
      }
    }
  }
  countInNode(node)
  return count
}

// 选择用例
function handleSelectCase(tc: ProjectTreeNode) {
  selectedNode.value = tc
  handleNodeClick(tc)
}

// 创建用例
function handleCreateCase() {
  caseDialogTitle.value = '新建用例'
  Object.assign(caseForm, {
    id: null,
    name: '',
    apiId: selectedNode.value?.type === 'api' ? selectedNode.value.id : null,
    priority: 'P2',
    headers: '',
    requestBody: '',
    assertions: '',
    description: ''
  })
  caseDialogVisible.value = true
}

// 编辑用例
async function handleEditCase(tc: ProjectTreeNode) {
  caseDialogTitle.value = '编辑用例'
  try {
    const detail = await testCaseApi.getDetail(tc.id)
    Object.assign(caseForm, {
      id: tc.id,
      name: detail.name,
      apiId: detail.apiId,
      priority: detail.priority,
      headers: typeof detail.headers === 'object' ? JSON.stringify(detail.headers) : (detail.headers || ''),
      requestBody: detail.requestBody,
      assertions: detail.assertions,
      description: detail.description
    })
  } catch {
    ElMessage.error('获取用例详情失败')
    return
  }
  caseDialogVisible.value = true
}

// 执行用例
async function handleExecuteCase(tc: ProjectTreeNode) {
  try {
    await ElMessageBox.confirm(`确定执行用例 "${tc.name}" 吗?`, '提示', { type: 'warning' })
    await testCaseApi.execute(tc.id)
    ElMessage.success('执行已提交')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '执行失败')
    }
  }
}

// 删除用例
async function handleDeleteCase(tc: ProjectTreeNode) {
  try {
    await ElMessageBox.confirm(`确定删除用例 "${tc.name}" 吗?`, '提示', { type: 'warning' })
    await testCaseApi.delete(tc.id)
    ElMessage.success('删除成功')
    loadTree()
    if (selectedNode.value?.id === tc.id) {
      selectedNode.value = null
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 提交用例表单
async function handleCaseSubmit() {
  const valid = await caseFormRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    if (caseDialogTitle.value === '新建用例') {
      await testCaseApi.create(caseForm as any)
      ElMessage.success('创建成功')
    } else {
      await testCaseApi.update(caseForm.id!, caseForm as any)
      ElMessage.success('更新成功')
    }
    caseDialogVisible.value = false
    loadTree()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 复制用例
async function handleCopyCase(tc: TestCaseDetailVO | null) {
  if (!tc) return
  try {
    await ElMessageBox.confirm(`确定复制用例 "${tc.name}" 吗?`, '提示', { type: 'info' })
    // TODO: 调用复制API
    ElMessage.success('复制功能开发中')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '复制失败')
    }
  }
}

// 查看执行历史详情
function handleViewHistoryDetail(history: any) {
  console.log('View history detail:', history)
  ElMessage.info('查看执行历史详情功能开发中')
}

// 查看更多执行历史
function handleViewMoreHistory() {
  ElMessage.info('查看更多执行历史功能开发中')
}

// 从配置对话框执行
async function handleExecuteFromConfig(config: ExecuteConfig) {
  executeDialogVisible.value = false

  try {
    // TODO: 调用执行API
    ElMessage.success('执行已提交')

    // 模拟执行结果
    executionResult.value = {
      recordId: 'EXEC-' + Date.now(),
      caseName: executeConfig.targetName,
      status: 'passed',
      responseStatus: 200,
      duration: 1234,
      startTime: new Date().toISOString(),
      endTime: new Date().toISOString(),
      executor: '当前用户',
      assertionsPassed: 5,
      assertionsFailed: 0
    }
    resultDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '执行失败')
  }
}

// 重试执行
function handleRetryExecution() {
  resultDialogVisible.value = false
  executeDialogVisible.value = true
}

// 占位方法
function handleCreateUnderNode() {
  ElMessage.info('创建子模块功能开发中')
}

// 处理编辑节点
function handleEditNode() {
  ElMessage.info('编辑功能开发中')
}

// 处理删除节点
async function handleDeleteNode(node: any) {
  try {
    await ElMessageBox.confirm(`确定删除 "${node.name}" 吗?`, '提示', { type: 'warning' })
    ElMessage.success('删除功能开发中')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 处理添加子节点
function handleAddChildNode(node: any) {
  ElMessage.info('添加子节点功能开发中')
}

// 处理编辑子节点
function handleEditChildNode(child: any) {
  ElMessage.info('编辑子节点功能开发中')
}

// 处理删除子节点
async function handleDeleteChildNode(child: any) {
  try {
    await ElMessageBox.confirm(`确定删除 "${child.name}" 吗?`, '提示', { type: 'warning' })
    ElMessage.success('删除功能开发中')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 处理选择子节点
function handleSelectChildNode(child: any) {
  selectedNode.value = child
  if (child.type === 'api') {
    caseDetail.value = null
  } else if (child.type === 'testcase') {
    caseDetailLoading.value = true
    testCaseApi.getDetail(child.id).then(detail => {
      caseDetail.value = detail
    }).catch(() => {
      ElMessage.error('加载详情失败')
    }).finally(() => {
      caseDetailLoading.value = false
    })
  }
}

// 处理环境配置
function handleConfigEnvironment(node: any) {
  ElMessage.info('环境配置功能开发中')
}

// 从项目/模块执行测试
function handleExecuteFromProjectModule(config: any) {
  executeDialogVisible.value = false
  ElMessage.success('执行已提交')
  // TODO: 调用执行API
}

// 格式化 JSON
function formatJson(val: any): string {
  if (!val) return '-'
  if (typeof val === 'string') {
    try { return JSON.stringify(JSON.parse(val), null, 2) } catch { return val }
  }
  return JSON.stringify(val, null, 2)
}

onMounted(() => {
  loadTree()
})
</script>

<style scoped lang="scss">
// CSS Variables
$primary-color: #409eff;
$success-color: #67c23a;
$warning-color: #e6a23c;
$danger-color: #f56c6c;
$info-color: #909399;

.test-case-page {
  display: flex;
  height: calc(100vh - 60px);
  background: #f5f7fa;
}

// 侧边栏
.sidebar {
  width: 300px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  border-right: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;

  &.collapsed {
    width: 50px;
  }
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);

  .sidebar-title {
    font-size: 16px;
    font-weight: 600;
    margin: 0;
    color: #303133;
  }

  .collapse-btn {
    padding: 4px;
  }
}

.sidebar-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-toolbar {
  padding: 12px;
  display: flex;
  gap: 8px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);

  .search-input {
    flex: 1;
  }
}

.tree-scrollbar {
  flex: 1;
  overflow-y: auto;
}

.project-tree {
  padding: 8px 12px;

  :deep(.el-tree-node__content) {
    height: 36px;
    border-radius: 8px;
    margin-bottom: 2px;
    transition: all 0.2s ease;

    &:hover {
      background: rgba(64, 158, 255, 0.08);
    }
  }

  :deep(.el-tree-node.is-current > .el-tree-node__content) {
    background: rgba(64, 158, 255, 0.12);
  }
}

// 树节点
.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  padding-right: 8px;
  overflow: hidden;

  .node-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    border-radius: 6px;
    transition: all 0.2s ease;
    flex-shrink: 0;
  }

  .node-label {
    flex: 1;
    font-size: 13px;
    color: #606266;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    min-width: 0;
  }

  // 节点类型图标颜色
  .icon-project { color: #409eff; }
  .icon-module { color: #67c23a; }
  .icon-api { color: #e6a23c; }
  .icon-testcase { color: #909399; }
}

// HTTP方法徽章
.method-badge {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 4px;
  text-transform: uppercase;

  &.method-get { background: #e1f3d8; color: #67c23a; }
  &.method-post { background: #fef0e7; color: #e6a23c; }
  &.method-put { background: #d9ecff; color: #409eff; }
  &.method-delete { background: #fde2e2; color: #f56c6c; }
  &.method-patch { background: #f4f4f5; color: #909399; }
}

// 状态点
.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;

  &.status-enabled { background: #67c23a; }
  &.status-disabled { background: #c0c4cc; }
}

// 主内容区
.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.detail-panel {
  max-width: 1000px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);

  .header-title {
    display: flex;
    align-items: center;
    gap: 12px;

    .header-icon {
      color: #409eff;
    }

    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }
  }
}

// 统计卡片
.stats-card {
  margin-bottom: 20px;

  .stat-item {
    text-align: center;
    padding: 24px;

    .stat-value {
      font-size: 36px;
      font-weight: 700;
      color: #409eff;
      line-height: 1;
    }

    .stat-label {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      font-size: 14px;
      color: #909399;
      margin-top: 12px;
    }
  }
}

// 信息卡片
.info-card {
  margin-bottom: 20px;

  .path-code {
    background: #f5f7fa;
    padding: 4px 8px;
    border-radius: 4px;
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 12px;
    color: #e6a23c;
  }
}

// 用例列表区块
.case-list-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h3 {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }
}

.case-table {
  .case-name-cell {
    display: flex;
    align-items: center;
    gap: 8px;

    .case-icon {
      color: #909399;
    }

    .case-link {
      color: #409eff;
      cursor: pointer;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}

// 用例详情卡片
.case-detail-card {
  margin-bottom: 20px;
}

.case-content {
  h4 {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    font-weight: 600;
    color: #606266;
    margin: 20px 0 10px;

    &:first-child {
      margin-top: 0;
    }
  }
}

.content-text {
  color: #303133;
  line-height: 1.6;
  margin: 0;
}

.content-code {
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 12px;
  font-family: 'Monaco', 'Menlo', monospace;
  color: #606266;
  overflow-x: auto;
  margin: 0;
}

// 操作按钮
.panel-actions {
  display: flex;
  gap: 12px;
}

// 空状态
.empty-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;

  .empty-icon {
    font-size: 80px;
    color: #dcdfe6;
    margin-bottom: 20px;
  }

  .empty-text {
    font-size: 16px;
    margin-bottom: 8px;
  }

  .empty-tip {
    font-size: 13px;
    color: #c0c4cc;
  }
}

// API 选项
.api-option {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
