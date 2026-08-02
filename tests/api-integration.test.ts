import { afterEach, describe, expect, it, vi } from 'vitest';
import { loginAdmin, loginStudent } from '../src/api/auth';
import {
  cancelPublishAdminCourse,
  fetchAdminCourseLogs,
  fetchAdminCourseStatistics,
  fetchAdminCourses,
  publishAdminCourse
} from '../src/api/admin-course';
import {
  cancelPublishAdminPaper,
  createAdminPaper,
  fetchAdminPaper,
  fetchAdminPaperLogs,
  fetchAdminPapers,
  publishAdminPaper,
  updateAdminPaper
} from '../src/api/admin-paper';
import { createAdminOrg, disableAdminOrg, enableAdminOrg, fetchAdminOrgTree, updateAdminOrg } from '../src/api/admin-org';
import {
  createAdminPermission,
  deleteAdminPermission,
  disableAdminPermission,
  enableAdminPermission,
  fetchAdminPermissionTree,
  updateAdminPermissionSorts,
  updateAdminPermission
} from '../src/api/admin-permission';
import {
  createAdminAccount,
  disableAdminAccount,
  enableAdminAccount,
  exportAdminAccounts,
  fetchAdminAccounts,
  resetAdminAccountPasswords,
  updateAdminAccount,
  updateAdminAccountOrg,
  updateAdminTeacherRoles
} from '../src/api/admin-account';
import {
  createAdminRole,
  deleteAdminRole,
  disableAdminRole,
  enableAdminRole,
  fetchAdminRoleLogs,
  fetchAdminRoles,
  updateAdminRole,
  updateAdminRolePermissions
} from '../src/api/admin-role';
import {
  fetchAdminProfile,
  updateAdminPassword,
  updateAdminProfileIdCard,
  updateAdminProfilePhone
} from '../src/api/admin-profile';
import {
  createAdminClassroom,
  createAdminScoreWeight,
  disableAdminClass,
  enableAdminClass,
  setAdminCurrentSemester
} from '../src/api/admin-settings';
import {
  exportAdminSemesterScores,
  fetchAdminSemesterScores,
  fetchAdminSemesterScoreStatistics
} from '../src/api/admin-semester-score';
import { clearAuthSession, requestJson, tryRequestJson } from '../src/api/http';
import {
  createTrainingRoom,
  fetchStudentArchiveDetail,
  fetchStudentCourses,
  fetchStudentProfile,
  fetchStudentResources,
  fetchStudentTrainings,
  fetchTrainingAppInstallation,
  fetchTrainingRoom,
  markAllStudentMessagesRead,
  markStudentMessageRead,
  startTrainingRoom,
  updateCoursewareProgress,
  updateStudentIdCard,
  updateStudentPassword,
  updateStudentPhone
} from '../src/api/student';

const originalFetch = globalThis.fetch;

afterEach(() => {
  globalThis.fetch = originalFetch;
  clearAuthSession();
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
});

class MemoryStorage {
  private values = new Map<string, string>();

  getItem(key: string) {
    return this.values.get(key) ?? null;
  }

  setItem(key: string, value: string) {
    this.values.set(key, value);
  }

  removeItem(key: string) {
    this.values.delete(key);
  }

  clear() {
    this.values.clear();
  }
}

function mockJsonResponse(payload: unknown, init: ResponseInit = {}) {
  return Promise.resolve(
    new Response(JSON.stringify(payload), {
      status: 200,
      headers: { 'Content-Type': 'application/json' },
      ...init
    })
  );
}

