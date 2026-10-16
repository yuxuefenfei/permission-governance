# 手工功能验收清单

## 登录 / Session / 安全

- [ ] 访问 `http://localhost:5173` 会跳转到登录页
- [ ] 错误密码展示 401，不暴露具体账号存在性
- [ ] 管理员登录可看到工作台、权限治理和业务页
- [ ] 查看员登录仅显示消防安全/消防设备菜单，无管理员入口
- [ ] 注销后再次访问后台页面被重定向到登录
- [ ] 直接调用非 GET API，缺失正确 CSRF token 返回 403

## 资源扫描 / 注册

- [ ] 权限资源池按编码、中文名称、模块、状态可筛选
- [ ] 详情有 HTTP Method、URL、handler 信息
- [ ] 连续同步 2 次不会出现重复资源或丢失已有角色授权
- [ ] 删除一个注解并点击同步后，该 code 成为 DEPRECATED，原菜单/角色绑定仍保留
- [ ] 相同 code、不同 name/module/kind/description 的冲突会导致同步回滚

## 菜单 / 角色 / 用户

- [ ] 菜单新增/修改/删除可用；父菜单存在子节点时删除失败
- [ ] 页面只能在权限池中选权限，目录不能绑定权限
- [ ] 子菜单不能反过来设置为自己的祖先
- [ ] 新建角色、改名、删除、编辑权限集合正常；内置角色不可删
- [ ] 创建用户、修改用户、分配角色、停用用户、重置密码可用
- [ ] 超级管理员不能通过普通后台页面被降权或禁用

## 业务权限闭环

- [ ] `operator` 可调用 GET `/api/demo/devices`
- [ ] `operator` 无新增权限时按钮隐藏；即便手工 POST 也返回 403
- [ ] `admin` 可 POST 新增设备
- [ ] 给 `ROLE_OPERATOR` 授予 `sop:fireSafety:deviceInfo:create`，不重新登录即可访问 POST
- [ ] 撤销该权限后，不重新登录再次 POST 返回 403
- [ ] 审计日志可见角色授权变化（GRANT）记录，密码不出现在 detail 中

## 推荐验证顺序

1. `cd backend && mvn test`
2. `cd frontend && npm run typecheck && npm run build`
3. 用两个无痕浏览器窗口分别登录 admin 和 operator，比对菜单与访问控制
4. 打开 DevTools → Network，确认写操作携带 X-XSRF-TOKEN，Session Cookie 是 HttpOnly
5. 重启服务确认 H2 文件库保存业务授权；核对数据未重置
