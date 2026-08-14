<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import type { NameValue } from '../../../types'

/**
 * 项目分类分布环形饼图（ECharts）。
 *
 * @author demo
 * @since 2026-08-15
 */
interface Props {
  /** 分类分布数据。 */
  data: NameValue[]
}

const props = defineProps<Props>()

const chartRef = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null

/** 构建环形饼图配置。 */
function buildOption(): echarts.EChartsOption {
  return {
    tooltip: { trigger: 'item' },
    legend: { top: '5%', left: 'center' },
    series: [
      {
        name: '项目分类',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderColor: 'transparent', borderWidth: 0 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
        data: props.data.map((item) => ({ name: item.name, value: item.value })),
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
  <div ref="chartRef" class="category-pie"></div>
</template>

<style scoped>
.category-pie {
  width: 100%;
  height: 320px;
}
</style>
