<template>
  <div class="create-task-page">
    <div class="page-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="breadcrumb">
          <span class="breadcrumb-item" @click="$router.push('/scheduled-tasks')">任务安排</span>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-item active">{{ isEdit ? '编辑定时任务' : '创建定时任务' }}</span>
        </div>
        <h1 class="page-title">{{ isEdit ? '编辑定时任务' : '创建定时任务' }}</h1>
      </div>

      <!-- 表单区域 -->
      <div class="form-container">
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="140px"
          class="task-form"
        >
          <!-- 基本信息 -->
          <div class="form-section">
            <div class="section-header">
              <h3 class="section-title">基本信息</h3>
            </div>

            <div class="form-grid">
              <el-form-item label="任务名称" prop="name" class="form-item-full">
                <el-input
                  v-model="formData.name"
                  placeholder="请输入任务名称"
                  maxlength="100"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="任务描述" prop="description" class="form-item-full">
                <el-input
                  v-model="formData.description"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入任务描述"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="所属项目" prop="projectId">
                <el-select
                  v-model="formData.projectId"
                  placeholder="请选择项目"
                  filterable
                  style="width: 100%"
                  :disabled="isEdit"
                  @change="handleProjectChange"
                >
                  <el-option
                    v-for="project in projects"
                    :key="project.id"
                    :label="project.name"
                    :value="project.id"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="执行目标" prop="targetId">
                <el-select
                  v-model="selectedTarget"
                  placeholder="请先选择项目"
                  filterable
                  style="width: 100%"
                  :disabled="!formData.projectId"
                  @change="handleTargetChange"
                >
                  <el-option-group
                    v-for="group in targetGroups"
                    :key="group.label"
                    :label="group.label"
                  >
                    <el-option
                      v-for="item in group.options"
                      :key="item.targetId"
                      :label="item.displayName"
                      :value="`${group.type}:${item.targetId}`"
                    />
                  </el-option-group>
                </el-select>
              </el-form-item>

              <el-form-item label="任务状态" prop="status">
                <el-switch v-model="formData.status" />
                <span class="switch-label">{{ formData.status ? '启用' : '禁用' }}</span>
              </el-form-item>
            </div>
          </div>

          <!-- 执行计划 -->
          <div class="form-section">
            <div class="section-header">
              <h3 class="section-title">执行计划</h3>
            </div>

            <!-- 执行频率 -->
            <div class="frequency-section">
              <el-form-item label="执行频率" prop="frequency">
                <div class="frequency-options">
                  <el-button
                    v-for="freq in frequencyOptions"
                    :key="freq.value"
                    :type="formData.frequency === freq.value ? 'primary' : 'default'"
                    @click="formData.frequency = freq.value"
                    class="frequency-btn"
                  >
                    <el-icon><Calendar /></el-icon>
                    {{ freq.label }}
                  </el-button>
                </div>
              </el-form-item>
            </div>

            <!-- 执行周期设置 -->
            <div class="cycle-section" v-if="formData.frequency === 'WEEKLY'">
              <el-form-item label="执行日期" prop="selectedDays">
                <div class="weekday-options">
                  <el-button
                    v-for="day in weekdays"
                    :key="day.value"
                    :type="formData.selectedDays.includes(day.value) ? 'primary' : 'default'"
                    @click="toggleDay(day.value)"
                    class="weekday-btn"
                  >
                    {{ day.label }}
                  </el-button>
                </div>
              </el-form-item>
            </div>

            <!-- 执行时间 -->
            <div class="time-section">
              <el-form-item label="执行时间" prop="executionTime">
                <el-time-picker
                  v-model="formData.executionTime"
                  placeholder="选择执行时间"
                  format="HH:mm"
                  value-format="HH:mm"
                  style="width: 200px"
                />
              </el-form-item>
            </div>

            <!-- Cron表达式 -->
            <div class="cron-section" v-if="formData.frequency === 'CRON'">
              <el-form-item label="Cron表达式" prop="cronExpression">
                <el-input
                  v-model="formData.cronExpression"
                  placeholder="例如: 0 0 2 * * ?"
                  style="width: 300px"
                />
                <div class="cron-hint">
                  格式: 秒 分 时 日 月 周 [年]<br>
                  例如: 0 0 2 * * ? 表示每天凌晨2点执行
                </div>
              </el-form-item>
            </div>

            <!-- 简单重复设置 -->
            <div class="simple-section" v-if="formData.frequency === 'SIMPLE'">
              <el-form-item label="重复间隔" prop="simpleRepeatInterval">
                <div class="repeat-config">
                  <span>每隔</span>
                  <el-input-number
                    v-model="formData.simpleRepeatInterval"
                    :min="1"
                    :max="9999"
                    style="width: 100px; margin: 0 10px"
                  />
                  <el-select v-model="formData.simpleRepeatUnit" style="width: 120px; margin: 0 10px">
                    <el-option label="秒" value="SECONDS" />
                    <el-option label="分钟" value="MINUTES" />
                    <el-option label="小时" value="HOURS" />
                    <el-option label="天" value="DAYS" />
                  </el-select>
                  <span>执行</span>
                  <el-input-number
                    v-model="formData.simpleRepeatCount"
                    :min="1"
                    :max="9999"
                    style="width: 100px; margin: 0 10px"
                  />
                  <span>次</span>
                </div>
              </el-form-item>
            </div>
          </div>

          <!-- 高级设置 -->
          <div class="form-section">
            <div class="section-header">
              <h3 class="section-title">高级设置</h3>
            </div>

            <div class="form-grid">
              <el-form-item label="超时时间(分钟)" prop="timeout">
                <el-input-number
                  v-model="formData.timeout"
                  :min="1"
                  :max="1440"
                  placeholder="执行超时时间"
                />
              </el-form-item>

              <el-form-item label="重试次数" prop="retryCount">
                <el-input-number
                  v-model="formData.retryCount"
                  :min="0"
                  :max="10"
                  placeholder="失败重试次数"
                />
              </el-form-item>

              <el-form-item label="成功通知" prop="notifyOnSuccess">
                <el-switch v-model="formData.notifyOnSuccess" />
                <span class="switch-label">{{ formData.notifyOnSuccess ? '开启' : '关闭' }}</span>
              </el-form-item>

              <el-form-item label="失败通知" prop="notifyOnFailure">
                <el-switch v-model="formData.notifyOnFailure" />
                <span class="switch-label">{{ formData.notifyOnFailure ? '开启' : '关闭' }}</span>
              </el-form-item>
            </div>
          </div>

        </el-form>

        <!-- 操作按钮 -->
        <div class="form-actions">
          <el-button @click="$router.go(-1)">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '保存' : '创建' }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Delete, Calendar } from '@element-plus/icons-vue'
