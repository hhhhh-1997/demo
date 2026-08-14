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
