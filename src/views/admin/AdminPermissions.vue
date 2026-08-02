<template>
  <AdminShell activeKey="permissions">
    <section class="admin-permission-page">
      <h1 class="admin-permission-title">功能管理</h1>

      <el-breadcrumb class="admin-permission-breadcrumb" separator="/">
        <el-breadcrumb-item>系统基础设置</el-breadcrumb-item>
        <el-breadcrumb-item>功能管理</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="admin-permission-layout" :class="{ 'has-panel': drawerVisible }">
        <section class="admin-permission-board">
          <header class="admin-permission-board-head">
            <strong>菜单权限树</strong>
            <el-button class="admin-permission-primary-button" type="primary" @click="openCreateRoot">
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
                    <tr v-for="row in pagedRows" :key="row.permissionId">
                      <td class="admin-permission-sort-cell">
                        <el-icon class="admin-permission-rank"><Rank /></el-icon>
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

        <aside v-if="drawerVisible" class="admin-permission-panel">
          <div class="admin-permission-panel-head">
            <div>
              <strong>{{ dialogTitle }}</strong>
              <p>维护菜单层级、路由地址和显示状态</p>
            </div>
            <el-button text circle :icon="Close" @click="closeDrawer" />
          </div>

          <div class="admin-permission-form">
            <div class="admin-permission-field">
              <span>菜单类型 <b>*</b></span>
              <el-radio-group v-model="form.permissionType" class="admin-permission-radio-row">
                <el-radio label="MENU">目录</el-radio>
                <el-radio label="PAGE">菜单</el-radio>
                <el-radio label="BUTTON">按钮</el-radio>
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
              <el-input v-model="form.permissionName" maxlength="8" placeholder="请输入菜单名称" @input="syncPermissionCode" />
            </label>

            <label class="admin-permission-field">
              <span>路由地址 <b>*</b></span>
              <el-input v-model="form.routePath" maxlength="100" placeholder="请输入路由地址" />
            </label>
          </div>

          <div class="admin-permission-panel-footer">
            <el-button class="admin-permission-dialog-cancel" @click="closeDrawer">取消</el-button>
            <el-button class="admin-permission-dialog-confirm" type="primary" :loading="saving" @click="savePermission">确定</el-button>
          </div>
        </aside>
      </div>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowDown, ArrowRight, Close, Files, Menu, Mouse, Plus, Rank } from '@element-plus/icons-vue';
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
const busyId = ref<number | null>(null);
const currentPage = ref(1);
const drawerVisible = ref(false);
const drawerMode = ref<DrawerMode>('create');
const editingPermission = ref<AdminPermissionRow | null>(null);
const permissionTree = ref<AdminPermissionNode[]>([]);
const usingFallbackPermissions = ref(false);
const expandedIds = ref(new Set<number>([1, 2, 12, 13]));

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
  const routePath = form.permissionType === 'BUTTON' ? routePathValue || null : routePathValue;

  if (!permissionName) {
    throw new Error('请输入菜单名称');
  }

  if (form.permissionType !== 'MENU' && !form.parentId) {
    throw new Error('请选择父级菜单');
  }

  if (form.permissionType !== 'BUTTON' && !routePath) {
    throw new Error('请输入路由地址');
  }
  if (form.permissionType === 'BUTTON' && !routePath) {
    throw new Error('请输入按钮权限标识');
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
    if (usingFallbackPermissions.value) {
      if (drawerMode.value === 'edit' && editingPermission.value) {
        updateLocalPermission(editingPermission.value.permissionId, payload);
        ElMessage.success('菜单已更新');
      } else {
        createLocalPermission(payload);
        ElMessage.success('菜单已新增');
      }
      drawerVisible.value = false;
    } else {
      if (drawerMode.value === 'edit' && editingPermission.value) {
        await updateAdminPermission(editingPermission.value.permissionId, payload);
        ElMessage.success('菜单已更新');
      } else {
        await createAdminPermission(payload);
        ElMessage.success('菜单已新增');
      }
      drawerVisible.value = false;
      await loadPermissionTree();
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '菜单保存失败');
  } finally {
    saving.value = false;
  }
}

