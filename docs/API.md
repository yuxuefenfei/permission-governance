# REST API 总览

所有响应统一：`{ "code": 0, "message": "操作成功", "data": ... }`。失败使用对应 HTTP 4xx/5xx，而不是总返回 HTTP 200。

| 动作 | URI | 功能 | 权限 |
|---|---|---|---|
| GET | `/api/auth/csrf` | 初始化 CSRF cookie | 公共 |
| POST | `/api/auth/login` | Session 登录 | 公共 + CSRF |
| GET | `/api/auth/me` | 当前用户 | 登录 |
| GET | `/api/auth/grants` | 当前有效权限码 | 登录 |
| POST | `/api/auth/logout` | 注销 Session | 登录 + CSRF |
| GET | `/api/dashboard` | 工作台数字 | `system:dashboard:read` |
| GET | `/api/permissions` | 权限列表与端点 | `system:permission:read` |
| POST | `/api/permissions/sync` | 扫描同步（含标废弃） | `system:permission:sync` |
| GET | `/api/menus/my` | 当前用户可见菜单 | 登录 |
| GET/POST | `/api/menus` | 菜单管理 | `system:menu:manage` |
| PUT/DELETE | `/api/menus/{id}` | 更新/删除菜单 | `system:menu:manage` |
| GET/POST | `/api/roles` | 角色列表/创建 | `system:role:manage` |
| PUT/DELETE | `/api/roles/{id}` | 更新/删除角色 | `system:role:manage` |
| PUT | `/api/roles/{id}/permissions` | 角色授予权限码数组 | `system:role:manage` |
| GET/POST | `/api/users` | 用户列表/新增 | `system:user:manage` |
| PUT | `/api/users/{id}` | 更新启停、显示名称、角色 | `system:user:manage` |
| PUT | `/api/users/{id}/password` | 重置密码 | `system:user:manage` |
| GET | `/api/audits` | 最近 200 条审计 | `system:audit:read` |
| GET/POST | `/api/demo/devices` | 查询/新增消防设备 | READ / CREATE 对应权限 |
| GET | `/api/demo/legacy-permission-check` | 旧 `@PreAuthorize` 兼容样例 | 旧 authority 快照校验 |

## 关键请求体

`POST /api/menus`、`PUT /api/menus/{id}`:

```json
{
  "parentId": 1,
  "displayName": "消防设备",
  "routePath": "/devices",
  "icon": "FireExtinguisher",
  "menuType": "PAGE",
  "sortOrder": 10,
  "enabled": true,
  "permissionCodes": ["sop:fireSafety:deviceInfo"]
}
```

`PUT /api/roles/{id}/permissions`: `{"permissionCodes":["sop:fireSafety:deviceInfo","sop:fireSafety:deviceInfo:create"]}`。

`POST /api/users`: `{"username":"inspector","displayName":"检查员","password":"YourStrongPass123","roleIds":[1]}`。

每个管理端 API 权限码在 `backend/src/main/java/com/example/permissionhub/constant/PermissionCodes.java` 定义，通过 `@RequirePermission` 自动发现并入库，不需要在前端硬编码整个权限字典。
