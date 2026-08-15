# Task 8 Report: AI 流式客户端 + System Prompt（TDD 解析部分）

## Status
DONE

## What was implemented

1. `frontend/src/ai/client.ts`
   - `ToolCallDelta` interface (index / id / type / function fields, all optional).
   - `accumulateToolCalls(existing, deltas)`: type-safe merge of SSE tool_call deltas keyed by `index`, concatenating `function.name` and `function.arguments` fragments.
   - `StreamOptions` / `StreamResult` interfaces.
   - `resolveUrl(baseUrl)`: trims trailing slashes, appends `/chat/completions` unless already present.
   - `streamChatCompletion(opts)`: `fetch` POST with `stream: true`, `ReadableStream` reader + `TextDecoder`, SSE line parsing (`data:` prefix, `[DONE]` sentinel, JSON.parse with try/catch), accumulates text deltas via `onDelta` and tool_call deltas, returns `{ content, toolCalls }`.

2. `frontend/src/ai/prompt.ts`
   - `SYSTEM_PROMPT`: Chinese data-analyst system prompt for the 北京市积分落户公示名单 dataset.

3. `frontend/src/ai/client.test.ts`
   - Single test covering `accumulateToolCalls` index-based fragment merging.

All code used verbatim from the brief (no `as any` hacks).

## Files changed
- `frontend/src/ai/client.ts` (created)
- `frontend/src/ai/prompt.ts` (created)
- `frontend/src/ai/client.test.ts` (created)

## TDD Evidence

### RED
Command: `cd frontend && npx vitest run src/ai/client.test.ts`
Result: FAIL — `Error: Failed to resolve import "./client" from "src/ai/client.test.ts". Does the file exist?`
(1 failed suite, 0 tests — client.ts did not exist yet.)

### GREEN
Command: `cd frontend && npx vitest run src/ai/client.test.ts`
Result: PASS — `✓ src/ai/client.test.ts (1 test) 1ms`, `Test Files 1 passed (1)`, `Tests 1 passed (1)`.

### Verification
- `npm run typecheck` → exit code 0 (vue-tsc --noEmit, strict + noUnusedLocals/noUnusedParameters).
- Full suite `npx vitest run` → 30 passed (7 files), no regressions.

## Self-review findings
- Confirmed the verbatim `accumulateToolCalls` implementation typechecks under strict mode; the `??` fallback object literal is accepted by the `Map<number, ToolCall>` setter with no widening error.
- `streamChatCompletion` typechecks (not unit-tested, as designed — needs a live gateway for manual verification).
- No unused locals/params; `ChatMessage` and `ToolCall` type imports are both used.
- `design/` directory (untracked, pre-existing, not part of this task) was left untouched.

## Concerns
- `streamChatCompletion` is not covered by automated tests (requires a live LLM gateway); the SSE parsing/streaming path is verified by typecheck only, per the brief's manual-verification note.
- The `accumulateToolCalls` merge appends `function.name` by concatenation (per the brief) — this matches the OpenAI SSE delta contract where name appears in a single fragment, but would mis-accumulate if a name were ever split across multiple deltas. Not a concern for the current gateway shape.

## Commit
- `24fcccf` feat: AI 流式客户端（SSE 解析 + tool_calls 累积）与 system prompt
