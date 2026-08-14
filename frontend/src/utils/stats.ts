import type { Record } from '../types'

export interface KpiSummary {
  total: number
  scoreMin: number
  scoreMax: number
  scoreAvg: number
  scoreMedian: number
  ageMin: number
  ageMax: number
  ageAvg: number
  ageMedian: number
}

export interface AgeBucket { age: number; count: number }
export interface UnitBucket { unit: string; count: number }
export interface UnitSizeBucket { label: string; units: number; people: number }
export interface ScoreBucket { label: string; lo: number; hi: number; count: number }

export function median(values: number[]): number {
  const sorted = [...values].sort((a, b) => a - b)
  const mid = Math.floor(sorted.length / 2)
  return sorted.length % 2 ? sorted[mid] : (sorted[mid - 1] + sorted[mid]) / 2
}

export function summarize(records: Record[]): KpiSummary {
  const scores = records.map(r => r.score)
  const ages = records.map(r => r.age)
  const total = records.length
  const avg = (xs: number[]) => (total ? xs.reduce((a, b) => a + b, 0) / total : 0)
  return {
    total,
    scoreMin: Math.min(...scores),
    scoreMax: Math.max(...scores),
    scoreAvg: avg(scores),
    scoreMedian: median(scores),
    ageMin: Math.min(...ages),
    ageMax: Math.max(...ages),
    ageAvg: avg(ages),
    ageMedian: median(ages),
  }
}

export function ageDistribution(records: Record[]): AgeBucket[] {
  const map = new Map<number, number>()
  for (const r of records) map.set(r.age, (map.get(r.age) ?? 0) + 1)
  return [...map.entries()].map(([age, count]) => ({ age, count })).sort((a, b) => a.age - b.age)
}

export function ageMode(records: Record[]): { value: number; count: number } {
  let best = { value: 0, count: 0 }
  for (const b of ageDistribution(records)) {
    if (b.count > best.count) best = { value: b.age, count: b.count }
  }
  return best
}

export function topUnits(records: Record[], n: number): UnitBucket[] {
  const map = new Map<string, number>()
  for (const r of records) map.set(r.unit, (map.get(r.unit) ?? 0) + 1)
  return [...map.entries()]
    .map(([unit, count]) => ({ unit, count }))
    .sort((a, b) => b.count - a.count)
    .slice(0, n)
}

export function totalUnits(records: Record[]): number {
  return new Set(records.map(r => r.unit)).size
}

export function unitSizeDistribution(records: Record[]): UnitSizeBucket[] {
  const map = new Map<string, number>()
  for (const r of records) map.set(r.unit, (map.get(r.unit) ?? 0) + 1)
  const buckets: UnitSizeBucket[] = [
    { label: '1 人', units: 0, people: 0 },
    { label: '2–5 人', units: 0, people: 0 },
    { label: '6–10 人', units: 0, people: 0 },
    { label: '11–20 人', units: 0, people: 0 },
    { label: '20+ 人', units: 0, people: 0 },
  ]
  const idx = (c: number) => (c <= 1 ? 0 : c <= 5 ? 1 : c <= 10 ? 2 : c <= 20 ? 3 : 4)
  for (const count of map.values()) {
    buckets[idx(count)].units += 1
    buckets[idx(count)].people += count
  }
  return buckets
}

export function scoreDistribution(records: Record[], binSize = 2): ScoreBucket[] {
  if (!records.length) return []
  const lo0 = Math.floor(Math.min(...records.map(r => r.score)) / binSize) * binSize
  const hi0 = Math.ceil(Math.max(...records.map(r => r.score)) / binSize) * binSize
  const buckets: ScoreBucket[] = []
  for (let lo = lo0; lo < hi0; lo += binSize) {
    const hi = lo + binSize
    buckets.push({
      label: `${lo}–${hi}`,
      lo, hi,
      count: records.filter(r => r.score >= lo && r.score < hi || (hi === hi0 && r.score === hi)).length,
    })
  }
  return buckets
}