import { scheduledTaskApi } from '@/api/modules/scheduling/scheduledTask'
import { projectApi } from '@/api/modules/project/project'
import { testSuiteApi } from '@/api/modules/testing/testSuite'
import { testCaseApi } from '@/api/modules/testing/testCase'
import { apiApi } from '@/api/modules/testing/api'

const route = useRoute()
const router = useRouter()

const taskId = computed(() => Number(route.params.id))
const isEdit = computed(() => !!taskId.value && !isNaN(taskId.value))

const formRef = ref()
const submitting = ref(false)
const projects = ref<{ id: number; name: string }[]>([])
const targetGroups = ref<{ label: string; type: string; options: { targetId: number; displayName: string }[] }[]>([])
const selectedTarget = ref<string>('')
const selectedTargetType = ref<string>('')
const selectedTargetId = ref<number | undefined>(undefined)

const formData = reactive({
  name: '',
  description: '',
  projectId: undefined as number | undefined,
  targetId: undefined as number | undefined,
  status: true,
  frequency: 'DAILY',
  selectedDays: ['THURSDAY'] as string[],
  executionTime: '10:00',
  cronExpression: '',
  simpleRepeatInterval: 1,
  simpleRepeatUnit: 'HOURS',
  simpleRepeatCount: 1,
  timeout: 60,
  retryCount: 0,
  notifyOnSuccess: false,
  notifyOnFailure: true
})

const formRules = {
  name: [
    { required: true, message: '请输入任务名称', trigger: 'blur' },
    { min: 1, max: 100, message: '任务名称长度在1-100个字符', trigger: 'blur' }
  ],
  projectId: [
    { required: true, message: '请选择所属项目', trigger: 'change' }
  ],
  targetId: [
    { required: true, message: '请选择执行目标', trigger: 'change' }
  ],
  executionTime: [
    { required: true, message: '请选择执行时间', trigger: 'change' }
  ]
}

