# Task 9 报告：JoinBright 设计系统 CSS + 明暗主题

## 状态
DONE

## 实现内容

### 1. `frontend/src/styles/theme.css`（修改）
将 `design/beijing-points-dashboard.html` 中 `<style>…</style>` 的内容（第 8–343 行）原样移植，
替换了原 Task 1 的单行占位。包含：
- `:root` 设计令牌（颜色、字体、间距、圆角、阴影、过渡、图表色）
- `[data-theme="blue"]`、`[data-color-scheme="dark"]` 主题覆盖
- `@media (prefers-reduced-motion: reduce)` 无障碍规则
- 全部组件类：`.nav-topbar`、`.card`、`.btn`、`.kpi`、`.stat`、`.filter-bar`、
  `table`、`.pagination`、`.chat*`、`.modal*`、`.toast`、`.tool-list`、
  `.form-grid`、`.state-overlay` 等

### 2. `frontend/src/composables/useTheme.ts`（新建）
按 brief 逐字实现：`read()` 从 localStorage key `points-settlement:theme` 读取；
`apply()` 在 `document.documentElement` 上设置/移除 `data-color-scheme="dark"` 并持久化；
导出 `useTheme()` 返回 `{ theme: Ref<'light' | 'dark'>, toggle() }`。

## 文件变更
- `frontend/src/styles/theme.css` — 337 行新增（替换 1 行占位）
- `frontend/src/composables/useTheme.ts` — 新增 28 行

未改动 `design/`（参考文件，未跟踪）。

## 验证结果
- CSS 逐字比对：`diff <(sed -n '8,343p' design/beijing-points-dashboard.html) frontend/src/styles/theme.css` → 无差异，完全一致。
- `npm run typecheck`（vue-tsc --noEmit）→ 通过，无错误。
- `npm run build`（vite build）→ 通过；产物 CSS 19.29 kB，确认完整样式已编译。
- `dist/` 被 `.gitignore` 忽略，构建产物未污染提交。

## 自审发现
1. `useTheme.ts` 与 brief 代码逐字一致（含 `read`/`apply`/`theme`/`toggle` 结构）。
2. 提交仅含两个目标文件：`git show --stat` 确认为 2 files changed。
3. 模块顶层 `apply(theme.value)` 在 import 时即执行（含 `localStorage`/`document` 访问），
   在浏览器环境下安全；与 brief 一致，未做 SSR 防护（本项目无 SSR）。

## 关注点（Concerns）
1. **brief 与父指令冲突**：brief Step 1 要求删除原型 `<body>` 的
   `display:flex; flex-direction:column; overflow:hidden` 三行，但父任务指令明确要求
   "Port the entire `<style>` content **verbatim** … Do NOT add/remove/reformat rules"。
   我遵循了 verbatim 指令，保留了三行。后果：在 Vue 应用中 `body` 成为 flex 容器，
   `#app` 是其唯一子项，`#app` 默认不会自动撑满高度，后续 Task 11–17 组装导航+内容布局时
   需在 `#app`/App.vue 上补充 `display:flex; flex-direction:column; height:100%` 之类的
   布局修正（或届时再移除这三行）。建议父任务确认是否需要在后续任务中处理。
2. `theme.css` 每行保留了原型 2 空格缩进（为"verbatim"保真）；若后续希望脱缩进可随时清理，
   不影响功能。

## 提交
- `4b6e08a` feat: JoinBright 设计系统 CSS 与明暗主题
- `bf0f33b` fix: 将 flex 布局从 body 迁移到 #app 挂载点

## 布局修复（追加，应协调者要求）
协调者评估：Vue 应用挂载于 `#app` 而非 `body` 直接作为 flex 容器，`body` 上的
flex 布局不会生效，会导致顶部导航与内容区错位。已在当前任务内修复：

1. 删除 `body` 规则中的 `display:flex; flex-direction:column; overflow:hidden` 三行，
   其余 body 属性保留。
2. 在 `theme.css` 末尾新增 `#app` 布局规则：
   ```css
   #app {
     height: 100%;
     display: flex;
     flex-direction: column;
     overflow: hidden;
   }
   ```

验证：`npm run typecheck` 与 `npm run build` 均通过（build 产物 CSS 19.31 kB）。
原关注点 #1 已解决。
