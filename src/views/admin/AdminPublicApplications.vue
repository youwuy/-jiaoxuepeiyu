<template>
  <AdminShell activeKey="public-application">
    <section v-if="detailPageVisible && selectedApplication" class="admin-public-review-page">
      <header class="admin-public-review-top">
        <button type="button" class="admin-public-review-back" @click="closeDetailPage">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </button>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>资源管理</el-breadcrumb-item>
          <el-breadcrumb-item>审核列表</el-breadcrumb-item>
          <el-breadcrumb-item>{{ detailMode === 'review' ? '审核详情' : '查看审核详情' }}</el-breadcrumb-item>
        </el-breadcrumb>
      </header>

      <section class="admin-public-review-info-card">
        <div class="admin-public-review-card-title">
          <span><el-icon><Document /></el-icon></span>
          <strong>资源审核详情</strong>
        </div>
        <dl>
          <div><dt>资源名称</dt><dd>{{ selectedApplication.resourceName }}</dd></div>
          <div><dt>资源类型</dt><dd><span class="admin-public-type-pill" :class="selectedApplication.typeTone">{{ selectedApplication.typeLabel }}</span></dd></div>
          <div><dt>所属专业</dt><dd>{{ selectedApplication.majorLabel }}</dd></div>
          <div><dt>所属课程</dt><dd>{{ selectedApplication.courseName || '-' }}</dd></div>
          <div><dt>申请人</dt><dd>{{ selectedApplication.applicantName || '-' }}</dd></div>
          <div><dt>申请时间</dt><dd>{{ selectedApplication.appliedAtLabel }}</dd></div>
        </dl>
      </section>

      <div class="admin-public-review-grid">
        <section class="admin-public-review-preview-card">
          <div class="admin-public-review-card-title">
            <span><el-icon><Document /></el-icon></span>
            <strong>资源内容预览</strong>
            <el-button class="admin-public-preview-download" :icon="Download" @click="downloadResource(selectedApplication)">下载</el-button>
          </div>
          <div class="admin-public-review-doc"><AdminResourcePreview :resource="selectedApplication" /></div>
        </section>

        <aside class="admin-public-review-progress-card">
          <div class="admin-public-review-card-title">
            <span><el-icon><Refresh /></el-icon></span>
            <strong>审核进度</strong>
          </div>
          <div class="admin-public-review-timeline">
            <article v-for="item in reviewTimeline" :key="item.title">
              <i></i>
              <div>
                <header>
                  <strong>{{ item.title }}</strong>
                  <span>{{ item.date }}</span>
                </header>
                <p>操作人： <b>{{ item.operator }}</b></p>
                <p>操作时间： <b>{{ item.time }}</b></p>
                <em v-if="item.status">{{ item.status }}</em>
                <small v-if="item.comment">{{ item.comment }}</small>
              </div>
            </article>
          </div>
        </aside>
      </div>

      <section v-if="detailMode === 'review'" class="admin-public-review-action-card">
        <div class="admin-public-review-card-title">
          <span><el-icon><Refresh /></el-icon></span>
          <strong>审核操作</strong>
        </div>
        <label>
          <span>审核意见</span>
          <el-input
            v-model="reviewComment"
            type="textarea"
            :rows="6"
            maxlength="500"
            show-word-limit
            placeholder="请输入审核意见，包括审核结论、修改建议等..."
          />
          <small>若驳回，则必须要填写审核意见</small>
        </label>
        <footer>
          <el-button class="admin-public-review-reject" :loading="saving" @click="submitDetailReview('REJECTED')">驳回</el-button>
          <el-button class="admin-public-review-approve" type="primary" :loading="saving" @click="submitDetailReview('APPROVED')">审核通过</el-button>
        </footer>
      </section>
    </section>

    <section v-else class="admin-public-page">
      <el-breadcrumb class="admin-public-breadcrumb" separator="/">
        <el-breadcrumb-item>资源管理</el-breadcrumb-item>
        <el-breadcrumb-item>资源公开申请</el-breadcrumb-item>
      </el-breadcrumb>

      <section class="admin-public-filter-card">
        <div class="admin-public-filter-row">
          <el-input
            v-model="draft.keyword"
            class="admin-public-search"
            :prefix-icon="Search"
            placeholder="搜索资源名称"
            clearable
            @keyup.enter="applyFilters"
          />
          <el-select v-model="draft.publicStatus" class="admin-public-select" placeholder="审核状态：全部" clearable>
            <el-option v-for="item in publicStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-button class="admin-public-query-button" @click="applyFilters">查询</el-button>
          <el-button class="admin-public-reset-button" @click="resetFilters">重置</el-button>
        </div>
      </section>

      <div class="admin-public-workspace">
        <section class="admin-public-board">
          <header class="admin-public-board-head">
            <div>
              <strong>申请列表</strong>
              <p>共 {{ totalCount }} 条申请</p>
            </div>
          </header>

          <div v-if="loading" class="admin-public-empty">申请加载中...</div>
          <div v-else-if="pagedApplications.length === 0" class="admin-public-empty">
            <el-empty description="暂无匹配申请" />
          </div>
          <template v-else>
            <div class="admin-public-table-scroll">
              <table class="admin-public-table">
                <thead>
                  <tr>
                    <th>资源名称</th>
                    <th>资源类型</th>
                    <th>所属专业</th>
                    <th>所属课程</th>
                    <th>申请人</th>
                    <th>申请时间</th>
                    <th>审核状态</th>
                    <th>审核时间</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="row in pagedApplications"
                    :key="row.applicationId"
                  >
                    <td>
                      <div class="admin-public-name-cell">
                        <img :src="row.coverResolved" :alt="row.resourceName" />
                        <div>
                          <strong>{{ row.resourceName }}</strong>
                          <span>{{ row.fileName || row.fileUrl || '-' }}</span>
                        </div>
                      </div>
                    </td>
                    <td>
                      <span class="admin-public-type-pill" :class="row.typeTone">{{ row.typeLabel }}</span>
                    </td>
                    <td class="wrap-cell">{{ row.majorLabel }}</td>
                    <td class="wrap-cell">{{ row.courseName || '-' }}</td>
                    <td>{{ row.applicantName || '-' }}</td>
                    <td>{{ row.appliedAtLabel }}</td>
                    <td>
                      <span class="admin-public-status" :class="row.statusTone">
                        <i />
                        {{ row.statusLabel }}
                      </span>
                    </td>
                    <td>{{ row.reviewedAtLabel }}</td>
                    <td>
                      <div class="admin-public-row-actions">
                        <el-button v-if="row.statusTone === 'pending' && can('update')" class="approve" @click.stop="openReviewDetail(row)">审核</el-button>
                        <el-button v-else class="detail-button" @click.stop="openReadonlyDetail(row)">查看审核详情</el-button>
                        <el-button v-if="row.statusTone === 'rejected' && can('update') && isOwnApplication(row)" class="warn" @click.stop="openEditRestatement(row)">编辑重申</el-button>
                        <el-button class="plain" @click.stop="openPreview(row)">预览</el-button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <footer class="admin-public-footer">
              <p>共 {{ totalCount }} 条记录</p>
              <el-pagination
                v-model:current-page="page"
                :page-size="pageSize"
                :total="totalCount"
                layout="prev, pager, next"
                background
              />
            </footer>
          </template>
        </section>
      </div>
    </section>

    <el-dialog v-model="previewVisible" class="admin-public-preview-modal" width="820px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-public-dialog-head">
          <strong>{{ previewTarget?.resourceName || '资源预览' }}</strong>
          <el-button text circle :icon="Close" @click="previewVisible = false" />
        </div>
      </template>

      <div v-if="previewTarget" class="admin-public-preview-doc"><AdminResourcePreview :resource="previewTarget" /></div>
      <template #footer>
        <el-button type="primary" :icon="Download" @click="downloadResource(previewTarget)">下载资源</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editRestatementVisible" class="admin-public-edit-dialog" width="600px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-public-edit-head">
          <strong>编辑重申</strong>
          <el-button text circle :icon="Close" @click="editRestatementVisible = false" />
        </div>
      </template>

      <div v-if="editTarget" class="admin-public-edit-form">
        <label>
          <span>资源名称 <b>*</b></span>
          <el-input v-model="editForm.resourceName" maxlength="20" show-word-limit />
        </label>
        <label>
          <span>封面图 <b>*</b></span>
          <div v-if="editForm.coverUrl" class="admin-public-edit-file cover">
            <img :src="editForm.coverUrl" alt="封面图" />
            <div>
              <strong>{{ editForm.coverName || '-' }}</strong>
              <p>{{ editForm.coverSize || '-' }}</p>
            </div>
            <el-button text circle :icon="Close" @click="clearEditCover" />
          </div>
          <button v-else type="button" class="admin-public-edit-upload-drop" :disabled="uploadingEditCover" @click="editCoverInput?.click()" @dragover.prevent @drop.prevent="uploadEditCoverDrop">上传封面图</button>
          <input ref="editCoverInput" type="file" accept="image/jpeg,image/png" hidden @change="uploadEditCover" />
        </label>
        <label>
          <span>资源内容 <b>*</b></span>
          <div v-if="editForm.fileUrl" class="admin-public-edit-file">
            <span class="admin-public-edit-file-icon"><el-icon><Document /></el-icon></span>
            <div>
              <strong>{{ editForm.fileName }}</strong>
              <p>{{ editForm.fileSizeLabel }} <i></i> 上传完成 <em>✓</em></p>
            </div>
            <el-button text circle :icon="Close" @click="clearEditFile" />
          </div>
          <button v-else type="button" class="admin-public-edit-upload-drop" :disabled="uploadingEditFile" @click="editFileInput?.click()" @dragover.prevent @drop.prevent="uploadEditFileDrop">上传资源文件</button>
          <input ref="editFileInput" type="file" hidden @change="uploadEditFile" />
        </label>
        <label>
          <span>所属专业 <b>*</b></span>
          <el-select v-model="editForm.majorId" placeholder="请选择所属专业" filterable>
            <el-option v-for="item in majorOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </label>
        <label>
          <span>所属课程</span>
          <el-input v-model="editForm.courseName" maxlength="30" show-word-limit />
        </label>
      </div>

      <template #footer>
        <div class="admin-public-edit-footer">
          <el-button @click="editRestatementVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitEditRestatement">编辑重申</el-button>
        </div>
      </template>
    </el-dialog>

  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeft, Close, Document, Download, Refresh, Search } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import AdminResourcePreview from '../../components/admin/AdminResourcePreview.vue';
