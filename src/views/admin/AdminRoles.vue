<template>
  <AdminShell activeKey="roles">
    <section class="admin-roles-page">
      <el-breadcrumb class="admin-roles-breadcrumb" separator="/">
        <el-breadcrumb-item>系统基础设置</el-breadcrumb-item>
        <el-breadcrumb-item>角色管理</el-breadcrumb-item>
      </el-breadcrumb>

      <section class="admin-roles-toolbar">
        <div class="admin-roles-filter">
          <el-input v-model="draft.keyword" class="admin-roles-search" :prefix-icon="Search" placeholder="搜索角色名称/编码" clearable @keyup.enter="applySearch" />
          <el-select v-model="draft.enabled" class="admin-roles-status-filter" placeholder="状态" clearable>
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
          <el-button class="admin-roles-ghost-button" @click="applySearch">查询</el-button>
          <el-button class="admin-roles-ghost-button" @click="resetSearch">重置</el-button>
        </div>
        <el-button class="admin-roles-primary-button" type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>
          新增角色
        </el-button>
      </section>

      <section class="admin-roles-table-card">
        <div v-if="loading" class="admin-course-empty">角色加载中...</div>
        <template v-else>
          <div class="admin-roles-table-scroll">
            <table class="admin-roles-table">
              <thead>
                <tr>
                  <th>角色名称</th>
                  <th>角色描述</th>
                  <th>数据权限</th>
                  <th>用户数</th>
                  <th>状态</th>
                  <th>创建时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="role in roles" :key="role.roleId" :class="{ disabled: role.enabled === false }">
                  <td>
                    <div class="admin-roles-name">
                      <strong>{{ role.roleName }}</strong>
                      <span>{{ role.roleCode }}</span>
                    </div>
                  </td>
                  <td class="admin-roles-remark">{{ role.remark || '-' }}</td>
                  <td>{{ dataScopeLabels[role.dataScope || ''] || role.dataScope || '-' }}</td>
                  <td>{{ role.userCount ?? 0 }}</td>
                  <td>
                    <span class="admin-roles-status" :class="role.enabled === false ? 'disabled' : 'enabled'">
                      <i></i>
                      {{ role.enabled === false ? '禁用' : '启用' }}
                    </span>
                  </td>
                  <td>{{ formatRoleTime(role.createdAt) }}</td>
                  <td>
                    <div class="admin-roles-actions">
                      <el-button class="plain" @click="openDetail(role)">查看</el-button>
                      <template v-if="!isBuiltInRole(role)">
                        <el-button class="plain" @click="openEdit(role)">编辑</el-button>
                        <el-button :class="role.enabled === false ? 'enable' : 'warn'" :loading="busyId === role.roleId" @click="toggleRole(role)">
                          {{ role.enabled === false ? '启用' : '禁用' }}
                        </el-button>
                        <el-button class="danger" :loading="busyId === role.roleId" @click="removeRole(role)">删除</el-button>
                      </template>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <footer class="admin-roles-footer">
            <p>共 <strong>{{ page.total }}</strong> 条记录</p>
            <el-pagination
              v-model:current-page="page.page"
              v-model:page-size="page.pageSize"
              :total="page.total"
              layout="prev, pager, next"
              background
              @current-change="loadRoles"
            />
          </footer>
        </template>
      </section>
    </section>

    <el-drawer v-model="drawerVisible" class="admin-roles-drawer" size="720px" :with-header="false" append-to-body>
      <section class="admin-roles-drawer-page">
        <header class="admin-roles-drawer-head">
          <div>
            <p>系统基础设置 / 角色管理</p>
            <h2>{{ drawerTitle }}</h2>
          </div>
          <el-button text circle :icon="Close" @click="drawerVisible = false" />
        </header>

        <template v-if="drawerMode === 'detail' && detailRole">
          <section class="admin-roles-detail-card">
            <dl class="admin-roles-detail">
              <div><dt>角色名称</dt><dd>{{ detailRole.roleName }}</dd></div>
              <div><dt>角色编码</dt><dd>{{ detailRole.roleCode }}</dd></div>
              <div><dt>数据权限</dt><dd>{{ dataScopeLabels[detailRole.dataScope || ''] || detailRole.dataScope || '-' }}</dd></div>
              <div><dt>授权功能</dt><dd>{{ detailRole.permissionIds?.length || 0 }} / {{ permissionTotal }} 项</dd></div>
              <div><dt>绑定用户</dt><dd>{{ detailRole.userCount ?? 0 }} 人</dd></div>
              <div><dt>状态</dt><dd>{{ detailRole.enabled === false ? '禁用' : '启用' }}</dd></div>
            </dl>
            <p>{{ detailRole.remark || '暂无描述' }}</p>
          </section>

          <section class="admin-roles-log-card">
            <h3>操作日志</h3>
            <div v-if="roleLogs.length === 0" class="admin-roles-log-empty">暂无操作记录</div>
            <div v-for="log in roleLogs" :key="log.logId" class="admin-roles-log-row">
              <strong>{{ log.action }}</strong>
              <span>{{ log.operatorName || '-' }} · {{ formatRoleTime(log.createdAt) }}</span>
              <p>{{ log.content || '-' }}</p>
            </div>
          </section>
        </template>

        <template v-else>
          <section class="admin-roles-form-card">
            <div class="admin-roles-form-grid">
              <label>
                <span>角色名称 <b>*</b></span>
                <el-input v-model="form.roleName" maxlength="20" placeholder="请输入角色名称" />
              </label>
              <label>
                <span>角色编码 <b>*</b></span>
                <el-input v-model="form.roleCode" maxlength="50" placeholder="如 training_teacher" :disabled="drawerMode === 'edit'" />
              </label>
              <label>
                <span>数据权限 <b>*</b></span>
                <el-select v-model="form.dataScope" placeholder="请选择数据权限">
                  <el-option label="全部数据" value="ALL" />
                  <el-option label="本组织及下级" value="ORG_AND_CHILDREN" />
                  <el-option label="仅本组织" value="ORG_ONLY" />
                  <el-option label="仅本人数据" value="SELF" />
                </el-select>
              </label>
              <label class="wide">
                <span>角色描述</span>
                <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="120" show-word-limit placeholder="请输入角色描述" />
              </label>
            </div>
          </section>

          <section class="admin-roles-permission-card">
            <header>
              <div>
                <h3>功能权限</h3>
                <p>已选择 {{ form.permissionIds.length }} 项权限</p>
              </div>
              <el-checkbox :model-value="allPermissionsChecked" :indeterminate="partialPermissionsChecked" @change="toggleAllPermissions">全选</el-checkbox>
            </header>
            <el-tree
              ref="permissionTreeRef"
              class="admin-roles-permission-tree"
              :data="permissionTree"
              node-key="permissionId"
              show-checkbox
              default-expand-all
              :props="{ label: 'permissionName', children: 'children' }"
              :default-checked-keys="form.permissionIds"
              @check="syncCheckedPermissions"
            />
          </section>

          <footer class="admin-roles-drawer-footer">
            <el-button @click="drawerVisible = false">取消</el-button>
            <el-button type="primary" :loading="saving" @click="saveRole">保存</el-button>
          </footer>
        </template>
      </section>
    </el-drawer>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox, type ElTree } from 'element-plus';
