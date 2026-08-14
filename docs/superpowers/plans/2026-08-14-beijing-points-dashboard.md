# 北京市积分落户分析仪表盘 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在现有 Vue3 + Vite 前端脚手架内，实现一个纯前端静态页：浏览器导入北京市积分落户 Excel → 总览/年龄/单位/积分分析图表 + AI 智能问数（OpenAI 兼容 function calling、流式）+ 大模型配置管理。

**Architecture:** 顶部导航栏 + 6 个板块的单页应用。数据由浏览器内 SheetJS 解析 Excel 导入，存 localStorage，全程无后端。视觉复刻 `design/beijing-points-dashboard.html` 的 JoinBright 设计系统（自定义 CSS + 明暗主题），图表用 ECharts 定制配色。AI 通过 `chat/completions` + `tools` 流式调用，固定 10 个查询工具。

**Tech Stack:** Vue 3 + TypeScript + Vite · ECharts（图表）· SheetJS `xlsx`（Excel 解析）· Vitest + jsdom（单测）· 原生 CSS（JoinBright 设计系统）。**弃用 Naive UI。**

**Spec:** `docs/superpowers/specs/2026-08-14-beijing-points-settlement-dashboard-design.md`

**参考原型:** `design/beijing-points-dashboard.html`（视觉与布局的权威来源，其中 `<style>` 为设计系统、`window.__DATA__` 与 `ANSWERS` 为演示脚手架，不复刻）

## Global Constraints

- 运行环境：纯前端静态产物，`npm run build` 后部署到任意静态服务器，无后端。
- 数据模型：`Record { id, name, birth, unit, score, age }`；`birth` 为 `YYYY-MM`；`age` 派生。
- 年龄口径：`age = 2026 − 出生年`（月不参与）。
- 积分分段：默认每 **2** 分（复刻原型），可调。
- 数据生命周期：项目初始无数据；首次导入 Excel = 全量初始化；再次导入 = 增量导入（按 `id` upsert：存在则更新、不存在则新增）；手动增删改即时写 `localStorage`。
- 数据与配置均存 `localStorage`，key 统一前缀 `points-settlement:`。
- AI 调用：`POST {baseUrl}/chat/completions`，OpenAI 兼容，`stream: true`（SSE），带 `tools`；baseUrl 为基地址（如 `https://…/v1`），客户端自动补 `/chat/completions`。
- AI 对话历史存 `localStorage`，只保留最近 **10 轮**（一轮 = 一次用户提问 + 对应助手回答，含中间 tool 消息），始终保留 system prompt；不做历史总结。
- 大模型配置为**多配置 CRUD**，每项字段 `{ id, name, baseUrl, apiKey, model, isDefault }`，标记一个默认；AI 助手按选中配置调用。
- 文案与界面元素以原型为准（顶部渐变导航栏、卡片、KPI 行、统计条、表格、弹窗、toast、聊天、表单、明暗切换）。
- 弃用 Naive UI；TypeScript `strict: true`、`noUnusedLocals`/`noUnusedParameters` 开启，代码不得有未使用变量/参数。
- 图表主色 `#2C7CF5`（高亮/众数），普通柱色 `#7BA9E8`，轨道 `#E9EEF5`。

---

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

### Task 2: 类型定义 + 统计聚合函数（TDD）

**Files:**
- Create: `frontend/src/types.ts`
- Create: `frontend/src/utils/stats.ts`
- Test: `frontend/src/utils/stats.test.ts`

**Interfaces:**
- Produces（后续所有任务依赖）:
  - `types.ts`: `interface Record { id: string; name: string; birth: string; unit: string; score: number; age: number }`、`interface LlmConfig { id: string; name: string; baseUrl: string; apiKey: string; model: string; isDefault: boolean }`、`interface ChatMessage { role: 'system' | 'user' | 'assistant' | 'tool'; content: string; tool_calls?: ToolCall[]; tool_call_id?: string; name?: string }`、`interface ToolCall { id: string; type: 'function'; function: { name: string; arguments: string } }`
  - `stats.ts` 导出下面全部函数（签名见实现）。

- [ ] **Step 1: 写失败测试**

创建 `frontend/src/utils/stats.test.ts`：

```ts
import { describe, it, expect } from 'vitest'
import {
  summarize, ageDistribution, ageMode, topUnits,
  unitSizeDistribution, totalUnits, scoreDistribution, median,
} from './stats'
import type { Record } from '../types'

const rec = (id: string, birth: string, unit: string, score: number): Record =>
  ({ id, name: 'N' + id, birth, unit, score, age: 2026 - Number(birth.slice(0, 4)) })

const records: Record[] = [
  rec('1', '1980-01', 'A公司', 130),
  rec('2', '1982-05', 'A公司', 120),
  rec('3', '1982-08', 'B公司', 140),
  rec('4', '1990-11', 'C公司', 125),
  rec('5', '1982-03', 'A公司', 121),
]

describe('median', () => {
  it('偶数个取平均', () => { expect(median([1, 2, 3, 4])).toBe(2.5) })
  it('奇数个取中间', () => { expect(median([5, 1, 3])).toBe(3) })
})

describe('summarize', () => {
  it('计算总分与积分/年龄统计', () => {
    const s = summarize(records)
    expect(s.total).toBe(5)
    expect(s.scoreMax).toBe(140)
    expect(s.scoreMin).toBe(120)
    expect(s.ageMin).toBe(36)
    expect(s.ageMax).toBe(46)
  })
})

describe('ageDistribution', () => {
  it('按年龄升序统计', () => {
    const d = ageDistribution(records)
    expect(d[0]).toEqual({ age: 36, count: 1 })
    expect(d.find(x => x.age === 44)!.count).toBe(3)
  })
})

describe('ageMode', () => {
  it('返回人数最多的年龄', () => {
    expect(ageMode(records)).toEqual({ value: 44, count: 3 })
  })
})

describe('topUnits', () => {
  it('按人数降序', () => {
    expect(topUnits(records, 2)).toEqual([
      { unit: 'A公司', count: 3 },
      { unit: 'B公司', count: 1 },
    ])
  })
})

describe('totalUnits', () => {
  it('统计去重单位数', () => { expect(totalUnits(records)).toBe(3) })
})

describe('unitSizeDistribution', () => {
  it('按规模分桶', () => {
    const d = unitSizeDistribution(records)
    expect(d[0]).toEqual({ label: '1 人', units: 2, people: 2 })
    expect(d[1]).toEqual({ label: '2–5 人', units: 1, people: 3 })
  })
})

describe('scoreDistribution', () => {
  it('每 2 分一个桶', () => {
    const d = scoreDistribution(records, 2)
    expect(d[0]).toMatchObject({ lo: 120, hi: 122, count: 2 })
  })
})
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd frontend && npx vitest run src/utils/stats.test.ts`
Expected: FAIL，`Cannot find module './stats'`。

- [ ] **Step 3: 实现 types.ts**

创建 `frontend/src/types.ts`：

```ts
export interface Record {
  id: string
  name: string
  birth: string
  unit: string
  score: number
  age: number
}

export interface LlmConfig {
  id: string
  name: string
  baseUrl: string
  apiKey: string
  model: string
  isDefault: boolean
}

export interface ToolCall {
  id: string
  type: 'function'
  function: { name: string; arguments: string }
}

export interface ChatMessage {
  role: 'system' | 'user' | 'assistant' | 'tool'
  content: string
  tool_calls?: ToolCall[]
  tool_call_id?: string
  name?: string
}
```

- [ ] **Step 4: 实现 stats.ts**

创建 `frontend/src/utils/stats.ts`：

