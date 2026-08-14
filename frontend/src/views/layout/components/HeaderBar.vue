<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Sunny, Moon, UserFilled, ArrowDown } from '@element-plus/icons-vue'
import { useThemeStore } from '../../../stores/theme'
import { useUserStore } from '../../../stores/user'

/**
 * 顶栏：项目 logo、主题切换与用户下拉菜单（退出登录）。
 *
 * @author demo
 * @since 2026-08-14
 */
const router = useRouter()
const themeStore = useThemeStore()
const userStore = useUserStore()

const nickname = computed(() => userStore.user?.nickname ?? '未登录')

/** 退出登录并跳转登录页。 */
async function handleLogout(): Promise<void> {
  await userStore.logout()
  await router.push('/login')
}
</script>

<template>
  <div class="header-bar">
    <div class="header-logo">
      <span class="header-title">综合计划储备项目管理</span>
    </div>
    <div class="header-actions">
      <el-button
        class="theme-toggle"
        text
        circle
        :aria-label="themeStore.theme === 'dark' ? '切换到浅色主题' : '切换到深色主题'"
        @click="themeStore.toggle()"
      >
        <el-icon>
          <Sunny v-if="themeStore.theme === 'dark'" />
          <Moon v-else />
        </el-icon>
      </el-button>
      <el-dropdown>
        <span class="header-user">
          <el-icon><UserFilled /></el-icon>
          <span class="header-nickname">{{ nickname }}</span>
          <el-icon><ArrowDown /></el-icon>
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
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.theme-toggle {
  color: var(--text-secondary);
  font-size: 1.1rem;
}

.header-user {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  color: var(--text-secondary);
  cursor: pointer;
  outline: none;
}

.header-user:hover {
  color: var(--text-primary);
}

.header-nickname {
  color: var(--text-primary);
}
</style>
