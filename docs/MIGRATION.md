# 已有 Spring Security 项目的渐进式迁移方案

## 建议顺序

**不要一次性改写数百个注解。** 先跑 `scripts/legacy_permission_inventory.py` 导出旧 `@PreAuthorize` 的疑似权限码清单，跟当前菜单表进行对比，标出：权限码无菜单、菜单权限无 API、同码多用途、超级管理员通配标识等。

**阶段一：兼容运行** 先将 `PermissionCodes`、`@RequirePermission` 和 `PermissionChecker` 接入业务项目。`@EnableMethodSecurity` 保留；旧 `@PreAuthorize` 仍走原生 Spring Security。新增代码统一使用注解。`LegacyDemoController` 保留了真实旧注解接口 `/api/demo/legacy-permission-check` 以便观察两种机制的区别，注意旧 `*:*:*` 仅是普通字符串 authority，不能当作内建通配语法。

**阶段二：引入资源扫描** 在业务服务启动后扫描 `RequestMappingHandlerMapping`；权限中心以 `(application_code, service_name, permission_code)` 业务维度接收资源快照（本 Demo 使用单库单服务，以 code 为主键）。权限注册不可向外暴露匿名写入 API，推荐内网 mTLS 与服务身份。首次只标记扫描到的新权限，不直接废弃未扫描到的旧权限。

**阶段三：菜单绑定** 菜单与权限采用多对多表 `menu_permissions`，管理端通过“按名称检索权限”配置；保留现有 `menu.permission_code` 作为迁移中读取入口，利用一次性脚本回填关联表，经双读校验无误再移除旧字段。

**阶段四：角色权限** 把授权事实统一迁到 `role_permissions`，排查旧用户特例、数据权限表达式、接口白名单及 admin authority。新注解使用实时 DB 查询；在高并发场景优化为 `Caffeine/Redis + 版本事件`，保证撤权能够及时生效。旧注解需要重新登录才能更新快照；迁移完成后再逐步改成新的实时鉴权。

**阶段五：全量调和** 只有权限定义完整且可信的服务版本上线后，才运行“标记未出现权限为废弃”。灰度/多实例场景必须按版本做快照，不能把一台旧实例的权限集当全局真相。

## 一次性检查清单

- [ ] 代码权限码的命名、中文名称、模块、类别统一
- [ ] 多 API 共享权限码时元数据相同，业务含义一致
- [ ] 被保护接口与菜单权限明确区分，前端按钮不代替后端校验
- [ ] 普通用户、超级管理员、停用用户分别有负向用例
- [ ] 旧注解权限不因本次改造失效
- [ ] 各角色权限重载后既有菜单与用户授权保持不变
- [ ] 权限删除只软废弃，并保留审计及历史引用
- [ ] 用户被禁用、角色被撤权时，新注解的授权立即收敛
- [ ] 前端视图的 RoutePath 与 Vue Router 白名单一一对应
- [ ] CI、回滚、数据库迁移、发布审批与内部同步认证完成

## 为什么不只使用枚举？

枚举能把 `code/name` 收拢，但管理员仍然得从代码或配置手工导入权限中心，并不知道一个 code 实际保护哪些 URL。注解 + 扫描 + 资源池 + 绑定形成可追溯的自动链路；枚举适用于 `PermissionKind` 这样的固定有限集合，权限码数量多且跨服务时推荐常量+生成清单。
