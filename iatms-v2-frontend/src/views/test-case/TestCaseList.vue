<template>
  <div class="test-case-page">
    <!-- 左侧树形导航 -->
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <h3 v-if="!sidebarCollapsed" class="sidebar-title">项目结构</h3>
        <el-button text @click="sidebarCollapsed = !sidebarCollapsed">
          {{ sidebarCollapsed ? '»' : '«' }}
        </el-button>
      </div>

      <div v-if="!sidebarCollapsed" class="sidebar-content">
        <div class="sidebar-toolbar">
          <el-input v-model="treeSearch" placeholder="搜索..." size="small" clearable />
          <el-button size="small" @click="loadTree">刷新</el-button>
        </div>

        <el-tree
          ref="treeRef"
          :data="treeData"
          :props="{ children: 'children', label: 'name' }"
          node-key="id"
          :expand-on-click-node="false"
          :filter-node-method="filterNode"
          highlight-current
          class="project-tree"
          @node-click="handleNodeClick"
        >
          <template #default="{ node, data }">
            <span class="tree-node">
              <span class="node-icon">{{ getNodeIcon(data) }}</span>
              <span class="node-label">{{ node.label }}</span>
              <span v-if="data.type === 'api'" class="node-method" :class="`method-${data.httpMethod?.toLowerCase()}`">
                {{ data.httpMethod }}
              </span>
            </span>
          </template>
        </el-tree>
      </div>
    </aside>

    <!-- 右侧内容区 -->
    <main class="main-content">
      <!-- 项目/模块统计视图 -->
      <div v-if="selectedNode && (selectedNode.type === 'project' || selectedNode.type === 'module')" class="detail-panel">
        <div class="panel-header">
          <h2>{{ selectedNode.name }}</h2>
          <el-tag :type="selectedNode.type === 'project' ? 'primary' : 'success'" size="large">
            {{ selectedNode.type === 'project' ? '项目' : '模块' }}
          </el-tag>
        </div>

        <el-card class="stats-card">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-value">{{ selectedNode.stats?.moduleCount || 0 }}</div>
                <div class="stat-label">子模块</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-value">{{ selectedNode.stats?.apiCount || selectedNode.apis?.length || 0 }}</div>
                <div class="stat-label">接口</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-value">{{ selectedNode.stats?.testCaseCount || countTestCases(selectedNode) }}</div>
                <div class="stat-label">用例</div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <div class="panel-actions">
          <el-button type="primary" @click="handleCreateUnderNode">新建子模块</el-button>
          <el-button @click="handleEditNode">编辑</el-button>
        </div>
      </div>

      <!-- 接口详情视图 -->
      <div v-else-if="selectedNode && selectedNode.type === 'api'" class="detail-panel">
        <div class="panel-header">
          <h2>{{ selectedNode.name }}</h2>
          <el-tag :type="getMethodTagType(selectedNode.httpMethod)">{{ selectedNode.httpMethod }}</el-tag>
        </div>

        <el-card class="info-card">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="路径">{{ selectedNode.path }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag size="small">{{ selectedNode.status }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="编码">{{ selectedNode.code }}</el-descriptions-item>
            <el-descriptions-item label="描述">{{ selectedNode.description || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <div class="case-list-section">
          <div class="section-header">
            <h3>测试用例 ({{ selectedNode.testCases?.length || 0 }})</h3>
            <el-button type="primary" size="small" @click="handleCreateCase">新建用例</el-button>
          </div>

          <el-table :data="selectedNode.testCases || []" stripe>
            <el-table-column prop="name" label="用例名称" min-width="150">
              <template #default="{ row }">
                <span class="case-link" @click="handleSelectCase(row)">{{ row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="priority" label="优先级" width="80">
              <template #default="{ row }">
                <el-tag :type="getPriorityType(row.priority)" size="small">{{ row.priority }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" size="small">
                  {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleEditCase(row)">编辑</el-button>
                <el-button link type="primary" @click="handleExecuteCase(row)">执行</el-button>
                <el-button link type="danger" @click="handleDeleteCase(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 用例详情视图 -->
      <div v-else-if="selectedNode && selectedNode.type === 'testcase'" class="detail-panel">
        <div class="panel-header">
          <h2>{{ selectedNode.name }}</h2>
          <el-tag :type="getPriorityType(selectedNode.priority)">{{ selectedNode.priority }}</el-tag>
        </div>

        <el-card v-loading="caseDetailLoading" class="case-detail-card">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用例编码">{{ caseDetail?.caseCode || selectedNode.code }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="selectedNode.status === 'ENABLED' ? 'success' : 'info'" size="small">
                {{ selectedNode.status === 'ENABLED' ? '启用' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ caseDetail?.createdAt || '-' }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ caseDetail?.updatedAt || '-' }}</el-descriptions-item>
          </el-descriptions>

          <el-divider />

          <div class="case-content">
            <h4>描述</h4>
            <p class="content-text">{{ caseDetail?.description || selectedNode.description || '暂无描述' }}</p>

            <h4>请求头</h4>
            <pre class="content-code">{{ formatJson(caseDetail?.headers) }}</pre>

            <h4>请求体</h4>
            <pre class="content-code">{{ caseDetail?.requestBody || selectedNode.requestBody || '暂无请求体' }}</pre>

            <h4>断言</h4>
            <pre class="content-code">{{ caseDetail?.assertions || '暂无断言' }}</pre>
          </div>
        </el-card>

        <div class="panel-actions">
          <el-button type="primary" @click="handleEditCase(selectedNode)">编辑用例</el-button>
          <el-button type="success" @click="handleExecuteCase(selectedNode)">执行用例</el-button>
          <el-button type="danger" @click="handleDeleteCase(selectedNode)">删除用例</el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-icon">📋</div>
        <div class="empty-text">从左侧选择项目、模块、接口或用例查看详情</div>
      </div>
    </main>

    <!-- 用例编辑对话框 -->
    <el-dialog v-model="caseDialogVisible" :title="caseDialogTitle" width="600px">
      <el-form ref="caseFormRef" :model="caseForm" :rules="caseFormRules" label-width="100px">
        <el-form-item label="用例名称" prop="name">
          <el-input v-model="caseForm.name" placeholder="请输入用例名称" />
        </el-form-item>
        <el-form-item label="所属接口" prop="apiId">
          <el-select v-model="caseForm.apiId" placeholder="请选择接口" style="width: 100%">
            <el-option v-for="api in availableApis" :key="api.id" :label="api.name" :value="api.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="caseForm.priority" style="width: 100%">
            <el-option label="P0" value="P0" />
            <el-option label="P1" value="P1" />
            <el-option label="P2" value="P2" />
            <el-option label="P3" value="P3" />
          </el-select>
        </el-form-item>
        <el-form-item label="请求头">
          <el-input v-model="caseForm.headers" type="textarea" rows="2" placeholder="JSON格式" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { testCaseApi, type ProjectTreeNode, type TestCaseDetailVO } from '@/api/modules/testing/testCase'
import type { FormInstance, FormRules } from 'element-plus'

// 状态
const sidebarCollapsed = ref(false)
const treeSearch = ref('')
const treeData = ref<ProjectTreeNode[]>([])
const treeRef = ref()
const selectedNode = ref<ProjectTreeNode | null>(null)
const caseDetail = ref<TestCaseDetailVO | null>(null)
const caseDetailLoading = ref(false)

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

// 可选的接口列表（用于创建用例时选择）
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
      if (node.apis) {
        apis.push(...node.apis)
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
    treeData.value = data || []
  } catch (error: any) {
    ElMessage.error('加载项目树失败: ' + error.message)
  }
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
    } catch (error: any) {
      ElMessage.error('加载用例详情失败')
    } finally {
      caseDetailLoading.value = false
    }
  } else {
    caseDetail.value = null
  }
}

// 获取节点图标
function getNodeIcon(node: ProjectTreeNode): string {
  switch (node.type) {
    case 'project': return '📁'
    case 'module': return '📂'
    case 'api': return '🔗'
    case 'testcase': return '📝'
    default: return '📄'
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
    P2: 'primary',
    P3: 'info'
  }
  return map[priority || ''] || 'info'
}

// 统计用例数量
function countTestCases(node: ProjectTreeNode): number {
  let count = 0
  const countInNode = (n: ProjectTreeNode) => {
    if (n.testCases) count += n.testCases.length
    if (n.apis) {
      for (const api of n.apis) {
        if (api.testCases) count += api.testCases.length
      }
    }
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

// 占位方法（后续完善）
function handleCreateUnderNode() {
  ElMessage.info('创建子模块功能开发中')
}

function handleEditNode() {
  ElMessage.info('编辑功能开发中')
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
.test-case-page {
  display: flex;
  height: calc(100vh - 60px);
  background: #f5f7fa;
}

.sidebar {
  width: 280px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  border-right: 1px solid rgba(0, 0, 0, 0.08);
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
  }
}

.sidebar-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.sidebar-toolbar {
  padding: 12px;
  display: flex;
  gap: 8px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.project-tree {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;

  .node-icon {
    font-size: 14px;
  }

  .node-label {
    flex: 1;
    font-size: 13px;
  }

  .node-method {
    font-size: 10px;
    padding: 2px 4px;
    border-radius: 3px;
    font-weight: 600;

    &.method-get { background: #e1f3d8; color: #67c23a; }
    &.method-post { background: #fef0e7; color: #e6a23c; }
    &.method-put { background: #d9ecff; color: #409eff; }
    &.method-delete { background: #fde2e2; color: #f56c6c; }
    &.method-patch { background: #f4f4f5; color: #909399; }
  }
}

.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.detail-panel {
  max-width: 1000px;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 20px;
  }
}

.stats-card {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;

  .stat-value {
    font-size: 32px;
    font-weight: 700;
    color: #409eff;
  }

  .stat-label {
    font-size: 14px;
    color: #909399;
    margin-top: 8px;
  }
}

.info-card {
  margin-bottom: 20px;
}

.case-list-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h3 {
      margin: 0;
      font-size: 16px;
    }
  }
}

.case-link {
  color: #409eff;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

.case-detail-card {
  margin-bottom: 20px;
}

.case-content {
  h4 {
    font-size: 14px;
    color: #606266;
    margin: 16px 0 8px;

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
  padding: 12px;
  border-radius: 6px;
  font-size: 12px;
  color: #606266;
  overflow-x: auto;
  margin: 0;
}

.panel-actions {
  display: flex;
  gap: 12px;
}

.empty-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;

  .empty-icon {
    font-size: 64px;
    margin-bottom: 16px;
  }

  .empty-text {
    font-size: 16px;
  }
}
</style>
