/**
 * 公共格式化函数
 */

// 状态类型映射
export function getStatusType(status: string): string {
  const map: Record<string, string> = {
    // 项目状态
    NOT_STARTED: 'info',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success',
    ARCHIVED: 'warning',
    // 执行状态
    PENDING: 'info',
    RUNNING: 'primary',
    SUCCESS: 'success',
    FAILED: 'danger',
    CANCELLED: 'warning',
    // 通用
    ENABLED: 'success',
    DISABLED: 'info'
  }
  return map[status] || 'info'
}

export function getStatusText(status: string): string {
  const map: Record<string, string> = {
    NOT_STARTED: '未开始',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    ARCHIVED: '已归档',
    PENDING: '待执行',
    RUNNING: '执行中',
    SUCCESS: '成功',
    FAILED: '失败',
    CANCELLED: '已取消',
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
