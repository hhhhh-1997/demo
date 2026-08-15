<script setup lang="ts">
/**
 * 统计卡片：卡片底 + 彩色大数字 + 渐变圆角图标块，用于页面顶部统计展示。
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
  /** 配色。 */
  color: StatColor
  /** FontAwesome 图标类名（如 fa-database）。 */
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
      <i class="fas" :class="icon"></i>
    </div>
  </div>
</template>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow);
  border-color: var(--primary-color);
}

.stat-value {
  margin: 0 0 4px;
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
}

.stat-title {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 12px;
  font-size: 28px;
  flex-shrink: 0;
}

.stat-card.primary .stat-value {
  color: var(--primary-color);
}

.stat-card.primary .stat-icon {
  background: linear-gradient(135deg, rgba(30, 136, 229, 0.2) 0%, rgba(30, 136, 229, 0.1) 100%);
  color: var(--primary-color);
}

.stat-card.success .stat-value {
  color: var(--success-color);
}

.stat-card.success .stat-icon {
  background: linear-gradient(135deg, rgba(67, 160, 71, 0.2) 0%, rgba(67, 160, 71, 0.1) 100%);
  color: var(--success-color);
}

.stat-card.warning .stat-value {
  color: var(--warning-color);
}

.stat-card.warning .stat-icon {
  background: linear-gradient(135deg, rgba(251, 140, 0, 0.2) 0%, rgba(251, 140, 0, 0.1) 100%);
  color: var(--warning-color);
}

.stat-card.danger .stat-value {
  color: var(--danger-color);
}

.stat-card.danger .stat-icon {
  background: linear-gradient(135deg, rgba(229, 57, 53, 0.2) 0%, rgba(229, 57, 53, 0.1) 100%);
  color: var(--danger-color);
}

.stat-card.info .stat-value {
  color: var(--info-color);
}

.stat-card.info .stat-icon {
  background: linear-gradient(135deg, rgba(0, 172, 193, 0.2) 0%, rgba(0, 172, 193, 0.1) 100%);
  color: var(--info-color);
}
</style>
