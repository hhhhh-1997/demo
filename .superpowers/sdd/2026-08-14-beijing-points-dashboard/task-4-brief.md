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

