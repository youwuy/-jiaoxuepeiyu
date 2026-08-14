<template>
  <AdminShell activeKey="permissions">
    <section class="admin-permission-page">
      <h1 class="admin-permission-title">功能管理</h1>

      <el-breadcrumb class="admin-permission-breadcrumb" separator="/">
        <el-breadcrumb-item>系统基础设置</el-breadcrumb-item>
        <el-breadcrumb-item>功能管理</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="admin-permission-layout">
        <section class="admin-permission-board">
          <header class="admin-permission-board-head">
            <strong>菜单权限树</strong>
            <el-button class="admin-permission-primary-button" type="primary" :disabled="!can('create')" @click="openCreateRoot">
              <el-icon><Plus /></el-icon>
              新增菜单
            </el-button>
          </header>

          <section class="admin-permission-table-card">
            <div v-if="loading" class="admin-permission-empty">菜单权限加载中...</div>
            <template v-else>
              <div class="admin-permission-table-scroll">
                <table class="admin-permission-table">
                  <thead>
                    <tr>
                      <th>排序</th>
                      <th>菜单名称</th>
                      <th>路由地址</th>
                      <th>显示</th>
                      <th>菜单类型</th>
                      <th>操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr
                      v-for="row in pagedRows"
                      :key="row.permissionId"
                      :class="{
                        'is-dragging': draggingId === row.permissionId,
                        'is-drag-over-before': dragOverId === row.permissionId && dragPosition === 'before',
                        'is-drag-over-after': dragOverId === row.permissionId && dragPosition === 'after'
                      }"
                      @dragover="handlePermissionDragOver(row, $event)"
                      @drop="dropPermission(row, $event)"
                    >
                      <td class="admin-permission-sort-cell">
                        <button
                          type="button"
                          class="admin-permission-rank"
                          draggable="true"
                          title="拖动调整同级菜单顺序"
                          aria-label="拖动调整同级菜单顺序"
                          :disabled="sorting || !can('update')"
                          @dragstart.stop="startPermissionDrag(row, $event)"
                          @dragend="finishPermissionDrag"
                        >
                          <el-icon><Rank /></el-icon>
                        </button>
                      </td>
                      <td>
                        <div class="admin-permission-name" :style="{ paddingLeft: `${row.level * 20}px` }">
                          <button
                            type="button"
                            class="admin-permission-toggle"
                            :class="{ placeholder: !row.hasChildren }"
                            :aria-label="row.hasChildren ? (expandedIds.has(row.permissionId) ? '收起菜单' : '展开菜单') : undefined"
                            @click="row.hasChildren && toggleExpanded(row.permissionId)"
                          >
                            <el-icon>
                              <ArrowDown v-if="row.hasChildren && expandedIds.has(row.permissionId)" />
                              <ArrowRight v-else-if="row.hasChildren" />
                            </el-icon>
                          </button>
                          <el-icon class="admin-permission-name-icon">
                            <component :is="permissionIcon(row.permissionType)" />
                          </el-icon>
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
                      <td>
                        <span class="admin-permission-type-pill" :class="row.permissionType.toLowerCase()">
                          {{ permissionTypeLabels[row.permissionType] }}
                        </span>
                      </td>
                      <td>
                        <div class="admin-permission-actions">
                          <el-button class="admin-permission-action edit" :disabled="!can('update')" @click="openEdit(row)">编辑</el-button>
                          <el-button
                            class="admin-permission-action"
                            :class="row.visible ? 'hide' : 'show'"
                            :disabled="!can(row.visible ? 'disable' : 'enable')"
                            :loading="busyId === row.permissionId"
                            @click="toggleVisible(row)"
                          >
                            {{ row.visible ? '隐藏' : '显示' }}
                          </el-button>
                          <el-button class="admin-permission-action danger" :disabled="!can('delete')" :loading="busyId === row.permissionId" @click="removePermission(row)">
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
                <el-pagination
                  v-model:current-page="currentPage"
                  :page-size="pageSize"
                  :total="permissionTotal"
                  layout="prev, pager, next"
                  background
                />
              </footer>
            </template>
          </section>
        </section>

      </div>

      <el-dialog
        v-model="drawerVisible"
        class="admin-permission-dialog"
        width="560px"
        :show-close="false"
        :close-on-click-modal="false"
        :close-on-press-escape="true"
        append-to-body
      >
        <template #header>
          <div class="admin-permission-panel-head">
            <div>
              <strong>{{ dialogTitle }}</strong>
              <p>维护菜单层级、路由地址和显示状态</p>
            </div>
            <el-button text circle :icon="Close" aria-label="关闭" @click="closeDrawer" />
          </div>
        </template>

        <div class="admin-permission-form">
          <div class="admin-permission-field">
            <span>菜单类型 <b>*</b></span>
            <el-radio-group v-model="form.permissionType" class="admin-permission-radio-row">
              <el-radio label="MENU">一级菜单</el-radio>
              <el-radio label="PAGE">二级菜单</el-radio>
              <el-radio label="BUTTON">功能按钮</el-radio>
            </el-radio-group>
          </div>

          <label class="admin-permission-field">
            <span>所属父级菜单</span>
            <el-select v-model="form.parentId" clearable placeholder="请选择父级菜单" :disabled="form.permissionType === 'MENU'">
              <el-option label="无上级菜单" :value="null" />
              <el-option v-for="item in parentOptions" :key="item.permissionId" :label="item.label" :value="item.permissionId" />
            </el-select>
          </label>

          <label class="admin-permission-field">
            <span>菜单名称 <b>*</b></span>
            <el-input v-model="form.permissionName" maxlength="8" show-word-limit placeholder="请输入菜单名称" @input="syncPermissionCode" />
          </label>

          <label class="admin-permission-field">
            <span>路由地址 <b>*</b></span>
            <el-input
              v-model="form.routePath"
              maxlength="100"
              show-word-limit
              :placeholder="form.permissionType === 'BUTTON' ? '请输入权限标识路由值' : '请输入路由地址'"
            />
          </label>
        </div>

        <template #footer>
          <div class="admin-permission-panel-footer">
            <el-button class="admin-permission-dialog-cancel" @click="closeDrawer">取消</el-button>
            <el-button
              class="admin-permission-dialog-confirm"
              type="primary"
              :disabled="!can(drawerMode === 'edit' ? 'update' : 'create')"
              :loading="saving"
              @click="savePermission"
            >确定</el-button>
          </div>
        </template>
      </el-dialog>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowDown, ArrowRight, Close, Files, Menu, Mouse, Plus, Rank } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import { useAdminPermissions } from '../../features/admin/use-admin-permissions';
