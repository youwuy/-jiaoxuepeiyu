import { requestJson } from './http';

export interface AdminOrgNode {
  orgId: number;
  parentId?: number | null;
  orgName: string;
  sortOrder: number;
  enabled: boolean;
  creatorName?: string;
  createdAt?: string;
  updaterName?: string;
  updatedAt?: string;
  children?: AdminOrgNode[];
}

export interface AdminOrgCommand {
  parentId?: number | null;
  orgName: string;
  sortOrder: number;
}

export async function fetchAdminOrgTree() {
  return requestJson<AdminOrgNode[]>('/admin/org/tree', {
    fallbackLabel: '组织树'
  });
}

export async function createAdminOrg(command: AdminOrgCommand) {
  const result = await requestJson<number | { orgId: number }>('/admin/org', {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: '新增组织'
  });

  return { orgId: typeof result === 'number' ? result : result.orgId };
}

export async function updateAdminOrg(orgId: number, command: AdminOrgCommand) {
  return requestJson<void>(`/admin/org/${orgId}`, {
    method: 'PUT',
    body: JSON.stringify(command),
    fallbackLabel: '编辑组织'
  });
}

export async function enableAdminOrg(orgId: number) {
  return requestJson<void>(`/admin/org/${orgId}/enable`, {
    method: 'POST',
    fallbackLabel: '启用组织'
  });
}

export async function disableAdminOrg(orgId: number) {
  return requestJson<void>(`/admin/org/${orgId}/disable`, {
    method: 'POST',
    fallbackLabel: '禁用组织'
  });
}
