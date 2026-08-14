<script setup lang="ts">
import { ref, computed, watch, nextTick, reactive } from 'vue'
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

const chatBody = ref<HTMLDivElement>()

const visibleMessages = computed(() => messages.value.filter(m => m.role === 'user' || m.role === 'assistant'))

const suggestions = [
  '华为入围了多少人？',
  '平均积分和最高积分是多少？',
  '哪个年龄段的人最多？',
  '积分在 130 分以上的有多少人？',
  '许磊的积分是多少？',
]

function toolNames(msg: ChatMessage): string {
  return (msg.tool_calls ?? []).map(tc => tc.function.name).join('、')
}

function scrollToBottom() {
  nextTick(() => {
    if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight
  })
}
watch(messages, scrollToBottom, { deep: true })

async function send(text?: string) {
  const q = (text ?? input.value).trim()
  if (!q || busy.value) return
  const cfg = activeConfig.value
  if (!cfg) {
    toast('请先到「大模型管理」新增配置')
    return
  }
  input.value = ''
  messages.value.push({ role: 'user', content: q })
  busy.value = true

  const loop: ChatMessage[] = [{ role: 'system', content: SYSTEM_PROMPT }, ...messages.value]

  try {
    for (let i = 0; i < 6; i++) {
      const assistant = reactive<ChatMessage>({ role: 'assistant', content: '' })
      messages.value.push(assistant)

      const res = await streamChatCompletion({
        baseUrl: cfg.baseUrl,
        apiKey: cfg.apiKey,
        model: cfg.model,
        messages: loop,
        tools: openaiTools(),
        onDelta: (d) => { assistant.content += d },
      })

      if (res.toolCalls.length) {
        assistant.tool_calls = res.toolCalls
        loop.push(assistant)
        for (const tc of res.toolCalls) {
          let content: string
          try {
            content = runTool(tc.function.name, records.value, JSON.parse(tc.function.arguments))
          } catch (e) {
            content = JSON.stringify({ error: String(e) })
          }
          loop.push({ role: 'tool', tool_call_id: tc.id, content })
        }
      } else {
        break
      }
    }

    saveHistory(messages.value)
  } catch (e) {
    toast(e instanceof Error ? e.message : '请求失败，请检查模型配置')
  } finally {
    busy.value = false
  }
}

function clearChat() {
  messages.value = []
  clearHistory()
}
</script>

<template>
  <div class="ai-toolbar">
    <div class="ai-toolbar-left">
      <select v-model="selectedId" class="ai-config-select" aria-label="选择模型配置">
        <option value="" disabled>请选择模型配置</option>
        <option v-for="c in configs" :key="c.id" :value="c.id">{{ c.name }}</option>
      </select>
      <span v-if="configs.length === 0" class="ai-hint">尚无模型配置，请先到「大模型管理」新增</span>
    </div>
    <button class="btn ghost" @click="clearChat">清空对话</button>
  </div>

  <div class="chat-wrap">
    <div class="chat">
      <div class="chat-body" ref="chatBody">
        <div v-if="!visibleMessages.length" class="msg ai">
          <div class="bubble">你好，我是积分落户数据助手。你可以直接问我统计、排名或明细问题，例如「华为有多少人入围」「平均积分是多少」。</div>
          <div class="meta">AI 助手 · 已连接</div>
        </div>
        <div v-for="(msg, i) in visibleMessages" :key="i" class="msg" :class="msg.role === 'user' ? 'user' : 'ai'">
          <div class="bubble">
            {{ msg.content }}
            <span v-if="msg.tool_calls?.length" class="src">调用工具：{{ toolNames(msg) }}</span>
          </div>
          <div class="meta">{{ msg.role === 'user' ? '你' : 'AI 助手' }}</div>
        </div>
      </div>
      <div class="chat-input">
        <textarea
          v-model="input"
          rows="1"
          placeholder="输入问题，例如：腾讯科技入围了多少人？"
          :disabled="busy"
          @keydown.enter.exact.prevent="send()"
        ></textarea>
        <button class="send" :disabled="busy" aria-label="发送" @click="send()">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="m5 12 14-7-4 14-3-5-7-2Z"/><path d="M12 14l5 5"/></svg>
        </button>
      </div>
    </div>
    <div class="suggest">
      <h3>试试这样问</h3>
      <button v-for="s in suggestions" :key="s" class="q" @click="send(s)">{{ s }}</button>
      <div class="tip">模型地址、API Key 与工具集可在「大模型管理」中配置。回答通过工具调用读取已导入的公示名单数据。</div>
    </div>
  </div>
</template>

<style scoped>
.ai-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}
.ai-toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.ai-config-select {
  height: 36px;
  padding: 0 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: var(--bg-input);
  color: var(--text-body);
}
.ai-hint {
  font-size: var(--text-xs);
  color: var(--text-label);
}
.msg .bubble {
  white-space: pre-wrap;
  word-break: break-word;
}
.chat-input textarea:disabled {
  opacity: 0.6;
}
.chat-input .send:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
