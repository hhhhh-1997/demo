<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import request from '../../../utils/request'
import type { Result, SysMenu } from '../../../types'
import SidebarMenuItem from './SidebarMenuItem.vue'

/**
 * 侧边动态菜单：拉取菜单树并按权限/可见性渲染。
 *
 * @author demo
 * @since 2026-08-14
 */
const route = useRoute()
const menus = ref<SysMenu[]>([])

/** 加载当前用户可见的菜单树。 */
async function loadMenus(): Promise<void> {
  try {
    const res = (await request.get('/system/menu/routers')) as unknown as Result<SysMenu[]>
    menus.value = res.data ?? []
  } catch {
    // 失败提示已由 request 拦截器统一处理，此处回退为空菜单，避免未处理的 Promise 拒绝。
    menus.value = []
  }
}

onMounted(loadMenus)
</script>

<template>
  <el-menu
    class="sidebar-menu"
    :default-active="route.path"
    router
  >
    <SidebarMenuItem :items="menus" base-path="" />
  </el-menu>
</template>

<style scoped>
.sidebar-menu {
  height: 100%;
  border-right: none;
  background-color: transparent;
  padding: 16px 0;
}

/* 目录标题（menu-group-title） */
.sidebar-menu :deep(.el-sub-menu__title) {
  height: auto;
  line-height: 1.6;
  padding: 14px 20px;
  color: var(--text-primary);
  font-weight: 500;
}

.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background-color: var(--bg-hover);
  color: var(--text-primary);
}

.sidebar-menu :deep(.el-sub-menu__title > .el-icon) {
  font-size: 18px;
  color: var(--primary-color);
}

/* 折叠箭头（可旋转） */
.sidebar-menu :deep(.el-sub-menu__icon-arrow) {
  font-size: 12px;
  color: var(--text-muted);
  transition: transform 0.2s;
}

/* 菜单项（submenu-item） */
.sidebar-menu :deep(.el-menu-item) {
  height: auto;
  line-height: 1.6;
  padding: 10px 20px 10px 52px;
  color: var(--text-secondary);
  font-size: 14px;
  border-left: 3px solid transparent;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: var(--bg-hover);
  color: var(--text-primary);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(30, 136, 229, 0.15) 0%, transparent 100%);
  color: var(--primary-color);
  border-left-color: var(--primary-color);
}
</style>