```ts
import type { Record } from '../types'

export interface KpiSummary {
  total: number
  scoreMin: number
  scoreMax: number
  scoreAvg: number
  scoreMedian: number
  ageMin: number
  ageMax: number
  ageAvg: number
  ageMedian: number
}

export interface AgeBucket { age: number; count: number }
export interface UnitBucket { unit: string; count: number }
export interface UnitSizeBucket { label: string; units: number; people: number }
export interface ScoreBucket { label: string; lo: number; hi: number; count: number }

export function median(values: number[]): number {
  const sorted = [...values].sort((a, b) => a - b)
  const mid = Math.floor(sorted.length / 2)
  return sorted.length % 2 ? sorted[mid] : (sorted[mid - 1] + sorted[mid]) / 2
}

export function summarize(records: Record[]): KpiSummary {
  const scores = records.map(r => r.score)
  const ages = records.map(r => r.age)
  const total = records.length
  const avg = (xs: number[]) => (total ? xs.reduce((a, b) => a + b, 0) / total : 0)
  return {
    total,
    scoreMin: Math.min(...scores),
    scoreMax: Math.max(...scores),
    scoreAvg: avg(scores),
    scoreMedian: median(scores),
    ageMin: Math.min(...ages),
    ageMax: Math.max(...ages),
    ageAvg: avg(ages),
    ageMedian: median(ages),
  }
}

export function ageDistribution(records: Record[]): AgeBucket[] {
  const map = new Map<number, number>()
  for (const r of records) map.set(r.age, (map.get(r.age) ?? 0) + 1)
  return [...map.entries()].map(([age, count]) => ({ age, count })).sort((a, b) => a.age - b.age)
}

export function ageMode(records: Record[]): { value: number; count: number } {
  let best = { value: 0, count: 0 }
  for (const b of ageDistribution(records)) {
    if (b.count > best.count) best = { value: b.age, count: b.count }
  }
  return best
}

export function topUnits(records: Record[], n: number): UnitBucket[] {
  const map = new Map<string, number>()
  for (const r of records) map.set(r.unit, (map.get(r.unit) ?? 0) + 1)
  return [...map.entries()]
    .map(([unit, count]) => ({ unit, count }))
    .sort((a, b) => b.count - a.count)
    .slice(0, n)
}

export function totalUnits(records: Record[]): number {
  return new Set(records.map(r => r.unit)).size
}

export function unitSizeDistribution(records: Record[]): UnitSizeBucket[] {
  const map = new Map<string, number>()
  for (const r of records) map.set(r.unit, (map.get(r.unit) ?? 0) + 1)
  const buckets: UnitSizeBucket[] = [
    { label: '1 人', units: 0, people: 0 },
    { label: '2–5 人', units: 0, people: 0 },
    { label: '6–10 人', units: 0, people: 0 },
    { label: '11–20 人', units: 0, people: 0 },
    { label: '20+ 人', units: 0, people: 0 },
  ]
  const idx = (c: number) => (c <= 1 ? 0 : c <= 5 ? 1 : c <= 10 ? 2 : c <= 20 ? 3 : 4)
  for (const count of map.values()) {
    buckets[idx(count)].units += 1
    buckets[idx(count)].people += count
  }
  return buckets
}

export function scoreDistribution(records: Record[], binSize = 2): ScoreBucket[] {
  if (!records.length) return []
  const lo0 = Math.floor(Math.min(...records.map(r => r.score)) / binSize) * binSize
  const hi0 = Math.ceil(Math.max(...records.map(r => r.score)) / binSize) * binSize
  if (lo0 === hi0) {
    return [{ label: `${lo0}–${lo0 + binSize}`, lo: lo0, hi: lo0 + binSize, count: records.length }]
  }
  const buckets: ScoreBucket[] = []
  for (let lo = lo0; lo < hi0; lo += binSize) {
    const hi = lo + binSize
    buckets.push({
      label: `${lo}–${hi}`,
      lo, hi,
      count: records.filter(r => (r.score >= lo && r.score < hi) || (hi === hi0 && r.score === hi)).length,
    })
  }
  return buckets
}
```

- [ ] **Step 5: 运行测试确认通过**

Run: `cd frontend && npx vitest run src/utils/stats.test.ts`
Expected: PASS（8 个测试全绿）。若 `Math.min(...[])` 对空数组返回 `Infinity` 的边界未覆盖，本任务不要求空数组处理（空数据由上层 UI 守卫）。

- [ ] **Step 6: Commit**

```bash
git add frontend/src/types.ts frontend/src/utils/stats.ts frontend/src/utils/stats.test.ts
git commit -m "feat: 数据模型与统计聚合函数"
```

---

### Task 3: Excel 导入解析（TDD）

**Files:**
- Create: `frontend/src/data/importer.ts`
- Test: `frontend/src/data/importer.test.ts`

**Interfaces:**
- Consumes: `Record`（Task 2）。
- Produces: `deriveAge(birth, baseYear?): number`、`rowsToRecords(rows: unknown[][]): Record[]`、`mergeRecords(existing, incoming): Record[]`、`parseWorkbook(buf: ArrayBuffer): Record[]`。

- [ ] **Step 1: 写失败测试**

创建 `frontend/src/data/importer.test.ts`：

```ts
import { describe, it, expect } from 'vitest'
import { deriveAge, rowsToRecords, mergeRecords } from './importer'
import type { Record } from '../types'

describe('deriveAge', () => {
  it('用基准年减出生年', () => {
    expect(deriveAge('1982-08', 2026)).toBe(44)
    expect(deriveAge('1969-11', 2026)).toBe(57)
  })
})

describe('rowsToRecords', () => {
  const header = ['公示编号', '姓名', '出生年月', '单位名称', '积分分值']
  it('映射表头并派生年龄', () => {
    const rows = [header, ['202600001', '许磊', '1982-08', '某公司', 145.29]]
    const out = rowsToRecords(rows)
    expect(out[0]).toEqual({ id: '202600001', name: '许磊', birth: '1982-08', unit: '某公司', score: 145.29, age: 44 })
  })
  it('积分按字符串也解析为数字', () => {
    const rows = [header, ['202600002', '某人', '1980-01', '某公司', '142.34']]
    expect(rowsToRecords(rows)[0].score).toBe(142.34)
  })
})

describe('mergeRecords', () => {
  const a: Record = { id: '1', name: '甲', birth: '1980-01', unit: 'A', score: 130, age: 46 }
  it('存在则更新', () => {
    const b: Record = { ...a, score: 135 }
    expect(mergeRecords([a], [b])).toEqual([b])
  })
  it('不存在则新增，保持现有顺序', () => {
    const b: Record = { id: '2', name: '乙', birth: '1980-02', unit: 'B', score: 120, age: 46 }
    expect(mergeRecords([a], [b]).map(r => r.id)).toEqual(['1', '2'])
  })
})
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd frontend && npx vitest run src/data/importer.test.ts`
Expected: FAIL，`Cannot find module './importer'`。

- [ ] **Step 3: 实现 importer.ts**

创建 `frontend/src/data/importer.ts`：

```ts
import * as XLSX from 'xlsx'
import type { Record } from '../types'

export function deriveAge(birth: string, baseYear = 2026): number {
  const y = Number(String(birth).slice(0, 4))
  return Number.isFinite(y) ? baseYear - y : 0
}

export function rowsToRecords(rows: unknown[][]): Record[] {
  const header = rows[0]?.map(String) ?? []
  const idx = {
    id: header.indexOf('公示编号'),
    name: header.indexOf('姓名'),
    birth: header.indexOf('出生年月'),
    unit: header.indexOf('单位名称'),
    score: header.indexOf('积分分值'),
  }
  const out: Record[] = []
  for (const row of rows.slice(1)) {
    if (!row || row.every(c => c == null || c === '')) continue
    const birth = String(row[idx.birth] ?? '')
    out.push({
      id: String(row[idx.id] ?? ''),
      name: String(row[idx.name] ?? ''),
      birth,
      unit: String(row[idx.unit] ?? ''),
      score: Number(row[idx.score] ?? 0),
      age: deriveAge(birth),
    })
  }
  return out
}

export function mergeRecords(existing: Record[], incoming: Record[]): Record[] {
  const map = new Map(existing.map(r => [r.id, r]))
  for (const r of incoming) map.set(r.id, r)
  return [...map.values()]
}

export function parseWorkbook(buf: ArrayBuffer): Record[] {
  const wb = XLSX.read(buf, { type: 'array' })
  const sheet = wb.Sheets[wb.SheetNames[0]]
  const rows = XLSX.utils.sheet_to_json(sheet, { header: 1, raw: true }) as unknown[][]
  return rowsToRecords(rows)
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd frontend && npx vitest run src/data/importer.test.ts`
Expected: PASS。

