<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'

const props = defineProps<{ option: EChartsOption }>()
const el = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null

onMounted(() => {
  if (!el.value) return
  chart = echarts.init(el.value)
  chart.setOption(props.option)
  const ro = new ResizeObserver(() => chart?.resize())
  ro.observe(el.value)
  ;(el.value as any).__ro = ro
})

watch(() => props.option, (o) => chart?.setOption(o, true))

onBeforeUnmount(() => {
  ;(el.value as any)?.__ro?.disconnect()
  chart?.dispose()
  chart = null
})
</script>

<template>
  <div ref="el" class="chart-box" style="width: 100%; height: 100%;"></div>
</template>
