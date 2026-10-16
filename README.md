# Permission Hub · 统一权限治理示例（Spring Boot + MyBatis-Flex + Vue 3 + TypeScript）

> 面向现有系统中大量 `@PreAuthorize("hasAnyAuthority('sop:fireSafety:deviceInfo','*:*:*')")`、需要人工翻源码拷贝权限码的问题，提供一套**可学习、可本地启动、前后端打通的最小完整权限治理项目**。

## 一、交付内容

- **代码端**：`PermissionCodes` 常量集中声明；带中文业务元数据的 `@RequirePermission`；Spring Security 方法级鉴权；旧 `@PreAuthorize` 可直接运行的兼容样例。
- **扫描端**：读取 `RequestMappingHandlerMapping` 实际路由，自动同步 `code/name/module/kind/HTTP Method/URL/handler`；元数据冲突拒绝入库；软废弃而不是直接删除。
- **管理端**：登录/退出、概览、权限资源筛选/端点详情/重新同步、菜单树 CRUD/绑定权限、角色 CRUD/授权、用户新增/编辑/启停/关联角色/重置密码、审计查询、消防设备查询/新增示例。
- **数据库**：无需安装 MySQL 的 H2 文件库，另附生产迁移用 MySQL DDL。
- **代码质量**：按技术职责统一分包、使用 DTO / enum / constants / Lombok、中文注释、Vue SFC `<script setup lang="ts">`、类型化 API、Pinia、路由保护、后端参数校验和全局异常处理。

## 二、运行环境

- JDK **17+**（推荐 21）
- Maven **3.9+**
- Node.js **20.19+** 或 **22.12+**，npm

Windows PowerShell 中，请先在当前终端选择 JDK 17 或 21，再执行 Maven 命令。下面使用本机已安装的 JDK 17 路径，其他机器按实际安装目录调整；只影响当前终端，不修改系统环境变量。

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17.0.7'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
mvn -version
```

> 本地数据目录：运行后端时的 `backend/data/permission_demo.mv.db`；删掉该文件会重置演示数据。不要删除正在使用的数据库文件。

### 1. 启动后端

```bash
cd backend
mvn clean spring-boot:run
```

启动后监听 `http://localhost:8080`，首次启动自动：建表 → 扫描注解 → 注册权限 → 创建示例用户/角色/菜单。再次启动保留已有数据。

### 2. 启动前端

```bash
cd frontend
npm ci
npm run dev
```

浏览器访问：**http://localhost:5173**。Vite 将 `/api` 同源代理到后端，Cookie Session 和 CSRF 能够协同工作。

### 3. 演示账号（仅限本地）

| 用户 | 密码 | 权限说明 |
|---|---|---|
| `admin` | `Admin@123456` | 内置演示超级管理员，能访问管理后台与新增业务设备 |
| `operator` | `Operator@123456` | 仅有消防设备的查看权限，不能新增设备 |

**绝对不要将上述账号与该配置原样放到公网或生产环境。** 演示数据初始化器 `DemoDataInitializer` 应在生产部署前删除或禁用，改用正式的账号创建流程。

## 三、按操作链验证闭环

1. 管理员登录 → **权限资源**：查看注解自动发现的 9 项权限和其绑定的 Controller URL；点击某条“详情”可看到真实接口映射。
2. 进入 **菜单配置** → 新增页面 → 通过中文名称搜索“查看消防设备”等权限，无需从源码复制 `sop:fireSafety:deviceInfo`。
3. 进入 **角色授权** → 给设备查看员角色增加“新增消防设备”权限 → 保存。
4. 用 `operator` 登录/刷新：消防设备页面会出现“新增设备”按钮，并且后端 POST 接口可以真正访问。撤销授权后立即失效。
5. 进入 **审计日志** 检查“GRANT”“CREATE”“UPDATE”等事件。可以在权限资源池点击“重新扫描同步”，确认菜单/角色原有绑定未被清空。

> 新注解实时鉴权，权限变更无需重新登录；旧 `hasAnyAuthority` 依赖登录时快照，仍需用户重新登录。