import {
  approveAdminPublicApplication,
  fetchAdminPublicApplication,
  fetchAdminPublicApplications,
  rejectAdminPublicApplication,
  submitAdminResourcePublicApplication,
  updateAdminResource,
  uploadAdminFile,
  type AdminPublicApplication,
  type AdminResourceQuery
} from '../../api/admin-resource';
import { fetchAdminMajors } from '../../api/admin-course';
import { resolvePublicUrl } from '../../api/http';
import { fileFromDrop, validateResourceUpload } from '../../features/admin/resource-upload';
import { useAdminPermissions } from '../../features/admin/use-admin-permissions';
import { getStoredUserId } from '../../api/http';
import { coverForResourceType } from '../../features/student/resources';

type ReviewMode = 'APPROVED' | 'REJECTED';
type ApplicationStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

interface ResourceOption {
  label: string;
  value: string;
}

interface MajorOption {
  label: string;
  value: number;
}

interface PublicApplicationRow extends AdminPublicApplication {
  coverResolved: string;
  typeLabel: string;
  typeTone: string;
  statusLabel: string;
  statusTone: 'pending' | 'approved' | 'rejected';
  majorLabel: string;
  fileSizeLabel: string;
  appliedAtLabel: string;
  reviewedAtLabel: string;
}

