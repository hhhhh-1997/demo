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
      component: () => import('../views/layout/index.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'Home',
          component: () => import('../views/home/index.vue'),
          meta: { title: '首页', requiresAuth: true },
        },
        {
          path: 'project',
          name: 'Project',
          component: () => import('../views/placeholder/index.vue'),
          meta: { title: '储备项目维护', permissions: ['project:add'] },
        },
        {
          path: 'review',
          name: 'Review',
          component: () => import('../views/placeholder/index.vue'),
          meta: { title: '储备项目论证', permissions: ['review:view'] },
        },
        {
          path: 'audit',
          name: 'Audit',
          component: () => import('../views/placeholder/index.vue'),
          meta: { title: '储备项目审核', permissions: ['audit:view'] },
        },
        {
          path: 'reserve',
          name: 'Reserve',
          component: () => import('../views/placeholder/index.vue'),
          meta: { title: '统一储备库', permissions: ['reserve:view'] },
        },
        {
          path: 'system/user',
          name: 'SystemUser',
          component: () => import('../views/placeholder/index.vue'),
          meta: { title: '用户管理', permissions: ['system:user'] },
        },
        {
          path: 'system/role',
          name: 'SystemRole',
          component: () => import('../views/placeholder/index.vue'),
          meta: { title: '角色管理', permissions: ['system:role'] },
        },
        {
          path: 'system/menu',
          name: 'SystemMenu',
          component: () => import('../views/placeholder/index.vue'),
          meta: { title: '菜单管理', permissions: ['system:menu'] },
        },
      ],
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
