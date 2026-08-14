<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Delete, Download, Edit, Plus, Promotion } from '@element-plus/icons-vue'
import { yuanToWan } from '../../utils/format'
import {
  create as createProject,
  deptTree,
  exportCsv as exportProjectCsv,
  page as pageProject,
  projectTypeDict,
  remove as removeProject,
  stats as projectStats,
  submit as submitProject,
  submitBatch as submitBatchProject,
  update as updateProject,
} from '../../api/project'
import type {
  ProjectSaveDTO,
  ProjectStats,
  ProjectStatus,
  ProjectVO,
  SysDept,
  SysDictData,
} from '../../types'
import StatCard from '../../components/StatCard.vue'
import SmartPagination from '../../components/SmartPagination.vue'

/**
 * 储备项目维护页：统计卡片、筛选、列表、新增/编辑、提报、批量提报、导出。
 *
 * @author demo
 * @since 2026-08-15
 */

/** 默认每页条数。 */
const PAGE_SIZE = 5

/** 可编辑 / 可提报的状态（草稿、论证退回、审核退回）。 */
const EDITABLE_STATUSES: ProjectStatus[] = ['草稿', '论证退回', '审核退回']

/** 状态筛选选项。 */
const statusOptions: Array<{ value: ProjectStatus; label: string }> = [
  { value: '草稿', label: '草稿' },
  { value: '待论证', label: '待论证' },
  { value: '论证退回', label: '论证退回' },
  { value: '待审核', label: '待审核' },
  { value: '审核退回', label: '审核退回' },
  { value: '待下达', label: '待下达' },
  { value: '已下达', label: '已下达' },
]

/** 状态标签配色。 */
const STATUS_TAG_TYPES: Record<ProjectStatus, 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
  草稿: 'warning',
  待论证: 'primary',
  论证退回: 'danger',
  待审核: 'warning',
  审核退回: 'danger',
  待下达: 'warning',
  已下达: 'success',
}

/** 筛选条件。 */
interface ProjectFilters {
  projectType: string
  deptId: number | undefined
  status: string
  name: string
}

/** 新增/编辑表单模型。 */
interface ProjectFormModel {
  projectName: string
  projectType: string
  investmentAmountWan: number | undefined
  deptId: number | undefined
  description: string
}

const loading = ref(false)
const stats = ref<ProjectStats>({ total: 0, draft: 0, reviewRejected: 0, auditRejected: 0 })
const projects = ref<ProjectVO[]>([])
const total = ref(0)
const currentPage = ref(1)

const projectTypes = ref<SysDictData[]>([])
const depts = ref<SysDept[]>([])
const selectedRows = ref<ProjectVO[]>([])

const filters = reactive<ProjectFilters>({
  projectType: '',
  deptId: undefined,
  status: '',
  name: '',
})

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const editingId = ref<number | null>(null)
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<ProjectFormModel>(emptyForm())

/** 空表单模型。 */
function emptyForm(): ProjectFormModel {
  return {
    projectName: '',
    projectType: '',
    investmentAmountWan: undefined,
    deptId: undefined,
    description: '',
  }
}

/** 投资金额校验：必填且大于 0。 */
function validateAmount(
  _rule: unknown,
  value: unknown,
  callback: (error?: string | Error) => void,
): void {
  const num = typeof value === 'number' ? value : Number.NaN
  if (Number.isNaN(num) || num <= 0) {
    callback(new Error('投资金额需大于 0'))
  } else {
    callback()
  }
}

const rules: FormRules<ProjectFormModel> = {
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  projectType: [{ required: true, message: '请选择二级分类', trigger: 'change' }],
  investmentAmountWan: [
    { required: true, message: '请输入投资金额', trigger: 'blur' },
    { validator: validateAmount, trigger: 'blur' },
  ],
  deptId: [{ required: true, message: '请选择所属单位', trigger: 'change' }],
}

/** 扁平化部门树（带层级缩进）。 */
function flattenDepts(nodes: SysDept[], depth = 0): Array<{ id: number; name: string }> {
  const result: Array<{ id: number; name: string }> = []
  for (const node of nodes) {
    result.push({ id: node.id, name: '　'.repeat(depth) + node.deptName })
    if (node.children && node.children.length > 0) {
      result.push(...flattenDepts(node.children, depth + 1))
    }
  }
  return result
}

const deptOptions = computed(() => flattenDepts(depts.value))

/** 构建查询参数。 */
function buildQuery(): Parameters<typeof pageProject>[0] {
  return {
    pageNum: currentPage.value,
    pageSize: PAGE_SIZE,
    projectType: filters.projectType || undefined,
    deptId: filters.deptId || undefined,
    status: filters.status || undefined,
    name: filters.name || undefined,
  }
}

/** 加载统计卡片。 */
async function loadStats(): Promise<void> {
  try {
    const res = await projectStats()
    stats.value = res.data
  } catch {
    // 拦截器已提示，统计失败不阻塞列表。
  }
}