const pageSize = 8;
const loading = ref(false);
const saving = ref(false);
const busyId = ref<number | null>(null);
const page = ref(1);
const applications = ref<PublicApplicationRow[]>([]);
const selectedApplication = ref<PublicApplicationRow | null>(null);
const reviewMode = ref<ReviewMode>('APPROVED');
const reviewTarget = ref<PublicApplicationRow | null>(null);
const reviewComment = ref('');
const previewVisible = ref(false);
const previewTarget = ref<PublicApplicationRow | null>(null);
const detailPageVisible = ref(false);
const detailMode = ref<'review' | 'readonly'>('review');
const editRestatementVisible = ref(false);
const { can } = useAdminPermissions('resource:public-apply');
const currentAdminId = Number(getStoredUserId('admin'));
const editTarget = ref<PublicApplicationRow | null>(null);
const editCoverInput = ref<HTMLInputElement | null>(null);
const editFileInput = ref<HTMLInputElement | null>(null);
const uploadingEditCover = ref(false);
const uploadingEditFile = ref(false);
const editForm = reactive({
  resourceName: '',
  coverUrl: '',
  coverName: '',
  coverSize: '',
  fileUrl: '',
  previewUrl: '',
  fileName: '',
  fileSize: 0,
  fileSizeLabel: '',
  majorId: null as number | null,
  courseName: ''
});
const reviewTimeline = computed(() => {
  const row = selectedApplication.value;
  if (!row) {
    return [];
  }

  return [
    ...(row.reviewedAtLabel === '-'
      ? []
      : [
          {
            title: '审核',
            date: row.reviewedAtLabel.slice(0, 10),
            operator: row.reviewerName || '-',
            time: row.reviewedAtLabel.slice(11) || '-',
            status: row.statusTone === 'approved' ? '审核通过' : row.statusTone === 'rejected' ? '审核驳回' : '',
            comment: row.reviewComment || '-'
          }
        ]),
    {
      title: '提交申请',
      date: row.appliedAtLabel.slice(0, 10),
      operator: row.applicantName || '-',
      time: row.appliedAtLabel.slice(11) || '-',
      status: '',
      comment: ''
    }
  ];
});

