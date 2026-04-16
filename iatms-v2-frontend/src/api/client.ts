import axios, { AxiosInstance, AxiosResponse } from 'axios'
import type { ApiResponse } from '@/types/api'
import { ElMessage } from 'element-plus'

export const client: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
client.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
client.interceptors.response.use(
  (response: AxiosResponse<ApiResponse<any>>) => {
    const { code, message, data } = response.data

    if (code === 200) {
      return data
    }

    // 认证失败
    if (code === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
      return Promise.reject(new Error(message))
    }

    ElMessage.error(message)
    return Promise.reject(new Error(message))
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        localStorage.removeItem('token')
        window.location.href = '/login'
        return Promise.reject(new Error('登录已过期，请重新登录'))
      }
      // 设置错误信息，让调用方统一处理提示
      error.message = data?.message || error.message || '请求失败'
    } else {
      error.message = '网络错误，请检查后端服务是否启动'
    }
    return Promise.reject(error)
  }
)

export default client
