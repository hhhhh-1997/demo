import request from '../utils/request'
import type {
  AuditCommand,
  AuditDetailVO,
  AuditStats,
  BatchAuditCommand,
  PageResult,
  ProjectQuery,
  ProjectVO,
  Result,
} from '../types'

/**
 * 储备项目审核接口封装。
 *
 * @author demo
 * @since 2026-08-15
 */

/** 分页查询待审核项目（含审核通过 / 审核退回）。 */
export function page(params: ProjectQuery): Promise<Result<PageResult<ProjectVO>>> {
  return request.get('/audit/page', { params }) as Promise<Result<PageResult<ProjectVO>>>
}

/** 查询审核详情（项目 + 最新审核记录）。 */
export function detail(id: number): Promise<Result<AuditDetailVO>> {
  return request.get(`/audit/${id}`) as Promise<Result<AuditDetailVO>>
}

/** 执行审核（通过 / 退回）。 */
export function execute(id: number, data: AuditCommand): Promise<Result<null>> {
  return request.post(`/audit/${id}/execute`, data) as Promise<Result<null>>
}

/** 批量审核（通过 / 退回）。 */
export function batch(data: BatchAuditCommand): Promise<Result<null>> {
  return request.post('/audit/batch', data) as Promise<Result<null>>
}

/** 审核统计卡片。 */
export function stats(): Promise<Result<AuditStats>> {
  return request.get('/audit/stats') as Promise<Result<AuditStats>>
}
