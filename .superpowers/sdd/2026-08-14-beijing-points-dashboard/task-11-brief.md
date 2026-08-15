### Task 11: App 外壳 + 顶部导航 + 页面切换

**Files:**
- Modify: `frontend/src/App.vue`

**Interfaces:**
- Consumes: `useTheme`（Task 9）、六个页面组件（Task 12–17，本任务先用占位 `<div>`，后续替换为真实组件）。
- Produces: 顶部导航栏（6 个 `nav-item`）、明暗切换按钮、`currentPage` 驱动的页面切换（`v-if`）。

- [ ] **Step 1: 实现 App.vue**

覆盖 `frontend/src/App.vue`：

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { useTheme } from './composables/useTheme'

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
    <div v-if="current === 'overview'">总览（待实现）</div>
    <div v-else-if="current === 'age'">年龄分析（待实现）</div>
    <div v-else-if="current === 'unit'">单位分析（待实现）</div>
    <div v-else-if="current === 'score'">积分分布（待实现）</div>
    <div v-else-if="current === 'ai'">AI 智能问数（待实现）</div>
    <div v-else-if="current === 'model'">大模型管理（待实现）</div>
  </main>
</template>
```

- [ ] **Step 2: 验证**

Run: `cd frontend && npm run dev`，浏览器确认：顶部渐变导航栏渲染、6 个导航项可切换（内容为占位文案）、明暗切换按钮可切换主题。

- [ ] **Step 3: Commit**

```bash
git add frontend/src/App.vue
git commit -m "feat: App 外壳（顶部导航 + 页面切换 + 明暗主题）"
```

---