import {
  createAdminPermission,
  deleteAdminPermission,
  disableAdminPermission,
  enableAdminPermission,
  fetchAdminPermissionTree,
  updateAdminPermission,
  updateAdminPermissionSorts,
  type AdminPermissionCommand,
  type AdminPermissionNode,
  type AdminPermissionType
} from '../../api/admin-permission';
import {
  countAdminPermissions,
  findAdminPermissionById,
  flattenAdminPermissionTree,
  flattenAllAdminPermissions,
  permissionTypeLabels,
  type AdminPermissionRow
} from '../../features/admin/permissions';

type DrawerMode = 'create' | 'edit';

const pageSize = 12;
const loading = ref(false);
const saving = ref(false);
const sorting = ref(false);
const busyId = ref<number | null>(null);
const draggingId = ref<number | null>(null);
const draggingParentId = ref<number | null>(null);
const dragOverId = ref<number | null>(null);
const dragPosition = ref<'before' | 'after'>('before');
const currentPage = ref(1);
const drawerVisible = ref(false);
const drawerMode = ref<DrawerMode>('create');
const editingPermission = ref<AdminPermissionRow | null>(null);
const permissionTree = ref<AdminPermissionNode[]>([]);
const expandedIds = ref(new Set<number>([1, 2, 12, 13]));
const adminPermissionsChangedEvent = 'admin-permissions-changed';
const { can } = useAdminPermissions('system:permission');

const form = reactive<AdminPermissionCommand>({
  parentId: null,
  permissionName: '',
  permissionCode: '',
  permissionType: 'MENU',
  routePath: '',
  visible: true,
  sortOrder: 1
});

const flattenedRows = computed(() => flattenAdminPermissionTree(permissionTree.value, expandedIds.value));
const permissionTotal = computed(() => countAdminPermissions(permissionTree.value));
const pagedRows = computed(() => flattenedRows.value.slice((currentPage.value - 1) * pageSize, currentPage.value * pageSize));
const dialogTitle = computed(() => (drawerMode.value === 'edit' ? '编辑菜单' : '新增菜单'));
const parentOptions = computed(() =>
  flattenAllAdminPermissions(permissionTree.value)
    .filter((item) => {
      if (item.permissionId === editingPermission.value?.permissionId) {
        return false;
      }
      if (form.permissionType === 'PAGE') {
        return item.permissionType === 'MENU';
      }
      if (form.permissionType === 'BUTTON') {
        return item.permissionType === 'PAGE';
      }
      return false;
    })
    .map((item) => ({ ...item, label: `${'　'.repeat(findLevel(item.permissionId))}${item.permissionName}` }))
);

