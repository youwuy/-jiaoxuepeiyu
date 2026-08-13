<template>
  <section v-if="roleFormPageVisible" class="admin-role-form-page">
      <header class="admin-role-form-topbar">
        <el-breadcrumb class="admin-role-form-breadcrumb" separator="/">
          <el-breadcrumb-item>系统管理</el-breadcrumb-item>
          <el-breadcrumb-item>角色管理</el-breadcrumb-item>
          <el-breadcrumb-item>{{ formMode === 'create' ? '新增角色' : '编辑角色' }}</el-breadcrumb-item>
        </el-breadcrumb>
        <div class="admin-role-form-user">
          <span>管</span>
          <strong>管理员</strong>
        </div>
      </header>

      <section class="admin-role-form-card">
        <h3><i></i>基本信息</h3>
        <div class="admin-role-basic-grid">
          <label>
            <span>角色名称 <b>*</b></span>
            <el-input v-model="form.roleName" maxlength="20" placeholder="请输入角色名称" />
          </label>
          <label>
            <span>角色描述</span>
            <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="120" placeholder="请输入角色描述" />
          </label>
        </div>
      </section>

      <section class="admin-role-form-card permissions">
        <h3><i></i>权限配置</h3>
        <div class="admin-role-permission-matrix">
          <table>
            <thead>
              <tr>
                <th>模块</th>
                <th>页面</th>
                <th>功能权限</th>
                <th>数据权限配置</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in permissionRows" :key="row.rowKey" :class="{ 'module-alt': row.striped }">
                <td class="module-cell">
                  <el-checkbox
                    v-if="row.moduleName"
                    :model-value="isModuleChecked(row.moduleCheckIds)"
                    :indeterminate="isModuleIndeterminate(row.moduleCheckIds)"
                    @change="(value: string | number | boolean) => toggleModule(row.moduleIds, value)"
                  >
                    {{ row.moduleName }}
                  </el-checkbox>
                </td>
                <td>
                  <el-checkbox
                    :model-value="isPageChecked(row)"
                    :indeterminate="isPageIndeterminate(row)"
                    @change="(value: string | number | boolean) => togglePage(row, value)"
                  >
                    {{ row.pageName }}
                  </el-checkbox>
                </td>
                <td>
                  <div class="admin-role-action-checks">
                    <el-checkbox
                      v-for="action in row.actions"
                      :key="action.key"
                      :model-value="isActionChecked(action)"
                      :disabled="action.id === null"
                      @change="(value: string | number | boolean) => toggleAction(row, action, value)"
                    >
                      {{ action.label }}
                    </el-checkbox>
                  </div>
                </td>
                <td>
                  <el-radio-group
                    :model-value="pageDataScope(row.pageId)"
                    class="admin-role-data-scope"
                    @change="(value: string | number | boolean) => setPageDataScope(row.pageId, String(value))"
                  >
                    <el-radio label="SELF">个人</el-radio>
                    <el-radio label="ORG_ONLY">管理组织</el-radio>
                    <el-radio label="ALL">全部</el-radio>
                  </el-radio-group>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <footer class="admin-role-form-actions">
        <el-button class="admin-role-form-cancel" @click="cancelRoleForm">取消</el-button>
        <el-button class="admin-role-form-confirm" type="primary" :loading="saving" @click="saveRole">确定</el-button>
      </footer>
  </section>

  <AdminShell v-else activeKey="roles">
    <section class="admin-roles-page">
      <el-breadcrumb class="admin-roles-breadcrumb" separator="/">
        <el-breadcrumb-item>系统基础设置</el-breadcrumb-item>
        <el-breadcrumb-item>角色管理</el-breadcrumb-item>
      </el-breadcrumb>

      <section class="admin-roles-toolbar">
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
                  <th>状态</th>
                  <th>创建时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="role in roles" :key="role.roleId" :class="{ disabled: role.enabled === false }">
                  <td class="strong-cell">{{ role.roleName }}</td>
                  <td class="admin-roles-remark">{{ role.remark || '-' }}</td>
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
                        <el-button class="warn" :loading="busyId === role.roleId" @click="toggleRole(role)">
                          {{ role.enabled === false ? '启用' : '禁用' }}
                        </el-button>
                        <el-button class="danger" :disabled="Boolean(role.userCount)" :loading="busyId === role.roleId" @click="removeRole(role)">
                          删除
                        </el-button>
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
              :page-size="page.pageSize"
              :total="page.total"
              layout="prev, pager, next"
              background
              @current-change="loadRoles"
            />
          </footer>
        </template>
      </section>
    </section>

    <el-dialog v-model="detailVisible" class="admin-roles-dialog" width="620px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-roles-dialog-head">
          <strong>角色详情</strong>
          <el-button text circle :icon="Close" @click="detailVisible = false" />
        </div>
      </template>
      <dl v-if="detailRole" class="admin-roles-detail">
        <div><dt>角色名称</dt><dd>{{ detailRole.roleName }}</dd></div>
        <div><dt>角色编码</dt><dd>{{ detailRole.roleCode }}</dd></div>
        <div><dt>状态</dt><dd>{{ detailRole.enabled === false ? '禁用' : '启用' }}</dd></div>
        <div><dt>数据权限</dt><dd>{{ roleDataScopeSummary(detailRole) }}</dd></div>
        <div class="permissions"><dt>授权权限</dt><dd>{{ rolePermissionNames(detailRole) }}</dd></div>
        <div><dt>创建时间</dt><dd>{{ formatRoleTime(detailRole.createdAt) }}</dd></div>
      </dl>
      <p v-if="detailRole" class="admin-roles-detail-remark">{{ detailRole.remark || '暂无描述' }}</p>
    </el-dialog>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Close, Plus } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import { fetchAdminPermissionTree, type AdminPermissionNode } from '../../api/admin-permission';
