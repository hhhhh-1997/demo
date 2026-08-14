import { ref } from 'vue'
import type { Ref } from 'vue'

const STORAGE_KEY = 'points-settlement:theme'
type Theme = 'light' | 'dark'

function read(): Theme {
  return (localStorage.getItem(STORAGE_KEY) as Theme) || 'light'
}

function apply(theme: Theme): void {
  if (theme === 'dark') document.documentElement.setAttribute('data-color-scheme', 'dark')
  else document.documentElement.removeAttribute('data-color-scheme')
  localStorage.setItem(STORAGE_KEY, theme)
}

const theme = ref<Theme>(read())
apply(theme.value)

export function useTheme(): { theme: Ref<Theme>; toggle: () => void } {
  return {
    theme,
    toggle() {
      theme.value = theme.value === 'dark' ? 'light' : 'dark'
      apply(theme.value)
    },
  }
}
