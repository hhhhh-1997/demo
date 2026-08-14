import { ref, computed } from 'vue'
import type { Ref, ComputedRef } from 'vue'
import type { Record } from '../types'
import { mergeRecords } from '../data/importer'

const STORAGE_KEY = 'points-settlement:records'

function load(): Record[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) as Record[] : []
  } catch {
    return []
  }
}

function persist(records: Record[]): void {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(records))
}

const records = ref<Record[]>(load())

export interface DataStore {
  records: Ref<Record[]>
  isEmpty: ComputedRef<boolean>
  importData: (incoming: Record[]) => void
  addRecord: (r: Record) => void
  updateRecord: (id: string, patch: Partial<Record>) => void
  removeRecord: (id: string) => void
  clearAll: () => void
}

export function useData(): DataStore {
  return {
    records,
    isEmpty: computed(() => records.value.length === 0),
    importData(incoming) {
      records.value = mergeRecords(records.value, incoming)
      persist(records.value)
    },
    addRecord(r) {
      records.value = [...records.value, r]
      persist(records.value)
    },
    updateRecord(id, patch) {
      records.value = records.value.map(r => (r.id === id ? { ...r, ...patch } : r))
      persist(records.value)
    },
    removeRecord(id) {
      records.value = records.value.filter(r => r.id !== id)
      persist(records.value)
    },
    clearAll() {
      records.value = []
      persist(records.value)
    },
  }
}
