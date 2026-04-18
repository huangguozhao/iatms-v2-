import client from '../../client'

export interface UserSummaryVO {
  userId: number
  name: string
  email: string
  avatarUrl?: string
  phone?: string
  position?: string
  departmentId?: number
  departmentName?: string
  employeeId?: string
  status: 'active' | 'inactive' | 'pending'
  role: string
  createdAt: string
  lastLoginTime?: string
}

export interface UserDetailVO extends UserSummaryVO {
  description?: string
}

export interface CreateUserDTO {
  name: string
  email: string
  password?: string
  phone?: string
  position?: string
  departmentId?: number
  employeeId?: string
  description?: string
  status?: 'active' | 'inactive' | 'pending'
}

export interface UpdateUserDTO {
  name?: string
  email?: string
  phone?: string
  position?: string
  departmentId?: number
  employeeId?: string
  description?: string
  status?: 'active' | 'inactive' | 'pending'
}

export interface UserQuery {
  keyword?: string
  status?: string
  position?: string
  departmentId?: number
  startDate?: string
  endDate?: string
  pageNum: number
  pageSize: number
}

export interface DepartmentVO {
  departmentId: number
  departmentCode: string
  departmentName: string
  parentId?: number
  managerId?: number
  managerName?: string
  description?: string
  level?: number
  status?: string
  children?: DepartmentVO[]
}

export const userApi = {
  query(params: UserQuery) {
    return client.get('/v1/users', { params })
  },

  getDetail(id: number) {
    return client.get(`/v1/users/${id}`)
  },

  create(data: CreateUserDTO) {
    return client.post('/v1/users', data)
  },

  update(id: number, data: UpdateUserDTO) {
    return client.put(`/v1/users/${id}`, data)
  },

  updateStatus(id: number, status: string) {
    return client.put(`/v1/users/${id}/status`, { status })
  },

  delete(id: number) {
    return client.delete(`/v1/users/${id}`)
  },

  searchUsers(keyword: string) {
    return client.get('/v1/users/search', { params: { keyword } })
  },

  listDepartments() {
    return client.get('/v1/users/departments')
  },

  listDepartmentsTree() {
    return client.get('/v1/users/departments/tree')
  },
}
