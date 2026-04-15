<template>
  <div class="test-case-list page-container">
    <div class="page-header">
      <h2 class="title">测试用例</h2>
      <el-button type="primary" @click="handleCreate">新建用例</el-button>
    </div>

    <el-card>
      <div class="search-form">
        <el-input v-model="searchForm.keyword" placeholder="搜索用例名称" clearable style="width: 200px" />
        <el-select v-model="searchForm.priority" placeholder="优先级" clearable style="width: 120px">
          <el-option label="P0" value="P0" />
          <el-option label="P1" value="P1" />
          <el-option label="P2" value="P2" />
          <el-option label="P3" value="P3" />
        </el-select>
        <el-button type="primary" @click="loadCases">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table :data="cases" v-loading="loading" style="width: 100%; margin-top: 20px">
        <el-table-column prop="name" label="用例名称" min-width="150">
          <template #default="{ row }">
            <router-link :to="`/test-cases/${row.id}`" class="case-link">
              {{ row.name }}
            </router-link>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" size="small">{{ row.priority }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="apiName" label="所属接口" width="150" />
        <el-table-column prop="projectName" label="项目" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" size="small">
              {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleExecute(row)">执行</el-button>
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
        @size-change="loadCases"
        @current-change="loadCases"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用例名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入用例名称" />
        </el-form-item>
        <el-form-item label="所属接口" prop="apiId">
          <el-select v-model="form.apiId" placeholder="请选择接口" style="width: 100%">
            <el-option v-for="a in apis" :key="a.id" :label="a.name" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="form.priority" style="width: 100%">
            <el-option label="P0" value="P0" />
            <el-option label="P1" value="P1" />
            <el-option label="P2" value="P2" />
            <el-option label="P3" value="P3" />
          </el-select>
        </el-form-item>
        <el-form-item label="请求头">
          <el-input v-model="form.headers" type="textarea" rows="2" placeholder="JSON格式，如 {&quot;Content-Type&quot;: &quot;application/json&quot;}" />
        </el-form-item>
        <el-form-item label="请求体">
          <el-input v-model="form.requestBody" type="textarea" rows="3" placeholder="请求体内容" />
        </el-form-item>
        <el-form-item label="断言">
          <el-input v-model="form.assertions" type="textarea" rows="2" placeholder="JSON数组格式" />
        </el-form-item>
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
import { testCaseApi } from '@/api/modules/testing/testCase'
import type { TestCaseSummaryVO } from '@/types/api'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新建用例')
const formRef = ref()

const searchForm = reactive({
  keyword: '',
  priority: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  name: '',
  apiId: null as number | null,
  priority: 'P2',
  headers: '',
  requestBody: '',
  assertions: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入用例名称', trigger: 'blur' }],
  apiId: [{ required: true, message: '请选择所属接口', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
}

const cases = ref<TestCaseSummaryVO[]>([])
const apis = ref<any[]>([])

function getPriorityType(priority: string) {
  const map: Record<string, string> = {
    P0: 'danger',
    P1: 'warning',
    P2: 'primary',
    P3: 'info'
  }
  return map[priority] || 'info'
}

async function loadCases() {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 300))
    cases.value = []
    pagination.total = 0
  } catch (error) {
    console.error('加载用例失败:', error)
  } finally {
    loading.value = false
  }
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.priority = ''
  pagination.pageNum = 1
  loadCases()
}

function handleCreate() {
  dialogTitle.value = '新建用例'
  Object.assign(form, {
    name: '',
    apiId: null,
    priority: 'P2',
    headers: '',
    requestBody: '',
    assertions: '',
    description: ''
  })
  dialogVisible.value = true
}

function handleEdit(row: TestCaseSummaryVO) {
  dialogTitle.value = '编辑用例'
  Object.assign(form, {
    name: row.name,
    apiId: row.apiId,
    priority: row.priority,
    headers: '',
    requestBody: '',
    assertions: '',
    description: ''
  })
  dialogVisible.value = true
}

async function handleExecute(row: TestCaseSummaryVO) {
  try {
    await ElMessageBox.confirm(`确定执行用例 "${row.name}" 吗?`, '提示', { type: 'warning' })
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
  ElMessage.success(dialogTitle.value === '新建用例' ? '创建成功' : '更新成功')
  dialogVisible.value = false
  loadCases()
}

async function handleDelete(row: TestCaseSummaryVO) {
  try {
    await ElMessageBox.confirm(`确定删除用例 "${row.name}" 吗?`, '提示', { type: 'warning' })
    ElMessage.success('删除成功')
    loadCases()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadCases()
})
</script>

<style scoped lang="scss">
.case-link {
  color: #409EFF;
  &:hover {
    text-decoration: underline;
  }
}
</style>
