import type { AdminAccount, AdminAccountKind, AdminClassOption, AdminRoleOption, PageResponse } from '../../api/admin-account';
import type { AdminOrgNode } from '../../api/admin-org';

export interface AdminAccountTab {
  key: AdminAccountKind;
  label: string;
  count: number;
}

export const mockAdminAccountTabs: AdminAccountTab[] = [
  { key: 'teacher', label: '教师管理', count: 128 },
  { key: 'student', label: '学员管理', count: 436 }
];

export const mockAdminAccounts: Record<AdminAccountKind, AdminAccount[]> = {
  teacher: [
    {
      userId: 101,
      accountNo: 'T20240001',
      realName: '李明远',
      maskedPhone: '138****6021',
      maskedIdCard: '3201**********1234',
      jobTitle: '信号系统教师',
      userType: 'teacher',
      orgId: 11,
      orgName: '交通与车辆工程学院/信号控制教研室',
      enabled: true,
      createdAt: '2024-01-15T09:30:00',
      roleIds: [1, 3],
      roleNames: ['普通管理员', '普通教师'],
      managedOrgIds: [11],
      teachingClassIds: [201, 202]
    },
    {
      userId: 102,
      accountNo: 'T20240003',
      realName: '周倩',
      maskedPhone: '139****3456',
      maskedIdCard: '3201**********4478',
      jobTitle: '车辆检修教师',
      userType: 'teacher',
      orgId: 12,
      orgName: '交通与车辆工程学院/车辆检修教研室',
      enabled: true,
      createdAt: '2024-02-20T10:15:00',
      roleIds: [3],
      roleNames: ['普通教师'],
      managedOrgIds: [12],
      teachingClassIds: [203]
    },
    {
      userId: 103,
      accountNo: 'T20240008',
      realName: '赵运会',
      maskedPhone: '186****7890',
      maskedIdCard: '-',
      jobTitle: '设备运维员',
      userType: 'teacher',
      orgId: 13,
      orgName: '交通与车辆工程学院/运输管理教研室',
      enabled: false,
      createdAt: '2024-03-05T14:20:00',
      roleIds: [2, 3],
      roleNames: ['实训管理员', '普通教师'],
      managedOrgIds: [13],
      teachingClassIds: [204]
    }
  ],
  student: [
    {
      userId: 201,
      accountNo: 'S202401001',
      realName: '张林林',
      maskedPhone: '137****2190',
      maskedIdCard: '4301**********4217',
      userType: 'student',
      orgId: 11,
      orgName: '交通与车辆工程学院',
      classId: 201,
      className: '城轨运营 2401 班',
      enabled: true,
      faceRecorded: true,
      fingerprintRecorded: false,
      createdAt: '2024-02-18T09:20:00'
    },
    {
      userId: 202,
      accountNo: 'S202401018',
      realName: '王一然',
      maskedPhone: '185****7632',
      maskedIdCard: '3301**********1036',
      userType: 'student',
      orgId: 11,
      orgName: '交通与车辆工程学院',
      classId: 202,
      className: '信号控制 2402 班',
      enabled: true,
      faceRecorded: false,
      fingerprintRecorded: false,
      createdAt: '2024-02-19T11:05:00'
    },
    {
      userId: 203,
      accountNo: 'S202402006',
      realName: '陈思远',
      maskedPhone: '136****8891',
      maskedIdCard: '-',
      userType: 'student',
      orgId: 12,
      orgName: '交通与车辆工程学院',
      classId: 203,
      className: '车辆检修 2401 班',
      enabled: false,
      faceRecorded: true,
      fingerprintRecorded: true,
      createdAt: '2024-03-02T15:42:00'
    }
  ]
};

export const mockAdminClasses: AdminClassOption[] = [
  { classId: 201, majorId: 1, majorName: '城轨运营', className: '城轨运营 2401 班', enabled: true },
  { classId: 202, majorId: 2, majorName: '信号控制', className: '信号控制 2402 班', enabled: true },
  { classId: 203, majorId: 3, majorName: '车辆检修', className: '车辆检修 2401 班', enabled: true },
  { classId: 204, majorId: 1, majorName: '城轨运营', className: '城轨运营 2402 班', enabled: true }
];

export const mockAdminRoles: AdminRoleOption[] = [
  { roleId: 1, roleName: '普通管理员', roleCode: 'admin', enabled: true },
  { roleId: 2, roleName: '实训管理员', roleCode: 'training_admin', enabled: true },
  { roleId: 3, roleName: '普通教师', roleCode: 'teacher', enabled: true }
];

export const mockAccountOrgTree: AdminOrgNode[] = [
  {
    orgId: 10,
    parentId: null,
    orgName: '交通与车辆工程学院',
    sortOrder: 1,
    enabled: true,
    children: [
      { orgId: 11, parentId: 10, orgName: '信号控制教研室', sortOrder: 1, enabled: true, children: [] },
      { orgId: 12, parentId: 10, orgName: '车辆检修教研室', sortOrder: 2, enabled: true, children: [] },
      { orgId: 13, parentId: 10, orgName: '运输管理教研室', sortOrder: 3, enabled: true, children: [] }
    ]
  }
];

export function toAccountPage(kind: AdminAccountKind, page = 1, pageSize = 20): PageResponse<AdminAccount> {
  const records = mockAdminAccounts[kind];
  return {
    records,
    total: kind === 'teacher' ? 128 : 436,
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
