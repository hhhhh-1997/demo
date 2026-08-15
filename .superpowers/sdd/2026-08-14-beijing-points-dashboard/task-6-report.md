# Task 6 Report: AI 对话历史（10 轮裁剪 + 持久化）（TDD）

## Status: DONE

## What I implemented
- Created `frontend/src/ai/history.ts` (verbatim from brief):
  - `MAX_TURNS = 10`
  - `trimHistory(messages)`: keeps leading system message if present, then keeps only the last 10 turns (a "turn" anchored at each user message, carrying its following assistant/tool messages). Returns the input unchanged when there are 10 or fewer user messages.
  - `loadHistory()`: reads and parses `localStorage` key `points-settlement:chatHistory`, returns `[]` on missing/corrupt data.
  - `saveHistory(messages)`: persists `trimHistory(messages)` (already trimmed).
  - `clearHistory()`: removes the storage key.
- Created `frontend/src/ai/history.test.ts` (verbatim from brief), covering trim and persistence.

## Files changed
- `frontend/src/ai/history.ts` (new)
- `frontend/src/ai/history.test.ts` (new)

## TDD Evidence

### RED
Ran `npx vitest run src/ai/history.test.ts` before implementing `history.ts`:
```
FAIL  src/ai/history.test.ts [ src/ai/history.test.ts ]
Error: Failed to resolve import "./history" from "src/ai/history.test.ts". Does the file exist?
 Test Files  1 failed (1)
      Tests  no tests
```

### GREEN
Ran `npx vitest run src/ai/history.test.ts` after implementing:
```
 ✓ src/ai/history.test.ts (3 tests) 2ms
 Test Files  1 passed (1)
      Tests  3 passed (3)
```

### Typecheck
Ran `npm run typecheck` (`vue-tsc --noEmit`): PASS, no errors.

## Self-review findings
- Code matches the brief verbatim; no deviations.
- `trimHistory` uses a shared array for the `head` case: `messages[0]` is referenced (not copied) and the `slice` produces new elements, so no mutation risk given callers treat the result as read-only. Fine for this use.
- `loadHistory` wraps `JSON.parse` in try/catch, so corrupt localStorage returns `[]` — no `localStorage` access guarded by try/catch itself, but `localStorage` is present in jsdom; the brief specified this exact code, so left as-is.

## Concerns
- None material. `loadHistory` trusts parsed shape as `ChatMessage[]` without runtime validation (per brief, acceptable for this scope).

## Commit
- `2f5086c feat: AI 对话历史（10 轮裁剪 + localStorage）`