async function toggleVisible(row: AdminPermissionRow) {
  busyId.value = row.permissionId;
  try {
    if (usingFallbackPermissions.value) {
      setLocalPermissionVisible(row.permissionId, !row.visible, row.visible);
    } else {
      if (row.visible) {
        await disableAdminPermission(row.permissionId);
      } else {
        await enableAdminPermission(row.permissionId);
      }
      await loadPermissionTree();
    }
    ElMessage.success(row.visible ? '菜单已隐藏' : '菜单已显示');
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
    if (usingFallbackPermissions.value) {
      deleteLocalPermission(row.permissionId);
    } else {
      await deleteAdminPermission(row.permissionId);
      await loadPermissionTree();
    }
    ElMessage.success('菜单已删除');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '删除失败');
  } finally {
    busyId.value = null;
  }
}

function buildDefaultExpandedIds(tree: AdminPermissionNode[]) {
  const ids = new Set<number>();
  const root = tree.find((item) => item.permissionCode === 'system') ?? tree[0];
  if (root) {
    ids.add(root.permissionId);
    const firstChild = root.children?.[0];
    if (firstChild) {
      ids.add(firstChild.permissionId);
    }
  }

  const teachingRoot = tree.find((item) => item.permissionCode === 'teaching');
  if (teachingRoot) {
    ids.add(teachingRoot.permissionId);
    const firstTeachingChild = teachingRoot.children?.[0];
    if (firstTeachingChild) {
      ids.add(firstTeachingChild.permissionId);
    }
  }

  return ids;
}

function nextLocalPermissionId() {
  return Math.max(0, ...flattenAllAdminPermissions(permissionTree.value).map((item) => item.permissionId)) + 1;
}

function createLocalPermission(payload: AdminPermissionCommand) {
  const node: AdminPermissionNode = {
    permissionId: nextLocalPermissionId(),
    parentId: payload.parentId ?? null,
    permissionName: payload.permissionName,
    permissionCode: payload.permissionCode,
    permissionType: payload.permissionType,
    routePath: payload.routePath ?? null,
    visible: payload.visible,
    sortOrder: payload.sortOrder,
    children: []
  };

  if (!node.parentId) {
    permissionTree.value = [...permissionTree.value, node];
    return;
  }

  const insertInto = (items: AdminPermissionNode[]): AdminPermissionNode[] =>
    items.map((item) => {
      if (item.permissionId === node.parentId) {
        return {
          ...item,
          children: [...(item.children ?? []), node]
        };
      }
      return {
        ...item,
        children: item.children ? insertInto(item.children) : undefined
      };
    });

  permissionTree.value = insertInto(permissionTree.value);
  expandedIds.value = new Set([...expandedIds.value, node.parentId]);
}

function updateLocalPermission(permissionId: number, payload: AdminPermissionCommand) {
  const updateIn = (items: AdminPermissionNode[]): AdminPermissionNode[] =>
    items.map((item) => {
      if (item.permissionId === permissionId) {
        return {
          ...item,
          parentId: payload.parentId ?? null,
          permissionName: payload.permissionName,
          permissionCode: payload.permissionCode,
          permissionType: payload.permissionType,
          routePath: payload.routePath ?? null,
          visible: payload.visible,
          sortOrder: payload.sortOrder
        };
      }
      return {
        ...item,
        children: item.children ? updateIn(item.children) : undefined
      };
    });

  permissionTree.value = updateIn(permissionTree.value);
}

function setLocalPermissionVisible(permissionId: number, visible: boolean, includeChildren: boolean) {
  const updateIn = (items: AdminPermissionNode[]): AdminPermissionNode[] =>
    items.map((item) => {
      if (item.permissionId === permissionId) {
        return setNodeVisible(item, visible, includeChildren);
      }
      return {
        ...item,
        children: item.children ? updateIn(item.children) : undefined
      };
    });

  permissionTree.value = updateIn(permissionTree.value);
}

function setNodeVisible(item: AdminPermissionNode, visible: boolean, includeChildren: boolean): AdminPermissionNode {
  return {
    ...item,
    visible,
    children: includeChildren ? item.children?.map((child) => setNodeVisible(child, visible, true)) : item.children
  };
}

function deleteLocalPermission(permissionId: number) {
  const removeFrom = (items: AdminPermissionNode[]): AdminPermissionNode[] =>
    items
      .filter((item) => item.permissionId !== permissionId)
      .map((item) => ({
        ...item,
        children: item.children ? removeFrom(item.children) : undefined
      }));

  permissionTree.value = removeFrom(permissionTree.value);
}

async function loadPermissionTree() {
  loading.value = true;
  try {
    const result = await fetchAdminPermissionTree();
    usingFallbackPermissions.value = false;
    permissionTree.value = result;
    expandedIds.value = buildDefaultExpandedIds(permissionTree.value);
    currentPage.value = 1;
  } catch (error) {
    usingFallbackPermissions.value = false;
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
