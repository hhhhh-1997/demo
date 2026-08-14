import request from '../utils/request'
import type {
  PageResult,
  ProjectQuery,
  ProjectVO,
  Result,
  ReviewCommand,
  ReviewDetailVO,
  ReviewStats,
} from '../types'

/**
 * 储备项目论证接口封装。
 *
 * @author demo
 * @since 2026-08-15
 */

/** 分页查询待论证项目（含论证通过 / 论证退回）。 */
export function page(params: ProjectQuery): Promise<Result<PageResult<ProjectVO>>> {
  return request.get('/review/page', { params }) as Promise<Result<PageResult<ProjectVO>>>
}

/** 查询论证详情（项目 + 最新论证记录）。 */
export function detail(id: number): Promise<Result<ReviewDetailVO>> {
  return request.get(`/review/${id}`) as Promise<Result<ReviewDetailVO>>
}

/** 执行论证（通过 / 不通过）。 */
export function execute(id: number, data: ReviewCommand): Promise<Result<null>> {
  return request.post(`/review/${id}/execute`, data) as Promise<Result<null>>
}

/** 论证统计卡片。 */
export function stats(): Promise<Result<ReviewStats>> {
  return request.get('/review/stats') as Promise<Result<ReviewStats>>
}