const draft = reactive({
  keyword: '',
  publicStatus: '' as '' | ApplicationStatus
});

const appliedFilters = ref({ ...draft });

const majorOptions = ref<MajorOption[]>([]);

const publicStatusOptions: ResourceOption[] = [
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
];

const totalCount = ref(0);
const pagedApplications = computed(() => applications.value);

function normalizeStatus(value?: string): ApplicationStatus {
  const upper = String(value || '').toUpperCase();
  if (upper === 'APPROVED' || upper === 'PUBLIC') {
    return 'APPROVED';
  }
  if (upper === 'REJECTED') {
    return 'REJECTED';
  }
  return 'PENDING';
}

function statusTone(status: ApplicationStatus): 'pending' | 'approved' | 'rejected' {
  if (status === 'APPROVED') {
    return 'approved';
  }
  if (status === 'REJECTED') {
    return 'rejected';
  }
  return 'pending';
}

function statusLabel(status: ApplicationStatus) {
  if (status === 'APPROVED') {
    return '审核通过';
  }
  if (status === 'REJECTED') {
    return '审核驳回';
  }
  return '审核中';
}

function normalizeResourceType(type?: string) {
  const raw = String(type || '').trim();
  if (!raw) {
    return '资源';
  }
  const upper = raw.toUpperCase();
  const labels: Record<string, string> = {
    DOCUMENT: '文本文档',
    PRESENTATION: '演示文稿',
    IMAGE: '图片',
    AUDIO: '音频',
    VIDEO: '视频'
  };
  return labels[upper] || raw;
}

function typeTone(type: string) {
  if (type === '视频') {
    return 'video';
  }
  if (type === '图片') {
    return 'image';
  }
  if (type === '音频') {
    return 'audio';
  }
  if (type === '演示文稿') {
    return 'presentation';
  }
  if (type === '实训试题') {
    return 'exam';
  }
  return 'document';
}

