# 北京市 2026 年积分落户公示名单分析仪表盘 — 设计文档

- 日期：2026-08-14
- 状态：已批准（待用户 review）

## 1. 背景与目标

北京市每年公示积分落户名单，含 6000+ 条落户人员信息。本项目的目标是一个**纯前端静态页面**，对这份数据进行多维度分析，让用户了解落户人员的整体特征与趋势。

核心诉求：

1. 数据查询：快速搜索、筛选、查看具体人员信息
2. 年龄分析：年龄分布特征与主要年龄段
3. 单位分析：哪些单位入围人数最多，单位规模分布
4. 积分分布：积分分值与分数段人数
5. AI 智能问数：自然语言提问（统计/排名/明细），大模型基于数据集回答，模型地址与 Key 可配置

## 2. 范围与约束

**在范围内：**

- 静态前端，构建产物可部署到任意静态服务器
- 浏览器内运行与交互，无后端服务器
- 数据从官方 Excel 导入，真实准确
- 搜索、筛选、图表、AI 问数
- Excel 导入（首次初始化 / 后续增量导入）与手动数据维护（增删改）

**在范围外（明确不做）：**

- 后端服务 / API
- 用户登录 / 权限
- 移动端适配（桌面端优先）

## 3. 技术选型（已确定）

| 项 | 选择 |
|---|---|
| 框架 | 现有 Vue 3 + TypeScript + Vite 脚手架 |
| UI 组件 | Naive UI（已依赖） |
| 图表 | ECharts（新增依赖） |
| 数据加载 | 浏览器内解析 Excel（SheetJS / `xlsx`），导入后存 localStorage |
| AI 调用 | OpenAI 兼容 `/v1/chat/completions`，支持 `tools`（function calling） |
| AI 数据访问 | 方案 A：固定工具集 |

## 4. 架构

单页应用，左侧边栏导航 + 右侧内容区，共 6 个板块。

```
frontend/
  src/
    types.ts                      # Record 接口
    data/importer.ts              # 浏览器解析 Excel（SheetJS）+ 派生字段（age）
    utils/stats.ts                # 聚合/分布计算（纯函数，供图表与 AI 工具共用）
    stores/useData.ts             # 全局数据状态 + localStorage 持久化
    stores/useLlmConfig.ts        # 大模型配置 CRUD + localStorage 持久化
    components/
      DataQuery.vue               # 板块1：数据查询（含导入 + 增删改）
      AgeAnalysis.vue             # 板块2：年龄分析
      UnitAnalysis.vue            # 板块3：单位分析
      ScoreDistribution.vue       # 板块4：积分分布
      AiAssistant.vue             # 板块5：AI 助手
      LlmConfig.vue               # 板块6：大模型管理
    ai/
      client.ts                   # OpenAI 兼容流式请求封装
      tools.ts                    # 工具定义 + handler
      prompt.ts                   # system prompt
      history.ts                  # 对话历史读写 + localStorage 持久化 + 裁剪
```

## 5. 数据层

- **数据源**：用户通过浏览器导入官方 Excel（如 `docs/北京市2026年积分落户公示名单.xlsx`，6003 条，5 列：公示编号、姓名、出生年月、单位名称、积分分值）。项目**初始无数据**，靠导入初始化。
- **导入**：`data/importer.ts` 用 SheetJS（`xlsx` 依赖，运行时）在浏览器解析 `.xlsx`；首次导入 = 初始化（全量加载），再次导入 = 增量导入（按公示编号 upsert：已存在则更新、不存在则新增）。
- **持久化**：当前数据存 `localStorage`（约 707KB，限额内），刷新后恢复；空数据时各板块显示「暂无数据，请先导入」。
- **手动维护**：单条记录的增 / 删 / 改，改动即时写入 `localStorage`。
- **数据模型**：`{ id, name, birth, unit, score }`，加载时派生 `age`。
- **年龄口径（决策）**：`age = 2026 − 出生年`（以公示年份 2026 为基准，月不参与；出生年月格式为 YYYY-MM）。

## 6. 六个板块

### 6.1 数据查询

- 「导入 Excel」按钮：选择 `.xlsx`，浏览器内解析；首次导入初始化（全量），之后增量导入（按公示编号 upsert：存在则更新、不存在则新增）
- 搜索框：姓名 / 公示编号 / 单位名称模糊匹配
- 筛选：年龄区间、积分区间
- `n-data-table` 展示（分页）
- 行点击弹出详情
- 记录增 / 删 / 改：新增记录、行内编辑、删除，改动即时写入 `localStorage`
- 「清空数据」按钮

