import { requestJson } from './http';

export interface AdminQuestionOption {
  optionKey?: string;
  optionText?: string;
  correct?: boolean;
}

export interface AdminQuestion {
  questionId: number;
  questionType?: string;
  courseName?: string;
  title: string;
  standardAnswer?: string;
  explanation?: string;
  score?: number;
  enabled?: boolean;
  creatorId?: number;
  creatorName?: string;
  createdAt?: string;
  updatedAt?: string;
  options?: AdminQuestionOption[];
}

export interface AdminQuestionQuery {
  keyword?: string;
  questionType?: string;
  enabled?: boolean;
  creatorId?: number;
  page?: number;
  pageSize?: number;
}

export interface AdminQuestionCommand {
  questionType: string;
  courseName: string;
  title: string;
  standardAnswer: string;
  explanation?: string;
  score: number;
  options?: AdminQuestionOption[];
}

export interface AdminQuestionLog {
  logId: number;
  questionId: number;
  operatorName?: string;
  action?: string;
  content?: string;
  createdAt?: string;
}

export interface AdminQuestionImportRow {
  rowNumber?: number;
  questionType?: string;
  title?: string;
  standardAnswer?: string;
  explanation?: string;
  score?: number;
  options?: AdminQuestionOption[];
}

export interface AdminQuestionImportPreview {
  validCount?: number;
  errorCount?: number;
  errors?: Array<{ rowNumber?: number; message?: string }>;
  validRows?: AdminQuestionImportRow[];
}

export interface AdminQuestionImportCommand {
  fileName: string;
  fileSize: number;
  courseName: string;
  rows: AdminQuestionImportRow[];
}

export interface PageResponse<T> {
  records: T[];
  total: number;
  page?: number;
  pageSize?: number;
}

function buildQuery(params: Record<string, string | number | boolean | undefined>) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== '') {
      search.set(key, String(value));
    }
  });
  const query = search.toString();
  return query ? `?${query}` : '';
}

function normalizePage<T>(payload: PageResponse<T> | T[]) {
  if (Array.isArray(payload)) {
    return { records: payload, total: payload.length, page: 1, pageSize: payload.length || 20 };
  }
  return {
    records: payload?.records ?? [],
    total: payload?.total ?? 0,
    page: payload?.page ?? 1,
    pageSize: payload?.pageSize ?? 20
  };
}

export async function fetchAdminQuestions(query: AdminQuestionQuery = {}) {
  const result = await requestJson<PageResponse<AdminQuestion> | AdminQuestion[]>(
    `/admin/questions${buildQuery({
      keyword: query.keyword?.trim(),
      questionType: query.questionType,
      enabled: query.enabled,
      creatorId: query.creatorId,
      page: query.page,
      pageSize: query.pageSize
    })}`,
    { fallbackLabel: '理论试题' }
  );
  return normalizePage(result);
}

export function fetchAdminQuestion(questionId: number) {
  return requestJson<AdminQuestion>(`/admin/questions/${questionId}`, { fallbackLabel: '试题详情' });
}

export function createAdminQuestion(command: AdminQuestionCommand) {
  return requestJson<number>('/admin/questions', {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: '新增试题'
  });
}

export function updateAdminQuestion(questionId: number, command: AdminQuestionCommand) {
  return requestJson<void>(`/admin/questions/${questionId}`, {
    method: 'PUT',
    body: JSON.stringify(command),
    fallbackLabel: '编辑试题'
  });
}

export function enableAdminQuestion(questionId: number) {
  return requestJson<void>(`/admin/questions/${questionId}/enable`, { method: 'POST', fallbackLabel: '启用试题' });
}

export function disableAdminQuestion(questionId: number) {
  return requestJson<void>(`/admin/questions/${questionId}/disable`, { method: 'POST', fallbackLabel: '停用试题' });
}

export function fetchAdminQuestionLogs(questionId: number) {
  return requestJson<AdminQuestionLog[]>(`/admin/questions/${questionId}/logs`, { fallbackLabel: '试题操作记录' });
}

export function previewAdminQuestionImport(command: AdminQuestionImportCommand) {
  return requestJson<AdminQuestionImportPreview>('/admin/questions/import/preview', {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: '导入预览'
  });
}

export function importAdminQuestions(command: AdminQuestionImportCommand) {
  return requestJson<number>('/admin/questions/import', {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: '导入试题'
  });
}
