/**
 * 公共格式化函数
 */

// 状态类型映射
export function getStatusType(status: string): string {
  const map: Record<string, string> = {
    // 项目状态 (与数据库一致)
    ACTIVE: 'success',
    INACTIVE: 'info',
    ARCHIVED: 'warning',
    NOT_STARTED: 'info',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success',
    // 执行状态 (与数据库 testexecutionrecords.status 一致: running/completed/failed/cancelled)
    running: 'primary',
    completed: 'success',
    failed: 'danger',
    cancelled: 'warning',
    pending: 'info',
    // 通用
    ENABLED: 'success',
    DISABLED: 'info'
  }
  return map[status] || 'info'
}

export function getStatusText(status: string): string {
  const map: Record<string, string> = {
    // 项目状态
    ACTIVE: '激活',
    INACTIVE: '停用',
    ARCHIVED: '归档',
    NOT_STARTED: '未开始',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    // 执行状态 (与数据库一致)
    running: '执行中',
    completed: '已完成',
    failed: '失败',
    cancelled: '已取消',
    pending: '待执行',
    // 通用
    ENABLED: '启用',
    DISABLED: '禁用'
  }
  return map[status] || status
}

/**
 * API 状态相关
 */
export function getApiStatusTagType(status?: string): string {
  const map: Record<string, string> = {
    ACTIVE: 'success',
    INACTIVE: 'info',
    DEPRECATED: 'warning'
  }
  return map[status || ''] || 'info'
}

export function getApiStatusText(status?: string): string {
  const map: Record<string, string> = {
    ACTIVE: '活跃',
    INACTIVE: '未激活',
    DEPRECATED: '已废弃'
  }
  return map[status || ''] || status || '-'
}

/**
 * HTTP 方法标签类型
 */
export function getMethodTagType(method?: string): string {
  const map: Record<string, string> = {
    GET: 'success',
    POST: 'warning',
    PUT: 'primary',
    DELETE: 'danger',
    PATCH: 'info'
  }
  return map[method?.toUpperCase() || ''] || 'info'
}

/**
 * 认证类型文本
 */
export function getAuthTypeText(authType?: string): string {
  const map: Record<string, string> = {
    none: '无',
    bearer: 'Bearer Token',
    basic: 'Basic Auth',
    api_key: 'API Key',
    oauth2: 'OAuth 2.0'
  }
  return map[authType?.toLowerCase() || ''] || authType || '无'
}

/**
 * 格式化认证配置
 */
export function formatAuthConfig(config: any): string {
  if (!config) return '-'
  if (typeof config === 'string') return config
  if (typeof config === 'object') return JSON.stringify(config)
  return String(config)
}

/**
 * 格式化时间
 */
export function formatDateTime(time?: string): string {
  if (!time) return '-'
  if (typeof time === 'string' && time.includes('T')) {
    return new Date(time).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    }).replace(/\//g, '-')
  }
  return time
}

/**
 * 截断文本
 */
export function truncateText(text: string, maxLength: number): string {
  if (!text) return '-'
  if (text.length <= maxLength) return text
  return text.slice(0, maxLength) + '...'
}

// HTTP 方法映射
export function getMethodType(method: string): string {
  const map: Record<string, string> = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'info'
  }
  return map[method] || 'info'
}

// 执行类型映射
export function getExecutionTypeText(type: string): string {
  const map: Record<string, string> = {
    API: '接口',
    TEST_CASE: '用例',
    TEST_SUITE: '套件',
    PROJECT: '项目'
  }
  return map[type] || type
}

// 优先级映射
export function getPriorityType(priority: string): string {
  const map: Record<string, string> = {
    P0: 'danger',
    P1: 'warning',
    P2: 'primary',
    P3: 'info'
  }
  return map[priority] || 'info'
}

// 定时任务类型映射
export function getTaskTypeText(type: string): string {
  const map: Record<string, string> = {
    TEST_SUITE: '测试套件',
    TEST_CASE: '测试用例',
    API: '接口',
    SINGLE_CASE: '单个用例',
    MODULE: '模块',
    PROJECT: '项目'
  }
  return map[type] || type
}