### 6.2 年龄分析

- 年龄分布直方图（ECharts）
- 主要年龄段高亮
- 汇总统计：人数 / 最小 / 最大 / 中位数 / 众数

### 6.3 单位分析

- 入围人数 Top N 单位横向条形图（默认 Top 20，可调）
- 单位规模分布：按单位入围人数分桶（1 人 / 2-5 / 6-10 / 11-20 / 20+）
- 按单位名查询明细

### 6.4 积分分布

- 积分直方图（分段默认每 10 分，可调）
- 各分数段人数表
- 汇总：最高 / 最低 / 平均 / 中位数

### 6.5 AI 助手

- 聊天界面
- 顶部配置选择：下拉选择使用哪个大模型配置（默认用「大模型管理」中标记为默认的配置）
- 「清空对话」按钮

### 6.6 大模型管理

- 大模型配置 CRUD 列表，每项字段：显示名称、Base URL、API Key、模型名
- 新增 / 编辑 / 删除
- 标记一个为默认配置
- 存 `localStorage`
- 注意：静态前端下 API Key 以明文存于浏览器 `localStorage`，属已知局限

## 7. AI 助手（方案 A：固定工具集）

### 7.1 调用方式

`POST {baseUrl}/chat/completions`，OpenAI 兼容，请求体含 `messages` 与 `tools`，且 `stream: true`（SSE 流式）。`baseUrl`、`apiKey`、`model` 取自 AI 助手当前选中的大模型配置；baseUrl 为基地址（如 `https://…/v1`），客户端自动补 `/chat/completions`。

### 7.2 工具集（约 10 个，覆盖 统计/排名/明细）

- `search_by_name` — 按姓名查询人员
- `search_by_unit` — 按单位查询人员
- `top_units` — 单位入围人数排名
- `unit_size_distribution` — 单位规模分布
- `score_distribution` — 积分分布（分数段人数）
- `age_distribution` — 年龄分布（年龄段人数）
- `score_stats` — 积分汇总统计
- `count_by_range` — 通用区间计数
- `top_people_by_score` — 按积分排名
- `get_person_detail` — 单人明细

每个工具返回小体积结构化结果。

### 7.3 System Prompt 要点

- 说明数据集 schema 与年龄口径
- 中文、简洁回答，给出数字来源
- 工具覆盖不到的问题如实说明

### 7.4 对话循环（流式）

用户消息 → 发送流式请求（`stream: true`）→ 边接收边渲染 `delta.content`，同时按 index 累积 `delta.tool_calls` 的参数分片 → 流结束：

- 若存在 `tool_calls`：执行 handler，追加 assistant 消息（含 tool_calls）与 `role:"tool"` 结果，发起**新一轮流式请求**，循环直到无 tool_calls。
- 否则：完成。

**实现要点：**

- 用 `fetch` + `ReadableStream` 读取 `text/event-stream`，逐行解析 `data:` 事件，`data: [DONE]` 结束。
- 工具调用参数为分片 JSON，需按 `tool_calls[i].index` 拼接后再 `JSON.parse`。

### 7.5 对话记忆管理

- **多轮上下文**：维护 `messages` 会话历史数组，跨轮次累积，每轮请求携带完整历史，支持追问指代（如「那排名第二的单位呢？」）。
- **持久化**：历史存 `localStorage`，刷新 / 重开页面后恢复上次对话。
- **历史上限**：只保留最近 **10 轮对话**（一轮 = 一次用户提问 + 对应助手回答，含中间的 tool_calls 与 tool 结果），超出删除最早一轮（始终保留 system prompt）。
- **不做历史总结**（暂不实现）。
- **清空对话**按钮：手动重置内存历史与 `localStorage`。

## 8. 错误处理

- 导入失败 / 文件解析失败 → 错误提示
- 数据为空 → 各板块显示「暂无数据，请先导入」
- AI 未配置 / 无默认配置 → 提示先到「大模型管理」新增配置
- AI 请求失败 / 非 200 / 超时 → 聊天内报错
- 工具执行异常 → 返回错误给模型
- 空搜索 / 空筛选 → 显示「无匹配」

## 9. 关键决策汇总（可调整）

1. 年龄口径：`2026 − 出生年`
2. 积分分段：默认每 10 分
3. AI 采用流式输出（SSE）
4. 数据初始为空，首次导入初始化、之后增量导入（按公示编号 upsert）
5. 数据存 `localStorage`
