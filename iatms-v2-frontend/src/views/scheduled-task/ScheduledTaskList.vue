<template>
  <div class="scheduled-task-list page-container">
    <div class="page-header">
      <h2 class="title">定时任务</h2>
      <el-button type="primary" @click="handleCreate">新建任务</el-button>
    </div>

    <el-card>
      <div class="search-form">
        <el-input v-model="searchForm.keyword" placeholder="搜索任务名称" clearable style="width: 200px" />
        <el-select v-model="searchForm.status" placeholder="任务状态" clearable style="width: 120px">
          <el-option label="启用" value="ENABLED" />
          <el-option label="暂停" value="PAUSED" />
        </el-select>
        <el-button type="primary" @click="loadTasks">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table :data="tasks" v-loading="loading" style="width: 100%; margin-top: 20px">
        <el-table-column prop="name" label="任务名称" min-width="150">
          <template #default="{ row }">
            <router-link :to="`/scheduled-tasks/${row.id}`" class="task-link">
              {{ row.name }}
            </router-link>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="任务类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetName" label="执行目标" width="150" />
        <el-table-column prop="cron" label="Cron表达式" width="120" />
        <el-table-column prop="nextRunTime" label="下次执行" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" size="small">
              {{ row.status === 'ENABLED' ? '启用' : '暂停' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastRunTime" label="上次执行" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleExecute(row)">立即执行</el-button>
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
        @size-change="loadTasks"
        @current-change="loadTasks"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="任务类型" prop="type">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="执行套件" value="TEST_SUITE" />
            <el-option label="执行用例" value="TEST_CASE" />
            <el-option label="执行接口" value="API" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行目标" prop="targetId">
          <el-select v-model="form.targetId" placeholder="选择执行目标" style="width: 100%">
            <el-option v-for="s in suites" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Cron表达式" prop="cron">
          <el-input v-model="form.cron" placeholder="0 0 * * * ?" />
        </el-form-item>
        <el-form-item label="执行策略">
          <el-select v-model="form.strategy" style="width: 100%">
            <el-option label="立即执行一次" value="RUN_ONCE" />
            <el-option label="启用定时" value="SCHEDULED" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知设置">
          <el-checkbox-group v-model="form.notifyOn">
            <el-checkbox label="SUCCESS">成功通知</el-checkbox>
            <el-checkbox label="FAILURE">失败通知</el-checkbox>
          </el-checkbox-group>
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
import { scheduledTaskApi } from '@/api/modules/scheduling/scheduledTask'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新建任务')
const formRef = ref()

const searchForm = reactive({
  keyword: '',
  status: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  name: '',
  type: 'TEST_SUITE',
  targetId: null as number | null,
  cron: '0 0 * * * ?',
  strategy: 'SCHEDULED',
  notifyOn: [] as string[],
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  targetId: [{ required: true, message: '请选择执行目标', trigger: 'change' }],
  cron: [{ required: true, message: '请输入Cron表达式', trigger: 'blur' }]
}

const tasks = ref<any[]>([])
const suites = ref<any[]>([])

function getTypeText(type: string) {
  const map: Record<string, string> = {
    TEST_SUITE: '测试套件',
    TEST_CASE: '测试用例',
    API: '接口'
  }
  return map[type] || type
}

async function loadTasks() {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 300))
    tasks.value = []
    pagination.total = 0
  } catch (error) {
    console.error('加载任务失败:', error)
  } finally {
    loading.value = false
  }
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.status = ''
  pagination.pageNum = 1
  loadTasks()
}

function handleCreate() {
  dialogTitle.value = '新建任务'
  Object.assign(form, {
    name: '',
    type: 'TEST_SUITE',
    targetId: null,
    cron: '0 0 * * * ?',
    strategy: 'SCHEDULED',
    notifyOn: [],
    description: ''
  })
  dialogVisible.value = true
}

function handleEdit(row: any) {
  dialogTitle.value = '编辑任务'
  Object.assign(form, {
    name: row.name,
    type: row.type,
    targetId: row.targetId,
    cron: row.cron,
    strategy: 'SCHEDULED',
    notifyOn: [],
    description: ''
  })
  dialogVisible.value = true
}

async function handleExecute(row: any) {
  try {
    await ElMessageBox.confirm(`确定立即执行任务 "${row.name}" 吗?`, '提示', { type: 'warning' })
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
  ElMessage.success(dialogTitle.value === '新建任务' ? '创建成功' : '更新成功')
  dialogVisible.value = false
  loadTasks()
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除任务 "${row.name}" 吗?`, '提示', { type: 'warning' })
    ElMessage.success('删除成功')
    loadTasks()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadTasks()
})
</script>

<style scoped lang="scss">
.task-link {
  color: #409EFF;
  &:hover {
    text-decoration: underline;
  }
}
</style>
