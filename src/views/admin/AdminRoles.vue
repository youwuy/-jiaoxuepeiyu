<template>
  <AdminShell activeKey="roles">
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
              <tr v-for="row in permissionRows" :key="row.rowKey">
                <td class="module-cell">
                  <el-checkbox
                    v-if="row.moduleName"
                    :model-value="isModuleChecked(row.moduleIds)"
                    :indeterminate="isModuleIndeterminate(row.moduleIds)"
                    @click.prevent="toggleModule(row.moduleIds)"
                  >
                    {{ row.moduleName }}
                  </el-checkbox>
                </td>
                <td>
                  <el-checkbox :model-value="isPermissionChecked(row.pageId)" @click.prevent="togglePermission(row.pageId)">
                    {{ row.pageName }}
                  </el-checkbox>
                </td>
                <td>
                  <div class="admin-role-action-checks">
                    <el-checkbox
                      v-for="action in row.actions"
                      :key="action.key"
                      :model-value="isActionChecked(action)"
                      :disabled="action.virtual"
                      @click.prevent="toggleAction(action)"
                    >
                      {{ action.label }}
                    </el-checkbox>
                  </div>
                </td>
                <td>
                  <el-radio-group v-model="form.dataScope" class="admin-role-data-scope">
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

    <section v-else class="admin-roles-page">
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
        <div><dt>数据权限</dt><dd>{{ dataScopeLabels[detailRole.dataScope || ''] || '-' }}</dd></div>
        <div><dt>授权权限</dt><dd>{{ detailRole.permissionIds?.length || 0 }} 项</dd></div>
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
  enableAdminRole,
  fetchAdminRoleDetail,
  fetchAdminRoles,
  updateAdminRole,
  type AdminRole,
  type AdminRoleCommand,
  type AdminRoleQuery
} from '../../api/admin-role';
import { dataScopeLabels, formatRoleTime, isBuiltInRole } from '../../features/admin/roles';

type FormMode = 'create' | 'edit';

interface PermissionMatrixAction {
  key: string;
  id: number;
  label: string;
  virtual?: boolean;
}

interface PermissionMatrixRow {
  rowKey: string;
  moduleName: string;
  moduleIds: number[];
  pageId: number;
  pageName: string;
  actions: PermissionMatrixAction[];
}

const defaultActions = ['列表', '新增', '删除', '修改', '启用', '禁用'];
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
  permissionIds: []
});

const form = reactive<AdminRoleCommand>(emptyForm());
const permissionRows = computed(() => buildPermissionRows(permissionTree.value));

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

function buildPermissionRows(tree: AdminPermissionNode[]): PermissionMatrixRow[] {
  return tree.flatMap((module) => {
    const pages = module.children && module.children.length > 0 ? module.children : [module];
    const moduleIds = collectNodeIds([module]);
    return pages.map((pageNode, index) => ({
      rowKey: `${module.permissionId}-${pageNode.permissionId}`,
      moduleName: index === 0 ? module.permissionName : '',
      moduleIds,
      pageId: pageNode.permissionId,
      pageName: pageNode.permissionName,
      actions: buildActions(pageNode)
    }));
  });
}

function buildActions(pageNode: AdminPermissionNode): PermissionMatrixAction[] {
  const children = pageNode.children ?? [];
  if (children.length > 0) {
    return [
      { key: `page-${pageNode.permissionId}`, id: pageNode.permissionId, label: '列表' },
      ...children.map((child) => ({
        key: `action-${child.permissionId}`,
        id: child.permissionId,
        label: normalizeActionName(child.permissionName)
      }))
    ];
  }
  return [
    { key: `page-${pageNode.permissionId}`, id: pageNode.permissionId, label: '列表' },
    ...defaultActions.slice(1).map((label, index) => ({
      key: `virtual-${pageNode.permissionId}-${index}`,
      id: pageNode.permissionId,
      label,
      virtual: true
    }))
  ];
}

function normalizeActionName(value: string) {
  return value.replace(/^新增.*$/, '新增').replace(/^删除.*$/, '删除').replace(/^修改.*$/, '修改').replace(/^编辑.*$/, '修改').replace(/^禁用.*$/, '禁用').replace(/^启用.*$/, '启用');
}

function collectNodeIds(nodes: AdminPermissionNode[]): number[] {
  return nodes.flatMap((node) => [node.permissionId, ...collectNodeIds(node.children ?? [])]);
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

function isActionChecked(action: PermissionMatrixAction) {
  return action.virtual ? false : isPermissionChecked(action.id);
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

function togglePermission(id: number, value: string | number | boolean = !isPermissionChecked(id)) {
  setPermission(id, value);
}

function toggleAction(action: PermissionMatrixAction, value: string | number | boolean = !isActionChecked(action)) {
  if (!action.virtual) {
    setPermission(action.id, value);
  }
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
  roleFormPageVisible.value = true;
  const detail = await safeRoleDetail(role);
  applyForm({
    roleName: detail.roleName,
    roleCode: detail.roleCode,
    dataScope: detail.dataScope || 'SELF',
    remark: detail.remark || '',
    permissionIds: detail.permissionIds || []
  });
}

async function openDetail(role: AdminRole) {
  detailRole.value = role;
  detailVisible.value = true;
  detailRole.value = await safeRoleDetail(role);
}

async function safeRoleDetail(role: AdminRole) {
  try {
    return await fetchAdminRoleDetail(role.roleId);
  } catch {
    return role;
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
    if (formMode.value === 'create') {
      await createAdminRole(form);
      ElMessage.success('新增角色成功');
    } else if (editingRole.value) {
      await updateAdminRole(editingRole.value.roleId, form);
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

onMounted(async () => {
  await loadPermissions();
  await loadRoles();
});
</script>
