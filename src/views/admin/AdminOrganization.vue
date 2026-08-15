<template>
  <AdminShell activeKey="organization">
    <section class="admin-org-page">
      <el-breadcrumb class="admin-org-breadcrumb" separator="/">
        <el-breadcrumb-item>系统基础设置</el-breadcrumb-item>
        <el-breadcrumb-item>组织管理</el-breadcrumb-item>
      </el-breadcrumb>

      <section class="admin-org-toolbar">
        <div class="admin-org-toolbar-left">
          <el-input
            v-model="draftKeyword"
            class="admin-org-search"
            :prefix-icon="Search"
            placeholder="搜索组织名称"
            clearable
            @keyup.enter="applySearch"
          />
          <el-button class="admin-org-ghost-button" @click="applySearch">查询</el-button>
          <el-button class="admin-org-ghost-button" @click="resetSearch">重置</el-button>
        </div>
        <el-button class="admin-org-primary-button" type="primary" :disabled="!can('create')" @click="openCreateRoot">
          <el-icon><Plus /></el-icon>
          新增一级组织
        </el-button>
      </section>

      <section class="admin-org-table-card">
        <div v-if="loading" class="admin-course-empty">组织加载中...</div>
        <template v-else>
          <div class="admin-org-table-scroll">
            <table class="admin-org-table">
              <thead>
                <tr>
                  <th>排序</th>
                  <th></th>
                  <th>组织名称</th>
                  <th>状态</th>
                  <th>创建人</th>
                  <th>创建时间</th>
                  <th>最近修改人</th>
                  <th>最近修改时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="row in pagedVisibleRows"
                  :key="row.orgId"
                  :class="{
                    disabled: !row.enabled,
                    'is-dragging': draggingId === row.orgId,
                    'is-drag-over-before': dragOverId === row.orgId && dragPosition === 'before',
                    'is-drag-over-after': dragOverId === row.orgId && dragPosition === 'after'
                  }"
                  @dragover="handleOrgDragOver(row, $event)"
                  @drop="dropOrg(row, $event)"
                >
                  <td>
                    <button
                      type="button"
                      class="admin-org-rank"
                      draggable="true"
                      :disabled="sorting || filtering || !can('update')"
                      title="拖动调整同级组织顺序"
                      aria-label="拖动调整同级组织顺序"
                      @dragstart.stop="startOrgDrag(row, $event)"
                      @dragend="finishOrgDrag"
                    >
                      <el-icon><Rank /></el-icon>
                    </button>
                  </td>
                  <td>
                    <button
                      v-if="row.hasChildren"
                      type="button"
                      class="admin-org-expand"
                      :aria-label="expandedIds.has(row.orgId) ? '收起组织' : '展开组织'"
                      @click="toggleExpanded(row.orgId)"
                    >
                      <el-icon><component :is="expandedIds.has(row.orgId) ? ArrowDown : ArrowRight" /></el-icon>
                    </button>
                  </td>
                  <td>
                    <div class="admin-org-name" :style="{ paddingLeft: `${row.level * 24}px` }">
                      <el-icon><OfficeBuilding /></el-icon>
                      <strong>{{ row.orgName }}</strong>
                    </div>
                  </td>
                  <td>
                    <span class="admin-org-status" :class="row.enabled ? 'enabled' : 'disabled'">
                      <i></i>
                      {{ row.enabled ? '启用' : '禁用' }}
                    </span>
                  </td>
                  <td>{{ row.creatorName || '-' }}</td>
                  <td>{{ formatDateTime(row.createdAt) }}</td>
                  <td>{{ row.updaterName || row.creatorName || '-' }}</td>
                  <td>{{ formatDateTime(row.updatedAt) }}</td>
                  <td>
                    <div class="admin-org-actions">
                      <el-button class="admin-org-action edit" :disabled="!can('update')" @click="openEdit(row)">编辑</el-button>
                      <el-button v-if="row.enabled" class="admin-org-action child" :disabled="!can('create')" @click="openCreateChild(row)">新增下级</el-button>
                      <el-button v-if="row.enabled" class="admin-org-action danger" :disabled="!can('disable')" :loading="busyId === row.orgId" @click="disableOrg(row)">
                        禁用
                      </el-button>
                      <el-button v-else class="admin-org-action enable" :disabled="!can('enable')" :loading="busyId === row.orgId" @click="enableOrg(row)">启用</el-button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <footer class="admin-org-footer">
            <p>共 {{ orgTotal }} 条记录</p>
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="visibleRows.length"
              layout="prev, pager, next"
              background
            />
          </footer>
        </template>
      </section>
    </section>

    <el-dialog
      v-model="dialogVisible"
      class="admin-org-dialog"
      width="480px"
      :show-close="false"
      :close-on-click-modal="false"
      append-to-body
    >
      <template #header>
        <div class="admin-org-dialog-head">
          <strong>{{ dialogTitle }}</strong>
          <el-button text circle :icon="Close" @click="dialogVisible = false" />
        </div>
      </template>

      <div class="admin-org-form">
        <label v-if="dialogMode !== 'root'" class="admin-org-field">
          <span>所属父级组织 <b>*</b></span>
          <el-select v-model="form.parentId" disabled>
            <el-option :label="parentName || '顶级组织'" :value="form.parentId" />
          </el-select>
        </label>

        <label class="admin-org-field">
          <span>组织名称 <b>*</b></span>
          <el-input v-model="form.orgName" maxlength="20" placeholder="请输入组织名称" />
          <small>最多输入20个字</small>
        </label>
      </div>

      <template #footer>
        <div class="admin-org-dialog-footer">
          <el-button class="admin-org-dialog-cancel" @click="dialogVisible = false">取消</el-button>
          <el-button class="admin-org-dialog-confirm" type="primary" :disabled="!can(dialogMode === 'edit' ? 'update' : 'create')" :loading="saving" @click="saveOrg">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowDown, ArrowRight, Close, OfficeBuilding, Plus, Rank, Search } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import { useAdminPermissions } from '../../features/admin/use-admin-permissions';
