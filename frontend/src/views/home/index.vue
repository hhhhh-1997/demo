<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'

/**
 * 首页：欢迎语 + 各业务模块快捷入口。
 *
 * @author demo
 * @since 2026-08-15
 */

const router = useRouter()
const userStore = useUserStore()

/** 业务模块入口（icon 为全局注册的 Element Plus 图标名）。 */
interface ModuleItem {
  title: string
  description: string
  path: string
  permission: string
  icon: string
  color: string
}

const modules: ModuleItem[] = [
  {
    title: '储备项目维护',
    description: '项目提报、编辑、删除与状态流转',
    path: '/project',
    permission: 'project:add',
    icon: 'Document',
    color: '#409eff',
  },
  {
    title: '储备项目论证',
    description: '对储备项目逐项开展论证检查',
    path: '/review',
    permission: 'review:view',
    icon: 'Search',
    color: '#67c23a',
  },
  {
    title: '储备项目审核',
    description: '审核论证结果并给出审核结论',
    path: '/audit',
    permission: 'audit:view',
    icon: 'Stamp',
    color: '#e6a23c',
  },
  {
    title: '统一储备库',
    description: '储备库项目查看、下达与统计分析',
    path: '/reserve',
    permission: 'reserve:view',
    icon: 'PieChart',
    color: '#f56c6c',
  },
  {
    title: '自动化测试',
    description: '编排接口链路，一键执行并回溯结果',
    path: '/autotest/scenario',
    permission: 'autotest:scenario:list',
    icon: 'Connection',
    color: '#909399',
  },
]

function go(path: string): void {
  router.push(path)
}
</script>

<template>
  <div class="home">
    <div class="welcome-card">
      <h1 class="welcome-title">欢迎回来，{{ userStore.user?.nickname || '管理员' }}</h1>
      <p class="welcome-subtitle">综合计划储备项目管理平台 · 覆盖项目提报、论证、审核到储备库下达的全流程</p>
    </div>

    <div class="card">
      <div class="card-header">
        <h3 class="card-title">快捷入口</h3>
      </div>
      <div class="module-grid">
        <button
          v-for="m in modules"
          :key="m.path"
          v-permission="m.permission"
          class="module-card"
          @click="go(m.path)"
        >
          <span class="module-icon" :style="{ color: m.color }">
            <el-icon :size="26"><component :is="m.icon" /></el-icon>
          </span>
          <span class="module-info">
            <span class="module-title">{{ m.title }}</span>
            <span class="module-desc">{{ m.description }}</span>
          </span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.welcome-card {
  padding: 2rem 1.5rem;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.14), rgba(64, 158, 255, 0.03));
  border: 1px solid var(--border-color);
  border-radius: 12px;
}

.welcome-title {
  margin: 0;
  font-size: 1.5rem;
  color: var(--text-primary);
}

.welcome-subtitle {
  margin: 0.5rem 0 0;
  color: var(--text-secondary);
}

.card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  overflow: hidden;
}

.card-header {
  padding: 1rem;
  border-bottom: 1px solid var(--border-color);
}

.card-title {
  margin: 0;
  font-size: 1rem;
  color: var(--text-primary);
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 1rem;
  padding: 1rem;
}

.module-card {
  display: flex;
  align-items: center;
  gap: 0.9rem;
  padding: 1.1rem;
  text-align: left;
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.module-card:hover {
  border-color: var(--primary-color);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.module-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  border-radius: 10px;
  background-color: var(--bg-hover);
}

.module-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.module-title {
  font-weight: 600;
  color: var(--text-primary);
}

.module-desc {
  font-size: 0.85rem;
  color: var(--text-secondary);
}
</style>