## 四、工程目录

```text
permission-governance-demo/
├── README.md
├── backend/
│   ├── pom.xml
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── schema.sql
│   ├── src/main/java/com/example/permissionhub/
│   │   ├── controller/      # HTTP 入口与请求校验
│   │   ├── service/         # 业务规则、权限扫描、用例事务
│   │   ├── repository/      # Flex 查询、DTO 映射、种子数据持久化
│   │   ├── mapper/          # MyBatis-Flex BaseMapper 接口
│   │   ├── entity/          # 数据表实体（APT 生成 TableDef）
│   │   ├── dto/             # 每个请求、响应与扫描记录独立成文件
│   │   ├── config/          # 安全配置与权限注册配置
│   │   ├── security/        # 权限注解与实时鉴权
│   │   ├── constant/        # 权限码与安全常量
│   │   ├── enums/           # 菜单与权限状态枚举
│   │   ├── exception/       # 业务异常与全局响应处理
│   │   └── bootstrap/       # 本地演示初始化编排
│   └── src/test/           # 权限与注册核心集成测试
├── frontend/
│   ├── src/api/            # 统一 API 调用
│   ├── src/types/          # TypeScript DTO 类型
│   ├── src/constants/      # 少量前端按钮权限常量
│   ├── src/stores/         # Pinia Session 状态
│   ├── src/router/         # 白名单路由+菜单守卫
│   ├── src/components/     # 可复用权限码选择器
│   ├── src/layouts/        # 菜单/布局
│   └── src/views/          # 9 个界面（含登录、无权页）
├── scripts/                # 旧注解权限字符串盘点脚本
└── docs/
    ├── ARCHITECTURE.md     # 完整设计与限制
    ├── API.md              # 接口契约
    ├── MIGRATION.md        # 旧项目接入指南
    ├── ACCEPTANCE.md       # 手工验收步骤
    └── mysql-schema.sql    # MySQL 8 DDL
```

## 五、开发与验证命令

```bash
# 在项目根目录执行
mvn -f backend/pom.xml clean verify
npm --prefix frontend ci
npm --prefix frontend run build
python3 scripts/legacy_permission_inventory.py /path/to/your/java/project ./legacy-permissions.csv
```

注意：`mvn clean verify` 会先由 MyBatis-Flex APT 生成表定义，再执行集成测试并打包。测试使用内存 H2，并不触碰本地文件库。`npm run build` 已包含 `vue-tsc --noEmit` 类型检查；`npm ci` 根据随项目提供的 `frontend/package-lock.json` 安装固定版本依赖。

构建产物：

- 后端：`backend/target/permission-governance-1.0.0.jar`，可在 `backend` 目录使用 `java -jar target/permission-governance-1.0.0.jar` 启动。
- 前端：`frontend/dist/`，部署时需将 `/api` 反向代理到后端；Vite 的 `server.proxy` 仅供本地开发使用。

## 六、边界与生产化事项

本项目是**从 0 到 1 的可演示教学系统**，而不是已具备高可用、安全合规、租户隔离的商业平台。尤其需要在生产化前补齐：OIDC/SSO、账号锁定与 MFA、密码策略、权限缓存/失效广播、审计分页与归档、会话吊销、多租户隔离、全量同步的版本控制与多实例协调、Flyway 迁移、审计不可篡改存储、CI Manifest 发布、测试覆盖和 API 文档自动生成。完整决策见 `docs/ARCHITECTURE.md`。

**验证状态（2026-10-16）：** 使用 JDK 17.0.7 / Maven 3.9.6 执行 `mvn clean verify` 成功，16 项集成测试全部通过；使用 Node.js 26.3.0 / npm 11.11.0 执行 `npm run build` 成功（含 Vue/TypeScript 类型检查）。已用内存 H2 启动打包后的 JAR，验证 CSRF、真实登录与 Session、权限查询、设备读取及越权写入返回 403，并停止验证进程。仍有 `AntPathRequestMatcher` 弃用及前端单个包超过 500 kB 的非阻断警告；未执行浏览器手工验收。
