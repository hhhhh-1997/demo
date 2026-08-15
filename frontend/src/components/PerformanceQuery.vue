<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { fetchMonthly } from '../api/performance'
import { ROLES, TOP_DEPTS, currentMonth } from '../constants/dict'
import type { Performance } from '../types/performance'
import { fmtScore, gradeBadge } from '../utils/format'

type ColumnKind = 'text' | 'score' | 'grade'
type SortOrder = 'ascending' | 'descending'

interface Column {
  prop: string
  label: string
  kind: ColumnKind
  width: number
  fixed?: boolean
  sortable?: boolean
}

// 15 列展示顺序与原型一致（售前支撑为等级项，穿插在分值列之间）
const COLUMNS: Column[] = [
  { prop: 'realname', label: '姓名', kind: 'text', width: 120, fixed: true },
  { prop: 'month', label: '月份', kind: 'text', width: 100 },
  { prop: 'topDeptName', label: '一级部门', kind: 'text', width: 120 },
  { prop: 'deptName', label: '二级部门', kind: 'text', width: 140 },
  { prop: 'roleName', label: '岗位', kind: 'text', width: 110 },
  { prop: 'taskFinishRate', label: '任务完成率', kind: 'score', width: 120, sortable: true },
  { prop: 'workEffectRate', label: '工作有效率', kind: 'score', width: 120, sortable: true },
  { prop: 'workNormativity', label: '工作规范性', kind: 'score', width: 120, sortable: true },
  { prop: 'learningImprovement', label: '学习及能力提升', kind: 'score', width: 120, sortable: true },
  { prop: 'softwareDesign', label: '软需设计', kind: 'score', width: 120, sortable: true },
  { prop: 'preSalesSupport', label: '售前支撑', kind: 'grade', width: 120 },
  { prop: 'bugCondition', label: '缺陷情况', kind: 'score', width: 120, sortable: true },
  { prop: 'systemDesign', label: '概要设计', kind: 'score', width: 120, sortable: true },
  { prop: 'codeReview', label: '代码评审', kind: 'score', width: 120, sortable: true },
  { prop: 'testQuality', label: '测试产出质量', kind: 'grade', width: 120 },
]

const filters = reactive<{ topDeptId: number | null; role: string; month: string | null }>({
  topDeptId: null, // null = 全部（空值表达）
  role: '',        // '' = 全部
  month: currentMonth(), // 默认当前月（动态，当前 2026-08）
})

const rows = ref<Performance[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

// 表头排序状态；null = 未排序（沿用后端自然顺序）
const sortState = ref<{ prop: string; order: SortOrder } | null>(null)

function handleSortChange({ prop, order }: { prop: string; order: SortOrder | null }) {
  sortState.value = order ? { prop, order } : null
}

// 可排序列均为分值项，非数值一律视为「无值」
function scoreValue(row: Performance, prop: string): number | null {
  const v = row[prop as keyof Performance]
  return typeof v === 'number' ? v : null
}

// NULL（无该项工作）恒排最后，不参与数值比较；0 是有效分值，正常排序
function compareScore(a: Performance, b: Performance, prop: string, dir: number): number {
  const av = scoreValue(a, prop)
  const bv = scoreValue(b, prop)
  if (av === null && bv === null) return 0
  if (av === null) return 1
  if (bv === null) return -1
  return av > bv ? dir : av < bv ? -dir : 0
}

// 前端排序：不改动后端返回原始顺序，仅对副本排序后展示
const displayRows = computed<Performance[]>(() => {
  if (!sortState.value) return rows.value
  const { prop, order } = sortState.value
  const dir = order === 'ascending' ? 1 : -1
  return [...rows.value].sort((a, b) => compareScore(a, b, prop, dir))
})

async function query() {
  loading.value = true
  error.value = null
  try {
    rows.value = await fetchMonthly({
      topDeptId: filters.topDeptId ?? undefined,
      role: filters.role || undefined,
      month: filters.month || undefined,
    })
  } catch (e) {
    // 不吞异常：错误信息透出到错误态
    error.value = e instanceof Error ? e.message : '接口请求异常，请稍后重试'
    rows.value = []
  } finally {
    loading.value = false
  }
}

function reset() {
  filters.topDeptId = null
  filters.role = ''
  filters.month = null
  query()
}

onMounted(query)
</script>

<template>
  <!-- 检索条件区 -->
  <section class="card">
    <div class="filter-row">
      <div class="form-control">
        <label class="form-label">一级部门</label>
        <el-select v-model="filters.topDeptId" class="filter-select" placeholder="全部">
          <el-option label="全部" :value="null" />
          <el-option v-for="d in TOP_DEPTS" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </div>
      <div class="form-control">
        <label class="form-label">岗位</label>
        <el-select v-model="filters.role" class="filter-select" placeholder="全部">
          <el-option label="全部" value="" />
          <el-option v-for="r in ROLES" :key="r.value" :label="r.label" :value="r.value" />
        </el-select>
      </div>
      <div class="form-control">
        <label class="form-label">月份</label>
        <el-date-picker
          v-model="filters.month"
          type="month"
          value-format="YYYY-MM"
          placeholder="全部"
          clearable
          class="filter-select"
        />
      </div>
      <div class="filter-actions">
        <el-button type="primary" @click="query">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
    </div>
  </section>

  <!-- 数据表格区 -->
  <section class="card table-card">
    <div class="table-toolbar">
      <span class="result-count">共 <strong>{{ rows.length }}</strong> 条记录</span>
      <span class="result-hint">默认不分页 · 表头固定 · 左侧姓名列冻结</span>
    </div>

    <div class="table-body">
      <el-table
        v-if="!error"
        v-loading="loading"
        :data="displayRows"
        border
        height="100%"
        class="perf-table"
        @sort-change="handleSortChange"
      >
      <template #empty>
        <el-empty description="暂无数据" :image-size="80">
          <el-button type="primary" @click="reset">重置条件</el-button>
        </el-empty>
      </template>

      <el-table-column
        v-for="c in COLUMNS"
        :key="c.prop"
        :prop="c.prop"
        :label="c.label"
        :width="c.width"
        :fixed="c.fixed ? 'left' : undefined"
        :sortable="c.sortable ? 'custom' : false"
        :align="c.kind === 'score' ? 'right' : 'left'"
      >
        <template v-if="c.kind === 'score'" #default="{ row }">
          <span :class="{ 'cell-muted': row[c.prop] == null }">{{ fmtScore(row[c.prop]) }}</span>
        </template>
        <template v-else-if="c.kind === 'grade'" #default="{ row }">
          <el-tag
            v-if="gradeBadge(row[c.prop]).type"
            :type="gradeBadge(row[c.prop]).type"
            size="small"
            round
          >
            {{ gradeBadge(row[c.prop]).text }}
          </el-tag>
          <span v-else class="cell-muted">-</span>
        </template>
      </el-table-column>
    </el-table>

      <el-result v-else status="error" title="查询失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="query">重试</el-button>
        </template>
      </el-result>
    </div>
  </section>
</template>
