<script setup lang="ts">
import { onMounted, ref } from 'vue'
import logo from './assets/logo-white.png'
import PerformanceQuery from './components/PerformanceQuery.vue'

const THEME_KEY = 'theme'
const isDark = ref(false)

function setTheme(dark: boolean) {
  isDark.value = dark
  document.documentElement.classList.toggle('dark', dark)
}

function toggleTheme() {
  setTheme(!isDark.value)
  localStorage.setItem(THEME_KEY, isDark.value ? 'dark' : 'light')
}

// 默认明色；仅当用户显式切到暗色并持久化后，才在下次进入时恢复暗色
onMounted(() => {
  setTheme(localStorage.getItem(THEME_KEY) === 'dark')
})
</script>

<template>
  <nav class="nav-topbar">
    <img class="nav-brand-logo" :src="logo" alt="JoinBright" />
    <span class="nav-topbar-title">绩效管理系统</span>
    <div class="nav-actions">
      <button
        class="nav-action-btn"
        type="button"
        :aria-label="isDark ? '切换到明色主题' : '切换到暗色主题'"
        :title="isDark ? '切换到明色主题' : '切换到暗色主题'"
        @click="toggleTheme"
      >
        <svg v-if="isDark" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="5" />
          <line x1="12" y1="1" x2="12" y2="3" />
          <line x1="12" y1="21" x2="12" y2="23" />
          <line x1="4.22" y1="4.22" x2="5.64" y2="5.64" />
          <line x1="18.36" y1="18.36" x2="19.78" y2="19.78" />
          <line x1="1" y1="12" x2="3" y2="12" />
          <line x1="21" y1="12" x2="23" y2="12" />
          <line x1="4.22" y1="19.78" x2="5.64" y2="18.36" />
          <line x1="18.36" y1="5.64" x2="19.78" y2="4.22" />
        </svg>
        <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
        </svg>
      </button>
      <span class="nav-avatar" aria-label="当前用户">管</span>
    </div>
  </nav>

  <main class="page-main">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item><a href="#">首页</a></el-breadcrumb-item>
      <el-breadcrumb-item><a href="#">绩效管理</a></el-breadcrumb-item>
      <el-breadcrumb-item><span class="breadcrumb-current">绩效月报查询</span></el-breadcrumb-item>
    </el-breadcrumb>
    <h1 class="page-title">绩效月报查询</h1>
    <PerformanceQuery />
  </main>
</template>
