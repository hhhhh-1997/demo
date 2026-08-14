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
