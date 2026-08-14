<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import type { NameValue } from '../../../types'

/**
 * 各单位项目分布柱状图（ECharts）。
 *
 * @author demo
 * @since 2026-08-15
 */
interface Props {
  /** 单位分布数据。 */
  data: NameValue[]
}

const props = defineProps<Props>()

const chartRef = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null

/** 构建柱状图配置。 */
function buildOption(): echarts.EChartsOption {
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: props.data.map((item) => item.name),
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '项目数',
        type: 'bar',
        data: props.data.map((item) => item.value),
      },
    ],
  }
}

/** 渲染图表。 */
function render(): void {
  chart?.setOption(buildOption())
}

/** 窗口缩放时自适应。 */
function handleResize(): void {
  chart?.resize()
}

onMounted(() => {
  if (chartRef.value) {
    chart = echarts.init(chartRef.value)
    render()
    window.addEventListener('resize', handleResize)
  }
})

watch(
  () => props.data,
  () => render(),
  { deep: true },
)

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
  chart = null
})
</script>

<template>
  <div ref="chartRef" class="dept-bar"></div>
</template>

<style scoped>
.dept-bar {
  width: 100%;
  height: 320px;
}
</style>
