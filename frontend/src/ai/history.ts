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
