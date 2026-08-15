<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Promotion, View } from '@element-plus/icons-vue'
import { yuanToWan, yuanToYi } from '../../utils/format'
import { deptTree, projectTypeDict } from '../../api/project'
import {
  detail as reserveDetail,
  exportCsv as exportReserveCsv,
  issue as issueReserve,
  page as pageReserve,
  stats as reserveStats,
} from '../../api/reserve'
import type {
  ProjectStatus,
  ProjectVO,
  ReserveDetailVO,
  ReserveStatsVO,
  SysDept,
  SysDictData,
} from '../../types'
import StatCard from '../../components/StatCard.vue'
import SmartPagination from '../../components/SmartPagination.vue'
import CategoryPie from './components/CategoryPie.vue'
import DeptBar from './components/DeptBar.vue'

/**
 * 统一储备库页：统计卡片、分类/单位图表、筛选、列表、下达、详情、导出。
 *
 * @author demo
 * @since 2026-08-15
 */

/** 默认每页条数。 */
const PAGE_SIZE = 5

/** Element Plus 标签配色类型。 */
type TagType = 'primary' | 'success' | 'warning' | 'danger' | 'info'

/** 状态筛选选项。 */
const statusOptions: Array<{ value: ProjectStatus; label: string }> = [
  { value: '待下达', label: '待下达' },
  { value: '已下达', label: '已下达' },
]

/** 筛选条件。 */
interface ReserveFilters {
  name: string
  projectType: string
  deptId: number | undefined
  status: string
}

const loading = ref(false)
const stats = ref<ReserveStatsVO>({
  total: 0,
  totalAmountYuan: 0,
  pending: 0,
  issued: 0,
  categoryDist: [],
  deptDist: [],
})
const projects = ref<ProjectVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const projectTypes = ref<SysDictData[]>([])
const depts = ref<SysDept[]>([])

const filters = reactive<ReserveFilters>({
  name: '',
  projectType: '',
  deptId: undefined,
  status: '',
})

const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<ReserveDetailVO | null>(null)
const issuingId = ref<number | null>(null)

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
function buildQuery(): Parameters<typeof pageReserve>[0] {
  return {
    pageNum: currentPage.value,
    pageSize: PAGE_SIZE,
    name: filters.name || undefined,
    projectType: filters.projectType || undefined,
    deptId: filters.deptId || undefined,
    status: filters.status || undefined,
  }
}

/** 加载统计卡片与图表数据。 */
async function loadStats(): Promise<void> {
  try {
    const res = await reserveStats()
    stats.value = res.data
  } catch {
    // 拦截器已提示，统计失败不阻塞列表。
  }
}

