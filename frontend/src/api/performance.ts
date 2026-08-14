import axios from 'axios'
import type { ApiResult, Performance, PerformanceQuery } from '../types/performance'

export async function fetchMonthly(query: PerformanceQuery): Promise<Performance[]> {
  const { data } = await axios.get<ApiResult<Performance[]>>('/api/performance/monthly', {
    params: query,
  })
  if (data.code !== 200) {
    throw new Error(data.msg || '查询失败')
  }
  return data.data
}
