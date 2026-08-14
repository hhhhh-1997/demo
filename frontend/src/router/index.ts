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
      redirect: '/login',
    },
  ],
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  const hasToken = !!userStore.token

  if (to.meta.requiresAuth && !hasToken) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/login' && hasToken) {
    return { path: '/' }
  }
  return true
})

export default router
