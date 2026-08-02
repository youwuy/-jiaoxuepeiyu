import { requestJson } from './http';

export type AdminRoleDataScope = 'SELF' | 'ORG_ONLY' | 'ALL';

export interface PageResponse<T> {
  records: T[];
  total: number;
  page: number;
  pageSize: number;
}

export interface AdminRole {
  roleId: number;
  roleName: string;
  roleCode: string;
  dataScope?: AdminRoleDataScope;
  remark?: string;
  enabled?: boolean;
  userCount?: number;
  permissionIds?: number[];
  createdAt?: string;
  updatedAt?: string;
}

export interface AdminRoleQuery {
  keyword?: string;
  enabled?: boolean | null;
  page?: number;
  pageSize?: number;
}

export interface AdminRoleCommand {
  roleName: string;
  roleCode: string;
  dataScope: AdminRoleDataScope;
  remark?: string;
  permissionIds: number[];
}

export interface AdminRoleLog {
  logId: number;
  roleId: number;
  operatorId?: number;
  operatorName?: string;
  action: string;
  content?: string;
  createdAt?: string;
}

function queryString(query: AdminRoleQuery = {}) {
  const params = new URLSearchParams();
  Object.entries(query).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      params.set(key, String(value));
    }
  });
  const text = params.toString();
  return text ? `?${text}` : '';
}

export async function fetchAdminRoles(query: AdminRoleQuery = {}) {
  return requestJson<PageResponse<AdminRole>>(`/admin/roles${queryString(query)}`, {
    fallbackLabel: '角色列表'
  });
}

export async function fetchAdminRoleDetail(roleId: number) {
  return requestJson<AdminRole>(`/admin/roles/${roleId}`, {
    fallbackLabel: '角色详情'
  });
}

export async function createAdminRole(command: AdminRoleCommand) {
  const result = await requestJson<number | { roleId: number }>('/admin/roles', {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: '新增角色'
  });

  return { roleId: typeof result === 'number' ? result : result.roleId };
}

export async function updateAdminRole(roleId: number, command: AdminRoleCommand) {
  return requestJson<void>(`/admin/roles/${roleId}`, {
    method: 'PUT',
    body: JSON.stringify(command),
    fallbackLabel: '编辑角色'
  });
}

export async function enableAdminRole(roleId: number) {
  return requestJson<void>(`/admin/roles/${roleId}/enable`, {
    method: 'POST',
    fallbackLabel: '启用角色'
  });
}

export async function disableAdminRole(roleId: number) {
  return requestJson<void>(`/admin/roles/${roleId}/disable`, {
    method: 'POST',
    fallbackLabel: '禁用角色'
  });
}

export async function deleteAdminRole(roleId: number) {
  return requestJson<void>(`/admin/roles/${roleId}/delete`, {
    method: 'POST',
    fallbackLabel: '删除角色'
  });
}

export async function updateAdminRolePermissions(roleId: number, permissionIds: number[]) {
  return requestJson<void>(`/admin/roles/${roleId}/permissions`, {
    method: 'PUT',
    body: JSON.stringify({ permissionIds }),
    fallbackLabel: '设置角色权限'
  });
}

export async function fetchAdminRoleLogs(roleId: number) {
  return requestJson<AdminRoleLog[]>(`/admin/roles/${roleId}/logs`, {
    fallbackLabel: '角色操作日志'
  });
}