describe('api http client', () => {
  it('prefixes relative paths with the api base url and unwraps data envelopes', async () => {
    const fetchMock = vi.fn(() => mockJsonResponse({ code: 200, data: { status: 'OK' } }));
    globalThis.fetch = fetchMock as typeof fetch;

    await expect(requestJson<{ status: string }>('/health')).resolves.toEqual({ status: 'OK' });
    expect(fetchMock).toHaveBeenCalledWith('/api/health', expect.any(Object));
  });

  it('tries candidate endpoints until one succeeds', async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(new Response('not found', { status: 404 }))
      .mockImplementationOnce(() => mockJsonResponse({ data: ['课程'] }));
    globalThis.fetch = fetchMock as typeof fetch;

    await expect(tryRequestJson<string[]>(['/missing', '/student/courses'])).resolves.toEqual(['课程']);
    expect(fetchMock).toHaveBeenCalledTimes(2);
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/student/courses', expect.any(Object));
  });

  it('passes the selected admin login type to the documented auth endpoint', async () => {
    const fetchMock = vi.fn(() => mockJsonResponse({ data: { token: 'test-token' } }));
    globalThis.fetch = fetchMock as typeof fetch;

    await loginAdmin({ account: '13800138000', password: 'abc123' }, 'phone');

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/auth/admin/login',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({ loginType: 'phone', account: '13800138000', password: 'abc123' })
      })
    );
  });

  it('keeps admin and student auth headers isolated after both portals login', async () => {
    vi.stubGlobal('localStorage', new MemoryStorage());
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => mockJsonResponse({ data: { token: 'admin-token', user: { id: 1 } } }))
      .mockImplementationOnce(() => mockJsonResponse({ data: { token: 'student-token', user: { id: 6 } } }))
      .mockImplementation(() => mockJsonResponse({ data: [] }));
    globalThis.fetch = fetchMock as typeof fetch;

    await loginAdmin({ account: 'admin', password: 'admin123' });
    await loginStudent('studentId', { studentId: '0012', password: 'student123' });
    await requestJson('/admin/roles');
    await requestJson('/student/courses');

    const adminHeaders = fetchMock.mock.calls[2][1]?.headers as Headers;
    const studentHeaders = fetchMock.mock.calls[3][1]?.headers as Headers;
    expect(adminHeaders.get('Authorization')).toBe('Bearer admin-token');
    expect(adminHeaders.get('X-User-Id')).toBe('1');
    expect(studentHeaders.get('Authorization')).toBe('Bearer student-token');
    expect(studentHeaders.get('X-User-Id')).toBe('6');
  });

  it('loads the documented admin course list endpoint and query string', async () => {
    const fetchMock = vi.fn(() =>
      mockJsonResponse({
        data: {
          records: [
            {
              courseId: 1,
              courseName: '城市轨道交通信号系统原理',
              publishStatus: 'PUBLISHED'
            }
          ],
          total: 15,
          page: 1,
          pageSize: 100
        }
      })
    );
    globalThis.fetch = fetchMock as typeof fetch;

    await expect(fetchAdminCourses({ keyword: '信号', publishStatus: 'PUBLISHED', page: 1, pageSize: 100 })).resolves.toMatchObject({
      records: [{ courseId: 1, courseName: '城市轨道交通信号系统原理' }],
      total: 15,
      pageSize: 100
    });

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/admin/courses?keyword=%E4%BF%A1%E5%8F%B7&publishStatus=PUBLISHED&page=1&pageSize=100',
      expect.any(Object)
    );
  });

  it('uses the documented admin course mutation endpoints', async () => {
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => mockJsonResponse({ data: null }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }))
      .mockImplementationOnce(() =>
        mockJsonResponse({
          data: [
            {
              logId: 99,
              courseId: 3,
              operatorName: '李教师',
              action: 'UPDATE',
              content: 'Update course',
              createdAt: '2026-07-31T10:00:00'
            }
          ]
        })
      )
      .mockImplementationOnce(() =>
        mockJsonResponse({ data: { courseId: 3, studentCount: 20, completedCount: 12, studyingCount: 5, notStartedCount: 3, pendingReviewCount: 4, averageScore: 88.5 } })
      );
    globalThis.fetch = fetchMock as typeof fetch;

    await publishAdminCourse(3);
    await cancelPublishAdminCourse(3);
    await expect(fetchAdminCourseLogs(3)).resolves.toMatchObject([
      { logId: 99, action: 'UPDATE', operatorName: '李教师' }
    ]);
    await expect(fetchAdminCourseStatistics(3)).resolves.toMatchObject({
      courseId: 3,
      averageScore: 88.5
    });

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/admin/courses/3/publish', expect.objectContaining({ method: 'POST' }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/admin/courses/3/cancel-publish', expect.objectContaining({ method: 'POST' }));
    expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/admin/courses/3/logs', expect.any(Object));
    expect(fetchMock).toHaveBeenNthCalledWith(4, '/api/admin/courses/3/statistics', expect.any(Object));
  });

  it('uses the documented admin paper endpoints', async () => {
    const command = {
      paperName: '期中理论试卷',
      composeMode: 'MANUAL',
      questions: [{ questionId: 1, score: 5 }]
    };
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() =>
        mockJsonResponse({
          data: {
            records: [{ paperId: 7, paperName: '期中理论试卷', publishStatus: 'OFFLINE' }],
            total: 1,
            page: 1,
            pageSize: 12
          }
        })
      )
      .mockImplementationOnce(() => mockJsonResponse({ data: { paperId: 7, paperName: '期中理论试卷', questions: [{ questionId: 1, score: 5 }] } }))
      .mockImplementationOnce(() => mockJsonResponse({ data: 7 }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }))
      .mockImplementationOnce(() => mockJsonResponse({ data: [{ logId: 1, paperId: 7, action: 'CREATE' }] }));
    globalThis.fetch = fetchMock as typeof fetch;

    await expect(fetchAdminPapers({ keyword: '期中', publishStatus: 'OFFLINE', creatorId: 3, page: 1, pageSize: 12 })).resolves.toMatchObject({
      records: [{ paperId: 7 }],
      total: 1
    });
    await expect(fetchAdminPaper(7)).resolves.toMatchObject({ paperId: 7, questions: [{ questionId: 1 }] });
    await expect(createAdminPaper(command)).resolves.toEqual(7);
    await updateAdminPaper(7, command);
    await publishAdminPaper(7);
    await cancelPublishAdminPaper(7);
    await expect(fetchAdminPaperLogs(7)).resolves.toMatchObject([{ logId: 1, action: 'CREATE' }]);

    expect(fetchMock).toHaveBeenNthCalledWith(
      1,
      '/api/admin/papers?keyword=%E6%9C%9F%E4%B8%AD&publishStatus=OFFLINE&creatorId=3&page=1&pageSize=12',
      expect.any(Object)
    );
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/admin/papers/7', expect.any(Object));
    expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/admin/papers', expect.objectContaining({ method: 'POST', body: JSON.stringify(command) }));
    expect(fetchMock).toHaveBeenNthCalledWith(4, '/api/admin/papers/7', expect.objectContaining({ method: 'PUT', body: JSON.stringify(command) }));
    expect(fetchMock).toHaveBeenNthCalledWith(5, '/api/admin/papers/7/publish', expect.objectContaining({ method: 'POST' }));
    expect(fetchMock).toHaveBeenNthCalledWith(6, '/api/admin/papers/7/cancel-publish', expect.objectContaining({ method: 'POST' }));
    expect(fetchMock).toHaveBeenNthCalledWith(7, '/api/admin/papers/7/logs', expect.any(Object));
  });

  it('uses the documented admin organization endpoints', async () => {
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() =>
        mockJsonResponse({
          data: [
            {
              orgId: 1,
              parentId: null,
              orgName: '交通与车辆工程学院',
              sortOrder: 1,
              enabled: true,
              children: []
            }
          ]
        })
      )
      .mockImplementationOnce(() => mockJsonResponse({ data: 9 }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }));
    globalThis.fetch = fetchMock as typeof fetch;

    await expect(fetchAdminOrgTree()).resolves.toMatchObject([{ orgId: 1, orgName: '交通与车辆工程学院' }]);
    await expect(createAdminOrg({ parentId: null, orgName: '城轨学院', sortOrder: 2 })).resolves.toEqual({ orgId: 9 });
    await updateAdminOrg(9, { parentId: null, orgName: '城轨学院', sortOrder: 2 });
    await disableAdminOrg(9);
    await enableAdminOrg(9);

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/admin/org/tree', expect.any(Object));
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      '/api/admin/org',
      expect.objectContaining({ method: 'POST', body: JSON.stringify({ parentId: null, orgName: '城轨学院', sortOrder: 2 }) })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      3,
      '/api/admin/org/9',
      expect.objectContaining({ method: 'PUT', body: JSON.stringify({ parentId: null, orgName: '城轨学院', sortOrder: 2 }) })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(4, '/api/admin/org/9/disable', expect.objectContaining({ method: 'POST' }));
    expect(fetchMock).toHaveBeenNthCalledWith(5, '/api/admin/org/9/enable', expect.objectContaining({ method: 'POST' }));
  });

  it('uses the documented admin permission endpoints', async () => {
    const command = {
      parentId: null,
      permissionName: '功能管理',
      permissionCode: 'system:permission',
      permissionType: 'MENU' as const,
      routePath: '/admin/permissions',
      visible: true,
      sortOrder: 3
    };
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() =>
        mockJsonResponse({
          data: [
            {
              permissionId: 1,
              parentId: null,
              permissionName: '系统基础设置',
              permissionCode: 'system',
              permissionType: 'MENU',
              routePath: '/system',
              visible: true,
              sortOrder: 1,
              children: []
            }
          ]
        })
      )
      .mockImplementationOnce(() => mockJsonResponse({ data: 12 }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }));
    globalThis.fetch = fetchMock as typeof fetch;

    await expect(fetchAdminPermissionTree()).resolves.toMatchObject([{ permissionId: 1, permissionName: '系统基础设置' }]);
    await expect(createAdminPermission(command)).resolves.toEqual({ permissionId: 12 });
    await updateAdminPermission(12, command);
    await disableAdminPermission(12);
    await enableAdminPermission(12);
    await updateAdminPermissionSorts([{ permissionId: 12, parentId: null, sortOrder: 4 }]);
    await deleteAdminPermission(12);

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/admin/permissions/tree', expect.any(Object));
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      '/api/admin/permissions',
      expect.objectContaining({ method: 'POST', body: JSON.stringify(command) })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      3,
      '/api/admin/permissions/12',
      expect.objectContaining({ method: 'PUT', body: JSON.stringify(command) })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(4, '/api/admin/permissions/12/disable', expect.objectContaining({ method: 'POST' }));
    expect(fetchMock).toHaveBeenNthCalledWith(5, '/api/admin/permissions/12/enable', expect.objectContaining({ method: 'POST' }));
    expect(fetchMock).toHaveBeenNthCalledWith(
      6,
      '/api/admin/permissions/sort',
      expect.objectContaining({ method: 'PUT', body: JSON.stringify({ items: [{ permissionId: 12, parentId: null, sortOrder: 4 }] }) })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(7, '/api/admin/permissions/12/delete', expect.objectContaining({ method: 'POST' }));
  });

  it('uses the documented admin account management endpoints', async () => {
    const command = {
      realName: '李明远',
      accountNo: 'T20240001',
      phone: '13800138000',
      jobTitle: '信号系统教师',
      orgId: 11,
      roleIds: [1, 3],
      managedOrgIds: [11],
      teachingClassIds: [201]
    };
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() =>
        mockJsonResponse({
          data: {
            records: [{ userId: 101, accountNo: 'T20240001', realName: '李明远', userType: 'teacher', enabled: true }],
            total: 1,
            page: 1,
            pageSize: 20
          }
        })
      )
      .mockImplementationOnce(() => mockJsonResponse({ data: 101 }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }));
    globalThis.fetch = fetchMock as typeof fetch;

    await expect(fetchAdminAccounts('teacher', { realName: '李', jobTitle: '信号', enabled: true, page: 1, pageSize: 20 })).resolves.toMatchObject({
      records: [{ userId: 101, accountNo: 'T20240001' }],
      total: 1
    });
    await expect(createAdminAccount('teacher', command)).resolves.toEqual({ userId: 101 });
    await updateAdminAccount('teacher', 101, command);
    await disableAdminAccount(101);
    await enableAdminAccount(101);
    await resetAdminAccountPasswords([101, 102], 'Abc@12345');
    await updateAdminAccountOrg([101, 102], 11);
    await updateAdminTeacherRoles(101, [1, 3]);

    expect(fetchMock).toHaveBeenNthCalledWith(
      1,
      '/api/admin/accounts/teachers?realName=%E6%9D%8E&jobTitle=%E4%BF%A1%E5%8F%B7&enabled=true&page=1&pageSize=20',
      expect.any(Object)
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      '/api/admin/accounts/teachers',
      expect.objectContaining({ method: 'POST', body: JSON.stringify({ ...command, userType: 'teacher' }) })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      3,
      '/api/admin/accounts/teachers/101',
      expect.objectContaining({ method: 'PUT', body: JSON.stringify({ ...command, userType: 'teacher' }) })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(4, '/api/admin/accounts/101/disable', expect.objectContaining({ method: 'POST' }));
    expect(fetchMock).toHaveBeenNthCalledWith(5, '/api/admin/accounts/101/enable', expect.objectContaining({ method: 'POST' }));
    expect(fetchMock).toHaveBeenNthCalledWith(
      6,
      '/api/admin/accounts/batch/reset-password',
      expect.objectContaining({ method: 'POST', body: JSON.stringify({ userIds: [101, 102], password: 'Abc@12345' }) })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      7,
      '/api/admin/accounts/batch/org',
      expect.objectContaining({ method: 'POST', body: JSON.stringify({ userIds: [101, 102], orgId: 11 }) })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      8,
      '/api/admin/accounts/teachers/101/roles',
      expect.objectContaining({ method: 'PUT', body: JSON.stringify({ roleIds: [1, 3] }) })
    );
  });

  it('downloads admin account exports from the file endpoint', async () => {
    const fetchMock = vi.fn(() =>
      Promise.resolve(
        new Response('User ID,Account No\r\n101,T20240001\r\n', {
          status: 200,
          headers: {
            'Content-Type': 'text/csv;charset=UTF-8',
            'Content-Disposition': 'attachment; filename="teacher-accounts.csv"'
          }
        })
      )
    );
    const appendChild = vi.fn();
    const removeChild = vi.fn();
    const click = vi.fn();
    const anchor = { href: '', download: '', click };
    globalThis.fetch = fetchMock as typeof fetch;
    globalThis.URL.createObjectURL = vi.fn(() => 'blob:accounts');
    globalThis.URL.revokeObjectURL = vi.fn();
    globalThis.document = {
      createElement: vi.fn(() => anchor),
      body: { appendChild, removeChild }
    } as unknown as Document;

    await exportAdminAccounts('teacher', { accountNo: 'T2024', page: 1, pageSize: 20 });

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/admin/accounts/teachers/export/file?accountNo=T2024&page=1&pageSize=20',
      expect.any(Object)
    );
    expect(anchor.download).toBe('teacher-accounts.csv');
    expect(anchor.href).toBe('blob:accounts');
    expect(click).toHaveBeenCalledTimes(1);
    expect(removeChild).toHaveBeenCalledWith(anchor);
    expect(globalThis.URL.revokeObjectURL).toHaveBeenCalledWith('blob:accounts');
  });

  it('uses the documented admin role management endpoints', async () => {
    const command = {
      roleName: '实训教师',
      roleCode: 'training_teacher',
      dataScope: 'SELF' as const,
      remark: '负责教学业务操作',
      permissionIds: [5, 6, 7]
    };
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() =>
        mockJsonResponse({
          data: {
            records: [{ roleId: 3, roleName: '实训教师', roleCode: 'training_teacher', enabled: true }],
            total: 1,
            page: 1,
            pageSize: 20
          }
        })
      )
      .mockImplementationOnce(() => mockJsonResponse({ data: 3 }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }))
      .mockImplementationOnce(() => mockJsonResponse({ data: null }))
      .mockImplementationOnce(() =>
        mockJsonResponse({
          data: [{ logId: 9, roleId: 3, operatorName: '李教师', action: 'UPDATE', content: 'Update role' }]
        })
      );
    globalThis.fetch = fetchMock as typeof fetch;

    await expect(fetchAdminRoles({ keyword: '实训', enabled: true, page: 1, pageSize: 20 })).resolves.toMatchObject({
      records: [{ roleId: 3, roleName: '实训教师' }],
      total: 1
    });
    await expect(createAdminRole(command)).resolves.toEqual({ roleId: 3 });
    await updateAdminRole(3, command);
    await disableAdminRole(3);
    await enableAdminRole(3);
    await updateAdminRolePermissions(3, [5, 6]);
    await deleteAdminRole(3);
    await expect(fetchAdminRoleLogs(3)).resolves.toMatchObject([{ logId: 9, action: 'UPDATE' }]);

    expect(fetchMock).toHaveBeenNthCalledWith(
      1,
      '/api/admin/roles?keyword=%E5%AE%9E%E8%AE%AD&enabled=true&page=1&pageSize=20',
      expect.any(Object)
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      '/api/admin/roles',
      expect.objectContaining({ method: 'POST', body: JSON.stringify(command) })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      3,
      '/api/admin/roles/3',
      expect.objectContaining({ method: 'PUT', body: JSON.stringify(command) })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(4, '/api/admin/roles/3/disable', expect.objectContaining({ method: 'POST' }));
    expect(fetchMock).toHaveBeenNthCalledWith(5, '/api/admin/roles/3/enable', expect.objectContaining({ method: 'POST' }));
    expect(fetchMock).toHaveBeenNthCalledWith(
      6,
      '/api/admin/roles/3/permissions',
      expect.objectContaining({ method: 'PUT', body: JSON.stringify({ permissionIds: [5, 6] }) })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(7, '/api/admin/roles/3/delete', expect.objectContaining({ method: 'POST' }));
    expect(fetchMock).toHaveBeenNthCalledWith(8, '/api/admin/roles/3/logs', expect.any(Object));
  });

  it('uses real admin profile endpoints and sends admin auth to password change', async () => {
    vi.stubGlobal('localStorage', new MemoryStorage());
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => mockJsonResponse({ data: { token: 'admin-token', user: { id: 1 } } }))
      .mockImplementation(() => mockJsonResponse({ data: { accountNo: 'A001', realName: '管理员' } }));
    globalThis.fetch = fetchMock as typeof fetch;

    await loginAdmin({ account: 'admin', password: 'admin123' });
    await fetchAdminProfile();
    await updateAdminProfilePhone('13800138000');
    await updateAdminProfileIdCard('410322201005124734');
    await updateAdminPassword('oldPass123', 'newPass123', 'newPass123');

    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/admin/profile', expect.any(Object));
    expect(fetchMock).toHaveBeenNthCalledWith(
      3,
      '/api/admin/profile/phone',
      expect.objectContaining({ method: 'PUT', body: JSON.stringify({ phone: '13800138000' }) })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      4,
      '/api/admin/profile/id-card',
      expect.objectContaining({ method: 'PUT', body: JSON.stringify({ idCard: '410322201005124734' }) })
    );

    const passwordOptions = fetchMock.mock.calls[4][1] as RequestInit;
    const passwordHeaders = passwordOptions.headers as Headers;
    expect(fetchMock.mock.calls[4][0]).toBe('/api/auth/password');
    expect(passwordOptions.method).toBe('PUT');
    expect(passwordOptions.body).toBe(JSON.stringify({ currentPassword: 'oldPass123', newPassword: 'newPass123', confirmPassword: 'newPass123' }));
    expect(passwordHeaders.get('Authorization')).toBe('Bearer admin-token');
  });

  it('maps backend student course cards into the existing course UI model', async () => {
    const fetchMock = vi.fn(() =>
      mockJsonResponse({
        data: [
          {
            courseId: 1,
            courseName: '城市轨道交通概论',
            academicTerm: '2026 下学期',
            progressPercent: 50,
            coursewareCount: 3,
            assignmentCount: 4,
            teacherNames: '王老师、李老师',
            openStartTime: '2026-07-01T00:00:00',
            openEndTime: '2026-08-31T23:59:59'
          }
        ]
      })
    );
    globalThis.fetch = fetchMock as typeof fetch;

    await expect(fetchStudentCourses()).resolves.toMatchObject([
      {
        id: 1,
        name: '城市轨道交通概论',
        completedItems: 50,
        totalItems: 100,
        teachers: ['王老师', '李老师']
      }
    ]);
  });

  it('passes course keyword filters to the documented course endpoint', async () => {
    const fetchMock = vi.fn(() => mockJsonResponse({ data: [] }));
    globalThis.fetch = fetchMock as typeof fetch;

    await fetchStudentCourses('信号 系统');

    expect(fetchMock).toHaveBeenCalledWith('/api/student/courses?keyword=%E4%BF%A1%E5%8F%B7+%E7%B3%BB%E7%BB%9F', expect.any(Object));
  });

  it('passes documented training filters and maps status and mode fields', async () => {
    const fetchMock = vi.fn(() =>
      mockJsonResponse({
        data: [
          {
            trainingId: 11,
            trainingName: '站台门故障处置',
            trainingMode: 'team',
            status: 'finished',
            openStartTime: '2026-07-01T00:00:00',
            openEndTime: '2026-07-31T23:59:59',
            teamSize: 4,
            roleCount: 3,
            activeRoomId: 88
          }
        ]
      })
    );
    globalThis.fetch = fetchMock as typeof fetch;

    await expect(fetchStudentTrainings({ mode: 'team', keyword: '站台门' })).resolves.toMatchObject([
      {
        id: 11,
        title: '站台门故障处置',
        mode: 'team',
        status: 'completed',
        activeRoomId: 88
      }
    ]);
    expect(fetchMock).toHaveBeenCalledWith(
      '/api/student/trainings?mode=team&keyword=%E7%AB%99%E5%8F%B0%E9%97%A8',
      expect.any(Object)
    );
  });

  it('uses the documented public resource endpoint and maps file metadata', async () => {
    const fetchMock = vi.fn(() =>
      mockJsonResponse({
        data: [
          {
            resourceId: 9,
            resourceName: '实训操作规范手册',
            resourceType: 'PDF',
            fileSize: 1048576,
            majorName: '城轨运营',
            updatedAt: '2026-07-30T10:30:00'
          }
        ]
      })
    );
    globalThis.fetch = fetchMock as typeof fetch;

    await expect(fetchStudentResources({ keyword: '规范', resourceType: 'PDF' })).resolves.toMatchObject([
      {
        id: 9,
        title: '实训操作规范手册',
        type: 'PDF',
        size: '1.0 MB'
      }
    ]);
    expect(fetchMock).toHaveBeenCalledWith(
      '/api/student/resources/public?keyword=%E8%A7%84%E8%8C%83&resourceType=PDF',
      expect.any(Object)
    );
  });

  it('combines documented profile, score, message, and archive endpoints for the profile page', async () => {
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() =>
        mockJsonResponse({ data: { studentId: 7, studentNo: 'A20260001', realName: '张林林', className: '城轨运营 2401 班' } })
      )
      .mockImplementationOnce(() =>
        mockJsonResponse({
          data: [
            {
              coursewareLearningScore: 92,
              trainingPracticeScore: 90,
              courseAssignmentScore: 86,
              examScore: 88,
              coursewareWeight: 20,
              trainingPracticeWeight: 30,
              assignmentWeight: 30,
              examWeight: 20
            }
          ]
        })
      )
      .mockImplementationOnce(() =>
        mockJsonResponse({ data: { unreadCount: 1, messages: [{ id: 3, title: '课程通知', messageType: '通知', read: false }] } })
      )
      .mockImplementationOnce(() =>
        mockJsonResponse({ data: [{ archiveId: 5, trainingName: '站台门故障处置', durationSeconds: 3661, personalScore: 91 }] })
      );
    globalThis.fetch = fetchMock as typeof fetch;

    const profile = await fetchStudentProfile();

    expect(profile.student).toMatchObject({ name: '张林林', studentId: 'A20260001' });
    expect(profile.scoreParts).toContainEqual({ label: '课件完成度', score: 92, weight: 0.2 });
    expect(profile.messages).toMatchObject([{ id: 3, unread: true }]);
    expect(profile.archives).toMatchObject([{ id: 5, duration: '01:01:01', score: 91 }]);
  });

  it('uses the documented courseware progress endpoint and request body', async () => {
    const fetchMock = vi.fn(() => mockJsonResponse({ code: 0, data: null }));
    globalThis.fetch = fetchMock as typeof fetch;

    await updateCoursewareProgress(12, 34, 90, false);

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/student/courses/12/progress',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({ contentId: 34, studiedSeconds: 90, completed: false })
      })
    );
  });

  it('uses documented training room and app installation endpoints', async () => {
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => mockJsonResponse({ data: { installed: true, version: '1.0.0' } }))
      .mockImplementationOnce(() => mockJsonResponse({ data: { roomId: 77, trainingId: 66, roomCode: 'A100' } }))
      .mockImplementationOnce(() => mockJsonResponse({ data: { roomId: 77, trainingId: 66, roomStatus: 'WAITING' } }))
      .mockImplementationOnce(() => mockJsonResponse({ data: { roomId: 77, trainingId: 66, roomStatus: 'STARTED' } }));
    globalThis.fetch = fetchMock as typeof fetch;

    await expect(fetchTrainingAppInstallation()).resolves.toMatchObject({ installed: true });
    await expect(createTrainingRoom(66)).resolves.toMatchObject({ roomId: 77, roomCode: 'A100' });
    await expect(fetchTrainingRoom(77)).resolves.toMatchObject({ roomStatus: 'WAITING' });
    await expect(startTrainingRoom(77)).resolves.toMatchObject({ roomStatus: 'STARTED' });

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/student/trainings/app-installation', expect.any(Object));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/student/trainings/66/rooms', expect.objectContaining({ method: 'POST' }));
    expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/student/training-rooms/77', expect.any(Object));
    expect(fetchMock).toHaveBeenNthCalledWith(4, '/api/student/training-rooms/77/start', expect.objectContaining({ method: 'POST' }));
  });

  it('maps documented archive detail records for the profile archive detail view', async () => {
    const fetchMock = vi.fn(() =>
      mockJsonResponse({
        data: {
          archiveId: 5,
          trainingName: '自动扶梯伤客任务演练',
          trainingMode: '多人实训',
          roleName: '值班站长',
          studentName: '张林林',
          studentNo: 'A20260001',
          className: '城轨运营 2401 班',
          submittedAt: '2026-07-30T10:30:22',
          submitType: '正常提交',
          durationSeconds: 178,
          personalScore: 90,
          teamScore: 95,
          recordingUrl: 'https://example.test/video.mp4',
          steps: [
            {
              stepId: 1,
              stepName: '发现伤者并上报',
              standardOperation: '立即上报',
              actualOperation: '立即上报',
              score: 15,
              durationSeconds: 9,
              videoStartSecond: 12
            }
          ]
        }
      })
    );
    globalThis.fetch = fetchMock as typeof fetch;

    await expect(fetchStudentArchiveDetail(5)).resolves.toMatchObject({
      id: 5,
      title: '自动扶梯伤客任务演练',
      studentNo: 'A20260001',
      steps: [{ id: 1, expected: '立即上报', videoStartSecond: 12 }]
    });
    expect(fetchMock).toHaveBeenCalledWith('/api/student/archives/5', expect.any(Object));
  });

  it('uses documented profile and message mutation endpoints', async () => {
    const fetchMock = vi.fn(() => mockJsonResponse({ data: null }));
    globalThis.fetch = fetchMock as typeof fetch;

    await markStudentMessageRead(3);
    await markAllStudentMessagesRead();
    await updateStudentPhone('13800138000');
    await updateStudentIdCard('320100200001011234');
    await updateStudentPassword('old-password', 'new-password', 'new-password');

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/student/messages/3/read', expect.objectContaining({ method: 'POST' }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/student/messages/read-all', expect.objectContaining({ method: 'POST' }));
    expect(fetchMock).toHaveBeenNthCalledWith(
      3,
      '/api/student/profile/phone',
      expect.objectContaining({ method: 'PUT', body: JSON.stringify({ phone: '13800138000' }) })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      4,
      '/api/student/profile/id-card',
      expect.objectContaining({ method: 'PUT', body: JSON.stringify({ idCard: '320100200001011234' }) })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      5,
      '/api/student/profile/password',
      expect.objectContaining({
        method: 'PUT',
        body: JSON.stringify({ currentPassword: 'old-password', newPassword: 'new-password', confirmPassword: 'new-password' })
      })
    );
  });
});

