<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { CircleCheck, CircleClose, Edit, View } from '@element-plus/icons-vue'
import { yuanToWan } from '../../utils/format'
import { projectTypeDict } from '../../api/project'
import { detail as reviewDetail } from '../../api/review'
import {
  batch as batchAudit,
  detail as auditDetail,
  execute as executeAudit,
  page as pageAudit,
  stats as auditStats,
} from '../../api/audit'
import type {
  AuditCommand,
  AuditDetailVO,
  AuditStats,
  ProjectStatus,
  ProjectVO,
  ReviewRecordVO,
  SysDictData,
} from '../../types'
import StatCard from '../../components/StatCard.vue'
import SmartPagination from '../../components/SmartPagination.vue'

/**
 * 储备项目审核页：统计卡片、筛选、列表、批量通过/退回、审核弹窗、详情弹窗。
 *
 * @author demo
 * @since 2026-08-15
 */

/** 默认每页条数。 */
const PAGE_SIZE = 5

/** Element Plus 标签配色类型。 */
type TagType = 'primary' | 'success' | 'warning' | 'danger' | 'info'

/** 审核状态筛选选项（前端语义，审核通过=待下达）。 */
const statusOptions: Array<{ value: string; label: string }> = [
  { value: '待审核', label: '待审核' },
  { value: '审核通过', label: '审核通过' },
  { value: '审核退回', label: '审核退回' },
]

/** 列表行：项目 + 最新论证记录（用于展示论证结果/意见）。 */
interface AuditRow extends ProjectVO {
  review: ReviewRecordVO | null
}

/** 筛选条件。 */
interface AuditFilters {
  name: string
  projectType: string
  status: string
}

/** 审核表单模型。 */
interface AuditFormModel {
  opinion: string
  result: '' | '通过' | '退回'
}

const loading = ref(false)
const stats = ref<AuditStats>({ pending: 0, passed: 0, rejected: 0, passRate: 0 })
const projects = ref<AuditRow[]>([])
const total = ref(0)
const currentPage = ref(1)
const projectTypes = ref<SysDictData[]>([])
const selectedRows = ref<AuditRow[]>([])
const tableRef = ref<{ clearSelection: () => void } | null>(null)

const filters = reactive<AuditFilters>({
  name: '',
  projectType: '',
  status: '',
})

const auditDialogVisible = ref(false)
const auditingRow = ref<AuditRow | null>(null)
const auditing = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<AuditFormModel>(emptyForm())

const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<AuditDetailVO | null>(null)
const detailRow = ref<AuditRow | null>(null)

/** 空审核表单模型。 */
function emptyForm(): AuditFormModel {
  return {
    opinion: '',
    result: '',
  }
}

const rules: FormRules<AuditFormModel> = {
  result: [{ required: true, message: '请选择审核结果', trigger: 'change' }],
}

/** 审核状态展示文案。 */
function auditStatusLabel(status: ProjectStatus): string {
  return status === '待下达' ? '审核通过' : status
}

/** 审核状态标签配色。 */
function auditStatusTagType(status: ProjectStatus): TagType {
  if (status === '待下达') {
    return 'success'
  }
  if (status === '审核退回') {
    return 'danger'
  }
  return 'warning'
}

/** 论证结果标签配色。 */
function reviewResultTagType(result: string): TagType {
  return result === '通过' ? 'success' : 'danger'
}

/** 通过率展示（保留 1 位小数 + %）。 */
function formatRate(rate: number): string {
  return `${rate.toFixed(1)}%`
}

/** 时间字符串展示（ISO 转空格分隔）。 */
function formatDateTime(value: string): string {
  if (!value) {
    return '-'
  }
  return value.replace('T', ' ')
}

/** 构建查询参数。 */
function buildQuery(): Parameters<typeof pageAudit>[0] {
  return {
    pageNum: currentPage.value,
    pageSize: PAGE_SIZE,
    name: filters.name || undefined,
    projectType: filters.projectType || undefined,
    status: filters.status || undefined,
  }
}

/** 加载统计卡片。 */
async function loadStats(): Promise<void> {
  try {
    const res = await auditStats()
    stats.value = res.data
  } catch {
    // 拦截器已提示，统计失败不阻塞列表。
  }
}

