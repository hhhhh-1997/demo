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
  parameters: Record<string, unknown>
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
      const min = a.min ?? -Infinity, max = a.max ?? Infinity
      return { count: records.filter(r => r[a.field] >= min && r[a.field] <= max).length }
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

