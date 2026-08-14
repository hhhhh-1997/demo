import request from '../utils/request'
import type {
  PageResult,
  ProjectQuery,
  ProjectVO,
  ReserveDetailVO,
  ReserveStatsVO,
  Result,
} from '../types'

/**
 * 统一储备库接口封装。
 *
 * @author demo
 * @since 2026-08-15
 */

/** 分页查询统一储备库（待下达 / 已下达）。 */
export function page(params: ProjectQuery): Promise<Result<PageResult<ProjectVO>>> {
  return request.get('/reserve/page', { params }) as Promise<Result<PageResult<ProjectVO>>>
}

/** 查询储备项目详情（项目 + 最新论证 + 最新审核记录）。 */
export function detail(id: number): Promise<Result<ReserveDetailVO>> {
  return request.get(`/reserve/${id}`) as Promise<Result<ReserveDetailVO>>
}

/** 下达储备项目。 */
export function issue(id: number): Promise<Result<null>> {
  return request.post(`/reserve/${id}/issue`) as Promise<Result<null>>
}

/** 统一储备库统计（卡片 + 图表聚合）。 */
export function stats(): Promise<Result<ReserveStatsVO>> {
  return request.get('/reserve/stats') as Promise<Result<ReserveStatsVO>>
}

/**
 * 导出统一储备库 CSV 并触发浏览器下载。
 *
 * @param params 查询条件
 */
export async function exportCsv(params: ProjectQuery): Promise<void> {
  const res = (await request.get('/reserve/export', {
    params,
    responseType: 'blob',
  })) as unknown as Blob
  const url = URL.createObjectURL(res)
  const link = document.createElement('a')
  link.href = url
  link.download = `统一储备库_${formatDate(new Date())}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

/** 日期格式化为 yyyyMMdd。 */
function formatDate(date: Date): string {
  const yyyy = date.getFullYear()
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  return `${yyyy}${mm}${dd}`
}
