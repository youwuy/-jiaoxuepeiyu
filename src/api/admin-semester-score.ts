import { requestBlob, requestJson } from './http';
import type { PageResponse } from './admin-account';

export interface AdminSemesterScoreQuery {
  semesterId?: number | null;
  classId?: number | null;
  majorId?: number | null;
  studentId?: number | null;
  keyword?: string;
  page?: number;
  pageSize?: number;
}

export interface AdminSemesterScore {
  scoreId: number;
  studentId: number;
  studentName: string;
  studentNo: string;
  classId?: number;
  className?: string;
  majorId?: number;
  majorName?: string;
  semesterId?: number;
  academicTerm?: string;
  coursewareLearningScore?: number;
  trainingPracticeScore?: number;
  courseAssignmentScore?: number;
  examScore?: number;
  coursewareWeight?: number;
  trainingPracticeWeight?: number;
  assignmentWeight?: number;
  examWeight?: number;
  comprehensiveScore?: number;
  rankNo?: number;
  publishedAt?: string;
}

export interface AdminSemesterScoreStatistics {
  studentCount: number;
  averageScore: number;
  maxScore: number;
  minScore: number;
  excellentCount: number;
  passCount: number;
}

export interface AdminSemesterScoreImportRow {
  rowNo?: number;
  studentNo?: string;
  studentId?: number;
  semesterId?: number;
  coursewareLearningScore?: number;
  trainingPracticeScore?: number;
  courseAssignmentScore?: number;
  examScore?: number;
  coursewareWeight?: number;
  trainingPracticeWeight?: number;
  assignmentWeight?: number;
  examWeight?: number;
  comprehensiveScore?: number;
  valid?: boolean;
  errors?: string[];
}

export interface AdminSemesterScoreImportPreview {
  totalCount: number;
  validCount: number;
  errorCount: number;
  rows: AdminSemesterScoreImportRow[];
}

export interface AdminSemesterScoreImportResult {
  importedCount: number;
}

function queryString(query: AdminSemesterScoreQuery = {}) {
  const params = new URLSearchParams();
  Object.entries(query).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      params.set(key, String(value));
    }
  });

  const text = params.toString();
  return text ? `?${text}` : '';
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

export function fetchAdminSemesterScores(query: AdminSemesterScoreQuery = {}) {
  return requestJson<PageResponse<AdminSemesterScore>>(`/admin/scores/semester${queryString(query)}`, {
    fallbackLabel: '综合成绩'
  });
}

export function fetchAdminSemesterScoreStatistics(query: AdminSemesterScoreQuery = {}) {
  return requestJson<AdminSemesterScoreStatistics>(`/admin/scores/semester/statistics${queryString(query)}`, {
    fallbackLabel: '综合成绩统计'
  });
}

export function previewAdminSemesterScoreImport(rows: AdminSemesterScoreImportRow[]) {
  return requestJson<AdminSemesterScoreImportPreview>('/admin/scores/semester/import/preview', {
    method: 'POST',
    body: JSON.stringify({ rows }),
    fallbackLabel: '线下成绩导入预览'
  });
}

export function importAdminSemesterScores(rows: AdminSemesterScoreImportRow[]) {
  return requestJson<AdminSemesterScoreImportResult>('/admin/scores/semester/import', {
    method: 'POST',
    body: JSON.stringify({ rows }),
    fallbackLabel: '线下成绩导入'
  });
}

export async function exportAdminSemesterScores(query: AdminSemesterScoreQuery = {}) {
  const result = await requestBlob(`/admin/scores/semester/export/file${queryString(query)}`, {
    fallbackLabel: '导出综合成绩'
  });
  downloadBlob(result.blob, result.filename || 'semester-scores.csv');
}
