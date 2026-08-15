# Task 7 Report: AI 工具集定义与执行（TDD）

## What was implemented

- Created `frontend/src/ai/tools.ts`:
  - `ToolDef` interface (name / description / parameters / handler).
  - `TOOLS: ToolDef[]` — 10 query tools: `search_by_name`, `search_by_unit`, `top_units`, `unit_size_distribution`, `score_distribution`, `age_distribution`, `score_stats`, `count_by_range`, `top_people_by_score`, `get_person_detail`.
  - `runTool(name, records, args): string` — throws `Error('未知工具: …')` on unknown tool, otherwise returns `JSON.stringify(handler(records, args ?? {}))`.
  - `openaiTools()` — maps `TOOLS` to OpenAI `tools` array shape (`{ type: 'function', function: { name, description, parameters } }`).
- Created `frontend/src/ai/tools.test.ts` — 4 tests (top_units, search_by_name, score_stats, unknown tool throws).

## Files changed

- `frontend/src/ai/tools.ts` (new)
- `frontend/src/ai/tools.test.ts` (new)

## TDD Evidence

### RED

`npx vitest run src/ai/tools.test.ts` before implementing `tools.ts`:

```
 FAIL  src/ai/tools.test.ts [ src/ai/tools.test.ts ]
Error: Failed to resolve import "./tools" from "src/ai/tools.test.ts". Does the file exist?
 Test Files  1 failed (1)
      Tests  no tests
```

### GREEN

After implementing `tools.ts`:

```
 ✓ src/ai/tools.test.ts (4 tests) 2ms
 Test Files  1 passed (1)
      Tests  4 passed (4)
```

Full suite (sanity):

```
 Test Files  6 passed (6)
      Tests  29 passed (29)
```

`npm run typecheck` (`vue-tsc --noEmit`): passes with no errors.

## Self-review findings / deviations from brief

The brief's `tools.ts` code was applied nearly verbatim, with two minimal changes required to satisfy `strict` + `noUnusedLocals`/`noUnusedParameters` typechecking:

1. `parameters: Record<string, unknown>` in the `ToolDef` interface collides with the imported domain type `import type { Record } from '../types'` (which shadows the built-in `Record<K, V>` utility type and is not generic — TS2315). Changed to the equivalent `parameters: { [key: string]: unknown }`.
2. In `count_by_range`'s handler, `r[a.field]` indexed the domain `Record` with an `any` key (TS7053 under strict). Changed to `const field = a.field as 'age' | 'score'` then `r[field]`, which preserves behavior.

Both changes are behavior-preserving; runtime output is identical to the brief's code. No functional logic was altered.

## Concerns

- None blocking. The brief's verbatim code as written does not pass `npm run typecheck` due to the `Record` name collision and the `any`-indexing line; documented deviations above are the minimal fixes. If the brief is updated to alias the import (e.g. `Record as PersonRecord`), the code could match more literally.

## Commit

- `9b0e431` feat: AI 固定工具集（10 个查询工具）
