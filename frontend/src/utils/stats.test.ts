import { describe, it, expect } from 'vitest'
import {
  summarize, ageDistribution, ageMode, topUnits,
  unitSizeDistribution, totalUnits, scoreDistribution, median,
} from './stats'
import type { Record } from '../types'

const rec = (id: string, birth: string, unit: string, score: number): Record =>
  ({ id, name: 'N' + id, birth, unit, score, age: 2026 - Number(birth.slice(0, 4)) })

const records: Record[] = [
  rec('1', '1980-01', 'A公司', 130),
  rec('2', '1982-05', 'A公司', 120),
  rec('3', '1982-08', 'B公司', 140),
  rec('4', '1990-11', 'C公司', 125),
  rec('5', '1982-03', 'A公司', 121),
]

describe('median', () => {
  it('偶数个取平均', () => { expect(median([1, 2, 3, 4])).toBe(2.5) })
  it('奇数个取中间', () => { expect(median([5, 1, 3])).toBe(3) })
})

describe('summarize', () => {
  it('计算总分与积分/年龄统计', () => {
    const s = summarize(records)
    expect(s.total).toBe(5)
    expect(s.scoreMax).toBe(140)
    expect(s.scoreMin).toBe(120)
    expect(s.ageMin).toBe(36)
    expect(s.ageMax).toBe(46)
  })
})

describe('ageDistribution', () => {
  it('按年龄升序统计', () => {
    const d = ageDistribution(records)
    expect(d[0]).toEqual({ age: 36, count: 1 })
    expect(d.find(x => x.age === 44)!.count).toBe(3)
  })
})

describe('ageMode', () => {
  it('返回人数最多的年龄', () => {
    expect(ageMode(records)).toEqual({ value: 44, count: 3 })
  })
})

describe('topUnits', () => {
  it('按人数降序', () => {
    expect(topUnits(records, 2)).toEqual([
      { unit: 'A公司', count: 3 },
      { unit: 'B公司', count: 1 },
    ])
  })
})

describe('totalUnits', () => {
  it('统计去重单位数', () => { expect(totalUnits(records)).toBe(3) })
})

describe('unitSizeDistribution', () => {
  it('按规模分桶', () => {
    const d = unitSizeDistribution(records)
    expect(d[0]).toEqual({ label: '1 人', units: 2, people: 2 })
    expect(d[1]).toEqual({ label: '2–5 人', units: 1, people: 3 })
  })
})

describe('scoreDistribution', () => {
  it('每 2 分一个桶', () => {
    const d = scoreDistribution(records, 2)
    expect(d[0]).toMatchObject({ lo: 120, hi: 122, count: 2 })
  })
})