- [ ] **Step 5: Commit**

```bash
git add frontend/src/data/importer.ts frontend/src/data/importer.test.ts
git commit -m "feat: Excel 导入解析与增量合并"
```

---

### Task 4: 数据 Store（localStorage 持久化 + CRUD）（TDD）

**Files:**
- Create: `frontend/src/stores/useData.ts`
- Test: `frontend/src/stores/useData.test.ts`

**Interfaces:**
- Consumes: `Record`、`mergeRecords`（Task 2/3）。
- Produces: `useData()` 返回 `{ records: Ref<Record[]>, isEmpty: ComputedRef<boolean>, importData(incoming), addRecord(r), updateRecord(id, patch), removeRecord(id), clearAll() }`。

- [ ] **Step 1: 写失败测试**

创建 `frontend/src/stores/useData.test.ts`：

```ts
import { describe, it, expect, beforeEach } from 'vitest'
import { useData } from './useData'
import type { Record } from '../types'

const KEY = 'points-settlement:records'
const r = (id: string): Record => ({ id, name: 'N', birth: '1980-01', unit: 'U', score: 100, age: 46 })

beforeEach(() => {
  localStorage.clear()
  useData().clearAll()
})

describe('useData', () => {
  it('初始为空', () => {
    const { records, isEmpty } = useData()
    expect(records.value).toEqual([])
    expect(isEmpty.value).toBe(true)
  })
  it('导入持久化到 localStorage', () => {
    const d = useData()
    d.importData([r('1')])
    expect(JSON.parse(localStorage.getItem(KEY)!)).toHaveLength(1)
  })
  it('增量导入按 id 去重', () => {
    const d = useData()
    d.importData([r('1')])
    d.importData([{ ...r('1'), score: 999 }, r('2')])
    expect(d.records.value).toHaveLength(2)
    expect(d.records.value.find(x => x.id === '1')!.score).toBe(999)
  })
  it('增删改与清空', () => {
    const d = useData()
    d.addRecord(r('1'))
    d.updateRecord('1', { score: 120 })
    expect(d.records.value[0].score).toBe(120)
    d.removeRecord('1')
    expect(d.records.value).toHaveLength(0)
    d.addRecord(r('9'))
    d.clearAll()
    expect(d.records.value).toHaveLength(0)
  })
})
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd frontend && npx vitest run src/stores/useData.test.ts`
Expected: FAIL，`Cannot find module './useData'`。

- [ ] **Step 3: 实现 useData.ts**

创建 `frontend/src/stores/useData.ts`：

```ts
import { ref, computed } from 'vue'
import type { Ref, ComputedRef } from 'vue'
import type { Record } from '../types'
import { mergeRecords } from '../data/importer'

const STORAGE_KEY = 'points-settlement:records'

function load(): Record[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) as Record[] : []
  } catch {
    return []
  }
}

function persist(records: Record[]): void {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(records))
}

const records = ref<Record[]>(load())

export interface DataStore {
  records: Ref<Record[]>
  isEmpty: ComputedRef<boolean>
  importData: (incoming: Record[]) => void
  addRecord: (r: Record) => void
  updateRecord: (id: string, patch: Partial<Record>) => void
  removeRecord: (id: string) => void
  clearAll: () => void
}

export function useData(): DataStore {
  return {
    records,
    isEmpty: computed(() => records.value.length === 0),
    importData(incoming) {
      records.value = mergeRecords(records.value, incoming)
      persist(records.value)
    },
    addRecord(r) {
      records.value = [...records.value, r]
      persist(records.value)
    },
    updateRecord(id, patch) {
      records.value = records.value.map(r => (r.id === id ? { ...r, ...patch } : r))
      persist(records.value)
    },
    removeRecord(id) {
      records.value = records.value.filter(r => r.id !== id)
      persist(records.value)
    },
    clearAll() {
      records.value = []
      persist(records.value)
    },
  }
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd frontend && npx vitest run src/stores/useData.test.ts`
Expected: PASS。

- [ ] **Step 5: Commit**

```bash
git add frontend/src/stores/useData.ts frontend/src/stores/useData.test.ts
git commit -m "feat: 数据 store（localStorage 持久化 + CRUD）"
```

---

### Task 5: 大模型配置 Store（TDD）

**Files:**
- Create: `frontend/src/stores/useLlmConfig.ts`
- Test: `frontend/src/stores/useLlmConfig.test.ts`

**Interfaces:**
- Consumes: `LlmConfig`（Task 2）。
- Produces: `useLlmConfig()` 返回 `{ configs, defaultConfig, add(config), update(id, patch), remove(id), setDefault(id), clearAll() }`；`add` 入参为 `Omit<LlmConfig, 'id' | 'isDefault'>`。

- [ ] **Step 1: 写失败测试**

创建 `frontend/src/stores/useLlmConfig.test.ts`：

```ts
import { describe, it, expect, beforeEach } from 'vitest'
import { useLlmConfig } from './useLlmConfig'

beforeEach(() => {
  localStorage.clear()
  useLlmConfig().clearAll()
})

const cfg = () => ({ name: '主模型', baseUrl: 'https://a.com/v1', apiKey: 'sk-1', model: 'gpt-4o-mini' })

describe('useLlmConfig', () => {
  it('新增配置，首个自动设为默认', () => {
    const s = useLlmConfig()
    const c = s.add(cfg())
    expect(c.id).toBeTruthy()
    expect(c.isDefault).toBe(true)
    expect(s.defaultConfig.value?.id).toBe(c.id)
  })
  it('setDefault 切换默认', () => {
    const s = useLlmConfig()
    const a = s.add(cfg())
    const b = s.add(cfg())
    s.setDefault(a.id)
    expect(s.defaultConfig.value?.id).toBe(a.id)
    expect(s.configs.value.find(x => x.id === b.id)!.isDefault).toBe(false)
  })
  it('删除后持久化', () => {
    const s = useLlmConfig()
    const c = s.add(cfg())
    s.remove(c.id)
    expect(s.configs.value).toHaveLength(0)
    expect(localStorage.getItem('points-settlement:llmConfigs')).toBe('[]')
  })
})
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd frontend && npx vitest run src/stores/useLlmConfig.test.ts`
Expected: FAIL。

- [ ] **Step 3: 实现 useLlmConfig.ts**

创建 `frontend/src/stores/useLlmConfig.ts`：

```ts
import { ref, computed } from 'vue'
import type { Ref, ComputedRef } from 'vue'
import type { LlmConfig } from '../types'

const STORAGE_KEY = 'points-settlement:llmConfigs'

function load(): LlmConfig[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) as LlmConfig[] : []
  } catch {
    return []
  }
}

const configs = ref<LlmConfig[]>(load())

function genId(): string {
  return 'cfg-' + Math.random().toString(36).slice(2, 10)
}

export interface LlmConfigStore {
  configs: Ref<LlmConfig[]>
  defaultConfig: ComputedRef<LlmConfig | null>
  add: (config: Omit<LlmConfig, 'id' | 'isDefault'>) => LlmConfig
  update: (id: string, patch: Partial<LlmConfig>) => void
  remove: (id: string) => void
  setDefault: (id: string) => void
  clearAll: () => void
}

export function useLlmConfig(): LlmConfigStore {
  return {
    configs,
    defaultConfig: computed(() => configs.value.find(c => c.isDefault) ?? null),
    add(config) {
      const item: LlmConfig = { ...config, id: genId(), isDefault: configs.value.length === 0 }
      configs.value = [...configs.value, item]
      localStorage.setItem(STORAGE_KEY, JSON.stringify(configs.value))
      return item
    },
    update(id, patch) {
      configs.value = configs.value.map(c => (c.id === id ? { ...c, ...patch } : c))
      localStorage.setItem(STORAGE_KEY, JSON.stringify(configs.value))
    },
    remove(id) {
      configs.value = configs.value.filter(c => c.id !== id)
      if (!configs.value.some(c => c.isDefault) && configs.value[0]) configs.value[0].isDefault = true
      localStorage.setItem(STORAGE_KEY, JSON.stringify(configs.value))
    },
    setDefault(id) {
      configs.value = configs.value.map(c => ({ ...c, isDefault: c.id === id }))
      localStorage.setItem(STORAGE_KEY, JSON.stringify(configs.value))
    },
    clearAll() {
      configs.value = []
      localStorage.setItem(STORAGE_KEY, '[]')
    },
  }
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd frontend && npx vitest run src/stores/useLlmConfig.test.ts`
Expected: PASS。

