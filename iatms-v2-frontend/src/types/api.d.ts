// User types
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
  userId: number
  userName?: string
  displayName?: string
  avatar?: string
  role: string
  joinedAt?: string
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
  baseUrl?: string
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
  requestBodyType?: string
  responseBodyType?: string
  tags?: string
  timeoutSeconds?: number
}

export interface CreateApiDTO {
  name: string
  description?: string
  collectionId: number
  httpMethod: string
  url: string
  path?: string
  baseUrl?: string
  headers?: Record<string, string>
  queryParams?: string
  requestBody?: string
  requestBodyType?: string
  responseBodyType?: string
  authConfig?: string
  tags?: string
  timeoutSeconds?: number
  status?: string
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
  severity?: string
  tags?: string
  preconditions?: string
  testSteps?: string
  testData?: string
  headers?: Record<string, string> | string
  requestParams?: string
  requestBody?: string
  assertions?: string
  expectedResponse?: string
  extractors?: string
  expectedHttpStatus?: number
  expectedResponseSchema?: string
  expectedResponseBody?: string
  requestOverride?: string
  isEnabled?: boolean
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
  taskType: string
  triggerType: string
  status: string
  nextRunTime: string
  lastRunTime: string | null
  createdAt: string
  // 后端返回字段别名
  type?: string
  targetId?: number
  targetName?: string
  cron?: string
  // 扩展字段（来自后端实体）
  description?: string
  executionEnvironment?: string
  totalRuns?: number
  successRuns?: number
  failedRuns?: number
  totalExecutions?: number
  successfulExecutions?: number
  failedExecutions?: number
  successRate?: number
  lastExecutionStatus?: string
}

export interface ScheduledTaskDetailVO extends ScheduledTaskSummaryVO {
  notifyOn: string[]
  description: string
  // 执行计划
  cronExpression?: string
  simpleRepeatInterval?: number
  simpleRepeatCount?: number
  dailyHour?: number
  dailyMinute?: number
  weeklyDays?: string
  monthlyDay?: number
  // 高级设置
  timeoutSeconds?: number
  retryEnabled?: boolean
  maxRetryAttempts?: number
  notifyOnSuccess?: boolean
  notifyOnFailure?: boolean
  // 用例关联
  caseIds?: number[]
  caseCount?: number
  // 统计
  skippedExecutions?: number
}

export interface CreateScheduledTaskDTO {
  name: string
  type: string
  targetId: number
  cron: string
  strategy: string
  notifyOn: string[]
  description?: string
  // 执行计划
  triggerType?: string
  cronExpression?: string
  dailyHour?: number
  dailyMinute?: number
  weeklyDays?: string
  monthlyDay?: number
  simpleRepeatInterval?: number
  simpleRepeatCount?: number
  // 高级设置
  timeoutSeconds?: number
  retryEnabled?: boolean
  maxRetryAttempts?: number
  notifyOnSuccess?: boolean
  notifyOnFailure?: boolean
  // 用例关联
  caseIds?: number[]
}

export interface ScheduledTaskQuery {
  keyword?: string
  type?: string
  status?: string
  projectId?: number
  pageNum: number
  pageSize: number
}

// 定时任务执行记录
export interface ScheduledTaskExecutionVO {
  executionId: number
  taskId: number
  status: string
  scheduledTime: string
  actualStartTime: string | null
  actualEndTime: string | null
  durationSeconds: number | null
  totalCases: number
  passedCases: number
  failedCases: number
  skippedCases: number
  successRate: number
  errorMessage: string | null
  retryCount: number
  isRetry: boolean
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
