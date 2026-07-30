import { afterEach, describe, expect, it, vi } from 'vitest';
import { requestJson, tryRequestJson } from '../src/api/http';
import { fetchStudentCourses, fetchStudentProfile, fetchStudentResources } from '../src/api/student';

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

    await expect(fetchStudentResources()).resolves.toMatchObject([
      {
        id: 9,
        title: '实训操作规范手册',
        type: 'PDF',
        size: '1.0 MB'
      }
    ]);
    expect(fetchMock).toHaveBeenCalledWith('/api/student/resources/public', expect.any(Object));
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
});
