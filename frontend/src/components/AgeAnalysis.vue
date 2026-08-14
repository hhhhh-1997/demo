<script setup lang="ts">
import { computed } from 'vue'
import { useData } from '../stores/useData'
import ChartBox from './ChartBox.vue'
import { summarize, ageDistribution, ageMode } from '../utils/stats'
import type { EChartsOption } from 'echarts'

const { records } = useData()

const kpi = computed(() => {
  if (!records.value.length) return null
  const s = summarize(records.value)
  const mode = ageMode(records.value)
  return {
    total: s.total,
    ageMin: s.ageMin,
    ageMax: s.ageMax,
    ageMedian: s.ageMedian,
    ageAvg: s.ageAvg,
    mode,
  }
})

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

const insight = computed(() => {
  if (!kpi.value) return ''
  const { ageMin, ageMax, ageAvg, mode } = kpi.value
  return `落户人员年龄集中在 ${ageMin}–${ageMax} 岁之间，平均年龄 ${ageAvg.toFixed(1)} 岁。${mode.value} 岁是人数最多的年龄段（${mode.count} 人），40–48 岁为主要区间，符合积分落户对在京稳定就业年限的较高要求。`
})
</script>

<template>
  <div class="stat-strip cols-5" v-if="kpi">
    <div class="stat"><div class="s-label">落户人数</div><div class="s-value num">{{ kpi.total }}<span class="unit">人</span></div></div>
    <div class="stat"><div class="s-label">最小年龄</div><div class="s-value num">{{ kpi.ageMin }}<span class="unit">岁</span></div></div>
    <div class="stat"><div class="s-label">最大年龄</div><div class="s-value num">{{ kpi.ageMax }}<span class="unit">岁</span></div></div>
    <div class="stat"><div class="s-label">年龄中位数</div><div class="s-value num">{{ kpi.ageMedian }}<span class="unit">岁</span></div></div>
    <div class="stat"><div class="s-label">众数年龄</div><div class="s-value num">{{ kpi.mode.value }}<span class="unit">岁</span></div><div class="s-sub">（{{ kpi.mode.count }} 人）</div></div>
  </div>

  <div class="card" v-if="kpi">
    <div class="card-head">
      <h2 class="card-title">年龄分布直方图</h2>
      <div class="legend"><span class="sw a"></span>众数年龄段<span class="sw g"></span>其他</div>
    </div>
    <div class="chart-box">
      <ChartBox :option="option" />
    </div>
  </div>

  <div class="card" v-if="kpi">
    <div class="card-head"><h2 class="card-title">解读</h2></div>
    <div style="font-size:var(--text-sm);color:var(--text-body);line-height:1.8;">{{ insight }}</div>
  </div>
</template>
