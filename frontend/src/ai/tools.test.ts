import { describe, it, expect } from 'vitest'
import { runTool } from './tools'
import type { Record } from '../types'

const rec = (id: string, name: string, unit: string, score: number, birth = '1980-01'): Record =>
  ({ id, name, birth, unit, score, age: 2026 - Number(birth.slice(0, 4)) })

const records: Record[] = [
  rec('1', '许磊', '华为', 145.29),
  rec('2', '张三', '腾讯', 130),
  rec('3', '李四', '华为', 128),
]

describe('runTool', () => {
  it('top_units 返回排名', () => {
    const out = JSON.parse(runTool('top_units', records, { n: 2 }))
    expect(out[0]).toEqual({ unit: '华为', count: 2 })
  })
  it('search_by_name 返回命中', () => {
    const out = JSON.parse(runTool('search_by_name', records, { name: '许磊' }))
    expect(out[0].id).toBe('1')
  })
  it('score_stats 返回汇总', () => {
    const out = JSON.parse(runTool('score_stats', records, {}))
    expect(out.scoreMax).toBe(145.29)
  })
  it('未知工具抛错', () => {
    expect(() => runTool('nope', records, {})).toThrow()
  })
})
