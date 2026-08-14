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
