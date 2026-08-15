<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { runDetail } from '../../../api/autotest'
import type { RunDetailVO, StepStatus } from '../../../types'
import StatusTag from '../../../components/StatusTag.vue'

/**
 * 自动化测试运行结果页：步骤结果列表 + 失败详情 + 轮询。
 *
 * @author demo
 * @since 2026-08-15
 */

const route = useRoute()
const runId = Number(route.params.id)

const loading = ref(false)
const detail = ref<RunDetailVO | null>(null)
let timer: ReturnType<typeof setInterval> | null = null

/** 运行状态标签文案与配色。 */
function runStatusLabel(status: number): string {
  if (status === 0) return '运行中'
  if (status === 1) return '成功'
  return '失败'
}

function runStatusType(status: number): 'warning' | 'success' | 'danger' {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  return 'danger'
}

function stepStatusLabel(status: StepStatus): string {
  if (status === 0) return '通过'
  if (status === 1) return '失败'
  return '跳过'
}

function stepStatusType(status: StepStatus): 'success' | 'danger' | 'info' {
  if (status === 0) return 'success'
  if (status === 1) return 'danger'
  return 'info'
}

function formatTime(value: string | null): string {
  return value ? value.replace('T', ' ') : '-'
}

function prettyJson(value: string | null): string {
  if (!value) return '-'
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
}

async function loadDetail(): Promise<void> {
  loading.value = true
  try {
    const res = await runDetail(runId)
    detail.value = res.data
    if (res.data.status === 0) {
      startPolling()
    } else {
      stopPolling()
    }
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

function startPolling(): void {
  if (timer) return
  timer = setInterval(async () => {
    try {
      const res = await runDetail(runId)
      detail.value = res.data
      if (res.data.status !== 0) {
        stopPolling()
      }
    } catch {
      stopPolling()
    }
  }, 1000)
}

function stopPolling(): void {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

onMounted(loadDetail)
onBeforeUnmount(stopPolling)
</script>

<template>
  <div class="run-detail-page">
    <div class="page-header">
      <h1 class="page-title">运行结果</h1>
      <button class="btn btn-outline" @click="$router.back()">返回</button>
    </div>

    <div v-if="detail" class="card summary-card">
      <div class="summary-row">
        <span class="summary-label">运行编号</span>
        <span>{{ detail.id }}</span>
        <span class="summary-label">状态</span>
        <StatusTag :type="runStatusType(detail.status)">{{ runStatusLabel(detail.status) }}</StatusTag>
        <span class="summary-label">开始时间</span>
        <span>{{ formatTime(detail.startTime) }}</span>
        <span class="summary-label">结束时间</span>
        <span>{{ formatTime(detail.endTime) }}</span>
      </div>
      <div v-if="detail.errorMsg" class="summary-error">失败摘要：{{ detail.errorMsg }}</div>
    </div>

    <div class="card" v-loading="loading">
      <div class="card-header">
        <h3 class="card-title">步骤结果</h3>
      </div>
      <el-table :data="detail?.steps ?? []" row-key="id">
        <el-table-column prop="stepOrder" label="顺序" width="70" />
        <el-table-column prop="name" label="步骤名" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :type="stepStatusType(row.status)">{{ stepStatusLabel(row.status) }}</StatusTag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="200">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} ~ {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="step-detail">
              <div class="detail-block">
                <h4>请求快照</h4>
                <pre>{{ prettyJson(row.requestSnapshot) }}</pre>
              </div>
              <div class="detail-block">
                <h4>响应快照</h4>
                <pre>{{ prettyJson(row.responseSnapshot) }}</pre>
              </div>
              <div v-if="row.assertDetail" class="detail-block">
                <h4>断言明细</h4>
                <pre>{{ row.assertDetail }}</pre>
              </div>
              <div v-if="row.errorMsg" class="detail-block">
                <h4>错误信息</h4>
                <pre>{{ row.errorMsg }}</pre>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.run-detail-page {
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

.card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  overflow: hidden;
}

.card-header {
  padding: 1rem;
  border-bottom: 1px solid var(--border-color);
}

.card-title {
  margin: 0;
  font-size: 1rem;
  color: var(--text-primary);
}

.summary-card {
  padding: 1rem;
}

.summary-row {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
  color: var(--text-primary);
}

.summary-label {
  color: var(--text-secondary);
  font-size: 0.85rem;
}

.summary-error {
  margin-top: 0.5rem;
  color: var(--danger-color, #f56c6c);
}

.step-detail {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.detail-block h4 {
  margin: 0 0 0.35rem;
  color: var(--text-primary);
}

.detail-block pre {
  margin: 0;
  padding: 0.75rem;
  background-color: var(--bg-hover);
  border-radius: 6px;
  font-size: 0.8rem;
  overflow-x: auto;
  color: var(--text-primary);
}
</style>
