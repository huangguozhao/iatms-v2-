<template>
  <div class="project-list page-container">
    <div class="page-header">
      <h2 class="title">项目管理</h2>
      <el-button type="primary" @click="handleCreate">新建项目</el-button>
    </div>

    <el-card>
      <div class="search-form">
        <el-input v-model="searchForm.keyword" placeholder="搜索项目名称或编号" clearable style="width: 200px" />
        <el-select v-model="searchForm.status" placeholder="项目状态" clearable style="width: 150px">
          <el-option label="激活" value="ACTIVE" />
          <el-option label="停用" value="INACTIVE" />
          <el-option label="归档" value="ARCHIVED" />
        </el-select>
        <el-select v-model="searchForm.sortBy" placeholder="排序方式" style="width: 150px">
          <el-option label="默认排序" value="createdAt" />
          <el-option label="按名称" value="name" />
          <el-option label="按更新时间" value="updatedAt" />
        </el-select>
        <el-select v-model="searchForm.sortOrder" placeholder="排序方向" style="width: 120px">
          <el-option label="升序" value="ASC" />
          <el-option label="降序" value="DESC" />
        </el-select>
        <el-button type="primary" @click="loadProjects">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table :data="projects" v-loading="loading" style="width: 100%; margin-top: 20px">
        <el-table-column prop="name" label="项目名称" min-width="150">
          <template #default="{ row }">
            <router-link :to="`/projects/${row.id}`" class="project-link">
              {{ row.name }}
            </router-link>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="项目编号" width="150" />
        <el-table-column prop="projectType" label="类型" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ownerName" label="负责人" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
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
        @size-change="loadProjects"
        @current-change="loadProjects"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目编号" prop="code">
          <el-input v-model="form.code" placeholder="请输入项目编号" />
        </el-form-item>
        <el-form-item label="项目类型">
          <el-select v-model="form.projectType" style="width: 100%">
            <el-option label="HTTP" value="HTTP" />
            <el-option label="WebSocket" value="WEBSOCKET" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="激活" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
            <el-option label="归档" value="ARCHIVED" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { projectApi, type CreateProjectDTO } from '@/api/modules/project/project'
import type { ProjectSummaryVO } from '@/types/api'
import { getStatusType, getStatusText } from '@/utils/formatters'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新建项目')
const formRef = ref()

const searchForm = reactive({
  keyword: '',
  status: '',
  sortBy: 'createdAt' as 'createdAt' | 'updatedAt' | 'name',
  sortOrder: 'DESC' as 'ASC' | 'DESC'
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: null as number | null,
  name: '',
  code: '',
  description: '',
  projectType: 'HTTP',
  status: 'ACTIVE'
})

const rules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入项目编号', trigger: 'blur' }]
}

const projects = ref<ProjectSummaryVO[]>([])

async function loadProjects() {
  loading.value = true
  try {
    const result = await projectApi.query({
      keyword: searchForm.keyword || undefined,
      status: searchForm.status || undefined,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      sortBy: searchForm.sortBy,
      sortOrder: searchForm.sortOrder
    })
    projects.value = result.records
    pagination.total = result.total
  } catch (error) {
    console.error('加载项目失败:', error)
  } finally {
    loading.value = false
  }
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.sortBy = 'createdAt'
  searchForm.sortOrder = 'DESC'
  pagination.pageNum = 1
  loadProjects()
}

function handleCreate() {
  dialogTitle.value = '新建项目'
  Object.assign(form, {
    name: '',
    code: '',
    description: '',
    projectType: 'HTTP',
    status: 'ACTIVE'
  })
  dialogVisible.value = true
}

async function handleEdit(row: ProjectSummaryVO) {
  dialogTitle.value = '编辑项目'
  try {
    const detail = await projectApi.getDetail(row.id)
    Object.assign(form, {
      id: row.id,
      name: detail.name,
      code: detail.code,
      description: detail.description,
      projectType: detail.projectType,
      status: detail.status
    })
  } catch (error) {
    ElMessage.error('获取项目详情失败')
    return
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    if (dialogTitle.value === '新建项目') {
      await projectApi.create(form)
      ElMessage.success('创建成功')
    } else {
      await projectApi.update((form as any).id, form)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    loadProjects()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

async function handleDelete(row: ProjectSummaryVO) {
  try {
    await ElMessageBox.confirm(`确定删除项目 "${row.name}" 吗?`, '提示', {
      type: 'warning'
    })
    await projectApi.delete(row.id)
    ElMessage.success('删除成功')
    loadProjects()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadProjects()
})
</script>

<style scoped lang="scss">
.project-link {
  color: #409EFF;
  &:hover {
    text-decoration: underline;
  }
}
</style>
