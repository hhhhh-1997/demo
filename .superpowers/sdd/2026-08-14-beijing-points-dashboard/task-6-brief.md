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