watch(
  () => form.permissionType,
  (nextType) => {
    if (nextType === 'MENU') {
      form.parentId = null;
    } else if (nextType === 'PAGE') {
      const parent = form.parentId ? findAdminPermissionById(permissionTree.value, form.parentId) : null;
      if (!parent || parent.permissionType === 'BUTTON') {
        const defaultParent = parentOptions.value.find((item) => item.permissionType === 'MENU');
        form.parentId = defaultParent?.permissionId ?? null;
      }
    } else if (nextType === 'BUTTON') {
      form.routePath = '';
      const parent = form.parentId ? findAdminPermissionById(permissionTree.value, form.parentId) : null;
      if (!parent || parent.permissionType === 'MENU') {
        const defaultParent =
          parentOptions.value.find((item) => item.permissionType === 'PAGE') ?? parentOptions.value.find((item) => item.permissionType === 'MENU');
        form.parentId = defaultParent?.permissionId ?? null;
      }
    }
    syncPermissionCode();
  },
  { immediate: true }
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

function toggleExpanded(permissionId: number) {
  const next = new Set(expandedIds.value);
  if (next.has(permissionId)) {
    next.delete(permissionId);
  } else {
    next.add(permissionId);
  }
  expandedIds.value = next;
}

function normalizedParentId(parentId?: number | null) {
  return parentId ?? null;
}

function startPermissionDrag(row: AdminPermissionRow, event: DragEvent) {
  if (sorting.value) {
    event.preventDefault();
    return;
  }
  draggingId.value = row.permissionId;
  draggingParentId.value = normalizedParentId(row.parentId);
  dragOverId.value = null;
  dragPosition.value = 'before';
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move';
    event.dataTransfer.setData('text/plain', String(row.permissionId));
  }
}

function handlePermissionDragOver(row: AdminPermissionRow, event: DragEvent) {
  if (
    draggingId.value === null ||
    draggingId.value === row.permissionId ||
    draggingParentId.value !== normalizedParentId(row.parentId)
  ) {
    return;
  }
  event.preventDefault();
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move';
  }
  const targetRow = event.currentTarget as HTMLElement;
  const bounds = targetRow.getBoundingClientRect();
  dragPosition.value = event.clientY >= bounds.top + bounds.height / 2 ? 'after' : 'before';
  dragOverId.value = row.permissionId;
}

function finishPermissionDrag() {
  draggingId.value = null;
  draggingParentId.value = null;
  dragOverId.value = null;
  dragPosition.value = 'before';
}

function findSiblingNodes(parentId: number | null) {
  if (parentId === null) {
    return permissionTree.value;
  }
  return findAdminPermissionById(permissionTree.value, parentId)?.children ?? null;
}

async function dropPermission(target: AdminPermissionRow, event: DragEvent) {
  event.preventDefault();
  const sourceId = draggingId.value;
  const parentId = draggingParentId.value;
  const position = dragPosition.value;
  const validTarget = parentId === normalizedParentId(target.parentId);
  finishPermissionDrag();
  if (sourceId === null || sourceId === target.permissionId || !validTarget) {
    return;
  }

  const siblings = findSiblingNodes(parentId);
  if (!siblings) {
    return;
  }
  const sourceIndex = siblings.findIndex((item) => item.permissionId === sourceId);
  const targetIndex = siblings.findIndex((item) => item.permissionId === target.permissionId);
  if (sourceIndex < 0 || targetIndex < 0) {
    return;
  }

  const [moved] = siblings.splice(sourceIndex, 1);
  const currentTargetIndex = siblings.findIndex((item) => item.permissionId === target.permissionId);
  const insertIndex = position === 'after' ? currentTargetIndex + 1 : currentTargetIndex;
  siblings.splice(insertIndex, 0, moved);
  siblings.forEach((item, index) => {
    item.sortOrder = index + 1;
  });
  permissionTree.value = [...permissionTree.value];

  sorting.value = true;
  try {
    await updateAdminPermissionSorts(
      siblings.map((item) => ({
        permissionId: item.permissionId,
        parentId,
        sortOrder: item.sortOrder
      }))
    );
    window.dispatchEvent(new Event(adminPermissionsChangedEvent));
    ElMessage.success('菜单排序已保存');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '菜单排序保存失败');
    await loadPermissionTree();
  } finally {
    sorting.value = false;
  }
}

