import { ref, computed } from 'vue'
import type { Ref, ComputedRef } from 'vue'
import type { LlmConfig } from '../types'

const STORAGE_KEY = 'points-settlement:llmConfigs'

function load(): LlmConfig[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) as LlmConfig[] : []
  } catch {
    return []
  }
}

const configs = ref<LlmConfig[]>(load())

function genId(): string {
  return 'cfg-' + Math.random().toString(36).slice(2, 10)
}

export interface LlmConfigStore {
  configs: Ref<LlmConfig[]>
  defaultConfig: ComputedRef<LlmConfig | null>
  add: (config: Omit<LlmConfig, 'id' | 'isDefault'>) => LlmConfig
  update: (id: string, patch: Partial<LlmConfig>) => void
  remove: (id: string) => void
  setDefault: (id: string) => void
  clearAll: () => void
}

export function useLlmConfig(): LlmConfigStore {
  return {
    configs,
    defaultConfig: computed(() => configs.value.find(c => c.isDefault) ?? null),
    add(config) {
      const item: LlmConfig = { ...config, id: genId(), isDefault: configs.value.length === 0 }
      configs.value = [...configs.value, item]
      localStorage.setItem(STORAGE_KEY, JSON.stringify(configs.value))
      return item
    },
    update(id, patch) {
      configs.value = configs.value.map(c => (c.id === id ? { ...c, ...patch } : c))
      localStorage.setItem(STORAGE_KEY, JSON.stringify(configs.value))
    },
    remove(id) {
      configs.value = configs.value.filter(c => c.id !== id)
      if (!configs.value.some(c => c.isDefault) && configs.value[0]) configs.value[0].isDefault = true
      localStorage.setItem(STORAGE_KEY, JSON.stringify(configs.value))
    },
    setDefault(id) {
      configs.value = configs.value.map(c => ({ ...c, isDefault: c.id === id }))
      localStorage.setItem(STORAGE_KEY, JSON.stringify(configs.value))
    },
    clearAll() {
      configs.value = []
      localStorage.setItem(STORAGE_KEY, '[]')
    },
  }
}
