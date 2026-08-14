import { describe, expect, it } from 'vitest'
import { fmtScore, gradeBadge } from './format'

describe('fmtScore', () => {
  it('NULL → "-"', () => {
    expect(fmtScore(null)).toBe('-')
    expect(fmtScore(undefined)).toBe('-')
  })
  it('数值 0 → "0.0"（与 NULL 语义相反）', () => {
    expect(fmtScore(0)).toBe('0.0')
  })
  it('整数与小数统一一位小数', () => {
    expect(fmtScore(80)).toBe('80.0')
    expect(fmtScore(101.3)).toBe('101.3')
  })
  it('超 100 不截断', () => {
    expect(fmtScore(120)).toBe('120.0')
  })
})

describe('gradeBadge', () => {
  it('1~5 映射中文文本与 tag type', () => {
    expect(gradeBadge(1)).toEqual({ text: '超出预期', type: 'success' })
    expect(gradeBadge(2)).toEqual({ text: '完全达标', type: 'primary' })
    expect(gradeBadge(3)).toEqual({ text: '需要提升', type: 'warning' })
    expect(gradeBadge(4)).toEqual({ text: '未达预期', type: 'danger' })
    expect(gradeBadge(5)).toEqual({ text: '无该项工作', type: 'info' })
  })
  it('NULL → "-" 兜底', () => {
    expect(gradeBadge(null)).toEqual({ text: '-', type: null })
    expect(gradeBadge(undefined)).toEqual({ text: '-', type: null })
  })
  it('越界（0 与 6）→ "-" 兜底，不回落', () => {
    expect(gradeBadge(0)).toEqual({ text: '-', type: null })
    expect(gradeBadge(6)).toEqual({ text: '-', type: null })
  })
})