describe('admin settings API integration', () => {
  it('uses documented education and facility config endpoints', async () => {
    const fetchMock = vi.fn(() => mockJsonResponse({ code: 0, data: null }));
    globalThis.fetch = fetchMock as typeof fetch;

    await setAdminCurrentSemester(12);
    await enableAdminClass(5);
    await disableAdminClass(5);
    await createAdminClassroom({
      roomName: 'Training Room A',
      cameras: [
        {
          nvrHost: '10.0.0.1',
          nvrPort: 554,
          adminUsername: 'admin',
          adminPassword: 'input-from-admin',
          nvrChannel: 'CH01',
          streamUrl: 'rtsp://10.0.0.1/live/ch01'
        }
      ]
    });

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/admin/semesters/12/current', expect.objectContaining({ method: 'POST' }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/admin/classes/5/enable', expect.objectContaining({ method: 'POST' }));
    expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/admin/classes/5/disable', expect.objectContaining({ method: 'POST' }));
    expect(fetchMock).toHaveBeenNthCalledWith(4, '/api/admin/classrooms', expect.objectContaining({ method: 'POST' }));
  });

  it('posts score weight history with the selected semester', async () => {
    const fetchMock = vi.fn(() => mockJsonResponse({ code: 0, data: 91 }));
    globalThis.fetch = fetchMock as typeof fetch;

    await createAdminScoreWeight({
      semesterId: 12,
      coursewareWeight: 30,
      trainingPracticeWeight: 30,
      assignmentWeight: 30,
      examWeight: 10
    });

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/admin/score-weights',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({
          semesterId: 12,
          coursewareWeight: 30,
          trainingPracticeWeight: 30,
          assignmentWeight: 30,
          examWeight: 10
        })
      })
    );
  });

  it('uses documented semester score list, statistics, export, and weight endpoints', async () => {
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() =>
        mockJsonResponse({
          data: {
            records: [{ scoreId: 1, studentName: '张明远', studentNo: 'S001', comprehensiveScore: 92.1 }],
            total: 1,
            page: 1,
            pageSize: 10
          }
        })
      )
      .mockImplementationOnce(() =>
        mockJsonResponse({
          data: {
            studentCount: 1,
            averageScore: 92.1,
            maxScore: 92.1,
            minScore: 92.1,
            excellentCount: 1,
            passCount: 1
          }
        })
      )
      .mockImplementationOnce(() => Promise.resolve(new Response('scoreId,studentNo\n1,S001', {
        status: 200,
        headers: { 'Content-Type': 'text/csv', 'Content-Disposition': 'attachment; filename="semester-scores.csv"' }
      })))
      .mockImplementationOnce(() => mockJsonResponse({ code: 0, data: 92 }));
    globalThis.fetch = fetchMock as typeof fetch;

    await expect(fetchAdminSemesterScores({ semesterId: 12, classId: 5, keyword: 'S001', page: 1, pageSize: 10 })).resolves.toMatchObject({
      records: [{ scoreId: 1, studentNo: 'S001' }],
      total: 1
    });
    await expect(fetchAdminSemesterScoreStatistics({ semesterId: 12, classId: 5, keyword: 'S001' })).resolves.toMatchObject({
      studentCount: 1,
      averageScore: 92.1
    });
    await exportAdminSemesterScores({ semesterId: 12, classId: 5, keyword: 'S001' });
    await createAdminScoreWeight({
      semesterId: 12,
      coursewareWeight: 25,
      trainingPracticeWeight: 35,
      assignmentWeight: 30,
      examWeight: 10
    });

    expect(fetchMock).toHaveBeenNthCalledWith(
      1,
      '/api/admin/scores/semester?semesterId=12&classId=5&keyword=S001&page=1&pageSize=10',
      expect.any(Object)
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      '/api/admin/scores/semester/statistics?semesterId=12&classId=5&keyword=S001',
      expect.any(Object)
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      3,
      '/api/admin/scores/semester/export/file?semesterId=12&classId=5&keyword=S001',
      expect.any(Object)
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      4,
      '/api/admin/score-weights',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({
          semesterId: 12,
          coursewareWeight: 25,
          trainingPracticeWeight: 35,
          assignmentWeight: 30,
          examWeight: 10
        })
      })
    );
  });
});
