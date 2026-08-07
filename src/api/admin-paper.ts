import { requestJson } from './http';
import type { AdminQuestionImportRow } from './admin-question';

export interface AdminPaperQuestion {
  questionId: number;
  questionType?: string;
  title?: string;
  standardAnswer?: string;
  score?: number;
  sortOrder?: number;
  options?: Array<{ optionKey?: string; optionText?: string }>;
}

export interface AdminPaper {
  paperId: number;
  paperName: string;
  courseName?: string;
  composeMode?: string;
  totalScore?: number;
  questionCount?: number;
  publishStatus?: string;
  creatorId?: number;
  creatorName?: string;
  createdAt?: string;
  updatedAt?: string;
  questions?: AdminPaperQuestion[];
}

export interface AdminPaperAutoRule { questionType: string; questionCount: number; scorePerQuestion: number; }
export interface AdminPaperQuestionCommand { questionId: number; score: number; }
export interface AdminPaperCommand { paperName: string; courseName?: string; composeMode: string; questions?: AdminPaperQuestionCommand[]; autoRules?: AdminPaperAutoRule[]; }
export interface AdminPaperLog { logId: number; paperId: number; operatorName?: string; action?: string; content?: string; createdAt?: string; }
export interface AdminPaperImportRow { rowNumber?: number; paperName?: string; composeMode?: string; questions?: AdminPaperQuestionCommand[]; autoRules?: AdminPaperAutoRule[]; }
export interface AdminPaperImportPreview { validCount?: number; invalidCount?: number; errors?: Array<{ rowNumber?: number; message?: string }>; rows?: AdminPaperImportRow[]; }
export interface AdminPaperQuestionImportCommand { paperName: string; courseName: string; fileName: string; fileSize: number; rows: AdminQuestionImportRow[]; }
export interface PageResponse<T> { records: T[]; total: number; page?: number; pageSize?: number; }

function query(params: Record<string, string | number | undefined>) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => { if (value !== undefined && value !== '') search.set(key, String(value)); });
  const text = search.toString();
  return text ? `?${text}` : '';
}

function page<T>(payload: PageResponse<T> | T[]) { return Array.isArray(payload) ? { records: payload, total: payload.length } : { records: payload?.records ?? [], total: payload?.total ?? 0 }; }

export async function fetchAdminPapers(params: { keyword?: string; composeMode?: string; publishStatus?: string; creatorId?: number; page?: number; pageSize?: number } = {}) {
  const result = await requestJson<PageResponse<AdminPaper> | AdminPaper[]>(`/admin/papers${query({ keyword: params.keyword?.trim(), composeMode: params.composeMode, publishStatus: params.publishStatus, creatorId: params.creatorId, page: params.page, pageSize: params.pageSize })}`, { fallbackLabel: '理论试卷' });
  return page(result);
}

export function fetchAdminPaper(paperId: number) { return requestJson<AdminPaper>(`/admin/papers/${paperId}`, { fallbackLabel: '试卷详情' }); }
export function createAdminPaper(command: AdminPaperCommand) { return requestJson<number>('/admin/papers', { method: 'POST', body: JSON.stringify(command), fallbackLabel: '新增试卷' }); }
export function updateAdminPaper(paperId: number, command: AdminPaperCommand) { return requestJson<void>(`/admin/papers/${paperId}`, { method: 'PUT', body: JSON.stringify(command), fallbackLabel: '编辑试卷' }); }
export function publishAdminPaper(paperId: number) { return requestJson<void>(`/admin/papers/${paperId}/publish`, { method: 'POST', fallbackLabel: '发布试卷' }); }
export function cancelPublishAdminPaper(paperId: number) { return requestJson<void>(`/admin/papers/${paperId}/cancel-publish`, { method: 'POST', fallbackLabel: '撤回试卷' }); }
export function fetchAdminPaperLogs(paperId: number) { return requestJson<AdminPaperLog[]>(`/admin/papers/${paperId}/logs`, { fallbackLabel: '试卷操作记录' }); }
export function previewAdminPaperImport(fileName: string, rows: AdminPaperImportRow[]) { return requestJson<AdminPaperImportPreview>('/admin/papers/import/preview', { method: 'POST', body: JSON.stringify({ fileName, fileSize: 0, rows }), fallbackLabel: '试卷导入预览' }); }
export function importAdminPaperQuestions(command: AdminPaperQuestionImportCommand) { return requestJson<number>('/admin/papers/import/questions', { method: 'POST', body: JSON.stringify(command), fallbackLabel: '导入试卷' }); }
