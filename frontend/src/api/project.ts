import request from '../utils/request'
import type {
  PageResult,
  ProjectQuery,
  ProjectSaveDTO,
  ProjectStats,
  ProjectVO,
  Result,
  SysDept,
  SysDictData,
} from '../types'

/**
 * 储备项目维护接口封装。
 *
 * @author demo
 * @since 2026-08-15
 */

/** 分页查询储备项目。 */
export function page(params: ProjectQuery): Promise<Result<PageResult<ProjectVO>>> {
  return request.get('/project/page', { params }) as Promise<Result<PageResult<ProjectVO>>>
}

/** 查询储备项目详情。 */
export function detail(id: number): Promise<Result<ProjectVO>> {
  return request.get(`/project/${id}`) as Promise<Result<ProjectVO>>
}

/** 新增储备项目。 */
export function create(data: ProjectSaveDTO): Promise<Result<number>> {
  return request.post('/project', data) as Promise<Result<number>>
}

/** 编辑储备项目。 */
export function update(id: number, data: ProjectSaveDTO): Promise<Result<null>> {
  return request.put(`/project/${id}`, data) as Promise<Result<null>>
}

/** 删除储备项目。 */
export function remove(id: number): Promise<Result<null>> {
  return request.delete(`/project/${id}`) as Promise<Result<null>>
}

/** 提报储备项目。 */
export function submit(id: number): Promise<Result<null>> {
  return request.post(`/project/submit/${id}`) as Promise<Result<null>>
}

/** 批量提报储备项目。 */
export function submitBatch(ids: number[]): Promise<Result<null>> {
  return request.post('/project/submit/batch', ids) as Promise<Result<null>>
}

/** 储备项目维护统计。 */
export function stats(): Promise<Result<ProjectStats>> {
  return request.get('/project/stats') as Promise<Result<ProjectStats>>
}

/** 项目分类字典。 */
export function projectTypeDict(): Promise<Result<SysDictData[]>> {
  return request.get('/system/dict/project_type') as Promise<Result<SysDictData[]>>
}

/** 部门树。 */
export function deptTree(): Promise<Result<SysDept[]>> {
  return request.get('/system/dept/tree') as Promise<Result<SysDept[]>>
}

/**
 * 导出储备项目 CSV 并触发浏览器下载。
 *
 * @param params 查询条件
 */
export async function exportCsv(params: ProjectQuery): Promise<void> {
  const res = (await request.get('/project/export', {
    params,
    responseType: 'blob',
  })) as unknown as Blob
  const url = URL.createObjectURL(res)
  const link = document.createElement('a')
  link.href = url
  link.download = `储备项目_${formatDate(new Date())}.csv`
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
