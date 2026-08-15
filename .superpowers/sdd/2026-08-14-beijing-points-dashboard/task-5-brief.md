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

