<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { UserFilled, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '../../../stores/user'
import logoWhite from '../../../assets/logo-white.png'

/**
 * 顶栏：项目 logo 与用户下拉菜单（退出登录）。
 *
 * @author demo
 * @since 2026-08-14
 */
const router = useRouter()
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
      <img class="header-logo-img" :src="logoWhite" alt="logo" />
      <span class="header-title">综合计划储备项目管理</span>
    </div>
    <div class="header-actions">
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
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.header-logo-img {
  height: 28px;
  width: auto;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
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
