<script setup lang="ts">
import { ref } from 'vue'

const count = ref(0)
</script>

<template>
  <n-config-provider>
    <div class="app">
      <h1>Demo Frontend</h1>
      <p>Vue 3 + TypeScript + Vite + Naive UI</p>
      <n-space>
        <n-button type="primary" @click="count++">count is {{ count }}</n-button>
        <n-button>Default</n-button>
      </n-space>
    </div>
  </n-config-provider>
</template>

<style scoped>
.app {
  max-width: 640px;
  margin: 0 auto;
  padding: 2rem;
}
</style>
