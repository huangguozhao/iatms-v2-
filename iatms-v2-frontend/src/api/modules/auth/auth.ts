import client from '../../client'
import type { LoginVO } from '@/types/api'

export const authApi = {
  login(username: string, password: string): Promise<LoginVO> {
    return client.post('/v1/auth/login', { username, password })
  },

  register(data: { username: string; password: string; displayName: string; email?: string }): Promise<LoginVO> {
    return client.post('/v1/auth/register', data)
  },

  getCurrentUser(): Promise<LoginVO> {
    return client.get('/v1/auth/current')
  }
}
