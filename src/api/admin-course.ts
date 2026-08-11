import { requestBlob, requestJson } from './http';
import type {
  AdminCourseChapter,
  AdminCourseContent,
  AdminCoursePublishStatus,
  AdminCourseRecord
} from '../features/admin/courses';

export interface AdminCourseQuery {
  keyword?: string;
  academicYearId?: number;
  semesterId?: number;
  majorId?: number;
  classId?: number;
  teacherId?: number;
  teachingStartTime?: string;
  teachingEndTime?: string;
  publishStatus?: string;
  page?: number;
  pageSize?: number;
}

export interface AdminCoursePage {
  records?: unknown[];
  rows?: unknown[];
  list?: unknown[];
  data?: unknown[];
  total?: number;
  page?: number;
  pageSize?: number;
}

export interface AdminCourseStatistics {
  courseId: number;
  studentCount: number;
  completedCount: number;
  studyingCount: number;
  notStartedCount: number;
  pendingReviewCount: number;
  averageScore: number;
}

export interface AdminCourseStudentStatisticsQuery {
  studentName?: string;
  studentNo?: string;
  className?: string;
  page?: number;
  pageSize?: number;
}

export interface AdminCourseStudentStatistics {
  studentId: number;
  studentName: string;
  studentNo: string;
  classId?: number;
  className?: string;
  progressPercent?: number;
  progressScore?: number;
  assignmentCount?: number;
  assignmentScore?: number;
}

export interface AdminCourseLog {
  logId: number;
  courseId: number;
  operatorId?: number;
  operatorName: string;
  action: string;
  content: string;
  createdAt: string;
}

export interface AdminCourseCommand {
  courseName?: string;
  academicYearId?: number;
  semesterId?: number;
  majorId?: number;
  coverUrl?: string;
  openStartTime?: string;
  openEndTime?: string;
  teacherIds?: number[];
  classIds?: number[];
  learningMode?: string;
  assignmentCompletionRule?: string;
  coursewareScoreCap?: number;
  publishStatus?: AdminCoursePublishStatus;
  chapters?: Array<{
    chapterTitle?: string;
    sortOrder?: number;
    contents?: Array<{
      itemType?: string;
      title?: string;
      resourceId?: number;
      assignmentId?: number;
      questionIds?: number[];
      requiredDurationSeconds?: number;
      learningStartTime?: string;
      learningEndTime?: string;
      assignmentCompletionRule?: string;
      passScore?: number;
      assignmentPublishMode?: string;
      answerStartTime?: string;
      answerEndTime?: string;
      assignmentTotalScore?: number;
      sortOrder?: number;
    }>;
    children?: AdminCourseCommand['chapters'];
  }>;
}

export interface AdminSemesterOption {
  semesterId: number;
  academicYearId: number;
  semesterName: string;
  current?: boolean;
}

export interface AdminAcademicYearOption {
  academicYearId: number;
  yearName: string;
  semesters?: AdminSemesterOption[];
}

export interface AdminMajorOption {
  majorId: number;
  majorName: string;
  enabled?: boolean;
}

export interface AdminClassOption {
  classId: number;
  majorId: number;
  majorName?: string;
  className: string;
  enabled?: boolean;
}

export interface AdminTeacherOption {
  userId: number;
  accountNo?: string;
  realName: string;
  enabled?: boolean;
}

export interface AdminAssignmentAttemptQuery {
  courseId?: number;
  assignmentId?: number;
  classId?: number;
  studentId?: number;
  status?: string;
  keyword?: string;
  page?: number;
  pageSize?: number;
}

export interface AdminAssignmentAttemptAnswer {
  questionId: number;
  questionType?: string;
  title?: string;
  standardAnswer?: string;
  answerContent?: string;
  questionScore?: number;
  score?: number;
  reviewComment?: string;
}

export interface AdminAssignmentAttempt {
  attemptId: number;
  assignmentId?: number;
  assignmentTitle?: string;
  assignmentType?: string;
  courseId?: number;
  courseName?: string;
  studentId?: number;
  studentName?: string;
  studentNo?: string;
  classId?: number;
  className?: string;
  totalScore?: number;
  status?: string;
  score?: number;
  reviewComment?: string;
  reviewerId?: number;
  reviewerName?: string;
  submittedAt?: string;
  reviewedAt?: string;
  answers?: AdminAssignmentAttemptAnswer[];
}

export interface AdminAssignmentReviewCommand {
  reviewComment?: string;
  answers: Array<{
    questionId: number;
    score: number;
    comment?: string;
  }>;
}

function buildQuery(params: object): string {
  const search = new URLSearchParams();
  Object.entries(params as Record<string, string | number | boolean | undefined>).forEach(([key, value]) => {
    if (value !== undefined && value !== '') {
      search.set(key, String(value));
    }
  });

  const query = search.toString();
  return query ? `?${query}` : '';
}

function normalizeListResponse<T>(value: AdminCoursePage | T[]): T[] {
  if (Array.isArray(value)) {
    return value;
  }

  return (value.records || value.rows || value.list || value.data || []) as T[];
}

export async function fetchAdminCourses(query: AdminCourseQuery = {}) {
  const result = await requestJson<AdminCoursePage>(`/admin/courses${buildQuery(query)}`, {
    fallbackLabel: '教学课程列表'
  });

  return {
    records: normalizeListResponse<AdminCourseRecord>(result),
    total: result.total ?? normalizeListResponse<AdminCourseRecord>(result).length,
    page: result.page ?? query.page ?? 1,
    pageSize: result.pageSize ?? query.pageSize ?? 20
  };
}

