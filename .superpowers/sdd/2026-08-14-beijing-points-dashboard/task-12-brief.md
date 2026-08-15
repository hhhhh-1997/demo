### Task 12: 总览板块（KPI + 查询筛选 + 表格 + 详情 + 导入 + 增删改）

**Files:**
- Create: `frontend/src/components/Overview.vue`
- Modify: `frontend/src/App.vue`（挂载 Overview）

**Interfaces:**
- Consumes: `useData`（Task 4）、`useToast`（Task 10）、`stats.ts`（Task 2）、`parseWorkbook`（Task 3）、`Record`。
- Produces: 完整总览页。列：公示编号 / 姓名 / 出生年月 / 年龄 / 单位名称 / 积分分值。

- [ ] **Step 1: 实现 Overview.vue**

创建 `frontend/src/components/Overview.vue`。要点（模板结构对齐原型 `#page-overview`，类名用 theme.css 已有类）：

```vue
<script setup lang="ts">
import { ref, computed } from 'vue'
import { useData } from '../stores/useData'
import { useToast } from '../composables/useToast'
import { summarize, totalUnits, topUnits } from '../utils/stats'
import { parseWorkbook, deriveAge } from '../data/importer'
import type { Record } from '../types'

const { records, isEmpty, importData, addRecord, updateRecord, removeRecord, clearAll } = useData()
const { toast } = useToast()

const kpi = computed(() => {
  if (!records.value.length) return null
  const s = summarize(records.value)
  return {
    total: s.total, scoreAvg: s.scoreAvg, scoreMax: s.scoreMax, scoreMin: s.scoreMin,
    ageAvg: s.ageAvg, units: totalUnits(records.value), topUnit: topUnits(records.value, 1)[0],
  }
})

// 筛选
const keyword = ref('')
const ageMin = ref<number | null>(null)
const ageMax = ref<number | null>(null)
const scoreMin = ref<number | null>(null)
const scoreMax = ref<number | null>(null)

const filtered = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return records.value.filter(r => {
    if (kw && !(r.id.toLowerCase().includes(kw) || r.name.toLowerCase().includes(kw) || r.unit.toLowerCase().includes(kw))) return false
    if (ageMin.value != null && r.age < ageMin.value) return false
    if (ageMax.value != null && r.age > ageMax.value) return false
    if (scoreMin.value != null && r.score < scoreMin.value) return false
    if (scoreMax.value != null && r.score > scoreMax.value) return false
    return true
  })
})

// 分页
const pageSize = 10
const page = ref(0)
const paged = computed(() => filtered.value.slice(page.value * pageSize, (page.value + 1) * pageSize))
function resetPage() { page.value = 0 }

// 详情弹窗
const detail = ref<Record | null>(null)

// 导入
async function onImport(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const buf = await file.arrayBuffer()
  const incoming = parseWorkbook(buf)
  const before = records.value.length
  importData(incoming)
  toast(`已导入 ${incoming.length} 条${before ? `（增量合并，当前共 ${records.value.length} 条）` : ''}`)
  ;(e.target as HTMLInputElement).value = ''
}

// 新增/编辑表单
const editing = ref<Record | null>(null)
const form = ref({ id: '', name: '', birth: '', unit: '', score: 0 })
function openNew() { form.value = { id: '', name: '', birth: '', unit: '', score: 0 }; editing.value = { id: '', name: '', birth: '', unit: '', score: 0, age: 0 } }
function openEdit(r: Record) { form.value = { id: r.id, name: r.name, birth: r.birth, unit: r.unit, score: r.score }; editing.value = r }
function saveForm() {
  const patch: Record = {
    id: form.value.id, name: form.value.name, birth: form.value.birth,
    unit: form.value.unit, score: Number(form.value.score), age: deriveAge(form.value.birth),
  }
  if (editing.value && editing.value.id && records.value.some(r => r.id === editing.value!.id)) updateRecord(editing.value.id, patch)
  else addRecord(patch)
  editing.value = null
  toast('已保存')
}
</script>
```

模板结构（在 `<template>` 内，样式类与原型一致）：

