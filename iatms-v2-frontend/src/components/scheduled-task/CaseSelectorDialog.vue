<template>
  <el-dialog
    :model-value="visible"
    title="选择测试用例"
    width="900px"
    :close-on-click-modal="false"
    @open="handleOpen"
    @close="handleClose"
  >
    <div class="case-selector">
      <div class="selector-header">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用例名称"
          clearable
          style="width: 300px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <div class="stats">
          已选择 {{ selectedCases.length }} 个用例
        </div>
      </div>

      <div class="tree-container" v-loading="loading">
        <div class="module-list">
          <div
            v-for="module in filteredTreeData"
            :key="module.id"
            class="module-item"
          >
            <div class="module-header" @click="toggleModuleExpand(module.id)">
              <el-icon>
                <ArrowRight v-if="!module.expanded" />
                <ArrowDown v-else />
              </el-icon>
              <el-icon><Folder /></el-icon>
              <span class="module-name">{{ module.name }}</span>
              <el-tag size="small" type="info">{{ module.apiCount }} 个接口</el-tag>
              <el-button
                v-if="module.expanded"
                type="primary"
                size="small"
                link
                @click.stop="handleModuleBatchSelect(module.id)"
              >
                {{ isModuleAllSelected(module.id) ? '取消' : '全选' }}
              </el-button>
            </div>

            <div v-if="module.expanded" class="api-list">
              <div
                v-for="api in module.apis"
                :key="api.id"
                class="api-item"
              >
                <div class="api-header" @click.stop="toggleApiCases(api.id, module.id)">
                  <el-icon>
                    <ArrowRight v-if="!api.expanded" />
                    <ArrowDown v-else />
                  </el-icon>
                  <el-tag :type="getMethodType(api.method)" size="small">
                    {{ api.method }}
                  </el-tag>
                  <span class="api-name">{{ api.name }}</span>
                  <span class="api-path">{{ api.path }}</span>
                  <el-tag size="small" type="info">{{ api.caseCount }} 个用例</el-tag>
                  <el-button
                    v-if="api.expanded && api.cases && api.cases.length > 0"
                    type="success"
                    size="small"
                    link
                    @click.stop="handleApiBatchSelect(api.id, module.id)"
                  >
                    {{ isApiAllSelected(api.id, module.id) ? '取消' : '全选' }}
                  </el-button>
                </div>

                <div v-if="api.expanded" class="case-list">
                  <div
                    v-for="caseItem in api.cases"
                    :key="caseItem.id"
                    class="case-item"
                    :class="{ 'is-selected': isSelected(caseItem.id) }"
                  >
                    <div class="case-info">
                      <span class="case-name">{{ caseItem.name }}</span>
                      <el-tag size="small" type="info">{{ caseItem.status }}</el-tag>
                    </div>
                    <el-button
                      :type="isSelected(caseItem.id) ? 'danger' : 'primary'"
                      size="small"
                      @click.stop="toggleCaseSelection(caseItem, module, api)"
                    >
                      {{ isSelected(caseItem.id) ? '移除' : '添加' }}
                    </el-button>
                  </div>
                  <div v-if="!api.cases || api.cases.length === 0" class="empty-cases">
                    该接口下暂无测试用例
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="filteredTreeData.length === 0 && !loading" class="empty-state">
          <el-empty description="未找到匹配的测试用例" />
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button type="primary" @click="handleConfirm">
        确定 ({{ selectedCases.length }})
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Folder, ArrowRight, ArrowDown } from '@element-plus/icons-vue'
import { projectApi } from '@/api/modules/project/project'
import { apiApi } from '@/api/modules/testing/api'
import { testCaseApi } from '@/api/modules/testing/testCase'

export interface CaseItem {
  id: number
  name: string
  status: string
  moduleId?: number
  moduleName?: string
  apiId?: number
  apiName?: string
}

interface ModuleNode {
  id: number
  name: string
  apiCount: number
  apis: ApiNode[]
  expanded: boolean
}

interface ApiNode {
  id: number
  name: string
  method: string
  path: string
  caseCount: number
  cases: CaseItem[]
  expanded: boolean
}

const props = defineProps<{
  visible: boolean
  projectId?: number
  selectedCases: CaseItem[]
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'update:selectedCases', value: CaseItem[]): void
  (e: 'confirm', value: CaseItem[]): void
}>()

const searchKeyword = ref('')
const loading = ref(false)
const treeData = ref<ModuleNode[]>([])

function toggleModuleExpand(moduleId: number) {
  const module = treeData.value.find(m => m.id === moduleId)
  if (module) {
    module.expanded = !module.expanded
    if (module.expanded && (!module.apis || module.apis.length === 0)) {
      loadModuleApis(module)
    }
  }
}

