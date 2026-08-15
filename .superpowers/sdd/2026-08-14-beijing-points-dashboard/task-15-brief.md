### Task 15: 积分分布板块

**Files:**
- Create: `frontend/src/components/ScoreDistribution.vue`
- Modify: `frontend/src/App.vue`

**Interfaces:**
- Consumes: `useData`、`ChartBox`、`stats.ts`（`summarize`/`scoreDistribution`）。

- [ ] **Step 1: 实现 ScoreDistribution.vue**

要点：统计条（最高/最低/平均/中位数积分）+ 直方图（`scoreDistribution(records, 2)`，柱按人数最多高亮）+ 分数段统计表（分数段/人数/占比/分布条）。

- [ ] **Step 2: 挂载到 App.vue**

- [ ] **Step 3: 验证**：`npm run dev`，确认直方图与分数段表、占比计算正确、空数据守卫。

- [ ] **Step 4: Commit**

```bash
git add frontend/src/components/ScoreDistribution.vue frontend/src/App.vue
git commit -m "feat: 积分分布板块"
```

---

