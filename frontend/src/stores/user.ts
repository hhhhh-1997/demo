import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi } from '../api/auth'
import type { LoginUser } from '../types'

/** 系统管理员角色标识。 */
const ADMIN_ROLE = 'admin'

/**
 * 认证用户 store：持有 token、当前用户、角色与权限。
 *
 * @author demo
 * @since 2026-08-14
 */
export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') ?? '')
  const user = ref<LoginUser | null>(null)
  const roles = ref<string[]>([])
  const permissions = ref<string[]>([])

  /** 登录：拉取 token 与用户信息并持久化 token。 */
  async function login(username: string, password: string): Promise<void> {
    const res = await loginApi({ username, password })
    token.value = res.data.token
    user.value = res.data
    roles.value = res.data.roles ?? []
    permissions.value = res.data.permissions ?? []
    localStorage.setItem('token', token.value)
  }

  /** 退出登录：清空本地状态与持久化 token。 */
  async function logout(): Promise<void> {
    try {
      await logoutApi()
    } finally {
      token.value = ''
      user.value = null
      roles.value = []
      permissions.value = []
      localStorage.removeItem('token')
    }
  }

  /** 是否拥有指定权限（管理员直接放行）。 */
  function hasPermi(perm: string): boolean {
    if (roles.value.includes(ADMIN_ROLE)) {
      return true
    }
    return permissions.value.includes(perm)
  }

  return { token, user, roles, permissions, login, logout, hasPermi }
})
