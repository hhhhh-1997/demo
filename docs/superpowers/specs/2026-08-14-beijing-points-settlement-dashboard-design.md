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
- 数据从官方 Excel 加载，真实准确
- 搜索、筛选、图表、AI 问数

**在范围外（明确不做）：**

- 后端服务 / API
- 数据写入与编辑
- 用户登录 / 权限
- 移动端适配（桌面端优先）
- AI 流式输出（v1 非流式，留作后续优化）

## 3. 技术选型（已确定）

| 项 | 选择 |
|---|---|
| 框架 | 现有 Vue 3 + TypeScript + Vite 脚手架 |
| UI 组件 | Naive UI（已依赖） |
| 图表 | ECharts（新增依赖） |
| 数据加载 | 构建时 xlsx → JSON，产物随静态资源打包 |
| AI 调用 | OpenAI 兼容 `/v1/chat/completions`，支持 `tools`（function calling） |
| AI 数据访问 | 方案 A：固定工具集 |

## 4. 架构

单页应用，左侧边栏导航 + 右侧内容区，共 5 个板块。

```
frontend/
  scripts/convert-xlsx.mjs        # 构建时 xlsx → JSON 转换脚本（devDependency: xlsx）
  public/data/records.json        # 转换产物，运行时 fetch
  src/
    types.ts                      # Record 接口
    data/loader.ts                # fetch + 解析 + 派生字段（age）
    utils/stats.ts                # 聚合/分布计算（纯函数，供图表与 AI 工具共用）
    stores/useData.ts             # 全局数据状态
    components/
      DataQuery.vue               # 板块1：数据查询
      AgeAnalysis.vue             # 板块2：年龄分析
      UnitAnalysis.vue            # 板块3：单位分析
      ScoreDistribution.vue       # 板块4：积分分布
      AiAssistant.vue             # 板块5：AI 助手
    ai/
      client.ts                   # OpenAI 兼容请求封装
      tools.ts                    # 工具定义 + handler
      prompt.ts                   # system prompt
```

## 5. 数据层

- **数据源**：`docs/北京市2026年积分落户公示名单.xlsx`（6003 条，5 列：公示编号、姓名、出生年月、单位名称、积分分值）。
- **转换**：`scripts/convert-xlsx.mjs` 用 SheetJS（`xlsx` 依赖）读取 Excel，输出 `public/data/records.json`；挂在 `prebuild`/`predev` 脚本上，构建/开发前自动执行。
- **数据模型**：`{ id, name, birth, unit, score }`，加载时派生 `age`。
- **年龄口径（决策）**：`age = 2026 − 出生年`（以公示年份 2026 为基准，月不参与；出生年月格式为 YYYY-MM）。

## 6. 五个板块

### 6.1 数据查询

- 搜索框：姓名 / 公示编号 / 单位名称模糊匹配
- 筛选：年龄区间、积分区间
- `n-data-table` 展示（分页，6003 行）
- 行点击弹出详情

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
- 右上角设置：Base URL / API Key / 模型名，存 `localStorage`

## 7. AI 助手（方案 A：固定工具集）

### 7.1 调用方式

`POST {baseUrl}/chat/completions`，OpenAI 兼容，请求体含 `messages` 与 `tools`。baseUrl 为用户填写的基地址（如 `https://…/v1`），客户端自动补 `/chat/completions`。

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

### 7.4 对话循环

用户消息 → 请求 → 若返回 `tool_calls` 则执行 handler、把结果以 `role:"tool"` 追加、再请求 → 得到最终回答渲染。

**决策：v1 非流式**（每轮一次完整请求，简单可靠）。

## 8. 错误处理

- 数据加载失败 → 错误态 + 重试按钮
- AI 未配置 → 提示先填 Base URL / Key
- AI 请求失败 / 非 200 / 超时 → 聊天内报错
- 工具执行异常 → 返回错误给模型
- 空搜索 / 空筛选 → 显示「无匹配」

## 9. 关键决策汇总（可调整）

1. 年龄口径：`2026 − 出生年`
2. 积分分段：默认每 10 分
3. AI v1 非流式
