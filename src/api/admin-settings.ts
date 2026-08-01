import { requestJson } from './http';

export interface AdminSemester {
  semesterId: number;
  academicYearId: number;
  semesterName: string;
  current: boolean;
}

export interface AdminAcademicYear {
  academicYearId: number;
  yearName: string;
  semesters: AdminSemester[];
}

export interface AdminMajor {
  majorId: number;
  majorName: string;
  enabled: boolean;
}

export interface AdminClass {
  classId: number;
  majorId: number;
  majorName?: string;
  className: string;
  enabled: boolean;
}

export interface AdminClassroom {
  classroomId: number;
  roomName: string;
  cameraCount: number;
  createdAt?: string;
}

export interface AdminJobRole {
  jobRoleId: number;
  roleName: string;
  sortOrder?: number;
  enabled: boolean;
}

export interface AdminScoreWeight {
  weightId: number;
  semesterId?: number;
  coursewareWeight: number;
  trainingPracticeWeight: number;
  assignmentWeight: number;
  examWeight: number;
  effectiveFrom?: string;
  createdAt?: string;
}

export interface AdminScoreGradeRule {
  ruleId: number;
  gradeName: string;
  minScore: number;
  maxScore: number;
  sortOrder: number;
}

export function fetchAdminAcademicYears() {
  return requestJson<AdminAcademicYear[]>('/admin/academic-years', { fallbackLabel: '学年学期配置' });
}

export function fetchAdminMajors() {
  return requestJson<AdminMajor[]>('/admin/majors', { fallbackLabel: '专业目录配置' });
}

export function fetchAdminClasses() {
  return requestJson<AdminClass[]>('/admin/classes', { fallbackLabel: '班级配置' });
}

export function fetchAdminClassrooms() {
  return requestJson<AdminClassroom[]>('/admin/classrooms', { fallbackLabel: '教室配置' });
}

export function fetchAdminJobRoles() {
  return requestJson<AdminJobRole[]>('/admin/job-roles', { fallbackLabel: '岗位角色配置' });
}

export function fetchAdminScoreWeights() {
  return requestJson<AdminScoreWeight[]>('/admin/score-weights', { fallbackLabel: '综合成绩权重配置' });
}

export function fetchAdminScoreGradeRules() {
  return requestJson<AdminScoreGradeRule[]>('/admin/score-grade-rules', { fallbackLabel: '成绩等级配置' });
}
