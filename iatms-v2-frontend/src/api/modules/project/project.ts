import client from '../../client'
import type { PageResult, ProjectSummaryVO, ProjectDetailVO, ProjectMemberVO } from '@/types/api'

export type { ProjectSummaryVO, ProjectDetailVO, ProjectMemberVO }

export interface ModuleDetailVO {
  id: number
  name: string
  code: string
  projectId: number | null
  projectName: string | null
  parentId: number | null
  parentName: string | null
  description: string | null
  status: string
  ownerId: number | null
  ownerName: string | null
  createdAt: string
  updatedAt: string
  creatorName: string | null
  stats: {
    apiCount: number
    testCaseCount: number
    passedCount: number
    failedCount: number
    notExecutedCount: number
  }
}

export interface CreateProjectDTO {
  name: string
  code: string
  description?: string
  projectType?: string
  status?: string
  startDate?: string
  endDate?: string
  iconColor?: string
}

export interface ProjectQuery {
  keyword?: string
  status?: string
  projectType?: string
  ownerId?: number
  pageNum?: number
  pageSize?: number
  sortBy?: 'createdAt' | 'updatedAt' | 'name'
  sortOrder?: 'ASC' | 'DESC'
}

export const projectApi = {
  create(data: CreateProjectDTO): Promise<ProjectDetailVO> {
    return client.post('/v1/projects', data)
  },

  update(id: number, data: Partial<CreateProjectDTO>): Promise<ProjectDetailVO> {
    return client.put('/v1/projects/' + id, data)
  },

  delete(id: number): Promise<void> {
    return client.delete('/v1/projects/' + id)
  },

  getDetail(id: number): Promise<ProjectDetailVO> {
    return client.get('/v1/projects/' + id)
  },

  query(params: ProjectQuery): Promise<PageResult<ProjectSummaryVO>> {
    return client.get('/v1/projects', { params })
  },

  getRecent(limit = 10): Promise<ProjectSummaryVO[]> {
    return client.get('/v1/projects/recent', { params: { limit } })
  },

  addMember(projectId: number, userId: number, role = 'MEMBER'): Promise<void> {
    return client.post('/v1/projects/' + projectId + '/members', null, { params: { userId, role } })
  },

  removeMember(projectId: number, userId: number): Promise<void> {
    return client.delete('/v1/projects/' + projectId + '/members/' + userId)
  },

  updateMemberRole(projectId: number, userId: number, role: string): Promise<void> {
    return client.put('/v1/projects/' + projectId + '/members/' + userId, null, { params: { role } })
  },

  getModuleDetail(moduleId: number): Promise<ModuleDetailVO> {
    return client.get('/v1/modules/' + moduleId)
  },

  getModulesByProject(projectId: number): Promise<ModuleDetailVO[]> {
    return client.get('/v1/projects/' + projectId + '/modules')
  }
}
