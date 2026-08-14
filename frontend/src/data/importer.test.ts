import { describe, it, expect } from 'vitest'
import { deriveAge, rowsToRecords, mergeRecords } from './importer'
import type { Record } from '../types'

describe('deriveAge', () => {
  it('用基准年减出生年', () => {
    expect(deriveAge('1982-08', 2026)).toBe(44)
    expect(deriveAge('1969-11', 2026)).toBe(57)
  })
})

describe('rowsToRecords', () => {
  const header = ['公示编号', '姓名', '出生年月', '单位名称', '积分分值']
  it('映射表头并派生年龄', () => {
    const rows = [header, ['202600001', '许磊', '1982-08', '某公司', 145.29]]
    const out = rowsToRecords(rows)
    expect(out[0]).toEqual({ id: '202600001', name: '许磊', birth: '1982-08', unit: '某公司', score: 145.29, age: 44 })
  })
  it('积分按字符串也解析为数字', () => {
    const rows = [header, ['202600002', '某人', '1980-01', '某公司', '142.34']]
    expect(rowsToRecords(rows)[0].score).toBe(142.34)
  })
})

describe('mergeRecords', () => {
  const a: Record = { id: '1', name: '甲', birth: '1980-01', unit: 'A', score: 130, age: 46 }
  it('存在则更新', () => {
    const b: Record = { ...a, score: 135 }
    expect(mergeRecords([a], [b])).toEqual([b])
  })
  it('不存在则新增，保持现有顺序', () => {
    const b: Record = { id: '2', name: '乙', birth: '1980-02', unit: 'B', score: 120, age: 46 }
    expect(mergeRecords([a], [b]).map(r => r.id)).toEqual(['1', '2'])
  })
})
