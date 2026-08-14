import { requestJson } from './http';

export interface PageResponse<T> {
  records?: T[];
  rows?: T[];
  list?: T[];
  data?: T[];
  total?: number;
  page?: number;
  pageSize?: number;
}

export interface AdminTrainingArchive {
  archiveId: number;
  trainingId?: number;
  studentId?: number;
  trainingName?: string;
  trainingMode?: string;
  studentName?: string;
  studentNo?: string;
  className?: string;
  roleName?: string;
  submittedAt?: string;
  submitType?: string;
  durationSeconds?: number;
  personalScore?: number | string;
  teamScore?: number | string;
}

export interface AdminTrainingArchiveStep {
  stepId?: number;
  stepName?: string;
  standardOperation?: string;
  actualOperation?: string;
  score?: number;
  maxScore?: number;
  durationSeconds?: number;
  videoStartSecond?: number;
  videoEndSecond?: number;
}

export interface AdminTrainingArchiveDetail extends AdminTrainingArchive {
  recordingUrl?: string;
  steps?: AdminTrainingArchiveStep[];
}

export interface AdminTrainingArchiveQuery {
  studentId?: number;
  classId?: number;
  trainingId?: number;
  trainingMode?: string;
  submitType?: string;
  keyword?: string;
  page?: number;
  pageSize?: number;
}

function buildQuery(query: AdminTrainingArchiveQuery = {}) {
  const search = new URLSearchParams();
  Object.entries(query).forEach(([key, value]) => {
    if (value !== undefined && value !== '') {
      search.set(key, String(value));
    }
  });
  const text = search.toString();
  return text ? `?${text}` : '';
}

function normalizePage<T>(payload: PageResponse<T> | T[]): { records: T[]; total: number; page: number; pageSize: number } {
  if (Array.isArray(payload)) {
    return { records: payload, total: payload.length, page: 1, pageSize: payload.length || 20 };
  }
  const records = payload.records || payload.rows || payload.list || payload.data || [];
  return {
    records,
    total: payload.total ?? records.length,
    page: payload.page ?? 1,
    pageSize: payload.pageSize ?? 20
  };
}

export async function fetchAdminTrainingArchives(query: AdminTrainingArchiveQuery = {}) {
  const result = await requestJson<PageResponse<AdminTrainingArchive> | AdminTrainingArchive[]>(`/admin/archives${buildQuery(query)}`, {
    fallbackLabel: '实训档案'
  });
  return normalizePage(result);
}

export function fetchAdminTrainingArchiveDetail(archiveId: number) {
  return requestJson<AdminTrainingArchiveDetail>(`/admin/archives/${archiveId}`, {
    fallbackLabel: '实训档案详情'
  });
}
