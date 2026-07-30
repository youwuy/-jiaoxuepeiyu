import { afterEach, describe, expect, it, vi } from 'vitest';
import { requestJson, tryRequestJson } from '../src/api/http';
import { fetchStudentCourses } from '../src/api/student';

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

  it('normalizes paged student course responses', async () => {
    const fetchMock = vi.fn(() =>
      mockJsonResponse({
        data: {
          records: [
            {
              id: 1,
              name: '城市轨道交通概论',
              term: '2026 下学期',
              startAt: '2026-07-01',
              endAt: '2026-08-31',
              completedItems: 1,
              totalItems: 2,
              resourceCount: 3,
              assignmentCount: 4,
              teachers: ['王老师'],
              mode: 'sequence',
              chapters: []
            }
          ]
        }
      })
    );
    globalThis.fetch = fetchMock as typeof fetch;

    await expect(fetchStudentCourses()).resolves.toMatchObject([
      {
        id: 1,
        name: '城市轨道交通概论'
      }
    ]);
  });
});