const frequencyOptions = [
  { label: '每日', value: 'DAILY' },
  { label: '每周', value: 'WEEKLY' },
  { label: '每月', value: 'MONTHLY' },
  { label: 'Cron', value: 'CRON' },
  { label: '简单重复', value: 'SIMPLE' }
]

const weekdays = [
  { label: '周一', value: 'MONDAY' },
  { label: '周二', value: 'TUESDAY' },
  { label: '周三', value: 'WEDNESDAY' },
  { label: '周四', value: 'THURSDAY' },
  { label: '周五', value: 'FRIDAY' },
  { label: '周六', value: 'SATURDAY' },
  { label: '周日', value: 'SUNDAY' }
]

function toggleDay(day: string) {
  const index = formData.selectedDays.indexOf(day)
  if (index > -1) {
    formData.selectedDays.splice(index, 1)
  } else {
    formData.selectedDays.push(day)
  }
}

async function loadTargets() {
  if (!formData.projectId) {
    targetGroups.value = []
    return
  }

  const groups: { label: string; type: string; options: { targetId: number; displayName: string }[] }[] = []

  try {
    const [suiteRes, caseRes, apiRes] = await Promise.allSettled([
      testSuiteApi.query({ projectId: formData.projectId, pageNum: 1, pageSize: 100 }),
      testCaseApi.query({ projectId: formData.projectId, pageNum: 1, pageSize: 100 }),
      apiApi.query({ projectId: formData.projectId, pageNum: 1, pageSize: 100 })
    ])

    if (suiteRes.status === 'fulfilled' && suiteRes.value) {
      groups.push({
        label: '测试套件',
        type: 'TEST_SUITE',
        options: (suiteRes.value.records || []).map((s: any) => ({
          targetId: s.id,
          displayName: s.name || s.suiteName
        }))
      })
    }
    if (caseRes.status === 'fulfilled' && caseRes.value) {
      groups.push({
        label: '测试用例',
        type: 'TEST_CASE',
        options: (caseRes.value.records || []).map((c: any) => ({
          targetId: c.id,
          displayName: c.name
        }))
      })
    }
    if (apiRes.status === 'fulfilled' && apiRes.value) {
      groups.push({
        label: '接口',
        type: 'API',
        options: (apiRes.value.records || []).map((a: any) => ({
          targetId: a.id,
          displayName: a.name
        }))
      })
    }
  } catch {
    // ignore
  }

  targetGroups.value = groups
}

function handleProjectChange() {
  selectedTarget.value = ''
  formData.targetId = undefined
  selectedTargetType.value = ''
  loadTargets()
}

function handleTargetChange(val: string) {
  const [type, id] = val.split(':')
  selectedTargetType.value = type
  formData.targetId = parseInt(id)
}