- [ ] **Step 5: Commit**

```bash
git add frontend/src/stores/useLlmConfig.ts frontend/src/stores/useLlmConfig.test.ts
git commit -m "feat: 大模型配置 store（多配置 CRUD + 默认）"
```

---

### Task 6: AI 对话历史（10 轮裁剪 + 持久化）（TDD）

**Files:**
- Create: `frontend/src/ai/history.ts`
- Test: `frontend/src/ai/history.test.ts`

**Interfaces:**
- Consumes: `ChatMessage`（Task 2）。
- Produces: `MAX_TURNS = 10`、`trimHistory(messages): ChatMessage[]`、`loadHistory(): ChatMessage[]`、`saveHistory(messages): void`、`clearHistory(): void`。

- [ ] **Step 1: 写失败测试**

创建 `frontend/src/ai/history.test.ts`：

```ts
import { describe, it, expect, beforeEach } from 'vitest'
import { trimHistory, loadHistory, saveHistory, clearHistory, MAX_TURNS } from './history'
import type { ChatMessage } from '../types'

const sys: ChatMessage = { role: 'system', content: 'S' }
const user = (i: number): ChatMessage => ({ role: 'user', content: `u${i}` })
const ai = (i: number): ChatMessage => ({ role: 'assistant', content: `a${i}` })

describe('trimHistory', () => {
  it('轮次不超过上限时原样返回', () => {
    const msgs = [sys, user(1), ai(1), user(2), ai(2)]
    expect(trimHistory(msgs)).toEqual(msgs)
  })
  it('超过上限保留 system + 最近 10 轮', () => {
    const msgs: ChatMessage[] = [sys]
    for (let i = 1; i <= 12; i++) msgs.push(user(i), ai(i))
    const out = trimHistory(msgs)
    expect(out[0]).toEqual(sys)
    expect(out.filter(m => m.role === 'user')).toHaveLength(MAX_TURNS)
    expect(out[1]).toEqual(user(3))
  })
})

describe('persistence', () => {
  beforeEach(() => localStorage.clear())
  it('保存与读取', () => {
    const msgs = [user(1), ai(1)]
    saveHistory(msgs)
    expect(loadHistory()).toEqual(msgs)
    clearHistory()
    expect(loadHistory()).toEqual([])
  })
})
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd frontend && npx vitest run src/ai/history.test.ts`
Expected: FAIL。

- [ ] **Step 3: 实现 history.ts**

创建 `frontend/src/ai/history.ts`：

```ts
import type { ChatMessage } from '../types'

const STORAGE_KEY = 'points-settlement:chatHistory'
export const MAX_TURNS = 10

export function trimHistory(messages: ChatMessage[]): ChatMessage[] {
  const userIdx = messages.map((m, i) => (m.role === 'user' ? i : -1)).filter(i => i >= 0)
  if (userIdx.length <= MAX_TURNS) return messages
  const start = userIdx[userIdx.length - MAX_TURNS]
  const head = messages[0]?.role === 'system' ? [messages[0]] : []
  return [...head, ...messages.slice(start)]
}

export function loadHistory(): ChatMessage[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) as ChatMessage[] : []
  } catch {
    return []
  }
}

export function saveHistory(messages: ChatMessage[]): void {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(trimHistory(messages)))
}

export function clearHistory(): void {
  localStorage.removeItem(STORAGE_KEY)
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd frontend && npx vitest run src/ai/history.test.ts`
Expected: PASS。

- [ ] **Step 5: Commit**

```bash
git add frontend/src/ai/history.ts frontend/src/ai/history.test.ts
git commit -m "feat: AI 对话历史（10 轮裁剪 + localStorage）"
```

---

### Task 7: AI 工具集定义与执行（TDD）

**Files:**
- Create: `frontend/src/ai/tools.ts`
- Test: `frontend/src/ai/tools.test.ts`

**Interfaces:**
- Consumes: `Record`、`stats.ts` 全部函数（Task 2）。
- Produces: `TOOLS: ToolDef[]`（含 OpenAI `tools` 数组定义 + handler）、`runTool(name, records, args): string`（返回 JSON 字符串，供 `role:"tool"` 消息用）。`ToolDef = { name; description; parameters: object; handler: (records, args) => unknown }`。

- [ ] **Step 1: 写失败测试**

创建 `frontend/src/ai/tools.test.ts`：

```ts
import { describe, it, expect } from 'vitest'
import { runTool } from './tools'
import type { Record } from '../types'

const rec = (id: string, name: string, unit: string, score: number, birth = '1980-01'): Record =>
  ({ id, name, birth, unit, score, age: 2026 - Number(birth.slice(0, 4)) })

const records: Record[] = [
  rec('1', '许磊', '华为', 145.29),
  rec('2', '张三', '腾讯', 130),
  rec('3', '李四', '华为', 128),
]

describe('runTool', () => {
  it('top_units 返回排名', () => {
    const out = JSON.parse(runTool('top_units', records, { n: 2 }))
    expect(out[0]).toEqual({ unit: '华为', count: 2 })
  })
  it('search_by_name 返回命中', () => {
    const out = JSON.parse(runTool('search_by_name', records, { name: '许磊' }))
    expect(out[0].id).toBe('1')
  })
  it('score_stats 返回汇总', () => {
    const out = JSON.parse(runTool('score_stats', records, {}))
    expect(out.scoreMax).toBe(145.29)
  })
  it('未知工具抛错', () => {
    expect(() => runTool('nope', records, {})).toThrow()
  })
})
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd frontend && npx vitest run src/ai/tools.test.ts`
Expected: FAIL。

- [ ] **Step 3: 实现 tools.ts**

创建 `frontend/src/ai/tools.ts`：

