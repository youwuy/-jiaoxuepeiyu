<template>
  <AdminShell activeKey="permissions">
    <section class="admin-permission-page">
      <el-breadcrumb class="admin-permission-breadcrumb" separator="/">
        <el-breadcrumb-item>系统基础设置</el-breadcrumb-item>
        <el-breadcrumb-item>功能管理</el-breadcrumb-item>
      </el-breadcrumb>

      <section class="admin-permission-toolbar">
        <div class="admin-permission-toolbar-left">
          <el-input
            v-model="draftKeyword"
            class="admin-permission-search"
            :prefix-icon="Search"
            placeholder="搜索菜单名称/编码"
            clearable
            @keyup.enter="applySearch"
          />
          <el-button class="admin-permission-ghost-button" @click="applySearch">查询</el-button>
          <el-button class="admin-permission-ghost-button" @click="resetSearch">重置</el-button>
        </div>
        <el-button class="admin-permission-primary-button" type="primary" @click="openCreateRoot">
          <el-icon><Plus /></el-icon>
          新增菜单
        </el-button>
      </section>

      <section class="admin-permission-table-card">
        <div v-if="loading" class="admin-course-empty">功能菜单加载中...</div>
        <template v-else>
          <div class="admin-permission-table-scroll">
            <table class="admin-permission-table">
              <thead>
                <tr>
                  <th>排序</th>
                  <th></th>
                  <th>菜单名称</th>
                  <th>路由</th>
                  <th>展示状态</th>
                  <th>菜单类型</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in visibleRows" :key="row.permissionId">
                  <td>
                    <el-icon class="admin-permission-rank"><Rank /></el-icon>
                  </td>
                  <td>
                    <button
                      v-if="row.hasChildren"
                      type="button"
                      class="admin-permission-expand"
                      :aria-label="expandedIds.has(row.permissionId) ? '收起菜单' : '展开菜单'"
                      @click="toggleExpanded(row.permissionId)"
                    >
                      <el-icon><component :is="expandedIds.has(row.permissionId) ? ArrowDown : ArrowRight" /></el-icon>
                    </button>
                  </td>
                  <td>
                    <div class="admin-permission-name" :style="{ paddingLeft: `${row.level * 24}px` }">
                      <el-icon><component :is="permissionIcon(row.permissionType)" /></el-icon>
                      <strong>{{ row.permissionName }}</strong>
                    </div>
                  </td>
                  <td class="admin-permission-route">{{ row.routePath || '-' }}</td>
                  <td>
                    <span class="admin-permission-status" :class="row.visible ? 'shown' : 'hidden'">
                      <i></i>
                      {{ row.visible ? '显示' : '隐藏' }}
                    </span>
                  </td>
                  <td>{{ permissionTypeLabels[row.permissionType] }}</td>
                  <td>
                    <div class="admin-permission-actions">
                      <el-button class="admin-permission-action edit" @click="openEdit(row)">编辑</el-button>
                      <el-button
                        class="admin-permission-action"
                        :class="row.visible ? 'hide' : 'show'"
                        :loading="busyId === row.permissionId"
                        @click="toggleVisible(row)"
                      >
                        {{ row.visible ? '隐藏' : '显示' }}
                      </el-button>
                      <el-button class="admin-permission-action danger" :loading="busyId === row.permissionId" @click="removePermission(row)">
                        删除
                      </el-button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <footer class="admin-permission-footer">
            <p>
              共 <strong>{{ permissionTotal }}</strong> 条记录
            </p>
            <el-pagination :current-page="1" :page-size="10" :total="permissionTotal" layout="prev, pager, next" background />
          </footer>
        </template>
      </section>
    </section>

    <el-dialog
      v-model="dialogVisible"
      class="admin-permission-dialog"
      width="560px"
      :show-close="false"
      :close-on-click-modal="false"
      append-to-body
    >
      <template #header>
        <div class="admin-permission-dialog-head">
          <strong>{{ dialogTitle }}</strong>
          <el-button text circle :icon="Close" @click="dialogVisible = false" />
        </div>
      </template>

      <div class="admin-permission-form">
        <label class="admin-permission-field">
          <span>上级菜单</span>
          <el-select v-model="form.parentId" clearable placeholder="请选择上级菜单">
            <el-option label="无上级菜单" :value="null" />
            <el-option v-for="item in parentOptions" :key="item.permissionId" :label="item.label" :value="item.permissionId" />
          </el-select>
        </label>

        <div class="admin-permission-form-grid">
          <label class="admin-permission-field">
            <span>菜单名称 <b>*</b></span>
            <el-input v-model="form.permissionName" maxlength="20" placeholder="请输入菜单名称" />
          </label>
          <label class="admin-permission-field">
            <span>权限编码 <b>*</b></span>
            <el-input v-model="form.permissionCode" maxlength="50" placeholder="如 system:user:list" />
          </label>
        </div>

        <div class="admin-permission-form-grid">
          <label class="admin-permission-field">
            <span>菜单类型 <b>*</b></span>
            <el-select v-model="form.permissionType">
              <el-option label="一级菜单" value="MENU" />
              <el-option label="页面菜单" value="PAGE" />
              <el-option label="功能按钮" value="BUTTON" />
            </el-select>
          </label>
          <label class="admin-permission-field">
            <span>排序</span>
            <el-input-number v-model="form.sortOrder" :min="0" :max="999" controls-position="right" />
          </label>
        </div>

        <label class="admin-permission-field">
          <span>路由地址</span>
          <el-input v-model="form.routePath" placeholder="按钮可不填写" />
        </label>

        <label class="admin-permission-switch-row">
          <span>展示状态</span>
          <el-switch v-model="form.visible" active-text="显示" inactive-text="隐藏" />
        </label>
      </div>

      <template #footer>
        <div class="admin-permission-dialog-footer">
          <el-button class="admin-permission-dialog-cancel" @click="dialogVisible = false">取消</el-button>
          <el-button class="admin-permission-dialog-confirm" type="primary" :loading="saving" @click="savePermission">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowDown, ArrowRight, Close, Files, Menu, Mouse, Plus, Rank, Search } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  createAdminPermission,
  deleteAdminPermission,
  disableAdminPermission,
  enableAdminPermission,
  fetchAdminPermissionTree,
  updateAdminPermission,
  type AdminPermissionCommand,
  type AdminPermissionNode,
  type AdminPermissionType
} from '../../api/admin-permission';
import {
  collectAdminPermissionIds,
  countAdminPermissions,
  flattenAdminPermissionTree,
  mockAdminPermissions,
  permissionTypeLabels,
  type AdminPermissionRow
} from '../../features/admin/permissions';