function formatFileSize(bytes?: number) {
  if (!bytes || bytes <= 0) {
    return '-';
  }
  if (bytes < 1024) {
    return `${bytes} B`;
  }
  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`;
  }
  if (bytes < 1024 * 1024 * 1024) {
    return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
  }
  return `${(bytes / 1024 / 1024 / 1024).toFixed(1)} GB`;
}

function formatDateTime(value?: string) {
  if (!value) {
    return '-';
  }
  return value.includes('T') ? value.replace('T', ' ').slice(0, 16) : value.slice(0, 16);
}

function findMajorLabel(majorId?: number | null) {
  return majorOptions.value.find((item) => item.value === majorId)?.label || '-';
}

function mapApplicationRow(application: AdminPublicApplication): PublicApplicationRow {
  const typeLabel = normalizeResourceType(application.resourceType);
  const status = normalizeStatus(application.publicStatus);
  const majorLabel = application.majorName || findMajorLabel(application.majorId);
  const resolvedCoverUrl = resolvePublicUrl(application.coverUrl);
  return {
    ...application,
    resourceType: typeLabel,
    coverUrl: resolvedCoverUrl || coverForResourceType(typeLabel),
    coverResolved: resolvedCoverUrl || coverForResourceType(typeLabel),
    fileUrl: resolvePublicUrl(application.fileUrl),
    previewUrl: resolvePublicUrl(application.previewUrl),
    typeLabel,
    typeTone: typeTone(typeLabel),
    statusLabel: statusLabel(status),
    statusTone: statusTone(status),
    majorLabel,
    fileSizeLabel: formatFileSize(application.fileSize),
    appliedAtLabel: formatDateTime(application.appliedAt),
    reviewedAtLabel: formatDateTime(application.reviewedAt),
    publicStatus: status
  };
}

function createEmptyFilters() {
  return {
    keyword: '',
    publicStatus: '' as '' | ApplicationStatus
  };
}

function openReviewDetail(application: PublicApplicationRow) {
  selectedApplication.value = application;
  detailMode.value = 'review';
  reviewComment.value = '';
  detailPageVisible.value = true;
  void loadApplicationDetail(application);
}

function openReadonlyDetail(application: PublicApplicationRow) {
  selectedApplication.value = application;
  detailMode.value = 'readonly';
  detailPageVisible.value = true;
  void loadApplicationDetail(application);
}

function closeDetailPage() {
  detailPageVisible.value = false;
}

async function submitDetailReview(mode: ReviewMode) {
  if (!selectedApplication.value) {
    return;
  }

  reviewTarget.value = selectedApplication.value;
  reviewMode.value = mode;
  const completed = await submitReview();
  if (completed) {
    detailPageVisible.value = false;
  }
}

function applyFilters() {
  appliedFilters.value = {
    keyword: draft.keyword,
    publicStatus: draft.publicStatus
  };
  page.value = 1;
  void loadApplications();
}

function resetFilters() {
  Object.assign(draft, createEmptyFilters());
  appliedFilters.value = createEmptyFilters();
  page.value = 1;
  void loadApplications();
}

function buildQuery(): AdminResourceQuery {
  return {
    keyword: appliedFilters.value.keyword.trim() || undefined,
    publicStatus: appliedFilters.value.publicStatus || undefined,
    page: page.value,
    pageSize
  };
}

async function loadApplications() {
  loading.value = true;
  try {
    const result = await fetchAdminPublicApplications(buildQuery());
    const mapped = result.records.map(mapApplicationRow);
    applications.value = mapped;
    totalCount.value = result.total;
  } catch (error) {
    applications.value = [];
    totalCount.value = 0;
    selectedApplication.value = null;
    ElMessage.error(error instanceof Error ? error.message : '资源公开申请加载失败');
  } finally {
    loading.value = false;
  }
}

async function loadApplicationDetail(application: PublicApplicationRow) {
  try {
    const detail = await fetchAdminPublicApplication(application.applicationId);
    selectedApplication.value = mapApplicationRow(detail);
  } catch {
    selectedApplication.value = application;
  }
}

function openPreview(application: PublicApplicationRow) {
  previewTarget.value = application;
  previewVisible.value = true;
}

function downloadResource(resource: PublicApplicationRow | null | undefined) {
  const url = resource?.fileUrl || resource?.previewUrl;
  if (!url) {
    ElMessage.warning('当前资源没有可下载文件');
    return;
  }
  const link = document.createElement('a');
  link.href = url;
  link.target = '_blank';
  link.rel = 'noopener';
  link.download = resource.fileName || resource.resourceName || '资源文件';
  document.body.appendChild(link);
  link.click();
  link.remove();
}

function openEditRestatement(application: PublicApplicationRow) {
  editTarget.value = application;
  Object.assign(editForm, {
    resourceName: application.resourceName,
    coverUrl: application.coverResolved,
    coverName: '',
    coverSize: '',
    fileUrl: application.fileUrl || '',
    previewUrl: application.previewUrl || '',
    fileName: application.fileName || '',
    fileSize: application.fileSize || 0,
    fileSizeLabel: application.fileSizeLabel === '-' ? '-' : application.fileSizeLabel,
    majorId: application.majorId ?? null,
    courseName: application.courseName || ''
  });
  editRestatementVisible.value = true;
}

function isOwnApplication(application: PublicApplicationRow) {
  return Number.isFinite(currentAdminId) && Number(application.applicantId) === currentAdminId;
}

function clearEditCover() {
  editForm.coverUrl = '';
  editForm.coverName = '';
  editForm.coverSize = '';
}

function clearEditFile() {
  editForm.fileUrl = '';
  editForm.previewUrl = '';
  editForm.fileName = '';
  editForm.fileSize = 0;
  editForm.fileSizeLabel = '';
}

async function uploadEditCover(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;
  const validation = validateResourceUpload(file, 'cover');
  if (validation) { ElMessage.warning(validation); input.value = ''; return; }
  uploadingEditCover.value = true;
  try {
    const uploaded = await uploadAdminFile(file, 'covers');
    editForm.coverUrl = uploaded.fileUrl;
    editForm.coverName = uploaded.fileName || file.name;
    editForm.coverSize = formatFileSize(uploaded.fileSize ?? file.size);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '封面上传失败');
  } finally {
    uploadingEditCover.value = false;
    input.value = '';
  }
}

async function uploadEditFile(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;
  const validation = validateResourceUpload(file);
  if (validation) { ElMessage.warning(validation); input.value = ''; return; }
  uploadingEditFile.value = true;
  try {
    const uploaded = await uploadAdminFile(file, 'resources');
    editForm.fileUrl = uploaded.fileUrl;
    editForm.previewUrl = uploaded.fileUrl;
    editForm.fileName = uploaded.fileName || file.name;
    editForm.fileSize = uploaded.fileSize ?? file.size;
    editForm.fileSizeLabel = formatFileSize(editForm.fileSize);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '资源文件上传失败');
  } finally {
    uploadingEditFile.value = false;
    input.value = '';
  }
}

function uploadEditCoverDrop(event: DragEvent) {
  const file = fileFromDrop(event);
  if (file) void uploadEditCover({ target: { files: [file] } } as unknown as Event);
}

function uploadEditFileDrop(event: DragEvent) {
  const file = fileFromDrop(event);
  if (file) void uploadEditFile({ target: { files: [file] } } as unknown as Event);
}

async function submitEditRestatement() {
  if (!editTarget.value) {
    return;
  }
  const resourceName = editForm.resourceName.trim();
  const majorId = Number(editForm.majorId);
  if (!resourceName) {
    ElMessage.warning('请输入资源名称');
    return;
  }
  if (!majorId) {
    ElMessage.warning('请选择所属专业');
    return;
  }
  if (!editForm.coverUrl || !editForm.fileUrl || !editForm.fileName || !editForm.fileSize) {
    ElMessage.warning('请上传封面图和资源文件');
    return;
  }
  saving.value = true;
  try {
    await updateAdminResource(editTarget.value.resourceId, {
      resourceName,
      coverUrl: editForm.coverUrl,
      fileUrl: editForm.fileUrl,
      previewUrl: editForm.previewUrl || editForm.fileUrl,
      fileName: editForm.fileName,
      fileSize: editForm.fileSize,
      majorId,
      courseName: editForm.courseName.trim() || undefined
    });
    await submitAdminResourcePublicApplication(editTarget.value.resourceId);
    editRestatementVisible.value = false;
    ElMessage.success('资源已编辑并重新提交审核');
    await loadApplications();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '编辑重申失败');
  } finally {
    saving.value = false;
  }
}

async function submitReview(): Promise<boolean> {
  if (!reviewTarget.value) {
    return false;
  }

  const application = reviewTarget.value;
  if (reviewMode.value === 'REJECTED' && !reviewComment.value.trim()) {
    ElMessage.warning('请填写驳回原因');
    return false;
  }

  try {
    await ElMessageBox.confirm(
      reviewMode.value === 'APPROVED'
        ? `确认通过资源「${application.resourceName}」的公开申请？`
        : `确认驳回资源「${application.resourceName}」的公开申请？`,
      reviewMode.value === 'APPROVED' ? '通过申请' : '驳回申请',
      {
        confirmButtonText: reviewMode.value === 'APPROVED' ? '通过' : '驳回',
        cancelButtonText: '取消',
        type: reviewMode.value === 'APPROVED' ? 'success' : 'warning'
      }
    );
  } catch {
    return false;
  }

  busyId.value = application.applicationId;
  saving.value = true;
  try {
    if (reviewMode.value === 'APPROVED') {
      await approveAdminPublicApplication(application.applicationId, {
        reviewComment: reviewComment.value.trim() || undefined
      });
      ElMessage.success('公开申请已通过');
    } else {
      await rejectAdminPublicApplication(application.applicationId, {
        reviewComment: reviewComment.value.trim()
      });
      ElMessage.success('公开申请已驳回');
    }
    await loadApplications();
    selectedApplication.value = applications.value.find((item) => item.applicationId === application.applicationId) || null;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '审核失败');
    return false;
  } finally {
    busyId.value = null;
    saving.value = false;
  }
  return true;
}

async function loadMajorOptions() {
  try {
    majorOptions.value = (await fetchAdminMajors())
      .filter((item) => item.enabled !== false)
      .map((item) => ({ label: item.majorName, value: item.majorId }));
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '专业数据加载失败');
  }
}

watch(page, () => {
  void loadApplications();
});

onMounted(() => {
  void loadMajorOptions();
  void loadApplications();
});
</script>
