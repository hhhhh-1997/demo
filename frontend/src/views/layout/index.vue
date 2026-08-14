<script setup lang="ts">
import HeaderBar from './components/HeaderBar.vue'
import SidebarMenu from './components/SidebarMenu.vue'

/**
 * 主框架布局：顶栏 + 左侧动态菜单 + 右侧内容区。
 *
 * @author demo
 * @since 2026-08-14
 */
</script>

<template>
  <el-container class="layout">
    <el-header class="layout-header">
      <HeaderBar />
    </el-header>
    <el-container class="layout-body">
      <el-aside class="layout-aside" width="220px">
        <SidebarMenu />
      </el-aside>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100vh;
}

.layout-header {
  padding: 0 1rem;
  background-color: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
}

.layout-body {
  height: calc(100vh - 60px);
}

.layout-aside {
  background-color: var(--bg-tertiary);
  border-right: 1px solid var(--border-color);
  overflow-x: hidden;
}

.layout-main {
  padding: 1rem;
  background-color: var(--bg-primary);
  overflow-y: auto;
}
</style>
