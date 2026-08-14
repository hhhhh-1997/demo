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
