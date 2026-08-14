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

const semesterNameMap: Record<string, string> = {
  FIRST: '上学期',
  SECOND: '下学期',
  '第一学期': '上学期',
  '第二学期': '下学期'
};

export function normalizeSemesterName(semesterName: string): string {
  const normalized = semesterName.trim();
  return semesterNameMap[normalized.toUpperCase()] ?? normalized;
}

export function normalizeAdminAcademicYears(years: AdminAcademicYear[]): AdminAcademicYear[] {
  return years.map((year) => {
    const semestersByName = new Map<string, AdminSemester>();

    for (const semester of year.semesters ?? []) {
      const semesterName = normalizeSemesterName(semester.semesterName);
      const normalizedSemester = { ...semester, semesterName };
      const existing = semestersByName.get(semesterName);

      if (!existing || (!existing.current && normalizedSemester.current)) {
        semestersByName.set(semesterName, normalizedSemester);
      }
    }

    const semesterOrder: Record<string, number> = { '上学期': 0, '下学期': 1 };
    const semesters = [...semestersByName.values()].sort((left, right) =>
      (semesterOrder[left.semesterName] ?? 99) - (semesterOrder[right.semesterName] ?? 99)
    );

    return { ...year, semesters };
  });
}

export interface AdminMajor {
  majorId: number;
  majorName: string;
  enabled: boolean;
}

export interface AdminClass {
  classId: number;
  majorId?: number;
  majorName?: string;
  className: string;
  enabled: boolean;
}

export interface AdminCamera {
  cameraId: number;
  classroomId: number;
  nvrHost: string;
  nvrPort: number;
  adminUsername: string;
  adminPassword: string;
  nvrChannel: string;
  streamUrl?: string;
  sortOrder?: number;
}

export interface AdminClassroom {
  classroomId: number;
  roomName: string;
  fixedDeviceCount: number;
  cameraCount: number;
  createdAt?: string;
  cameras?: AdminCamera[];
}

export interface AdminScoreWeight {
  weightId: number;
  semesterId?: number;
  coursewareWeight: number;
  trainingPracticeWeight: number;
  assignmentWeight: number;
  examWeight: number;
  effectiveFrom?: string;
  operatorName?: string;
  createdAt?: string;
}

export interface AdminScoreGradeRule {
  ruleId: number;
  gradeName: string;
  minScore: number;
  maxScore: number;
  sortOrder: number;
}

export interface AdminScoreGradeRuleLog {
  logId: number;
  beforeContent: string;
  afterContent: string;
  operatorId: number;
  operatorName?: string;
  createdAt: string;
}

export interface AdminAcademicYearCommand {
  yearName: string;
}

export interface AdminMajorCommand {
  majorName: string;
}

export interface AdminClassCommand {
  majorId?: number;
  className: string;
}

export interface AdminCameraCommand {
  nvrHost: string;
  nvrPort: number;
  adminUsername: string;
  adminPassword: string;
  nvrChannel: string;
  streamUrl?: string;
}

export interface AdminClassroomCommand {
  roomName: string;
  fixedDeviceCount: number;
  cameras: AdminCameraCommand[];
}

export interface AdminScoreWeightCommand {
  semesterId: number;
  coursewareWeight: number;
  trainingPracticeWeight: number;
  assignmentWeight: number;
  examWeight: number;
}

export interface AdminScoreGradeRuleCommand {
  gradeName: string;
  minScore: number;
  maxScore: number;
}

export async function fetchAdminAcademicYears() {
  const years = await requestJson<AdminAcademicYear[]>('/admin/academic-years', { fallbackLabel: '学年学期配置' });
  return normalizeAdminAcademicYears(years);
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

export function fetchAdminScoreWeights() {
  return requestJson<AdminScoreWeight[]>('/admin/score-weights', { fallbackLabel: '综合成绩权重配置' });
}

export function fetchAdminScoreGradeRules() {
  return requestJson<AdminScoreGradeRule[]>('/admin/score-grade-rules', { fallbackLabel: '成绩等级配置' });
}

export function fetchAdminScoreGradeRuleLogs() {
  return requestJson<AdminScoreGradeRuleLog[]>('/admin/score-grade-rules/logs', { fallbackLabel: '成绩等级操作日志' });
}

export function createAdminAcademicYear(command: AdminAcademicYearCommand) {
  return requestJson<number>('/admin/academic-years', {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: '添加学年'
  });
}

export function setAdminCurrentSemester(semesterId: number) {
  return requestJson<void>(`/admin/semesters/${semesterId}/current`, {
    method: 'POST',
    fallbackLabel: '设置当前学期'
  });
}

export function createAdminMajor(command: AdminMajorCommand) {
  return requestJson<number>('/admin/majors', {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: '添加专业'
  });
}

export function enableAdminMajor(majorId: number) {
  return requestJson<void>(`/admin/majors/${majorId}/enable`, {
    method: 'POST',
    fallbackLabel: '启用专业'
  });
}

export function disableAdminMajor(majorId: number) {
  return requestJson<void>(`/admin/majors/${majorId}/disable`, {
    method: 'POST',
    fallbackLabel: '禁用专业'
  });
}

export function createAdminClass(command: AdminClassCommand) {
  return requestJson<number>('/admin/classes', {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: '新增班级'
  });
}

export function enableAdminClass(classId: number) {
  return requestJson<void>(`/admin/classes/${classId}/enable`, {
    method: 'POST',
    fallbackLabel: '启用班级'
  });
}

export function disableAdminClass(classId: number) {
  return requestJson<void>(`/admin/classes/${classId}/disable`, {
    method: 'POST',
    fallbackLabel: '禁用班级'
  });
}

export function createAdminClassroom(command: AdminClassroomCommand) {
  return requestJson<number>('/admin/classrooms', {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: '添加教室'
  });
}

export function updateAdminClassroom(classroomId: number, command: AdminClassroomCommand) {
  return requestJson<void>(`/admin/classrooms/${classroomId}`, {
    method: 'PUT',
    body: JSON.stringify(command),
    fallbackLabel: '编辑教室'
  });
}

export function deleteAdminClassroom(classroomId: number) {
  return requestJson<void>(`/admin/classrooms/${classroomId}`, {
    method: 'DELETE',
    fallbackLabel: '删除教室'
  });
}

export function createAdminScoreWeight(command: AdminScoreWeightCommand) {
  return requestJson<number>('/admin/score-weights', {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: '保存综合成绩权重'
  });
}

export function replaceAdminScoreGradeRules(command: AdminScoreGradeRuleCommand[]) {
  return requestJson<void>('/admin/score-grade-rules', {
    method: 'PUT',
    body: JSON.stringify(command),
    fallbackLabel: '保存成绩等级配置'
  });
}