type DialogMode = 'create' | 'edit';

const loading = ref(false);
const saving = ref(false);
const busyId = ref<number | null>(null);
const permissionTree = ref<AdminPermissionNode[]>(mockAdminPermissions);
const expandedIds = ref(new Set<number>(collectAdminPermissionIds(mockAdminPermissions)));
const draftKeyword = ref('');
const keyword = ref('');
const dialogVisible = ref(false);
const dialogMode = ref<DialogMode>('create');
const editingPermission = ref<AdminPermissionRow | null>(null);

const form = reactive<AdminPermissionCommand>({
  parentId: null,
  permissionName: '',
  permissionCode: '',
  permissionType: 'MENU',
  routePath: '',
  visible: true,
  sortOrder: 0
});

const visibleRows = computed(() => flattenAdminPermissionTree(permissionTree.value, expandedIds.value, draftKeyword.value || keyword.value));
const permissionTotal = computed(() => countAdminPermissions(permissionTree.value));
const dialogTitle = computed(() => (dialogMode.value === 'edit' ? '编辑菜单' : '新增菜单'));
const parentOptions = computed(() =>
  flattenAll(permissionTree.value)
    .filter((item) => item.permissionType !== 'BUTTON' && item.permissionId !== editingPermission.value?.permissionId)
    .map((item) => ({ ...item, label: `${'　'.repeat(findLevel(item.permissionId))}${item.permissionName}` }))
);

function permissionIcon(type: AdminPermissionType) {
  if (type === 'BUTTON') {
    return Mouse;
  }
  if (type === 'PAGE') {
    return Files;
  }
  return Menu;
}

function applySearch() {
  keyword.value = draftKeyword.value.trim();
}

function resetSearch() {
  draftKeyword.value = '';
  keyword.value = '';
}

