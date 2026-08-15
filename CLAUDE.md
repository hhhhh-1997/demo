# CLAUDE.md

绩效月报查询模块。前后端分离：前端负责检索交互与数据展示，后端负责检索并**原样返回原始数据**。

## 上下文指针

- `docs/需求文档.md` —— 需求与验收口径全文。

## 技术栈

- 后端：Spring Boot 3 / Java 21 / MyBatis-Plus / MySQL / Redis / MinIO。
- 前端：Vue 3 + TypeScript（strict）+ Vite + Element Plus。
- 具体版本与配置见 `backend/pom.xml`、`backend/src/main/resources/application.yml`、`frontend/package.json`。

## 常用命令

| 用途 | 命令 |
|---|---|
| 启动后端 | `cd backend && mvn spring-boot:run` |
| 后端测试 | `cd backend && mvn test` |
| 启动前端 | `cd frontend && npm run dev`（默认端口 5175） |
| 前端类型检查 | `cd frontend && npm run typecheck` |
| 前端测试 | `cd frontend && npm test` |
| 生成种子数据 | `python3 scripts/import_xlsx.py`（生成 `database/seed.sql`） |
| 启动中间件 | `docker compose up -d`（MySQL / Redis / MinIO） |

## 架构与职责边界

后端只做「检索」与「原始数据返回」，**一切「展示规则」落在展示层**。任何一层不得静默修正数据语义。

| 环节 | 负责方 |
|---|---|
| NULL 与数值 0 原样返回 | 后端 |
| 等级码原样返回，不做文本映射 | 后端 |
| 小数精度格式化、等级码映射、越界/空值兜底 | 展示层 |

## 展示规则落地口径

### 空值语义（核心）

- `NULL` 表示「无该项工作」，展示为 `-`。
- 数值 `0` 表示「有该项工作但得 0 分」，展示为 `0.0`。
- **二者语义相反，不可互相替代**，任何环节不得将 `0` 归并为「无数据」。

### 等级项

- 等级码由展示层映射为中文文本；数值大小与优劣方向**相反**（小值更优）。
- 等级码为 `NULL` 或越界时展示 `-`，不报错、不回落；兜底分支须实现并有单测覆盖。
- 等级项不得求和、求平均或按数值排序，界面上与分值项视觉区分。

### 其它

- 分值项统一保留一位小数；超出上限按原值展示，不截断。
- 月份为定宽字符串，直接字符串比较，不转日期类型。
- 检索条件「全部」以空值表达，不拼入过滤逻辑；字典全量展示，不动态裁剪。

## 红线约束（强制，违反必须返工）

1. **SQL 注入**：禁止字符串拼接 SQL。MyBatis 一律使用 `#{}` 占位符；外部输入禁止进入 `${}`。
2. **敏感信息**：密码、token、身份证、手机号等禁止出现在日志、异常信息、接口返回中；输出需脱敏。
3. **密码存储**：禁止明文或可逆加密存储，使用 BCrypt 等加盐哈希。
4. **参数校验**：Controller 入参必须用 `@Validated` / `@Valid` 校验，禁止信任前端传入值。
5. **越权防护**：写操作必须校验数据归属与权限，不能只依赖前端隐藏按钮。
6. **事务一致**：多表写操作加 `@Transactional`；异常被 catch 后如需回滚，必须手动回滚（`TransactionAspectSupport`）。
7. **资源释放**：IO 流、连接用 try-with-resources 或 finally 关闭。
8. **异常处理**：禁止空 catch 吞异常；捕获后必须记录日志或向上抛出，不得静默。
9. **线程安全**：禁止用 `Executors` 创建线程池，必须用 `ThreadPoolExecutor` 显式传参；`SimpleDateFormat` 禁止作为共享静态变量（用 `DateTimeFormatter`）。
10. **金额与小数**：金额一律 `BigDecimal`，禁止 `float` / `double`；比较用 `compareTo`，禁止 `equals` 判等与 `new BigDecimal(double)`。

## 命名规范

- 类名 UpperCamelCase；方法、参数、变量 lowerCamelCase；常量 UPPER_SNAKE_CASE；包名全小写。
- 分层命名按用途区分：DO / BO / VO / DTO，禁止混用。
- POJO 布尔属性不加 `is` 前缀（数据库字段才用 `is_xxx`）。
- 抽象类 `Abstract` / `Base` 开头；异常类 `Exception` 结尾；测试类 `Test` 结尾。
- 禁止魔法值：抽取为常量或枚举（`-1` / `0` / `1` 等循环控制除外）。

## 代码格式

- 4 空格缩进，禁用 tab；单行不超过 120 字符。
- 大括号 K&R 风格；`if` / `for` / `while` 必须使用大括号，即使只有一行。
- 运算符两侧加空格，`,` 后加空格。
- 类、方法使用 Javadoc；类必须含 `@author` 与创建日期；抽象方法必须写 Javadoc。

## 集合 / OOP / 并发

- 覆写 `equals` 必须同时覆写 `hashCode`。
- 包装类型（`Integer` 等）比较用 `equals`，不用 `==`。
- 遍历中删除用 `Iterator.remove()`，禁止 foreach 中 `add` / `remove`。
- 循环字符串拼接用 `StringBuilder`。
- `Arrays.asList` 结果不可 `add` / `remove` / `clear`；`ArrayList.subList` 结果不可强转 `ArrayList`。

## 数据库规约（MySQL）

- 表名、字段名小写 + 下划线；禁止数字开头、两个下划线中间纯数字。
- 索引命名：主键 `pk_`、唯一 `uk_`、普通 `idx_`。
- 是与否字段用 `is_xxx` + `unsigned tinyint`；小数用 `decimal`。
- 查询禁用 `SELECT *`，必须列出字段；更新记录需同时更新 `update_time`。

## 前端规范（Vue 3 + TypeScript + Element Plus）

- 统一使用组合式 API + `<script setup lang="ts">`，禁止 Options API。
- 组件文件 PascalCase（`UserList.vue`）；组合式函数 `useXxx`；事件处理 `handleXxx`。
- 变量、方法 lowerCamelCase；常量 UPPER_SNAKE_CASE；类型/接口 PascalCase。
- 禁用 `any`：props、API 返回、表单模型均需显式类型。
- Props 用 `defineProps<{...}>()` 声明类型；模板中组件与属性用 kebab-case。
- 样式用 `scoped`，避免全局污染；类名语义化或 BEM。
- 组件库统一使用 Element Plus（`el-` 前缀）；表单校验用 `el-form` + `rules`。
- 接口请求统一封装，禁止在组件中散落裸 fetch / axios 调用。

## 编码与构建门禁

- 后端分层清晰；前端严格 TS，提交前 `npm run typecheck` 通过。
- 后端提交前 `mvn test` 通过；前端提交前 `npm test` 通过。
- 关键业务规则（空值语义、等级码映射、精度处理）须加注释。

## 禁止事项

1. 不得将 `0` 归并为「无数据」或 `NULL`。
2. 不得对等级项求和、求平均或按数值排序。
3. 不得在后端做展示层修正（空值归并、等级码映射、精度截断）。
4. 不得吞掉异常。
5. 不得将月份转为日期类型再比较。
6. 不得截断超上限的分值。
7. 不得动态裁剪字典，不得省略等级码越界兜底分支（即使数据中未出现）。
