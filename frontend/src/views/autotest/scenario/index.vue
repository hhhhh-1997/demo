<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus, CaretTop, CaretBottom, Delete, VideoPlay, Edit } from '@element-plus/icons-vue'
import { create, detail, page, remove, run, update } from '../../../api/autotest'
import type { AssertItem, AssertOp, AssertType, ExtractItem, ScenarioSaveDTO, ScenarioVO, StepItem } from '../../../types'
import SmartPagination from '../../../components/SmartPagination.vue'

/**
 * 自动化测试场景管理页：场景表格 + 步骤编排器抽屉。
 *
 * @author demo
 * @since 2026-08-15
 */

const PAGE_SIZE = 5
const router = useRouter()

/** headers/query 的键值对编辑行。 */
interface KeyValueRow {
  key: string
  value: string
}

/** 编排器内的步骤模型（headers/query 用 KV 行、body 用 JSON 文本）。 */
interface StepEditorModel {
  name: string
  method: string
  path: string
  headers: KeyValueRow[]
  query: KeyValueRow[]
  body: string
  asserts: AssertItem[]
  extracts: ExtractItem[]
}

const METHODS = ['GET', 'POST', 'PUT', 'DELETE']
const ASSERT_TYPES: Array<{ value: AssertType; label: string }> = [
  { value: 'STATUS', label: '状态码' },
  { value: 'JSON', label: 'JSONPath' },
]
const ASSERT_OPS: Array<{ value: AssertOp; label: string }> = [
  { value: 'EQUALS', label: '等于' },
  { value: 'CONTAINS', label: '包含' },
  { value: 'EXISTS', label: '存在' },
]

const loading = ref(false)
const scenarios = ref<ScenarioVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const filters = reactive({ name: '' })

const drawerVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  name: '',
  description: '',
  baseUrl: '',
  variablesText: '',
  steps: [] as StepEditorModel[],
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入场景名', trigger: 'blur' }],
}

/** 空步骤。 */
function emptyStep(): StepEditorModel {
  return {
    name: '',
    method: 'GET',
    path: '',
    headers: [],
    query: [],
    body: '{}',
    asserts: [],
    extracts: [],
  }
}

/** 空断言。 */
function emptyAssert(): AssertItem {
  return { type: 'STATUS', expected: '200', jsonPath: '', op: 'EQUALS' }
}

/** 空提取。 */
function emptyExtract(): ExtractItem {
  return { name: '', jsonPath: '' }
}

function emptyForm(): void {
  form.name = ''
  form.description = ''
  form.baseUrl = ''
  form.variablesText = '{}'
  form.steps = [emptyStep()]
}

/** KV 行 ↔ Record 转换。 */
function rowsToRecord(rows: KeyValueRow[]): Record<string, string> {
  const record: Record<string, string> = {}
  for (const row of rows) {
    if (row.key) {
      record[row.key] = row.value
    }
  }
  return record
}

function recordToRows(record: Record<string, string>): KeyValueRow[] {
  return Object.entries(record ?? {}).map(([key, value]) => ({ key, value }))
}

/** 编辑器步骤 ↔ 后端 StepItem。 */
function toApiStep(step: StepEditorModel): StepItem {
  let body: Record<string, unknown> = {}
  try {
    body = JSON.parse(step.body || '{}') as Record<string, unknown>
  } catch {
    body = {}
  }
  return {
    name: step.name,
    method: step.method,
    path: step.path,
    headers: rowsToRecord(step.headers),
    query: rowsToRecord(step.query),
    body,
    asserts: step.asserts,
    extracts: step.extracts,
  }
}

function toEditorStep(step: StepItem): StepEditorModel {
  return {
    name: step.name,
    method: step.method,
    path: step.path,
    headers: recordToRows(step.headers ?? {}),
    query: recordToRows(step.query ?? {}),
    body: JSON.stringify(step.body ?? {}, null, 2),
    asserts: step.asserts ?? [],
    extracts: step.extracts ?? [],
  }
}

function buildPayload(): ScenarioSaveDTO {
  let variables: Record<string, unknown> = {}
  try {
    variables = JSON.parse(form.variablesText || '{}') as Record<string, unknown>
  } catch {
    ElMessage.error('预置变量 JSON 格式错误')
    throw new Error('预置变量 JSON 格式错误')
  }
  return {
    name: form.name,
    description: form.description,
    baseUrl: form.baseUrl,
    variables,
    steps: form.steps.map(toApiStep),
  }
}

