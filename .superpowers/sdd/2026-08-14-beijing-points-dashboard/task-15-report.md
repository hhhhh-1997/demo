# Task 15 Report: 积分分布板块

## What I implemented

Created `frontend/src/components/ScoreDistribution.vue` and mounted it in `frontend/src/App.vue`, replacing the "积分分布（待实现）" placeholder for the `score` page.

The component:
- **统计条** (`stat-strip cols-4`): 最高积分 / 最低积分 / 平均积分 / 积分中位数, each from `summarize(records)` and formatted to 2 decimals (mirrors prototype `fmt(x, 2)`).
- **积分分布直方图**: ECharts bar built from `scoreDistribution(records.value, 2)`. `xAxis` category uses `bucket.label`, series data uses `bucket.count`. The max-count bucket is highlighted `#2C7CF5`, all others `#7BA9E8`. Uses `ChartBox` inside a `.chart-box`.
- **分数段统计表**: `<table>` with columns 分数段 / 人数 / 占比 / 分布. 占比 = `count/total*100` to 1 decimal; 分布 is a thin progress bar (track `var(--chart-track)`, inner bar width `count/maxCount*100%`, highlighted `var(--color-primary)` for the max bucket, `var(--chart-bar)` otherwise).
- **Empty guard**: a `kpi` computed returns `null` when `records` is empty, and the whole template is wrapped in `<template v-if="kpi">` (mirrors `AgeAnalysis.vue` / `UnitAnalysis.vue`).

## Files changed

- `frontend/src/components/ScoreDistribution.vue` (new)
- `frontend/src/App.vue` (import + mount, 1 line swapped)

## Verification

- `npm run typecheck` (vue-tsc --noEmit): passed.
- `npm run build` (vite build): passed (only the pre-existing chunk-size warning).
- `npm test` (vitest run): 7 files, 30 tests, all green.

## Self-review findings

- Legend copy in the histogram card uses `sw a` (人数最多) + `sw g` (其他), consistent with `AgeAnalysis.vue`'s legend pattern.
- `axisLabel: { interval: 0 }` shows every bucket label, matching the prototype's `xStep: 1`.
- Empty-data path: with no records, `kpi` is null so nothing renders; `buckets`/`maxCount`/`option` all degrade safely (empty arrays, no NaN) and the table is unreachable.
- `noUnusedLocals`/`noUnusedParameters` strict: no unused imports/vars introduced.

## Concerns

- `design/` and the staged `docs/*.xlsx` are untracked/staged in the repo but unrelated to this task; I did not touch or commit them. My commit only includes the two task files.
- The histogram with ~14 score buckets at full width could crowd x-axis labels on narrow screens; the prototype behaves the same way (all labels shown), so I kept it consistent.
