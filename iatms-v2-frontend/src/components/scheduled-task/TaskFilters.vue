<template>
  <div class="filter-section">
    <div class="filter-left">
      <el-select
        v-model="localFilters.projectId"
        placeholder="所属项目"
        class="filter-item"
        clearable
        @change="handleFilterChange"
      >
        <el-option label="全部项目" :value="undefined" />
        <el-option
          v-for="project in projects"
          :key="project.id"
          :label="project.name"
          :value="project.id"
        />
      </el-select>

      <el-select
        v-model="localFilters.status"
        placeholder="全部状态"
        class="filter-item"
        clearable
        @change="handleFilterChange"
      >
        <el-option label="全部状态" :value="undefined" />
        <el-option label="启用" value="enabled" />
        <el-option label="禁用" value="disabled" />
      </el-select>

      <div class="frequency-filter">
        <span class="filter-label">执行频率：</span>
        <el-button
          v-for="freq in frequencyTypes"
          :key="freq.value"
          :type="localFilters.frequency === freq.value ? 'primary' : 'default'"
          @click="handleFrequencyChange(freq.value)"
          class="frequency-btn"
        >
          {{ freq.label }}
        </el-button>
      </div>

      <el-date-picker
        v-model="localFilters.dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        class="date-picker"
        value-format="YYYY-MM-DD"
        @change="handleFilterChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { projectApi } from '@/api/modules/project/project'

export interface TaskFilters {
  projectId?: number
  status?: string
  frequency: string
  dateRange: string[] | null
}

const props = defineProps<{
  modelValue: TaskFilters
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: TaskFilters): void
  (e: 'change', value: TaskFilters): void
}>()

const localFilters = reactive<TaskFilters>({
  projectId: props.modelValue.projectId,
  status: props.modelValue.status,
  frequency: props.modelValue.frequency || 'all',
  dateRange: props.modelValue.dateRange || null
})

const frequencyTypes = [
  { label: '全部', value: 'all' },
  { label: '每日', value: 'daily' },
  { label: '每周', value: 'weekly' },
  { label: '每月', value: 'monthly' }
]

const projects = ref<{ id: number; name: string }[]>([])

async function loadProjects() {
  try {
    const res = await projectApi.query({ pageNum: 1, pageSize: 100 })
    if (res) {
      projects.value = (res.records || []).map((p: any) => ({
        id: p.id,
        name: p.name
      }))
    }
  } catch {
    projects.value = []
  }
}

function handleFrequencyChange(value: string) {
  localFilters.frequency = value
  handleFilterChange()
}

function handleFilterChange() {
  emit('update:modelValue', { ...localFilters })
  emit('change', { ...localFilters })
}

onMounted(() => {
  loadProjects()
})
</script>

<style scoped lang="scss">
.filter-section {
  margin-bottom: 20px;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  box-shadow:
    0 4px 16px rgba(0, 0, 0, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-item {
  width: 160px;
}

.filter-label {
  font-size: 13px;
  color: #8c92a4;
  font-weight: 500;
  white-space: nowrap;
}

.frequency-filter {
  display: flex;
  align-items: center;
  gap: 8px;
}

.frequency-btn {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.frequency-btn:hover {
  transform: translateY(-1px);
}

.date-picker {
  width: 280px;
}
</style>
