# 绩效月报查询模块 · 前端设计（DESIGN）

> 关联：规格 `docs/superpowers/specs/2026-08-14-绩效月报查询-design.md` ｜ 原型 `docs/prototype/performance-report-query.html`（视觉与交互的唯一依据）
> 核心原则：**展示规则全部落在前端**（空值语义、等级映射、精度格式化）；页面视觉**完全依赖原型图**。

---

## 1. 技术选型

| 层 | 技术 | 版本 | 说明 |
|---|---|---|---|
| 框架 | Vue 3 | ^3.5.13 | `<script setup lang="ts">` 组合式 API |
| 语言 | TypeScript | ~5.7.2 | `strict` + `noUnusedLocals` + `noUnusedParameters` |
| 构建 | Vite | ^6.0.5 | dev 端口 5173，`/api` 代理到 `localhost:8080` |
| UI | **Element Plus** | 最新 | **替换**原 `naive-ui`（规格 §2.1 决策） |
| HTTP | axios | 最新 | 接口封装 |
| 测试 | Vitest | 最新 | 纯函数单测 |

**依赖变更**（相对现有骨架）：`package.json` 移除 `naive-ui`，新增 `element-plus`、`axios`（dependencies）与 `vitest`（devDependencies）。

### 1.1 组件映射（原型 → Element Plus）

| 原型元素 | Element Plus 组件 |
|---|---|
| 渐变顶栏 | 手写 CSS（非组件库职责） |
| 面包屑 | `el-breadcrumb` |
| 检索下拉 | `el-select` |
| 查询/重置按钮 | `el-button`（`primary` / `default`） |
| 等级徽标 | `el-tag`（`round size="small"`，按码映射 `type`） |
| 加载态 | `v-loading` 指令 |
| 空态 | `el-empty`（走 `el-table` 的 `#empty` 插槽） |
| 错误态 + 重试 | `el-result`（`status="error"` + `#extra` 重试按钮） |
| 卡片 | `el-card` 或手写 `.card`（保持原型圆角/留白） |
| 表格（固定表头 + 冻结左列 + 不分页） | `el-table`：`height` + 列 `fixed="left"` + `border`，不配 `el-pagination` |

### 1.2 主题贴合

- 覆写 CSS 变量 `--el-color-primary: #2C7CF5`（博瑞蓝）。
- 卡片圆角、等级徽标色板覆写少量变量，贴近原型。
- 原型非组件职责的视觉（渐变顶栏、冻结列阴影、徽标圆角）以自定义 CSS 落地。

---

## 2. 项目结构

```
frontend/src/
  main.ts                      # createApp + app.use(ElementPlus) + 引入主题 CSS
  App.vue                      # 布局外壳：渐变顶栏 + 面包屑 + 标题 + 挂载主查询组件
  styles/theme.css             # 全局主题：--el-color-primary 覆写、页面底色、顶栏渐变、徽标色板
  api/performance.ts           # 接口封装 fetchMonthly()
  types/performance.ts         # Performance / PerformanceQuery / ApiResult 类型
  constants/dict.ts            # 一级部门 / 岗位 / 数据月份字典 + currentMonth() / monthOptions()
  utils/format.ts              # fmtScore / gradeBadge 纯函数（展示规则唯一入口）
  utils/format.test.ts         # Vitest：空值语义 + 等级映射 + 越界兜底
  components/PerformanceQuery.vue  # 主查询组件（检索区 + 表格区 + 三态）
  assets/logo-white.png        # 顶栏 logo（复制自 docs/prototype/assets/logo-white.png）
```

---

## 3. 路由设计

**本期不引入 vue-router。** 模块为单页面，`App.vue` 作为根布局直接渲染 `PerformanceQuery.vue`，无需路由层（YAGNI）。

- 原型面包屑「首页 / 绩效管理 / 绩效月报查询」为**静态展示**（原型中 `href="#"`），对应 `el-breadcrumb` 静态项，不驱动真实导航。
- **演进预留**：若后续新增页面（如明细页），再引入 `vue-router`，规划路由表：

| 路径 | 组件 | 面包屑 |
|---|---|---|
| `/` | redirect → `/performance/monthly` | — |
| `/performance/monthly` | `PerformanceQuery` | 首页 / 绩效管理 / 绩效月报查询 |

此时 `App.vue` 顶栏作为持久外壳，正文改为 `<router-view>`。当前实现不预留该层，保持最小。

---

## 4. 状态管理

**不引入 Pinia/Vuex**（单页、无跨组件共享状态，YAGNI）。状态由 `PerformanceQuery.vue` 本地 `ref`/`reactive` 管理：

```ts
const filters = reactive<{ topDeptId: number | null; role: string; month: string }>({
  topDeptId: null,      // null = 全部（空值表达）
  role: '',             // '' = 全部
  month: currentMonth() // 默认当前月（动态，当前为 2026-08）
})
const rows = ref<Performance[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
```

| 状态 | 类型 | 触发 |
|---|---|---|
| `filters` | `reactive` | 检索条件，驱动 query 参数 |
| `rows` | `ref<Performance[]>` | 表格数据 |
| `loading` | `ref<boolean>` | 请求中 → `v-loading` |
| `error` | `ref<string \| null>` | 非空 → `el-result` 错误态 |

状态流转：`query()` 置 `loading=true`、清 `error` → 成功写 `rows`；失败写 `error`；`finally` 置 `loading=false`。**不吞异常**：`catch` 中把异常信息写入 `error`，供错误态展示。

