# 绩效月报查询模块

前后端分离的绩效月报在线查询系统：按「一级部门 / 岗位 / 月份」检索，以表格展示 2026-05/06/07 三个月共 642 行人员月度绩效数据。

## 项目简介

- **后端**：Spring Boot 3.4.1 / Java 21 / MyBatis-Plus 3.5.9 / MySQL 8，对外提供 `GET /api/performance/monthly`，只负责**检索并原样返回原始数据**（不做任何展示层修正）。
- **前端**：Vue 3 + TypeScript(strict) + Vite + Element Plus，负责检索交互与**全部展示规则**（空值语义、等级码映射、小数精度），页面视觉完全依赖原型 `docs/prototype/performance-report-query.html`。
- **数据**：`data/人员月度绩效_2026-05至07.xlsx`（642 行，2026-05=216 / 2026-06=213 / 2026-07=213）经 `scripts/import_xlsx.py` 全量导入 MySQL。

### 架构与职责边界

| 环节 | 负责方 |
|---|---|
| 检索过滤、排序（`ORDER BY month DESC, id ASC`） | 后端 |
| NULL 与数值 0 原样返回、等级码原样返回 | 后端 |
| 小数精度格式化（一位小数）、等级码 → 中文文本映射、越界/空值兜底 | 前端 |

**核心口径（不可违背）**：分值项 `NULL` = 「无该项工作」展示为 `-`；数值 `0` = 「有该项工作但得 0 分」展示为 `0.0`，二者语义相反，任何环节不得将 `0` 归并为「无数据」。

### 目录结构

```
backend/    Spring Boot 服务（controller / service / mapper / domain / dto / common）
frontend/   Vue 3 + Vite（src 下组件化组织）
database/   schema.sql（建表 DDL）+ seed.sql（642 行种子数据，由脚本生成）
scripts/    import_xlsx.py（xlsx → MySQL 导入脚本）
docs/       需求文档 / 问题清单 / 设计规格 / 实现计划 / 设计文档 / 原型
data/       原始 xlsx 数据源
```

## 环境要求

| 依赖 | 版本 | 说明 |
|---|---|---|
| JDK | 21 | 后端编译/运行（见下方 `JAVA_HOME` 说明） |
| Maven | 3.9+ | 后端构建 |
| Node.js | 18+（建议 22） | 前端构建/运行 |
| npm | 10+ | 前端依赖管理 |
| Docker | 20+ | 运行 MySQL/Redis/MinIO 容器 |

## 部署说明

### 1. 启动基础设施

```bash
docker compose up -d
```

启动 MySQL（库 `default_db`，端口 3306，root/123456）、Redis（6379，密码 123456）、MinIO（9000/9001）。其中 **MySQL 是本模块必需**；Redis/MinIO 为骨架服务，本模块不涉及。

### 2. 初始化数据库

> 先启动 MySQL 容器，再按顺序执行建表与导数据。

```bash
# 有 mysql 客户端时：
mysql -h127.0.0.1 -uroot -p123456 < database/schema.sql
mysql -h127.0.0.1 -uroot -p123456 default_db < database/seed.sql

# 无 mysql 客户端时，用容器内客户端：
docker exec -i mysql-server mysql -uroot -p123456 < database/schema.sql
docker exec -i mysql-server mysql -uroot -p123456 default_db < database/seed.sql
```

校验入库结果（应为 642）：

```bash
mysql -h127.0.0.1 -uroot -p123456 -e "SELECT COUNT(*) FROM default_db.monthly_performance;"
```

> `database/seed.sql` 已随仓库提供。如需从 xlsx 重新生成：`python3 scripts/import_xlsx.py`（零依赖，读取 `data/人员月度绩效_2026-05至07.xlsx` 重写 `database/seed.sql`）。

### 3. 启动后端

项目目标 Java 21；若本机默认 `java` 非 21，需显式指定 `JAVA_HOME`：

```bash
cd backend
JAVA_HOME=/path/to/jdk-21 mvn spring-boot:run
```

后端监听 `http://localhost:8080`，数据源/Redis/MinIO 连接信息见 `backend/src/main/resources/application.yml`（默认 root/123456）。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

开发服务器监听 `http://localhost:5173`，`/api` 已代理到 `http://localhost:8080`（见 `frontend/vite.config.ts`）。

生产构建：

```bash
cd frontend
npm run build     # 产物输出到 frontend/dist/
```

### 5. 验证

浏览器打开 `http://localhost:5173`：

- 默认月份为当前月（动态），初始为空态「暂无数据」。
- 月份选「全部」或 2026-05/06/07 → 表格展示数据，表头固定、左侧姓名列冻结。
- 分值项 `-`（无该项工作）与 `0.0`（得 0 分）正确区分；等级项（售前支撑、测试产出质量）显示徽标。
- 停掉后端后查询 → 显示错误态「查询失败」+ 重试。

## 测试

```bash
# 后端（JUnit 5 + MockMvc + Mockito）
cd backend && JAVA_HOME=/path/to/jdk-21 mvn test

# 前端（Vitest 纯函数单测 + 类型检查）
cd frontend && npm test
cd frontend && npm run typecheck
```

## 关键业务规则

- **空值语义**：分值项 `NULL` → `-`，`0` → `0.0`，不得互相替代。
- **等级项**：`pre_sales_support` / `test_quality` 存等级码 1~5，映射为 1 超出预期 / 2 完全达标 / 3 需要提升 / 4 未达预期 / 5 无该项工作；`NULL` 或越界 → `-`；不得求和/平均/数值排序，界面以徽标与分值项区分。
- **精度**：分值项统一一位小数；`work_effect_rate` 超 100（上限 120）按原值不截断。
- **月份**：定宽字符串 `YYYY-MM`，直接字符串比较，不转日期类型。
- **检索条件**：「全部」以空值表达，空值不拼入过滤；字典全量展示，不动态裁剪。
- **错误态**：不吞异常，异常向上传递并在展示层转化为错误态。
