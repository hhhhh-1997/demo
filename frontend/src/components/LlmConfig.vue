<script setup lang="ts">
import { ref } from 'vue'
import { useLlmConfig } from '../stores/useLlmConfig'
import { useToast } from '../composables/useToast'
import { TOOLS } from '../ai/tools'
import type { LlmConfig } from '../types'

const { configs, add, update, remove, setDefault } = useLlmConfig()
const { toast } = useToast()

const editing = ref<string | null>(null)
const showKey = ref(false)

const emptyForm = (): Omit<LlmConfig, 'id' | 'isDefault'> => ({
  name: '',
  baseUrl: '',
  apiKey: '',
  model: '',
})
const form = ref(emptyForm())

function openEdit(c: LlmConfig) {
  editing.value = c.id
  form.value = { name: c.name, baseUrl: c.baseUrl, apiKey: c.apiKey, model: c.model }
  showKey.value = false
}

function resetForm() {
  editing.value = null
  form.value = emptyForm()
  showKey.value = false
}

function save() {
  const name = form.value.name.trim()
  const baseUrl = form.value.baseUrl.trim()
  const apiKey = form.value.apiKey.trim()
  const model = form.value.model.trim()
  if (!name || !baseUrl || !apiKey || !model) {
    toast('请完整填写显示名称、Base URL、API Key 与模型名称')
    return
  }
  const payload = { name, baseUrl, apiKey, model }
  if (editing.value) {
    update(editing.value, payload)
    toast('已保存修改')
  } else {
    add(payload)
    toast('已新增配置')
  }
  resetForm()
}

function removeConfig(c: LlmConfig) {
  remove(c.id)
  if (editing.value === c.id) resetForm()
  toast('已删除')
}
</script>

<template>
  <div class="card">
    <div class="card-head">
      <h2 class="card-title">模型配置列表</h2>
      <span class="card-hint">AI 助手按默认配置调用</span>
    </div>
    <div v-if="configs.length === 0" class="empty-hint">暂无模型配置，请在下方新增一个。</div>
    <div v-else class="config-list">
      <div v-for="c in configs" :key="c.id" class="config-item">
        <div class="config-info">
          <div class="config-name-row">
            <span class="config-name">{{ c.name }}</span>
            <span v-if="c.isDefault" class="tag">默认</span>
          </div>
          <div class="config-meta">{{ c.model }} · {{ c.baseUrl }}</div>
        </div>
        <div class="config-actions">
          <button v-if="!c.isDefault" type="button" class="btn ghost" @click="setDefault(c.id); toast('已设为默认')">设为默认</button>
          <button type="button" class="btn ghost" @click="openEdit(c)">编辑</button>
          <button type="button" class="btn ghost" @click="removeConfig(c)">删除</button>
        </div>
      </div>
    </div>
  </div>

  <div class="card">
    <div class="card-head">
      <h2 class="card-title">{{ editing ? '编辑配置' : '新增配置' }}</h2>
      <span class="card-hint">保存于本地浏览器 localStorage</span>
    </div>
    <div class="form-grid">
      <div class="f-field">
        <label>显示名称<span class="req">*</span></label>
        <input v-model="form.name" placeholder="例如：主模型" />
      </div>
      <div class="f-field">
        <label>模型名称<span class="req">*</span></label>
        <input v-model="form.model" placeholder="gpt-4o-mini" />
        <span class="help">调用 chat/completions 时使用的 model 参数</span>
      </div>
      <div class="f-field full">
        <label>Base URL<span class="req">*</span></label>
        <input v-model="form.baseUrl" placeholder="https://api.example.com/v1" />
        <span class="help">OpenAI 兼容接口地址，客户端自动补全 /chat/completions</span>
      </div>
      <div class="f-field full">
        <label>API Key<span class="req">*</span></label>
        <div class="key-row">
          <input v-model="form.apiKey" :type="showKey ? 'text' : 'password'" placeholder="sk-..." />
          <button type="button" class="btn ghost" @click="showKey = !showKey">{{ showKey ? '隐藏' : '显示' }}</button>
        </div>
        <span class="help">明文保存于浏览器 localStorage，请勿在公共电脑上使用</span>
      </div>
    </div>
    <div class="form-actions">
      <button type="button" class="btn secondary" @click="resetForm">取消</button>
      <button type="button" class="btn primary" @click="save">{{ editing ? '保存修改' : '新增配置' }}</button>
    </div>
  </div>

  <div class="card">
    <div class="card-head">
      <h2 class="card-title">固定工具集（{{ TOOLS.length }} 个）</h2>
      <span class="card-hint">AI 通过 function calling 访问数据</span>
    </div>
    <div class="tool-list">
      <div v-for="t in TOOLS" :key="t.name" class="tool-item">
        <span class="t-name">{{ t.name }}</span>
        <span class="t-desc">{{ t.description }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.config-list {
  display: flex;
  flex-direction: column;
}
.config-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 0;
  border-bottom: 1px solid var(--border-color);
}
.config-item:last-child {
  border-bottom: none;
}
.config-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}
.config-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.config-name {
  font-size: var(--text-sm);
  font-weight: var(--weight-medium);
  color: var(--text-heading);
}
.tag {
  font-size: var(--text-xs);
  color: var(--color-primary);
  background: var(--color-primary-light);
  padding: 1px 8px;
  border-radius: var(--radius-round);
}
.config-meta {
  font-size: var(--text-xs);
  color: var(--text-secondary);
  font-family: var(--font-mono);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.config-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}
.empty-hint {
  font-size: var(--text-13);
  color: var(--text-secondary);
  padding: 8px 0;
}
</style>
