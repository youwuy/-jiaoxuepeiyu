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
        <el-button class="admin-org-primary-button" type="primary" @click="openCreateRoot">
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
                <tr v-for="row in visibleRows" :key="row.orgId" :class="{ disabled: !row.enabled }">
                  <td>
                    <el-icon class="admin-org-rank"><Rank /></el-icon>
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
                      <el-button v-if="row.enabled" class="admin-org-action edit" @click="openEdit(row)">编辑</el-button>
                      <el-button v-if="row.enabled" class="admin-org-action child" @click="openCreateChild(row)">新增下级</el-button>
                      <el-button v-if="row.enabled" class="admin-org-action danger" :loading="busyId === row.orgId" @click="disableOrg(row)">
                        禁用
                      </el-button>
                      <el-button v-else class="admin-org-action enable" :loading="busyId === row.orgId" @click="enableOrg(row)">启用</el-button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <footer class="admin-org-footer">
            <p>共 {{ orgTotal }} 条记录</p>
            <el-pagination :current-page="1" :page-size="10" :total="orgTotal" layout="prev, pager, next" background />
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
        <label v-if="dialogMode === 'child'" class="admin-org-field">
          <span>所属父级组织 <b>*</b></span>
          <el-select v-model="form.parentId" disabled>
            <el-option :label="parentName" :value="form.parentId" />
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
          <el-button class="admin-org-dialog-confirm" type="primary" :loading="saving" @click="saveOrg">确定</el-button>
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
import {
  createAdminOrg,
  disableAdminOrg,
  enableAdminOrg,
  fetchAdminOrgTree,
  updateAdminOrg,
  type AdminOrgCommand,
  type AdminOrgNode
} from '../../api/admin-org';
import { collectAdminOrgIds, countAdminOrgs, flattenAdminOrgTree, type AdminOrgRow } from '../../features/admin/org';

type DialogMode = 'root' | 'child' | 'edit';

const loading = ref(false);
const saving = ref(false);
const busyId = ref<number | null>(null);
const orgTree = ref<AdminOrgNode[]>([]);
const expandedIds = ref(new Set<number>());
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
const orgTotal = computed(() => countAdminOrgs(orgTree.value));
const dialogTitle = computed(() => (dialogMode.value === 'edit' ? '编辑组织' : '新增组织'));

function formatDateTime(value?: string) {
  if (!value) {
    return '-';
  }

  const normalized = value.includes('T') ? value.replace('T', ' ') : value;
  return normalized.slice(0, 16);
}

function applySearch() {
  keyword.value = draftKeyword.value.trim();
}

function resetSearch() {
  draftKeyword.value = '';
  keyword.value = '';
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
