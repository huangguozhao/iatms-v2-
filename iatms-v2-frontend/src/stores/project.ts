import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { projectApi } from '@/api/modules/project/project'
import type { ProjectSummaryVO, ProjectDetailVO } from '@/types/api'

export const useProjectStore = defineStore('project', () => {
  // 状态
  const projects = ref<ProjectSummaryVO[]>([])
  const currentProject = ref<ProjectDetailVO | null>(null)
  const loading = ref(false)
  const pagination = ref({
    pageNum: 1,
    pageSize: 10,
    total: 0
  })

  // 计算属性
  const projectMap = computed(() => {
    const map = new Map<number, ProjectSummaryVO>()
    projects.value.forEach(p => map.set(p.id, p))
    return map
  })

  const activeProjects = computed(() =>
    projects.value.filter(p => p.status === 'IN_PROGRESS')
  )

  // Actions
  async function fetchProjects(params?: {
    keyword?: string
    status?: string
    pageNum?: number
    pageSize?: number
  }) {
    loading.value = true
    try {
      const result = await projectApi.query({
        pageNum: params?.pageNum ?? pagination.value.pageNum,
        pageSize: params?.pageSize ?? pagination.value.pageSize,
        keyword: params?.keyword,
        status: params?.status
      })
      projects.value = result.records || []
      pagination.value.total = result.total || 0
      return result
    } finally {
      loading.value = false
    }
  }

  async function fetchProjectById(id: number) {
    loading.value = true
    try {
      const result = await projectApi.getDetail(id)
      currentProject.value = result
      return result
    } finally {
      loading.value = false
    }
  }

  async function createProject(data: {
    name: string
    code: string
    description?: string
    projectType?: string
    status?: string
  }) {
    const result = await projectApi.create(data as any)
    await fetchProjects()
    return result
  }

  async function updateProject(id: number, data: {
    name: string
    code: string
    description?: string
    projectType?: string
    status?: string
  }) {
    const result = await projectApi.update(id, data as any)
    await fetchProjects()
    return result
  }

  async function deleteProject(id: number) {
    const result = await projectApi.delete(id)
    projects.value = projects.value.filter(p => p.id !== id)
    return result
  }

  function getProjectById(id: number) {
    return projectMap.value.get(id)
  }

  function clearCurrentProject() {
    currentProject.value = null
  }

  return {
    // 状态
    projects,
    currentProject,
    loading,
    pagination,
    // 计算属性
    projectMap,
    activeProjects,
    // Actions
    fetchProjects,
    fetchProjectById,
    createProject,
    updateProject,
    deleteProject,
    getProjectById,
    clearCurrentProject
  }
})
