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

  for (let i = 0; i < 6; i++) {
    const assistant: ChatMessage = { role: 'assistant', content: '' }
    messages.value.push(assistant)

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
    } else {
      break
    }
  }

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