```ts
import type { Record } from '../types'
import {
  summarize, ageDistribution, topUnits, unitSizeDistribution, scoreDistribution,
} from '../utils/stats'

export interface ToolDef {
  name: string
  description: string
  parameters: { [key: string]: unknown }
  handler: (records: Record[], args: any) => unknown
}

function top(records: Record[], n: number) {
  return [...records].sort((a, b) => b.score - a.score).slice(0, n)
    .map(r => ({ id: r.id, name: r.name, unit: r.unit, score: r.score }))
}

function searchName(records: Record[], name: string) {
  return records.filter(r => r.name.includes(name)).slice(0, 20)
    .map(r => ({ id: r.id, name: r.name, birth: r.birth, unit: r.unit, score: r.score, age: r.age }))
}

export const TOOLS: ToolDef[] = [
  {
    name: 'search_by_name', description: '按姓名模糊查询人员明细',
    parameters: { type: 'object', properties: { name: { type: 'string', description: '姓名关键词' } }, required: ['name'] },
    handler: (records, a) => searchName(records, a.name),
  },
  {
    name: 'search_by_unit', description: '按单位名称查询该单位入围人员',
    parameters: { type: 'object', properties: { unit: { type: 'string', description: '单位名称关键词' } }, required: ['unit'] },
    handler: (records, a) => {
      const hit = records.filter(r => r.unit.includes(a.unit))
      return { count: hit.length, people: hit.slice(0, 50).map(r => ({ id: r.id, name: r.name, score: r.score })) }
    },
  },
  {
    name: 'top_units', description: '单位入围人数排名',
    parameters: { type: 'object', properties: { n: { type: 'integer', description: '返回前 N 名，默认 20' } } },
    handler: (records, a) => topUnits(records, a.n ?? 20),
  },
  {
    name: 'unit_size_distribution', description: '单位规模分布（按每单位入围人数分桶）',
    parameters: { type: 'object', properties: {} },
    handler: (records) => unitSizeDistribution(records),
  },
  {
    name: 'score_distribution', description: '积分分数段分布',
    parameters: { type: 'object', properties: { binSize: { type: 'integer', description: '分段宽度，默认 2' } } },
    handler: (records, a) => scoreDistribution(records, a.binSize ?? 2),
  },
  {
    name: 'age_distribution', description: '年龄分布（每个年龄的人数）',
    parameters: { type: 'object', properties: {} },
    handler: (records) => ageDistribution(records),
  },
  {
    name: 'score_stats', description: '积分汇总统计（最高/最低/平均/中位数）',
    parameters: { type: 'object', properties: {} },
    handler: (records) => {
      const s = summarize(records)
      return { scoreMin: s.scoreMin, scoreMax: s.scoreMax, scoreAvg: s.scoreAvg, scoreMedian: s.scoreMedian, total: s.total }
    },
  },
  {
    name: 'count_by_range', description: '通用区间计数（按 age 或 score）',
    parameters: {
      type: 'object',
      properties: {
        field: { type: 'string', enum: ['age', 'score'], description: '统计字段' },
        min: { type: 'number', description: '下界（含）' },
        max: { type: 'number', description: '上界（含）' },
      },
      required: ['field'],
    },
    handler: (records, a) => {
      const field = a.field as 'age' | 'score'
      const min = a.min ?? -Infinity, max = a.max ?? Infinity
      return { count: records.filter(r => r[field] >= min && r[field] <= max).length }
    },
  },
  {
    name: 'top_people_by_score', description: '按积分排名的人员',
    parameters: { type: 'object', properties: { n: { type: 'integer', description: '返回前 N 名，默认 10' } } },
    handler: (records, a) => top(records, a.n ?? 10),
  },
  {
    name: 'get_person_detail', description: '按公示编号或姓名查询单人明细',
    parameters: { type: 'object', properties: { key: { type: 'string', description: '公示编号或姓名' } }, required: ['key'] },
    handler: (records, a) => {
      const r = records.find(x => x.id === a.key || x.name === a.key)
      return r ? { id: r.id, name: r.name, birth: r.birth, age: r.age, unit: r.unit, score: r.score } : { notFound: true }
    },
  },
]

export function runTool(name: string, records: Record[], args: any): string {
  const tool = TOOLS.find(t => t.name === name)
  if (!tool) throw new Error(`未知工具: ${name}`)
  return JSON.stringify(tool.handler(records, args ?? {}))
}

export function openaiTools() {
  return TOOLS.map(t => ({ type: 'function', function: { name: t.name, description: t.description, parameters: t.parameters } }))
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd frontend && npx vitest run src/ai/tools.test.ts`
Expected: PASS。

- [ ] **Step 5: Commit**

```bash
git add frontend/src/ai/tools.ts frontend/src/ai/tools.test.ts
git commit -m "feat: AI 固定工具集（10 个查询工具）"
```

---

### Task 8: AI 流式客户端 + System Prompt（TDD 解析部分）

**Files:**
- Create: `frontend/src/ai/client.ts`
- Create: `frontend/src/ai/prompt.ts`
- Test: `frontend/src/ai/client.test.ts`

**Interfaces:**
- Consumes: `ChatMessage`、`ToolCall`、`openaiTools`（Task 7）。
- Produces:
  - `client.ts`: `ToolCallDelta`、`accumulateToolCalls(existing: ToolCall[], deltas: ToolCallDelta[]): ToolCall[]`、`streamChatCompletion(opts): Promise<{ content: string; toolCalls: ToolCall[] }>`。`opts = { baseUrl; apiKey; model; messages; tools; onDelta(text) }`。
  - `prompt.ts`: `SYSTEM_PROMPT: string`。

- [ ] **Step 1: 写失败测试（纯函数部分）**

创建 `frontend/src/ai/client.test.ts`：

```ts
import { describe, it, expect } from 'vitest'
import { accumulateToolCalls } from './client'
import type { ToolCallDelta } from './client'

describe('accumulateToolCalls', () => {
  it('按 index 合并分片参数', () => {
    const deltas: ToolCallDelta[] = [
      { index: 0, id: 'call_1', function: { name: 'top_units', arguments: '{"n":' } },
      { index: 0, function: { arguments: '2}' } },
    ]
    const out = accumulateToolCalls([], deltas)
    expect(out).toHaveLength(1)
    expect(out[0].function.arguments).toBe('{"n":2}')
    expect(out[0].function.name).toBe('top_units')
  })
})
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd frontend && npx vitest run src/ai/client.test.ts`
Expected: FAIL。

- [ ] **Step 3: 实现 prompt.ts**

创建 `frontend/src/ai/prompt.ts`：

```ts
export const SYSTEM_PROMPT = [
  '你是「北京市积分落户公示名单」数据分析助手。数据集每条记录含字段：公示编号 id、姓名 name、出生年月 birth（YYYY-MM）、单位名称 unit、积分分值 score、年龄 age（age = 2026 − 出生年）。',
  '用户会用中文提问统计、排名或明细类问题。你必须通过 function calling 调用工具访问真实数据，不得凭空编造数字。',
  '回答要求：用简体中文、简洁；给出具体数字，并注明数据来源（工具名）；工具无法覆盖的问题要如实说明。',
].join('\n')
```

- [ ] **Step 4: 实现 client.ts**

创建 `frontend/src/ai/client.ts`：

```ts
import type { ChatMessage, ToolCall } from '../types'

export interface ToolCallDelta {
  index?: number
  id?: string
  type?: 'function'
  function?: { name?: string; arguments?: string }
}

export function accumulateToolCalls(existing: ToolCall[], deltas: ToolCallDelta[]): ToolCall[] {
  const byIndex = new Map<number, ToolCall>()
  existing.forEach((c, i) => byIndex.set(i, c))
  for (const d of deltas) {
    const idx = d.index ?? 0
    const cur = byIndex.get(idx) ?? { id: '', type: 'function', function: { name: '', arguments: '' } }
    if (d.id) cur.id = d.id
    if (d.function?.name) cur.function.name += d.function.name
    if (d.function?.arguments) cur.function.arguments += d.function.arguments
    byIndex.set(idx, cur)
  }
  return [...byIndex.values()]
}

interface StreamOptions {
  baseUrl: string
  apiKey: string
  model: string
  messages: ChatMessage[]
  tools: unknown[]
  onDelta: (text: string) => void
}

export interface StreamResult {
  content: string
  toolCalls: ToolCall[]
}

function resolveUrl(baseUrl: string): string {
  const base = baseUrl.replace(/\/+$/, '')
  return base.endsWith('/chat/completions') ? base : `${base}/chat/completions`
}

export async function streamChatCompletion(opts: StreamOptions): Promise<StreamResult> {
  const res = await fetch(resolveUrl(opts.baseUrl), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${opts.apiKey}`,
    },
    body: JSON.stringify({
      model: opts.model,
      messages: opts.messages,
      tools: opts.tools,
      stream: true,
    }),
  })
  if (!res.ok || !res.body) throw new Error(`请求失败: ${res.status}`)

  const reader = res.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let content = ''
  const toolDeltas: ToolCallDelta[] = []

  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop() ?? ''
    for (const line of lines) {
      const trimmed = line.trim()
      if (!trimmed.startsWith('data:')) continue
      const data = trimmed.slice(5).trim()
      if (data === '[DONE]') continue
      try {
        const json = JSON.parse(data)
        const delta = json.choices?.[0]?.delta
        if (delta?.content) {
          content += delta.content
          opts.onDelta(delta.content)
        }
        if (delta?.tool_calls) toolDeltas.push(...delta.tool_calls)
      } catch { /* 忽略无法解析的行 */ }
    }
  }

  const toolCalls = accumulateToolCalls([], toolDeltas)
  return { content, toolCalls }
}
```

- [ ] **Step 5: 运行测试确认通过**

Run: `cd frontend && npx vitest run src/ai/client.test.ts`
Expected: PASS。

- [ ] **Step 6: Commit**

```bash
git add frontend/src/ai/client.ts frontend/src/ai/prompt.ts frontend/src/ai/client.test.ts
git commit -m "feat: AI 流式客户端（SSE 解析 + tool_calls 累积）与 system prompt"
```

---

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

### Task 10: 共享 UI（ECharts 封装 + Toast）

**Files:**
- Create: `frontend/src/components/ChartBox.vue`
- Create: `frontend/src/composables/useToast.ts`

**Interfaces:**
- Consumes: `theme.css`（Task 9）。
- Produces: `ChartBox`（props: `option: echarts.EChartsOption`；自动 init/resize/dispose）；`useToast()` 返回 `{ toast(msg): void }`（通过全局单例 DOM 元素）。

- [ ] **Step 1: 实现 ChartBox.vue**

创建 `frontend/src/components/ChartBox.vue`：

```vue
<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'

