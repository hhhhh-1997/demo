# 综合计划储备项目管理

对综合计划类储备项目进行全生命周期管理的系统，覆盖「储备项目维护 / 储备项目论证 / 储备项目审核 / 统一储备库」四大环节，实现储备项目的规范化、流程化管控。

## 项目简介

### 功能模块

| 模块 | 说明 |
|------|------|
| 储备项目维护 | 项目基础数据的新增、编辑、删除与提报，展示全部状态 |
| 储备项目论证 | 逐项核查项目的合规性与可行性（信息完整性 / 三重一大 / 拆分立项 / 界面混淆） |
| 储备项目审核 | 对论证结果进行人工复核，通过或退回 |
| 统一储备库 | 审核通过项目的统一管理、下达与图表统计分析 |
| 系统管理 | 用户 / 角色 / 菜单权限（RBAC，菜单级 + 按钮级动态配置） |

### 状态机

项目全生命周期共 **7 种状态**：

```
草稿 / 论证退回 / 审核退回 ──提报──▶ 待论证 ──论证通过──▶ 待审核 ──审核通过──▶ 待下达 ──下达──▶ 已下达
                                       │                    │
                                       └─论证不通过▶论证退回  └─审核不通过▶审核退回
```

### 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot 3.4 · Java 21 · MyBatis-Plus 3.5 · Sa-Token（认证 + 鉴权）· MySQL 8 · Redis 7 · MinIO |
| 前端 | Vue 3 · TypeScript · Vite · Element Plus · Pinia · Vue Router · Axios · ECharts |
| 测试 | JUnit 5 · Mockito · Spring Boot Test + MockMvc · H2（MySQL 兼容模式） |

### 权限模型

- 预置 5 角色：`admin`（系统管理员）/ `maintainer`（维护员）/ `reviewer`（论证员）/ `auditor`（审核员）/ `reserve`（储备库管理员）
- 权限字符串按 `模块:操作` 划分，后端 `@SaCheckPermission` 注解鉴权，前端路由守卫 + `v-permission` 指令控制按钮
- 密码 BCrypt 加盐哈希存储；接口返回与日志均不含密码 / token

## 首次部署

### 环境要求

- JDK 21
- Maven 3.8+
- Node.js 18+（含 npm）
- Docker + Docker Compose（用于 MySQL / Redis / MinIO）

### 1. 启动中间件

项目根目录执行：

```bash
docker compose up -d
```

将启动：

| 服务 | 端口 | 账号 / 密码 |
|------|------|-------------|
| MySQL | 3306 | root / 123456（库 `default_db`） |
| Redis | 6379 | 密码 `123456` |
| MinIO | 9000（API）/ 9001（控制台） | ruoyi / ruoyi123 |

### 2. 初始化数据库

建表脚本与种子数据位于 `database/`：

```bash
mysql -uroot -p123456 default_db < database/schema.sql
mysql -uroot -p123456 default_db < database/data.sql
```

> 种子数据包含：部门、字典、5 个角色、菜单与按钮权限、以及一个管理员账号（`admin` / `123456`，密码为 BCrypt 哈希）。

### 3. 后端配置

按需修改 `backend/src/main/resources/application.yml`（数据库、Redis、MinIO 连接信息，默认与 docker-compose 一致）。

### 4. 前端依赖

```bash
cd frontend
npm install
```

## 项目启动

### 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认监听 `http://localhost:8080`，REST API 统一前缀 `/api`。

### 启动前端

```bash
cd frontend
npm run dev
```

前端默认监听 `http://localhost:5173`，`/api` 已代理到后端 `8080`。

### 登录

浏览器打开 `http://localhost:5173`，使用管理员账号：

```
用户名：admin
密码：123456
```

### 运行测试

```bash
# 后端单元测试
cd backend && mvn test

# 后端集成测试（*IT 类，H2 内存库，不依赖外部 MySQL/Redis）
cd backend && mvn test -Dtest='ProjectControllerIT,ReviewControllerIT,AuditControllerIT,ReserveControllerIT'

# 前端类型检查 / 构建
cd frontend && npm run typecheck
cd frontend && npm run build
```

## 目录结构

```
backend/    Spring Boot 后端（src/main/java、src/test）
frontend/   Vue 3 前端（src/views、src/api、src/stores、src/components）
database/   数据库初始化脚本（schema.sql / data.sql）
docs/       需求文档、原型图、后端设计、设计规格（specs）、实现计划（plans）
docker-compose.yml  MySQL / Redis / MinIO
```
