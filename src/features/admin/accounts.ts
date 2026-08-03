import type { AdminAccount, AdminAccountKind, AdminClassOption, AdminRoleOption, PageResponse } from '../../api/admin-account';
import type { AdminOrgNode } from '../../api/admin-org';

export interface AdminAccountTab {
  key: AdminAccountKind;
  label: string;
  count: number;
}

export const mockAdminAccounts: Record<AdminAccountKind, AdminAccount[]> = {
  teacher: [],
  student: []
};

export const mockAdminClasses: AdminClassOption[] = [];

export const mockAdminRoles: AdminRoleOption[] = [];

export const mockAccountOrgTree: AdminOrgNode[] = [];

export function toAccountPage(kind: AdminAccountKind, page = 1, pageSize = 20): PageResponse<AdminAccount> {
  return {
    records: mockAdminAccounts[kind],
    total: 0,
    page,
    pageSize
  };
}

export function normalizeRoleOptions(payload: PageResponse<AdminRoleOption> | AdminRoleOption[]) {
  return Array.isArray(payload) ? payload : payload.records;
}

export function flattenOrgOptions(nodes: AdminOrgNode[], level = 0): Array<{ orgId: number; orgName: string; label: string }> {
  return nodes.flatMap((node) => [
    {
      orgId: node.orgId,
      orgName: node.orgName,
      label: `${'　'.repeat(level)}${node.orgName}`
    },
    ...flattenOrgOptions(node.children ?? [], level + 1)
  ]);
}

export function formatAccountTime(value?: string) {
  if (!value) {
    return '-';
  }
  return value.replace('T', ' ').slice(0, 16);
}

export function compactList(values?: Array<string | number>) {
  if (!values || values.length === 0) {
    return '-';
  }
  return values.join('、');
}

export function accountKindLabel(kind: AdminAccountKind) {
  return kind === 'teacher' ? '教师' : '学员';
}