const props = defineProps<{ option: EChartsOption }>()
const el = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null

onMounted(() => {
  if (!el.value) return
  chart = echarts.init(el.value)
  chart.setOption(props.option)
  const ro = new ResizeObserver(() => chart?.resize())
  ro.observe(el.value)
  ;(el.value as any).__ro = ro
})

watch(() => props.option, (o) => chart?.setOption(o, true))

onBeforeUnmount(() => {
  ;(el.value as any)?.__ro?.disconnect()
  chart?.dispose()
  chart = null
})
</script>

<template>
  <div ref="el" class="chart-box" style="width: 100%; height: 100%;"></div>
</template>
```

- [ ] **Step 2: 实现 useToast.ts**

创建 `frontend/src/composables/useToast.ts`：

```ts
let el: HTMLDivElement | null = null
let timer: ReturnType<typeof setTimeout> | null = null

export function useToast(): { toast: (msg: string) => void } {
  return {
    toast(msg) {
      if (!el) {
        el = document.createElement('div')
        el.className = 'toast'
        el.innerHTML = '<span id="toast-msg"></span>'
        document.body.appendChild(el)
      }
      el.querySelector('#toast-msg')!.textContent = msg
      el.classList.add('show')
      if (timer) clearTimeout(timer)
      timer = setTimeout(() => el?.classList.remove('show'), 2200)
    },
  }
}
```

- [ ] **Step 3: 验证**

Run: `cd frontend && npm run typecheck`
Expected: PASS（ChartBox 类型正确）。

- [ ] **Step 4: Commit**

```bash
git add frontend/src/components/ChartBox.vue frontend/src/composables/useToast.ts
git commit -m "feat: ECharts 封装组件与 toast 工具"
```

---

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

### Task 12: 总览板块（KPI + 查询筛选 + 表格 + 详情 + 导入 + 增删改）

**Files:**
- Create: `frontend/src/components/Overview.vue`
- Modify: `frontend/src/App.vue`（挂载 Overview）

**Interfaces:**
- Consumes: `useData`（Task 4）、`useToast`（Task 10）、`stats.ts`（Task 2）、`parseWorkbook`（Task 3）、`Record`。
- Produces: 完整总览页。列：公示编号 / 姓名 / 出生年月 / 年龄 / 单位名称 / 积分分值。

- [ ] **Step 1: 实现 Overview.vue**

创建 `frontend/src/components/Overview.vue`。要点（模板结构对齐原型 `#page-overview`，类名用 theme.css 已有类）：

```vue
<script setup lang="ts">
import { ref, computed } from 'vue'
import { useData } from '../stores/useData'
import { useToast } from '../composables/useToast'
import { summarize, totalUnits, topUnits } from '../utils/stats'
import { parseWorkbook, deriveAge } from '../data/importer'
import type { Record } from '../types'

const { records, isEmpty, importData, addRecord, updateRecord, removeRecord, clearAll } = useData()
const { toast } = useToast()

const kpi = computed(() => {
  if (!records.value.length) return null
  const s = summarize(records.value)
  return {
    total: s.total, scoreAvg: s.scoreAvg, scoreMax: s.scoreMax, scoreMin: s.scoreMin,
    ageAvg: s.ageAvg, units: totalUnits(records.value), topUnit: topUnits(records.value, 1)[0],
  }
})

// 筛选
const keyword = ref('')
const ageMin = ref<number | null>(null)
const ageMax = ref<number | null>(null)
const scoreMin = ref<number | null>(null)
const scoreMax = ref<number | null>(null)

const filtered = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return records.value.filter(r => {
    if (kw && !(r.id.toLowerCase().includes(kw) || r.name.toLowerCase().includes(kw) || r.unit.toLowerCase().includes(kw))) return false
    if (ageMin.value != null && r.age < ageMin.value) return false
    if (ageMax.value != null && r.age > ageMax.value) return false
    if (scoreMin.value != null && r.score < scoreMin.value) return false
    if (scoreMax.value != null && r.score > scoreMax.value) return false
    return true
  })
})

// 分页
const pageSize = 10
const page = ref(0)
const paged = computed(() => filtered.value.slice(page.value * pageSize, (page.value + 1) * pageSize))
function resetPage() { page.value = 0 }

// 详情弹窗
const detail = ref<Record | null>(null)

// 导入
async function onImport(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const buf = await file.arrayBuffer()
  const incoming = parseWorkbook(buf)
  const before = records.value.length
  importData(incoming)
  toast(`已导入 ${incoming.length} 条${before ? `（增量合并，当前共 ${records.value.length} 条）` : ''}`)
  ;(e.target as HTMLInputElement).value = ''
}

// 新增/编辑表单
const editing = ref<Record | null>(null)
const form = ref({ id: '', name: '', birth: '', unit: '', score: 0 })
function openNew() { form.value = { id: '', name: '', birth: '', unit: '', score: 0 }; editing.value = { id: '', name: '', birth: '', unit: '', score: 0, age: 0 } }
function openEdit(r: Record) { form.value = { id: r.id, name: r.name, birth: r.birth, unit: r.unit, score: r.score }; editing.value = r }
function saveForm() {
  const patch: Record = {
    id: form.value.id, name: form.value.name, birth: form.value.birth,
    unit: form.value.unit, score: Number(form.value.score), age: deriveAge(form.value.birth),
  }
  if (editing.value && editing.value.id && records.value.some(r => r.id === editing.value!.id)) updateRecord(editing.value.id, patch)
  else addRecord(patch)
  editing.value = null
  toast('已保存')
}
</script>
```

模板结构（在 `<template>` 内，样式类与原型一致）：

