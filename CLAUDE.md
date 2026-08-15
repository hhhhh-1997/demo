# CLAUDE.md

## 技术选型

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot 3.4 · Java 21 · MyBatis-Plus 3.5 · Sa-Token |
| 前端 | Vue 3 · TypeScript · Vite · Element Plus · Pinia · Vue Router · Axios · ECharts |
| 中间件 | MySQL 8 · Redis 7 · MinIO |

## 常用命令

| 场景 | 命令 |
|------|------|
| 启动中间件 | `docker compose up -d` |
| 后端启动 | `cd backend && mvn spring-boot:run` |
| 后端单元测试 | `cd backend && mvn test` |
| 后端集成测试 | `cd backend && mvn test -Dtest='ProjectControllerIT,ReviewControllerIT,AuditControllerIT,ReserveControllerIT'` |
| 前端启动 | `cd frontend && npm run dev` |
| 前端类型检查 | `cd frontend && npm run typecheck` |
| 前端构建 | `cd frontend && npm run build` |

## 关键规范

### 后端红线（强制，违反必须返工）

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

### 命名规范

- 类名 UpperCamelCase；方法、参数、变量 lowerCamelCase；常量 UPPER_SNAKE_CASE；包名全小写。
- 分层命名按用途区分：DO / BO / VO / DTO，禁止混用。
- POJO 布尔属性不加 `is` 前缀（数据库字段才用 `is_xxx`）。
- 抽象类 `Abstract` / `Base` 开头；异常类 `Exception` 结尾；测试类 `Test` 结尾。
- 禁止魔法值：抽取为常量或枚举（`-1` / `0` / `1` 等循环控制除外）。

### 代码格式

- 4 空格缩进，禁用 tab；单行不超过 120 字符。
- 大括号 K&R 风格；`if` / `for` / `while` 必须使用大括号，即使只有一行。
- 运算符两侧加空格，`,` 后加空格。
- 类、方法使用 Javadoc；类必须含 `@author` 与创建日期；抽象方法必须写 Javadoc。

### 集合 / OOP / 并发

- 覆写 `equals` 必须同时覆写 `hashCode`。
- 包装类型（`Integer` 等）比较用 `equals`，不用 `==`。
- 遍历中删除用 `Iterator.remove()`，禁止 foreach 中 `add` / `remove`。
- 循环字符串拼接用 `StringBuilder`。
- `Arrays.asList` 结果不可 `add` / `remove` / `clear`；`ArrayList.subList` 结果不可强转 `ArrayList`。

### 前端规范

- 统一使用组合式 API + `<script setup lang="ts">`，禁止 Options API。
- 组件文件 PascalCase（`UserList.vue`）；组合式函数 `useXxx`；事件处理 `handleXxx`。
- 变量、方法 lowerCamelCase；常量 UPPER_SNAKE_CASE；类型/接口 PascalCase。
- 禁用 `any`：props、API 返回、表单模型均需显式类型。
- Props 用 `defineProps<{...}>()` 声明类型；模板中组件与属性用 kebab-case。
- 样式用 `scoped`，避免全局污染；类名语义化或 BEM。
- 组件库统一使用 Element Plus（`el-` 前缀）；表单校验用 `el-form` + `rules`。
- 接口请求统一封装，禁止在组件中散落裸 fetch / axios 调用。