import {
  createAdminRole,
  disableAdminRole,
  deleteAdminRole,
  enableAdminRole,
  fetchAdminRoleDetail,
  fetchAdminRoles,
  updateAdminRole,
  type AdminRole,
  type AdminRoleCommand,
  type AdminRoleQuery
} from '../../api/admin-role';
import {
  buildRolePermissionRows,
  dataScopeLabels,
  formatRoleTime,
  isRolePermissionPageChecked,
  isRolePermissionPageIndeterminate,
  isBuiltInRole,
  rolePermissionActionIds,
  type RolePermissionAction,
  type RolePermissionRow
} from '../../features/admin/roles';

type FormMode = 'create' | 'edit';

const loading = ref(false);
const saving = ref(false);
const busyId = ref<number | null>(null);
const roles = ref<AdminRole[]>([]);
const permissionTree = ref<AdminPermissionNode[]>([]);
const roleFormPageVisible = ref(false);
const formMode = ref<FormMode>('create');
const editingRole = ref<AdminRole | null>(null);
const detailVisible = ref(false);
const detailRole = ref<AdminRole | null>(null);
const page = reactive({ page: 1, pageSize: 20, total: 0 });
const query = reactive<AdminRoleQuery>({});

const emptyForm = (): AdminRoleCommand => ({
  roleName: '',
  roleCode: '',
  dataScope: 'SELF',
  remark: '',
  permissionIds: [],
  pageDataScopes: []
});

const form = reactive<AdminRoleCommand>(emptyForm());
const permissionRows = computed(() => buildRolePermissionRows(permissionTree.value));

function currentQuery() {
  return { ...query, page: page.page, pageSize: page.pageSize };
}

async function loadPermissions() {
  try {
    const nextTree = await fetchAdminPermissionTree();
    permissionTree.value = nextTree;
  } catch (error) {
    permissionTree.value = [];
    ElMessage.error(error instanceof Error ? error.message : '角色权限树加载失败');
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
  } catch (error) {
    roles.value = [];
    page.total = 0;
    ElMessage.error(error instanceof Error ? error.message : '角色列表加载失败');
  } finally {
    loading.value = false;
  }
}

function isModuleChecked(ids: number[]) {
  return ids.length > 0 && ids.every((id) => form.permissionIds.includes(id));
}

function isModuleIndeterminate(ids: number[]) {
  const checkedCount = ids.filter((id) => form.permissionIds.includes(id)).length;
  return checkedCount > 0 && checkedCount < ids.length;
}

function isPermissionChecked(id: number) {
  return form.permissionIds.includes(id);
}

function rowActionIds(row: RolePermissionRow) {
  return rolePermissionActionIds(row);
}

function isPageChecked(row: RolePermissionRow) {
  return isRolePermissionPageChecked(row, form.permissionIds);
}

function isPageIndeterminate(row: RolePermissionRow) {
  return isRolePermissionPageIndeterminate(row, form.permissionIds);
}

function isActionChecked(action: RolePermissionAction) {
  return action.id !== null && isPermissionChecked(action.id);
}

function setPermission(id: number, value: string | number | boolean) {
  const next = new Set(form.permissionIds);
  if (value) {
    next.add(id);
  } else {
    next.delete(id);
  }
  form.permissionIds = [...next];
}

function togglePage(row: RolePermissionRow, value: string | number | boolean = !isPageChecked(row)) {
  const ids = [row.pageId, ...rowActionIds(row)];
  const next = new Set(form.permissionIds);
  ids.forEach((id) => value ? next.add(id) : next.delete(id));
  form.permissionIds = [...next];
  syncModuleAccess(row);
}

function toggleAction(
  row: RolePermissionRow,
  action: RolePermissionAction,
  value: string | number | boolean = !isActionChecked(action)
) {
  if (action.id !== null) {
    setPermission(action.id, value);
    const hasSelectedAction = rowActionIds(row).some(isPermissionChecked);
    setPermission(row.pageId, hasSelectedAction);
    syncModuleAccess(row);
  }
}

function syncModuleAccess(row: RolePermissionRow) {
  const hasSelectedPermission = row.moduleCheckIds.some(isPermissionChecked);
  row.moduleAccessIds.forEach((id) => setPermission(id, hasSelectedPermission));
}

