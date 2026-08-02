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
  cameraCount: number;
  createdAt?: string;
  cameras?: AdminCamera[];
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

export interface AdminAcademicYearCommand {
  yearName: string;
}

export interface AdminMajorCommand {
  majorName: string;
}

export interface AdminClassCommand {
  majorId: number;
  className: string;
}

export interface AdminJobRoleCommand {
  roleName: string;
  sortOrder?: number;
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

export function fetchAdminAcademicYears() {
  return requestJson<AdminAcademicYear[]>('/admin/academic-years', { fallbackLabel: 'academic years' });
}

export function fetchAdminMajors() {
  return requestJson<AdminMajor[]>('/admin/majors', { fallbackLabel: 'majors' });
}

export function fetchAdminClasses() {
  return requestJson<AdminClass[]>('/admin/classes', { fallbackLabel: 'classes' });
}

export function fetchAdminClassrooms() {
  return requestJson<AdminClassroom[]>('/admin/classrooms', { fallbackLabel: 'classrooms' });
}

export function fetchAdminJobRoles() {
  return requestJson<AdminJobRole[]>('/admin/job-roles', { fallbackLabel: 'job roles' });
}

export function fetchAdminScoreWeights() {
  return requestJson<AdminScoreWeight[]>('/admin/score-weights', { fallbackLabel: 'score weights' });
}

export function fetchAdminScoreGradeRules() {
  return requestJson<AdminScoreGradeRule[]>('/admin/score-grade-rules', { fallbackLabel: 'score grade rules' });
}

export function createAdminAcademicYear(command: AdminAcademicYearCommand) {
  return requestJson<number>('/admin/academic-years', {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: 'add academic year'
  });
}

export function setAdminCurrentSemester(semesterId: number) {
  return requestJson<void>(`/admin/semesters/${semesterId}/current`, {
    method: 'POST',
    fallbackLabel: 'set current semester'
  });
}

export function createAdminMajor(command: AdminMajorCommand) {
  return requestJson<number>('/admin/majors', {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: 'add major'
  });
}

export function enableAdminMajor(majorId: number) {
  return requestJson<void>(`/admin/majors/${majorId}/enable`, {
    method: 'POST',
    fallbackLabel: 'enable major'
  });
}

export function disableAdminMajor(majorId: number) {
  return requestJson<void>(`/admin/majors/${majorId}/disable`, {
    method: 'POST',
    fallbackLabel: 'disable major'
  });
}

export function createAdminClass(command: AdminClassCommand) {
  return requestJson<number>('/admin/classes', {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: 'add class'
  });
}

export function enableAdminClass(classId: number) {
  return requestJson<void>(`/admin/classes/${classId}/enable`, {
    method: 'POST',
    fallbackLabel: 'enable class'
  });
}

export function disableAdminClass(classId: number) {
  return requestJson<void>(`/admin/classes/${classId}/disable`, {
    method: 'POST',
    fallbackLabel: 'disable class'
  });
}

export function createAdminJobRole(command: AdminJobRoleCommand) {
  return requestJson<number>('/admin/job-roles', {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: 'add job role'
  });
}

export function updateAdminJobRole(jobRoleId: number, command: AdminJobRoleCommand) {
  return requestJson<void>(`/admin/job-roles/${jobRoleId}`, {
    method: 'PUT',
    body: JSON.stringify(command),
    fallbackLabel: 'update job role'
  });
}

export function enableAdminJobRole(jobRoleId: number) {
  return requestJson<void>(`/admin/job-roles/${jobRoleId}/enable`, {
    method: 'POST',
    fallbackLabel: 'enable job role'
  });
}

export function disableAdminJobRole(jobRoleId: number) {
  return requestJson<void>(`/admin/job-roles/${jobRoleId}/disable`, {
    method: 'POST',
    fallbackLabel: 'disable job role'
  });
}

export function createAdminClassroom(command: AdminClassroomCommand) {
  return requestJson<number>('/admin/classrooms', {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: 'add classroom'
  });
}

export function updateAdminClassroom(classroomId: number, command: AdminClassroomCommand) {
  return requestJson<void>(`/admin/classrooms/${classroomId}`, {
    method: 'PUT',
    body: JSON.stringify(command),
    fallbackLabel: 'update classroom'
  });
}

export function deleteAdminClassroom(classroomId: number) {
  return requestJson<void>(`/admin/classrooms/${classroomId}`, {
    method: 'DELETE',
    fallbackLabel: 'delete classroom'
  });
}

export function createAdminScoreWeight(command: AdminScoreWeightCommand) {
  return requestJson<number>('/admin/score-weights', {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: 'score weight'
  });
}

export function replaceAdminScoreGradeRules(command: AdminScoreGradeRuleCommand[]) {
  return requestJson<void>('/admin/score-grade-rules', {
    method: 'PUT',
    body: JSON.stringify(command),
    fallbackLabel: 'score grade rules'
  });
}
