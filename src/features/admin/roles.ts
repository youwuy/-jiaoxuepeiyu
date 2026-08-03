import type { AdminRole, PageResponse } from '../../api/admin-role';
import type { AdminPermissionNode } from '../../api/admin-permission';

export const dataScopeLabels: Record<string, string> = {
  ALL: '全部数据',
  ORG_AND_CHILDREN: '本组织及下级',
  ORG_ONLY: '仅本组织',
  SELF: '仅本人数据'
};

export const mockAdminRoles: AdminRole[] = [];

export function toRolePage(page = 1, pageSize = 20): PageResponse<AdminRole> {
  return {
    records: [],
    total: 0,
    page,
    pageSize
  };
}

export function isBuiltInRole(role?: AdminRole | null) {
  return role?.roleCode === 'super_admin' || role?.roleName === '超级管理员';
}

export function formatRoleTime(value?: string) {
  if (!value) {
    return '-';
  }
  return value.replace('T', ' ').slice(0, 16);
}

export function countPermissionNodes(tree: AdminPermissionNode[]): number {
  return tree.reduce((sum, item) => sum + 1 + countPermissionNodes(item.children ?? []), 0);
}

export function collectPermissionIds(tree: AdminPermissionNode[]): number[] {
  return tree.flatMap((item) => [item.permissionId, ...collectPermissionIds(item.children ?? [])]);
}
