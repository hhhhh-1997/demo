import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({ baseURL: '/api', timeout: 15000 })

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

request.interceptors.response.use(
  async (res) => {
    // 文件流响应：若后端返回的是 JSON 错误信封（HTTP 200 + application/json），
    // 按业务错误处理并提示；否则透传 Blob，避免导出时把错误 JSON 当 CSV 下载。
    if (res.config.responseType === 'blob') {
      const contentType = String(res.headers['content-type'] ?? '')
      if (contentType.includes('application/json')) {
        const r = JSON.parse(await res.data.text())
        if (r.code === 401) {
          localStorage.removeItem('token')
          window.location.href = '/login'
        } else {
          ElMessage.error(r.msg)
        }
        return Promise.reject(r)
      }
      return res.data
    }
    const r = res.data
    if (r.code !== 200) {
      if (r.code === 401) {
        localStorage.removeItem('token')
        window.location.href = '/login'
      } else {
        ElMessage.error(r.msg)
      }
      return Promise.reject(r)
    }
    return r
  },
  (err) => {
    ElMessage.error(err.message)
    return Promise.reject(err)
  },
)

export default request
