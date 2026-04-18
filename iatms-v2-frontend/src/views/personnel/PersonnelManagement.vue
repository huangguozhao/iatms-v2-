<template>
  <div class="personnel-page">
    <div class="page-breadcrumb">
      <span class="breadcrumb-item" @click="$router.push('/dashboard')">首页</span>
      <span class="breadcrumb-sep">/</span>
      <span class="breadcrumb-item active">人员管理</span>
    </div>

    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">人员管理</h2>
        <p class="page-subtitle">管理系统用户和项目成员分配</p>
      </div>
      <div class="header-right" v-if="isAdmin && activeTab === 'users'">
        <el-button type="primary" @click="handleOpenCreate">
          <el-icon><Plus /></el-icon>
          创建新用户
        </el-button>
      </div>
    </div>

    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon stat-icon-primary">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ pagination.total }}</span>
          <span class="stat-label">总用户数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-success">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ activeCount }}</span>
          <span class="stat-label">活跃用户</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-warning">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ pendingCount }}</span>
          <span class="stat-label">待审核</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-danger">
          <el-icon><CircleClose /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ inactiveCount }}</span>
          <span class="stat-label">已禁用</span>
        </div>
      </div>
    </div>

    <div class="personnel-content">
      <el-tabs v-model="activeTab" class="personnel-tabs">

        <!-- 用户管理标签页 -->
        <el-tab-pane label="用户管理" name="users" v-if="isAdmin">
          <div class="toolbar">
            <div class="toolbar-left">
              <el-input
                v-model="searchForm.keyword"
                placeholder="搜索用户姓名、邮箱..."
                clearable
                style="width: 260px"
                @keyup.enter="handleSearch"
                @clear="handleSearch"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
            <div class="toolbar-right">
              <el-button :type="hasActiveFilters ? 'primary' : 'default'" @click="filterDialogVisible = true">
                <el-icon><Filter /></el-icon>
                筛选条件
                <span v-if="hasActiveFilters" class="filter-dot"></span>
              </el-button>
            </div>
          </div>

          <el-table
            :data="users"
            v-loading="loading"
            style="width: 100%; margin-top: 16px"
            :header-cell-style="{ background: '#fafafa', color: '#595959' }"
          >
            <el-table-column label="用户" min-width="220">
              <template #default="{ row }">
                <div class="user-cell">
                  <el-avatar :size="36" :style="getAvatarStyle(row.name)">
                    {{ getInitials(row.name) }}
                  </el-avatar>
                  <div class="user-info">
                    <span class="user-name">{{ row.name }}</span>
                    <span class="user-id">ID: {{ row.userId }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="email" label="邮箱" min-width="200">
              <template #default="{ row }">
                <span class="email-cell">{{ row.email }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="phone" label="电话" width="150">
              <template #default="{ row }">
                {{ row.phone || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="position" label="职位" width="140">
              <template #default="{ row }">
                <el-tag size="small" type="info">{{ row.position || '暂无' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)" size="small">
                  <span class="status-dot"></span>
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="140" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <div class="action-btns">
                  <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
                  <el-button
                    link
                    :type="row.status === 'active' ? 'warning' : 'success'"
                    size="small"
                    :loading="statusChangingIds.has(row.userId)"
                    @click="handleToggleStatus(row)"
                  >
                    {{ row.status === 'active' ? '禁用' : row.status === 'inactive' ? '启用' : '激活' }}
                  </el-button>
                  <el-button
                    link
                    type="danger"
                    size="small"
                    :loading="deletingIds.has(row.userId)"
                    @click="handleDelete(row)"
                  >删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            style="margin-top: 20px; justify-content: flex-end"
            @size-change="loadUsers"
            @current-change="loadUsers"
          />
        </el-tab-pane>

        <!-- 项目分配标签页 -->
        <el-tab-pane label="项目分配" name="projects">
          <div class="project-assignment-layout">
            <!-- 左侧：项目列表 -->
            <div class="project-list-panel">
              <div class="panel-header">
                <span class="panel-title">项目列表</span>
                <span class="project-count">{{ projects.length }} 个项目</span>
              </div>
              <div class="project-search">
                <el-input
                  v-model="projectKeyword"
                  placeholder="搜索项目名称..."
                  clearable
                  size="small"
                  @input="onProjectSearch"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </div>
              <div class="project-list" v-loading="projectsLoading">
                <div
                  v-for="p in filteredProjects"
                  :key="p.id"
                  class="project-item"
                  :class="{ active: selectedProjectId === p.id }"
                  @click="handleSelectProject(p)"
                >
                  <div class="project-icon" :style="{ background: p.iconColor || '#1890ff' }">
                    {{ p.name?.charAt(0) || '?' }}
                  </div>
                  <div class="project-meta">
                    <span class="project-name">{{ p.name }}</span>
                    <span class="project-code">{{ p.code }}</span>
                  </div>
                  <el-tag size="small" :type="getStatusType(p.status)">
                    {{ getStatusText(p.status) }}
                  </el-tag>
                </div>
                <el-empty v-if="filteredProjects.length === 0 && !projectsLoading" description="暂无项目" />
              </div>
            </div>

            <!-- 右侧：成员管理 -->
            <div class="member-panel">
              <div v-if="selectedProjectId" class="member-content">
                <div class="panel-header">
                  <div class="member-title-wrap">
                    <span class="panel-title">成员管理</span>
                    <el-tag size="small">{{ selectedProject?.name }}</el-tag>
                  </div>
                  <el-button type="primary" size="small" @click="handleOpenAddMember">
                    <el-icon><Plus /></el-icon>
                    添加成员
                  </el-button>
                </div>

                <el-table
                  :data="members"
                  v-loading="membersLoading"
                  style="width: 100%; margin-top: 12px"
                  :header-cell-style="{ background: '#fafafa', color: '#595959' }"
                >
                  <el-table-column label="成员" min-width="200">
                    <template #default="{ row }">
                      <div class="member-cell">
                        <el-avatar :size="32" :style="getAvatarStyle(row.displayName || row.userName || '?')">
                          {{ getInitials(row.displayName || row.userName || '') }}
                        </el-avatar>
                        <div class="member-info">
                          <span class="member-name">{{ row.displayName || row.userName || '未知用户' }}</span>
                          <span class="member-id">ID: {{ row.userId }}</span>
                        </div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column prop="role" label="角色" width="140">
                    <template #default="{ row }">
                      <el-select
                        :model-value="row.role"
                        size="small"
                        style="width: 110px"
                        @change="(val) => handleRoleChange(row, val)"
                      >
                        <el-option label="项目负责人" value="owner" />
                        <el-option label="项目管理员" value="manager" />
                        <el-option label="开发人员" value="developer" />
                        <el-option label="测试人员" value="tester" />
                        <el-option label="查看者" value="viewer" />
                      </el-select>
                    </template>
                  </el-table-column>
                  <el-table-column prop="joinedAt" label="加入时间" width="160">
                    <template #default="{ row }">
                      {{ row.joinedAt ? formatDate(row.joinedAt) : '-' }}
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="120" fixed="right">
                    <template #default="{ row }">
                      <el-button
                        link
                        type="danger"
                        size="small"
                        :loading="removingMemberIds.has(row.userId)"
                        @click="handleRemoveMember(row)"
                      >移除</el-button>
                    </template>
                  </el-table-column>
                </el-table>

                <el-empty v-if="members.length === 0 && !membersLoading" description="暂无成员，请添加" />
              </div>

              <div v-else class="member-empty">
                <el-icon size="48" color="#d9d9d9"><FolderOpened /></el-icon>
                <p>请从左侧选择一个项目</p>
                <p class="hint">选择后可管理该项目的成员</p>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 创建/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="560px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="用户名" prop="name">
          <el-input v-model="form.name" placeholder="请输入用户名" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEditMode">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码（至少6位）"
            show-password
          />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入电话号码（可选）" />
        </el-form-item>
        <el-form-item label="职位" prop="position">
          <el-input v-model="form.position" placeholder="请输入用户职位" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="活跃" value="active" />
            <el-option label="待激活" value="pending" />
            <el-option label="已禁用" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入用户描述（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEditMode ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 筛选对话框 -->
    <el-dialog v-model="filterDialogVisible" title="筛选条件" width="480px">
      <el-form :model="filterForm" label-width="80px">
        <el-form-item label="用户状态">
          <el-radio-group v-model="filterForm.status">
            <el-radio-button value="">全部</el-radio-button>
            <el-radio-button value="active">活跃</el-radio-button>
            <el-radio-button value="pending">待审核</el-radio-button>
            <el-radio-button value="inactive">已禁用</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="职位">
          <el-input v-model="filterForm.position" placeholder="支持模糊匹配" clearable />
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleFilterReset">重置</el-button>
        <el-button type="primary" @click="handleFilterApply">应用筛选</el-button>
      </template>
    </el-dialog>

    <!-- 添加成员对话框 -->
    <el-dialog v-model="addMemberDialogVisible" title="添加成员" width="480px">
      <div class="add-member-form">
        <div class="search-user-wrap">
          <el-input
            v-model="memberSearchKeyword"
            placeholder="搜索用户姓名或邮箱..."
            clearable
            size="default"
            @input="onMemberSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="user-select-list" v-loading="memberSearchLoading">
          <div
            v-for="u in searchableUsers"
            :key="u.userId"
            class="user-select-item"
            :class="{ selected: addingUserId === u.userId }"
            @click="handleSelectUser(u)"
          >
            <el-avatar :size="32" :style="getAvatarStyle(u.name)">
              {{ getInitials(u.name) }}
            </el-avatar>
            <div class="user-select-info">
              <span class="user-select-name">{{ u.name }}</span>
              <span class="user-select-email">{{ u.email }}</span>
            </div>
            <el-icon v-if="addingUserId === u.userId" color="#409eff"><Check /></el-icon>
          </div>
          <el-empty v-if="searchableUsers.length === 0 && memberSearchKeyword" description="未找到匹配的用户" />
        </div>
        <div class="role-select-wrap" v-if="addingUserId">
          <el-form label-width="60px">
            <el-form-item label="角色">
              <el-select v-model="addingUserRole" style="width: 100%">
                <el-option label="项目负责人" value="owner" />
                <el-option label="项目管理员" value="manager" />
                <el-option label="开发人员" value="developer" />
                <el-option label="测试人员" value="tester" />
                <el-option label="查看者" value="viewer" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </div>
      <template #footer>
        <el-button @click="addMemberDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addMemberSubmitting" :disabled="!addingUserId" @click="handleAddMemberConfirm">
          添加
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, User, CircleCheck, CircleClose, Clock, Search, Filter,
  FolderOpened, Check
} from '@element-plus/icons-vue'
import { userApi, type UserSummaryVO, type CreateUserDTO, type UpdateUserDTO } from '@/api/modules/system/user'
import { projectApi, type ProjectSummaryVO } from '@/api/modules/project/project'
import type { ProjectMemberVO } from '@/types/api'
import { getStatusType, getStatusText } from '@/utils/formatters'

const AVATAR_GRADIENTS = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
  'linear-gradient(135deg, #d299c2 0%, #fef9d7 100%)',
  'linear-gradient(135deg, #89f7fe 0%, #66a6ff 100%)',
]

const isAdmin = ref(true)
const activeTab = ref('users')

// --- 用户管理相关 ---
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const filterDialogVisible = ref(false)
const isEditMode = ref(false)
const currentUserId = ref<number | null>(null)
const formRef = ref()
const statusChangingIds = ref(new Set<number>())
const deletingIds = ref(new Set<number>())

const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const searchForm = reactive({ keyword: '' })
const filterForm = reactive({ status: '', position: '', startDate: '', endDate: '' })
const dateRange = ref<[string, string] | null>(null)

const form = reactive<CreateUserDTO & { password: string }>({
  name: '', email: '', password: '', phone: '', position: '', status: 'pending', description: ''
})

const formRules = {
  name: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

const users = ref<UserSummaryVO[]>([])
const dialogTitle = computed(() => isEditMode.value ? '编辑用户' : '创建新用户')
const activeCount = computed(() => users.value.filter(u => u.status === 'active').length)
const pendingCount = computed(() => users.value.filter(u => u.status === 'pending').length)
const inactiveCount = computed(() => users.value.filter(u => u.status === 'inactive').length)
const hasActiveFilters = computed(() => !!(filterForm.status || filterForm.position || filterForm.startDate || filterForm.endDate))

// --- 项目分配相关 ---
const projectsLoading = ref(false)
const projects = ref<ProjectSummaryVO[]>([])
const selectedProjectId = ref<number | null>(null)
const selectedProject = computed(() => projects.value.find(p => p.id === selectedProjectId.value) || null)
const projectKeyword = ref('')
const members = ref<ProjectMemberVO[]>([])
const membersLoading = ref(false)
const removingMemberIds = ref(new Set<number>())

const addMemberDialogVisible = ref(false)
const memberSearchLoading = ref(false)
const memberSearchKeyword = ref('')
const searchableUsers = ref<UserSummaryVO[]>([])
const addingUserId = ref<number | null>(null)
const addingUserRole = ref('tester')
const addMemberSubmitting = ref(false)

const filteredProjects = computed(() => {
  if (!projectKeyword.value) return projects.value
  const kw = projectKeyword.value.toLowerCase()
  return projects.value.filter(p =>
    p.name?.toLowerCase().includes(kw) || p.projectCode?.toLowerCase().includes(kw)
  )
})

// --- 通用方法 ---
function getInitials(name: string): string {
  const n = (name || '').trim()
  if (!n) return '?'
  if (/[a-zA-Z]/.test(n)) {
    const parts = n.split(/\s+/)
    return parts.length === 1 ? parts[0].charAt(0).toUpperCase()
      : (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase()
  }
  return n.length <= 2 ? n : n.slice(-2)
}

function getAvatarStyle(name: string) {
  const idx = (name?.charCodeAt(0) || 0) % AVATAR_GRADIENTS.length
  return { background: AVATAR_GRADIENTS[idx] }
}

function getStatusText(status: string): string {
  const map: Record<string, string> = { active: '活跃', pending: '待审核', inactive: '已禁用' }
  return map[status] || status
}

function getStatusTagType(status: string): string {
  const map: Record<string, string> = { active: 'success', pending: 'warning', inactive: 'danger' }
  return map[status] || 'info'
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 16)
}

// --- 用户管理方法 ---
async function loadUsers() {
  loading.value = true
  try {
    const result = await userApi.query({
      keyword: searchForm.keyword || undefined,
      status: filterForm.status || undefined,
      position: filterForm.position || undefined,
      startDate: filterForm.startDate || undefined,
      endDate: filterForm.endDate || undefined,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    users.value = result.records || []
    pagination.total = result.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载用户列表失败')
    users.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadUsers()
}

function handleFilterApply() {
  if (dateRange.value) {
    filterForm.startDate = dateRange.value[0]
    filterForm.endDate = dateRange.value[1]
  } else {
    filterForm.startDate = ''
    filterForm.endDate = ''
  }
  pagination.pageNum = 1
  filterDialogVisible.value = false
  loadUsers()
}

function handleFilterReset() {
  filterForm.status = ''
  filterForm.position = ''
  filterForm.startDate = ''
  filterForm.endDate = ''
  dateRange.value = null
  pagination.pageNum = 1
  filterDialogVisible.value = false
  loadUsers()
}

function handleOpenCreate() {
  isEditMode.value = false
  currentUserId.value = null
  Object.assign(form, { name: '', email: '', password: '', phone: '', position: '', status: 'pending', description: '' })
  dialogVisible.value = true
}

function handleEdit(row: UserSummaryVO) {
  isEditMode.value = true
  currentUserId.value = row.userId
  userApi.getDetail(row.userId).then(detail => {
    Object.assign(form, {
      name: detail.name || '', email: detail.email || '', password: '',
      phone: detail.phone || '', position: detail.position || '',
      status: detail.status || 'active', description: detail.description || ''
    })
  }).catch(() => ElMessage.error('获取用户详情失败'))
  dialogVisible.value = true
}

function handleDialogClose() {
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEditMode.value && currentUserId.value) {
      const data: UpdateUserDTO = {
        name: form.name, email: form.email,
        phone: form.phone || undefined, position: form.position || undefined,
        status: form.status as any, description: form.description || undefined
      }
      await userApi.update(currentUserId.value, data)
      ElMessage.success('用户信息更新成功')
    } else {
      await userApi.create({
        name: form.name, email: form.email, password: form.password,
        phone: form.phone || undefined, position: form.position || undefined,
        status: form.status as any, description: form.description || undefined
      })
      ElMessage.success('用户创建成功')
    }
    dialogVisible.value = false
    loadUsers()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleToggleStatus(row: UserSummaryVO) {
  if (statusChangingIds.value.has(row.userId)) return
  const nextStatus = row.status === 'active' ? 'inactive' : row.status === 'inactive' ? 'active' : 'active'
  statusChangingIds.value.add(row.userId)
  try {
    await userApi.updateStatus(row.userId, nextStatus)
    row.status = nextStatus as any
    ElMessage.success('状态更新成功')
  } catch (error: any) {
    ElMessage.error(error.message || '状态更新失败')
  } finally {
    statusChangingIds.value.delete(row.userId)
  }
}

async function handleDelete(row: UserSummaryVO) {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${row.name}" 吗？`, '提示', { type: 'warning' })
  } catch { return }
  if (deletingIds.value.has(row.userId)) return
  deletingIds.value.add(row.userId)
  try {
    await userApi.delete(row.userId)
    ElMessage.success('用户删除成功')
    if (users.value.length === 1 && pagination.pageNum > 1) pagination.pageNum--
    loadUsers()
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败')
  } finally {
    deletingIds.value.delete(row.userId)
  }
}

// --- 项目分配方法 ---
async function loadProjects() {
  projectsLoading.value = true
  try {
    const result = await projectApi.query({ pageNum: 1, pageSize: 200 })
    projects.value = result.records || []
  } catch (error: any) {
    ElMessage.error(error.message || '加载项目列表失败')
  } finally {
    projectsLoading.value = false
  }
}

function onProjectSearch() {
  // filteredProjects 通过计算属性实现
}

async function handleSelectProject(p: ProjectSummaryVO) {
  selectedProjectId.value = p.id
  await loadMembers()
}

async function loadMembers() {
  if (!selectedProjectId.value) return
  membersLoading.value = true
  members.value = []
  try {
    const detail = await projectApi.getDetail(selectedProjectId.value)
    members.value = (detail as any).members || []
  } catch (error: any) {
    ElMessage.error(error.message || '加载成员列表失败')
  } finally {
    membersLoading.value = false
  }
}

async function handleRoleChange(row: ProjectMemberVO, newRole: string) {
  if (!selectedProjectId.value) return
  try {
    await projectApi.updateMemberRole(selectedProjectId.value, row.userId, newRole)
    row.role = newRole
    ElMessage.success('角色更新成功')
  } catch (error: any) {
    ElMessage.error(error.message || '角色更新失败')
    await loadMembers()
  }
}

async function handleRemoveMember(row: ProjectMemberVO) {
  if (!selectedProjectId.value) return
  try {
    await ElMessageBox.confirm(`确定要从项目中移除成员 "${row.displayName || row.username}" 吗？`, '提示', { type: 'warning' })
  } catch { return }
  if (removingMemberIds.value.has(row.userId)) return
  removingMemberIds.value.add(row.userId)
  try {
    await projectApi.removeMember(selectedProjectId.value, row.userId)
    ElMessage.success('成员已移除')
    await loadMembers()
  } catch (error: any) {
    ElMessage.error(error.message || '移除成员失败')
  } finally {
    removingMemberIds.value.delete(row.userId)
  }
}

function handleOpenAddMember() {
  if (!selectedProjectId.value) return
  addMemberDialogVisible.value = true
  memberSearchKeyword.value = ''
  addingUserId.value = null
  addingUserRole.value = 'tester'
  searchableUsers.value = []
}

function onMemberSearch() {
  if (!memberSearchKeyword.value.trim()) {
    searchableUsers.value = []
    return
  }
  if (memberSearchTimer) clearTimeout(memberSearchTimer)
  memberSearchTimer = setTimeout(() => doSearchMembers(), 300)
}

let memberSearchTimer: ReturnType<typeof setTimeout>

async function doSearchMembers() {
  if (!memberSearchKeyword.value.trim()) return
  memberSearchLoading.value = true
  try {
    const result = await userApi.searchUsers(memberSearchKeyword.value)
    const memberUserIds = new Set(members.value.map(m => m.userId))
    searchableUsers.value = (result || []).filter(u => !memberUserIds.has(u.userId))
  } catch (error: any) {
    ElMessage.error(error.message || '搜索用户失败')
  } finally {
    memberSearchLoading.value = false
  }
}

function handleSelectUser(u: UserSummaryVO) {
  addingUserId.value = u.userId
}

async function handleAddMemberConfirm() {
  if (!selectedProjectId.value || !addingUserId.value) return
  addMemberSubmitting.value = true
  try {
    await projectApi.addMember(selectedProjectId.value, addingUserId.value, addingUserRole.value)
    ElMessage.success('成员添加成功')
    addMemberDialogVisible.value = false
    await loadMembers()
  } catch (error: any) {
    ElMessage.error(error.message || '添加成员失败')
  } finally {
    addMemberSubmitting.value = false
  }
}

onMounted(() => {
  loadUsers()
  loadProjects()
})
</script>

<style scoped lang="scss">
.personnel-page {
  padding: 20px 28px;
  background: #f0f2f5;
  min-height: 100vh;
}

.page-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 13px;
}

.breadcrumb-item {
  color: #8c8c8c;
  cursor: pointer;
  &:hover { color: #409eff; }
  &.active { color: #409eff; font-weight: 500; }
}

.breadcrumb-sep { color: #d9d9d9; }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f1f1f;
  margin: 0;
}

.page-subtitle {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.stat-icon-primary { background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%); color: #1890ff; }
.stat-icon-success { background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%); color: #52c41a; }
.stat-icon-warning  { background: linear-gradient(135deg, #fffbe6 0%, #ffe58f 100%); color: #faad14; }
.stat-icon-danger  { background: linear-gradient(135deg, #fff1f0 0%, #ffccc7 100%); color: #ff4d4f; }

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #1f1f1f;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #8c8c8c;
}

.personnel-content {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.personnel-tabs {
  :deep(.el-tabs__item) {
    font-size: 15px;
    padding: 0 20px;
    height: 44px;
    line-height: 44px;
  }
  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
  }
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-dot {
  width: 8px;
  height: 8px;
  background: #ff4d4f;
  border-radius: 50%;
  margin-left: 4px;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name { font-weight: 500; color: #1f1f1f; }
.user-id { font-size: 12px; color: #bfbfbf; }
.email-cell { color: #409eff; font-family: 'Monaco', 'Consolas', monospace; font-size: 13px; }

.status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 4px;
}

:deep(.el-tag) { display: inline-flex; align-items: center; }

.action-btns {
  display: flex;
  gap: 4px;
  align-items: center;
}

/* --- 项目分配布局 --- */
.project-assignment-layout {
  display: flex;
  gap: 20px;
  height: 560px;
}

.project-list-panel {
  width: 280px;
  flex-shrink: 0;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.member-panel {
  flex: 1;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.panel-title {
  font-weight: 600;
  font-size: 14px;
  color: #1f1f1f;
}

.project-count {
  font-size: 12px;
  color: #8c8c8c;
}

.project-search {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.project-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.project-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
  &:hover { background: #f5f5f5; }
  &.active { background: #e6f7ff; }
  & + .project-item { margin-top: 4px; }
}

.project-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  font-size: 15px;
  flex-shrink: 0;
}

.project-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.project-name {
  font-size: 13px;
  font-weight: 500;
  color: #1f1f1f;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.project-code {
  font-size: 11px;
  color: #8c8c8c;
}

.member-title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.member-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.member-panel .panel-header {
  height: 52px;
}

.member-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 12px;
  color: #8c8c8c;
  p { margin: 0; font-size: 14px; }
  .hint { font-size: 12px; color: #bfbfbf; }
}

.member-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.member-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.member-name { font-weight: 500; font-size: 13px; color: #1f1f1f; }
.member-id { font-size: 11px; color: #bfbfbf; }

/* --- 添加成员对话框 --- */
.add-member-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-user-wrap {
  margin-bottom: 4px;
}

.user-select-list {
  max-height: 280px;
  overflow-y: auto;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  padding: 4px;
}

.user-select-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s;
  &:hover { background: #f5f5f5; }
  &.selected { background: #e6f7ff; }
  & + .user-select-item { margin-top: 2px; }
}

.user-select-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.user-select-name { font-size: 13px; font-weight: 500; color: #1f1f1f; }
.user-select-email { font-size: 11px; color: #8c8c8c; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.role-select-wrap {
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

@media (max-width: 1200px) {
  .stats-cards { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .personnel-page { padding: 16px; }
  .stats-cards { grid-template-columns: 1fr; }
  .toolbar { flex-direction: column; align-items: stretch; }
  .project-assignment-layout { flex-direction: column; height: auto; }
  .project-list-panel { width: 100%; }
}
</style>
