### Task 10: 共享 UI（ECharts 封装 + Toast）

**Files:**
- Create: `frontend/src/components/ChartBox.vue`
- Create: `frontend/src/composables/useToast.ts`

**Interfaces:**
- Consumes: `theme.css`（Task 9）。
- Produces: `ChartBox`（props: `option: echarts.EChartsOption`；自动 init/resize/dispose）；`useToast()` 返回 `{ toast(msg): void }`（通过全局单例 DOM 元素）。

- [ ] **Step 1: 实现 ChartBox.vue**

创建 `frontend/src/components/ChartBox.vue`：

```vue
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
```

- [ ] **Step 2: 实现 useToast.ts**

创建 `frontend/src/composables/useToast.ts`：

```ts
let el: HTMLDivElement | null = null
let timer: ReturnType<typeof setTimeout> | null = null

export function useToast(): { toast: (msg: string) => void } {
  return {
    toast(msg) {
      if (!el) {
        el = document.createElement('div')
        el.className = 'toast'
        el.innerHTML = '<span id="toast-msg"></span>'
        document.body.appendChild(el)
      }
      el.querySelector('#toast-msg')!.textContent = msg
      el.classList.add('show')
      if (timer) clearTimeout(timer)
      timer = setTimeout(() => el?.classList.remove('show'), 2200)
    },
  }
}
```

- [ ] **Step 3: 验证**

Run: `cd frontend && npm run typecheck`
Expected: PASS（ChartBox 类型正确）。

- [ ] **Step 4: Commit**

```bash
git add frontend/src/components/ChartBox.vue frontend/src/composables/useToast.ts
git commit -m "feat: ECharts 封装组件与 toast 工具"
```

---

