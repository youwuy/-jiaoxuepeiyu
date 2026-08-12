import { requestJson } from './http';
import * as XLSX from 'xlsx';

export type AdminAccountKind = 'teacher' | 'student';

export interface PageResponse<T> {
  records: T[];
  total: number;
  page: number;
  pageSize: number;
}

export interface AdminAccount {
  userId: number;
  accountNo: string;
  realName: string;
  phone?: string;
  maskedPhone?: string;
  idCard?: string;
  maskedIdCard?: string;
  jobTitle?: string;
  userType: AdminAccountKind | string;
  orgId?: number | null;
  orgName?: string;
  classId?: number | null;
  className?: string;
  enabled: boolean;
  faceRecorded?: boolean;
  fingerprintRecorded?: boolean;
  createdAt?: string;
  roleIds?: number[];
  roleNames?: string[];
  managedOrgIds?: number[];
  teachingClassIds?: number[];
}

export interface AdminAccountQuery {
  orgId?: number | null;
  classId?: number | null;
  realName?: string;
  accountNo?: string;
  phone?: string;
  jobTitle?: string;
  enabled?: boolean | null;
  page?: number;
  pageSize?: number;
}

export interface AdminAccountCommand {
  realName: string;
  accountNo?: string;
  phone?: string;
  idCard?: string;
  jobTitle?: string;
  userType?: AdminAccountKind;
  orgId?: number | null;
  classId?: number | null;
  faceFileId?: number | null;
  fingerprintFileId?: number | null;
  initialPassword?: string;
  roleIds?: number[];
  managedOrgIds?: number[];
  teachingClassIds?: number[];
}

export interface AdminAccountImportRow extends AdminAccountCommand {
  rowNo?: number;
  valid?: boolean;
  errors?: string[];
}

export interface AdminAccountImportPreview {
  totalCount: number;
  validCount: number;
  errorCount: number;
  rows: AdminAccountImportRow[];
}

export interface AdminAccountImportResult {
  importedCount: number;
  userIds: number[];
}

interface AdminAccountExportRow {
  accountNo: string;
  realName: string;
  maskedPhone?: string;
  maskedIdCard?: string;
  orgName?: string;
  className?: string;
  jobTitle?: string;
  enabled?: boolean;
  createdAt?: string;
}

export interface AdminClassOption {
  classId: number;
  majorId?: number;
  majorName?: string;
  className: string;
  enabled: boolean;
}

export interface AdminRoleOption {
  roleId: number;
  roleName: string;
  roleCode?: string;
  enabled?: boolean;
}

function accountPath(kind: AdminAccountKind) {
  return kind === 'teacher' ? '/admin/accounts/teachers' : '/admin/accounts/students';
}

function queryString(query: AdminAccountQuery = {}) {
  const params = new URLSearchParams();
  Object.entries(query).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      params.set(key, String(value));
    }
  });

  const text = params.toString();
  return text ? `?${text}` : '';
}

export async function fetchAdminAccounts(kind: AdminAccountKind, query: AdminAccountQuery = {}) {
  return requestJson<PageResponse<AdminAccount>>(`${accountPath(kind)}${queryString(query)}`, {
    fallbackLabel: kind === 'teacher' ? '教师账号列表' : '学员账号列表'
  });
}

export async function fetchAdminAccountDetail(userId: number) {
  return requestJson<AdminAccount>(`/admin/accounts/${userId}`, {
    fallbackLabel: '用户详情'
  });
}

export async function createAdminAccount(kind: AdminAccountKind, command: AdminAccountCommand) {
  const result = await requestJson<number | { userId: number }>(accountPath(kind), {
    method: 'POST',
    body: JSON.stringify({ ...command, userType: kind }),
    fallbackLabel: kind === 'teacher' ? '新增教师' : '新增学员'
  });

  return { userId: typeof result === 'number' ? result : result.userId };
}

export async function updateAdminAccount(kind: AdminAccountKind, userId: number, command: AdminAccountCommand) {
  return requestJson<void>(`${accountPath(kind)}/${userId}`, {
    method: 'PUT',
    body: JSON.stringify({ ...command, userType: kind }),
    fallbackLabel: kind === 'teacher' ? '编辑教师' : '编辑学员'
  });
}

export async function enableAdminAccount(userId: number) {
  return requestJson<void>(`/admin/accounts/${userId}/enable`, {
    method: 'POST',
    fallbackLabel: '启用账号'
  });
}

export async function disableAdminAccount(userId: number) {
  return requestJson<void>(`/admin/accounts/${userId}/disable`, {
    method: 'POST',
    fallbackLabel: '禁用账号'
  });
}

export async function resetAdminAccountPasswords(userIds: number[], password: string) {
  return requestJson<void>('/admin/accounts/batch/reset-password', {
    method: 'POST',
    body: JSON.stringify({ userIds, password }),
    fallbackLabel: '重置密码'
  });
}

export async function updateAdminAccountOrg(userIds: number[], orgId: number) {
  return requestJson<void>('/admin/accounts/batch/org', {
    method: 'POST',
    body: JSON.stringify({ userIds, orgId }),
    fallbackLabel: '批量设置所属组织'
  });
}

export async function updateAdminTeacherRoles(userId: number, roleIds: number[]) {
  return requestJson<void>(`/admin/accounts/teachers/${userId}/roles`, {
    method: 'PUT',
    body: JSON.stringify({ roleIds }),
    fallbackLabel: '设置角色'
  });
}

export async function previewAdminAccountImport(kind: AdminAccountKind, rows: AdminAccountImportRow[]) {
  return requestJson<AdminAccountImportPreview>(`${accountPath(kind)}/import/preview`, {
    method: 'POST',
    body: JSON.stringify({ rows }),
    fallbackLabel: '导入预览'
  });
}

export async function importAdminAccounts(kind: AdminAccountKind, rows: AdminAccountImportRow[]) {
  return requestJson<AdminAccountImportResult>(`${accountPath(kind)}/import`, {
    method: 'POST',
    body: JSON.stringify({ rows }),
    fallbackLabel: '导入用户'
  });
}

export async function exportAdminAccounts(kind: AdminAccountKind, query: AdminAccountQuery = {}) {
  const rows = await requestJson<AdminAccountExportRow[]>(`${accountPath(kind)}/export${queryString(query)}`, {
    fallbackLabel: '导出用户'
  });
  const table = rows.map((row) => ({
    [kind === 'teacher' ? '工号' : '学号']: row.accountNo,
    姓名: row.realName,
    手机号: row.maskedPhone || '',
    身份证号: row.maskedIdCard || '',
    ...(kind === 'teacher' ? { 岗位: row.jobTitle || '', 所属组织: row.orgName || '' } : { 班级: row.className || '' }),
    状态: row.enabled === false ? '禁用' : '启用',
    创建时间: row.createdAt || ''
  }));
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, XLSX.utils.json_to_sheet(table), kind === 'teacher' ? '教师' : '学员');
  XLSX.writeFile(workbook, `${kind === 'teacher' ? '教师' : '学员'}账号.xlsx`);
}

export async function fetchAdminClasses() {
  return requestJson<AdminClassOption[]>('/admin/classes', {
    fallbackLabel: '班级列表'
  });
}

export async function fetchAdminRoles() {
  return requestJson<PageResponse<AdminRoleOption> | AdminRoleOption[]>('/admin/roles?page=1&pageSize=200', {
    fallbackLabel: '角色列表'
  });
}
