import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, getMe } from '../api/auth'
import type { LoginUser } from '../types'

/** 系统管理员角色标识。 */
const ADMIN_ROLE = 'admin'

/** localStorage 键。 */
const TOKEN_KEY = 'token'
const ROLES_KEY = 'user-roles'
const PERMISSIONS_KEY = 'user-permissions'

/** 从 localStorage 读取字符串数组（解析失败时返回空数组）。 */
function readStringArray(key: string): string[] {
  const raw = localStorage.getItem(key)
  if (!raw) {
    return []
  }
  try {
    const parsed: unknown = JSON.parse(raw)
    if (Array.isArray(parsed)) {
      return parsed.filter((item): item is string => typeof item === 'string')
    }
  } catch {
    return []
  }
  return []
}

/**
 * 认证用户 store：持有 token、当前用户、角色与权限。
 *
 * @author demo
 * @since 2026-08-14
 */
export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) ?? '')
  const user = ref<LoginUser | null>(null)
  const roles = ref<string[]>(readStringArray(ROLES_KEY))
  const permissions = ref<string[]>(readStringArray(PERMISSIONS_KEY))

  /** 登录：拉取 token 与用户信息并持久化 token、角色与权限。 */
  async function login(username: string, password: string): Promise<void> {
    const res = await loginApi({ username, password })
    token.value = res.data.token
    user.value = res.data
    roles.value = res.data.roles ?? []
    permissions.value = res.data.permissions ?? []
    localStorage.setItem(TOKEN_KEY, token.value)
    localStorage.setItem(ROLES_KEY, JSON.stringify(roles.value))
    localStorage.setItem(PERMISSIONS_KEY, JSON.stringify(permissions.value))
  }

  /** 刷新后从后端恢复当前用户信息（token 已持久化，仅回填 user/roles/permissions）。 */
  async function fetchMe(): Promise<void> {
    if (!token.value) {
      return
    }
    const res = await getMe()
    user.value = { ...res.data, token: token.value }
    roles.value = res.data.roles ?? []
    permissions.value = res.data.permissions ?? []
    localStorage.setItem(ROLES_KEY, JSON.stringify(roles.value))
    localStorage.setItem(PERMISSIONS_KEY, JSON.stringify(permissions.value))
  }

  /** 退出登录：清空本地状态与持久化数据。 */
  async function logout(): Promise<void> {
    try {
      await logoutApi()
    } finally {
      token.value = ''
      user.value = null
      roles.value = []
      permissions.value = []
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(ROLES_KEY)
      localStorage.removeItem(PERMISSIONS_KEY)
    }
  }

  /** 是否拥有指定权限（管理员直接放行）。 */
  function hasPermi(perm: string): boolean {
    if (roles.value.includes(ADMIN_ROLE)) {
      return true
    }
    return permissions.value.includes(perm)
  }

  return { token, user, roles, permissions, login, logout, fetchMe, hasPermi }
})
