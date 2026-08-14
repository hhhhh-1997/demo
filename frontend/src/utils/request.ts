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
  (res) => {
    // 文件流响应直接透传，避免按 Result 信封误判为业务错误。
    if (res.config.responseType === 'blob') {
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
