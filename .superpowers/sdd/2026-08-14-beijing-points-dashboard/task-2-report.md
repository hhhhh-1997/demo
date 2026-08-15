# Task 2 Report: 类型定义 + 统计聚合函数（TDD）

## Status: DONE

## What I implemented
- `frontend/src/types.ts` — data model interfaces: `Record`, `LlmConfig`, `ToolCall`, `ChatMessage` (verbatim from brief).
- `frontend/src/utils/stats.ts` — pure stats/aggregation functions: `median`, `summarize`, `ageDistribution`, `ageMode`, `topUnits`, `totalUnits`, `unitSizeDistribution`, `scoreDistribution`, plus exported bucket/summary interfaces (`KpiSummary`, `AgeBucket`, `UnitBucket`, `UnitSizeBucket`, `ScoreBucket`).
- `frontend/src/utils/stats.test.ts` — TDD test file (verbatim from brief).

## Files changed
- Created: `frontend/src/types.ts`
- Created: `frontend/src/utils/stats.ts`
- Created: `frontend/src/utils/stats.test.ts`

## TDD Evidence

### RED (before implementation)
Command: `cd frontend && npx vitest run src/utils/stats.test.ts`

Output:
```
 FAIL  src/utils/stats.test.ts [ src/utils/stats.test.ts ]
Error: Failed to resolve import "./stats" from "src/utils/stats.test.ts". Does the file exist?
  Plugin: vite:import-analysis
  File: /Users/jinyihui/IdeaProjects/demo/frontend/src/utils/stats.test.ts:5:7
   ...
 Test Files  1 failed (1)
      Tests  no tests
```

Why expected: the test imports `./stats`, which did not exist yet, so module resolution failed. This confirms the test is actually exercising the not-yet-written implementation.

### GREEN (after implementation)
Command: `cd frontend && npx vitest run src/utils/stats.test.ts`

Output:
```
 ✓ src/utils/stats.test.ts (9 tests) 2ms

 Test Files  1 passed (1)
      Tests  9 passed (9)
```

Typecheck: `npm run typecheck` (vue-tsc --noEmit) — passed with no output.

Note: the brief's Step 5 mentioned "8 个测试" but the test file actually contains 9 `it()` cases (median has 2). All 9 pass.

## Self-review findings
- All functions/signatures/interfaces copied verbatim from the brief; no extra functions or changed signatures.
- No unused imports/vars/params — `npm run typecheck` (strict + noUnusedLocals/noUnusedParameters) passes clean.
- `summarize`'s `avg` helper correctly guards `total === 0` (avoids 0/0 NaN), matching the brief's code.

## Concerns
- Empty-array edge cases are intentionally unhandled per the brief (e.g. `summarize([])` would yield `scoreMin/scoreMax = Infinity` from `Math.min/max(...[])`). The brief states this boundary is not in scope; upper-layer UI guards handle empty data.

---

## Fix Report (post-review)

### 缺陷修复（Important）
Coordinator review identified an inherent bug in the plan's `scoreDistribution`:

1. **空数组返回**：当所有记录的分数相同且恰为 `binSize` 的整数倍（如全部 score=120, binSize=2）时，`lo0 === hi0 === 120`，`for (lo = lo0; lo < hi0; ...)` 永不执行，返回 `[]`。这不是空 records 的情况（已有 `!records.length` 守卫），UI 守卫无法捕获。
2. **计数谓词优先级脆弱**：`r.score >= lo && r.score < hi || (hi === hi0 && r.score === hi)` 依赖 `&&` 高于 `||`，虽正确但易碎。

### 修改内容
`frontend/src/utils/stats.ts` 的 `scoreDistribution`：
- 新增 `if (lo0 === hi0)` 分支，直接返回单个 bucket `{ label: `${lo0}–${lo0 + binSize}`, lo: lo0, hi: lo0 + binSize, count: records.length }`。
- 计数谓词加括号：`(r.score >= lo && r.score < hi) || (hi === hi0 && r.score === hi)`。

`frontend/src/utils/stats.test.ts` 新增边界测试：
```ts
it('所有分数相同且为分段整数倍时不返回空', () => {
  const same = [rec('1', '1980-01', 'A', 120), rec('2', '1981-01', 'A', 120)]
  const d = scoreDistribution(same, 2)
  expect(d).toHaveLength(1)
  expect(d[0].count).toBe(2)
})
```

### 覆盖测试命令与输出
Command: `cd /Users/jinyihui/IdeaProjects/demo/frontend && npx vitest run src/utils/stats.test.ts`

```
 ✓ src/utils/stats.test.ts (10 tests) 2ms

 Test Files  1 passed (1)
      Tests  10 passed (10)
```

Typecheck: `npm run typecheck` (vue-tsc --noEmit) — passed with no output.

### 提交
- `e39bbb3` fix: scoreDistribution 对同分值输入返回空数组（2 files changed, 10 insertions, 1 deletion）
