import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi, menuApi } from '@/api'
import type { CurrentUser, MenuView } from '@/types'

/** Session 状态仅缓存在内存，刷新时向服务端重新确认。 */
export const useAuthStore = defineStore('auth', () => {
  const user = ref<CurrentUser | null>(null)
  const grants = ref<string[]>([])
  const menus = ref<MenuView[]>([])
  const ready = ref(false)

  async function initialize(): Promise<void> {
    if (ready.value) return
    try {
      await authApi.csrf()
      user.value = await authApi.me()
      await refreshAccess()
    } catch {
      user.value = null
      grants.value = []
      menus.value = []
    } finally { ready.value = true }
  }

  async function refreshAccess(): Promise<void> {
    const [granted, navigations] = await Promise.all([authApi.grants(), menuApi.mine()])
    grants.value = granted
    menus.value = navigations
  }

  async function login(username: string, password: string): Promise<void> {
    await authApi.csrf()
    user.value = await authApi.login(username, password)
    await refreshAccess()
    ready.value = true
  }

  async function logout(): Promise<void> {
    try { await authApi.logout() } finally {
      user.value = null; grants.value = []; menus.value = []; ready.value = false
    }
  }

  function hasPermission(code: string): boolean {
    return !!user.value && (user.value.superAdmin || grants.value.includes(code))
  }
  return { user, grants, menus, ready, initialize, login, logout, refreshAccess, hasPermission }
})
