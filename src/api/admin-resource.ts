import { requestJson } from './http';

export interface PageResponse<T> {
  records: T[];
  total: number;
  page: number;
  pageSize: number;
}

export interface AdminResource {
  resourceId: number;
  sourceResourceId?: number;
  resourceName: string;
  resourceType?: string;
  coverUrl?: string;
  fileUrl?: string;
  previewUrl?: string;
  fileName?: string;
  fileSize?: number;
  majorId?: number | null;
  majorName?: string;
  courseName?: string;
  uploaderId?: number;
  uploaderName?: string;
  publicStatus?: string;
  currentVersion?: number;
  publicVersion?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface AdminResourceQuery {
  keyword?: string;
  resourceType?: string;
  majorId?: number | null;
  courseName?: string;
  uploaderId?: number | null;
  publicStatus?: string;
  uploadStartDate?: string;
  uploadEndDate?: string;
  page?: number;
  pageSize?: number;
}

export interface AdminResourceCommand {
  resourceName: string;
  coverUrl: string;
  fileUrl: string;
  previewUrl?: string;
  fileName: string;
  fileSize: number;
  majorId: number;
  courseName?: string;
}

export interface AdminResourceBatchCommand {
  resourceIds: number[];
  coverUrl?: string;
  majorId?: number;
  courseName?: string;
}

export interface AdminPublicReviewCommand {
  reviewComment?: string;
}

export interface AdminPublicApplication extends AdminResource {
  applicationId: number;
  publicResourceId?: number;
  resourceVersion?: number;
  applicantId?: number;
  applicantName?: string;
  reviewerId?: number;
  reviewerName?: string;
  reviewComment?: string;
  appliedAt?: string;
  reviewedAt?: string;
}

export interface AdminResourceLog {
  logId: number;
  resourceId: number;
  operatorName: string;
  action: string;
  content: string;
  createdAt: string;
}

export interface UploadedFile {
  fileUrl: string;
  fileName: string;
  storedFileName?: string;
  fileSize?: number;
  contentType?: string;
  category?: string;
}

function buildQuery(params: Record<string, string | number | boolean | null | undefined>): string {
  const search = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, String(value));
    }
  });

  const query = search.toString();
  return query ? `?${query}` : '';
}

function normalizePage<T>(payload: PageResponse<T> | T[] | undefined): PageResponse<T> {
  if (Array.isArray(payload)) {
    return {
      records: payload,
      total: payload.length,
      page: 1,
      pageSize: payload.length || 20
    };
  }

  return {
    records: payload?.records ?? [],
    total: payload?.total ?? 0,
    page: payload?.page ?? 1,
    pageSize: payload?.pageSize ?? 20
  };
}

export async function fetchAdminResources(query: AdminResourceQuery = {}) {
  const result = await requestJson<PageResponse<AdminResource> | AdminResource[]>(
    `/admin/resources${buildQuery({
      keyword: query.keyword?.trim(),
      resourceType: query.resourceType,
      majorId: query.majorId ?? undefined,
      courseName: query.courseName?.trim(),
      uploaderId: query.uploaderId ?? undefined,
      publicStatus: query.publicStatus,
      uploadStartDate: query.uploadStartDate,
      uploadEndDate: query.uploadEndDate,
      page: query.page,
      pageSize: query.pageSize
    })}`,
    {
      fallbackLabel: '个人资源库'
    }
  );
  return normalizePage(result as PageResponse<AdminResource> | AdminResource[]);
}

export async function fetchAdminResource(resourceId: number) {
  return requestJson<AdminResource>(`/admin/resources/${resourceId}`, {
    fallbackLabel: '资源详情'
  });
}

export async function createAdminResource(command: AdminResourceCommand) {
  return requestJson<number>('/admin/resources', {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: '新增资源'
  });
}

export async function updateAdminResource(resourceId: number, command: AdminResourceCommand) {
  return requestJson<void>(`/admin/resources/${resourceId}`, {
    method: 'PUT',
    body: JSON.stringify(command),
    fallbackLabel: '编辑资源'
  });
}

export async function batchUpdateAdminResources(command: AdminResourceBatchCommand) {
  return requestJson<void>('/admin/resources/batch', {
    method: 'PUT',
    body: JSON.stringify(command),
    fallbackLabel: '批量修改资源'
  });
}

export async function deleteAdminResources(resourceIds: number[]) {
  return requestJson<void>('/admin/resources/batch/delete', {
    method: 'POST',
    body: JSON.stringify({ resourceIds }),
    fallbackLabel: '删除资源'
  });
}

export async function submitAdminResourcePublicApplication(resourceId: number) {
  return requestJson<number>(`/admin/resources/${resourceId}/public-applications`, {
    method: 'POST',
    fallbackLabel: '公开申请'
  });
}

export async function fetchAdminPublicApplications(query: AdminResourceQuery = {}) {
  const result = await requestJson<PageResponse<AdminPublicApplication> | AdminPublicApplication[]>(
    `/admin/public-applications${buildQuery({
      keyword: query.keyword?.trim(),
      resourceType: query.resourceType,
      majorId: query.majorId ?? undefined,
      courseName: query.courseName?.trim(),
      uploaderId: query.uploaderId ?? undefined,
      publicStatus: query.publicStatus,
      uploadStartDate: query.uploadStartDate,
      uploadEndDate: query.uploadEndDate,
      page: query.page,
      pageSize: query.pageSize
    })}`,
    {
      fallbackLabel: '资源公开申请'
    }
  );
  return normalizePage(result as PageResponse<AdminPublicApplication> | AdminPublicApplication[]);
}

export async function fetchAdminPublicApplication(applicationId: number) {
  return requestJson<AdminPublicApplication>(`/admin/public-applications/${applicationId}`, {
    fallbackLabel: '公开申请详情'
  });
}

export async function fetchAdminPublicResources(query: AdminResourceQuery = {}) {
  const result = await requestJson<PageResponse<AdminResource> | AdminResource[]>(
    `/admin/public-resources${buildQuery({
      keyword: query.keyword?.trim(),
      resourceType: query.resourceType,
      majorId: query.majorId ?? undefined,
      courseName: query.courseName?.trim(),
      uploaderId: query.uploaderId ?? undefined,
      publicStatus: query.publicStatus,
      uploadStartDate: query.uploadStartDate,
      uploadEndDate: query.uploadEndDate,
      page: query.page,
      pageSize: query.pageSize
    })}`,
    {
      fallbackLabel: '公开资源库'
    }
  );
  return normalizePage(result as PageResponse<AdminResource> | AdminResource[]);
}

export async function approveAdminPublicApplication(applicationId: number, command: AdminPublicReviewCommand = {}) {
  return requestJson<void>(`/admin/public-applications/${applicationId}/approve`, {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: '公开申请通过'
  });
}

export async function rejectAdminPublicApplication(applicationId: number, command: AdminPublicReviewCommand = {}) {
  return requestJson<void>(`/admin/public-applications/${applicationId}/reject`, {
    method: 'POST',
    body: JSON.stringify(command),
    fallbackLabel: '公开申请驳回'
  });
}

export async function fetchAdminResourceLogs(resourceId: number) {
  return requestJson<AdminResourceLog[]>(`/admin/resources/${resourceId}/logs`, {
    fallbackLabel: '资源日志'
  });
}

export async function uploadAdminFile(file: File, category: string) {
  const body = new FormData();
  body.append('file', file);
  body.append('category', category);

  return requestJson<UploadedFile>('/files', {
    method: 'POST',
    body,
    fallbackLabel: '文件上传'
  });
}