async function toggleApiCases(apiId: number, moduleId: number) {
  const module = treeData.value.find(m => m.id === moduleId)
  if (!module) return

  const api = module.apis?.find(a => a.id === apiId)
  if (!api) return

  api.expanded = !api.expanded
  if (api.expanded && (!api.cases || api.cases.length === 0)) {
    await loadApiCases(apiId, api)
  }
}

const filteredTreeData = computed(() => {
  if (!searchKeyword.value) return treeData.value

  const keyword = searchKeyword.value.toLowerCase()

  const filterNode = (modules: ModuleNode[]): ModuleNode[] => {
    return modules.reduce<ModuleNode[]>((result, module) => {
      const moduleMatch = module.name.toLowerCase().includes(keyword)

      const filteredApis = module.apis ? module.apis.filter(api => {
        const apiMatch = api.name.toLowerCase().includes(keyword) ||
                         api.path.toLowerCase().includes(keyword)
        const filteredCases = api.cases ? api.cases.filter(c =>
          c.name.toLowerCase().includes(keyword)
        ) : []
        return apiMatch || filteredCases.length > 0
      }).map(api => {
        const filteredCases = api.cases ? api.cases.filter(c =>
          c.name.toLowerCase().includes(keyword)
        ) : []
        return { ...api, cases: filteredCases }
      }) : []

      if (moduleMatch || filteredApis.length > 0) {
        result.push({ ...module, apis: filteredApis })
      }
      return result
    }, [])
  }

  return filterNode(treeData.value)
})

function getMethodType(method: string): 'success' | 'primary' | 'warning' | 'danger' | 'info' {
  const map: Record<string, 'success' | 'primary' | 'warning' | 'danger' | 'info'> = {
    'GET': 'success',
    'POST': 'primary',
    'PUT': 'warning',
    'DELETE': 'danger',
    'PATCH': 'info'
  }
  return map[method?.toUpperCase()] || 'info'
}

function isSelected(caseId: number): boolean {
  return props.selectedCases.some(item => item.id === caseId)
}

async function loadApiCases(apiId: number, apiTarget: ApiNode) {
  try {
    const res = await testCaseApi.query({ apiId, projectId: props.projectId, pageNum: 1, pageSize: 1000 })
    if (res) {
      const casesList = res.records || []
      apiTarget.cases = casesList.map((item: any) => ({
        id: item.id || item.caseId,
        name: item.name || item.caseName,
        status: item.status || '正常'
      }))
      apiTarget.caseCount = apiTarget.cases.length
    }
  } catch {
    apiTarget.cases = []
    apiTarget.caseCount = 0
  }
}

async function loadModuleApis(module: ModuleNode) {
  try {
    const res = await apiApi.query({ moduleId: module.id, projectId: props.projectId, pageNum: 1, pageSize: 100 })
    if (res) {
      const apis = res.records || []
      module.apis = apis.map((api: any) => ({
        id: api.id || api.apiId,
        name: api.name,
        method: api.method,
        path: api.path,
        caseCount: 0,
        cases: [],
        expanded: false
      }))
      module.apiCount = module.apis.length
    }
  } catch {
    module.apis = []
    module.apiCount = 0
  }
}

function toggleCaseSelection(caseItem: CaseItem, module: ModuleNode, api: ApiNode) {
  const newSelected = [...props.selectedCases]
  const index = newSelected.findIndex(item => item.id === caseItem.id)

  if (index > -1) {
    newSelected.splice(index, 1)
  } else {
    newSelected.push({
      ...caseItem,
      moduleId: module.id,
      moduleName: module.name,
      apiId: api.id,
      apiName: api.name
    })
  }

  emit('update:selectedCases', newSelected)
}

function isApiAllSelected(apiId: number, moduleId: number): boolean {
  const module = treeData.value.find(m => m.id === moduleId)
  if (!module) return false

  const api = module.apis?.find(a => a.id === apiId)
  if (!api || !api.cases || api.cases.length === 0) return false

  return api.cases.every(caseItem =>
    props.selectedCases.some(selected => selected.id === caseItem.id)
  )
}

function isModuleAllSelected(moduleId: number): boolean {
  const module = treeData.value.find(m => m.id === moduleId)
  if (!module || !module.apis || module.apis.length === 0) return false

  let totalCases = 0
  let selectedCases = 0

  for (const api of module.apis) {
    if (api.cases) {
      totalCases += api.cases.length
      selectedCases += api.cases.filter(c =>
        props.selectedCases.some(selected => selected.id === c.id)
      ).length
    }
  }

  return totalCases > 0 && selectedCases === totalCases
}

