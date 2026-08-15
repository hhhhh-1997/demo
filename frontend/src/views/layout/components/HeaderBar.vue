<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, Moon, OfficeBuilding, Sunny, UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '../../../stores/user'
import { useThemeStore } from '../../../stores/theme'

/**
 * 顶栏：图标 + 渐变标题、主题切换、用户下拉（退出登录）。
 *
 * @author demo
 * @since 2026-08-14
 */
const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const nickname = computed(() => userStore.user?.nickname ?? '未登录')

/** 主题切换按钮图标：暗色显示太阳（切亮），亮色显示月亮（切暗）。 */
const themeIcon = computed(() => (themeStore.theme === 'dark' ? Sunny : Moon))

/** 退出登录并跳转登录页。 */
async function handleLogout(): Promise<void> {
  await userStore.logout()
  await router.push('/login')
}
</script>

<template>
  <div class="header-bar">
    <div class="header-logo">
      <el-icon class="header-logo-icon"><OfficeBuilding /></el-icon>
      <span class="header-title">综合计划储备项目管理</span>
    </div>
    <div class="header-actions">
      <button class="theme-toggle" title="切换主题" @click="themeStore.toggle()">
        <el-icon><component :is="themeIcon" /></el-icon>
      </button>
      <el-dropdown>
        <span class="header-user">
          <el-icon class="header-user-icon"><UserFilled /></el-icon>
          <span class="header-nickname">{{ nickname }}</span>
          <el-icon class="header-user-arrow"><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<style scoped>
.header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.header-logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-logo-icon {
  font-size: 28px;
  color: var(--primary-color);
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  background: linear-gradient(135deg, var(--primary-color) 0%, #64b5f6 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.theme-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  padding: 0;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--warning-color);
  font-size: 20px;
  cursor: pointer;
  transition: background 0.2s;
}

.theme-toggle:hover {
  background: var(--bg-hover);
}

.header-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  border-radius: 8px;
  color: var(--text-secondary);
  cursor: pointer;
  outline: none;
}

.header-user:hover {
  background: var(--bg-hover);
}

.header-user-icon {
  font-size: 32px;
  color: var(--primary-color);
}

.header-user-arrow {
  font-size: 12px;
  color: var(--text-muted);
}

.header-nickname {
  color: var(--text-primary);
}
</style>
