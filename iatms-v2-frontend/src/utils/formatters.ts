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
