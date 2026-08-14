<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import { createMenu, menuList, removeMenu, updateMenu } from '../../../api/system'
import type { MenuType, SysMenu, SysMenuSaveDTO } from '../../../types'

/**
 * 菜单管理页：树形菜单列表、新增/编辑/删除。
 *
 * @author demo
 * @since 2026-08-15
 */

/** 启用标记。 */
const ENABLED = 1

/** 菜单类型选项。 */
const MENU_TYPE_OPTIONS: Array<{ value: MenuType; label: string }> = [
  { value: 'M', label: '目录' },
  { value: 'C', label: '菜单' },
  { value: 'F', label: '按钮' },
]

/** 新增/编辑表单模型。 */
interface MenuFormModel {
  menuName: string
  menuType: '' | MenuType
  parentId: number | undefined
  perms: string
  path: string
  sort: number
  icon: string
}

const loading = ref(false)
const menus = ref<SysMenu[]>([])

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const editingId = ref<number | null>(null)
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<MenuFormModel>(emptyForm())

/** 空表单模型。 */
function emptyForm(): MenuFormModel {
  return {
    menuName: '',
    menuType: '',
    parentId: undefined,
    perms: '',
    path: '',
    sort: 0,
    icon: '',
  }
}

const rules: FormRules<MenuFormModel> = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
}

/** 菜单类型文案。 */
function menuTypeLabel(type: MenuType): string {
  const item = MENU_TYPE_OPTIONS.find((option) => option.value === type)
  return item?.label ?? type
}

/** 菜单类型标签配色。 */
function menuTypeTagType(type: MenuType): 'primary' | 'success' | 'warning' {
  if (type === 'M') {
    return 'primary'
  }
  if (type === 'C') {
    return 'success'
  }
  return 'warning'
}

/** 状态文案。 */
function statusLabel(status: number): string {
  return status === ENABLED ? '启用' : '停用'
}

/** 状态标签配色。 */
function statusTagType(status: number): 'success' | 'info' {
  return status === ENABLED ? 'success' : 'info'
}

/** 空值占位展示。 */
function textOrDash(value: string | null | undefined): string {
  return value || '-'
}

/** 加载菜单树。 */
async function loadMenus(): Promise<void> {
  loading.value = true
  try {
    const res = await menuList()
    menus.value = res.data ?? []
  } catch {
    menus.value = []
  } finally {
    loading.value = false
  }
}

/** 打开新增弹窗（默认挂到当前选中的父级下）。 */
function openCreate(parentId?: number): void {
  dialogMode.value = 'create'
  editingId.value = null
  Object.assign(form, emptyForm(), { parentId: parentId ?? undefined })
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

/** 打开编辑弹窗。 */
function openEdit(row: SysMenu): void {
  dialogMode.value = 'edit'
  editingId.value = row.id
  Object.assign(form, {
    menuName: row.menuName,
    menuType: row.menuType,
    parentId: row.parentId || undefined,
    perms: row.perms ?? '',
    path: row.path ?? '',
    sort: row.sort,
    icon: row.icon ?? '',
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
    const dto: SysMenuSaveDTO = {
      menuName: form.menuName,
      menuType: form.menuType as MenuType,
      parentId: form.parentId ?? 0,
      perms: form.perms,
      path: form.path,
      sort: form.sort,
      icon: form.icon,
    }
    if (dialogMode.value === 'create') {
      await createMenu(dto)
      ElMessage.success('新增成功')
    } else {
      await updateMenu(editingId.value as number, dto)
      ElMessage.success('保存成功')
    }
    dialogVisible.value = false
    await loadMenus()
  } catch {
    // 拦截器已统一提示，此处不重复处理。
  } finally {
    saving.value = false
  }
}

/** 删除菜单。 */
async function handleDelete(row: SysMenu): Promise<void> {
  let ok = false
  try {
    await ElMessageBox.confirm(`确定要删除菜单「${row.menuName}」吗？此操作不可恢复。`, '确认删除', {
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
    await removeMenu(row.id)
    ElMessage.success('删除成功')
    await loadMenus()
  } catch {
    // 拦截器已统一提示，此处不重复处理。
  }
}

onMounted(loadMenus)
</script>

<template>
  <div class="system-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">菜单管理</h1>
        <p class="page-description">维护系统目录、菜单与按钮权限</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate()">新增菜单</el-button>
    </div>

    <div class="card">
      <div class="card-header">
        <h3 class="card-title">菜单列表</h3>
      </div>

      <el-table
        v-loading="loading"
        :data="menus"
        row-key="id"
        default-expand-all
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="200" show-overflow-tooltip />
        <el-table-column label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="menuTypeTagType(row.menuType)" size="small">
              {{ menuTypeLabel(row.menuType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="权限标识" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            {{ textOrDash(row.perms) }}
          </template>
        </el-table-column>
        <el-table-column label="路径" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            {{ textOrDash(row.path) }}
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="right" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" title="新增下级" @click="openCreate(row.id)">
              <el-icon><Plus /></el-icon>
            </el-button>
            <el-button link type="primary" title="编辑" @click="openEdit(row)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button link type="danger" title="删除" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增菜单' : '编辑菜单'"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" maxlength="64" clearable />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-select v-model="form.menuType" placeholder="请选择菜单类型" style="width: 100%">
            <el-option
              v-for="item in MENU_TYPE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="上级菜单" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="menus"
            :props="{ label: 'menuName', children: 'children' }"
            node-key="id"
            check-strictly
            :render-after-expand="false"
            placeholder="顶级菜单"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="权限标识" prop="perms">
          <el-input v-model="form.perms" placeholder="如 system:user" maxlength="100" clearable />
        </el-form-item>
        <el-form-item label="路由路径" prop="path">
          <el-input v-model="form.path" placeholder="如 /system/user" maxlength="200" clearable />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :step="1" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="如 Setting" maxlength="64" clearable />
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
</style>
