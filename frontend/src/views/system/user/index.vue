<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Delete, Edit, Plus, Search } from '@element-plus/icons-vue'
import {
  createUser,
  deptTree,
  removeUser,
  roleList,
  updateUser,
  userPage,
} from '../../../api/system'
import type { SysDept, SysRole, SysUserDTO, SysUserVO } from '../../../types'
import SmartPagination from '../../../components/SmartPagination.vue'

/**
 * 用户管理页：用户列表、新增/编辑（含角色分配）、删除。
 *
 * @author demo
 * @since 2026-08-15
 */

/** 默认每页条数。 */
const PAGE_SIZE = 10

/** 启用标记。 */
const ENABLED = 1

/** 新增/编辑表单模型。 */
interface UserFormModel {
  username: string
  password: string
  nickname: string
  deptId: number | undefined
  roleIds: number[]
  status: number
}

const loading = ref(false)
const users = ref<SysUserVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const usernameFilter = ref('')

const depts = ref<SysDept[]>([])
const roles = ref<SysRole[]>([])

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const editingId = ref<number | null>(null)
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<UserFormModel>(emptyForm())

/** 空表单模型。 */
function emptyForm(): UserFormModel {
  return {
    username: '',
    password: '',
    nickname: '',
    deptId: undefined,
    roleIds: [],
    status: ENABLED,
  }
}

/** 初始密码校验：仅新增时必填。 */
function validatePassword(
  _rule: unknown,
  value: unknown,
  callback: (error?: string | Error) => void,
): void {
  if (dialogMode.value === 'create' && !value) {
    callback(new Error('请输入初始密码'))
  } else {
    callback()
  }
}

const rules: FormRules<UserFormModel> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }],
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

/** 部门 ID → 名称映射（用于列表「所属单位」展示）。 */
const deptNameMap = computed(() => {
  const map = new Map<number, string>()
  const walk = (nodes: SysDept[]): void => {
    for (const node of nodes) {
      map.set(node.id, node.deptName)
      if (node.children && node.children.length > 0) {
        walk(node.children)
      }
    }
  }
  walk(depts.value)
  return map
})

/** 用户状态文案。 */
function statusLabel(status: number): string {
  return status === ENABLED ? '启用' : '停用'
}

/** 用户状态标签配色。 */
function statusTagType(status: number): 'success' | 'info' {
  return status === ENABLED ? 'success' : 'info'
}

/** 加载用户列表。 */
async function loadUsers(): Promise<void> {
  loading.value = true
  try {
    const res = await userPage({
      pageNum: currentPage.value,
      pageSize: PAGE_SIZE,
      username: usernameFilter.value || undefined,
    })
    users.value = res.data?.list ?? []
    total.value = res.data?.total ?? 0
  } catch {
    users.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/** 加载部门树与角色列表。 */
async function loadOptions(): Promise<void> {
  try {
    const [deptRes, roleRes] = await Promise.all([deptTree(), roleList()])
    depts.value = deptRes.data ?? []
    roles.value = roleRes.data ?? []
  } catch {
    // 拦截器已提示，回退为空选项。
  }
}

/** 用户名筛选变化：回到第一页并刷新。 */
function handleFilterChange(): void {
  currentPage.value = 1
  loadUsers()
}

/** 分页切换。 */
function handlePageChange(page: number): void {
  currentPage.value = page
  loadUsers()
}

/** 序号（跨页累计）。 */
function indexMethod(index: number): number {
  return (currentPage.value - 1) * PAGE_SIZE + index + 1
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
function openEdit(row: SysUserVO): void {
  dialogMode.value = 'edit'
  editingId.value = row.id
  Object.assign(form, {
    username: row.username,
    password: '',
    nickname: row.nickname || '',
    deptId: row.deptId,
    roleIds: row.roleIds ?? [],
    status: row.status,
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
    const dto: SysUserDTO = {
      username: form.username,
      password: form.password || undefined,
      nickname: form.nickname,
      deptId: form.deptId,
      status: form.status,
      roleIds: form.roleIds,
    }
    if (dialogMode.value === 'create') {
      await createUser(dto)
      ElMessage.success('新增成功')
    } else {
      await updateUser(editingId.value as number, dto)
      ElMessage.success('保存成功')
    }
    dialogVisible.value = false
    currentPage.value = 1
    await loadUsers()
  } catch {
    // 拦截器已统一提示，此处不重复处理。
  } finally {
    saving.value = false
  }
}

/** 删除用户。 */
async function handleDelete(row: SysUserVO): Promise<void> {
  let ok = false
  try {
    await ElMessageBox.confirm(`确定要删除用户「${row.username}」吗？此操作不可恢复。`, '确认删除', {
      type: 'error',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger',
    })
    ok = true
  } catch {
    ok = false
  }
  if (!ok) {
    return
  }
  try {
    await removeUser(row.id)
    ElMessage.success('删除成功')
    currentPage.value = 1
    await loadUsers()
  } catch {
    // 拦截器已统一提示，此处不重复处理。
  }
}

onMounted(() => {
  loadUsers()
  loadOptions()
})
</script>

<template>
  <div class="system-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">用户管理</h1>
        <p class="page-description">维护系统用户，支持新增、编辑、删除及角色分配</p>
      </div>
      <el-button v-permission="'system:user'" type="primary" :icon="Plus" @click="openCreate">
        新增用户
      </el-button>
    </div>

    <div class="card">
      <div class="filter-bar">
        <div class="filter-item filter-search">
          <label class="filter-label">用户名</label>
          <el-input
            v-model="usernameFilter"
            placeholder="输入用户名搜索..."
            clearable
            @change="handleFilterChange"
            @keyup.enter="handleFilterChange"
          >
            <template #suffix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <h3 class="card-title">用户列表</h3>
      </div>

      <el-table v-loading="loading" :data="users" row-key="id">
        <el-table-column type="index" label="序号" width="64" :index="indexMethod" />
        <el-table-column prop="username" label="用户名" min-width="140" show-overflow-tooltip />
        <el-table-column prop="nickname" label="昵称" min-width="140" show-overflow-tooltip />
        <el-table-column label="所属单位" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            {{ deptNameMap.get(row.deptId) ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-permission="'system:user'"
              link
              type="primary"
              title="编辑"
              @click="openEdit(row)"
            >
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button
              v-permission="'system:user'"
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
      :title="dialogMode === 'create' ? '新增用户' : '编辑用户'"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" maxlength="64" clearable />
        </el-form-item>
        <el-form-item v-if="dialogMode === 'create'" label="初始密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入初始密码"
            maxlength="64"
            show-password
          />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" maxlength="64" clearable />
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
        <el-form-item label="角色" prop="roleIds">
          <el-select
            v-model="form.roleIds"
            placeholder="请选择角色"
            multiple
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="item in roles"
              :key="item.id"
              :label="item.roleName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="停用"
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
.system-page {
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
}

.filter-label {
  color: var(--text-secondary);
  font-size: 0.85rem;
}

.filter-search {
  min-width: 240px;
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
</style>
