import request from '../utils/request'
import type {
  PageResult,
  Result,
  RunDetailVO,
  RunQuery,
  RunVO,
  ScenarioQuery,
  ScenarioSaveDTO,
  ScenarioVO,
} from '../types'

/**
 * 自动化测试接口封装。
 *
 * @author demo
 * @since 2026-08-15
 */

/** 场景分页。 */
export function page(params: ScenarioQuery): Promise<Result<PageResult<ScenarioVO>>> {
  return request.get('/autotest/scenario/page', { params }) as Promise<Result<PageResult<ScenarioVO>>>
}

/** 场景详情（含步骤）。 */
export function detail(id: number): Promise<Result<ScenarioVO>> {
  return request.get(`/autotest/scenario/${id}`) as Promise<Result<ScenarioVO>>
}

/** 新建场景。 */
export function create(data: ScenarioSaveDTO): Promise<Result<number>> {
  return request.post('/autotest/scenario', data) as Promise<Result<number>>
}

/** 编辑场景。 */
export function update(id: number, data: ScenarioSaveDTO): Promise<Result<null>> {
  return request.put(`/autotest/scenario/${id}`, data) as Promise<Result<null>>
}

/** 删除场景。 */
export function remove(id: number): Promise<Result<null>> {
  return request.delete(`/autotest/scenario/${id}`) as Promise<Result<null>>
}

/** 触发执行。 */
export function run(id: number): Promise<Result<number>> {
  return request.post(`/autotest/scenario/${id}/run`) as Promise<Result<number>>
}

/** 运行详情。 */
export function runDetail(id: number): Promise<Result<RunDetailVO>> {
  return request.get(`/autotest/run/${id}`) as Promise<Result<RunDetailVO>>
}

/** 运行历史分页。 */
export function runPage(params: RunQuery): Promise<Result<PageResult<RunVO>>> {
  return request.get('/autotest/run/page', { params }) as Promise<Result<PageResult<RunVO>>>
}