import { Close, Plus, Search } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import { fetchAdminPermissionTree, type AdminPermissionNode } from '../../api/admin-permission';
import {
  createAdminRole,
  deleteAdminRole,
  disableAdminRole,
  enableAdminRole,
  fetchAdminRoleDetail,
  fetchAdminRoleLogs,
  fetchAdminRoles,
  updateAdminRole,
  type AdminRole,
  type AdminRoleCommand,
  type AdminRoleLog,
  type AdminRoleQuery
} from '../../api/admin-role';
import { collectAdminPermissionIds, mockAdminPermissions } from '../../features/admin/permissions';
import {
  countPermissionNodes,
  dataScopeLabels,
  formatRoleTime,
  isBuiltInRole,
  mockAdminRoles,
  toRolePage
} from '../../features/admin/roles';

type DrawerMode = 'create' | 'edit' | 'detail';

const loading = ref(false);
const saving = ref(false);
const busyId = ref<number | null>(null);
const roles = ref<AdminRole[]>(mockAdminRoles);
const permissionTree = ref<AdminPermissionNode[]>(mockAdminPermissions);
const permissionTreeRef = ref<InstanceType<typeof ElTree>>();
const drawerVisible = ref(false);
const drawerMode = ref<DrawerMode>('create');
const editingRole = ref<AdminRole | null>(null);
const detailRole = ref<AdminRole | null>(null);
const roleLogs = ref<AdminRoleLog[]>([]);
const page = reactive({ page: 1, pageSize: 20, total: mockAdminRoles.length });
const query = reactive<AdminRoleQuery>({});
const draft = reactive<AdminRoleQuery>({ enabled: null });

const emptyForm = (): AdminRoleCommand => ({
  roleName: '',
  roleCode: '',
  dataScope: 'ORG_AND_CHILDREN',
  remark: '',
  permissionIds: []
});

const form = reactive<AdminRoleCommand>(emptyForm());
const permissionTotal = computed(() => countPermissionNodes(permissionTree.value));
const allPermissionIds = computed(() => collectAdminPermissionIds(permissionTree.value));
const allPermissionsChecked = computed(() => allPermissionIds.value.length > 0 && form.permissionIds.length === allPermissionIds.value.length);
const partialPermissionsChecked = computed(() => form.permissionIds.length > 0 && !allPermissionsChecked.value);
const drawerTitle = computed(() => {
  if (drawerMode.value === 'detail') {
    return '角色详情';
  }
  return drawerMode.value === 'edit' ? '编辑角色' : '新增角色';
});