import {
  createAdminOrg,
  disableAdminOrg,
  enableAdminOrg,
  fetchAdminOrgTree,
  updateAdminOrg,
  updateAdminOrgSorts,
  type AdminOrgCommand,
  type AdminOrgNode
} from '../../api/admin-org';
import { collectAdminOrgIds, countAdminOrgs, flattenAdminOrgTree, type AdminOrgRow } from '../../features/admin/org';

type DialogMode = 'root' | 'child' | 'edit';

const { can } = useAdminPermissions('system:org');
const loading = ref(false);
const saving = ref(false);
const sorting = ref(false);
const busyId = ref<number | null>(null);
const draggingId = ref<number | null>(null);
const draggingParentId = ref<number | null>(null);
const dragOverId = ref<number | null>(null);
const dragPosition = ref<'before' | 'after'>('before');
const orgTree = ref<AdminOrgNode[]>([]);
const expandedIds = ref(new Set<number>());
const currentPage = ref(1);
const pageSize = 10;
const draftKeyword = ref('');
const keyword = ref('');
const dialogVisible = ref(false);
const dialogMode = ref<DialogMode>('root');
const editingOrg = ref<AdminOrgRow | null>(null);
const parentName = ref('');

const form = reactive<AdminOrgCommand>({
  parentId: null,
  orgName: '',
  sortOrder: 0
});

const visibleRows = computed(() => flattenAdminOrgTree(orgTree.value, expandedIds.value, draftKeyword.value || keyword.value));
const pagedVisibleRows = computed(() => visibleRows.value.slice((currentPage.value - 1) * pageSize, currentPage.value * pageSize));
const orgTotal = computed(() => countAdminOrgs(orgTree.value));
const dialogTitle = computed(() => (dialogMode.value === 'edit' ? '编辑组织' : '新增组织'));
const filtering = computed(() => Boolean((draftKeyword.value || keyword.value).trim()));

function formatDateTime(value?: string) {
  if (!value) {
    return '-';
  }

  const normalized = value.includes('T') ? value.replace('T', ' ') : value;
  return normalized.slice(0, 16);
}

function applySearch() {
  keyword.value = draftKeyword.value.trim();
  currentPage.value = 1;
}

function resetSearch() {
  draftKeyword.value = '';
  keyword.value = '';
  currentPage.value = 1;
}

function toggleExpanded(orgId: number) {
  const next = new Set(expandedIds.value);
  if (next.has(orgId)) {
    next.delete(orgId);
  } else {
    next.add(orgId);
  }
  expandedIds.value = next;
}

function normalizedParentId(parentId?: number | null) {
  return parentId ?? null;
}

function startOrgDrag(row: AdminOrgRow, event: DragEvent) {
  if (sorting.value || filtering.value) {
    event.preventDefault();
    return;
  }
  draggingId.value = row.orgId;
  draggingParentId.value = normalizedParentId(row.parentId);
  dragOverId.value = null;
  dragPosition.value = 'before';
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move';
    event.dataTransfer.setData('text/plain', String(row.orgId));
  }
}

