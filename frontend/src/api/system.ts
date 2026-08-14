import request from '../utils/request'
import type {
  PageResult,
  Result,
  RoleDTO,
  SysDept,
  SysDictData,
  SysMenu,
  SysMenuSaveDTO,
  SysRole,
  SysUserDTO,
  SysUserVO,
} from '../types'

/**
 * 系统管理接口封装（用户 / 角色 / 菜单 / 部门 / 字典）。
 *
 * @author demo
 * @since 2026-08-15
 */

/** 用户分页查询条件。 */
export interface UserPageQuery {
  pageNum: number
  pageSize: number
  username?: string
}

/** 分页查询用户。 */
export function userPage(params: UserPageQuery): Promise<Result<PageResult<SysUserVO>>> {
  return request.get('/system/user/page', { params }) as Promise<Result<PageResult<SysUserVO>>>
}

/** 新增用户。 */
export function createUser(data: SysUserDTO): Promise<Result<null>> {
  return request.post('/system/user', data) as Promise<Result<null>>
}

/** 编辑用户。 */
export function updateUser(id: number, data: SysUserDTO): Promise<Result<null>> {
  return request.put(`/system/user/${id}`, data) as Promise<Result<null>>
}

/** 删除用户。 */
export function removeUser(id: number): Promise<Result<null>> {
  return request.delete(`/system/user/${id}`) as Promise<Result<null>>
}

/** 查询全部角色。 */
export function roleList(): Promise<Result<SysRole[]>> {
  return request.get('/system/role/list') as Promise<Result<SysRole[]>>
}

/** 新增角色。 */
export function createRole(data: RoleDTO): Promise<Result<null>> {
  return request.post('/system/role', data) as Promise<Result<null>>
}

/** 编辑角色。 */
export function updateRole(id: number, data: RoleDTO): Promise<Result<null>> {
  return request.put(`/system/role/${id}`, data) as Promise<Result<null>>
}

/** 删除角色。 */
export function removeRole(id: number): Promise<Result<null>> {
  return request.delete(`/system/role/${id}`) as Promise<Result<null>>
}

/** 查询菜单树。 */
export function menuList(): Promise<Result<SysMenu[]>> {
  return request.get('/system/menu/list') as Promise<Result<SysMenu[]>>
}

/** 新增菜单。 */
export function createMenu(data: SysMenuSaveDTO): Promise<Result<null>> {
  return request.post('/system/menu', data) as Promise<Result<null>>
}

/** 编辑菜单。 */
export function updateMenu(id: number, data: SysMenuSaveDTO): Promise<Result<null>> {
  return request.put(`/system/menu/${id}`, data) as Promise<Result<null>>
}

/** 删除菜单。 */
export function removeMenu(id: number): Promise<Result<null>> {
  return request.delete(`/system/menu/${id}`) as Promise<Result<null>>
}

/** 查询部门树。 */
export function deptTree(): Promise<Result<SysDept[]>> {
  return request.get('/system/dept/tree') as Promise<Result<SysDept[]>>
}

/** 查询指定类型字典。 */
export function dict(type: string): Promise<Result<SysDictData[]>> {
  return request.get(`/system/dict/${type}`) as Promise<Result<SysDictData[]>>
}
