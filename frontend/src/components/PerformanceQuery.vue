<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { fetchMonthly } from '../api/performance'
import { ROLES, TOP_DEPTS, currentMonth, monthOptions } from '../constants/dict'
import type { Performance } from '../types/performance'
import { fmtScore, gradeBadge } from '../utils/format'

type ColumnKind = 'text' | 'score' | 'grade'

interface Column {
  prop: string
  label: string
  kind: ColumnKind
  width: number
  fixed?: boolean
}

// 15 列展示顺序与原型一致（售前支撑为等级项，穿插在分值列之间）
const COLUMNS: Column[] = [
  { prop: 'realname', label: '姓名', kind: 'text', width: 120, fixed: true },
  { prop: 'month', label: '月份', kind: 'text', width: 100 },
  { prop: 'topDeptName', label: '一级部门', kind: 'text', width: 120 },
  { prop: 'deptName', label: '二级部门', kind: 'text', width: 140 },
  { prop: 'roleName', label: '岗位', kind: 'text', width: 110 },
  { prop: 'taskFinishRate', label: '任务完成率', kind: 'score', width: 120 },
  { prop: 'workEffectRate', label: '工作有效率', kind: 'score', width: 120 },
  { prop: 'workNormativity', label: '工作规范性', kind: 'score', width: 120 },
  { prop: 'learningImprovement', label: '学习及能力提升', kind: 'score', width: 120 },
  { prop: 'softwareDesign', label: '软需设计', kind: 'score', width: 120 },
  { prop: 'preSalesSupport', label: '售前支撑', kind: 'grade', width: 120 },
  { prop: 'bugCondition', label: '缺陷情况', kind: 'score', width: 120 },
  { prop: 'systemDesign', label: '概要设计', kind: 'score', width: 120 },
  { prop: 'codeReview', label: '代码评审', kind: 'score', width: 120 },
  { prop: 'testQuality', label: '测试产出质量', kind: 'grade', width: 120 },
]

const months = monthOptions()

const filters = reactive<{ topDeptId: number | null; role: string; month: string }>({
  topDeptId: null, // null = 全部（空值表达）
  role: '',        // '' = 全部
  month: currentMonth(), // 默认当前月（动态，当前 2026-08）
})

const rows = ref<Performance[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

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
  filters.month = currentMonth()
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
        <el-select v-model="filters.month" class="filter-select">
          <el-option label="全部" value="" />
          <el-option v-for="m in months" :key="m" :label="m" :value="m" />
        </el-select>
      </div>
      <div class="filter-actions">
        <el-button type="primary" @click="query">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
    </div>
  </section>

  <!-- 数据表格区 -->
  <section class="card">
    <div class="table-toolbar">
      <span class="result-count">共 <strong>{{ rows.length }}</strong> 条记录</span>
      <span class="result-hint">默认不分页 · 表头固定 · 左侧姓名列冻结</span>
    </div>

    <el-table
      v-if="!error"
      v-loading="loading"
      :data="rows"
      border
      height="62vh"
      class="perf-table"
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
  </section>
</template>