function handleOrgDragOver(row: AdminOrgRow, event: DragEvent) {
  if (
    filtering.value ||
    draggingId.value === null ||
    draggingId.value === row.orgId ||
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
  dragOverId.value = row.orgId;
}

function finishOrgDrag() {
  draggingId.value = null;
  draggingParentId.value = null;
  dragOverId.value = null;
  dragPosition.value = 'before';
}

function findOrgSiblings(parentId: number | null) {
  if (parentId === null) {
    return orgTree.value;
  }
  return findOrgNode(orgTree.value, parentId)?.children ?? null;
}

function findOrgNode(items: AdminOrgNode[], orgId: number): AdminOrgNode | null {
  for (const item of items) {
    if (item.orgId === orgId) {
      return item;
    }
    const child = findOrgNode(item.children ?? [], orgId);
    if (child) {
      return child;
    }
  }
  return null;
}

async function dropOrg(target: AdminOrgRow, event: DragEvent) {
  event.preventDefault();
  const sourceId = draggingId.value;
  const parentId = draggingParentId.value;
  const position = dragPosition.value;
  const validTarget = parentId === normalizedParentId(target.parentId);
  finishOrgDrag();
  if (sourceId === null || sourceId === target.orgId || !validTarget) {
    return;
  }

  const siblings = findOrgSiblings(parentId);
  if (!siblings) {
    return;
  }
  const sourceIndex = siblings.findIndex((item) => item.orgId === sourceId);
  const targetIndex = siblings.findIndex((item) => item.orgId === target.orgId);
  if (sourceIndex < 0 || targetIndex < 0) {
    return;
  }

  const [moved] = siblings.splice(sourceIndex, 1);
  const currentTargetIndex = siblings.findIndex((item) => item.orgId === target.orgId);
  const insertIndex = position === 'after' ? currentTargetIndex + 1 : currentTargetIndex;
  siblings.splice(insertIndex, 0, moved);
  siblings.forEach((item, index) => {
    item.sortOrder = index + 1;
  });
  orgTree.value = [...orgTree.value];

  sorting.value = true;
  try {
    await updateAdminOrgSorts(siblings.map((item) => ({ orgId: item.orgId, sortOrder: item.sortOrder })));
    ElMessage.success('组织排序已保存');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '组织排序保存失败');
    await loadOrgTree();
  } finally {
    sorting.value = false;
  }
}

function openCreateRoot() {
  dialogMode.value = 'root';
  editingOrg.value = null;
  parentName.value = '';
  form.parentId = null;
  form.orgName = '';
  form.sortOrder = nextSortOrder(null);
  dialogVisible.value = true;
}

function openCreateChild(row: AdminOrgRow) {
  dialogMode.value = 'child';
  editingOrg.value = null;
  parentName.value = row.orgName;
  form.parentId = row.orgId;
  form.orgName = '';
  form.sortOrder = nextSortOrder(row.orgId);
  dialogVisible.value = true;
}

function openEdit(row: AdminOrgRow) {
  dialogMode.value = 'edit';
  editingOrg.value = row;
  parentName.value = findOrgName(row.parentId) || '';
  form.parentId = row.parentId ?? null;
  form.orgName = row.orgName;
  form.sortOrder = row.sortOrder;
  dialogVisible.value = true;
}

function nextSortOrder(parentId: number | null) {
  const siblings = flattenAll(orgTree.value).filter((item) => (item.parentId ?? null) === parentId);
  return siblings.length + 1;
}

function findOrgName(orgId?: number | null) {
  if (!orgId) {
    return '';
  }

  return flattenAll(orgTree.value).find((item) => item.orgId === orgId)?.orgName || '';
}

function flattenAll(items: AdminOrgNode[]): AdminOrgNode[] {
  return items.flatMap((item) => [item, ...flattenAll(item.children ?? [])]);
}

function validateForm() {
  const orgName = form.orgName.trim();
  if (!orgName) {
    throw new Error('请输入组织名称');
  }
  if (orgName.length > 20) {
    throw new Error('组织名称最多输入20个字');
  }

  return {
    parentId: form.parentId ?? null,
    orgName,
    sortOrder: form.sortOrder
  };
}

async function saveOrg() {
  let payload: AdminOrgCommand;
  try {
    payload = validateForm();
  } catch (error) {
    ElMessage.warning(error instanceof Error ? error.message : '请完善组织信息');
    return;
  }

  saving.value = true;
  try {
    if (dialogMode.value === 'edit' && editingOrg.value) {
      await updateAdminOrg(editingOrg.value.orgId, payload);
      ElMessage.success('组织已更新');
    } else {
      await createAdminOrg(payload);
      ElMessage.success('组织已新增');
    }
    dialogVisible.value = false;
    await loadOrgTree();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '组织保存失败');
  } finally {
    saving.value = false;
  }
}

async function disableOrg(row: AdminOrgRow) {
  try {
    await ElMessageBox.confirm(`确认禁用组织「${row.orgName}」？`, '禁用组织', {
      confirmButtonText: '禁用',
      cancelButtonText: '取消',
      type: 'warning'
    });
  } catch {
    return;
  }

  busyId.value = row.orgId;
  try {
    await disableAdminOrg(row.orgId);
    ElMessage.success('组织已禁用');
    await loadOrgTree();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '禁用失败');
  } finally {
    busyId.value = null;
  }
}

async function enableOrg(row: AdminOrgRow) {
  busyId.value = row.orgId;
  try {
    await enableAdminOrg(row.orgId);
    ElMessage.success('组织已启用');
    await loadOrgTree();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '启用失败');
  } finally {
    busyId.value = null;
  }
}

async function loadOrgTree() {
  loading.value = true;
  try {
    const result = await fetchAdminOrgTree();
    orgTree.value = result;
    expandedIds.value = new Set(collectAdminOrgIds(orgTree.value));
    currentPage.value = 1;
  } catch (error) {
    orgTree.value = [];
    expandedIds.value = new Set();
    ElMessage.error(error instanceof Error ? error.message : '组织树加载失败');
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadOrgTree();
});
</script>
