import { describe, it, expect, beforeEach } from 'vitest'
import { useData } from './useData'
import type { Record } from '../types'

const KEY = 'points-settlement:records'
const r = (id: string): Record => ({ id, name: 'N', birth: '1980-01', unit: 'U', score: 100, age: 46 })

beforeEach(() => {
  localStorage.clear()
  useData().clearAll()
})

describe('useData', () => {
  it('初始为空', () => {
    const { records, isEmpty } = useData()
    expect(records.value).toEqual([])
    expect(isEmpty.value).toBe(true)
  })
  it('导入持久化到 localStorage', () => {
    const d = useData()
    d.importData([r('1')])
    expect(JSON.parse(localStorage.getItem(KEY)!)).toHaveLength(1)
  })
  it('增量导入按 id 去重', () => {
    const d = useData()
    d.importData([r('1')])
    d.importData([{ ...r('1'), score: 999 }, r('2')])
    expect(d.records.value).toHaveLength(2)
    expect(d.records.value.find(x => x.id === '1')!.score).toBe(999)
  })
  it('增删改与清空', () => {
    const d = useData()
    d.addRecord(r('1'))
    d.updateRecord('1', { score: 120 })
    expect(d.records.value[0].score).toBe(120)
    d.removeRecord('1')
    expect(d.records.value).toHaveLength(0)
    d.addRecord(r('9'))
    d.clearAll()
    expect(d.records.value).toHaveLength(0)
  })
})
