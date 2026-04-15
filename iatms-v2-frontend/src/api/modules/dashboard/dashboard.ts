import client from '../../client'

export interface DashboardStatisticsVO {
  projectCount: number
  apiCount: number
  testCaseCount: number
  executionCount: number
  todayExecutionCount: number
  weekExecutionCount: number
  successRate: number
}

export interface RecentActivityVO {
  executionId: string
  scope: string
  refName: string
  status: string
  executionType: string
  executedBy: number | null
  executedByName: string | null
  startTime: string
  successRate: number | null
  totalCases: number | null
  failedCases: number | null
}

export const dashboardApi = {
  getStatistics(): Promise<DashboardStatisticsVO> {
    return client.get('/v1/dashboard/statistics')
  },

  getRecentActivities(limit = 10): Promise<RecentActivityVO[]> {
    return client.get('/v1/dashboard/activities', { params: { limit } })
  }
}
