/**
 * 项目和模块加载 Composable
 */
import { ref } from 'vue'
import { projectApi } from '@/api/modules/project/project'

export interface ProjectOption {
  id: number
  name: string
  [key: string]: any
}

export interface ModuleOption {
  id: number
  name: string
  [key: string]: any
}

export function useProjectModules() {
  const availableProjects = ref<ProjectOption[]>([])
  const projectsLoading = ref(false)

  const availableModules = ref<ModuleOption[]>([])
  const modulesLoading = ref(false)

  const loadProjects = async () => {
    if (availableProjects.value.length > 0) return

    projectsLoading.value = true
    try {
      const res = await projectApi.query({ pageNum: 1, pageSize: 100 })
      availableProjects.value = res.list || []
    } catch (e) {
      console.error('加载项目列表失败', e)
      availableProjects.value = []
    } finally {
      projectsLoading.value = false
    }
  }

  const loadModules = async (projectId: number) => {
    modulesLoading.value = true
    try {
      const res = await projectApi.getModuleDetail(projectId)
      availableModules.value = res ? [res as ModuleOption] : []
    } catch (e) {
      console.error('加载模块列表失败', e)
      availableModules.value = []
    } finally {
      modulesLoading.value = false
    }
  }

  const reset = () => {
    availableProjects.value = []
    availableModules.value = []
  }

  return {
    availableProjects,
    projectsLoading,
    availableModules,
    modulesLoading,
    loadProjects,
    loadModules,
    reset
  }
}
