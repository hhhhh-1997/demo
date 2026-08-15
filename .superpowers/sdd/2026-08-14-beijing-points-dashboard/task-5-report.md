# Task 5 Report: 大模型配置 Store（TDD）

## What I Implemented

Created a singleton LLM-config store at `frontend/src/stores/useLlmConfig.ts` with:
- Module-level `const configs = ref<LlmConfig[]>(load())` persisted to localStorage under key `points-settlement:llmConfigs`.
- `load()` helper that safely parses localStorage (returns `[]` on parse error / absence).
- `genId()` producing `cfg-` prefixed ids.
- Exported `LlmConfigStore` interface and `useLlmConfig()` returning `{ configs, defaultConfig, add, update, remove, setDefault, clearAll }`.
- `add` accepts `Omit<LlmConfig, 'id' | 'isDefault'>`, marks first config as default (`isDefault: configs.value.length === 0`).
- `remove` re-assigns default to the first remaining config when none is default.
- `defaultConfig` is a computed: first config where `isDefault` is true, else `null`.

Also created the test file `frontend/src/stores/useLlmConfig.test.ts` (verbatim from the brief) covering: first-add-is-default, setDefault switch, and remove-persistence.

## Files Changed

- `frontend/src/stores/useLlmConfig.ts` (new)
- `frontend/src/stores/useLlmConfig.test.ts` (new)

## TDD Evidence

### RED

Command: `cd frontend && npx vitest run src/stores/useLlmConfig.test.ts`

Result: FAIL — `Failed to resolve import "./useLlmConfig" from "src/stores/useLlmConfig.test.ts". Does the file exist?` (test suite failed, 0 tests collected).

### GREEN

Command: `cd frontend && npx vitest run src/stores/useLlmConfig.test.ts`

Result: PASS — `Test Files 1 passed (1)`, `Tests 3 passed (3)`.

### Typecheck

Command: `cd frontend && npm run typecheck` (vue-tsc --noEmit)

Result: PASS — no output, exit 0.

## Self-Review Findings

- Verified the implementation matches the brief verbatim (no deviations).
- Confirmed `types.ts` already exports `LlmConfig` with all required fields (`id`, `name`, `baseUrl`, `apiKey`, `model`, `isDefault`), so the import resolves and typechecks under `strict`.
- `beforeEach` reset via `localStorage.clear()` + `useLlmConfig().clearAll()` works with the module-level singleton; tests pass in isolation and together.

## Concerns

- `Math.random()`-based id generation is not collision-proof; acceptable for this dashboard's scope but worth noting if ids become load-bearing later.
- Singleton module state means the store is shared across consumers (intended per brief); no SSR safety needed since this is a Vite/Vitest jsdom client-only app.

## Commit

- `a92491f` feat: 大模型配置 store（多配置 CRUD + 默认）
