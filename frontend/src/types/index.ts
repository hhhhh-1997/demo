/** 统一响应体。 */
export interface Result<T> {
  code: number
  msg: string
  data: T
}

/** 分页响应体。 */
export interface PageResult<T> {
  total: number
  list: T[]
}

/** 项目状态（7 态，存库值为中文）。 */
export type ProjectStatus =
  | '草稿'
  | '待论证'
  | '论证退回'
  | '待审核'
  | '审核退回'
  | '待下达'
  | '已下达'

/** 项目展示对象。 */
export interface ProjectVO {
  id: number
  projectCode: string
  projectName: string
  projectType: string
  investmentAmount: number
  deptId: number
  deptName: string
  description: string
  status: ProjectStatus
  createTime: string
  issueTime: string
}

/** 项目维护页统计卡片。 */
export interface ProjectStats {
  total: number
  draft: number
  reviewRejected: number
  auditRejected: number
}

/** 名称-数值统计项（用于图表聚合）。 */
export interface NameValue {
  name: string
  value: number
}

/** 项目分页/导出查询条件。 */
export interface ProjectQuery {
  pageNum?: number
  pageSize?: number
  projectType?: string
  deptId?: number
  status?: string
  name?: string
}

/** 项目新增/编辑请求体。 */
export interface ProjectSaveDTO {
  projectName: string
  projectType: string
  investmentAmount: number
  deptId: number
  description?: string
  planStartDate?: string
  planEndDate?: string
  validUntil?: string
}

/** 论证记录展示对象。 */
export interface ReviewRecordVO {
  infoComplete: string
  threeImportant: string
  splitProject: string
  interfaceConfusion: string
  opinion: string
  result: string
  reviewTime: string
}

/** 论证执行请求体。 */
export interface ReviewCommand {
  infoComplete?: string
  threeImportant?: string
  splitProject?: string
  interfaceConfusion?: string
  opinion?: string
  pass: boolean
}

/** 论证详情展示对象。 */
export interface ReviewDetailVO {
  project: ProjectVO
  review: ReviewRecordVO | null
}

/** 论证统计卡片（passRate 已为百分比数值）。 */
export interface ReviewStats {
  pending: number
  passed: number
  rejected: number
  passRate: number
}

/** 审核记录展示对象。 */
export interface AuditRecordVO {
  opinion: string
  result: string
  auditTime: string
}

/** 审核执行请求体。 */
export interface AuditCommand {
  opinion?: string
  pass: boolean
}

/** 批量审核请求体。 */
export interface BatchAuditCommand {
  ids: number[]
  pass: boolean
}

/** 审核详情展示对象。 */
export interface AuditDetailVO {
  project: ProjectVO
  audit: AuditRecordVO | null
  reviewResult: string | null
  reviewOpinion: string | null
}

/** 审核统计卡片（passRate 已为百分比数值）。 */
export interface AuditStats {
  pending: number
  passed: number
  rejected: number
  passRate: number
}

/** 统一储备库详情展示对象。 */
export interface ReserveDetailVO {
  project: ProjectVO
  review: ReviewRecordVO | null
  audit: AuditRecordVO | null
}

/** 统一储备库统计展示对象。 */
export interface ReserveStatsVO {
  total: number
  totalAmountYuan: number
  pending: number
  issued: number
  categoryDist: NameValue[]
  deptDist: NameValue[]
}

/** 登录成功返回体。 */
export interface LoginUser {
  token: string
  userId: number
  username: string
  nickname: string
  roles: string[]
  permissions: string[]
}

/** 用户列表返回体（不含密码）。 */
export interface SysUserVO {
  id: number
  username: string
  nickname: string
  deptId: number
  status: number
  createTime: string
}

/** 菜单类型：M 目录 / C 菜单 / F 按钮。 */
export type MenuType = 'M' | 'C' | 'F'

/** 系统菜单树节点。 */
export interface SysMenu {
  id: number
  menuName: string
  parentId: number
  path: string | null
  component: string | null
  perms: string | null
  menuType: MenuType
  icon: string | null
  sort: number
  visible: number
  status: number
  children: SysMenu[] | null
}

/** 字典数据项。 */
export interface SysDictData {
  id: number
  dictType: string
  dictLabel: string
  dictValue: string
  sort: number
  status: number
}

/** 部门树节点。 */
export interface SysDept {
  id: number
  deptName: string
  parentId: number
  sort: number
  status: number
  children: SysDept[] | null
}