function currentQuery() {
  return { ...query, page: page.page, pageSize: page.pageSize };
}

async function loadPermissions() {
  try {
    permissionTree.value = await fetchAdminPermissionTree();
  } catch {
    permissionTree.value = mockAdminPermissions;
  }
}

async function loadRoles() {
  loading.value = true;
  try {
    const result = await fetchAdminRoles(currentQuery());
    roles.value = result.records;
    page.total = result.total;
    page.page = result.page;
    page.pageSize = result.pageSize;
  } catch {
    const fallback = toRolePage(page.page, page.pageSize);
    roles.value = fallback.records;
    page.total = fallback.total;
  } finally {
    loading.value = false;
  }
}

function applySearch() {
  query.keyword = draft.keyword?.trim() || undefined;
  query.enabled = draft.enabled;
  page.page = 1;
  loadRoles();
}

function resetSearch() {
  draft.keyword = '';
  draft.enabled = null;
  query.keyword = undefined;
  query.enabled = undefined;
  page.page = 1;
  loadRoles();
}

function applyForm(next: AdminRoleCommand) {
  Object.assign(form, emptyForm(), next);
}

async function hydrateTreeSelection() {
  await nextTick();
  permissionTreeRef.value?.setCheckedKeys(form.permissionIds);
}

function openCreate() {
  drawerMode.value = 'create';
  editingRole.value = null;
  detailRole.value = null;
  applyForm(emptyForm());
  drawerVisible.value = true;
  hydrateTreeSelection();
}

async function openEdit(role: AdminRole) {
  if (isBuiltInRole(role)) {
    openDetail(role);
    return;
  }
  drawerMode.value = 'edit';
  editingRole.value = role;
  detailRole.value = null;
  drawerVisible.value = true;
  const detail = await safeRoleDetail(role);
  applyForm({
    roleName: detail.roleName,
    roleCode: detail.roleCode,
    dataScope: detail.dataScope || 'ORG_AND_CHILDREN',
    remark: detail.remark || '',
    permissionIds: detail.permissionIds || []
  });
  hydrateTreeSelection();
}

async function openDetail(role: AdminRole) {
  drawerMode.value = 'detail';
  editingRole.value = null;
  detailRole.value = role;
  roleLogs.value = [];
  drawerVisible.value = true;
  detailRole.value = await safeRoleDetail(role);
  try {
    roleLogs.value = await fetchAdminRoleLogs(role.roleId);
  } catch {
    roleLogs.value = [];
  }
}

async function safeRoleDetail(role: AdminRole) {
  try {
    return await fetchAdminRoleDetail(role.roleId);
  } catch {
    return role;
  }
}

function syncCheckedPermissions(_: unknown, payload: { checkedKeys: number[]; halfCheckedKeys: number[] }) {
  form.permissionIds = [...new Set([...payload.checkedKeys, ...payload.halfCheckedKeys])];
}

function toggleAllPermissions(value: string | number | boolean) {
  form.permissionIds = value ? allPermissionIds.value : [];
  permissionTreeRef.value?.setCheckedKeys(form.permissionIds);
}

function validateForm() {
  if (!form.roleName.trim()) {
    throw new Error('请输入角色名称');
  }
  if (!form.roleCode.trim()) {
    throw new Error('请输入角色编码');
  }
  if (!form.dataScope) {
    throw new Error('请选择数据权限');
  }
}

async function saveRole() {
  try {
    validateForm();
  } catch (error) {
    ElMessage.warning(error instanceof Error ? error.message : '请完善角色信息');
    return;
  }

  saving.value = true;
  try {
    if (drawerMode.value === 'create') {
      await createAdminRole(form);
      ElMessage.success('新增角色成功');
    } else if (editingRole.value) {
      await updateAdminRole(editingRole.value.roleId, form);
      ElMessage.success('编辑角色成功');
    }
    drawerVisible.value = false;
    await loadRoles();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function toggleRole(role: AdminRole) {
  busyId.value = role.roleId;
  try {
    if (role.enabled === false) {
      await enableAdminRole(role.roleId);
      ElMessage.success('角色已启用');
    } else {
      await disableAdminRole(role.roleId);
      ElMessage.success('角色已禁用');
    }
    await loadRoles();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '操作失败');
  } finally {
    busyId.value = null;
  }
}

async function removeRole(role: AdminRole) {
  try {
    await ElMessageBox.confirm(`确认删除角色「${role.roleName}」？`, '删除角色', { type: 'warning' });
    busyId.value = role.roleId;
    await deleteAdminRole(role.roleId);
    ElMessage.success('角色已删除');
    await loadRoles();
  } catch (error) {
    if (error instanceof Error) {
      ElMessage.error(error.message);
    }
  } finally {
    busyId.value = null;
  }
}

onMounted(async () => {
  await loadPermissions();
  await loadRoles();
});
</script>
