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
  classroomId?: number;
  teacherIds?: number[];
  scoreBasis?: 'HIGHEST' | 'LAST_SUBMIT';
  topicIds?: number[];
  teacherNames?: string;
  classroomName?: string;
  topicCount?: number;
  examStartedAt?: string;
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
  classroomId?: number;
  teacherIds?: number[];
  scoreBasis?: 'HIGHEST' | 'LAST_SUBMIT';
  topicIds?: number[];
  classIds?: number[];
  roles?: AdminTrainingRoleCommand[];
  publishStatus?: string;
}

export interface AdminTrainingQuery {
  keyword?: string;
  trainingType?: string;
  trainingMode?: string;
  publishStatus?: string;
  rangeStart?: string;
  rangeEnd?: string;
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

export interface AdminTrainingWeakStep {
  topicName?: string;
  stepName?: string;
  errorCount?: number;
  totalCount?: number;
  errorRate?: number;
}

export interface AdminTrainingCameraState {
  cameraId?: number;
  classroomName?: string;
  cameraName?: string;
  nvrChannel?: string;
  streamUrl?: string;
  cameraStatus?: string;
}

export interface AdminTrainingStudentState {
  studentId?: number;
  studentName?: string;
  studentNo?: string;
  className?: string;
  clientIp?: string;
  deskStatus?: string;
  progressStatus?: string;
  currentTopicName?: string;
  trainingMode?: string;
  submittedTopicCount?: number;
  totalTopicCount?: number;
  score?: number;
  teamScore?: number;
  roomId?: number;
  roomCode?: string;
  roomStatus?: string;
  roleName?: string;
  teammateNames?: string;
  desktopStreamUrl?: string;
}

export interface AdminTrainingMonitorSnapshot {
  trainingId?: number;
  generatedAt?: string;
  cameras?: AdminTrainingCameraState[];
  students?: AdminTrainingStudentState[];
  statistics?: AdminTrainingStatistics;
}

export interface AdminTrainingTopic {
  topicId: number;
  topicName: string;
  category?: string;
  trainingMode?: 'SINGLE' | 'TEAM';
  durationMinutes?: number;
  score?: number;
  roleNames?: string;
}

export interface AdminTrainingReviewRow {
  studentId: number;
  studentName?: string;
  studentNo?: string;
  classId?: number;
  className?: string;
  topicId: number;
  topicName?: string;
  trainingMode?: string;
  attemptId?: number;
  submittedAt?: string;
  systemScore?: number;
  manualScore?: number;
  teamScore?: number;
  reviewComment?: string;
  reviewedAt?: string;
  roleName?: string;
  durationSeconds?: number;
  submitCount?: number;
  teammateScores?: string;
}

export interface AdminTrainingReviewAttempt {
  attemptId: number;
  submittedAt?: string;
  systemScore?: number;
  manualScore?: number;
  teamScore?: number;
  reviewComment?: string;
  reviewedAt?: string;
  roleName?: string;
  durationSeconds?: number;
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

export function startAdminTrainingExam(trainingId: number) {
  return requestJson<void>(`/admin/trainings/${trainingId}/start-exam`, {
    method: 'POST',
    fallbackLabel: '开始实训考试'
  });
}

export function deleteAdminTraining(trainingId: number) {
  return requestJson<void>(`/admin/trainings/${trainingId}/delete`, { method: 'POST', fallbackLabel: '删除实训课' });
}

export function fetchAdminTrainingMonitor(trainingId: number) {
  return requestJson<AdminTrainingMonitorSnapshot>(`/admin/trainings/${trainingId}/monitor`, { fallbackLabel: '实训监控' });
}

export function dissolveAdminTrainingRoom(trainingId: number, roomId: number) {
  return requestJson<void>(`/admin/trainings/${trainingId}/rooms/${roomId}/dissolve`, {
    method: 'POST',
    fallbackLabel: '解散实训房间'
  });
}

export function fetchAdminTrainingStatistics(trainingId: number) {
  return requestJson<AdminTrainingStatistics>(`/admin/trainings/${trainingId}/statistics`, { fallbackLabel: '实训成绩统计' });
}

export function fetchAdminTrainingLogs(trainingId: number) {
  return requestJson<AdminTrainingLog[]>(`/admin/trainings/${trainingId}/logs`, { fallbackLabel: '实训操作日志' });
}

export function fetchAdminTrainingTopics() {
  return requestJson<AdminTrainingTopic[]>('/admin/training-topics', { fallbackLabel: '实训题库' });
}

export function fetchAdminTrainingReviews(trainingId: number) {
  return requestJson<AdminTrainingReviewRow[]>(`/admin/trainings/${trainingId}/reviews`, { fallbackLabel: '实训阅卷列表' });
}

export function fetchAdminTrainingWeakSteps(trainingId: number, className?: string) {
  const query = className ? `?className=${encodeURIComponent(className)}` : '';
  return requestJson<AdminTrainingWeakStep[]>(`/admin/trainings/${trainingId}/statistics/weak-steps${query}`, {
    fallbackLabel: '实训薄弱环节'
  });
}

export function fetchAdminTrainingReviewAttempts(trainingId: number, studentId: number, topicId: number) {
  return requestJson<AdminTrainingReviewAttempt[]>(`/admin/trainings/${trainingId}/reviews/${studentId}/${topicId}/attempts`, {
    fallbackLabel: '实训提交记录'
  });
}

export function reviewAdminTrainingAttempt(trainingId: number, attemptId: number, command: { manualScore: number; comment?: string }) {
  return requestJson<void>(`/admin/trainings/${trainingId}/attempts/${attemptId}/review`, {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: '保存实训批阅'
  });
}
