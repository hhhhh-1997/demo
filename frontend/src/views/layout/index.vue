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
    <el-header class="layout-header" height="64px">
      <HeaderBar />
    </el-header>
    <el-container class="layout-body">
      <el-aside class="layout-aside" width="280px">
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
  padding: 0 24px;
  background: linear-gradient(135deg, var(--bg-secondary) 0%, var(--bg-primary) 100%);
  border-bottom: 1px solid var(--border-color);
  box-shadow: var(--shadow);
  z-index: 100;
}

.layout-body {
  height: calc(100vh - 64px);
}

.layout-aside {
  background-color: var(--bg-primary);
  border-right: 1px solid var(--border-color);
  overflow-x: hidden;
}

.layout-main {
  padding: 24px;
  background-color: var(--bg-tertiary);
  overflow-y: auto;
}
</style>
