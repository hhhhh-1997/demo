import { defineStore } from 'pinia'
import { ref } from 'vue'

/** 主题类型。 */
type Theme = 'dark' | 'light'

/** localStorage 键。 */
const THEME_KEY = 'app-theme'

/** 从 localStorage 读取主题，仅合法值生效，缺省暗色。 */
function readTheme(): Theme {
  return localStorage.getItem(THEME_KEY) === 'light' ? 'light' : 'dark'
}

/**
 * 主题 store：默认暗色，切换时联动自定义 CSS 变量与 Element Plus 暗色，localStorage 持久化。
 *
 * @author demo
 * @since 2026-08-15
 */
export const useThemeStore = defineStore('theme', () => {
  const theme = ref<Theme>(readTheme())

  /** 将主题应用到 <html>：data-theme 驱动自定义变量，dark class 驱动 Element Plus 暗色。 */
  function apply(): void {
    const root = document.documentElement
    root.setAttribute('data-theme', theme.value)
    root.classList.toggle('dark', theme.value === 'dark')
  }

  /** 切换暗/亮主题并持久化。 */
  function toggle(): void {
    theme.value = theme.value === 'dark' ? 'light' : 'dark'
    localStorage.setItem(THEME_KEY, theme.value)
    apply()
  }

  /** 初始化：应用已持久化的主题（默认暗色）。 */
  function init(): void {
    apply()
  }

  return { theme, toggle, init }
})
