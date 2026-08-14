let el: HTMLDivElement | null = null
let timer: ReturnType<typeof setTimeout> | null = null

export function useToast(): { toast: (msg: string) => void } {
  return {
    toast(msg) {
      if (!el) {
        el = document.createElement('div')
        el.className = 'toast'
        el.innerHTML = '<span id="toast-msg"></span>'
        document.body.appendChild(el)
      }
      el.querySelector('#toast-msg')!.textContent = msg
      el.classList.add('show')
      if (timer) clearTimeout(timer)
      timer = setTimeout(() => el?.classList.remove('show'), 2200)
    },
  }
}
