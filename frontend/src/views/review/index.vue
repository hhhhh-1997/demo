<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { CircleCheck, View } from '@element-plus/icons-vue'
import { yuanToWan } from '../../utils/format'
import { projectTypeDict } from '../../api/project'
import { detail as reviewDetail, execute as executeReview, page as pageReview, stats as reviewStats } from '../../api/review'
import type {
  ProjectStatus,
  ProjectVO,
  ReviewCommand,
  ReviewDetailVO,
  ReviewRecordVO,
  ReviewStats,
  SysDictData,
} from '../../types'
import StatCard from '../../components/StatCard.vue'
import SmartPagination from '../../components/SmartPagination.vue'

/**
 * 储备项目论证页：统计卡片、筛选、列表、论证弹窗、详情弹窗。
 *
 * @author demo
 * @since 2026-08-15
 */

/** 默认每页条数。 */
const PAGE_SIZE = 5

/** Element Plus 标签配色类型。 */
type TagType = 'primary' | 'success' | 'warning' | 'danger' | 'info'

/** 论证检查项字段名。 */
type CheckItemKey = 'infoComplete' | 'threeImportant' | 'splitProject' | 'interfaceConfusion'

/** 论证状态筛选选项（前端语义，论证通过=待审核）。 */
const statusOptions: Array<{ value: string; label: string }> = [
  { value: '待论证', label: '待论证' },
  { value: '论证通过', label: '论证通过' },
  { value: '论证退回', label: '论证退回' },
]

/** 列表行：项目 + 最新论证记录（论证前为 null）。 */
interface ReviewRow extends ProjectVO {
  review: ReviewRecordVO | null
}

/** 筛选条件。 */
interface ReviewFilters {
  name: string
  projectType: string
  status: string
}

/** 论证表单模型。 */
interface ReviewFormModel {
  infoComplete: string
  threeImportant: string
  splitProject: string
  interfaceConfusion: string
  opinion: string
  result: '' | '通过' | '不通过'
}

const loading = ref(false)
const stats = ref<ReviewStats>({ pending: 0, passed: 0, rejected: 0, passRate: 0 })
const projects = ref<ReviewRow[]>([])
const total = ref(0)
const currentPage = ref(1)
const projectTypes = ref<SysDictData[]>([])

const filters = reactive<ReviewFilters>({
  name: '',
  projectType: '',
  status: '',
})

const reviewDialogVisible = ref(false)
const reviewingRow = ref<ReviewRow | null>(null)
const reviewing = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<ReviewFormModel>(emptyForm())

const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<ReviewDetailVO | null>(null)

/** 空论证表单模型（检查项默认取合规值）。 */
function emptyForm(): ReviewFormModel {
  return {
    infoComplete: '完整',
    threeImportant: '符合',
    splitProject: '无',
    interfaceConfusion: '无',
    opinion: '',
    result: '',
  }
}

const rules: FormRules<ReviewFormModel> = {
  result: [{ required: true, message: '请选择论证结果', trigger: 'change' }],
}

/** 论证状态展示文案。 */
function reviewStatusLabel(status: ProjectStatus): string {
  return status === '待审核' ? '论证通过' : status
}

/** 论证状态标签配色。 */
function reviewStatusTagType(status: ProjectStatus): TagType {
  if (status === '待审核') {
    return 'success'
  }
  if (status === '论证退回') {
    return 'danger'
  }
  return 'warning'
}

/** 检查项徽标配色（完整=success/不完整=warning，符合=success/不符合=danger，无=success/存在=danger）。 */
function checkItemTagType(item: CheckItemKey, value: string): TagType {
  if (item === 'infoComplete') {
    return value === '完整' ? 'success' : 'warning'
  }
  if (item === 'threeImportant') {
    return value === '符合' ? 'success' : 'danger'
  }
  return value === '无' ? 'success' : 'danger'
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
function buildQuery(): Parameters<typeof pageReview>[0] {
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
    const res = await reviewStats()
    stats.value = res.data
  } catch {
    // 拦截器已提示，统计失败不阻塞列表。
  }
}

