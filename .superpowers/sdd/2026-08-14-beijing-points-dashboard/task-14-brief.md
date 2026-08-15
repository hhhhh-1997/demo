### Task 14: 单位分析板块

**Files:**
- Create: `frontend/src/components/UnitAnalysis.vue`
- Modify: `frontend/src/App.vue`

**Interfaces:**
- Consumes: `useData`、`ChartBox`、`stats.ts`（`topUnits`/`unitSizeDistribution`/`totalUnits`/`summarize`）。

- [ ] **Step 1: 实现 UnitAnalysis.vue**

要点：统计条（涉及单位总数/Top 单位入围人数/Top 单位名称/平均每单位人数）+ Top 单位横向条形图（Top 10/20 切换，ECharts `bar` 横向 `yAxis: type:'category'`）+ 单位规模分布。规模分布用 ECharts 横向条形图；底部加解读文案（复刻原型的 `renderUnitSize` 注释）。

- [ ] **Step 2: 挂载到 App.vue**

- [ ] **Step 3: 验证**：`npm run dev`，确认 Top 单位排名正确、Top 10/20 切换生效、规模分布正确、空数据守卫。

- [ ] **Step 4: Commit**

```bash
git add frontend/src/components/UnitAnalysis.vue frontend/src/App.vue
git commit -m "feat: 单位分析板块"
```

---

