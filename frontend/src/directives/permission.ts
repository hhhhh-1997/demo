import type { Directive } from 'vue'
import { useUserStore } from '../stores/user'

/**
 * v-permission 权限指令：绑定权限标识后，若无该权限则移除元素。
 *
 * @author demo
 * @since 2026-08-14
 */
export const permission: Directive<HTMLElement, string> = {
  mounted(el, binding) {
    const userStore = useUserStore()
    const perm = binding.value
    if (perm && !userStore.hasPermi(perm)) {
      el.parentNode?.removeChild(el)
    }
  },
}
