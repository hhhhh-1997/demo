# SDD ledger — plan: docs/superpowers/plans/2026-08-14-beijing-points-dashboard.md

## Pre-flight rulings
- Ruling: 在现有 `dev1` 分支直接实现（非 main，spec/plan 已提交于此），不创建 worktree。成本：若需回滚可用 git 恢复；无分支隔离风险低（单人本地仓库）。
- Ruling: 计划 Task 3 的未使用 `HEADER` 常量、Task 7 的未使用 `ageMode`/`median` 导入会触发 `noUnusedLocals`（Task 18 typecheck 失败），已在计划中修复并提交。

## Pre-flight scan
| 任务对 | 接口/文件 | 检查结果 |
|---|---|---|
| 1→9 | `styles/theme.css` | Task1 建占位、Task9 填充，一致 |
| 2→3,4,7,12-17 | `types.ts`（Record/LlmConfig/ChatMessage/ToolCall） | 一致 |
| 2→7,13,14,15 | `stats.ts` 函数 | 一致 |
| 3→4 | `mergeRecords` | 一致 |
| 3→12 | `deriveAge`/`parseWorkbook` | 一致 |
| 4→12,16 | `useData` | 一致 |
| 5→16,17 | `useLlmConfig` | 一致 |
| 6→16 | `history.ts` | 一致 |
| 7→8,16 | `runTool`/`openaiTools` | 一致 |
| 8→16 | `streamChatCompletion` | 一致 |
| 9→10,11-17 | `theme.css` 类名 | 一致 |
| 10→13,14,15 | `ChartBox` | 一致 |
| 11→12-17 | `App.vue` 挂载 | 一致 |

## Deferred minors
- (pre-flight) `summarize`/`median` 对空数组未防护（`Infinity`/`undefined`）；UI 用 `v-if` 守卫，AI 工具在空数据时可能返回退化统计。低优先级，不阻塞。

## Tasks

### Task 1
- Task 1: complete (commits 42f20bb..d7a6cd2, review clean)
- Task 1: minor (deferred): `npm test` exits 1 with no test files（brief 固定，测试在 Task 2 落地）
- Task 1: minor (deferred): xlsx@0.18.5 有 CVE-2023-30533（brief 固定；客户端解析可信文件，暂接受）

### Task 2
- Ruling: `scoreDistribution` 在「所有分数相同且为 binSize 整数倍」时返回空数组（lo0===hi0 循环不执行）是真实 bug；spec 要求正确积分分段，故修复。成本：几乎为零，收益是避免下游积分图表在退化数据下渲染为空。
- Task 2: fix round 1/5 (2 addressed, 0 open — scoreDistribution 空数组 + 括号优先级; commits 99a6664..e39bbb3)
- Task 2: complete (commits d7a6cd2..e39bbb3, review clean after 1 fix round)

### Task 3
- Task 3: complete (commits e39bbb3..91be70c, review clean)
- Task 3: minor (deferred): `deriveAge` 空出生年月返回 2026（`Number('')===0`）——brief 固有；官方数据出生年月恒有，低影响。

### Task 4（pre-flight 修复）
- Ruling: Task 4/5 的 store 单例测试在 `it()` 之间共享模块级 `ref`，`beforeEach(localStorage.clear())` 不重置内存状态，会导致后序断言失败。已改为 beforeEach 调用 `useData().clearAll()` / `useLlmConfig().clearAll()`（useLlmConfig 新增 `clearAll()`）。成本：近乎零。

### Task 4
- Task 4: complete (commits 806e400..926479a, review clean)
- Task 4: minor (deferred): `load()` 未校验 JSON 形状（非数组会让后续 map/filter 抛错）；`isEmpty` 每次调用重建 computed；`updateRecord` 未 Omit id。均为低风险 polish，不阻塞。

### Task 5
- Task 5: complete (commits 926479a..a92491f, review clean)
- Task 5: minor (deferred): remove 就地变异与不可变重建风格不一致；setDefault 传不存在 id 会清空默认；update 可绕过 setDefault 破坏单默认不变量；load 未校验数组形状；genId 用 Math.random；「删除默认后首个转默认」无测试覆盖。均为防御性/继承性 polish。

### Task 6
- Task 6: complete (commits a92491f..2f5086c, review clean)
- Task 6: minor (deferred): saveHistory/clearHistory 未 try/catch（loadHistory 有）；loadHistory 未校验 JSON 形状；trim-on-save 路径无测试覆盖。均为 hardening，brief 继承。

### Task 7
- Ruling: 计划 tools.ts 有两处类型错误——`parameters: Record<string, unknown>` 被域名 `Record` 遮蔽（TS2315），`r[a.field]` 索引（TS7053）。实现者做最小修复（`{ [key:string]: unknown }` + `field as 'age'|'score'`），行为不变；已在计划中修复。
- Task 7: complete (commits 2f5086c..9b0e431, review clean; 2 verified type-fix deviations)
- Task 7: minor (deferred): openaiTools 无显式返回类型；仅 3/10 工具 + throw 有测试覆盖；handler args 用 any。均为 polish。

