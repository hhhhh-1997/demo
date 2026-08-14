<script setup lang="ts">
/**
 * 统计卡片：渐变图标卡片，用于页面顶部统计展示。
 *
 * @author demo
 * @since 2026-08-15
 */
type StatColor = 'primary' | 'success' | 'warning' | 'danger' | 'info'

interface Props {
  /** 卡片标题。 */
  title: string
  /** 卡片数值。 */
  value: number | string
  /** 渐变配色。 */
  color: StatColor
  /** Element Plus 图标组件名。 */
  icon: string
}

defineProps<Props>()
</script>

<template>
  <div class="stat-card" :class="color">
    <div class="stat-info">
      <h3 class="stat-value">{{ value }}</h3>
      <p class="stat-title">{{ title }}</p>
    </div>
    <div class="stat-icon">
      <el-icon><component :is="icon" /></el-icon>
    </div>
  </div>
</template>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.25rem 1.5rem;
  border-radius: 12px;
  color: #ffffff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.25);
}

.stat-card.primary {
  background: linear-gradient(135deg, var(--primary-color) 0%, #0d47a1 100%);
}

.stat-card.success {
  background: linear-gradient(135deg, var(--success-color) 0%, #1b5e20 100%);
}

.stat-card.warning {
  background: linear-gradient(135deg, var(--warning-color) 0%, #e65100 100%);
}

.stat-card.danger {
  background: linear-gradient(135deg, var(--danger-color) 0%, #b71c1c 100%);
}

.stat-card.info {
  background: linear-gradient(135deg, var(--info-color) 0%, #006064 100%);
}

.stat-value {
  margin: 0;
  font-size: 1.75rem;
  font-weight: 700;
  line-height: 1.2;
}

.stat-title {
  margin: 0.25rem 0 0;
  font-size: 0.85rem;
  opacity: 0.9;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  font-size: 22px;
}
</style>
