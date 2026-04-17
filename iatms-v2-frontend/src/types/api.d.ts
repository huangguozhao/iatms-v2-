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
  totalModules?: number
  totalApis?: number
  totalTestCases?: number
  passedCount?: number
  failedCount?: number
  notExecutedCount?: number
  updatedAt?: string
  creatorName?: string
  startDate?: string
  endDate?: string
  iconColor?: string
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
  requestType?: string
  httpMethod?: string
  url?: string
  headers?: Record<string, string> | string
  queryParams?: string
  requestBody?: string
  authConfig?: string
  collectionId?: number
  collectionName?: string
  orderNum?: number
  createdBy?: number
  creatorName?: string
  updatedAt?: string
}

export interface CreateApiDTO {
  name: string
  description?: string
  collectionId: number
  httpMethod: string
  url: string
  headers?: Record<string, string>
  queryParams?: string
  requestBody?: string
  authConfig?: string
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
  caseCode?: string
  name: string
  apiId?: number
  apiName?: string
  projectId: number
  projectName?: string
  moduleId?: number
  priority: string
  status: string
  createdAt: string
  createdBy?: string
}

export interface TestCaseDetailVO extends TestCaseSummaryVO {
  caseCode?: string
  description?: string
  testType?: string
  preconditions?: string
  testSteps?: string
  testData?: string
  headers?: Record<string, string> | string
  requestParams?: string
  requestBody?: string
  assertions?: string
  expectedResponse?: string
  extractors?: string
  updatedAt?: string
  creatorName?: string
  creator?: { name?: string }
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