```html
<div class="kpi-row" v-if="kpi">
  <div class="kpi"><div class="k-label">落户总人数</div><div class="k-value num">{{ kpi.total }}<span class="unit">人</span></div></div>
  <div class="kpi"><div class="k-label">平均积分</div><div class="k-value num">{{ kpi.scoreAvg.toFixed(2) }}<span class="unit">分</span></div></div>
  <div class="kpi"><div class="k-label">最高积分</div><div class="k-value num">{{ kpi.scoreMax.toFixed(2) }}<span class="unit">分</span></div></div>
  <div class="kpi"><div class="k-label">平均年龄</div><div class="k-value num">{{ kpi.ageAvg.toFixed(1) }}<span class="unit">岁</span></div></div>
  <div class="kpi"><div class="k-label">涉及单位</div><div class="k-value num">{{ kpi.units }}<span class="unit">家</span></div></div>
</div>

<div class="state-overlay" v-if="isEmpty">
  <div class="state-card">
    <h3>暂无数据</h3>
    <p>请先导入北京市积分落户公示名单 Excel 文件。</p>
    <button class="btn primary" @click="fileInput?.click()">导入 Excel</button>
  </div>
</div>

<div v-else>
  <div class="card">
    <div class="card-head">
      <h2 class="card-title">筛选与搜索</h2>
      <span class="card-hint">支持姓名 / 公示编号 / 单位名称模糊匹配</span>
    </div>
    <div class="filter-bar">
      <div class="f-group"><label>关键词搜索</label>
        <div class="f-search"><input v-model="keyword" placeholder="姓名 / 编号 / 单位" @input="resetPage" /></div>
      </div>
      <div class="f-group"><label>年龄区间</label>
        <div class="f-range"><input v-model.number="ageMin" type="number" placeholder="最小" /><span>—</span><input v-model.number="ageMax" type="number" placeholder="最大" /></div>
      </div>
      <div class="f-group"><label>积分区间</label>
        <div class="f-range"><input v-model.number="scoreMin" type="number" step="0.01" placeholder="最小" /><span>—</span><input v-model.number="scoreMax" type="number" step="0.01" placeholder="最大" /></div>
      </div>
      <div class="f-group"><label>&nbsp;</label>
        <div style="display:flex;gap:10px;">
          <button class="btn primary" @click="resetPage">查询</button>
          <button class="btn ghost" @click="keyword='';ageMin=null;ageMax=null;scoreMin=null;scoreMax=null;resetPage()">重置</button>
          <button class="btn secondary" @click="fileInput?.click()">导入 Excel</button>
          <button class="btn secondary" @click="openNew">新增记录</button>
          <button class="btn ghost" @click="clearAll(); toast('已清空')">清空数据</button>
        </div>
      </div>
    </div>
    <input ref="fileInput" type="file" accept=".xlsx,.xls" style="display:none" @change="onImport" />
  </div>

  <div class="card">
    <div class="card-head"><h2 class="card-title">公示名单明细</h2><span class="card-hint">共 {{ filtered.length }} 条</span></div>
    <div class="table-wrap">
      <table>
        <thead><tr><th>公示编号</th><th>姓名</th><th>出生年月</th><th class="r">年龄</th><th>单位名称</th><th class="r">积分分值</th><th></th></tr></thead>
        <tbody>
          <tr v-for="r in paged" :key="r.id" @click="detail = r">
            <td><span class="id">{{ r.id }}</span></td><td>{{ r.name }}</td><td class="num">{{ r.birth }}</td>
            <td class="r"><span class="age">{{ r.age }}</span></td><td><span class="unit-cell">{{ r.unit }}</span></td>
            <td class="r"><span class="score">{{ r.score.toFixed(2) }}</span></td>
            <td>
              <button class="btn ghost" @click.stop="openEdit(r)">编辑</button>
              <button class="btn ghost" @click.stop="removeRecord(r.id); toast('已删除')">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div class="pagination">
      <span class="info">显示第 {{ page * pageSize + 1 }}–{{ Math.min((page + 1) * pageSize, filtered.length) }} 条，共 {{ filtered.length }} 条</span>
      <div class="pg">
        <button :disabled="page <= 0" @click="page--">‹</button>
        <span class="mono">{{ page + 1 }} / {{ Math.max(1, Math.ceil(filtered.length / pageSize)) }}</span>
        <button :disabled="page >= Math.ceil(filtered.length / pageSize) - 1" @click="page++">›</button>
      </div>
    </div>
  </div>
</div>

<!-- 详情弹窗 -->
<div class="modal-mask" :class="{ show: detail }">
  <div class="modal">
    <div class="modal-h"><h3>人员详情</h3><button class="x" @click="detail = null">×</button></div>
    <div class="modal-b"><dl class="dl" v-if="detail">
      <dt>公示编号</dt><dd>{{ detail.id }}</dd><dt>姓名</dt><dd>{{ detail.name }}</dd>
      <dt>出生年月</dt><dd>{{ detail.birth }}</dd><dt>年龄</dt><dd>{{ detail.age }} 岁</dd>
      <dt>单位名称</dt><dd>{{ detail.unit }}</dd><dt>积分分值</dt><dd>{{ detail.score.toFixed(2) }} 分</dd>
    </dl></div>
    <div class="modal-f"><button class="btn primary" @click="detail = null">关闭</button></div>
  </div>
</div>

<!-- 新增/编辑弹窗 -->
<div class="modal-mask" :class="{ show: editing }">
  <div class="modal">
    <div class="modal-h"><h3>{{ editing?.id && records.some(r => r.id === editing.id) ? '编辑记录' : '新增记录' }}</h3><button class="x" @click="editing = null">×</button></div>
    <div class="modal-b">
      <div class="form-grid">
        <div class="f-field"><label>公示编号</label><input v-model="form.id" /></div>
        <div class="f-field"><label>姓名</label><input v-model="form.name" /></div>
        <div class="f-field"><label>出生年月</label><input v-model="form.birth" placeholder="YYYY-MM" /></div>
        <div class="f-field"><label>单位名称</label><input v-model="form.unit" /></div>
        <div class="f-field"><label>积分分值</label><input v-model.number="form.score" type="number" step="0.01" /></div>
      </div>
    </div>
    <div class="modal-f"><button class="btn ghost" @click="editing = null">取消</button><button class="btn primary" @click="saveForm">保存</button></div>
  </div>
</div>
```

（Vue 模板中用 `?` 需要 `fileInput` ref；在 script 中补 `const fileInput = ref<HTMLInputElement>()`。）

- [ ] **Step 2: 挂载到 App.vue**

在 `App.vue` 中 import Overview 并替换总览占位：

```vue
<script setup lang="ts">
import Overview from './components/Overview.vue'
// ...
</script>
<!-- template 中 -->
<Overview v-if="current === 'overview'" />
```

- [ ] **Step 3: 验证**

Run: `cd frontend && npm run dev`。导入 `../../docs/北京市2026年积分落户公示名单.xlsx`，确认：KPI 显示 6003、表格分页、搜索/筛选、行详情弹窗、新增/编辑/删除、清空、刷新后数据保留。

- [ ] **Step 4: Commit**

```bash
git add frontend/src/components/Overview.vue frontend/src/App.vue
git commit -m "feat: 总览板块（KPI + 查询筛选 + 表格 + 导入 + 增删改）"
```

---

