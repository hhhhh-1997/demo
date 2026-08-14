# 绩效月报查询模块 · 后端设计（DESIGN）

> 关联：规格 `docs/superpowers/specs/2026-08-14-绩效月报查询-design.md` ｜ 需求 `docs/需求文档.md` ｜ 表结构 `database/schema.sql`
> 核心原则：**后端只做「检索」与「原始数据返回」，一切展示规则（空值归并、等级映射、精度截断）不得在后端静默修正。**

---

## 1. 技术选型

| 层 | 技术 | 版本 | 说明 |
|---|---|---|---|
| 语言 | Java | 21 | 与 `backend/pom.xml` `<java.version>21</java.version>` 一致 |
| 框架 | Spring Boot | 3.4.1 | `spring-boot-starter-web` / `spring-boot-starter-validation` |
| ORM | MyBatis-Plus | 3.5.9 | `mybatis-plus-spring-boot3-starter`，`BaseMapper` + `QueryWrapper` |
| 数据库 | MySQL | 8 | `mysql-connector-j`，库 `default_db` |
| 缓存/对象存储 | Redis / MinIO | — | 本期模块不涉及，沿用骨架配置 |
| 测试 | JUnit 5 + MockMvc + Mockito | — | `spring-boot-starter-test` |

**设计决策**：检索仅涉及三列等值过滤 + 两列排序，使用 `QueryWrapper` 动态拼接即可，**不引入自定义 XML**（`database/../mapper/PerformanceMapper.xml` 仅在确有复杂 SQL 时才需要）。

---

## 2. 项目结构

```
backend/src/main/java/org/dromara/demo/
  DemoApplication.java            # 已有，启动类（不动）
  config/MinioConfig.java         # 已有（不动）
  config/MinioProperties.java     # 已有（不动）
  controller/PerformanceController.java    # GET /api/performance/monthly
  service/PerformanceService.java          # 检索：过滤 + 排序
  mapper/PerformanceMapper.java            # MyBatis-Plus Mapper（空接口）
  domain/Performance.java                  # 实体，对应 monthly_performance
  dto/PerformanceQuery.java                # 查询入参
  common/Result.java                       # 统一响应体 { code, msg, data }
  common/GlobalExceptionHandler.java       # 异常 → Result.error(500)
```

（目录结构与规格 §9 一致，另补充 `common/GlobalExceptionHandler.java` 以满足「错误态可感知、不吞异常」的验收口径。）

---

## 3. 分层架构

```
Controller (PerformanceController)
   │  参数绑定 PerformanceQuery，返回 Result<List<Performance>>
   ▼
Service (PerformanceService)
   │  构建 QueryWrapper：三条件空值不拼入 + 排序 month DESC, id ASC
   ▼
Mapper (PerformanceMapper extends BaseMapper<Performance>)
   │  MyBatis-Plus 自动 SQL，字段 camelCase ↔ snake_case 映射
   ▼
MySQL (monthly_performance)
```

职责边界：

| 层 | 职责 | 禁止 |
|---|---|---|
| Controller | 接收 query 参数，调用 service，包装 `Result` | 不做过滤/映射/格式化 |
| Service | 组装检索条件与排序 | 不做展示层修正（空值归并、等级映射、精度截断） |
| Mapper | 单一数据访问 | 无业务逻辑 |

- **不吞异常**：Service 不捕获异常，异常向上冒泡，由 `GlobalExceptionHandler` 统一转换为 `Result.error(500, msg)`，HTTP 状态 500。
- 检索逻辑集中在 `buildWrapper`，**独立可测**（不依赖数据库）。

---

## 4. 数据设计

### 4.1 表结构（权威定义见 `database/schema.sql`）

单表 `monthly_performance`，20 字段，与需求 §5 一致：

| 字段 | 类型 | 关键口径 |
|---|---|---|
| `id` | INT AUTO_INCREMENT | 主键；导入时保留 xlsx 原始 `id`（10001…） |
| `user_id` | INT NOT NULL | 与 `month` 组合唯一 `uk_user_month` |
| `realname` | VARCHAR(64) NOT NULL | 姓名 |
| `month` | VARCHAR(7) NOT NULL | 定宽 `YYYY-MM`，**直接字符串比较** |
| `task_finish_rate` | DECIMAL(4,1) NOT NULL | 分值项，全表无空值 |
| `work_effect_rate` | DECIMAL(4,1) NOT NULL | 分值项，超 100 上限 120，不截断 |
| `work_normativity` | INT NOT NULL | 分值项，整型存储 0/10/…/100 |
| `learning_improvement` | DECIMAL(4,1) NULL | 分值项 |
| `software_design` | DECIMAL(4,1) NULL | 分值项 |
| `pre_sales_support` | TINYINT NULL | **等级项**（码 1~5） |
| `bug_condition` | DECIMAL(4,1) NULL | 分值项 |
| `system_design` | DECIMAL(4,1) NULL | 分值项 |
| `code_review` | DECIMAL(4,1) NULL | 分值项 |
| `test_quality` | TINYINT NULL | **等级项**（码 1~5） |
| `dept` | INT NOT NULL | 二级部门 ID |
| `role` | VARCHAR(32) NOT NULL | 岗位 key |
| `role_name` | VARCHAR(64) NOT NULL | 岗位名（源导出冗余） |
| `dept_name` | VARCHAR(64) NOT NULL | 二级部门名 |
| `top_dept_id` | INT NOT NULL | 一级部门 ID（筛选键） |
| `top_dept_name` | VARCHAR(64) NOT NULL | 一级部门名（仅展示） |

