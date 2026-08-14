<script setup lang="ts">
import { ref } from 'vue'

const count = ref(0)
</script>

<template>
  <el-config-provider>
    <div class="app">
      <h1>Demo Frontend</h1>
      <p>Vue 3 + TypeScript + Vite + Element Plus</p>
      <el-space>
        <el-button type="primary" @click="count++">count is {{ count }}</el-button>
        <el-button>Default</el-button>
      </el-space>
    </div>
  </el-config-provider>
</template>

<style scoped>
.app {
  max-width: 640px;
  margin: 0 auto;
  padding: 2rem;
}
</style>