---

## 5. 展示规则实现方式

展示规则**唯一入口**为 `utils/format.ts` 纯函数，`PerformanceQuery.vue` 的表格列只调用它们，不内联任何格式化逻辑。

### 5.1 分值项格式化 `fmtScore`

```ts
// NULL → '-'；数值（含 0）→ 保留一位小数（0 → '0.0'，120 → '120.0'，不截断）
export function fmtScore(v: number | null | undefined): string {
  if (v === null || v === undefined) return '-'
  return v.toFixed(1)
}
```

- `NULL` = 「无该项工作」→ `-`；`0` = 「有该项工作但得 0 分」→ `0.0`。二者语义相反，**不可归并**。
- `work_effect_rate` 超 100（上限 120）按原值 `toFixed(1)`，不截断。
- 分值项统一一位小数（含整型存储的 `work_normativity`，`80` → `80.0`）。

### 5.2 等级项映射 `gradeBadge`

```ts
const GRADE_TEXT: Record<number, string> = {
  1: '超出预期', 2: '完全达标', 3: '需要提升', 4: '未达预期', 5: '无该项工作',
}
// 原型 badge 色板 → el-tag type
const GRADE_TAG: Record<number, TagType> = {
  1: 'success', 2: 'primary', 3: 'warning', 4: 'danger', 5: 'info',
}

export type GradeBadge = { text: string; type: TagType | null }

export function gradeBadge(code: number | null | undefined): GradeBadge {
  if (code === null || code === undefined) return { text: '-', type: null }  // NULL 兜底
  const text = GRADE_TEXT[code]
  if (!text) return { text: '-', type: null }                                 // 越界兜底
  return { text, type: GRADE_TAG[code] }
}
```

- 等级码 `NULL` 或**越界**（<1 或 >5）→ `-`，不报错、不回落为任一等级。
- 等级项**不参与**小数格式化；数值大小与优劣方向相反（1 最优）。
- 界面上以 `el-tag` 徽标与分值项视觉区分；**不做**求和/平均/数值排序（表格列仅为展示，无聚合）。

### 5.3 表格列渲染

`PerformanceQuery.vue` 用「有序列配置 + 判别 `kind`」渲染 15 列，保证原型列序（售前支撑为等级项，穿插在分值列之间）：

```ts
const COLUMNS = [
  { prop: 'realname', label: '姓名', kind: 'text', fixed: true, width: 120 },
  { prop: 'month', label: '月份', kind: 'text', width: 100 },
  { prop: 'topDeptName', label: '一级部门', kind: 'text', width: 120 },
  { prop: 'deptName', label: '二级部门', kind: 'text', width: 140 },
  { prop: 'roleName', label: '岗位', kind: 'text', width: 110 },
  { prop: 'taskFinishRate', label: '任务完成率', kind: 'score', width: 120 },
  { prop: 'workEffectRate', label: '工作有效率', kind: 'score', width: 120 },
  { prop: 'workNormativity', label: '工作规范性', kind: 'score', width: 120 },
  { prop: 'learningImprovement', label: '学习及能力提升', kind: 'score', width: 120 },
  { prop: 'softwareDesign', label: '软需设计', kind: 'score', width: 120 },
  { prop: 'preSalesSupport', label: '售前支撑', kind: 'grade', width: 120 },
  { prop: 'bugCondition', label: '缺陷情况', kind: 'score', width: 120 },
  { prop: 'systemDesign', label: '概要设计', kind: 'score', width: 120 },
  { prop: 'codeReview', label: '代码评审', kind: 'score', width: 120 },
  { prop: 'testQuality', label: '测试产出质量', kind: 'grade', width: 120 },
]
```

- `kind: 'text'` 原样渲染；`kind: 'score'` 走 `fmtScore`；`kind: 'grade'` 走 `gradeBadge`。
- `realname` 列 `fixed="left"` 冻结；`el-table` 设 `height` 固定表头；不配 `el-pagination`（默认不分页）。
- 隐藏字段 `id` / `user_id` / `dept` / `role` / `top_dept_id` 接口仍返回，仅不进入 `COLUMNS`。

### 5.4 检索条件「全部」与默认月份

- 三个检索条件以空值表达「全部」：`topDeptId: null`、`role: ''`、`month: ''`；空值不拼入请求参数（`undefined` 被 axios 省略）。
- 月份选项 = 「全部」 + 数据月份（`DATA_MONTHS` 前端维护 `['2026-05','2026-06','2026-07']`）+ 当前月（`currentMonth()` 动态计算，当前 `2026-08`），去重后降序。
- **默认选中当前月**（`2026-08`）；「重置」将一级部门、岗位清空为「全部」，月份回到当前月，并触发查询。
- 选中无数据岗位（`hr`/`top` 等）走空态，选项本身不消失（字典全量展示，不动态裁剪）。

---

## 6. 测试策略

`utils/format.test.ts`（Vitest）覆盖验收重点：

| 函数 | 用例 |
|---|---|
| `fmtScore` | `null` → `'-'`；`0` → `'0.0'`；`120` → `'120.0'`；`101.3` → `'101.3'`；`80` → `'80.0'` |
| `gradeBadge` | 1~5 映射文本 + `type`；`null` / `0` / `6`（越界）→ `{ text:'-', type:null }` |

> 等级码越界/`NULL` 兜底分支**必须有测试覆盖**（本期数据仅出现 2/3/5，仍不可省略，规格 §10 验收重点）。
