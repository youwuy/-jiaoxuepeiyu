import type { AdminPermissionNode, AdminPermissionType } from '../../api/admin-permission';

export interface AdminPermissionRow extends AdminPermissionNode {
  level: number;
  hasChildren: boolean;
}

export const permissionTypeLabels: Record<AdminPermissionType, string> = {
  MENU: '目录',
  PAGE: '菜单',
  BUTTON: '按钮'
};

export const mockAdminPermissions: AdminPermissionNode[] = [];

export function flattenAdminPermissionTree(tree: AdminPermissionNode[], expandedIds: Set<number>): AdminPermissionRow[] {
  const rows: AdminPermissionRow[] = [];

  const walk = (items: AdminPermissionNode[], level: number, parentVisible: boolean) => {
    items.forEach((item) => {
      const children = item.children ?? [];
      const visibleRow = parentVisible;

      rows.push({
        ...item,
        level,
        hasChildren: children.length > 0
      });

      if (children.length > 0 && expandedIds.has(item.permissionId)) {
        walk(children, level + 1, visibleRow);
      }
    });
  };

  walk(tree, 0, true);
  return rows;
}

export function collectAdminPermissionIds(tree: AdminPermissionNode[]): number[] {
  return tree.flatMap((item) => [item.permissionId, ...collectAdminPermissionIds(item.children ?? [])]);
}

export function countAdminPermissions(tree: AdminPermissionNode[]): number {
  return collectAdminPermissionIds(tree).length;
}

export function flattenAllAdminPermissions(tree: AdminPermissionNode[]): AdminPermissionNode[] {
  return tree.flatMap((item) => [item, ...flattenAllAdminPermissions(item.children ?? [])]);
}

export function findAdminPermissionById(tree: AdminPermissionNode[], permissionId: number): AdminPermissionNode | null {
  for (const item of tree) {
    if (item.permissionId === permissionId) {
      return item;
    }
    const child = findAdminPermissionById(item.children ?? [], permissionId);
    if (child) {
      return child;
    }
  }
  return null;
}
