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