### Task 8（pre-flight）
- Ruling: 重写 `accumulateToolCalls` 为类型安全的 `ToolCallDelta`（避免 `__index`/`as any` 类型错误）；修复 client.ts 代码块缺失的关闭围栏（曾导致 task-brief 过度抽取到 Task 9-18）。已在计划中修复。
- Task 8: complete (commits c0f2cb5..24fcccf, review clean)
- Task 8: minor (deferred): 读循环结束后 `buffer` 未 flush；`function.name` 用拼接而非「若未设置则赋值」；`tools: unknown[]` 弱类型；测试未断言 id 合并。均为 robust/polish。

### Task 9
- Ruling: 原型 `body` 的 flex 布局在 Vue 下失效（布局容器是 `#app`），需删除 body 的 `display:flex;flex-direction:column;overflow:hidden` 并在 `#app` 上新增同款布局。实现者原样移植导致潜在布局错位，已派发修复。
- Task 9: fix round 1/5（待实现者修复后登记）
- Task 9: complete (commits 24fcccf..bf0f33b, review clean after 1 fix round)
- Task 9: minor (deferred): useTheme read() 未校验非法 localStorage 值；theme.css 全 2 空格缩进；模块顶层副作用（无 SSR，安全）。均为 polish。

### Task 10
- Task 10: complete (commits bf0f33b..72fb6e0, review clean)
- Task 10: minor (deferred): ResizeObserver 用 `as any` 挂 DOM；watch 无 deep（父组件须整体替换 option 对象——后续板块用 computed 返回新对象，安全）；ChartBox inline height:100% 覆盖 theme 的 320px。均为 polish。

### Task 11
- Task 11: complete (commits 72fb6e0..8b8d665, review clean)
- Task 11: minor (deferred): nav button 缺 type="button"（不在 form 内，无功能影响）；实现者未做 dev 浏览器目检（typecheck+build 已过）。

### Task 12
- Ruling: 实现者三处必要修复（均正确，属计划缺陷）：补 `fileInput` ref；`.state-overlay` 加 `show` 类（否则空态不可见）；edit-modal 标题用 `editing!.id` 解决 vue-tsc 严格空值。数据管线已验证 6003 行/KPI 正确，但未能驱动真实浏览器（交互留待 Task 18 集成验证）。
- Ruling: 审查发现 1 Critical + 2 Important（均计划缺陷，真实功能 bug）：(1) 空态导入按钮失效——`fileInput` input 在 v-else 内，空态时不在 DOM；(2) 区间筛选未 resetPage；(3) 清空区间输入产生空串 `''` 被强转为 0 过滤掉所有行。需修复。
- Task 12: fix round 1/5 (3 addressed, 0 open — 空态导入 + 分页重置 + 空串归一; commits 8b00bea..96d738d)
- Task 12: complete (commits 8b8d665..96d738d, review clean after 1 fix round)
- Task 12: minor (deferred): kpi.scoreMin/topUnit 死代码；空结果分页文案「1–0 条」；onImport 无 try/catch；无表单校验。均为 polish。

### Task 13
- Task 13: complete (commits 96d738d..6a0aaf0, review clean)
- Task 13: minor (deferred): 解读「40–48 岁」硬编码；「其他」图例 swatch 颜色与柱色 #7BA9E8 略不一致；解读卡用 inline style；ageMode 计算两次。均为 cosmetic。

### Task 14
- Task 14: complete (commits 6a0aaf0..c21fcec, review clean)
- Task 14: minor (deferred): 数字未千分位（原型用 toLocaleString）；sizeNote 硬编码 bucket 顺序索引；Top 单位名称无 ellipsis；Top 图无图例；unitSizeDistribution 计算两次。均为 cosmetic。

### Task 15
- Task 15: complete (commits c21fcec..50d7845, review clean)
- Task 15: minor (deferred): 「其他」legend/分布条用 var(--chart-bar) 与直方图 #7BA9E8 略不一致；并列最大分数段会全部高亮；空态无「暂无数据」提示。均为 cosmetic。

### Task 16
- Ruling: 实现者被用户手动停止，留下了未提交但完整的 AiAssistant.vue（189 行）+ App.vue 改动。用户指示「审查+提交现有工作」，我已核实其正确（流式循环/工具调用/历史/配置选择均正确，额外做了 visibleMessages 过滤、滚动、错误处理）并提交为 9503a10。
- Ruling: 复查确认流式渲染未真正生效——`onDelta` 里 `assistant.content += d` 改的是 push 进 `messages.value` 前的 raw 对象（Vue 3 响应式陷阱：push 后数组存响应式代理、本地变量仍指 raw，改 raw 不触发 re-render），文字会等 `busy=false` 才一次性渲染而非流式。已实证（watch 触发计数：raw 突变 0 次 / 代理突变 1 次）。已修复：`assistant` 改用 `reactive<ChatMessage>(...)`。
- Task 16: fix round 1/5 (1 addressed, 0 open — 流式响应式; commit 8888ed6)
- Task 16: complete (commits 9503a10..8888ed6, review clean after 1 fix round)
- Task 16: minor (deferred): 工具调用中间轮 assistant 消息 content 为空仍渲染空气泡（仅「调用工具：xxx」）；suggest/clear 按钮缺 type="button"。均为 cosmetic。

### Task 17
- Task 17: complete (commits 8888ed6..66948f7, review clean)
- Task 17: minor (deferred): 删除配置无二次确认；保存仅校验非空、未校验 Base URL 格式；API Key 明文存 localStorage（help 文案已提示，设计固有）。均为 polish。
