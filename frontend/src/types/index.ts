/** 前后端传输类型与 Java record/DTO 字段保持一致。 */
export type PermissionKind = 'PAGE' | 'ACTION' | 'ADMIN'
export type PermissionStatus = 'ACTIVE' | 'DEPRECATED'
export type MenuType = 'DIRECTORY' | 'PAGE'
export interface ApiResponse<T> { code: number; message: string; data: T }
export interface CurrentUser { username: string; displayName: string; superAdmin: boolean }
export interface EndpointView { httpMethod: string; urlPath: string; handler: string }
export interface PermissionView {
  code: string; displayName: string; moduleName: string; kind: PermissionKind
  status: PermissionStatus; description: string; serviceName: string
  lastSeenAt: string; endpoints: EndpointView[]
}
export interface MenuView {
  id: number; parentId: number | null; displayName: string; routePath: string
  icon: string; menuType: MenuType; sortOrder: number; enabled: boolean
  permissionCodes: string[]
}
export type MenuPayload = Omit<MenuView, 'id'>
export interface RoleView {
  id: number; roleCode: string; displayName: string; description: string
  builtIn: boolean; permissionCodes: string[]
}
export type RolePayload = Pick<RoleView, 'roleCode' | 'displayName' | 'description'>
export interface UserView {
  id: number; username: string; displayName: string
  enabled: boolean; superAdmin: boolean; roleIds: number[]
}
export interface AuditEntry {
  id: number; actor: string; action: string; resourceType: string
  resourceId: string; detail: string; createdAt: string
}
export interface DashboardData {
  permissionCount: number; deprecatedCount: number; roleCount: number
  userCount: number; menuCount: number
}