function toggleModule(ids: number[], value: string | number | boolean = !isModuleChecked(ids)) {
  const next = new Set(form.permissionIds);
  ids.forEach((id) => {
    if (value) {
      next.add(id);
    } else {
      next.delete(id);
    }
  });
  form.permissionIds = [...next];
}

function pageDataScope(pagePermissionId: number) {
  return form.pageDataScopes.find((item) => item.pagePermissionId === pagePermissionId)?.dataScope || 'SELF';
}

function roleDataScopeSummary(role: AdminRole) {
  if (role.pageDataScopes?.length) {
    return `${role.pageDataScopes.length} 个页面已配置`;
  }
  return dataScopeLabels[role.dataScope || ''] || '-';
}

function rolePermissionNames(role: AdminRole) {
  const selected = new Set(role.permissionIds ?? []);
  const names = permissionRows.value
    .filter((row) => selected.has(row.pageId))
    .map((row) => row.pageName);
  return names.length ? names.join('、') : '未分配';
}

function setPageDataScope(pagePermissionId: number, dataScope: string) {
  if (!['SELF', 'ORG_ONLY', 'ALL'].includes(dataScope)) {
    return;
  }
  const next = form.pageDataScopes.filter((item) => item.pagePermissionId !== pagePermissionId);
  next.push({ pagePermissionId, dataScope: dataScope as AdminRoleCommand['dataScope'] });
  form.pageDataScopes = next;
}

function applyForm(next: AdminRoleCommand) {
  Object.assign(form, emptyForm(), next);
}

function openCreate() {
  formMode.value = 'create';
  editingRole.value = null;
  applyForm(emptyForm());
  roleFormPageVisible.value = true;
}

async function openEdit(role: AdminRole) {
  if (isBuiltInRole(role)) {
    openDetail(role);
    return;
  }
  formMode.value = 'edit';
  editingRole.value = role;
  try {
    const detail = await fetchAdminRoleDetail(role.roleId);
    applyForm({
      roleName: detail.roleName,
      roleCode: detail.roleCode,
      dataScope: detail.dataScope || 'SELF',
      remark: detail.remark || '',
      permissionIds: detail.permissionIds || [],
      pageDataScopes: detail.pageDataScopes || []
    });
    roleFormPageVisible.value = true;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '角色详情加载失败，无法编辑');
  }
}

async function openDetail(role: AdminRole) {
  try {
    detailRole.value = await fetchAdminRoleDetail(role.roleId);
    detailVisible.value = true;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '角色详情加载失败');
  }
}

function cancelRoleForm() {
  roleFormPageVisible.value = false;
  editingRole.value = null;
  applyForm(emptyForm());
}

function validateForm() {
  if (!form.roleName.trim()) {
    throw new Error('请输入角色名称');
  }
  if (!form.roleCode.trim()) {
    form.roleCode = roleCodeFromName(form.roleName);
  }
  if (form.permissionIds.length === 0) {
    throw new Error('请至少选择一项功能权限');
  }
}

function roleCodeFromName(value: string) {
  return value.trim().toLowerCase().replace(/\s+/g, '_') || `role_${Date.now()}`;
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
    const selectedPageIds = new Set(
      permissionRows.value
        .filter((row) => form.permissionIds.includes(row.pageId))
        .map((row) => row.pageId)
    );
    const command: AdminRoleCommand = {
      ...form,
      permissionIds: [...form.permissionIds],
      pageDataScopes: [...selectedPageIds].map((pagePermissionId) => ({
        pagePermissionId,
        dataScope: pageDataScope(pagePermissionId)
      }))
    };
    if (formMode.value === 'create') {
      await createAdminRole(command);
      ElMessage.success('新增角色成功');
    } else if (editingRole.value) {
      await updateAdminRole(editingRole.value.roleId, command);
      ElMessage.success('编辑角色成功');
    }
    roleFormPageVisible.value = false;
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
      await ElMessageBox.confirm(`确认禁用角色「${role.roleName}」？`, '禁用角色', { type: 'warning' });
      await disableAdminRole(role.roleId);
      ElMessage.success('角色已禁用');
    }
    await loadRoles();
  } catch (error) {
    if (error instanceof Error) {
      ElMessage.error(error.message);
    }
  } finally {
    busyId.value = null;
  }
}

async function removeRole(role: AdminRole) {
  if (role.userCount) {
    ElMessage.warning('该角色仍有用户使用，不能删除');
    return;
  }
  try {
    await ElMessageBox.confirm(`确认删除角色「${role.roleName}」？`, '删除角色', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    });
  } catch {
    return;
  }
  busyId.value = role.roleId;
  try {
    await deleteAdminRole(role.roleId);
    ElMessage.success('角色已删除');
    await loadRoles();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '角色删除失败');
  } finally {
    busyId.value = null;
  }
}

onMounted(async () => {
  await loadPermissions();
  await loadRoles();
});
</script>
