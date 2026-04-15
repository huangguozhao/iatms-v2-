<template>
  <div class="api-list page-container">
    <div class="page-header">
      <h2 class="title">接口管理</h2>
      <el-button type="primary" @click="handleCreate">新建接口</el-button>
    </div>

    <el-card>
      <div class="search-form">
        <el-input v-model="searchForm.keyword" placeholder="搜索接口名称或路径" clearable style="width: 200px" />
        <el-select v-model="searchForm.method" placeholder="请求方法" clearable style="width: 120px">
          <el-option label="GET" value="GET" />
          <el-option label="POST" value="POST" />
          <el-option label="PUT" value="PUT" />
          <el-option label="DELETE" value="DELETE" />
          <el-option label="PATCH" value="PATCH" />
        </el-select>
        <el-select v-model="searchForm.projectId" placeholder="所属项目" clearable style="width: 150px">
          <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
        <el-button type="primary" @click="loadApis">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table :data="apis" v-loading="loading" style="width: 100%; margin-top: 20px">
        <el-table-column prop="name" label="接口名称" min-width="150">
          <template #default="{ row }">
            <router-link :to="`/apis/${row.id}`" class="api-link">
              {{ row.name }}
            </router-link>
          </template>
        </el-table-column>
        <el-table-column prop="method" label="方法" width="80">
          <template #default="{ row }">
            <el-tag :type="getMethodType(row.method)" size="small">{{ row.method }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径" min-width="200" />
        <el-table-column prop="projectName" label="项目" width="120" />
        <el-table-column prop="moduleName" label="模块" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleCopy(row)">复制</el-button>
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
        @size-change="loadApis"
        @current-change="loadApis"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="接口名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入接口名称" />
        </el-form-item>
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="form.projectId" style="width: 100%">
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属模块">
          <el-select v-model="form.moduleId" style="width: 100%">
            <el-option v-for="m in modules" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="请求方法" prop="method">
              <el-select v-model="form.method" style="width: 100%">
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
                <el-option label="PUT" value="PUT" />
                <el-option label="DELETE" value="DELETE" />
                <el-option label="PATCH" value="PATCH" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="请求路径" prop="path">
              <el-input v-model="form.path" placeholder="/api/xxx" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" rows="2" />
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
import { apiApi } from '@/api/modules/testing/api'
import type { ApiSummaryVO } from '@/types/api'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新建接口')
const formRef = ref()

const searchForm = reactive({
  keyword: '',
  method: '',
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
  moduleId: null as number | null,
  method: 'GET',
  path: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入接口名称', trigger: 'blur' }],
  projectId: [{ required: true, message: '请选择所属项目', trigger: 'change' }],
  method: [{ required: true, message: '请选择请求方法', trigger: 'change' }],
  path: [{ required: true, message: '请输入请求路径', trigger: 'blur' }]
}

const apis = ref<ApiSummaryVO[]>([])
const projects = ref<any[]>([])
const modules = ref<any[]>([])

function getMethodType(method: string) {
  const map: Record<string, string> = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'info'
  }
  return map[method] || 'info'
}

async function loadApis() {
  loading.value = true
  try {
    const result = await apiApi.query({
      keyword: searchForm.keyword || undefined,
      method: searchForm.method || undefined,
      projectId: searchForm.projectId || undefined,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    apis.value = result.records
    pagination.total = result.total
  } catch (error) {
    console.error('加载接口失败:', error)
  } finally {
    loading.value = false
  }
}

async function loadProjects() {
  try {
    const result = await projectApi.query({ pageNum: 1, pageSize: 100 })
    projects.value = result.records
  } catch (error) {
    console.error('加载项目失败:', error)
  }
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.method = ''
  searchForm.projectId = null
  pagination.pageNum = 1
  loadApis()
}

function handleCreate() {
  dialogTitle.value = '新建接口'
  Object.assign(form, {
    id: null,
    name: '',
    projectId: projects.value[0]?.id || null,
    moduleId: null,
    method: 'GET',
    path: '',
    description: ''
  })
  dialogVisible.value = true
}

function handleEdit(row: ApiSummaryVO) {
  dialogTitle.value = '编辑接口'
  Object.assign(form, {
    id: row.id,
    name: row.name,
    projectId: row.projectId,
    moduleId: row.moduleId,
    method: row.method,
    path: row.path,
    description: ''
  })
  dialogVisible.value = true
}

function handleCopy(row: ApiSummaryVO) {
  Object.assign(form, {
    name: row.name + '_copy',
    projectId: row.projectId,
    moduleId: row.moduleId,
    method: row.method,
    path: row.path + '_copy',
    description: row.description
  })
  dialogTitle.value = '新建接口'
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (dialogTitle.value === '新建接口') {
      await apiApi.create(form)
      ElMessage.success('创建成功')
    } else {
      await apiApi.update(form.id!, form)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    loadApis()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

async function handleDelete(row: ApiSummaryVO) {
  try {
    await ElMessageBox.confirm(`确定删除接口 "${row.name}" 吗?`, '提示', { type: 'warning' })
    await apiApi.delete(row.id)
    ElMessage.success('删除成功')
    loadApis()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadProjects()
  loadApis()
})
</script>

<style scoped lang="scss">
.api-link {
  color: #409EFF;
  &:hover {
    text-decoration: underline;
  }
}
</style>
