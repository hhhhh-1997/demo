import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import { permission } from './directives/permission'
import { useThemeStore } from './stores/theme'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import './styles/variables.css'
import './styles/index.css'
import App from './App.vue'

const app = createApp(App)

app.directive('permission', permission)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus)

useThemeStore(pinia).init()

app.mount('#app')
