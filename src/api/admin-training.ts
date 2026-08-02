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

export interface AdminTrainingRoleCommand {
  roleName?: string;
  roleCode?: string;
  capacity?: number;
  aiFillEnabled?: boolean;
  sortOrder?: number;
}

export interface AdminTraining {
  trainingId: number;
  trainingName?: string;
  academicYearId?: number;
  academicYearName?: string;
  semesterId?: number;
  semesterName?: string;
  majorId?: number;
  majorName?: string;
  coverUrl?: string;
  trainingType?: string;
  trainingMode?: string;
  paperMode?: string;
  paperId?: number;
  paperName?: string;
  publishStatus?: string;
  openStartTime?: string;
  openEndTime?: string;
  teamSize?: number;
  appRequired?: boolean;
  classNames?: string;
  classIds?: number[];
  roles?: AdminTrainingRoleCommand[];
  participantCount?: number;
  roomCount?: number;
  startedRoomCount?: number;
  averageScore?: number;
  creatorName?: string;
  createdAt?: string;
}

export interface AdminTrainingCommand {
  trainingName?: string;
  academicYearId?: number;
  semesterId?: number;
  majorId?: number;
  coverUrl?: string;
  trainingType?: string;
  trainingMode?: string;
  paperMode?: string;
  paperId?: number;
  openStartTime?: string;
  openEndTime?: string;
  teamSize?: number;
  appRequired?: boolean;
  classIds?: number[];
  roles?: AdminTrainingRoleCommand[];
  publishStatus?: string;
}

export interface AdminTrainingQuery {
  keyword?: string;
  trainingType?: string;
  trainingMode?: string;
  publishStatus?: string;
  page?: number;
  pageSize?: number;
}

export interface AdminTrainingLog {
  logId?: number;
  trainingId?: number;
  operatorName?: string;
  action?: string;
  content?: string;
  createdAt?: string;
}

export interface AdminTrainingStatistics {
  trainingId?: number;
  participantCount?: number;
  waitingRoomCount?: number;
  startedRoomCount?: number;
  dissolvedRoomCount?: number;
  submittedAttemptCount?: number;
  averageScore?: number;
  maxScore?: number;
  minScore?: number;
}

export interface AdminTrainingCameraState {
  cameraId?: number;
  classroomName?: string;
  cameraName?: string;
  streamUrl?: string;
  cameraStatus?: string;
}

export interface AdminTrainingStudentState {
  studentId?: number;
  studentName?: string;
  studentNo?: string;
  className?: string;
  deskStatus?: string;
  progressStatus?: string;
  score?: number;
  roomId?: number;
  roomStatus?: string;
  roleName?: string;
}

export interface AdminTrainingMonitorSnapshot {
  trainingId?: number;
  generatedAt?: string;
  cameras?: AdminTrainingCameraState[];
  students?: AdminTrainingStudentState[];
  statistics?: AdminTrainingStatistics;
}

function buildQuery(query: object = {}) {
  const search = new URLSearchParams();
  Object.entries(query as Record<string, string | number | boolean | undefined>).forEach(([key, value]) => {
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

export async function fetchAdminTrainings(query: AdminTrainingQuery = {}) {
  const result = await requestJson<PageResponse<AdminTraining> | AdminTraining[]>(`/admin/trainings${buildQuery(query)}`, {
    fallbackLabel: '实训组课列表'
  });
  return normalizePage(result);
}

export function fetchAdminTraining(trainingId: number) {
  return requestJson<AdminTraining>(`/admin/trainings/${trainingId}`, { fallbackLabel: '实训课详情' });
}

export async function createAdminTraining(command: AdminTrainingCommand) {
  const result = await requestJson<number | { trainingId: number }>('/admin/trainings', {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: '新增实训课'
  });
  return { trainingId: typeof result === 'number' ? result : result.trainingId };
}

export function updateAdminTraining(trainingId: number, command: AdminTrainingCommand) {
  return requestJson<void>(`/admin/trainings/${trainingId}`, {
    method: 'PUT',
    body: JSON.stringify(command),
    fallbackLabel: '编辑实训课'
  });
}

export function publishAdminTraining(trainingId: number) {
  return requestJson<void>(`/admin/trainings/${trainingId}/publish`, { method: 'POST', fallbackLabel: '发布实训课' });
}

export function cancelPublishAdminTraining(trainingId: number) {
  return requestJson<void>(`/admin/trainings/${trainingId}/cancel-publish`, { method: 'POST', fallbackLabel: '撤回实训课' });
}

export function deleteAdminTraining(trainingId: number) {
  return requestJson<void>(`/admin/trainings/${trainingId}/delete`, { method: 'POST', fallbackLabel: '删除实训课' });
}

export function fetchAdminTrainingMonitor(trainingId: number) {
  return requestJson<AdminTrainingMonitorSnapshot>(`/admin/trainings/${trainingId}/monitor`, { fallbackLabel: '实训监控' });
}

export function fetchAdminTrainingStatistics(trainingId: number) {
  return requestJson<AdminTrainingStatistics>(`/admin/trainings/${trainingId}/statistics`, { fallbackLabel: '实训成绩统计' });
}

export function fetchAdminTrainingLogs(trainingId: number) {
  return requestJson<AdminTrainingLog[]>(`/admin/trainings/${trainingId}/logs`, { fallbackLabel: '实训操作日志' });
}
