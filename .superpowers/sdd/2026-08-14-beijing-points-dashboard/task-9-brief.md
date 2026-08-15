### Task 9: JoinBright 设计系统 CSS + 明暗主题

**Files:**
- Modify: `frontend/src/styles/theme.css`（替换占位，完整移植原型样式）
- Create: `frontend/src/composables/useTheme.ts`

**Interfaces:**
- Produces: `useTheme()` 返回 `{ theme: Ref<'light' | 'dark'>, toggle(): void }`；`theme.css` 提供原型全部类（`.nav-topbar`、`.card`、`.btn`、`.kpi`、`.stat`、`.filter-bar`、`table`、`.pagination`、`.chat*`、`.modal*`、`.toast`、`.tool-list`、`.form-grid`、`.state-overlay` 等）。

- [ ] **Step 1: 移植 CSS**

将 `design/beijing-points-dashboard.html` 中 `<style>…</style>`（第 7–343 行）内容**原样**复制到 `frontend/src/styles/theme.css`，做以下适配：

1. 删除原型 `<body>` 的 `display:flex; flex-direction:column; overflow:hidden` 之外的演示专用样式不做改动（这些样式本就是给该布局用的，保留）。
2. 保留 `:root`、`[data-theme="blue"]`、`[data-color-scheme="dark"]`、`@media (prefers-reduced-motion)` 与全部组件类。
3. 无需改动选择器——Vue 组件模板会直接复用这些类名（Task 11–17 使用）。

- [ ] **Step 2: 实现 useTheme.ts**

创建 `frontend/src/composables/useTheme.ts`：

```ts
import { ref } from 'vue'
import type { Ref } from 'vue'

const STORAGE_KEY = 'points-settlement:theme'
type Theme = 'light' | 'dark'

function read(): Theme {
  return (localStorage.getItem(STORAGE_KEY) as Theme) || 'light'
}

function apply(theme: Theme): void {
  if (theme === 'dark') document.documentElement.setAttribute('data-color-scheme', 'dark')
  else document.documentElement.removeAttribute('data-color-scheme')
  localStorage.setItem(STORAGE_KEY, theme)
}

const theme = ref<Theme>(read())
apply(theme.value)

export function useTheme(): { theme: Ref<Theme>; toggle: () => void } {
  return {
    theme,
    toggle() {
      theme.value = theme.value === 'dark' ? 'light' : 'dark'
      apply(theme.value)
    },
  }
}
```

- [ ] **Step 3: 验证**

Run: `cd frontend && npm run dev`，在浏览器 console 执行 `document.documentElement.setAttribute('data-color-scheme','dark')` 应看到深色生效（此时 App 仍是占位壳，但主题变量已就绪）。

- [ ] **Step 4: Commit**

```bash
git add frontend/src/styles/theme.css frontend/src/composables/useTheme.ts
git commit -m "feat: JoinBright 设计系统 CSS 与明暗主题"
```

---

