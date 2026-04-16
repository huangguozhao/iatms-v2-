export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
  hasNext: boolean
  hasPrevious: boolean
}

export interface LoginVO {
  token: string
  userId: number
  username: string
  displayName: string
  email: string
  avatarUrl?: string
}

export interface ProjectSummaryVO {
  id: number
  name: string
  code: string
  projectType: string
  status: string
  startDate?: string
  endDate?: string
  ownerId: number
  ownerName: string
  iconColor?: string
  createdAt: string
}

export interface ProjectDetailVO extends ProjectSummaryVO {
  description?: string
  members: ProjectMemberVO[]
  totalModules: number
  totalApis: number
  totalTestCases: number
  updatedAt: string
  creatorName: string
}

export interface ProjectMemberVO {
  id: number
  userId: number
  username: string
  displayName: string
  avatarUrl?: string
  role: string
}

// API types
export interface ApiSummaryVO {
  id: number
  name: string
  method: string
  path: string
  projectId: number
  projectName: string
  moduleId: number | null
  moduleName: string | null
  description: string
  status: string
  createdAt: string
}

export interface ApiDetailVO extends ApiSummaryVO {
  headers: Record<string, string>
  requestBody: string
  responseBody: string
  assertions: string
}

export interface CreateApiDTO {
  name: string
  projectId?: number | null
  moduleId?: number | null
  method: string
  path: string
  description?: string
  headers?: Record<string, string>
  requestBody?: string
  assertions?: string
}

export interface ApiQuery {
  keyword?: string
  method?: string
  projectId?: number
  moduleId?: number
  pageNum: number
  pageSize: number
}

// TestCase types
export interface TestCaseSummaryVO {
  id: number
  name: string
  apiId: number
  apiName: string
  projectId: number
  projectName: string
  priority: string
  status: string
  createdAt: string
}

export interface TestCaseDetailVO extends TestCaseSummaryVO {
  headers: Record<string, string>
  requestBody: string
  assertions: string
  extractors: string
  description: string
}

// TestSuite types
export interface TestSuiteSummaryVO {
  id: number
  name: string
  projectId: number
  projectName: string
  caseCount: number
  executionStrategy: string
  failStrategy: string
  status: string
  createdAt: string
}

export interface TestSuiteDetailVO extends TestSuiteSummaryVO {
  caseIds: number[]
  description: string
}

export interface CreateTestSuiteDTO {
  name: string
  projectId: number
  caseIds: number[]
  executionStrategy: string
  failStrategy: string
  description?: string
}

export interface TestSuiteQuery {
  keyword?: string
  projectId?: number
  pageNum: number
  pageSize: number
}

// Execution types
export interface ExecutionSummaryVO {
  id: number
  type: string
  targetId: number
  targetName: string
  status: string
  statusText: string
  total: number
  passed: number
  failed: number
  skipped: number
  duration: number
  executor: string
  createdAt: string
}

export interface TestResultVO {
  id: number
  name: string
  status: string
  duration: number
  errorMessage: string
  requestData: string
  responseData: string
  assertions: AssertionResult[]
}

export interface AssertionResult {
  expression: string
  expected: string
  actual: string
  passed: boolean
}

export interface ExecutionDetailVO extends ExecutionSummaryVO {
  results: TestResultVO[]
}

export interface ExecutionProgressVO {
  executionId: number
  status: string
  total: number
  passed: number
  failed: number
  current: number
  currentName: string
}

// ScheduledTask types
export interface ScheduledTaskSummaryVO {
  id: number
  name: string
  type: string
  targetId: number
  targetName: string
  cron: string
  status: string
  nextRunTime: string
  lastRunTime: string | null
  createdAt: string
}

export interface ScheduledTaskDetailVO extends ScheduledTaskSummaryVO {
  notifyOn: string[]
  description: string
}

// Report types
export interface ReportSummaryVO {
  id: number
  executionId: number
  projectId: number
  projectName: string
  type: string
  status: string
  total: number
  passed: number
  failed: number
  skipped: number
  passRate: number
  duration: number
  createdAt: string
}

export interface ReportResult {
  id: number
  name: string
  method: string
  path: string
  status: string
  duration: number
  errorMessage: string
  requestData: string
  responseData: string
  assertions: AssertionResult[]
  logs: string[]
}

export interface ReportDetailVO extends ReportSummaryVO {
  results: ReportResult[]
  summary: string
}

// Notification types
export interface NotificationVO {
  id: number
  type: string
  title: string
  content: string
  relatedId?: number
  relatedType?: string
  read: boolean
  createdAt: string
}
