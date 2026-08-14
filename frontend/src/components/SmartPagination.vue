<script setup lang="ts">
import { computed } from 'vue'

/**
 * 智能分页：左侧「共 N 条，显示第 M-N 条」，右侧上一页 / 页码 / 下一页。
 * ≤7 页全显；>7 页首尾固定 + 当前页 ±1，中间以省略号折叠。
 *
 * @author demo
 * @since 2026-08-15
 */
interface Props {
  /** 总条数。 */
  total: number
  /** 每页条数。 */
  pageSize?: number
  /** 当前页码（从 1 开始）。 */
  currentPage: number
}

const props = withDefaults(defineProps<Props>(), {
  pageSize: 5,
})

const emit = defineEmits<{
  change: [page: number]
}>()

/** 总页数（至少 1 页）。 */
const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))

/** 当前页起始条号。 */
const start = computed(() => {
  if (props.total === 0) {
    return 0
  }
  return (props.currentPage - 1) * props.pageSize + 1
})

/** 当前页结束条号。 */
const end = computed(() => Math.min(props.currentPage * props.pageSize, props.total))

/** 页码序列（数字与省略号交替）。 */
const pages = computed<Array<number | string>>(() => {
  const count = totalPages.value
  if (count <= 7) {
    return Array.from({ length: count }, (_, i) => i + 1)
  }
  const set = new Set<number>([1, count])
  for (let i = props.currentPage - 1; i <= props.currentPage + 1; i++) {
    if (i >= 1 && i <= count) {
      set.add(i)
    }
  }
  const sorted = Array.from(set).sort((a, b) => a - b)
  const result: Array<number | string> = []
  let prev = 0
  for (const page of sorted) {
    if (prev !== 0 && page - prev > 1) {
      result.push('...')
    }
    result.push(page)
    prev = page
  }
  return result
})

/** 切换页码（越界与重复跳转忽略）。 */
function handleChange(page: number): void {
  if (page < 1 || page > totalPages.value || page === props.currentPage) {
    return
  }
  emit('change', page)
}

/** 页码点击（省略号忽略）。 */
function handlePageClick(item: number | string): void {
  if (typeof item === 'number') {
    handleChange(item)
  }
}
</script>

<template>
  <div class="smart-pagination">
    <span class="pagination-info">
      共 {{ total }} 条，显示第 {{ start }}-{{ end }} 条
    </span>
    <div class="pagination-controls">
      <button
        class="page-btn"
        :disabled="currentPage <= 1"
        @click="handleChange(currentPage - 1)"
      >
        上一页
      </button>
      <button
        v-for="(item, index) in pages"
        :key="index"
        class="page-btn"
        :class="{ active: item === currentPage, ellipsis: item === '...' }"
        :disabled="item === '...'"
        @click="handlePageClick(item)"
      >
        {{ item }}
      </button>
      <button
        class="page-btn"
        :disabled="currentPage >= totalPages"
        @click="handleChange(currentPage + 1)"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<style scoped>
.smart-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1rem;
  border-top: 1px solid var(--border-color);
}

.pagination-info {
  color: var(--text-secondary);
  font-size: 0.85rem;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.page-btn {
  min-width: 32px;
  height: 32px;
  padding: 0 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background-color: var(--bg-card);
  color: var(--text-primary);
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.page-btn:hover:not(:disabled):not(.ellipsis) {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.page-btn.active {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  color: #ffffff;
}

.page-btn.ellipsis {
  border: none;
  background: transparent;
  cursor: default;
  color: var(--text-muted);
}

.page-btn:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}
</style>