async function loadTaskDetail() {
  if (!isEdit.value) return

  try {
    const res = await scheduledTaskApi.getDetail(taskId.value)
    if (res) {
      formData.name = res.name || ''
      formData.description = res.description || ''
      formData.projectId = res.projectId
      formData.targetId = res.targetId
      selectedTargetType.value = res.targetName || ''
      if (res.targetId && res.targetName) {
        selectedTarget.value = `${res.targetName}:${res.targetId}`
      }
      formData.status = res.status === 'ACTIVE' || res.status === 'ENABLED'

      formData.frequency = (res.triggerType || res.frequency || 'DAILY').toUpperCase()
      formData.timeout = res.timeoutSeconds ? Math.ceil(res.timeoutSeconds / 60) : 60
      formData.retryCount = res.maxRetryAttempts || 0
      formData.notifyOnSuccess = res.notifyOnSuccess || false
      formData.notifyOnFailure = res.notifyOnFailure || false

      if (res.dailyHour !== undefined) {
        formData.executionTime = `${String(res.dailyHour).padStart(2, '0')}:${String(res.dailyMinute || 0).padStart(2, '0')}`
      }

      if (res.weeklyDays) {
        formData.selectedDays = res.weeklyDays.split(',').map((d: string) => d.trim().toUpperCase())
      }

      if (res.cronExpression) {
        formData.cronExpression = res.cronExpression
      }

      if (res.simpleRepeatInterval) {
        formData.simpleRepeatInterval = res.simpleRepeatInterval
      }
    }
  } catch {
    ElMessage.error('加载任务详情失败')
  }
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    ElMessage.warning('请填写必填项')
    return
  }

  if (formData.frequency === 'WEEKLY' && formData.selectedDays.length === 0) {
    ElMessage.warning('请选择至少一个执行日期')
    return
  }

  submitting.value = true

  try {
    const timeParts = formData.executionTime.split(':')
    const dailyHour = parseInt(timeParts[0])
    const dailyMinute = parseInt(timeParts[1])

    const weekDayMap: Record<string, string> = {
      'MONDAY': '1',
      'TUESDAY': '2',
      'WEDNESDAY': '3',
      'THURSDAY': '4',
      'FRIDAY': '5',
      'SATURDAY': '6',
      'SUNDAY': '7'
    }
    const weeklyDays = formData.frequency === 'WEEKLY' && formData.selectedDays.length > 0
      ? formData.selectedDays.map(day => weekDayMap[day]).join(',')
      : undefined

    const submitData: any = {
      name: formData.name,
      description: formData.description,
      taskType: selectedTargetType.value,
      targetId: formData.targetId,
      triggerType: formData.frequency,
      cronExpression: formData.cronExpression || `0 ${dailyMinute} ${dailyHour} * * ?`,
      dailyHour,
      dailyMinute,
      weeklyDays,
      monthlyDay: formData.frequency === 'MONTHLY' ? 1 : undefined,
      simpleRepeatInterval: formData.frequency === 'SIMPLE' ? formData.simpleRepeatInterval * getIntervalMs(formData.simpleRepeatUnit) : undefined,
      simpleRepeatCount: formData.frequency === 'SIMPLE' ? formData.simpleRepeatCount : undefined,
      timeoutSeconds: formData.timeout * 60,
      retryEnabled: formData.retryCount > 0,
      maxRetryAttempts: formData.retryCount,
      notifyOnSuccess: formData.notifyOnSuccess,
      notifyOnFailure: formData.notifyOnFailure
    }

    if (isEdit.value) {
      await scheduledTaskApi.update(taskId.value, submitData)
      ElMessage.success('更新成功')
    } else {
      await scheduledTaskApi.create(submitData)
      ElMessage.success('创建成功')
    }
    router.push('/scheduled-tasks')
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

function getIntervalMs(unit: string): number {
  const map: Record<string, number> = {
    SECONDS: 1000,
    MINUTES: 60 * 1000,
    HOURS: 60 * 60 * 1000,
    DAYS: 24 * 60 * 60 * 1000
  }
  return map[unit] || 60 * 1000
}

onMounted(async () => {
  await loadProjects()
  if (isEdit.value) {
    await loadTaskDetail()
  }
})
</script>

<style scoped lang="scss">
.create-task-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.page-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.breadcrumb {
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
}

.breadcrumb-item {
  &:hover {
    color: #409eff;
  }

  &.active {
    color: #409eff;
    font-weight: 500;
  }
}

.breadcrumb-separator {
  margin: 0 8px;
  color: #ccc;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.form-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 24px;
  margin-bottom: 24px;
}

.form-section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-item-full {
  grid-column: 1 / -1;
}

.frequency-section,
.cycle-section,
.time-section,
.cron-section,
.simple-section {
  margin-bottom: 20px;
}

.frequency-options,
.weekday-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.frequency-btn,
.weekday-btn {
  min-width: 80px;
}

.repeat-config {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.cron-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}

.switch-label {
  margin-left: 8px;
  color: #606266;
}

.selected-cases {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  overflow: hidden;
}

.module-header {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.module-name {
  display: flex;
  align-items: center;
  gap: 6px;
}

.api-list {
  padding: 12px;
  background-color: #f5f7fa;
}

.api-item {
  margin-bottom: 16px;

  &:last-child {
    margin-bottom: 0;
  }
}

.api-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding: 8px 12px;
  background-color: #fff;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.api-name {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  color: #409eff;
}

.case-list {
  padding-left: 24px;
}

.case-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  margin-bottom: 8px;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.case-name {
  font-weight: normal;
  color: #606266;
}

.empty-cases {
  padding: 40px 20px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  background-color: #fafafa;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .frequency-options,
  .weekday-options {
    justify-content: center;
  }
}
</style>
