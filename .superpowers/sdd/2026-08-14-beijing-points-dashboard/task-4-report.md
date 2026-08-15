# Task 4 Report: 数据 Store（localStorage 持久化 + CRUD）

## Status
DONE

## What was implemented
Singleton data store at `frontend/src/stores/useData.ts` (no Pinia — a module-level `ref<Record[]>` initialized from localStorage), exposing `useData()` which returns `{ records, isEmpty, importData, addRecord, updateRecord, removeRecord, clearAll }`.

- `records` is a module-level `ref<Record[]>(load())`; all exported methods mutate it and call `persist()` to write to localStorage under key `points-settlement:records`.
- `load()` reads/parses localStorage with a try/catch fallback to `[]` on corrupt data.
- `importData` delegates dedup-by-id merging to `mergeRecords` (from `../data/importer`, Task 3).
- `updateRecord(id, patch)` maps and spreads `{ ...r, ...patch }`.
- `clearAll()` resets to `[]` and persists — also used by the test's `beforeEach` to reset the module-level singleton between tests (intentional, not just `localStorage.clear()`).

## Files changed
- Created `frontend/src/stores/useData.ts` (implementation, verbatim from brief)
- Created `frontend/src/stores/useData.test.ts` (4 test cases, verbatim from brief)

## TDD Evidence

### RED (Step 2)
Command: `cd frontend && npx vitest run src/stores/useData.test.ts`

```
 FAIL  src/stores/useData.test.ts [ src/stores/useData.test.ts ]
Error: Failed to resolve import "./useData" from "src/stores/useData.test.ts". Does the file exist?
 ...
 Test Files  1 failed (1)
      Tests  no tests
```

### GREEN (Step 4)
Command: `cd frontend && npx vitest run src/stores/useData.test.ts`

```
 ✓ src/stores/useData.test.ts (4 tests) 2ms

 Test Files  1 passed (1)
      Tests  4 passed (4)
```

### Typecheck
Command: `cd frontend && npm run typecheck` — clean, no output/errors.

## Self-review findings
- Imported `Ref`/`ComputedRef` as type-only to satisfy `strict` + `noUnusedLocals` (they are used in the `DataStore` interface). `computed`/`ref` runtime imports used.
- All test cases pass: initial empty, persist to localStorage, id-dedup import with latest-wins score, add/update/remove/clearAll.
- `localStorage.clear()` and `useData().clearAll()` both run in `beforeEach`, correctly resetting both raw storage and the in-memory singleton.

## Concerns
- None. The store is a module-level singleton; tests share the singleton state, but the `beforeEach` reset handles isolation. This is a known tradeoff of the "poor-man's store" approach (no Pinia) per the brief.

## Commit
- `926479a feat: 数据 store（localStorage 持久化 + CRUD）`
