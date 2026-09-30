# XuYa Plus

RuoYi-Vue-Plus 同款后台管理脚手架（去多租户），基于 [Quick-Dev](https://github.com/xuya-dev/Quick-Dev) 注解式 CRUD 框架构建。

> 绝大多数系统管理接口由 `@QuickCrud` 在启动期自动注册，本项目手写的只有：认证、聚合写接口（事务）、
> 监控端点，以及各类框架 SPI（权限数据源 / 字典加载器 / 操作日志落地 / 数据权限）。

## 功能清单

| 模块 | 说明 | 实现方式 |
|------|------|----------|
| 认证 | 验证码、登录/登出、当前用户、动态路由 | 手写（Sa-Token + BCrypt + Redis） |
| 用户管理 | 分页/详情/导出 + 增删改（含角色岗位绑定）、状态、重置密码、个人中心 | `@QuickCrud` 读 + 手写聚合写 |
| 角色管理 | CRUD + 菜单授权 + 自定义数据权限部门 | `@QuickCrud` 读 + 手写聚合写 |
| 菜单管理 | 树/列表/详情 + CRUD（变更即刷新权限缓存） | `@QuickCrud` + 手写 |
| 部门管理 | 树 + CRUD（维护 ancestors 祖级路径） | `@QuickCrud` + 手写 |
| 岗位管理 | 全套 CRUD + 导出 | `@QuickCrud` 零手写 |
| 字典管理 | 类型/数据 CRUD，变更即刷新字典内存缓存；数据写接口经 DictDataHook 校验类型并刷缓存 | 类型写手写（级联改码）；数据写 `@QuickCrud` + `CrudHook` |
| 参数配置 | CRUD + 按键名取值（内存缓存零查库） | `@QuickCrud` 读 + 手写 |
| 通知公告 | 全套 CRUD | `@QuickCrud` 零手写 |
| 操作日志 | `@QuickLog` 切面异步落库 + 分页查询/清空 | SPI 实现 + `@QuickCrud` |
| 登录日志 | 登录成功/失败落库 + 分页查询/清空 | 手写记录 + `@QuickCrud` |
| 在线用户 | Redis 会话注册表，查询/强退 | 手写 |
| 缓存管理 | RBAC/字典/参数缓存状态查看与手动刷新 | 手写 |
| 数据权限 | 角色五种 data_scope（全部/自定义/本部门/本部门及以下/仅本人） | `DataScopeResolver` SPI |

> 基于 Quick-Dev **0.2.0**：用户列表的 `deptId` 走 `@Translate(mode = APPEND)`（保留原始 ID 并附加 `deptName`）；
> 字典数据的增删改由 `@QuickCrud` 生成、`DictDataHook`（`CrudHook` SPI）负责类型校验与缓存刷新。

## 快速开始

### 1. 环境准备

- JDK 17+（本机 21 验证通过）
- Maven 3.9+
- MySQL 8.x、Redis（默认连 WSL 内的实例，均可通过环境变量覆盖）

### 2. 安装 Quick-Dev 到本地仓库

```bash
git clone https://github.com/xuya-dev/Quick-Dev.git
cd Quick-Dev && mvn install -DskipTests
```

### 3. 初始化数据库

```bash
mysql -uroot -p < sql/xuya_plus.sql
```

脚本自带建库（`xuya_plus`）、16 张表与种子数据。

### 4. 配置

`src/main/resources/application.yml` 默认值（全部可用环境变量覆盖）：

| 变量 | 默认 | 说明 |
|------|------|------|
| `MYSQL_HOST/PORT/DATABASE` | localhost:3306/xuya_plus | 数据库 |
| `MYSQL_USERNAME/MYSQL_PASSWORD` | root / root | 数据库账号 |
| `REDIS_HOST/PORT` | localhost:6379 | Redis（Sa-Token/验证码/在线用户） |

### 5. 启动

```bash
mvn spring-boot:run
# 或
mvn package -DskipTests && java -jar target/xuya-plus.jar
```

- 接口文档：http://localhost:8080/swagger-ui.html
- 默认账号：`admin / admin123`（超级管理员，`*:*:*` 全部权限）
- 演示账号：`test / admin123`（普通角色，"本部门及以下"数据权限）

## 默认接口约定（Quick-Dev 风格）

统一响应 `R<T>`：`{code, msg, data}`（200 成功；400 参数；401/403 真实 HTTP 状态码）。

以用户管理为例（其余模块同理，路径 = 实体名 kebab-case）：

```
GET    /sys-user/page?current=1&size=10&nickname=张&status=0&orderBy=id&order=desc
GET    /sys-user/list?status=0
GET    /sys-user/count?status=0
GET    /sys-user/{id}
PUT    /sys-user                          修改（null 字段不更新）
DELETE /sys-user/{ids}                    删除（逗号分隔批量）
GET    /sys-user/export                   导出 Excel（复用分页查询条件）
POST   /sys-user                          新增（含角色/岗位绑定，事务）
PUT    /sys-user/{id}/status/{status}
PUT    /sys-user/{id}/password            {"password": "..."}
GET    /sys-user/{id}/role-ids            编辑表单回显
PUT    /sys-user/profile                  个人中心-改资料
PUT    /sys-user/profile/password         个人中心-改密码 {"oldPassword","newPassword"}
```

查询参数与实体字段同名：`@QueryField(LIKE/BETWEEN/IN/...)` 决定条件方式，未标注的字段精确匹配；
BETWEEN 时间格式 `2026-01-01T00:00:00,2026-12-31T23:59:59`。

认证接口：

```
GET  /auth/captcha            图形验证码 {uuid, img(base64)}（sys_config 可关）
POST /auth/login              {username, password, code?, uuid?} -> {tokenName, tokenValue, expireIn}
POST /auth/logout
GET  /auth/info               当前用户 {user, roles, permissions}
GET  /auth/routers            前端动态路由（RuoYi 风格 RouterVo 树）
```

登录后所有请求带 `Authorization: {tokenValue}`（自动剥离 `Bearer ` 前缀）。

## 权限码约定

`@QuickCrud(permission = "sys:user")` 自动派生：`sys:user:list / detail / add / edit / remove / export / import`。
手写接口用 `@RequiresPerm("sys:user:add")`、`@RequiresLogin` 声明；角色表 `role_key = admin` 或用户 id=1 视为
超管（Sa-Token 通配 `*:*:*`）。菜单表 `sys_menu.perms` 与权限码一一对应，种子数据已配齐。

## 缓存优先设计

- **RBAC**（用户→角色→权限码/菜单）：全量内存，变更接口主动刷新 + 60s 定时兜底（`xuya-plus.cache-auto-refresh-seconds`）
- **字典**：`DictLoader` SPI 提供 `sys_dict_data` 全量给框架 `DictCacheService`，变更即刷新
- **参数**：`sys_config` 全量内存 Map，业务取值零查库
- **在线用户**：Redis `xuya:online:{token}`，与会话同 TTL
- 运维入口：`GET /sys-cache`、`POST /sys-cache/refresh/{rbac|dict|config|all}`

## 目录结构

```
xuya-plus
├── sql/xuya_plus.sql                  建库 + 16 表 + 种子数据
└── src/main/java/com/xuya/plus
    ├── XuyaPlusApplication.java
    ├── auth/                          认证（controller / service / dto）
    ├── framework/                     框架 SPI 对接
    │   ├── security/                  RbacCacheService、StpInterface、数据权限、参数缓存
    │   ├── translate/                 DbDictLoader
    │   ├── log/                       操作日志 Sink、登录日志
    │   └── monitor/                   在线用户
    ├── monitor/controller/            /sys-online、/sys-cache
    ├── system/                        实体 / Mapper / Service / Controller
    └── common/util/                   IP 工具
```
