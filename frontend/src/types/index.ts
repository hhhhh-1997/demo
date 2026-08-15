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
  /** 已分配的角色 ID（列表接口回填，用于编辑回显）。 */
  roleIds?: number[]
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

/** 系统角色。 */
export interface SysRole {
  id: number
  roleName: string
  roleKey: string
  sort: number
  status: number
  createTime?: string
  /** 已分配的菜单 ID（列表接口回填，用于权限树回显）。 */
  menuIds?: number[]
}

/** 用户新增/编辑请求体。 */
export interface SysUserDTO {
  username: string
  password?: string
  nickname?: string
  deptId?: number
  status?: number
  roleIds?: number[]
}

/** 角色新增/编辑请求体。 */
export interface RoleDTO {
  roleName: string
  roleKey: string
  sort?: number
  status?: number
  menuIds?: number[]
}

/** 菜单新增/编辑请求体。 */
export interface SysMenuSaveDTO {
  menuName: string
  menuType: MenuType
  parentId?: number
  path?: string
  component?: string
  perms?: string
  icon?: string
  sort?: number
  visible?: number
  status?: number
}

/** 断言操作类型。 */
export type AssertOp = 'EQUALS' | 'CONTAINS' | 'EXISTS'

/** 断言类型。 */
export type AssertType = 'STATUS' | 'JSON'

/** 单个断言。 */
export interface AssertItem {
  type: AssertType
  expected?: string
  jsonPath?: string
  op?: AssertOp
}

/** 变量提取。 */
export interface ExtractItem {
  name: string
  jsonPath: string
}

/** 步骤（编排器与后端共用的扁平模型）。 */
export interface StepItem {
  id?: number
  stepOrder?: number
  name: string
  method: string
  path: string
  headers: Record<string, string>
  query: Record<string, string>
  body: Record<string, unknown>
  asserts: AssertItem[]
  extracts: ExtractItem[]
}

/** 场景展示对象。 */
export interface ScenarioVO {
  id: number
  name: string
  description: string
  baseUrl: string
  variables: Record<string, unknown>
  createTime: string
  steps: StepItem[] | null
}

/** 场景保存请求体。 */
export interface ScenarioSaveDTO {
  name: string
  description?: string
  baseUrl?: string
  variables: Record<string, unknown>
  steps: StepItem[]
}

/** 场景分页查询条件。 */
export interface ScenarioQuery {
  pageNum?: number
  pageSize?: number
  name?: string
}

/** 运行状态：0 运行中 / 1 成功 / 2 失败。 */
export type RunStatus = 0 | 1 | 2

/** 步骤结果状态：0 通过 / 1 失败 / 2 跳过。 */
export type StepStatus = 0 | 1 | 2

/** 运行记录展示对象。 */
export interface RunVO {
  id: number
  scenarioId: number
  status: RunStatus
  failStepId: number | null
  errorMsg: string | null
  startTime: string
  endTime: string | null
  triggerBy: number
  createTime: string
}

/** 运行分页查询条件。 */
export interface RunQuery {
  pageNum?: number
  pageSize?: number
  scenarioId?: number
  status?: RunStatus
}

/** 步骤结果展示对象。 */
export interface StepResultVO {
  id: number
  stepId: number
  stepOrder: number
  name: string
  status: StepStatus
  requestSnapshot: string
  responseSnapshot: string
  assertDetail: string | null
  errorMsg: string | null
  startTime: string
  endTime: string | null
}

/** 运行详情展示对象。 */
export interface RunDetailVO {
  id: number
  scenarioId: number
  status: RunStatus
  failStepId: number | null
  errorMsg: string | null
  startTime: string
  endTime: string | null
  steps: StepResultVO[]
}