### 4.2 实体类型映射（camelCase）

DECIMAL → `BigDecimal`，TINYINT/INT → `Integer`，VARCHAR → `String`。

> **关键**：分值项/等级项字段必须保留 `null` 与 `0` 的原样区分 —— `BigDecimal`/`Integer` 的 `null` 序列化为 JSON `null`，`0` 序列化为 JSON `0`，二者语义相反，后端不做任何归并。

### 4.3 数据装载

- 建表：`database/schema.sql`（已存在，DROP + CREATE）。
- 种子数据：`scripts/import_xlsx.py`（Python 标准库，零依赖）解析 `data/人员月度绩效_2026-05至07.xlsx` 生成 `database/seed.sql`（642 行，三个月全量）。
- xlsx 结构：sheet1「绩效原始数据」第 1 行为字段名（A~T 共 20 列），第 2 行为中文注释，第 3 行起为 642 行数据；空单元格 → `NULL`，含 `<v>0</v>` 的单元格 → `0`。

---

## 5. API 接口设计

### 5.1 接口

```
GET /api/performance/monthly
```

Query 参数（均可缺省 / 空串表示「全部」）：

| 参数 | 类型 | 说明 |
|---|---|---|
| `topDeptId` | int | 一级部门 ID，空 = 全部 |
| `role` | string | 岗位 key，空 = 全部 |
| `month` | string | 月份 `YYYY-MM`，空 = 全部 |

### 5.2 统一响应体

```json
{ "code": 200, "msg": "success", "data": [ ... ] }
```

| 场景 | HTTP | code | data | 前端表现 |
|---|---|---|---|---|
| 成功有数据 | 200 | 200 | 对象数组 | 表格 |
| 成功无数据 | 200 | 200 | `[]` | 空态 |
| 失败 | 500 | 500 | `null` | 错误态 + 重试 |

### 5.3 data 元素

返回 `monthly_performance` 全部 20 字段**原样**（NULL → JSON `null`，0 → JSON `0`，等级码 → 1~5 整数，decimal → 数值不截断）。展示层只渲染 15 列，格式化/映射在前端完成。

---

## 6. 检索逻辑设计

核心方法 `PerformanceService.buildWrapper(PerformanceQuery)`：

```
QueryWrapper<Performance> w = new QueryWrapper<>();
if (topDeptId != null)          w.eq("top_dept_id", topDeptId);   // 空值不拼入
if (StringUtils.hasText(role))  w.eq("role", role);               // '' 不拼入
if (StringUtils.hasText(month)) w.eq("month", month);             // 定宽字符串等值比较，不转日期
w.orderByDesc("month").orderByAsc("id");                          // 排序：month DESC, id ASC
```

规则：

1. **空值表达「全部」**：`topDeptId` 为 `null` 不拼入；`role` / `month` 空串不拼入。不使用 `0`/`"all"` 哨兵值。
2. **月份字符串比较**：`month` 为定宽 `YYYY-MM`，直接 `eq` 等值比较，**不得**转 `DATE`/`STR_TO_DATE`。
3. **排序固定**：`ORDER BY month DESC, id ASC`（在后端完成，属检索范畴；`id` 即 xlsx 原始记录 ID，保证同月内原始顺序）。
4. **等级项不排序**：排序键不含 `pre_sales_support` / `test_quality`。

单测覆盖（`PerformanceServiceTest`）：三条件拼接/空值不拼接、排序片段、月份字符串比较（断言 SQL 不含日期转换函数）。

---

## 7. 错误处理

`GlobalExceptionHandler`（`@RestControllerAdvice`）：

```java
@ExceptionHandler(Exception.class)
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public Result<Void> handle(Exception e) {
    return Result.error(500, e.getMessage());
}
```

- 任何异常向上冒泡 → 统一返回 HTTP 500 + `{ code: 500, msg, data: null }`。
- **不吞异常**：错误信息（`msg`）透传给前端，前端据此展示错误态。