```html
<div class="kpi-row" v-if="kpi">
  <div class="kpi"><div class="k-label">落户总人数</div><div class="k-value num">{{ kpi.total }}<span class="unit">人</span></div></div>
  <div class="kpi"><div class="k-label">平均积分</div><div class="k-value num">{{ kpi.scoreAvg.toFixed(2) }}<span class="unit">分</span></div></div>
  <div class="kpi"><div class="k-label">最高积分</div><div class="k-value num">{{ kpi.scoreMax.toFixed(2) }}<span class="unit">分</span></div></div>
  <div class="kpi"><div class="k-label">平均年龄</div><div class="k-value num">{{ kpi.ageAvg.toFixed(1) }}<span class="unit">岁</span></div></div>
  <div class="kpi"><div class="k-label">涉及单位</div><div class="k-value num">{{ kpi.units }}<span class="unit">家</span></div></div>
</div>

<div class="state-overlay" v-if="isEmpty">
  <div class="state-card">
    <h3>暂无数据</h3>
    <p>请先导入北京市积分落户公示名单 Excel 文件。</p>
    <button class="btn primary" @click="fileInput?.click()">导入 Excel</button>
  </div>
</div>

<div v-else>
  <div class="card">
    <div class="card-head">
      <h2 class="card-title">筛选与搜索</h2>
      <span class="card-hint">支持姓名 / 公示编号 / 单位名称模糊匹配</span>
    </div>
    <div class="filter-bar">
      <div class="f-group"><label>关键词搜索</label>
        <div class="f-search"><input v-model="keyword" placeholder="姓名 / 编号 / 单位" @input="resetPage" /></div>
      </div>
      <div class="f-group"><label>年龄区间</label>
        <div class="f-range"><input v-model.number="ageMin" type="number" placeholder="最小" /><span>—</span><input v-model.number="ageMax" type="number" placeholder="最大" /></div>
      </div>
      <div class="f-group"><label>积分区间</label>
        <div class="f-range"><input v-model.number="scoreMin" type="number" step="0.01" placeholder="最小" /><span>—</span><input v-model.number="scoreMax" type="number" step="0.01" placeholder="最大" /></div>
      </div>
      <div class="f-group"><label>&nbsp;</label>
        <div style="display:flex;gap:10px;">
          <button class="btn primary" @click="resetPage">查询</button>
          <button class="btn ghost" @click="keyword='';ageMin=null;ageMax=null;scoreMin=null;scoreMax=null;resetPage()">重置</button>
          <button class="btn secondary" @click="fileInput?.click()">导入 Excel</button>
          <button class="btn secondary" @click="openNew">新增记录</button>
          <button class="btn ghost" @click="clearAll(); toast('已清空')">清空数据</button>
        </div>
      </div>
    </div>
    <input ref="fileInput" type="file" accept=".xlsx,.xls" style="display:none" @change="onImport" />
  </div>

  <div class="card">
    <div class="card-head"><h2 class="card-title">公示名单明细</h2><span class="card-hint">共 {{ filtered.length }} 条</span></div>
    <div class="table-wrap">
      <table>
        <thead><tr><th>公示编号</th><th>姓名</th><th>出生年月</th><th class="r">年龄</th><th>单位名称</th><th class="r">积分分值</th><th></th></tr></thead>
        <tbody>
          <tr v-for="r in paged" :key="r.id" @click="detail = r">
            <td><span class="id">{{ r.id }}</span></td><td>{{ r.name }}</td><td class="num">{{ r.birth }}</td>
            <td class="r"><span class="age">{{ r.age }}</span></td><td><span class="unit-cell">{{ r.unit }}</span></td>
            <td class="r"><span class="score">{{ r.score.toFixed(2) }}</span></td>
            <td>
              <button class="btn ghost" @click.stop="openEdit(r)">编辑</button>
              <button class="btn ghost" @click.stop="removeRecord(r.id); toast('已删除')">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div class="pagination">
      <span class="info">显示第 {{ page * pageSize + 1 }}–{{ Math.min((page + 1) * pageSize, filtered.length) }} 条，共 {{ filtered.length }} 条</span>
      <div class="pg">
        <button :disabled="page <= 0" @click="page--">‹</button>
        <span class="mono">{{ page + 1 }} / {{ Math.max(1, Math.ceil(filtered.length / pageSize)) }}</span>
        <button :disabled="page >= Math.ceil(filtered.length / pageSize) - 1" @click="page++">›</button>
      </div>
    </div>
  </div>
</div>

<!-- 详情弹窗 -->
<div class="modal-mask" :class="{ show: detail }">
  <div class="modal">
    <div class="modal-h"><h3>人员详情</h3><button class="x" @click="detail = null">×</button></div>
    <div class="modal-b"><dl class="dl" v-if="detail">
      <dt>公示编号</dt><dd>{{ detail.id }}</dd><dt>姓名</dt><dd>{{ detail.name }}</dd>
      <dt>出生年月</dt><dd>{{ detail.birth }}</dd><dt>年龄</dt><dd>{{ detail.age }} 岁</dd>
      <dt>单位名称</dt><dd>{{ detail.unit }}</dd><dt>积分分值</dt><dd>{{ detail.score.toFixed(2) }} 分</dd>
    </dl></div>
    <div class="modal-f"><button class="btn primary" @click="detail = null">关闭</button></div>
  </div>
</div>

<!-- 新增/编辑弹窗 -->
<div class="modal-mask" :class="{ show: editing }">
  <div class="modal">
    <div class="modal-h"><h3>{{ editing?.id && records.some(r => r.id === editing.id) ? '编辑记录' : '新增记录' }}</h3><button class="x" @click="editing = null">×</button></div>
    <div class="modal-b">
      <div class="form-grid">
        <div class="f-field"><label>公示编号</label><input v-model="form.id" /></div>
        <div class="f-field"><label>姓名</label><input v-model="form.name" /></div>
        <div class="f-field"><label>出生年月</label><input v-model="form.birth" placeholder="YYYY-MM" /></div>
        <div class="f-field"><label>单位名称</label><input v-model="form.unit" /></div>
        <div class="f-field"><label>积分分值</label><input v-model.number="form.score" type="number" step="0.01" /></div>
      </div>
    </div>
    <div class="modal-f"><button class="btn ghost" @click="editing = null">取消</button><button class="btn primary" @click="saveForm">保存</button></div>
  </div>
</div>
```

（Vue 模板中用 `?` 需要 `fileInput` ref；在 script 中补 `const fileInput = ref<HTMLInputElement>()`。）

- [ ] **Step 2: 挂载到 App.vue**

在 `App.vue` 中 import Overview 并替换总览占位：

```vue
<script setup lang="ts">
import Overview from './components/Overview.vue'
// ...
</script>
<!-- template 中 -->
<Overview v-if="current === 'overview'" />
```

- [ ] **Step 3: 验证**

Run: `cd frontend && npm run dev`。导入 `../../docs/北京市2026年积分落户公示名单.xlsx`，确认：KPI 显示 6003、表格分页、搜索/筛选、行详情弹窗、新增/编辑/删除、清空、刷新后数据保留。

- [ ] **Step 4: Commit**

```bash
git add frontend/src/components/Overview.vue frontend/src/App.vue
git commit -m "feat: 总览板块（KPI + 查询筛选 + 表格 + 导入 + 增删改）"
```

---

### Task 13: 年龄分析板块

**Files:**
- Create: `frontend/src/components/AgeAnalysis.vue`
- Modify: `frontend/src/App.vue`

**Interfaces:**
- Consumes: `useData`、`ChartBox`、`stats.ts`（`summarize`/`ageDistribution`/`ageMode`）。

- [ ] **Step 1: 实现 AgeAnalysis.vue**

要点：统计条（落户人数/最小年龄/最大年龄/年龄中位数/众数年龄）+ 直方图（`ChartBox`，ECharts bar）+ 解读文案。ECharts option：

```ts
const option = computed<EChartsOption>(() => {
  const dist = ageDistribution(records.value)
  const mode = ageMode(records.value)
  return {
    grid: { left: 48, right: 16, top: 24, bottom: 40 },
    xAxis: { type: 'category', data: dist.map(d => d.age), axisLabel: { interval: 1 } },
    yAxis: { type: 'value' },
    tooltip: { trigger: 'axis' },
    series: [{
      type: 'bar', barMaxWidth: 24,
      data: dist.map(d => ({ value: d.count, itemStyle: { color: d.age === mode.value ? '#2C7CF5' : '#7BA9E8' } })),
    }],
  }
})
```

模板结构对齐原型 `#page-age`（`.stat-strip.cols-5` + `.card` + `.chart-box` + 解读卡）。

- [ ] **Step 2: 挂载到 App.vue**

- [ ] **Step 3: 验证**：`npm run dev`，确认直方图渲染、众数年龄段高亮、统计条正确、空数据时不渲染图表（用 `v-if` 守卫）。

- [ ] **Step 4: Commit**

```bash
git add frontend/src/components/AgeAnalysis.vue frontend/src/App.vue
git commit -m "feat: 年龄分析板块"
```

---

### Task 14: 单位分析板块

**Files:**
- Create: `frontend/src/components/UnitAnalysis.vue`
- Modify: `frontend/src/App.vue`

**Interfaces:**
- Consumes: `useData`、`ChartBox`、`stats.ts`（`topUnits`/`unitSizeDistribution`/`totalUnits`/`summarize`）。

- [ ] **Step 1: 实现 UnitAnalysis.vue**

要点：统计条（涉及单位总数/Top 单位入围人数/Top 单位名称/平均每单位人数）+ Top 单位横向条形图（Top 10/20 切换，ECharts `bar` 横向 `yAxis: type:'category'`）+ 单位规模分布。规模分布用 ECharts 横向条形图；底部加解读文案（复刻原型的 `renderUnitSize` 注释）。

- [ ] **Step 2: 挂载到 App.vue**

- [ ] **Step 3: 验证**：`npm run dev`，确认 Top 单位排名正确、Top 10/20 切换生效、规模分布正确、空数据守卫。

- [ ] **Step 4: Commit**

```bash
git add frontend/src/components/UnitAnalysis.vue frontend/src/App.vue
git commit -m "feat: 单位分析板块"
```

---

### Task 15: 积分分布板块