/** 加载储备库列表。 */
async function loadProjects(): Promise<void> {
  loading.value = true
  try {
    const res = await pageReserve(buildQuery())
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

/** 状态标签配色（待下达=warning，已下达=success）。 */
function statusTagType(status: ProjectStatus): TagType {
  return status === '已下达' ? 'success' : 'warning'
}

/** 论证 / 审核结论标签配色（通过=success）。 */
function resultTagType(result: string): TagType {
  return result === '通过' ? 'success' : 'danger'
}

/** 时间字符串展示（ISO 转空格分隔）。 */
function formatDateTime(value: string): string {
  if (!value) {
    return '-'
  }
  return value.replace('T', ' ')
}

/** 下达确认（绿色）。 */
async function confirmIssue(message: string): Promise<boolean> {
  try {
    await ElMessageBox.confirm(message, '确认下达', {
      type: 'success',
      confirmButtonText: '确认下达',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--success',
    })
    return true
  } catch {
    return false
  }
}

/** 下达项目。 */
async function handleIssue(row: ProjectVO): Promise<void> {
  const ok = await confirmIssue(`确定要下达项目「${row.projectName}」吗？下达后项目进入计划执行阶段。`)
  if (!ok) {
    return
  }
  issuingId.value = row.id
  try {
    await issueReserve(row.id)
    ElMessage.success('下达成功')
    currentPage.value = 1
    await refresh()
  } catch {
    // 拦截器已统一提示，此处不重复处理。
  } finally {
    issuingId.value = null
  }
}

/** 打开详情弹窗（项目 + 论证/审核状态 + 下达时间）。 */
async function openDetail(row: ProjectVO): Promise<void> {
  detailDialogVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    const res = await reserveDetail(row.id)
    detailData.value = res.data
  } catch {
    detailData.value = null
  } finally {
    detailLoading.value = false
  }
}

/** 导出 CSV。 */
async function handleExport(): Promise<void> {
  try {
    await exportReserveCsv(buildQuery())
  } catch {
    // 拦截器已统一提示，此处不重复处理。
  }
}

onMounted(() => {
  refresh()
  loadDicts()
})
</script>

<template>
  <div class="reserve-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">统一储备库</h1>
        <p class="page-description">经过审核的储备项目，纳入统一储备库，具备计划下达条件</p>
      </div>
    </div>

    <div class="stats-grid">
      <StatCard title="储备库项目总数" :value="stats.total" color="primary" icon="Collection" />
      <StatCard title="总投资（亿元）" :value="yuanToYi(stats.totalAmountYuan)" color="success" icon="Coin" />
      <StatCard title="待下达" :value="stats.pending" color="warning" icon="Clock" />
      <StatCard title="已下达" :value="stats.issued" color="info" icon="Promotion" />
    </div>

    <div class="chart-grid">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">项目分类分布</h3>
        </div>
        <div class="chart-body">
          <CategoryPie :data="stats.categoryDist" />
        </div>
      </div>
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">各单位项目分布</h3>
        </div>
        <div class="chart-body">
          <DeptBar :data="stats.deptDist" />
        </div>
      </div>
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
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <h3 class="card-title">项目列表</h3>
        <div class="card-actions">
          <el-button v-permission="'reserve:export'" :icon="Download" @click="handleExport">
            导出
          </el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="projects" row-key="id">
        <el-table-column prop="projectCode" label="项目编码" min-width="130" show-overflow-tooltip />
        <el-table-column prop="projectName" label="项目名称" min-width="200" show-overflow-tooltip />
        <el-table-column label="二级分类" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.projectType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="投资金额（万元）" width="160" align="right">
          <template #default="{ row }">
            {{ yuanToWan(row.investmentAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="所属单位" width="140" show-overflow-tooltip />
        <el-table-column label="入库时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === '待下达'"
              v-permission="'reserve:issue'"
              link
              type="success"
              :loading="issuingId === row.id"
              :icon="Promotion"
              @click="handleIssue(row)"
            >
              下达
            </el-button>
            <el-button
              v-permission="'reserve:view'"
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

    <el-dialog v-model="detailDialogVisible" title="项目详情" width="680px">
      <el-descriptions v-loading="detailLoading" :column="2" border>
        <el-descriptions-item label="项目编码">
          {{ detailData?.project.projectCode ?? '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="项目名称">
          {{ detailData?.project.projectName ?? '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="二级分类">
          {{ detailData?.project.projectType ?? '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="投资金额（万元）">
          {{ detailData ? yuanToWan(detailData.project.investmentAmount) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="所属单位">
          {{ detailData?.project.deptName ?? '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="入库时间">
          {{ formatDateTime(detailData?.project.createTime ?? '') }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="detailData" :type="statusTagType(detailData.project.status)" size="small">
            {{ detailData.project.status }}
          </el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="论证状态">
          <el-tag v-if="detailData?.review" :type="resultTagType(detailData.review.result)" size="small">
            {{ detailData.review.result }}
          </el-tag>
          <span v-else class="empty-value">—</span>
        </el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag v-if="detailData?.audit" :type="resultTagType(detailData.audit.result)" size="small">
            {{ detailData.audit.result }}
          </el-tag>
          <span v-else class="empty-value">—</span>
        </el-descriptions-item>
        <el-descriptions-item label="下达时间">
          {{ formatDateTime(detailData?.project.issueTime ?? '') }}
        </el-descriptions-item>
        <el-descriptions-item label="项目描述" :span="2">
          {{ detailData?.project.description || '—' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<style scoped>
.reserve-page {
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

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  overflow: hidden;
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

.chart-body {
  padding: 0.5rem 1rem 1rem;
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

.empty-value {
  color: var(--text-muted);
}

@media (max-width: 1100px) {
  .stats-grid,
  .chart-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
