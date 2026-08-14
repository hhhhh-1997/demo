<script setup lang="ts">
// 页面占位组件：业务页面在后续任务中替换为真实实现。
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <div class="placeholder">
    <h2>{{ route.meta.title ?? '页面建设中' }}</h2>
    <p>功能开发中，敬请期待。</p>
  </div>
</template>

<style scoped>
.placeholder {
  padding: 2rem;
  color: var(--text-primary);
}

.placeholder p {
  color: var(--text-secondary);
}
</style>
