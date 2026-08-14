export interface DictOption<T> {
  value: T
  label: string
}

export const TOP_DEPTS: DictOption<number>[] = [
  { value: 10, label: '技术中心' },
  { value: 20, label: '第一事业部' },
  { value: 30, label: '第二事业部' },
  { value: 40, label: '第三事业部' },
  { value: 50, label: '第四事业部' },
  { value: 60, label: '第五事业部' },
]

export const ROLES: DictOption<string>[] = [
  { value: 'dev', label: '研发' },
  { value: 'qa', label: '测试' },
  { value: 'pm', label: '项目经理' },
  { value: 'po', label: '产品经理' },
  { value: 'td', label: '需求设计' },
  { value: 'operation', label: '运营/行政' },
  { value: 'support', label: '实施' },
  { value: 'ue', label: 'UE' },
  { value: 'wish', label: '巴长' },
  { value: 'hr', label: '人资' },
  { value: 'top', label: '高层管理' },
  { value: 'ro', label: '实施需设' },
]

export function currentMonth(): string {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  return `${y}-${m}`
}
