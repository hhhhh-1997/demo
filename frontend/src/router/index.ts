import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

declare module 'vue-router' {
  interface RouteMeta {
    /** 是否需要登录。 */
    requiresAuth?: boolean
    /** 页面标题。 */
    title?: string
    /** 访问所需权限标识。 */
    permissions?: string[]
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/login/index.vue'),
      meta: { title: '登录' },
    },
    {
      path: '/',
      name: 'Home',
      component: () => import('../views/home/index.vue'),
      meta: { title: '首页', requiresAuth: true },
    },
  ],
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  const hasToken = !!userStore.token

  if (to.meta.requiresAuth && !hasToken) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.permissions && to.meta.permissions.length > 0) {
    const allowed = to.meta.permissions.some((perm) => userStore.hasPermi(perm))
    if (!allowed) {
      return { path: '/' }
    }
  }
  if (to.path === '/login' && hasToken) {
    return { path: '/' }
  }
  return true
})

export default router
