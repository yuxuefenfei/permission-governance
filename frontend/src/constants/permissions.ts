/** 仅用于前端按钮显隐；真正访问控制始终由 Spring Security 执行。 */
export const PERMISSION_CODES = {
  DEVICE_READ: 'sop:fireSafety:deviceInfo',
  DEVICE_CREATE: 'sop:fireSafety:deviceInfo:create',
  PERMISSION_SYNC: 'system:permission:sync'
} as const