/** 加载审核列表（每行补充最新论证记录）。 */
async function loadProjects(): Promise<void> {
  loading.value = true
  try {
    const res = await pageAudit(buildQuery())
    const list = res.data?.list ?? []
    total.value = res.data?.total ?? 0
    projects.value = await Promise.all(list.map(enrichRow))
  } catch {
    projects.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/** 补充行内论证记录（失败回退为 null，论证结果列降级为 —）。 */
async function enrichRow(p: ProjectVO): Promise<AuditRow> {
  try {
    const res = await reviewDetail(p.id)
    return { ...p, review: res.data.review }
  } catch {
    return { ...p, review: null }
  }
}

/** 加载项目分类字典。 */
async function loadDicts(): Promise<void> {
  try {
    const res = await projectTypeDict()
    projectTypes.value = res.data ?? []
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
function handleSelectionChange(rows: AuditRow[]): void {
  selectedRows.value = rows
}

/** 行复选框可选（仅待审核）。 */
function selectable(row: AuditRow): boolean {
  return row.status === '待审核'
}

/** 是否待审核（可执行审核）。 */
function isPendingAudit(status: ProjectStatus): boolean {
  return status === '待审核'
}

/** 批量操作确认。 */
async function confirmBatch(message: string, pass: boolean): Promise<boolean> {
  try {
    await ElMessageBox.confirm(message, pass ? '批量通过' : '批量退回', {
      type: pass ? 'success' : 'warning',
      confirmButtonText: pass ? '确认通过' : '确认退回',
      cancelButtonText: '取消',
      confirmButtonClass: pass ? 'el-button--success' : 'el-button--danger',
    })
    return true
  } catch {
    return false
  }
}

/** 批量通过 / 批量退回。 */
async function handleBatch(pass: boolean): Promise<void> {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先勾选需要审核的项目')
    return
  }
  const action = pass ? '通过' : '退回'
  const ok = await confirmBatch(`确定要${action}选中的 ${selectedRows.value.length} 个项目吗？`, pass)
  if (!ok) {
    return
  }
  try {
    await batchAudit({ ids: selectedRows.value.map((p) => p.id), pass })
    ElMessage.success(`批量${action}成功`)
    tableRef.value?.clearSelection()
    currentPage.value = 1
    await refresh()
  } catch {
    // 拦截器已统一提示，此处不重复处理。
  }
}

/** 打开审核弹窗。 */
function openAudit(row: AuditRow): void {
  auditingRow.value = row
  Object.assign(form, emptyForm())
  auditDialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

/** 提交审核。 */
async function handleAuditSubmit(): Promise<void> {
  if (!formRef.value) {
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid || !auditingRow.value) {
    return
  }
  auditing.value = true
  try {
    const cmd: AuditCommand = {
      opinion: form.opinion,
      pass: form.result === '通过',
    }
    await executeAudit(auditingRow.value.id, cmd)
    ElMessage.success('审核完成')
    auditDialogVisible.value = false
    currentPage.value = 1
    await refresh()
  } catch {
    // 拦截器已统一提示，此处不重复处理。
  } finally {
    auditing.value = false
  }
}

/** 打开详情弹窗。 */
async function openDetail(row: AuditRow): Promise<void> {
  detailRow.value = row
  detailDialogVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    const res = await auditDetail(row.id)
    detailData.value = res.data
  } catch {
    detailData.value = null
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  refresh()
  loadDicts()
})
</script>

<template>
  <div class="audit-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">储备项目审核</h1>
        <p class="page-description">人工复核论证是否准确，通过或退回业务流程</p>
      </div>
      <div class="header-actions">
        <el-button
          v-permission="'audit:batch'"
          type="success"
          :icon="CircleCheck"
          @click="handleBatch(true)"
        >
          批量通过
        </el-button>
        <el-button
          v-permission="'audit:batch'"
          type="danger"
          :icon="CircleClose"
          @click="handleBatch(false)"
        >
          批量退回
        </el-button>
      </div>
    </div>

    <div class="stats-grid">
      <StatCard title="待审核" :value="stats.pending" color="warning" icon="Clock" />
      <StatCard title="已审核通过" :value="stats.passed" color="success" icon="CircleCheck" />
      <StatCard title="已退回" :value="stats.rejected" color="danger" icon="RefreshLeft" />
      <StatCard title="通过率" :value="formatRate(stats.passRate)" color="info" icon="TrendCharts" />
    </div>

    <div class="card">
      <div class="filter-bar">
        <div class="filter-item">
          <label class="filter-label">项目名称</label>
          <el-input
            v-model="filters.name"
            placeholder="项目名称..."
            clearable
            @change="handleFilterChange"
            @keyup.enter="handleFilterChange"
          />
        </div>
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
          <label class="filter-label">审核状态</label>
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
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <h3 class="card-title">项目列表</h3>
      </div>

      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="projects"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="42" :selectable="selectable" />
        <el-table-column prop="projectCode" label="项目编码" min-width="130" show-overflow-tooltip />
        <el-table-column prop="projectName" label="项目名称" min-width="200" show-overflow-tooltip />
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
        <el-table-column label="论证结果" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.review" :type="reviewResultTagType(row.review.result)" size="small">
              {{ row.review.result }}
            </el-tag>
            <span v-else class="empty-value">—</span>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="110">
          <template #default="{ row }">
            <el-tag :type="auditStatusTagType(row.status)" size="small">
              {{ auditStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="isPendingAudit(row.status)"
              v-permission="'audit:execute'"
              link
              type="primary"
              :icon="Edit"
              @click="openAudit(row)"
            >
              审核
            </el-button>
            <el-button
              v-permission="'audit:view'"
              link
              type="primary"
              :icon="View"
              @click="openDetail(row)"
            >
              查看详情
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
      v-model="auditDialogVisible"
      title="项目审核"
      width="620px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="项目编码">
          <span class="readonly-text">{{ auditingRow?.projectCode }}</span>
        </el-form-item>
        <el-form-item label="项目名称">
          <span class="readonly-text">{{ auditingRow?.projectName }}</span>
        </el-form-item>
        <el-form-item label="二级分类">
          <el-tag v-if="auditingRow" size="small">{{ auditingRow.projectType }}</el-tag>
        </el-form-item>
        <el-form-item label="投资金额">
          <span class="readonly-text">
            {{ auditingRow ? `${yuanToWan(auditingRow.investmentAmount)} 万元` : '-' }}
          </span>
        </el-form-item>
        <el-form-item label="论证结果">
          <el-tag v-if="auditingRow?.review" :type="reviewResultTagType(auditingRow.review.result)" size="small">
            {{ auditingRow.review.result }}
          </el-tag>
          <span v-else class="empty-value">—</span>
        </el-form-item>
        <el-form-item label="论证意见">
          <div class="opinion-box">{{ auditingRow?.review?.opinion || '—' }}</div>
        </el-form-item>
        <el-divider content-position="left">审核意见</el-divider>
        <el-form-item label="审核意见" prop="opinion">
          <el-input
            v-model="form.opinion"
            type="textarea"
            :rows="3"
            placeholder="请输入审核意见"
            maxlength="500"
          />
        </el-form-item>
        <el-form-item label="审核结果" prop="result">
          <el-select v-model="form.result" placeholder="请选择" style="width: 100%">
            <el-option label="通过" value="通过" />
            <el-option label="退回" value="退回" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="auditing" @click="handleAuditSubmit">提交审核</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="审核详情" width="680px">
      <el-descriptions v-loading="detailLoading" :column="2" border>
        <el-descriptions-item label="项目编码">{{ detailData?.project.projectCode ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="项目名称">{{ detailData?.project.projectName ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="二级分类">{{ detailData?.project.projectType ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="投资金额（万元）">
          {{ detailData ? yuanToWan(detailData.project.investmentAmount) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="论证结果">
          <el-tag v-if="detailRow?.review" :type="reviewResultTagType(detailRow.review.result)" size="small">
            {{ detailRow.review.result }}
          </el-tag>
          <span v-else class="empty-value">—</span>
        </el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag v-if="detailData" :type="auditStatusTagType(detailData.project.status)" size="small">
            {{ auditStatusLabel(detailData.project.status) }}
          </el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="论证意见" :span="2">
          {{ detailRow?.review?.opinion || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">
          {{ detailData?.audit?.opinion || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="审核时间" :span="2">
          {{ formatDateTime(detailData?.audit?.auditTime ?? '') }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<style scoped>
.audit-page {
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

.header-actions {
  display: flex;
  gap: 0.5rem;
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
  min-width: 180px;
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

.empty-value {
  color: var(--text-muted);
}

.readonly-text {
  color: var(--text-primary);
}

.opinion-box {
  width: 100%;
  padding: 0.5rem 0.75rem;
  border-radius: 8px;
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  min-height: 40px;
}

@media (max-width: 1100px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
