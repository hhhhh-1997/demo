# Task 1 Report: 工程初始化（去除 Naive UI，接入 ECharts / xlsx / Vitest）

## What was implemented

Per the task brief, all 8 steps completed:

1. **Dependencies** (`frontend/package.json`): removed `naive-ui`, added `echarts ^5.6.0` + `xlsx ^0.18.5` to dependencies, added `jsdom ^26.0.0` + `vitest ^3.0.5` to devDependencies, and added a `test: "vitest run"` script. Content matches the brief verbatim.
2. **Vite config** (`frontend/vite.config.ts`): switched `defineConfig` import to `vitest/config`, dropped the `/api` proxy, added `test.environment: 'jsdom'`.
3. **main.ts** (`frontend/src/main.ts`): removed Naive UI plugin registration, added `import './styles/theme.css'`.
4. **Theme placeholder** (`frontend/src/styles/theme.css`): created with the placeholder comment for Task 9.
5. **index.html**: title changed to `北京市积分落户公示名单 · 数据分析`.
6. **App.vue**: replaced with the minimal `<div>加载中…</div>` shell.

## What was tested and results

- `npm install` — succeeded. `added 85 packages, removed 21 packages` (naive-ui and its tree removed). One deprecation warning (`whatwg-encoding@3.1.1`), non-blocking.
- `npm run typecheck` (`vue-tsc --noEmit`) — passed, no errors.
- `npm test` (`vitest run`) — command exists and runs (vitest v3.2.7 resolved from `^3.0.5`); reports `No test files found, exiting with code 1` as expected (tests arrive in Task 2).
- `npm run dev` — booted cleanly on http://localhost:5173 (Vite 6.4.3, ready in 153ms). Curl of `/` returned the HTML with the new title and `/src/main.ts` entry, no errors in the log. Dev server was stopped afterward.
- Grep confirmed no remaining `naive` references anywhere under `frontend/`.

## Files changed

- `frontend/package.json` (modified)
- `frontend/package-lock.json` (modified by `npm install`)
- `frontend/vite.config.ts` (modified)
- `frontend/src/main.ts` (modified)
- `frontend/src/App.vue` (modified)
- `frontend/index.html` (modified)
- `frontend/src/styles/theme.css` (created)

## Self-review findings

- Diff matches the brief verbatim for all file contents (package.json, vite.config.ts, main.ts, theme.css, index.html title, App.vue).
- Only files inside `frontend/` were staged/committed. The pre-existing untracked `design/` directory at repo root was left untouched.
- `noUnusedLocals`/`noUnusedParameters` (strict tsconfig) are satisfied — the minimal files introduce no unused imports.

## Concerns

- `npm test` exits with code 1 until Task 2 adds test files; this is expected and called out in the brief, but CI would treat it as a failure if wired before Task 2 lands.
- `vitest` resolved to `3.2.7` and `vite` to `6.4.3` (both within the brief's semver ranges), so the lockfile pins slightly newer patch versions than the ranges' minimums.
- `npm install` emitted a `whatwg-encoding@3.1.1` deprecation warning (transitive dep of jsdom); non-blocking.
