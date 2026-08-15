### Task 13: 年龄分析板块

**Files:**
- Create: `frontend/src/components/AgeAnalysis.vue`
- Modify: `frontend/src/App.vue`

**Interfaces:**
- Consumes: `useData`、`ChartBox`、`stats.ts`（`summarize`/`ageDistribution`/`ageMode`）。

- [ ] **Step 1: 实现 AgeAnalysis.vue**

要点：统计条（落户人数/最小年龄/最大年龄/年龄中位数/众数年龄）+ 直方图（`ChartBox`，ECharts bar）+ 解读文案。ECharts option：

```ts
const option = computed<EChartsOption>(() => {
  const dist = ageDistribution(records.value)
  const mode = ageMode(records.value)
  return {
    grid: { left: 48, right: 16, top: 24, bottom: 40 },
    xAxis: { type: 'category', data: dist.map(d => d.age), axisLabel: { interval: 1 } },
    yAxis: { type: 'value' },
    tooltip: { trigger: 'axis' },
    series: [{
      type: 'bar', barMaxWidth: 24,
      data: dist.map(d => ({ value: d.count, itemStyle: { color: d.age === mode.value ? '#2C7CF5' : '#7BA9E8' } })),
    }],
  }
})
```

模板结构对齐原型 `#page-age`（`.stat-strip.cols-5` + `.card` + `.chart-box` + 解读卡）。

- [ ] **Step 2: 挂载到 App.vue**

- [ ] **Step 3: 验证**：`npm run dev`，确认直方图渲染、众数年龄段高亮、统计条正确、空数据时不渲染图表（用 `v-if` 守卫）。

- [ ] **Step 4: Commit**

```bash
git add frontend/src/components/AgeAnalysis.vue frontend/src/App.vue
git commit -m "feat: 年龄分析板块"
```

---

