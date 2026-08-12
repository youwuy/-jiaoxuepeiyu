import { requestJson } from './http';

export type AdminPermissionType = 'MENU' | 'PAGE' | 'BUTTON';

export interface AdminPermissionNode {
  permissionId: number;
  parentId?: number | null;
  permissionName: string;
  permissionCode: string;
  permissionType: AdminPermissionType;
  routePath?: string | null;
  visible: boolean;
  sortOrder: number;
  children?: AdminPermissionNode[];
}

export interface AdminPermissionCommand {
  parentId?: number | null;
  permissionName: string;
  permissionCode: string;
  permissionType: AdminPermissionType;
  routePath?: string | null;
  visible: boolean;
  sortOrder: number;
}

export interface AdminPermissionSortItem {
  permissionId: number;
  parentId?: number | null;
  sortOrder: number;
}

export async function fetchAdminPermissionTree() {
  return requestJson<AdminPermissionNode[]>('/admin/permissions/tree', {
    fallbackLabel: '功能权限树'
  });
}

export async function fetchMyAdminPermissionTree() {
  return requestJson<AdminPermissionNode[]>('/admin/permissions/mine/tree', {
    fallbackLabel: '当前账号菜单权限'
  });
}

export async function createAdminPermission(command: AdminPermissionCommand) {
  const result = await requestJson<number | { permissionId: number }>('/admin/permissions', {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: '新增菜单'
  });

  return { permissionId: typeof result === 'number' ? result : result.permissionId };
}

export async function updateAdminPermission(permissionId: number, command: AdminPermissionCommand) {
  return requestJson<void>(`/admin/permissions/${permissionId}`, {
    method: 'PUT',
    body: JSON.stringify(command),
    fallbackLabel: '编辑菜单'
  });
}

export async function enableAdminPermission(permissionId: number) {
  return requestJson<void>(`/admin/permissions/${permissionId}/enable`, {
    method: 'POST',
    fallbackLabel: '显示菜单'
  });
}

export async function disableAdminPermission(permissionId: number) {
  return requestJson<void>(`/admin/permissions/${permissionId}/disable`, {
    method: 'POST',
    fallbackLabel: '隐藏菜单'
  });
}

export async function deleteAdminPermission(permissionId: number) {
  return requestJson<void>(`/admin/permissions/${permissionId}/delete`, {
    method: 'POST',
    fallbackLabel: '删除菜单'
  });
}

export async function updateAdminPermissionSorts(items: AdminPermissionSortItem[]) {
  return requestJson<void>('/admin/permissions/sort', {
    method: 'PUT',
    body: JSON.stringify({ items }),
    fallbackLabel: '保存菜单排序'
  });
}
