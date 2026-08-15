### Task 1: 工程初始化（去除 Naive UI，接入 ECharts / xlsx / Vitest）

**Files:**
- Modify: `frontend/package.json`
- Modify: `frontend/vite.config.ts`
- Modify: `frontend/src/main.ts`
- Modify: `frontend/index.html`
- Modify: `frontend/src/App.vue`（临时最小壳）

**Interfaces:**
- Produces: 可运行的空白 Vue 应用；测试命令 `npm test` 可用；`main.ts` 全局引入 `styles/theme.css`（Task 9 创建，本任务先引用并留空文件占位由 Task 9 填充）。

- [ ] **Step 1: 更新依赖**

编辑 `frontend/package.json`：删除 `naive-ui`，新增 `echarts`、`xlsx` 依赖与 `vitest`、`jsdom` 开发依赖，并加 `test` 脚本。

```json
{
  "name": "demo-frontend",
  "private": true,
  "version": "0.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview",
    "typecheck": "vue-tsc --noEmit",
    "test": "vitest run"
  },
  "dependencies": {
    "echarts": "^5.6.0",
    "vue": "^3.5.13",
    "xlsx": "^0.18.5"
  },
  "devDependencies": {
    "@types/node": "^22.10.2",
    "@vitejs/plugin-vue": "^5.2.1",
    "jsdom": "^26.0.0",
    "typescript": "~5.7.2",
    "vite": "^6.0.5",
    "vitest": "^3.0.5",
    "vue-tsc": "^2.1.10"
  }
}
```

- [ ] **Step 2: 更新 vite 配置（含 Vitest）**

覆盖 `frontend/vite.config.ts`（去掉 `/api` 代理，本应用不依赖后端）：

```ts
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: { port: 5173 },
  test: {
    environment: 'jsdom',
  },
})
```

- [ ] **Step 3: 更新 main.ts（去 Naive UI，引主题）**

覆盖 `frontend/src/main.ts`：

```ts
import { createApp } from 'vue'
import App from './App.vue'
import './styles/theme.css'

createApp(App).mount('#app')
```

- [ ] **Step 4: 创建主题占位文件**

创建 `frontend/src/styles/theme.css`，内容暂为空注释（Task 9 会完整填充）：

```css
/* JoinBright 设计系统 — Task 9 填充 */
```

- [ ] **Step 5: 更新 index.html 标题**

编辑 `frontend/index.html` 的 `<title>`：

```html
<title>北京市积分落户公示名单 · 数据分析</title>
```

- [ ] **Step 6: App.vue 临时最小壳**

覆盖 `frontend/src/App.vue`（后续 Task 11 重写为完整导航）：

```vue
<template>
  <div>加载中…</div>
</template>
```

- [ ] **Step 7: 安装依赖并验证**

Run: `cd frontend && npm install`
Run: `npm run dev` — 浏览器打开 http://localhost:5173 应显示「加载中…」，无报错。
Run: `npm run typecheck` — 应通过。

- [ ] **Step 8: Commit**

```bash
git add frontend/package.json frontend/package-lock.json frontend/vite.config.ts frontend/src/main.ts frontend/src/App.vue frontend/index.html frontend/src/styles/theme.css
git commit -m "chore: 初始化前端工程，去 Naive UI，接入 ECharts/xlsx/Vitest"
```

---

