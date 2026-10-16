import { request } from '@/utils/http'
import type {
  AuditEntry, CurrentUser, DashboardData, MenuPayload, MenuView,
  PermissionView, RolePayload, RoleView, UserView
} from '@/types'

/** API 按资源聚合，页面组件不直接拼接 URL。 */
export const authApi = {
  csrf: () => request<string>('GET', '/auth/csrf'),
  login: (username: string, password: string) => request<CurrentUser>('POST', '/auth/login', { username, password }),
  me: () => request<CurrentUser>('GET', '/auth/me'),
  grants: () => request<string[]>('GET', '/auth/grants'),
  logout: () => request<void>('POST', '/auth/logout')
}
export const permissionApi = {
  list: (params?: { keyword?: string; module?: string; status?: string }) =>
    request<PermissionView[]>('GET', '/permissions', undefined, params),
  sync: () => request<{ discovered: number; endpoints: number; deprecated: number }>('POST', '/permissions/sync')
}
export const menuApi = {
  mine: () => request<MenuView[]>('GET', '/menus/my'),
  list: () => request<MenuView[]>('GET', '/menus'),
  create: (body: MenuPayload) => request<number>('POST', '/menus', body),
  update: (id: number, body: MenuPayload) => request<void>('PUT', `/menus/${id}`, body),
  delete: (id: number) => request<void>('DELETE', `/menus/${id}`)
}
export const roleApi = {
  list: () => request<RoleView[]>('GET', '/roles'),
  create: (body: RolePayload) => request<number>('POST', '/roles', body),
  update: (id: number, body: RolePayload) => request<void>('PUT', `/roles/${id}`, body),
  grant: (id: number, permissionCodes: string[]) => request<void>('PUT', `/roles/${id}/permissions`, { permissionCodes }),
  delete: (id: number) => request<void>('DELETE', `/roles/${id}`)
}
export const userApi = {
  list: () => request<UserView[]>('GET', '/users'),
  create: (body: { username: string; displayName: string; password: string; roleIds: number[] }) =>
    request<number>('POST', '/users', body),
  update: (id: number, body: { displayName: string; enabled: boolean; roleIds: number[] }) =>
    request<void>('PUT', `/users/${id}`, body),
  resetPassword: (id: number, newPassword: string) => request<void>('PUT', `/users/${id}/password`, { newPassword })
}
export const auditApi = { list: () => request<AuditEntry[]>('GET', '/audits') }
export const dashboardApi = { overview: () => request<DashboardData>('GET', '/dashboard') }
export const deviceApi = {
  list: () => request<{ name: string; location: string }[]>('GET', '/demo/devices'),
  create: (name: string, location: string) => request<void>('POST', '/demo/devices', { name, location })
}