async function loadScenarios(): Promise<void> {
  loading.value = true
  try {
    const res = await page({ pageNum: currentPage.value, pageSize: PAGE_SIZE, name: filters.name || undefined })
    scenarios.value = res.data?.list ?? []
    total.value = res.data?.total ?? 0
  } catch {
    scenarios.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleFilterChange(): void {
  currentPage.value = 1
  loadScenarios()
}

function handlePageChange(page: number): void {
  currentPage.value = page
  loadScenarios()
}

async function openCreate(): Promise<void> {
  editingId.value = null
  emptyForm()
  drawerVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function openEdit(row: ScenarioVO): Promise<void> {
  try {
    const res = await detail(row.id)
    const data = res.data
    editingId.value = data.id
    form.name = data.name
    form.description = data.description
    form.baseUrl = data.baseUrl
    form.variablesText = JSON.stringify(data.variables ?? {}, null, 2)
    form.steps = (data.steps ?? []).map(toEditorStep)
    drawerVisible.value = true
    await nextTick()
    formRef.value?.clearValidate()
  } catch {
    // 拦截器已提示
  }
}

async function handleSave(): Promise<void> {
  if (!formRef.value) {
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  let payload: ScenarioSaveDTO
  try {
    payload = buildPayload()
  } catch {
    return
  }
  saving.value = true
  try {
    if (editingId.value == null) {
      await create(payload)
      ElMessage.success('创建成功')
    } else {
      await update(editingId.value, payload)
      ElMessage.success('保存成功')
    }
    drawerVisible.value = false
    currentPage.value = 1
    await loadScenarios()
  } catch {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}

async function handleRemove(row: ScenarioVO): Promise<void> {
  try {
    await remove(row.id)
    ElMessage.success('删除成功')
    await loadScenarios()
  } catch {
    // 拦截器已提示
  }
}

async function handleRun(row: ScenarioVO): Promise<void> {
  try {
    const res = await run(row.id)
    ElMessage.success('已触发执行')
    router.push(`/autotest/run/${res.data}`)
  } catch {
    // 拦截器已提示
  }
}

/** 步骤增删与排序。 */
function addStep(): void {
  form.steps.push(emptyStep())
}

function removeStep(index: number): void {
  form.steps.splice(index, 1)
}

function moveStep(index: number, delta: number): void {
  const target = index + delta
  if (target < 0 || target >= form.steps.length) {
    return
  }
  const tmp = form.steps[index]
  form.steps[index] = form.steps[target]
  form.steps[target] = tmp
}

function addHeader(step: StepEditorModel): void {
  step.headers.push({ key: '', value: '' })
}

function addQuery(step: StepEditorModel): void {
  step.query.push({ key: '', value: '' })
}

function addAssert(step: StepEditorModel): void {
  step.asserts.push(emptyAssert())
}

function addExtract(step: StepEditorModel): void {
  step.extracts.push(emptyExtract())
}

onMounted(loadScenarios)
</script>

<template>
  <div class="autotest-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">自动化测试</h1>
        <p class="page-description">编排接口链路（登录 → 业务 → 登出），一键执行并回溯每步结果</p>
      </div>
      <button class="btn btn-primary" v-permission="'autotest:scenario:add'" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新建场景
      </button>
    </div>

    <div class="card">
      <div class="filter-bar">
        <div class="filter-item">
          <label class="filter-label">场景名</label>
          <el-input
            v-model="filters.name"
            placeholder="场景名..."
            clearable
            @change="handleFilterChange"
            @keyup.enter="handleFilterChange"
          />
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <h3 class="card-title">场景列表</h3>
      </div>
      <el-table v-loading="loading" :data="scenarios" row-key="id">
        <el-table-column prop="name" label="场景名" min-width="160" show-overflow-tooltip />
        <el-table-column prop="baseUrl" label="基础地址" min-width="220" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ row.createTime ? row.createTime.replace('T', ' ') : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <button v-permission="'autotest:scenario:run'" class="icon-btn success" title="运行" @click="handleRun(row)">
                <el-icon><VideoPlay /></el-icon>
              </button>
              <button v-permission="'autotest:scenario:edit'" class="icon-btn" title="编辑" @click="openEdit(row)">
                <el-icon><Edit /></el-icon>
              </button>
              <button v-permission="'autotest:scenario:delete'" class="icon-btn danger" title="删除" @click="handleRemove(row)">
                <el-icon><Delete /></el-icon>
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <SmartPagination :total="total" :page-size="PAGE_SIZE" :current-page="currentPage" @change="handlePageChange" />
    </div>

    <el-drawer v-model="drawerVisible" :title="editingId == null ? '新建场景' : '编辑场景'" size="72%">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="场景名" prop="name">
          <el-input v-model="form.name" placeholder="场景名" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="描述" />
        </el-form-item>
        <el-form-item label="基础地址">
          <el-input v-model="form.baseUrl" placeholder="http://localhost:8080" />
        </el-form-item>
        <el-form-item label="预置变量">
          <el-input v-model="form.variablesText" type="textarea" :rows="4" placeholder='{"username":"admin"}' />
        </el-form-item>
      </el-form>

      <el-divider content-position="left">步骤编排</el-divider>

      <div v-for="(step, index) in form.steps" :key="index" class="step-card">
        <div class="step-card__header">
          <span class="step-card__title">步骤 {{ index + 1 }}</span>
          <div class="step-card__actions">
            <button class="icon-btn" title="上移" @click="moveStep(index, -1)">
              <el-icon><CaretTop /></el-icon>
            </button>
            <button class="icon-btn" title="下移" @click="moveStep(index, 1)">
              <el-icon><CaretBottom /></el-icon>
            </button>
            <button class="icon-btn danger" title="删除步骤" @click="removeStep(index)">
              <el-icon><Delete /></el-icon>
            </button>
          </div>
        </div>

        <div class="step-grid">
          <el-input v-model="step.name" class="step-name" placeholder="步骤名" />
          <el-select v-model="step.method" class="step-method">
            <el-option v-for="m in METHODS" :key="m" :label="m" :value="m" />
          </el-select>
          <el-input v-model="step.path" class="step-path" placeholder="/api/xxx" />
        </div>

        <el-collapse>
          <el-collapse-item title="Headers">
            <div v-for="(row, i) in step.headers" :key="i" class="kv-row">
              <el-input v-model="row.key" placeholder="Header 名" />
              <el-input v-model="row.value" placeholder="值（支持 {{变量}}）" />
              <button class="icon-btn danger" @click="step.headers.splice(i, 1)"><el-icon><Delete /></el-icon></button>
            </div>
            <button class="btn btn-outline btn-sm" @click="addHeader(step)">+ Header</button>
          </el-collapse-item>
          <el-collapse-item title="Query">
            <div v-for="(row, i) in step.query" :key="i" class="kv-row">
              <el-input v-model="row.key" placeholder="参数名" />
              <el-input v-model="row.value" placeholder="值（支持 {{变量}}）" />
              <button class="icon-btn danger" @click="step.query.splice(i, 1)"><el-icon><Delete /></el-icon></button>
            </div>
            <button class="btn btn-outline btn-sm" @click="addQuery(step)">+ Query</button>
          </el-collapse-item>
          <el-collapse-item title="Body (JSON)">
            <el-input v-model="step.body" type="textarea" :rows="6" placeholder='{"username":"{{username}}"}' />
          </el-collapse-item>
          <el-collapse-item title="断言">
            <div v-for="(a, i) in step.asserts" :key="i" class="assert-row">
              <el-select v-model="a.type" style="width: 110px">
                <el-option v-for="t in ASSERT_TYPES" :key="t.value" :label="t.label" :value="t.value" />
              </el-select>
              <template v-if="a.type === 'STATUS'">
                <el-input v-model="a.expected" placeholder="期望状态码" style="width: 140px" />
              </template>
              <template v-else>
                <el-input v-model="a.jsonPath" placeholder="$.code" style="width: 160px" />
                <el-select v-model="a.op" style="width: 110px">
                  <el-option v-for="o in ASSERT_OPS" :key="o.value" :label="o.label" :value="o.value" />
                </el-select>
                <el-input v-if="a.op !== 'EXISTS'" v-model="a.expected" placeholder="期望值" style="width: 140px" />
              </template>
              <button class="icon-btn danger" @click="step.asserts.splice(i, 1)"><el-icon><Delete /></el-icon></button>
            </div>
            <button class="btn btn-outline btn-sm" @click="addAssert(step)">+ 断言</button>
          </el-collapse-item>
          <el-collapse-item title="变量提取">
            <div v-for="(e, i) in step.extracts" :key="i" class="kv-row">
              <el-input v-model="e.name" placeholder="变量名" />
              <el-input v-model="e.jsonPath" placeholder="$.data.token" />
              <button class="icon-btn danger" @click="step.extracts.splice(i, 1)"><el-icon><Delete /></el-icon></button>
            </div>
            <button class="btn btn-outline btn-sm" @click="addExtract(step)">+ 提取</button>
          </el-collapse-item>
        </el-collapse>
      </div>

      <button class="btn btn-outline btn-block" @click="addStep">+ 添加步骤</button>

      <template #footer>
        <button class="btn btn-outline" @click="drawerVisible = false">取消</button>
        <button class="btn btn-primary" :disabled="saving" @click="handleSave">保存</button>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped>
.autotest-page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  margin: 0;
  font-size: 1.5rem;
  color: var(--text-primary);
}

.page-description {
  margin: 0.25rem 0 0;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  overflow: hidden;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 1rem;
  padding: 1rem;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  min-width: 220px;
}

.filter-label {
  color: var(--text-secondary);
  font-size: 0.85rem;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem;
  border-bottom: 1px solid var(--border-color);
}

.card-title {
  margin: 0;
  font-size: 1rem;
  color: var(--text-primary);
}

.action-buttons {
  display: flex;
  gap: 0.25rem;
}

.step-card {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 0.75rem;
  margin-bottom: 0.75rem;
}

.step-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.step-card__title {
  font-weight: 600;
  color: var(--text-primary);
}

.step-card__actions {
  display: flex;
  gap: 0.25rem;
}

.step-grid {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.step-name {
  width: 140px;
}

.step-method {
  width: 120px;
}

.step-path {
  flex: 1;
}

.kv-row {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.assert-row {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  align-items: center;
}

.btn-block {
  width: 100%;
  margin-top: 0.5rem;
}

.btn-sm {
  padding: 0.2rem 0.6rem;
  font-size: 0.85rem;
}
</style>
