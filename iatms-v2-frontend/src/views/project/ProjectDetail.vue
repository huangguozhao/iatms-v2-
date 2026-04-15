<template>
  <div class="project-detail page-container">
    <div class="page-header">
      <el-button @click="router.push('/projects')">返回</el-button>
      <div>
        <el-button type="primary" @click="handleEdit">编辑</el-button>
      </div>
    </div>

    <el-card v-if="project">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="项目名称">{{ project.name }}</el-descriptions-item>
        <el-descriptions-item label="项目编号">{{ project.code }}</el-descriptions-item>
        <el-descriptions-item label="项目类型">{{ project.projectType }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(project.status)">{{ project.statusText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="负责人">{{ project.ownerName }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ project.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ project.description }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>模块列表</span>
        <el-button type="primary" size="small" @click="handleCreateModule">新建模块</el-button>
      </template>
      <el-table :data="modules" style="width: 100%">
        <el-table-column prop="name" label="模块名称" />
        <el-table-column prop="parentId" label="父模块" width="150" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEditModule(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDeleteModule(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="moduleDialogVisible" :title="moduleDialogTitle" width="500px">
      <el-form ref="moduleFormRef" :model="moduleForm" :rules="moduleRules" label-width="100px">
        <el-form-item label="模块名称" prop="name">
          <el-input v-model="moduleForm.name" placeholder="请输入模块名称" />
        </el-form-item>
        <el-form-item label="父模块">
          <el-select v-model="moduleForm.parentId" placeholder="请选择父模块" clearable style="width: 100%">
            <el-option v-for="m in modules" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="moduleForm.description" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="moduleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleModuleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { projectApi } from '@/api/modules/project/project'
import type { ProjectDetailVO } from '@/types/api'

const router = useRouter()
const route = useRoute()
const projectId = Number(route.params.id)

const project = ref<ProjectDetailVO | null>(null)
const modules = ref<any[]>([])

const moduleDialogVisible = ref(false)
const moduleDialogTitle = ref('新建模块')
const moduleFormRef = ref()
const moduleForm = ref({
  name: '',
  parentId: null as number | null,
  description: ''
})
const moduleRules = {
  name: [{ required: true, message: '请输入模块名称', trigger: 'blur' }]
}

function getStatusType(status: string) {
  const map: Record<string, string> = {
    NOT_STARTED: 'info',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success'
  }
  return map[status] || 'info'
}

async function loadProject() {
  try {
    project.value = await projectApi.getDetail(projectId)
  } catch (error) {
    console.error('加载项目失败:', error)
  }
}

function handleEdit() {
  router.push(`/projects/${projectId}/edit`)
}

function handleCreateModule() {
  moduleDialogTitle.value = '新建模块'
  moduleForm.value = { name: '', parentId: null, description: '' }
  moduleDialogVisible.value = true
}

function handleEditModule(row: any) {
  moduleDialogTitle.value = '编辑模块'
  moduleForm.value = { name: row.name, parentId: row.parentId, description: row.description }
  moduleDialogVisible.value = true
}

async function handleModuleSubmit() {
  const valid = await moduleFormRef.value?.validate().catch(() => false)
  if (!valid) return
  ElMessage.success('模块保存成功')
  moduleDialogVisible.value = false
}

async function handleDeleteModule(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除模块 "${row.name}" 吗?`, '提示', { type: 'warning' })
    ElMessage.success('删除成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadProject()
})
</script>

<style scoped lang="scss">
.project-detail {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }
}
</style>
