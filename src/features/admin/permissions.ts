import type { AdminPermissionNode, AdminPermissionType } from '../../api/admin-permission';

export interface AdminPermissionRow extends AdminPermissionNode {
  level: number;
  hasChildren: boolean;
  visibleRow: boolean;
}

export const permissionTypeLabels: Record<AdminPermissionType, string> = {
  MENU: '一级菜单',
  PAGE: '页面菜单',
  BUTTON: '功能按钮'
};

export const mockAdminPermissions: AdminPermissionNode[] = [
  {
    permissionId: 1,
    parentId: null,
    permissionName: '系统基础设置',
    permissionCode: 'system',
    permissionType: 'MENU',
    routePath: '/system',
    visible: true,
    sortOrder: 1,
    children: [
      {
        permissionId: 2,
        parentId: 1,
        permissionName: '组织管理',
        permissionCode: 'system:org',
        permissionType: 'PAGE',
        routePath: '/admin/organization',
        visible: true,
        sortOrder: 1,
        children: [
          {
            permissionId: 3,
            parentId: 2,
            permissionName: '新增组织',
            permissionCode: 'system:org:create',
            permissionType: 'BUTTON',
            routePath: null,
            visible: true,
            sortOrder: 1
          },
          {
            permissionId: 4,
            parentId: 2,
            permissionName: '注销组织',
            permissionCode: 'system:org:disable',
            permissionType: 'BUTTON',
            routePath: null,
            visible: true,
            sortOrder: 2
          }
        ]
      },
      {
        permissionId: 5,
        parentId: 1,
        permissionName: '用户管理',
        permissionCode: 'system:user',
        permissionType: 'PAGE',
        routePath: '/admin/users',
        visible: true,
        sortOrder: 2
      }
    ]
  },
  {
    permissionId: 6,
    parentId: null,
    permissionName: '资源管理',
    permissionCode: 'resource',
    permissionType: 'MENU',
    routePath: '/resource',
    visible: true,
    sortOrder: 2
  },
  {
    permissionId: 7,
    parentId: null,
    permissionName: '教学实训',
    permissionCode: 'teaching',
    permissionType: 'MENU',
    routePath: '/teaching',
    visible: false,
    sortOrder: 3
  }
];

export function flattenAdminPermissionTree(tree: AdminPermissionNode[], expandedIds: Set<number>, keyword = ''): AdminPermissionRow[] {
  const normalizedKeyword = keyword.replace(/\s+/g, '').toLowerCase();
  const rows: AdminPermissionRow[] = [];

  const walk = (items: AdminPermissionNode[], level: number, parentVisible: boolean) => {
    items.forEach((item) => {
      const children = item.children ?? [];
      const haystack = `${item.permissionName}${item.permissionCode}${item.routePath ?? ''}`.replace(/\s+/g, '').toLowerCase();
      const selfMatches = !normalizedKeyword || haystack.includes(normalizedKeyword);
      const descendantsMatch = hasMatchingDescendant(children, normalizedKeyword);
      const visibleRow = parentVisible && (selfMatches || descendantsMatch || !normalizedKeyword);

      rows.push({
        ...item,
        level,
        hasChildren: children.length > 0,
        visibleRow
      });

      const shouldShowChildren = normalizedKeyword ? visibleRow : visibleRow && expandedIds.has(item.permissionId);
      if (children.length > 0) {
        walk(children, level + 1, shouldShowChildren);
      }
    });
  };

  walk(tree, 0, true);
  return rows.filter((row) => row.visibleRow);
}

export function collectAdminPermissionIds(tree: AdminPermissionNode[]): number[] {
  return tree.flatMap((item) => [item.permissionId, ...collectAdminPermissionIds(item.children ?? [])]);
}

export function countAdminPermissions(tree: AdminPermissionNode[]): number {
  return collectAdminPermissionIds(tree).length;
}

function hasMatchingDescendant(children: AdminPermissionNode[], keyword: string): boolean {
  if (!keyword) {
    return true;
  }

  return children.some((child) => {
    const haystack = `${child.permissionName}${child.permissionCode}${child.routePath ?? ''}`.replace(/\s+/g, '').toLowerCase();
    return haystack.includes(keyword) || hasMatchingDescendant(child.children ?? [], keyword);
  });
}
