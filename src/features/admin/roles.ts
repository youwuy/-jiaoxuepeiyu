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

export interface RolePermissionAction {
  key: string;
  id: number | null;
  label: string;
}

export interface RolePermissionRow {
  rowKey: string;
  moduleName: string;
  moduleIds: number[];
  pageId: number;
  pageName: string;
  actions: RolePermissionAction[];
  striped: boolean;
}

const roleActionSlots = [
  { label: '列表', suffix: 'list' },
  { label: '新增', suffix: 'create' },
  { label: '删除', suffix: 'delete' },
  { label: '修改', suffix: 'update' },
  { label: '启用', suffix: 'enable' },
  { label: '禁用', suffix: 'disable' }
] as const;

interface PermissionGroup {
  name: string;
  nodes: AdminPermissionNode[];
  pages: Array<{ node: AdminPermissionNode; displayName: string }>;
}

export function buildRolePermissionRows(tree: AdminPermissionNode[]): RolePermissionRow[] {
  const groups = tree.flatMap(buildPermissionGroups);

  return groups.flatMap((group, groupIndex) => {
    const moduleIds = collectPermissionIds(group.nodes);
    return group.pages.map(({ node, displayName }, pageIndex) => ({
      rowKey: `${group.nodes[0]?.permissionId ?? 'group'}-${node.permissionId}`,
      moduleName: pageIndex === 0 ? group.name : '',
      moduleIds,
      pageId: node.permissionId,
      pageName: displayName,
      actions: buildRolePermissionActions(node),
      striped: groupIndex % 2 === 1
    }));
  });
}

function buildPermissionGroups(module: AdminPermissionNode): PermissionGroup[] {
  const children = module.children ?? [];
  const directPages = children.filter((child) => child.permissionType === 'PAGE');
  const nestedMenus = children.filter((child) => child.permissionType === 'MENU');
  const directButtons = children.filter((child) => child.permissionType === 'BUTTON');
  const collapsedMenus = nestedMenus.filter((menu) => descendantPages(menu).length === 1);
  const separateMenus = nestedMenus.filter((menu) => descendantPages(menu).length > 1);
  const parentPages = [
    ...directPages.map((node) => ({ node, displayName: node.permissionName })),
    ...collapsedMenus.map((menu) => ({
      node: descendantPages(menu)[0],
      displayName: menu.permissionName
    }))
  ];
  const groups: PermissionGroup[] = [];

  if (parentPages.length > 0) {
    groups.push({
      name: module.permissionName,
      nodes: [{ ...module, children: [] }, ...directPages, ...collapsedMenus, ...directButtons],
      pages: parentPages
    });
  }

  separateMenus.forEach((menu) => {
    groups.push({
      name: menu.permissionName,
      nodes: [menu],
      pages: descendantPages(menu).map((node) => ({ node, displayName: node.permissionName }))
    });
  });

  return groups;
}

function descendantPages(node: AdminPermissionNode): AdminPermissionNode[] {
  return (node.children ?? []).flatMap((child) => {
    if (child.permissionType === 'PAGE') {
      return [child];
    }
    if (child.permissionType === 'MENU') {
      return descendantPages(child);
    }
    return [];
  });
}

function buildRolePermissionActions(page: AdminPermissionNode): RolePermissionAction[] {
  const buttons = (page.children ?? []).filter((child) => child.permissionType === 'BUTTON');
  const bySuffix = new Map(buttons.map((button) => [permissionSuffix(button.permissionCode), button]));

  return roleActionSlots.map((slot) => {
    const button = slot.suffix === 'list' ? page : bySuffix.get(slot.suffix);
    return {
      key: `${page.permissionId}-${slot.suffix}`,
      id: button?.permissionId ?? null,
      label: slot.label
    };
  });
}

function permissionSuffix(permissionCode: string) {
  return permissionCode.split(':').pop()?.toLowerCase() ?? '';
}