async function handleApiBatchSelect(apiId: number, moduleId: number) {
  const module = treeData.value.find(m => m.id === moduleId)
  if (!module) return

  const api = module.apis?.find(a => a.id === apiId)
  if (!api) return

  if (!api.cases || api.cases.length === 0) {
    await loadApiCases(apiId, api)
  }

  const isAllSelected = isApiAllSelected(apiId, moduleId)
  let newSelected = [...props.selectedCases]

  if (isAllSelected) {
    const apiCaseIds = new Set(api.cases.map(c => c.id))
    newSelected = newSelected.filter(item => !apiCaseIds.has(item.id))
    ElMessage.success(`已取消选择 ${api.cases.length} 个用例`)
  } else {
    for (const caseItem of api.cases) {
      const exists = newSelected.some(item => item.id === caseItem.id)
      if (!exists) {
        newSelected.push({
          ...caseItem,
          moduleId: module.id,
          moduleName: module.name,
          apiId: api.id,
          apiName: api.name
        })
      }
    }
    ElMessage.success(`已添加 ${api.cases.length} 个用例`)
  }

  emit('update:selectedCases', newSelected)
}

async function handleModuleBatchSelect(moduleId: number) {
  const module = treeData.value.find(m => m.id === moduleId)
  if (!module) return

  if (!module.expanded) {
    module.expanded = true
  }

  for (const api of module.apis) {
    if (!api.cases || api.cases.length === 0) {
      await loadApiCases(api.id, api)
    }
  }

  const isAllSelected = isModuleAllSelected(moduleId)
  let newSelected = [...props.selectedCases]

  if (isAllSelected) {
    const moduleCaseIds = new Set<number>()
    for (const api of module.apis) {
      api.cases.forEach(c => moduleCaseIds.add(c.id))
    }
    newSelected = newSelected.filter(item => !moduleCaseIds.has(item.id))
    ElMessage.success(`已取消选择该模块下所有用例`)
  } else {
    let addCount = 0
    for (const api of module.apis) {
      for (const caseItem of api.cases || []) {
        const exists = newSelected.some(item => item.id === caseItem.id)
        if (!exists) {
          newSelected.push({
            ...caseItem,
            moduleId: module.id,
            moduleName: module.name,
            apiId: api.id,
            apiName: api.name
          })
          addCount++
        }
      }
    }
    ElMessage.success(`已添加 ${addCount} 个用例`)
  }

  emit('update:selectedCases', newSelected)
}

async function loadTreeData() {
  if (!props.projectId) {
    ElMessage.warning('请先选择所属项目')
    emit('update:visible', false)
    return
  }

  loading.value = true
  try {
    treeData.value = []

    const res = await projectApi.getModulesByProject(props.projectId)
    const moduleList: any[] = Array.isArray(res) ? res : []

    for (const mod of moduleList) {
      const moduleId = mod.id
      if (!moduleId) continue

      const apiList: ModuleNode = {
        id: moduleId,
        name: mod.name,
        apiCount: 0,
        apis: [],
        expanded: false
      }

      treeData.value.push(apiList)
    }
  } catch {
    ElMessage.error('加载模块数据失败')
  } finally {
    loading.value = false
  }
}

function handleOpen() {
  if (props.projectId) {
    loadTreeData()
  }
}

function handleClose() {
  emit('update:visible', false)
}

function handleConfirm() {
  emit('confirm', props.selectedCases)
  emit('update:visible', false)
}
</script>

<style scoped lang="scss">
.case-selector {
  max-height: 600px;
}

.selector-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.stats {
  font-size: 14px;
  color: #606266;
}

.tree-container {
  max-height: 500px;
  overflow-y: auto;
}

.module-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.module-item {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.module-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background-color: #f5f7fa;
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background-color: #eef1f5;
  }

  .el-button {
    margin-left: auto;
  }
}

.module-name {
  flex: 1;
  font-weight: 500;
}

.api-list {
  padding-left: 20px;
}

.api-item {
  margin-bottom: 12px;
}

.api-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background-color: #fafafa;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background-color: #eef1f5;
  }

  .el-button {
    margin-left: auto;
  }
}

.api-name {
  font-weight: 500;
  color: #303133;
}

.api-path {
  flex: 1;
  color: #909399;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.case-list {
  padding-left: 32px;
  margin-top: 8px;
}

.case-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  margin-bottom: 6px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fff;
  transition: all 0.2s ease;

  &:hover {
    border-color: #c0c4cc;
  }

  &.is-selected {
    border-color: #409eff;
    background-color: #ecf5ff;
  }
}

.case-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.case-name {
  font-weight: 500;
  color: #303133;
}

.empty-cases {
  text-align: center;
  padding: 16px;
  color: #909399;
  font-size: 13px;
}

.empty-state {
  padding: 40px 20px;
}
</style>
