export type TagType = 'success' | 'primary' | 'warning' | 'danger' | 'info'

const GRADE_TEXT: Record<number, string> = {
  1: '超出预期',
  2: '完全达标',
  3: '需要提升',
  4: '未达预期',
  5: '无该项工作',
}

// 原型 badge 色板 → el-tag type（1 最优、4 最差，方向与数值相反）
const GRADE_TAG: Record<number, TagType> = {
  1: 'success',
  2: 'primary',
  3: 'warning',
  4: 'danger',
  5: 'info',
}

export interface GradeBadge {
  text: string
  type: TagType | null
}

/**
 * 分值项：NULL = 「无该项工作」→ '-'；数值（含 0）→ 保留一位小数。
 * 0 → '0.0'（与 NULL 语义相反，不得归并）；超 100 按原值不截断。
 */
export function fmtScore(v: number | null | undefined): string {
  if (v === null || v === undefined) return '-'
  return v.toFixed(1)
}

/**
 * 等级项：等级码 → 中文文本 + 徽标 type；NULL 或越界 → '-'（不报错、不回落）。
 */
export function gradeBadge(code: number | null | undefined): GradeBadge {
  if (code === null || code === undefined) return { text: '-', type: null }
  const text = GRADE_TEXT[code]
  if (!text) return { text: '-', type: null }
  return { text, type: GRADE_TAG[code] }
}
