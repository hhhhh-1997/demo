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

