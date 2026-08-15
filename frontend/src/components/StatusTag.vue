<script setup lang="ts">
/**
 * 状态徽标：半透明底色 + 彩色字（复刻原型 .badge-*）。
 *
 * @author demo
 * @since 2026-08-15
 */
type TagType = 'primary' | 'success' | 'warning' | 'danger' | 'info'

interface Props {
  /** 徽标配色。 */
  type?: TagType
}

withDefaults(defineProps<Props>(), {
  type: 'info',
})
</script>

<template>
  <span class="status-tag" :class="`status-tag--${type}`">
    <slot />
  </span>
</template>

<style scoped>
.status-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1;
}

.status-tag--primary {
  background: rgba(30, 136, 229, 0.2);
  color: var(--primary-color);
}

.status-tag--success {
  background: rgba(67, 160, 71, 0.2);
  color: var(--success-color);
}

.status-tag--warning {
  background: rgba(251, 140, 0, 0.2);
  color: var(--warning-color);
}

.status-tag--danger {
  background: rgba(229, 57, 53, 0.2);
  color: var(--danger-color);
}

.status-tag--info {
  background: rgba(0, 172, 193, 0.2);
  color: var(--info-color);
}
</style>
