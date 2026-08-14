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
  const url = resolveUrl(opts.baseUrl)
  console.info(`[AI] POST ${url} | model=${opts.model} | messages=${opts.messages.length} | tools=${opts.tools.length}`)

  const res = await fetch(url, {
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

  if (!res.ok) {
    let bodyText = ''
    try { bodyText = await res.text() } catch { /* ignore */ }
    console.error(`[AI] 请求失败 ${res.status}`, bodyText)
    const excerpt = bodyText.slice(0, 300)
    throw new Error(`请求失败: ${res.status}${excerpt ? ' — ' + excerpt : ''}`)
  }
  if (!res.body) throw new Error('响应无 body')

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
        if (delta?.tool_calls) {
          toolDeltas.push(...delta.tool_calls)
          console.debug('[AI] tool_calls delta:', delta.tool_calls)
        }
      } catch (e) {
        console.warn('[AI] SSE 行解析失败:', trimmed, e)
      }
    }
  }

  const toolCalls = accumulateToolCalls([], toolDeltas)
  console.info(`[AI] 完成 | content=${content.length} chars | tool_calls=${toolCalls.length}`)
  if (toolCalls.length) console.debug('[AI] 累积 tool_calls:', toolCalls)
  return { content, toolCalls }
}
