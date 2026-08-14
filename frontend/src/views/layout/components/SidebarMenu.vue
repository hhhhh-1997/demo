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

/** 加载菜单树。 */
async function loadMenus(): Promise<void> {
  const res = (await request.get('/system/menu/list')) as unknown as Result<SysMenu[]>
  menus.value = res.data ?? []
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
}

.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  color: var(--text-secondary);
}

.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background-color: var(--bg-hover);
  color: var(--text-primary);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  color: var(--primary-color);
}
</style>
