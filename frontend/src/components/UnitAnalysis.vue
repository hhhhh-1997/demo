<script setup lang="ts">
import { ref, computed } from 'vue'
import { useData } from '../stores/useData'
import ChartBox from './ChartBox.vue'
import { summarize, topUnits, totalUnits, unitSizeDistribution } from '../utils/stats'
import type { EChartsOption } from 'echarts'

const { records } = useData()

const topN = ref(20)

const kpi = computed(() => {
  if (!records.value.length) return null
  const s = summarize(records.value)
  const units = totalUnits(records.value)
  const top = topUnits(records.value, 1)[0]
  return {
    totalUnits: units,
    topCount: top.count,
    topName: top.unit,
    avgPerUnit: s.total / units,
  }
})

const topOption = computed<EChartsOption>(() => {
  const data = topUnits(records.value, topN.value)
  const names = data.map(d => d.unit).reverse()
  const counts = data.map(d => d.count).reverse()
  return {
    grid: { left: 8, right: 40, top: 8, bottom: 8, containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: names, axisLabel: { width: 200, overflow: 'truncate' } },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    series: [{
      type: 'bar', barMaxWidth: 20,
      data: counts.map((c, i) => ({ value: c, itemStyle: { color: i === counts.length - 1 ? '#2C7CF5' : '#7BA9E8' } })),
    }],
  }
})

const sizeOption = computed<EChartsOption>(() => {
  const buckets = unitSizeDistribution(records.value)
  const labels = buckets.map(b => b.label).reverse()
  const units = buckets.map(b => b.units).reverse()
  return {
    grid: { left: 8, right: 40, top: 8, bottom: 8, containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: labels },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    series: [{
      type: 'bar', barMaxWidth: 20,
      data: units.map((v, i) => ({ value: v, itemStyle: { color: i === units.length - 1 ? '#2C7CF5' : '#7BA9E8' } })),
    }],
  }
})

const sizeNote = computed(() => {
  if (!records.value.length) return null
  const buckets = unitSizeDistribution(records.value)
  const total = totalUnits(records.value)
  const pct = total ? ((buckets[0].units / total) * 100).toFixed(1) : '0.0'
  const big = buckets[buckets.length - 1]
  return { pct, bigUnits: big.units, bigPeople: big.people }
})
</script>

<template>
  <template v-if="kpi">
    <div class="stat-strip cols-4">
      <div class="stat"><div class="s-label">涉及单位总数</div><div class="s-value num">{{ kpi.totalUnits }}<span class="unit">家</span></div></div>
      <div class="stat"><div class="s-label">Top 单位入围人数</div><div class="s-value num">{{ kpi.topCount }}<span class="unit">人</span></div></div>
      <div class="stat">
        <div class="s-label">Top 单位名称</div>
        <div class="s-value" style="font-size:16px;font-family:var(--font-family);font-weight:var(--weight-medium);line-height:1.5;color:var(--text-heading);" :title="kpi.topName">{{ kpi.topName }}</div>
      </div>
      <div class="stat"><div class="s-label">平均每单位人数</div><div class="s-value num">{{ kpi.avgPerUnit.toFixed(2) }}<span class="unit">人</span></div></div>
    </div>

    <div class="grid-2">
      <div class="card">
        <div class="card-head">
          <h2 class="card-title">入围人数 Top 单位</h2>
          <select v-model.number="topN" style="height:32px;padding:0 8px;border:1px solid var(--border-color);border-radius:6px;background:var(--bg-input);font-size:var(--text-13);color:var(--text-body);">
            <option :value="10">Top 10</option>
            <option :value="20">Top 20</option>
          </select>
        </div>
        <div class="chart-box" style="height:520px;">
          <ChartBox :option="topOption" />
        </div>
      </div>

      <div class="card">
        <div class="card-head"><h2 class="card-title">单位规模分布</h2></div>
        <div class="chart-box" style="height:460px;">
          <ChartBox :option="sizeOption" />
        </div>
        <div style="margin-top:14px;padding-top:14px;border-top:1px solid var(--border-color);font-size:var(--text-13);color:var(--text-secondary);line-height:1.8;" v-if="sizeNote">
          <b style="color:var(--text-heading);font-weight:var(--weight-medium);">{{ sizeNote.pct }}%</b> 的单位仅 1 人入围；仅 <b style="color:var(--text-heading);font-weight:var(--weight-medium);">{{ sizeNote.bigUnits }} 家</b>单位入围 20 人以上（共 {{ sizeNote.bigPeople }} 人）。
        </div>
      </div>
    </div>
  </template>
</template>
