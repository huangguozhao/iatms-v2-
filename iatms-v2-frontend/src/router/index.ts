import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册', public: true }
  },
  {
    path: '/',
    component: () => import('@/components/layout/AppLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'projects',
        name: 'Projects',
        component: () => import('@/views/project/ProjectList.vue'),
        meta: { title: '项目管理' }
      },
      {
        path: 'projects/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/project/ProjectDetail.vue'),
        meta: { title: '项目详情' }
      },
      {
        path: 'apis',
        name: 'Apis',
        component: () => import('@/views/api/ApiList.vue'),
        meta: { title: '接口管理' }
      },
      {
        path: 'test-cases',
        name: 'TestCases',
        component: () => import('@/views/test-case/TestCaseList.vue'),
        meta: { title: '测试用例' }
      },
      {
        path: 'test-suites',
        name: 'TestSuites',
        component: () => import('@/views/test-suite/TestSuiteList.vue'),
        meta: { title: '测试套件' }
      },
      {
        path: 'executions',
        name: 'Executions',
        component: () => import('@/views/execution/ExecutionList.vue'),
        meta: { title: '执行记录' }
      },
      {
        path: 'scheduled-tasks',
        name: 'ScheduledTasks',
        component: () => import('@/views/scheduled-task/ScheduledTaskList.vue'),
        meta: { title: '定时任务' }
      },
      {
        path: 'scheduled-tasks/create',
        name: 'ScheduledTaskCreate',
        component: () => import('@/views/scheduled-task/ScheduledTaskCreate.vue'),
        meta: { title: '创建定时任务' }
      },
      {
        path: 'scheduled-tasks/:id',
        name: 'ScheduledTaskDetail',
        component: () => import('@/views/scheduled-task/ScheduledTaskDetail.vue'),
        meta: { title: '任务详情' }
      },
      {
        path: 'scheduled-tasks/:id/edit',
        name: 'ScheduledTaskEdit',
        component: () => import('@/views/scheduled-task/ScheduledTaskCreate.vue'),
        meta: { title: '编辑定时任务' }
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('@/views/report/ReportList.vue'),
        meta: { title: '测试报告' }
      },
      {
        path: 'notifications',
        name: 'Notifications',
        component: () => import('@/views/Notifications.vue'),
        meta: { title: '消息中心' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/Settings.vue'),
        meta: { title: '系统设置' }
      },
      {
        path: 'ai-diagnosis',
        name: 'AIDiagnosis',
        component: () => import('@/views/ai/Diagnosis.vue'),
        meta: { title: 'AI 诊断' }
      },
      {
        path: 'personnel',
        name: 'Personnel',
        component: () => import('@/views/personnel/PersonnelManagement.vue'),
        meta: { title: '人员管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const isPublic = to.matched.some(record => record.meta.public)

  if (!isPublic && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
