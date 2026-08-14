<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { ElTree } from 'element-plus'
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import {
  createRole,
  menuList,
  removeRole,
  roleList,
  updateRole,
} from '../../../api/system'
import type { RoleDTO, SysMenu, SysRole } from '../../../types'

/**
 * 角色管理页：角色列表、新增/编辑（含权限树分配菜单）。
 *
 * @author demo
 * @since 2026-08-15
 */

/** 启用标记。 */
const ENABLED = 1

/** 新增/编辑表单模型。 */
interface RoleFormModel {
  roleName: string
  roleKey: string
  sort: number
}

const loading = ref(false)
const roles = ref<SysRole[]>([])

const menuTree = ref<SysMenu[]>([])

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const editingId = ref<number | null>(null)
const saving = ref(false)
const formRef = ref<FormInstance>()
const treeRef = ref<InstanceType<typeof ElTree>>()
/** 弹窗打开时的已勾选菜单（编辑时回填角色已有 menuIds）。 */
const checkedMenuIds = ref<number[]>([])
/** 弹窗每次打开自增，触发权限树重挂载以应用 default-checked-keys。 */
const treeVersion = ref(0)
const form = reactive<RoleFormModel>(emptyForm())

/** 空表单模型。 */
function emptyForm(): RoleFormModel {
  return {
    roleName: '',
    roleKey: '',
    sort: 0,
  }
}

const rules: FormRules<RoleFormModel> = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入角色标识', trigger: 'blur' }],
}

/** 角色状态文案。 */
function statusLabel(status: number): string {
  return status === ENABLED ? '启用' : '停用'
}

/** 角色状态标签配色。 */
function statusTagType(status: number): 'success' | 'info' {
  return status === ENABLED ? 'success' : 'info'
}

/** 加载角色列表。 */
async function loadRoles(): Promise<void> {
  loading.value = true
  try {
    const res = await roleList()
    roles.value = res.data ?? []
  } catch {
    roles.value = []
  } finally {
    loading.value = false
  }
}

/** 加载菜单树。 */
async function loadMenuTree(): Promise<void> {
  try {
    const res = await menuList()
    menuTree.value = res.data ?? []
  } catch {
    menuTree.value = []
  }
}

/** 打开新增弹窗。 */
function openCreate(): void {
  dialogMode.value = 'create'
  editingId.value = null
  Object.assign(form, emptyForm())
  checkedMenuIds.value = []
  treeVersion.value += 1
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

/** 打开编辑弹窗。 */
function openEdit(row: SysRole): void {
  dialogMode.value = 'edit'
  editingId.value = row.id
  Object.assign(form, {
    roleName: row.roleName,
    roleKey: row.roleKey,
    sort: row.sort,
  })
  checkedMenuIds.value = row.menuIds ?? []
  treeVersion.value += 1
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
    // 勾选叶子（按钮）+ 半选父级（目录/菜单），确保上级目录一并保存。
    const checked = (treeRef.value?.getCheckedKeys() ?? []).map((key) => Number(key))
    const halfChecked = (treeRef.value?.getHalfCheckedKeys() ?? []).map((key) => Number(key))
    const menuIds = Array.from(new Set([...checked, ...halfChecked]))
    const dto: RoleDTO = {
      roleName: form.roleName,
      roleKey: form.roleKey,
      sort: form.sort,
      menuIds,
    }
    if (dialogMode.value === 'create') {
      await createRole(dto)
      ElMessage.success('新增成功')
    } else {
      await updateRole(editingId.value as number, dto)
      ElMessage.success('保存成功')
    }
    dialogVisible.value = false
    await loadRoles()
  } catch {
    // 拦截器已统一提示，此处不重复处理。
  } finally {
    saving.value = false
  }
}

/** 删除角色。 */
async function handleDelete(row: SysRole): Promise<void> {
  let ok = false
  try {
    await ElMessageBox.confirm(`确定要删除角色「${row.roleName}」吗？此操作不可恢复。`, '确认删除', {
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
    await removeRole(row.id)
    ElMessage.success('删除成功')
    await loadRoles()
  } catch {
    // 拦截器已统一提示，此处不重复处理。
  }
}

onMounted(() => {
  loadRoles()
  loadMenuTree()
})
</script>

<template>
  <div class="system-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">角色管理</h1>
        <p class="page-description">维护系统角色，并为角色分配菜单与按钮权限</p>
      </div>
      <el-button v-permission="'system:role'" type="primary" :icon="Plus" @click="openCreate">
        新增角色
      </el-button>
    </div>

    <div class="card">
      <div class="card-header">
        <h3 class="card-title">角色列表</h3>
      </div>

      <el-table v-loading="loading" :data="roles" row-key="id">
        <el-table-column type="index" label="序号" width="64" />
        <el-table-column prop="roleName" label="角色名" min-width="140" show-overflow-tooltip />
        <el-table-column prop="roleKey" label="角色键" min-width="140" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="80" align="right" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-permission="'system:role'"
              link
              type="primary"
              title="编辑"
              @click="openEdit(row)"
            >
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button
              v-permission="'system:role'"
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
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增角色' : '编辑角色'"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" maxlength="64" clearable />
        </el-form-item>
        <el-form-item label="角色标识" prop="roleKey">
          <el-input v-model="form.roleKey" placeholder="请输入角色标识" maxlength="64" clearable />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :step="1" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="权限分配" prop="menuIds">
          <div class="menu-tree">
            <el-tree
              ref="treeRef"
              :key="treeVersion"
              :data="menuTree"
              :props="{ label: 'menuName', children: 'children' }"
              node-key="id"
              show-checkbox
              default-expand-all
              :default-checked-keys="checkedMenuIds"
            />
          </div>
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

.menu-tree {
  width: 100%;
  max-height: 320px;
  overflow-y: auto;
  padding: 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: 6px;
}
</style>
