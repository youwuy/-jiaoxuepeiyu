import type { AdminRole, PageResponse } from '../../api/admin-role';
import type { AdminPermissionNode } from '../../api/admin-permission';

export const dataScopeLabels: Record<string, string> = {
  ALL: '全部数据',
  ORG_AND_CHILDREN: '本组织及下级',
  ORG_ONLY: '仅本组织',
  SELF: '仅本人数据'
};

export const mockAdminRoles: AdminRole[] = [
  {
    roleId: 1,
    roleName: '超级管理员',
    roleCode: 'super_admin',
    dataScope: 'ALL',
    remark: '系统最高权限角色，拥有全部菜单功能权限及全部数据权限，不可修改、删除、禁用',
    enabled: true,
    userCount: 1,
    permissionIds: [1, 2, 3, 4, 5, 6, 7],
    createdAt: '2024-01-15T09:00:00'
  },
  {
    roleId: 2,
    roleName: '平台管理员',
    roleCode: 'platform_admin',
    dataScope: 'ORG_AND_CHILDREN',
    remark: '负责系统基础设置、人员管理、权限分配等平台级运维管理，拥有管理组织范围内全量数据权限',
    enabled: true,
    userCount: 6,
    permissionIds: [1, 2, 3, 4, 5],
    createdAt: '2024-01-15T09:30:00'
  },
  {
    roleId: 3,
    roleName: '实训教师',
    roleCode: 'training_teacher',
    dataScope: 'SELF',
    remark: '负责教学课程管理、实训组课创建与发布、监考阅卷、成绩统计、资源管理等教学业务操作',
    enabled: true,
    userCount: 22,
    permissionIds: [5, 6, 7],
    createdAt: '2024-02-01T10:00:00'
  }
];

export function toRolePage(page = 1, pageSize = 20): PageResponse<AdminRole> {
  return {
    records: mockAdminRoles,
    total: mockAdminRoles.length,
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
