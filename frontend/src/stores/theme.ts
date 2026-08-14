import { defineStore } from 'pinia'
import { ref } from 'vue'

export type ThemeMode = 'dark' | 'light'

export const useThemeStore = defineStore('theme', () => {
  const theme = ref<ThemeMode>((localStorage.getItem('app-theme') as ThemeMode) || 'dark')

  function apply() {
    document.documentElement.classList.toggle('dark', theme.value === 'dark')
    document.documentElement.setAttribute('data-theme', theme.value)
    localStorage.setItem('app-theme', theme.value)
  }

  function toggle() {
    theme.value = theme.value === 'dark' ? 'light' : 'dark'
    apply()
  }

  apply()

  return { theme, toggle, apply }
})
