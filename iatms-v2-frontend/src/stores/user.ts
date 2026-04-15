import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LoginVO } from '@/types/api'
import { authApi } from '@/api/modules/auth/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const userInfo = ref<LoginVO | null>(null)

  const isLoggedIn = computed(() => !!token.value)

  async function login(username: string, password: string) {
    const result = await authApi.login(username, password)
    token.value = result.token
    userInfo.value = result
    localStorage.setItem('token', result.token)
    return result
  }

  async function logout() {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
  }

  async function fetchUserInfo() {
    if (!token.value) return null
    try {
      const result = await authApi.getCurrentUser()
      userInfo.value = result
      return result
    } catch {
      logout()
      return null
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    logout,
    fetchUserInfo
  }
})