export async function fetchAdminCourseDetail(courseId: number) {
  return requestJson<AdminCourseRecord>(`/admin/courses/${courseId}`, {
    fallbackLabel: '课程详情'
  });
}

export async function createAdminCourse(command: AdminCourseCommand) {
  const result = await requestJson<number | { courseId: number }>('/admin/courses', {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: '新增课程'
  });

  return { courseId: typeof result === 'number' ? result : result.courseId };
}

export async function updateAdminCourse(courseId: number, command: AdminCourseCommand) {
  return requestJson<void>(`/admin/courses/${courseId}`, {
    method: 'PUT',
    body: JSON.stringify(command),
    fallbackLabel: '更新课程'
  });
}

export async function publishAdminCourse(courseId: number) {
  return requestJson<void>(`/admin/courses/${courseId}/publish`, {
    method: 'POST',
    fallbackLabel: '发布课程'
  });
}

export async function cancelPublishAdminCourse(courseId: number) {
  return requestJson<void>(`/admin/courses/${courseId}/cancel-publish`, {
    method: 'POST',
    fallbackLabel: '取消发布'
  });
}

export async function deleteAdminCourse(courseId: number) {
  return requestJson<void>(`/admin/courses/${courseId}/delete`, {
    method: 'POST',
    fallbackLabel: '删除课程'
  });
}

export async function copyAdminCourse(courseId: number) {
  const result = await requestJson<number | { courseId: number }>(`/admin/courses/${courseId}/copy`, {
    method: 'POST',
    fallbackLabel: '复制课程'
  });

  return { courseId: typeof result === 'number' ? result : result.courseId };
}

export async function fetchAdminCourseStatistics(courseId: number) {
  return requestJson<AdminCourseStatistics>(`/admin/courses/${courseId}/statistics`, {
    fallbackLabel: '课程统计'
  });
}

export async function fetchAdminCourseStudentStatistics(courseId: number, query: AdminCourseStudentStatisticsQuery = {}) {
  const result = await requestJson<AdminCoursePage>(`/admin/courses/${courseId}/student-statistics${buildQuery(query)}`, {
    fallbackLabel: '课程学员成绩'
  });

  return {
    records: normalizeListResponse<AdminCourseStudentStatistics>(result),
    total: result.total ?? normalizeListResponse<AdminCourseStudentStatistics>(result).length,
    page: result.page ?? query.page ?? 1,
    pageSize: result.pageSize ?? query.pageSize ?? 20
  };
}

export async function exportAdminCourseStudentStatistics(courseId: number, query: AdminCourseStudentStatisticsQuery = {}) {
  const result = await requestBlob(`/admin/courses/${courseId}/student-statistics/export/file${buildQuery(query)}`, {
    fallbackLabel: '导出课程学员成绩'
  });
  downloadBlob(result.blob, result.filename || 'course-student-statistics.csv');
}

export async function fetchAdminCourseLogs(courseId: number) {
  return requestJson<AdminCourseLog[]>(`/admin/courses/${courseId}/logs`, {
    fallbackLabel: '课程日志'
  });
}

function downloadBlob(blob: Blob, filename: string) {
  if (!globalThis.document || !globalThis.URL) {
    return;
  }

  const url = globalThis.URL.createObjectURL(blob);
  const link = globalThis.document.createElement('a');
  link.href = url;
  link.download = filename;
  globalThis.document.body.appendChild(link);
  link.click();
  globalThis.document.body.removeChild(link);
  globalThis.URL.revokeObjectURL(url);
}

export async function fetchAdminAcademicYears() {
  return requestJson<AdminAcademicYearOption[]>('/admin/academic-years', {
    fallbackLabel: '学年学期'
  });
}

export async function fetchAdminMajors() {
  return requestJson<AdminMajorOption[]>('/admin/majors', {
    fallbackLabel: '专业列表'
  });
}

export async function fetchAdminClasses(majorId?: number) {
  return requestJson<AdminClassOption[]>(`/admin/classes${buildQuery({ majorId })}`, {
    fallbackLabel: '班级列表'
  });
}

export async function fetchAdminTeachers() {
  const result = await requestJson<AdminCoursePage & { records?: AdminTeacherOption[] }>('/admin/accounts/teachers?page=1&pageSize=200', {
    fallbackLabel: '教师列表'
  });

  return normalizeListResponse<AdminTeacherOption>(result);
}

export async function fetchAdminAssignmentAttempts(query: AdminAssignmentAttemptQuery = {}) {
  const result = await requestJson<AdminCoursePage>(`/admin/assignment-attempts${buildQuery(query)}`, {
    fallbackLabel: '作业批阅列表'
  });

  return {
    records: normalizeListResponse<AdminAssignmentAttempt>(result),
    total: result.total ?? normalizeListResponse<AdminAssignmentAttempt>(result).length,
    page: result.page ?? query.page ?? 1,
    pageSize: result.pageSize ?? query.pageSize ?? 20
  };
}

export async function fetchAdminAssignmentAttemptDetail(attemptId: number) {
  return requestJson<AdminAssignmentAttempt>(`/admin/assignment-attempts/${attemptId}`, {
    fallbackLabel: '作业批阅详情'
  });
}

export async function reviewAdminAssignmentAttempt(attemptId: number, command: AdminAssignmentReviewCommand) {
  return requestJson<void>(`/admin/assignment-attempts/${attemptId}/review`, {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: '保存阅卷结果'
  });
}

export type { AdminCourseChapter, AdminCourseContent, AdminCoursePublishStatus, AdminCourseRecord };