/** 加载项目列表。 */
async function loadProjects(): Promise<void> {
  loading.value = true
  try {
    const res = await pageProject(buildQuery())
    projects.value = res.data?.list ?? []
    total.value = res.data?.total ?? 0
  } catch {
    projects.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/** 加载字典与部门树。 */
async function loadDicts(): Promise<void> {
  try {
    const [dictRes, deptRes] = await Promise.all([projectTypeDict(), deptTree()])
    projectTypes.value = dictRes.data ?? []
    depts.value = deptRes.data ?? []
  } catch {
    // 拦截器已提示，回退为空选项。
  }
}

/** 刷新统计与列表。 */
async function refresh(): Promise<void> {
  await Promise.all([loadStats(), loadProjects()])
}

/** 筛选变化：回到第一页并刷新。 */
function handleFilterChange(): void {
  currentPage.value = 1
  loadProjects()
}

/** 分页切换。 */
function handlePageChange(page: number): void {
  currentPage.value = page
  loadProjects()
}

/** 表格勾选变化。 */
function handleSelectionChange(rows: ProjectVO[]): void {
  selectedRows.value = rows
}

/** 是否可编辑/提报。 */
function isEditable(status: ProjectStatus): boolean {
  return EDITABLE_STATUSES.includes(status)
}

/** 是否可删除（仅草稿）。 */
function isDeletable(status: ProjectStatus): boolean {
  return status === '草稿'
}

/** 行复选框可选（仅草稿/论证退回/审核退回）。 */
function selectable(row: ProjectVO): boolean {
  return isEditable(row.status)
}

/** 状态标签配色。 */
function statusTagType(status: ProjectStatus): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  return STATUS_TAG_TYPES[status] ?? 'info'
}

/** 序号（跨页累计）。 */
function indexMethod(index: number): number {
  return (currentPage.value - 1) * PAGE_SIZE + index + 1
}

/** 时间字符串展示（ISO 转空格分隔）。 */
function formatDateTime(value: string): string {
  if (!value) {
    return '-'
  }
  return value.replace('T', ' ')
}

/** 元 → 万元（保留 2 位小数）。 */
function yuanToWanNumber(yuan: number): number {
  return Number((yuan / 10000).toFixed(2))
}

/** 打开新增弹窗。 */
function openCreate(): void {
  dialogMode.value = 'create'
  editingId.value = null
  Object.assign(form, emptyForm())
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

/** 打开编辑弹窗。 */
function openEdit(row: ProjectVO): void {
  dialogMode.value = 'edit'
  editingId.value = row.id
  Object.assign(form, {
    projectName: row.projectName,
    projectType: row.projectType,
    investmentAmountWan: yuanToWanNumber(row.investmentAmount),
    deptId: row.deptId,
    description: row.description || '',
  })
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

/** 保存新增/编辑。 */
async function handleSave(): Promise<void> {
  if (!formRef.value) {
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  saving.value = true
  try {
    const dto: ProjectSaveDTO = {
      projectName: form.projectName,
      projectType: form.projectType,
      investmentAmount: Math.round((form.investmentAmountWan ?? 0) * 10000),
      deptId: form.deptId as number,
      description: form.description,
    }
    if (dialogMode.value === 'create') {
      await createProject(dto)
      ElMessage.success('新增成功')
    } else {
      await updateProject(editingId.value as number, dto)
      ElMessage.success('保存成功')
    }
    dialogVisible.value = false
    currentPage.value = 1
    await refresh()
  } finally {
    saving.value = false
  }
}

/** 提报确认（绿色）。 */
async function confirmSubmit(message: string): Promise<boolean> {
  try {
    await ElMessageBox.confirm(message, '确认提报', {
      type: 'success',
      confirmButtonText: '确认提报',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--success',
    })
    return true
  } catch {
    return false
  }
}

/** 删除确认（红色）。 */
async function confirmDelete(message: string): Promise<boolean> {
  try {
    await ElMessageBox.confirm(message, '确认删除', {
      type: 'error',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger',
    })
    return true
  } catch {
    return false
  }
}

/** 提报单个项目。 */
async function handleSubmit(row: ProjectVO): Promise<void> {
  const ok = await confirmSubmit(`确定要提报项目「${row.projectName}」吗？提报后将无法再编辑或删除。`)
  if (!ok) {
    return
  }
  await submitProject(row.id)
  ElMessage.success('提报成功')
  currentPage.value = 1
  await refresh()
}

/** 批量提报。 */
async function handleBatchSubmit(): Promise<void> {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先勾选需要提报的项目')
    return
  }
  const names = selectedRows.value.map((p) => p.projectName).join('、')
  const ok = await confirmSubmit(`确定要提报以下 ${selectedRows.value.length} 个项目吗？${names}`)
  if (!ok) {
    return
  }
  await submitBatchProject(selectedRows.value.map((p) => p.id))
  ElMessage.success('批量提报成功')
  currentPage.value = 1
  await refresh()
}

/** 删除项目。 */
async function handleDelete(row: ProjectVO): Promise<void> {
  const ok = await confirmDelete(`确定要删除项目「${row.projectName}」吗？此操作不可恢复。`)
  if (!ok) {
    return
  }
  await removeProject(row.id)
  ElMessage.success('删除成功')
  currentPage.value = 1
  await refresh()
}

/** 导出 CSV。 */
async function handleExport(): Promise<void> {
  await exportProjectCsv(buildQuery())
}

onMounted(() => {
  refresh()
  loadDicts()
})
</script>

<template>
  <div class="project-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">储备项目维护</h1>
        <p class="page-description">维护储备项目，支持新增、编辑、删除及提报操作</p>
      </div>
      <el-button v-permission="'project:add'" type="primary" :icon="Plus" @click="openCreate">
        新增项目
      </el-button>
    </div>

    <div class="stats-grid">
      <StatCard title="项目总数" :value="stats.total" color="primary" icon="Layers" />
      <StatCard title="草稿（未提报）" :value="stats.draft" color="warning" icon="EditPen" />
      <StatCard title="论证退回" :value="stats.reviewRejected" color="danger" icon="CircleClose" />
      <StatCard title="审核退回" :value="stats.auditRejected" color="info" icon="Warning" />
    </div>

    <div class="card">
      <div class="filter-bar">
        <div class="filter-item">
          <label class="filter-label">二级分类</label>
          <el-select
            v-model="filters.projectType"
            placeholder="全部分类"
            clearable
            @change="handleFilterChange"
          >
            <el-option
              v-for="item in projectTypes"
              :key="item.id"
              :label="item.dictLabel"
              :value="item.dictValue"
            />
          </el-select>
        </div>
        <div class="filter-item">
          <label class="filter-label">所属单位</label>
          <el-select
            v-model="filters.deptId"
            placeholder="全部单位"
            clearable
            filterable
            @change="handleFilterChange"
          >
            <el-option
              v-for="item in deptOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </div>
        <div class="filter-item">
          <label class="filter-label">状态</label>
          <el-select
            v-model="filters.status"
            placeholder="全部状态"
            clearable
            @change="handleFilterChange"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </div>
        <div class="filter-item filter-search">
          <label class="filter-label">搜索</label>
          <el-input
            v-model="filters.name"
            placeholder="项目名称..."
            clearable
            @change="handleFilterChange"
            @keyup.enter="handleFilterChange"
          />
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <h3 class="card-title">项目列表</h3>
        <div class="card-actions">
          <el-button
            v-permission="'project:submit'"
            type="success"
            :icon="Promotion"
            @click="handleBatchSubmit"
          >
            批量提报
          </el-button>
          <el-button v-permission="'project:export'" :icon="Download" @click="handleExport">
            导出
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="projects"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="42" :selectable="selectable" />
        <el-table-column type="index" label="序号" width="64" :index="indexMethod" />
        <el-table-column prop="projectName" label="项目名称" min-width="220" show-overflow-tooltip />
        <el-table-column label="二级分类" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.projectType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="投资金额（万元）" width="140" align="right">
          <template #default="{ row }">
            {{ yuanToWan(row.investmentAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="所属单位" width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="isEditable(row.status)"
              v-permission="'project:submit'"
              link
              type="primary"
              title="提报"
              @click="handleSubmit(row)"
            >
              <el-icon><Promotion /></el-icon>
            </el-button>
            <el-button
              v-if="isEditable(row.status)"
              v-permission="'project:edit'"
              link
              type="primary"
              title="编辑"
              @click="openEdit(row)"
            >
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button
              v-if="isDeletable(row.status)"
              v-permission="'project:delete'"
              link
              type="danger"
              title="删除"
              @click="handleDelete(row)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <SmartPagination
        :total="total"
        :page-size="PAGE_SIZE"
        :current-page="currentPage"
        @change="handlePageChange"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增储备项目' : '编辑储备项目'"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="form.projectName" placeholder="请输入项目名称" maxlength="100" clearable />
        </el-form-item>
        <el-form-item label="二级分类" prop="projectType">
          <el-select v-model="form.projectType" placeholder="请选择分类" clearable style="width: 100%">
            <el-option
              v-for="item in projectTypes"
              :key="item.id"
              :label="item.dictLabel"
              :value="item.dictValue"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="投资金额（万元）" prop="investmentAmountWan">
          <el-input-number
            v-model="form.investmentAmountWan"
            :min="0.01"
            :precision="2"
            :step="100"
            :controls="false"
            placeholder="请输入投资金额"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="所属单位" prop="deptId">
          <el-select v-model="form.deptId" placeholder="请选择单位" clearable filterable style="width: 100%">
            <el-option
              v-for="item in deptOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入项目描述（选填）"
            maxlength="500"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.project-page {
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
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
}

.filter-label {
  color: var(--text-secondary);
  font-size: 0.85rem;
}

.filter-search {
  min-width: 220px;
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

.card-actions {
  display: flex;
  gap: 0.5rem;
}

@media (max-width: 1100px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
