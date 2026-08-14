import * as XLSX from 'xlsx'
import type { Record } from '../types'

export function deriveAge(birth: string, baseYear = 2026): number {
  const y = Number(String(birth).slice(0, 4))
  return Number.isFinite(y) ? baseYear - y : 0
}

export function rowsToRecords(rows: unknown[][]): Record[] {
  const header = rows[0]?.map(String) ?? []
  const idx = {
    id: header.indexOf('公示编号'),
    name: header.indexOf('姓名'),
    birth: header.indexOf('出生年月'),
    unit: header.indexOf('单位名称'),
    score: header.indexOf('积分分值'),
  }
  const out: Record[] = []
  for (const row of rows.slice(1)) {
    if (!row || row.every(c => c == null || c === '')) continue
    const birth = String(row[idx.birth] ?? '')
    out.push({
      id: String(row[idx.id] ?? ''),
      name: String(row[idx.name] ?? ''),
      birth,
      unit: String(row[idx.unit] ?? ''),
      score: Number(row[idx.score] ?? 0),
      age: deriveAge(birth),
    })
  }
  return out
}

export function mergeRecords(existing: Record[], incoming: Record[]): Record[] {
  const map = new Map(existing.map(r => [r.id, r]))
  for (const r of incoming) map.set(r.id, r)
  return [...map.values()]
}

export function parseWorkbook(buf: ArrayBuffer): Record[] {
  const wb = XLSX.read(buf, { type: 'array' })
  const sheet = wb.Sheets[wb.SheetNames[0]]
  const rows = XLSX.utils.sheet_to_json(sheet, { header: 1, raw: true }) as unknown[][]
  return rowsToRecords(rows)
}