function toggleExpanded(permissionId: number) {
  const next = new Set(expandedIds.value);
  if (next.has(permissionId)) {
    next.delete(permissionId);
  } else {
    next.add(permissionId);
  }
  expandedIds.value = next;
}

function openCreateRoot() {
  dialogMode.value = 'create';
  editingPermission.value = null;
  Object.assign(form, {
    parentId: null,
    permissionName: '',
    permissionCode: '',
    permissionType: 'MENU',
    routePath: '',
    visible: true,
    sortOrder: nextSortOrder(null)
  });
  dialogVisible.value = true;
}

function openEdit(row: AdminPermissionRow) {
  dialogMode.value = 'edit';
  editingPermission.value = row;
  Object.assign(form, {
    parentId: row.parentId ?? null,
    permissionName: row.permissionName,
    permissionCode: row.permissionCode,
    permissionType: row.permissionType,
    routePath: row.routePath ?? '',
    visible: row.visible,
    sortOrder: row.sortOrder
  });
  dialogVisible.value = true;
}

function nextSortOrder(parentId: number | null) {
  const siblings = flattenAll(permissionTree.value).filter((item) => (item.parentId ?? null) === parentId);
  return siblings.length + 1;
}

function findLevel(permissionId: number, items = permissionTree.value, level = 0): number {
  for (const item of items) {
    if (item.permissionId === permissionId) {
      return level;
    }
    const childLevel = findLevel(permissionId, item.children ?? [], level + 1);
    if (childLevel > -1) {
      return childLevel;
    }
  }
  return -1;
}

function flattenAll(items: AdminPermissionNode[]): AdminPermissionNode[] {
  return items.flatMap((item) => [item, ...flattenAll(item.children ?? [])]);
}

function validateForm(): AdminPermissionCommand {
  const permissionName = form.permissionName.trim();
  const permissionCode = form.permissionCode.trim();
  const routePath = form.routePath?.trim() || null;

  if (!permissionName) {
    throw new Error('请输入菜单名称');
  }
  if (!permissionCode) {
    throw new Error('请输入权限编码');
  }

  return {
    parentId: form.parentId ?? null,
    permissionName,
    permissionCode,
    permissionType: form.permissionType,
    routePath,
    visible: form.visible,
    sortOrder: form.sortOrder || 0
  };
}

async function savePermission() {
  let payload: AdminPermissionCommand;
  try {
    payload = validateForm();
  } catch (error) {
    ElMessage.warning(error instanceof Error ? error.message : '请完善菜单信息');
    return;
  }

  saving.value = true;
  try {
    if (dialogMode.value === 'edit' && editingPermission.value) {
      await updateAdminPermission(editingPermission.value.permissionId, payload);
      ElMessage.success('菜单已更新');
    } else {
      await createAdminPermission(payload);
      ElMessage.success('菜单已新增');
    }
    dialogVisible.value = false;
    await loadPermissionTree();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '菜单保存失败');
  } finally {
    saving.value = false;
  }
}

async function toggleVisible(row: AdminPermissionRow) {
  busyId.value = row.permissionId;
  try {
    if (row.visible) {
      await disableAdminPermission(row.permissionId);
      ElMessage.success('菜单已隐藏');
    } else {
      await enableAdminPermission(row.permissionId);
      ElMessage.success('菜单已显示');
    }
    await loadPermissionTree();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '状态更新失败');
  } finally {
    busyId.value = null;
  }
}

async function removePermission(row: AdminPermissionRow) {
  try {
    await ElMessageBox.confirm(`确认删除菜单「${row.permissionName}」？`, '删除菜单', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    });
  } catch {
    return;
  }

  busyId.value = row.permissionId;
  try {
    await deleteAdminPermission(row.permissionId);
    ElMessage.success('菜单已删除');
    await loadPermissionTree();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '删除失败');
  } finally {
    busyId.value = null;
  }
}

async function loadPermissionTree() {
  loading.value = true;
  try {
    const result = await fetchAdminPermissionTree();
    permissionTree.value = result.length > 0 ? result : mockAdminPermissions;
    expandedIds.value = new Set(collectAdminPermissionIds(permissionTree.value));
  } catch {
    permissionTree.value = mockAdminPermissions;
    expandedIds.value = new Set(collectAdminPermissionIds(mockAdminPermissions));
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadPermissionTree();
});
</script>