**Files:**
- Create: `frontend/src/components/ScoreDistribution.vue`
- Modify: `frontend/src/App.vue`

**Interfaces:**
- Consumes: `useData`、`ChartBox`、`stats.ts`（`summarize`/`scoreDistribution`）。

- [ ] **Step 1: 实现 ScoreDistribution.vue**

要点：统计条（最高/最低/平均/中位数积分）+ 直方图（`scoreDistribution(records, 2)`，柱按人数最多高亮）+ 分数段统计表（分数段/人数/占比/分布条）。

- [ ] **Step 2: 挂载到 App.vue**

- [ ] **Step 3: 验证**：`npm run dev`，确认直方图与分数段表、占比计算正确、空数据守卫。

- [ ] **Step 4: Commit**

```bash
git add frontend/src/components/ScoreDistribution.vue frontend/src/App.vue
git commit -m "feat: 积分分布板块"
```

---

### Task 16: AI 智能问数板块（流式 + 工具循环 + 历史 + 配置选择）

**Files:**
- Create: `frontend/src/components/AiAssistant.vue`
- Modify: `frontend/src/App.vue`

**Interfaces:**
- Consumes: `useData`、`useLlmConfig`、`streamChatCompletion`（Task 8）、`SYSTEM_PROMPT`（Task 8）、`runTool`/`openaiTools`（Task 7）、`loadHistory`/`saveHistory`/`clearHistory`（Task 6）、`useToast`。

- [ ] **Step 1: 实现对话循环（核心逻辑）**

在 `AiAssistant.vue` 的 `script setup` 中实现多轮流式 + 工具调用循环：

```ts
import { ref, computed } from 'vue'
import { useData } from '../stores/useData'
import { useLlmConfig } from '../stores/useLlmConfig'
import { useToast } from '../composables/useToast'
import { streamChatCompletion } from '../ai/client'
import { openaiTools, runTool } from '../ai/tools'
import { SYSTEM_PROMPT } from '../ai/prompt'
import { loadHistory, saveHistory, clearHistory } from '../ai/history'
import type { ChatMessage } from '../types'

const { records } = useData()
const { configs, defaultConfig } = useLlmConfig()
const { toast } = useToast()

const selectedId = ref<string | null>(defaultConfig.value?.id ?? null)
const activeConfig = computed(() => configs.value.find(c => c.id === selectedId.value) ?? defaultConfig.value)

const messages = ref<ChatMessage[]>(loadHistory())
const input = ref('')
const busy = ref(false)

async function send(text?: string) {
  const q = (text ?? input.value).trim()
  if (!q || busy.value) return
  if (!activeConfig.value) { toast('请先到「大模型管理」新增配置'); return }
  input.value = ''
  messages.value.push({ role: 'user', content: q })
  busy.value = true

  const loop: ChatMessage[] = [{ role: 'system', content: SYSTEM_PROMPT }, ...messages.value]
  let assistant: ChatMessage = { role: 'assistant', content: '' }

  for (let i = 0; i < 6; i++) {
    const res = await streamChatCompletion({
      baseUrl: activeConfig.value.baseUrl,
      apiKey: activeConfig.value.apiKey,
      model: activeConfig.value.model,
      messages: loop,
      tools: openaiTools(),
      onDelta: (d) => { assistant.content += d },
    })

    if (res.toolCalls.length) {
      assistant.tool_calls = res.toolCalls
      loop.push(assistant)
      for (const tc of res.toolCalls) {
        let content: string
        try { content = runTool(tc.function.name, records.value, JSON.parse(tc.function.arguments)) }
        catch (e) { content = JSON.stringify({ error: String(e) }) }
        loop.push({ role: 'tool', tool_call_id: tc.id, content })
      }
      assistant = { role: 'assistant', content: '' }
    } else {
      break
    }
  }

  messages.value.push(assistant)
  saveHistory(messages.value)
  busy.value = false
}

function clearChat() { messages.value = []; clearHistory() }
```

（流式时 `assistant.content` 需响应式驱动渲染；把 `assistant` 先 push 到 `messages.value`，再在 `onDelta` 里修改其 `content`，即可边接收边渲染。）

- [ ] **Step 2: 实现模板**

聊天区（`.chat-wrap`：左 `.chat` 消息列表 + 输入框，右 `.suggest` 示例问题）+ 顶部配置下拉（`<select v-model="selectedId">`）+「清空对话」按钮。消息气泡按 `msg.role` 分左右样式（类名 `.msg.user`/`.msg.ai`，见 theme.css）。

- [ ] **Step 3: 挂载到 App.vue**

- [ ] **Step 4: 验证**

Run: `cd frontend && npm run dev`。在「大模型管理」先配置一个真实/网关地址，回到 AI 页测试流式回答、追问指代、工具调用（「华为多少人入围」应触发 `top_units`/`search_by_unit`）、清空对话、刷新后历史保留。

- [ ] **Step 5: Commit**

```bash
git add frontend/src/components/AiAssistant.vue frontend/src/App.vue
git commit -m "feat: AI 智能问数板块（流式 + 工具循环 + 历史 + 配置选择）"
```

---

### Task 17: 大模型管理板块

**Files:**
- Create: `frontend/src/components/LlmConfig.vue`
- Modify: `frontend/src/App.vue`

**Interfaces:**
- Consumes: `useLlmConfig`（Task 5）、`TOOLS`（Task 7）、`useToast`。

- [ ] **Step 1: 实现 LlmConfig.vue**

要点（模板对齐原型 `#page-model`）：
- 配置列表：`configs` 渲染为卡片/表格，每项显示名称、模型、Base URL、默认标记（`isDefault`），操作：设为默认 / 编辑 / 删除。
- 表单：显示名称、Base URL、API Key（password + 显示/隐藏切换）、模型名，保存 = `add`/`update`。
- 固定工具集面板：`TOOLS` 渲染为 `.tool-list`（名称 + 描述），只读展示。
- API Key 明文存 localStorage 提示文案。

- [ ] **Step 2: 挂载到 App.vue**

- [ ] **Step 3: 验证**：`npm run dev`，确认新增/编辑/删除/设默认、刷新后保留、默认配置被 AI 助手读取。

- [ ] **Step 4: Commit**

```bash
git add frontend/src/components/LlmConfig.vue frontend/src/App.vue
git commit -m "feat: 大模型管理板块（多配置 CRUD + 工具集展示）"
```

---

### Task 18: 集成验证与构建

**Files:**
- 无新增，仅验证。

- [ ] **Step 1: 类型检查 + 全量单测**

Run: `cd frontend && npm run typecheck`
Run: `cd frontend && npm test`
Expected: 类型检查通过；全部单测通过。

- [ ] **Step 2: 生产构建**

Run: `cd frontend && npm run build`
Expected: 构建成功，产物在 `frontend/dist/`。

- [ ] **Step 3: 预览验证**

Run: `cd frontend && npm run preview`，浏览器逐板块验证：导入 Excel → 总览 KPI/查询/增删改、年龄/单位/积分图表、AI 流式问答（真实配置下）、大模型管理、明暗切换、刷新后数据与配置保留、清空数据后的空态。

- [ ] **Step 4: Commit（如有修正）**

```bash
git add -A
git commit -m "chore: 集成验证与构建修正"
```

---

## 自检记录

- **Spec 覆盖**：数据查询/导入/增删改（Task 12）、年龄分析（13）、单位分析（14）、积分分布（15）、AI 问数（16）、大模型管理（17）、AI 流式/工具/记忆（7/8/16）、主题（9）、错误与空态（12/16 内联处理）。
- **占位符**：无 TBD/TODO；UI 任务给出关键代码与模板结构，配合 `theme.css`（Task 9 完整移植）与原型 HTML 可机械完成。
- **类型一致**：`Record`/`LlmConfig`/`ChatMessage`/`ToolCall` 定义于 Task 2，后续统一引用；`mergeRecords`/`deriveAge`（Task 3）、`stats.*`（Task 2）、`runTool`/`openaiTools`（Task 7）、`streamChatCompletion`/`accumulateToolCalls`（Task 8）、`trimHistory` 等（Task 6）签名在各任务 Interfaces 中一致。
