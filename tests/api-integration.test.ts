import { afterEach, describe, expect, it, vi } from 'vitest';
import { loginAdmin } from '../src/api/auth';
import {
  cancelPublishAdminCourse,
  fetchAdminCourseLogs,
  fetchAdminCourseStatistics,
  fetchAdminCourses,
  publishAdminCourse
} from '../src/api/admin-course';
import { createAdminOrg, disableAdminOrg, enableAdminOrg, fetchAdminOrgTree, updateAdminOrg } from '../src/api/admin-org';
import { requestJson, tryRequestJson } from '../src/api/http';
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
  vi.restoreAllMocks();
});

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
