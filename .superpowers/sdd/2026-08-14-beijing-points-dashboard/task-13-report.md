# Task 13 Report — 年龄分析板块

## Status: DONE

## What was implemented

Created `frontend/src/components/AgeAnalysis.vue` and mounted it in `App.vue`, replacing the "年龄分析（待实现）" placeholder.

- **Stat strip** (`.stat-strip.cols-5`) with 5 stat cards: 落户人数 / 最小年龄 / 最大年龄 / 年龄中位数 / 众数年龄. The 众数年龄 card carries a `.s-sub` line `（N 人）` matching the prototype.
- **Histogram card** (`.card` → `.card-head` with title + `.legend` swatches → `.chart-box` wrapping `ChartBox`). ECharts option used verbatim from the brief: category xAxis = ages, bar series with counts, mode age highlighted `#2C7CF5`, others `#7BA9E8`, `barMaxWidth: 24`, `tooltip: { trigger: 'axis' }`.
- **解读 card** with dynamic insight text mirroring the prototype's `#age-insight` copy.
- **Empty-data guard**: `kpi` computed returns `null` when `records` is empty; the stat strip, chart card, and insight card are all gated with `v-if="kpi"`, so nothing renders on empty data (chart is never mounted with empty `ageDistribution` / `ageMode`). This also avoids `summarize`'s `Math.min/Math.max([])` → `Infinity`/`-Infinity` edge case.

## Files changed

- `frontend/src/components/AgeAnalysis.vue` (new, 71 lines)
- `frontend/src/App.vue` (import `AgeAnalysis`; swap placeholder for `<AgeAnalysis v-else-if="current === 'age'" />`)

## Verification

- `npm run typecheck` (vue-tsc --noEmit): PASS — no output, exit 0.
- `npm run build` (vite build): PASS — 578 modules; only the pre-existing chunk-size advisory (echarts ~1.46 MB), no errors/warnings.
- `npm test` (vitest run): PASS — 7 files / 30 tests, no regression.
- `npm run dev` smoke check: root `/` returns 200; `/src/components/AgeAnalysis.vue` and `/src/App.vue` transform with HTTP 200 (SFCs compile at runtime, no compile error).

## Self-review findings

- ECharts option is verbatim from the brief, including `axisLabel: { interval: 1 }` (show every age label). With ~24 distinct ages on a 320px-wide chart this can crowd the x-axis labels, but it follows the brief's spec exactly (prototype used every-other label via `xStep: 2`; not applied here because the brief's option is authoritative).
- `ChartBox` renders its own `.chart-box` div with inline `height: 100%`, so it must be wrapped in a fixed-height `.chart-box` container (theme.css gives `.chart-box` `height: 320px`). The brief's template note ("`.chart-box` wrapping `ChartBox`") matches this; without the wrapper the chart would collapse to 0 height.
- Legend swatches reuse theme classes (`.sw.a` = `--color-primary` #2C7CF5, `.sw.g` = `--chart-bar` oklch blue), which is the prototype's approach. The chart itself uses the brief's hardcoded `#2C7CF5` / `#7BA9E8`, so the "其他" swatch color may not be pixel-identical to the bar color; cosmetic only and consistent with the prototype design.
- The insight text's "40–48 岁为主要区间" phrase is hardcoded, copied verbatim from the prototype (its own text was hardcoded the same way), while all numbers (min/max/avg/mode age, mode count) are interpolated dynamically.

## Concerns

- Could not drive a real browser in this environment, so the rendered chart (bar heights, mode-age highlight, tooltip) was not visually confirmed in a live DOM. Verified at compile, type, unit-test, and SFC-transform level only.
- The 40–48 岁 phrasing in the insight is static; if the underlying data's modal band changes, the text won't adapt. Faithful to the prototype and not in scope to fix.
