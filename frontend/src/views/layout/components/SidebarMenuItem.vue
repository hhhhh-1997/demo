<script setup lang="ts">
import type { SysMenu } from '../../../types'

/**
 * 动态菜单项：递归渲染目录（el-sub-menu）与菜单（el-menu-item）。
 * 仅展示 menuType 为 M/C 且 visible、status 均为启用的节点。
 *
 * @author demo
 * @since 2026-08-14
 */
const props = defineProps<{
  /** 当前层级的菜单节点。 */
  items: SysMenu[]
  /** 父级路径（用于拼接相对子路径）。 */
  basePath: string
}>()

/** 目录类型。 */
const MENU_TYPE_DIR = 'M'
/** 菜单类型。 */
const MENU_TYPE_MENU = 'C'
/** 启用标记。 */
const ENABLED = 1

/** 解析完整路径：绝对路径原样返回，相对路径拼接父路径。 */
function resolvePath(path: string, basePath: string): string {
  if (path.startsWith('/')) {
    return path
  }
  return `${basePath}/${path}`.replace(/\/{2,}/g, '/')
}

/** 过滤出可见、启用且为目录/菜单的节点。 */
function filterMenus(items: SysMenu[]): SysMenu[] {
  return items.filter(
    (item) =>
      (item.menuType === MENU_TYPE_DIR || item.menuType === MENU_TYPE_MENU) &&
      item.visible === ENABLED &&
      item.status === ENABLED,
  )
}
</script>

<template>
  <template v-for="item in filterMenus(props.items)" :key="item.id">
    <el-sub-menu
      v-if="item.menuType === MENU_TYPE_DIR"
      :index="resolvePath(item.path ?? '', props.basePath)"
    >
      <template #title>
        <el-icon v-if="item.icon">
          <component :is="item.icon" />
        </el-icon>
        <span>{{ item.menuName }}</span>
      </template>
      <SidebarMenuItem
        :items="item.children ?? []"
        :base-path="resolvePath(item.path ?? '', props.basePath)"
      />
    </el-sub-menu>
    <el-menu-item
      v-else
      :index="resolvePath(item.path ?? '', props.basePath)"
    >
      <el-icon v-if="item.icon">
        <component :is="item.icon" />
      </el-icon>
      <template #title>
        <span>{{ item.menuName }}</span>
      </template>
    </el-menu-item>
  </template>
</template>
