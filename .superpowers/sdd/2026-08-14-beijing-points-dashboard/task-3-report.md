# Task 3 Report: Excel 导入解析（TDD）

## Status: DONE

## What was implemented

Created two files per the task brief, using the exact code verbatim:

- `frontend/src/data/importer.ts` — Excel 导入解析 module:
  - `deriveAge(birth, baseYear = 2026)` — 年龄 = 基准年 − 出生年（从 `出生年月` 字符串取前 4 位），非法输入返回 0。
  - `rowsToRecords(rows: unknown[][])` — 按 5 列表头（公示编号/姓名/出生年月/单位名称/积分分值）映射为 `Record[]`，积分用 `Number()` 强制转数字，并派生 `age`。
  - `mergeRecords(existing, incoming)` — 按 `id` upsert，Map 保持插入顺序（已存在则更新、不存在则追加到末尾）。
  - `parseWorkbook(buf: ArrayBuffer)` — SheetJS `xlsx` 读取第一个 sheet 并转 `unknown[][]`，委托给 `rowsToRecords`（thin wrapper，按 brief 不做单测）。
- `frontend/src/data/importer.test.ts` — 5 个测试用例覆盖 `deriveAge`（2）、`rowsToRecords`（2）、`mergeRecords`（2，实际 4 个 describe/it 共 5 断言）。

## Files changed

- `frontend/src/data/importer.ts` (new, 45 lines)
- `frontend/src/data/importer.test.ts` (new, 35 lines)

## Self-review findings

- Both files match the brief verbatim (diff reviewed against the brief — no drift).
- `import type { Record } from '../types'` intentionally shadows the global `Record<K,V>` utility type; `tsconfig` `strict` + `noUnusedLocals`/`noUnusedParameters` on and `npm run typecheck` passes — no unused imports/vars/params. The brief's unused `HEADER` const was not re-added.
- `parseWorkbook` uses `xlsx` (already a dependency, `^0.18.5`) and is not unit-tested by design.
- `mergeRecords` preserves Map insertion order, satisfying the "不存在则新增，保持现有顺序" test.

## Concerns

None. Note: `design/` was already an untracked directory in the working tree before this task and was intentionally left out of the commit (only the two task files were staged).

## TDD Evidence

### RED (before implementation)

Command:
```
cd /Users/jinyihui/IdeaProjects/demo/frontend && npx vitest run src/data/importer.test.ts
```

Failing output (exit code 1):
```
 RUN  v3.2.7 /Users/jinyihui/IdeaProjects/demo/frontend

⎯⎯⎯⎯⎯⎯ Failed Suites 1 ⎯⎯⎯⎯⎯⎯

 FAIL  src/data/importer.test.ts [ src/data/importer.test.ts ]
Error: Failed to resolve import "./importer" from "src/data/importer.test.ts". Does the file exist?
  Plugin: vite:import-analysis
  File: /Users/jinyihui/IdeaProjects/demo/frontend/src/data/importer.test.ts:2:55
  1  |  import { describe, it, expect } from "vitest";
  2  |  import { deriveAge, rowsToRecords, mergeRecords } from "./importer";
     |                                                          ^
...
 Test Files  1 failed (1)
      Tests  no tests
```

Expected per brief: FAIL with "Cannot find module './importer'" — confirmed (Vite reports "Failed to resolve import ... Does the file exist?", i.e. module missing).

### GREEN (after implementation)

Command:
```
cd /Users/jinyihui/IdeaProjects/demo/frontend && npx vitest run src/data/importer.test.ts
```

Passing output (exit code 0):
```
 RUN  v3.2.7 /Users/jinyihui/IdeaProjects/demo/frontend

 ✓ src/data/importer.test.ts (5 tests) 1ms

 Test Files  1 passed (1)
      Tests  5 passed (5)
```

Typecheck:
```
cd /Users/jinyihui/IdeaProjects/demo/frontend && npm run typecheck
```
Passed (no errors).

## Commit

`91be70c` feat: Excel 导入解析与增量合并
