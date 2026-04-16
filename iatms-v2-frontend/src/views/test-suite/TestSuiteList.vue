<template>
  <div class="test-suite-list page-container">
    <div class="page-header">
      <h2 class="title">测试套件</h2>
      <el-button type="primary" @click="handleCreate">新建套件</el-button>
    </div>

    <el-card>
      <div class="search-form">
        <el-input v-model="searchForm.keyword" placeholder="搜索套件名称" clearable style="width: 200px" />
        <el-select v-model="searchForm.projectId" placeholder="所属项目" clearable style="width: 150px">
          <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
        <el-button type="primary" @click="loadSuites">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table :data="suites" v-loading="loading" style="width: 100%; margin-top: 20px">
        <el-table-column prop="name" label="套件名称" min-width="150">
          <template #default="{ row }">
            <router-link :to="`/test-suites/${row.id}`" class="suite-link">
              {{ row.name }}
            </router-link>
          </template>
        </el-table-column>
        <el-table-column prop="projectName" label="项目" width="150" />
        <el-table-column prop="caseCount" label="用例数量" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" size="small">
              {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleExecute(row)">执行</el-button>
            <el-button link type="warning" @click="handleCopy(row)">复制</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="loadSuites"
        @current-change="loadSuites"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="套件名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入套件名称" />
        </el-form-item>
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="form.projectId" style="width: 100%">
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="包含用例">
          <el-select v-model="form.caseIds" multiple placeholder="选择用例" style="width: 100%">
            <el-option v-for="c in cases" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行策略">
          <el-select v-model="form.executionStrategy" style="width: 100%">
            <el-option label="顺序执行" value="SEQUENTIAL" />
            <el-option label="并行执行" value="PARALLEL" />
          </el-select>
        </el-form-item>
        <el-form-item label="失败策略">
          <el-select v-model="form.failStrategy" style="width: 100%">
            <el-option label="继续执行" value="CONTINUE" />
            <el-option label="停止执行" value="STOP" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { projectApi } from '@/api/modules/project/project'
import { testSuiteApi, type CreateTestSuiteDTO } from '@/api/modules/testing/testSuite'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新建套件')
const formRef = ref()

const searchForm = reactive({
  keyword: '',
  projectId: null as number | null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: null as number | null,
  name: '',
  projectId: null as number | null,
  caseIds: [] as number[],
  executionStrategy: 'SEQUENTIAL',
  failStrategy: 'CONTINUE',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入套件名称', trigger: 'blur' }],
  projectId: [{ required: true, message: '请选择所属项目', trigger: 'change' }]
}

const suites = ref<any[]>([])
const projects = ref<any[]>([])
const cases = ref<any[]>([])

async function loadSuites() {
  loading.value = true
  try {
    const params = {
      keyword: searchForm.keyword || undefined,
      projectId: searchForm.projectId || undefined,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await testSuiteApi.query(params)
    suites.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('加载套件失败:', error)
    ElMessage.error('加载套件失败')
  } finally {
    loading.value = false
  }
}

async function loadProjects() {
  try {
    const result = await projectApi.query({ pageNum: 1, pageSize: 100 })
    projects.value = result.records || []
  } catch (error) {
    console.error('加载项目失败:', error)
  }
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.projectId = null
  pagination.pageNum = 1
  loadSuites()
}

function handleCreate() {
  dialogTitle.value = '新建套件'
  Object.assign(form, {
    id: null,
    name: '',
    projectId: projects.value[0]?.id || null,
    caseIds: [],
    executionStrategy: 'SEQUENTIAL',
    failStrategy: 'CONTINUE',
    description: ''
  })
  dialogVisible.value = true
}

async function handleEdit(row: any) {
  dialogTitle.value = '编辑套件'
  try {
    const detail = await testSuiteApi.getDetail(row.id)
    Object.assign(form, {
      id: row.id,
      name: detail.data?.name || row.name,
      projectId: detail.data?.projectId || row.projectId,
      caseIds: detail.data?.caseIds || [],
      executionStrategy: detail.data?.executionStrategy || 'SEQUENTIAL',
      failStrategy: detail.data?.failStrategy || 'CONTINUE',
      description: detail.data?.description || ''
    })
  } catch (error) {
    ElMessage.error('获取套件详情失败')
    return
  }
  dialogVisible.value = true
}

function handleCopy(row: any) {
  Object.assign(form, {
    id: null,
    name: row.name + '_copy',
    projectId: row.projectId,
    caseIds: row.caseIds || [],
    executionStrategy: row.executionStrategy,
    failStrategy: row.failStrategy,
    description: row.description
  })
  dialogTitle.value = '新建套件'
  dialogVisible.value = true
}

async function handleExecute(row: any) {
  try {
    await ElMessageBox.confirm(`确定执行套件 "${row.name}" 吗?`, '提示', { type: 'warning' })
    await testSuiteApi.execute(row.id)
    ElMessage.success('执行已提交')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '执行失败')
    }
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (dialogTitle.value === '新建套件') {
      await testSuiteApi.create(form as CreateTestSuiteDTO)
      ElMessage.success('创建成功')
    } else {
      await testSuiteApi.update(form.id!, form as CreateTestSuiteDTO)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    loadSuites()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除套件 "${row.name}" 吗?`, '提示', { type: 'warning' })
    await testSuiteApi.delete(row.id)
    ElMessage.success('删除成功')
    loadSuites()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadProjects()
  loadSuites()
})
</script>

<style scoped lang="scss">
.suite-link {
  color: #409EFF;
  &:hover {
    text-decoration: underline;
  }
}
</style>