/** 加载论证列表（非待论证行补充最新论证记录）。 */
async function loadProjects(): Promise<void> {
  loading.value = true
  try {
    const res = await pageReview(buildQuery())
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

/** 补充行内论证记录（论证前无记录，失败回退为 null）。 */
async function enrichRow(p: ProjectVO): Promise<ReviewRow> {
  if (p.status === '待论证') {
    return { ...p, review: null }
  }
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

/** 是否待论证（可执行论证）。 */
function isPendingReview(status: ProjectStatus): boolean {
  return status === '待论证'
}

/** 打开论证弹窗。 */
function openReview(row: ReviewRow): void {
  reviewingRow.value = row
  Object.assign(form, emptyForm())
  reviewDialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

/** 提交论证。 */
async function handleReviewSubmit(): Promise<void> {
  if (!formRef.value) {
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid || !reviewingRow.value) {
    return
  }
  reviewing.value = true
  try {
    const cmd: ReviewCommand = {
      infoComplete: form.infoComplete,
      threeImportant: form.threeImportant,
      splitProject: form.splitProject,
      interfaceConfusion: form.interfaceConfusion,
      opinion: form.opinion,
      pass: form.result === '通过',
    }
    await executeReview(reviewingRow.value.id, cmd)
    ElMessage.success('论证完成')
    reviewDialogVisible.value = false
    currentPage.value = 1
    await refresh()
  } catch {
    // 拦截器已统一提示，此处不重复处理。
  } finally {
    reviewing.value = false
  }
}

/** 打开详情弹窗。 */
async function openDetail(row: ReviewRow): Promise<void> {
  detailDialogVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    const res = await reviewDetail(row.id)
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
  <div class="review-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">储备项目论证</h1>
        <p class="page-description">对储备项目开展储备监督检查，逐项进行论证分析</p>
      </div>
    </div>

    <div class="stats-grid">
      <StatCard title="待论证" :value="stats.pending" color="warning" icon="Clock" />
      <StatCard title="已通过论证" :value="stats.passed" color="success" icon="CircleCheck" />
      <StatCard title="论证不通过" :value="stats.rejected" color="danger" icon="CircleClose" />
      <StatCard title="论证通过率" :value="formatRate(stats.passRate)" color="info" icon="TrendCharts" />
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
          <label class="filter-label">论证状态</label>
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

      <el-table v-loading="loading" :data="projects" row-key="id">
        <el-table-column prop="projectCode" label="项目编码" min-width="130" show-overflow-tooltip />
        <el-table-column prop="projectName" label="项目名称" min-width="200" show-overflow-tooltip />
        <el-table-column label="信息完整性" width="110">
          <template #default="{ row }">
            <el-tag
              v-if="row.review"
              :type="checkItemTagType('infoComplete', row.review.infoComplete)"
              size="small"
            >
              {{ row.review.infoComplete }}
            </el-tag>
            <span v-else class="empty-value">—</span>
          </template>
        </el-table-column>
        <el-table-column label="三重一大" width="100">
          <template #default="{ row }">
            <el-tag
              v-if="row.review"
              :type="checkItemTagType('threeImportant', row.review.threeImportant)"
              size="small"
            >
              {{ row.review.threeImportant }}
            </el-tag>
            <span v-else class="empty-value">—</span>
          </template>
        </el-table-column>
        <el-table-column label="拆分立项" width="100">
          <template #default="{ row }">
            <el-tag
              v-if="row.review"
              :type="checkItemTagType('splitProject', row.review.splitProject)"
              size="small"
            >
              {{ row.review.splitProject }}
            </el-tag>
            <span v-else class="empty-value">—</span>
          </template>
        </el-table-column>
        <el-table-column label="界面混淆" width="100">
          <template #default="{ row }">
            <el-tag
              v-if="row.review"
              :type="checkItemTagType('interfaceConfusion', row.review.interfaceConfusion)"
              size="small"
            >
              {{ row.review.interfaceConfusion }}
            </el-tag>
            <span v-else class="empty-value">—</span>
          </template>
        </el-table-column>
        <el-table-column label="论证状态" width="110">
          <template #default="{ row }">
            <el-tag :type="reviewStatusTagType(row.status)" size="small">
              {{ reviewStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="isPendingReview(row.status)"
              v-permission="'review:execute'"
              link
              type="primary"
              :icon="CircleCheck"
              @click="openReview(row)"
            >
              论证
            </el-button>
            <el-button
              v-permission="'review:view'"
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
      v-model="reviewDialogVisible"
      title="项目论证"
      width="640px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="项目编码">
          <span class="readonly-text">{{ reviewingRow?.projectCode }}</span>
        </el-form-item>
        <el-form-item label="项目名称">
          <span class="readonly-text">{{ reviewingRow?.projectName }}</span>
        </el-form-item>
        <el-divider content-position="left">论证检查项</el-divider>
        <el-form-item label="信息完整性">
          <el-radio-group v-model="form.infoComplete">
            <el-radio value="完整">完整</el-radio>
            <el-radio value="不完整">不完整</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="三重一大">
          <el-radio-group v-model="form.threeImportant">
            <el-radio value="符合">符合</el-radio>
            <el-radio value="不符合">不符合</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="拆分立项">
          <el-radio-group v-model="form.splitProject">
            <el-radio value="无">无</el-radio>
            <el-radio value="存在">存在</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="界面混淆">
          <el-radio-group v-model="form.interfaceConfusion">
            <el-radio value="无">无</el-radio>
            <el-radio value="存在">存在</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="论证意见" prop="opinion">
          <el-input
            v-model="form.opinion"
            type="textarea"
            :rows="3"
            placeholder="请输入论证意见"
            maxlength="500"
          />
        </el-form-item>
        <el-form-item label="论证结果" prop="result">
          <el-select v-model="form.result" placeholder="请选择" style="width: 100%">
            <el-option label="通过" value="通过" />
            <el-option label="不通过" value="不通过" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewing" @click="handleReviewSubmit">提交论证</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="论证详情" width="680px">
      <el-descriptions v-loading="detailLoading" :column="2" border>
        <el-descriptions-item label="项目编码">{{ detailData?.project.projectCode ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="项目名称">{{ detailData?.project.projectName ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="二级分类">{{ detailData?.project.projectType ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="投资金额（万元）">
          {{ detailData ? yuanToWan(detailData.project.investmentAmount) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="所属单位">{{ detailData?.project.deptName ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="论证状态">
          <el-tag v-if="detailData" :type="reviewStatusTagType(detailData.project.status)" size="small">
            {{ reviewStatusLabel(detailData.project.status) }}
          </el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="信息完整性">
          <el-tag
            v-if="detailData?.review"
            :type="checkItemTagType('infoComplete', detailData.review.infoComplete)"
            size="small"
          >
            {{ detailData.review.infoComplete }}
          </el-tag>
          <span v-else class="empty-value">—</span>
        </el-descriptions-item>
        <el-descriptions-item label="三重一大">
          <el-tag
            v-if="detailData?.review"
            :type="checkItemTagType('threeImportant', detailData.review.threeImportant)"
            size="small"
          >
            {{ detailData.review.threeImportant }}
          </el-tag>
          <span v-else class="empty-value">—</span>
        </el-descriptions-item>
        <el-descriptions-item label="拆分立项">
          <el-tag
            v-if="detailData?.review"
            :type="checkItemTagType('splitProject', detailData.review.splitProject)"
            size="small"
          >
            {{ detailData.review.splitProject }}
          </el-tag>
          <span v-else class="empty-value">—</span>
        </el-descriptions-item>
        <el-descriptions-item label="界面混淆">
          <el-tag
            v-if="detailData?.review"
            :type="checkItemTagType('interfaceConfusion', detailData.review.interfaceConfusion)"
            size="small"
          >
            {{ detailData.review.interfaceConfusion }}
          </el-tag>
          <span v-else class="empty-value">—</span>
        </el-descriptions-item>
        <el-descriptions-item label="论证时间">{{ formatDateTime(detailData?.review?.reviewTime ?? '') }}</el-descriptions-item>
        <el-descriptions-item label="论证意见" :span="2">
          {{ detailData?.review?.opinion || '—' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<style scoped>
.review-page {
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

@media (max-width: 1100px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