function openCreateRoot() {
  drawerMode.value = 'create';
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
  drawerVisible.value = true;
}

function closeDrawer() {
  drawerVisible.value = false;
}

function openEdit(row: AdminPermissionRow) {
  drawerMode.value = 'edit';
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
  drawerVisible.value = true;
}

function nextSortOrder(parentId: number | null) {
  const siblings = flattenAllAdminPermissions(permissionTree.value).filter((item) => (item.parentId ?? null) === parentId);
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
  return 0;
}

function buildPermissionCode() {
  const parent = form.parentId ? findAdminPermissionById(permissionTree.value, form.parentId) : null;
  const parentCode = parent?.permissionCode ?? 'menu';
  const cleanedName = form.permissionName.trim().replace(/[^\w\u4e00-\u9fa5]+/g, '');
  const fallback = `item${Date.now().toString().slice(-6)}`;
  const suffix = cleanedName ? cleanedName.toLowerCase() : fallback;
  return `${parentCode}:${suffix}`;
}

function syncPermissionCode() {
  form.permissionCode = buildPermissionCode();
}

function validateForm(): AdminPermissionCommand {
  const permissionName = form.permissionName.trim();
  const routePathValue = String(form.routePath ?? '').trim();
  const routePath = routePathValue;

  if (!permissionName) {
    throw new Error('请输入菜单名称');
  }

  if (form.permissionType !== 'MENU' && !form.parentId) {
    throw new Error('请选择父级菜单');
  }

  if (!routePath) {
    throw new Error(form.permissionType === 'BUTTON' ? '请输入权限标识路由值' : '请输入路由地址');
  }
  return {
    parentId: form.permissionType === 'MENU' ? null : form.parentId ?? null,
    permissionName,
    permissionCode: buildPermissionCode(),
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
    if (drawerMode.value === 'edit' && editingPermission.value) {
      await updateAdminPermission(editingPermission.value.permissionId, payload);
      ElMessage.success('菜单已更新');
      drawerVisible.value = false;
    } else {
      await createAdminPermission(payload);
      ElMessage.success('菜单已新增');
      drawerVisible.value = false;
    }
    await loadPermissionTree();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '菜单保存失败');
  } finally {
    saving.value = false;
  }
}

async function toggleVisible(row: AdminPermissionRow) {
  const action = row.visible ? '隐藏' : '显示';
  try {
    await ElMessageBox.confirm(
      row.visible
        ? `确认隐藏菜单「${row.permissionName}」？隐藏后其下级节点也将不可见。`
        : `确认显示菜单「${row.permissionName}」？下级节点将沿用原有显示状态。`,
      `${action}菜单`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
  } catch {
    return;
  }

  busyId.value = row.permissionId;
  try {
    if (row.visible) {
      await disableAdminPermission(row.permissionId);
    } else {
      await enableAdminPermission(row.permissionId);
    }
    await loadPermissionTree();
    window.dispatchEvent(new Event(adminPermissionsChangedEvent));
    ElMessage.success(`菜单已${action}`);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '状态更新失败');
  } finally {
    busyId.value = null;
  }
}

async function removePermission(row: AdminPermissionRow) {
  if (row.hasChildren) {
    ElMessage.warning('该菜单存在下级菜单或功能按钮，无法删除');
    return;
  }

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
    await loadPermissionTree();
    ElMessage.success('菜单已删除');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '删除失败');
  } finally {
    busyId.value = null;
  }
}

function buildDefaultExpandedIds(tree: AdminPermissionNode[]) {
  const ids = new Set<number>();
  const collect = (nodes: AdminPermissionNode[]) => nodes.forEach((node) => {
    if (node.children?.length) ids.add(node.permissionId);
    collect(node.children ?? []);
  });
  collect(tree);

  return ids;
}

async function loadPermissionTree() {
  loading.value = true;
  try {
    const result = await fetchAdminPermissionTree();
    permissionTree.value = result;
    expandedIds.value = buildDefaultExpandedIds(permissionTree.value);
    currentPage.value = 1;
  } catch (error) {
    permissionTree.value = [];
    expandedIds.value = new Set();
    currentPage.value = 1;
    ElMessage.error(error instanceof Error ? error.message : '功能权限树加载失败');
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadPermissionTree();
});
</script>
