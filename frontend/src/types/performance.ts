export interface Performance {
  id: number
  userId: number
  realname: string
  month: string
  taskFinishRate: number
  workEffectRate: number
  workNormativity: number
  learningImprovement: number | null
  softwareDesign: number | null
  preSalesSupport: number | null
  bugCondition: number | null
  systemDesign: number | null
  codeReview: number | null
  testQuality: number | null
  dept: number
  role: string
  roleName: string
  deptName: string
  topDeptId: number
  topDeptName: string
}

export interface PerformanceQuery {
  topDeptId?: number
  role?: string
  month?: string
}

export interface ApiResult<T> {
  code: number
  msg: string
  data: T
}
