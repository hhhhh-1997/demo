import request from '../utils/request'
import type { LoginUser, Result } from '../types'

/** 登录请求体。 */
export interface LoginParams {
  username: string
  password: string
}

/** 登录，返回统一响应信封。 */
export function login(data: LoginParams): Promise<Result<LoginUser>> {
  return request.post('/auth/login', data) as Promise<Result<LoginUser>>
}

/** 退出登录。 */
export function logout(): Promise<Result<null>> {
  return request.post('/auth/logout') as Promise<Result<null>>
}

/** 获取当前登录用户信息。 */
export function getMe(): Promise<Result<LoginUser>> {
  return request.get('/auth/me') as Promise<Result<LoginUser>>
}
