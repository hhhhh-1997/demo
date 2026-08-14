<script setup lang="ts">
import { ref } from 'vue'
import { useTheme } from './composables/useTheme'
import Overview from './components/Overview.vue'
import AgeAnalysis from './components/AgeAnalysis.vue'

const { theme, toggle } = useTheme()

const pages = [
  { key: 'overview', label: '总览' },
  { key: 'age', label: '年龄分析' },
  { key: 'unit', label: '单位分析' },
  { key: 'score', label: '积分分布' },
  { key: 'ai', label: 'AI 智能问数' },
  { key: 'model', label: '大模型管理' },
] as const

const current = ref<(typeof pages)[number]['key']>('overview')
</script>

<template>
  <nav class="nav-topbar">
    <span class="nav-product">积分落户分析</span>
    <div class="nav-items">
      <button
        v-for="p in pages"
        :key="p.key"
        class="nav-item"
        :class="{ active: current === p.key }"
        @click="current = p.key"
      >{{ p.label }}</button>
    </div>
    <div class="nav-right">
      <button class="color-scheme-toggle" type="button" @click="toggle" :title="theme === 'dark' ? '切换浅色模式' : '切换深色模式'">
        {{ theme === 'dark' ? '☀' : '☾' }}
      </button>
    </div>
  </nav>

  <main class="content">
    <!-- Task 12–17 逐板块替换为真实组件 -->
    <Overview v-if="current === 'overview'" />
    <AgeAnalysis v-else-if="current === 'age'" />
    <div v-else-if="current === 'unit'">单位分析（待实现）</div>
    <div v-else-if="current === 'score'">积分分布（待实现）</div>
    <div v-else-if="current === 'ai'">AI 智能问数（待实现）</div>
    <div v-else-if="current === 'model'">大模型管理（待实现）</div>
  </main>
</template>
