<template>
  <AdminShell activeKey="personal-resource">
    <section class="admin-resource-page">
      <el-breadcrumb class="admin-resource-breadcrumb" separator="/">
        <el-breadcrumb-item>资源管理</el-breadcrumb-item>
        <el-breadcrumb-item>个人资源库</el-breadcrumb-item>
      </el-breadcrumb>

      <section class="admin-resource-filter-card">
        <div class="admin-resource-filter-row">
          <label>
            <span>资源名称</span>
            <el-input
              v-model="draft.keyword"
              class="admin-resource-search"
              :prefix-icon="Search"
              placeholder="请输入资源名称"
              clearable
              @keyup.enter="applyFilters"
            />
          </label>
          <label>
            <span>分类</span>
            <el-select v-model="draft.resourceType" class="admin-resource-select" placeholder="请选择分类" clearable>
              <el-option v-for="item in resourceTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </label>
          <label>
            <span>所属专业</span>
            <el-select v-model="draft.majorId" class="admin-resource-select" placeholder="请选择专业" clearable filterable>
              <el-option v-for="item in majorOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </label>
          <label>
            <span>所属课程</span>
            <el-input v-model="draft.courseName" class="admin-resource-select" placeholder="请选择课程" clearable />
          </label>
          <label>
            <span>上传时间段</span>
            <el-date-picker
              v-model="draft.uploadDateRange"
              class="admin-resource-date-range"
              type="daterange"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              unlink-panels
            />
          </label>
          <div class="admin-resource-filter-actions">
            <el-button class="admin-resource-query-button" type="primary" :icon="Search" @click="applyFilters">查询</el-button>
            <el-button class="admin-resource-reset-button" @click="resetFilters">重置</el-button>
          </div>
        </div>
      </section>

      <section class="admin-resource-actions-row">
        <el-button v-if="can('create')" class="admin-resource-primary-button" type="primary" @click="openCreatePanel">
          <el-icon><Plus /></el-icon>
          上传资源
        </el-button>
        <el-button v-if="can('update')" class="admin-resource-lite-button" :disabled="selectedIds.length === 0" @click="openBatchEdit">
          <el-icon><Setting /></el-icon>
          批量设置
        </el-button>
        <el-button v-if="can('delete')" class="admin-resource-lite-button danger" :disabled="selectedIds.length === 0" @click="batchDeleteResources">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </section>

      <section class="admin-resource-board">
        <div v-if="loading" class="admin-resource-empty">资源加载中...</div>
        <div v-else-if="pagedResources.length === 0" class="admin-resource-empty">
          <el-empty description="暂无匹配资源" />
        </div>
        <template v-else>
          <div class="admin-resource-table-scroll">
            <table class="admin-resource-table design">
              <thead>
                <tr>
                  <th class="check-col">
                    <el-checkbox :model-value="allCurrentSelected" :indeterminate="partCurrentSelected" @change="toggleAllCurrent" />
                  </th>
                  <th>序号</th>
                  <th>封面</th>
                  <th>名称</th>
                  <th>分类</th>
                  <th>所属专业</th>
                  <th>所属课程</th>
                  <th>是否公开</th>
                  <th>上传日期</th>
                  <th>上传人</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, index) in pagedResources" :key="row.resourceId" :class="{ selected: selectedIds.includes(row.resourceId) }">
                  <td class="check-col">
                    <el-checkbox :model-value="selectedIds.includes(row.resourceId)" @change="toggleOne(row.resourceId)" />
                  </td>
                  <td>{{ (page - 1) * pageSize + index + 1 }}</td>
                  <td>
                    <img class="admin-resource-cover-thumb" :src="row.coverResolved" :alt="row.resourceName" />
                  </td>
                  <td class="admin-resource-title-cell">{{ row.resourceName }}</td>
                  <td>
                    <span class="admin-resource-type-pill" :class="row.typeTone">{{ row.typeLabel }}</span>
                  </td>
                  <td class="wrap-cell">{{ row.majorLabel }}</td>
                  <td class="wrap-cell">{{ row.courseName || '-' }}</td>
                  <td>
                    <span class="admin-resource-public-text" :class="row.statusTone">{{ row.statusLabel }}</span>
                  </td>
                  <td>{{ row.createdAtLabel }}</td>
                  <td>{{ row.uploaderName || '-' }}</td>
                  <td>
                    <div class="admin-resource-row-actions design">
                      <el-button class="plain" @click="openPreview(row)">预览</el-button>
                      <el-button v-if="can('update')" class="plain" @click="openEditPanel(row)">编辑</el-button>
                      <el-button v-if="can('delete')" class="plain" :loading="busyId === row.resourceId" @click="deleteResource(row)">删除</el-button>
                      <el-button v-if="can('update') && (row.publicStatus === 'DRAFT' || row.publicStatus === 'REJECTED')" class="warn" @click="applyPublic(row)">申请公开</el-button>
                      <el-button v-else-if="can('update') && row.publicStatus === 'PUBLISHED' && row.currentVersion !== row.publicVersion" class="warn" @click="applyPublic(row)">申请公开最新版</el-button>
                      <el-button class="log" @click="openLogs(row)">操作日志</el-button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <footer class="admin-resource-footer design">
            <p>显示 <b>{{ pageStart }}</b> 到 <b>{{ pageEnd }}</b> 条，共 <b>{{ totalCount }}</b> 条记录</p>
            <el-pagination
              v-model:current-page="page"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50]"
              :total="totalCount"
              layout="prev, pager, next, sizes"
              background
            />
          </footer>
        </template>
      </section>

      <el-dialog v-model="resourceFormVisible" class="admin-resource-design-dialog" width="600px" :show-close="false" :close-on-click-modal="false" append-to-body>
        <template #header>
          <div class="admin-resource-design-dialog-head">
            <span v-if="panelMode === 'create'" class="admin-resource-dialog-icon upload">
              <el-icon><UploadFilled /></el-icon>
            </span>
            <div>
              <strong>{{ panelMode === 'create' ? '上传资源' : '编辑资源' }}</strong>
              <p v-if="panelMode === 'create'">请填写资源信息并上传文件</p>
            </div>
            <el-button text circle :icon="Close" @click="closePanel" />
          </div>
        </template>

        <div class="admin-resource-upload-form">
          <label class="admin-resource-modal-field">
            <span>资源名称 <b>*</b></span>
            <el-input v-model="form.resourceName" maxlength="20" show-word-limit placeholder="请输入资源名称" />
          </label>

          <div class="admin-resource-modal-field">
            <span>封面图 <b>*</b></span>
            <div v-if="form.coverUrl" class="admin-resource-file-card cover" @click.stop>
              <img :src="form.coverUrl" alt="封面图" />
              <div>
                <strong>{{ form.coverName || '-' }}</strong>
                <p>{{ form.coverSize || '-' }}</p>
              </div>
              <el-button text circle :icon="Delete" @click.stop="clearCover" />
            </div>
            <button v-else type="button" class="admin-resource-upload-drop cover" :disabled="uploadingCover" @click="coverInput?.click()" @dragover.prevent @drop.prevent="uploadCoverDrop">
              <el-icon><Picture /></el-icon>
              <strong>{{ uploadingCover ? '封面上传中...' : '点击或拖拽上传封面图' }}</strong>
              <span>支持 JPG、PNG 格式，大小不超过 5MB</span>
            </button>
            <input ref="coverInput" class="admin-resource-hidden-file" type="file" accept="image/jpeg,image/png,image/jpg" hidden @change="uploadCoverFile" />
          </div>

          <div class="admin-resource-modal-field">
            <span>资源内容 <b>*</b></span>
            <div v-if="form.fileName" class="admin-resource-file-card content" @click.stop>
              <span class="admin-resource-file-icon">
                <el-icon><Document /></el-icon>
              </span>
              <div>
                <strong>{{ form.fileName }}</strong>
                <p>{{ form.fileSizeLabel || form.fileSize || '-' }} <i></i> 上传完成 <em>✓</em></p>
              </div>
              <el-button text circle :icon="Delete" @click.stop="clearFile" />
            </div>
            <button v-else type="button" class="admin-resource-upload-drop content" :disabled="uploadingFile" @click="fileInput?.click()" @dragover.prevent @drop.prevent="uploadResourceDrop">
              <el-icon><UploadFilled /></el-icon>
              <strong>{{ uploadingFile ? '资源上传中...' : '点击或拖拽上传资源文件' }}</strong>
              <span>支持 PDF、Word、PPT、视频等多种格式，大小不超过 200MB</span>
            </button>
            <input ref="fileInput" class="admin-resource-hidden-file" type="file" hidden @change="uploadResourceFile" />
          </div>

          <label class="admin-resource-modal-field">
            <span>所属专业 <b>*</b></span>
            <el-select v-model="form.majorId" placeholder="请选择所属专业" filterable>
              <el-option v-for="item in majorOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </label>

          <label class="admin-resource-modal-field">
            <span>所属课程</span>
            <el-input v-model="form.courseName" maxlength="30" show-word-limit placeholder="请输入所属课程名称" />
          </label>
        </div>

        <template #footer>
          <div class="admin-resource-design-footer">
            <el-button @click="closePanel">取消</el-button>
            <el-button type="primary" :loading="saving" @click="saveResource">
              <el-icon v-if="panelMode === 'create'"><UploadFilled /></el-icon>
              {{ panelMode === 'create' ? '确认上传' : '确认' }}
            </el-button>
          </div>
        </template>
      </el-dialog>

      <el-dialog v-model="previewVisible" class="admin-resource-preview-dialog" width="820px" :show-close="false" append-to-body>
        <template #header>
          <div class="admin-resource-preview-head">
            <strong>{{ detailResource?.resourceName || '资源预览' }}</strong>
            <el-button text circle :icon="Close" @click="previewVisible = false" />
          </div>
        </template>

        <section class="admin-resource-preview-content">
          <div class="admin-resource-preview-document"><AdminResourcePreview :resource="detailResource" /></div>
        </section>

        <template #footer>
          <div class="admin-resource-preview-footer">
            <el-button type="primary" @click="downloadResource">下载</el-button>
          </div>
        </template>
      </el-dialog>

      <el-drawer v-model="detailVisible" class="admin-resource-drawer" direction="rtl" size="720px" :with-header="false">
        <div class="admin-drawer-head">
          <div>
            <p>操作日志</p>
            <h3>{{ detailResource?.resourceName || '资源日志' }}</h3>
          </div>
          <el-button text :icon="Close" @click="detailVisible = false" />
        </div>

        <section class="admin-resource-detail-card admin-resource-log-card">
          <p>操作日志</p>
          <div v-if="detailLogs.length === 0" class="admin-resource-log-empty">暂无日志</div>
          <table v-else class="admin-resource-log-table">
            <thead><tr><th>操作人</th><th>操作类型</th><th>操作内容</th><th>操作时间</th></tr></thead>
            <tbody><tr v-for="item in detailLogs" :key="item.logId"><td>{{ item.operatorName || '-' }}</td><td>{{ resourceLogAction(item.action) }}</td><td>{{ item.content || '-' }}</td><td>{{ formatDateTime(item.createdAt) }}</td></tr></tbody>
          </table>
        </section>
      </el-drawer>

      <el-dialog v-model="batchEditVisible" class="admin-resource-design-dialog" width="600px" :show-close="false" append-to-body>
        <template #header>
          <div class="admin-resource-design-dialog-head">
            <div>
              <strong>批量设置</strong>
              <p>你可以设置以下一项或多项</p>
            </div>
            <el-button text circle :icon="Close" @click="batchEditVisible = false" />
          </div>
        </template>

        <div class="admin-resource-upload-form batch">
          <label class="admin-resource-modal-field">
            <span>封面图</span>
            <button type="button" class="admin-resource-upload-drop cover" :disabled="uploadingBatchCover" @click="batchCoverInput?.click()" @dragover.prevent @drop.prevent="uploadBatchCoverDrop">
              <el-icon><Picture /></el-icon>
              <strong>{{ uploadingBatchCover ? '封面上传中...' : batchForm.coverName || '点击或拖拽上传封面图' }}</strong>
              <span>{{ batchForm.coverSize || '支持 JPG、PNG 格式，大小不超过 5MB' }}</span>
            </button>
            <input ref="batchCoverInput" class="admin-resource-hidden-file" type="file" accept="image/jpeg,image/png,image/jpg" hidden @change="uploadBatchCoverFile" />
          </label>
          <label class="admin-resource-modal-field">
            <span>所属专业</span>
            <el-select v-model="batchForm.majorId" placeholder="请选择所属专业" filterable>
              <el-option v-for="item in majorOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </label>
          <label class="admin-resource-modal-field">
            <span>所属课程</span>
            <el-input v-model="batchForm.courseName" maxlength="30" show-word-limit placeholder="请输入所属课程名称" />
          </label>
        </div>

        <template #footer>
          <div class="admin-resource-design-footer">
            <el-button @click="batchEditVisible = false">取消</el-button>
            <el-button type="primary" :loading="saving" @click="saveBatchEdit">确认</el-button>
          </div>
        </template>
      </el-dialog>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Close, Delete, Document, Picture, Plus, Search, Setting, UploadFilled } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import AdminResourcePreview from '../../components/admin/AdminResourcePreview.vue';
import {
  batchUpdateAdminResources,
  createAdminResource,
  deleteAdminResources,
  fetchAdminResource,
  fetchAdminResourceLogs,
  fetchAdminResources,
  submitAdminResourcePublicApplication,
  updateAdminResource,
  uploadAdminFile,
  type AdminResource,
  type AdminResourceCommand,
  type AdminResourceLog,
  type AdminResourceQuery
} from '../../api/admin-resource';
import { resolvePublicUrl } from '../../api/http';
import { coverForResourceType } from '../../features/student/resources';
import { fetchAdminMajors } from '../../api/admin-settings';
import { fileFromDrop, validateResourceUpload } from '../../features/admin/resource-upload';
import { useAdminPermissions } from '../../features/admin/use-admin-permissions';

type ResourceStatus = 'DRAFT' | 'REVIEWING' | 'PUBLISHED' | 'REJECTED';
type PanelMode = 'create' | 'edit';

interface ResourceOption {
  label: string;
  value: string;
}

interface MajorOption {
  label: string;
  value: number;
}

interface ResourceRow extends AdminResource {
  coverResolved: string;
  typeLabel: string;
  typeTone: string;
  statusLabel: string;
  statusTone: 'draft' | 'reviewing' | 'published' | 'rejected';
  majorLabel: string;
  fileSizeLabel: string;
  updatedAtLabel: string;
  createdAtLabel: string;
}

interface ResourceForm {
  resourceName: string;
  resourceType: string;
  majorId: number | null;
  courseName: string;
  uploaderName: string;
  coverUrl: string;
  fileUrl: string;
  previewUrl: string;
  fileName: string;
  fileSize: string;
  fileSizeLabel: string;
  coverName: string;
  coverSize: string;
  publicStatus: ResourceStatus;
  currentVersion: string;
  publicVersion: string;
}

const pageSize = ref(10);
const loading = ref(false);
const saving = ref(false);
const page = ref(1);
const resources = ref<ResourceRow[]>([]);
const detailResource = ref<ResourceRow | null>(null);
const detailLogs = ref<AdminResourceLog[]>([]);
const detailVisible = ref(false);
const resourceFormVisible = ref(false);
const previewVisible = ref(false);
const panelMode = ref<PanelMode>('create');
const editingId = ref<number | null>(null);
const busyId = ref<number | null>(null);
const batchEditVisible = ref(false);
const selectedIds = ref<number[]>([]);
const coverInput = ref<HTMLInputElement | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);
const batchCoverInput = ref<HTMLInputElement | null>(null);
const uploadingCover = ref(false);
const uploadingFile = ref(false);
const uploadingBatchCover = ref(false);
const { can } = useAdminPermissions('resource:personal');

const draft = reactive({
  keyword: '',
  resourceType: '',
  majorId: null as number | null,
  courseName: '',
  publicStatus: '' as '' | ResourceStatus,
  uploadDateRange: [] as string[]
});

const majorOptions = ref<MajorOption[]>([]);

const resourceTypeOptions: ResourceOption[] = [
  { label: '文本文档', value: 'DOCUMENT' },
  { label: '演示文稿', value: 'PRESENTATION' },
  { label: '图片', value: 'IMAGE' },
  { label: '音频', value: 'AUDIO' },
  { label: '视频', value: 'VIDEO' }
];

const appliedFilters = ref({ ...draft });
const form = reactive<ResourceForm>(createEmptyForm());
const batchForm = reactive({
  majorId: null as number | null,
  courseName: '',
  coverUrl: '',
  coverName: '',
  coverSize: ''
});

const totalCount = ref(0);
const pageStart = computed(() => (totalCount.value === 0 ? 0 : (page.value - 1) * pageSize.value + 1));
const pageEnd = computed(() => Math.min(page.value * pageSize.value, totalCount.value));
const pagedResources = computed(() => resources.value);
const allCurrentSelected = computed(() => pagedResources.value.length > 0 && pagedResources.value.every((item) => selectedIds.value.includes(item.resourceId)));
const partCurrentSelected = computed(() => selectedIds.value.length > 0 && !allCurrentSelected.value);
function createEmptyForm(): ResourceForm {
  return {
    resourceName: '',
    resourceType: '',
    majorId: null,
    courseName: '',
    uploaderName: '',
    coverUrl: '',
    fileUrl: '',
    previewUrl: '',
    fileName: '',
    fileSize: '',
    fileSizeLabel: '',
    coverName: '',
    coverSize: '',
    publicStatus: 'DRAFT',
    currentVersion: '',
    publicVersion: ''
  };
}


function mapResourceRow(resource: AdminResource): ResourceRow {
  const typeLabel = normalizeResourceTypeLabel(resource.resourceType);
  const status = normalizeStatus(resource.publicStatus);
  const majorLabel = resource.majorName || findMajorLabel(resource.majorId) || '-';
  const fileSizeLabel = formatFileSize(resource.fileSize);
  const updatedAtLabel = formatDateTime(resource.updatedAt);
  const resolvedCoverUrl = resolvePublicUrl(resource.coverUrl);
  return {
    ...resource,
    resourceType: typeLabel,
    coverUrl: resolvedCoverUrl || coverForResourceType(typeLabel),
    coverResolved: resolvedCoverUrl || coverForResourceType(typeLabel),
    fileUrl: resolvePublicUrl(resource.fileUrl),
    previewUrl: resolvePublicUrl(resource.previewUrl),
    typeLabel,
    typeTone: typeTone(typeLabel),
    statusLabel: statusLabels[status],
    statusTone: statusTone(status),
    majorLabel,
    fileSizeLabel,
    updatedAtLabel,
    createdAtLabel: formatDateTime(resource.createdAt),
    publicStatus: status
  };
}

function normalizeStatus(value?: string): ResourceStatus {
  const upper = String(value || '').toUpperCase();
  if (upper === 'PUBLISHED' || upper === 'PUBLIC' || upper === '已公示') {
    return 'PUBLISHED';
  }

  if (upper === 'REVIEWING' || upper === 'PENDING' || upper === 'REVIEW' || upper === '审核中' || upper === 'APPROVING') {
    return 'REVIEWING';
  }

  if (upper === 'REJECTED' || upper === '驳回' || upper === '审核驳回' || upper === '已驳回') {
    return 'REJECTED';
  }

  return 'DRAFT';
}

function statusTone(status: ResourceStatus): 'draft' | 'reviewing' | 'published' | 'rejected' {
  if (status === 'PUBLISHED') {
    return 'published';
  }

  if (status === 'REVIEWING') {
    return 'reviewing';
  }

  if (status === 'REJECTED') {
    return 'rejected';
  }

  return 'draft';
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

const statusLabels: Record<ResourceStatus, string> = {
  DRAFT: '未公开',
  REVIEWING: '申请中',
  PUBLISHED: '已公开',
  REJECTED: '审核驳回'
};

function normalizeResourceTypeCode(type?: string) {
  const raw = String(type || '').trim().toUpperCase();
  const codes: Record<string, string> = { 文本文档: 'DOCUMENT', 演示文稿: 'PRESENTATION', 图片: 'IMAGE', 图像: 'IMAGE', 音频: 'AUDIO', 视频: 'VIDEO' };
  return codes[type || ''] || raw;
}

function normalizeResourceTypeLabel(type?: string) {
  const labels: Record<string, string> = { DOCUMENT: '文本文档', PRESENTATION: '演示文稿', IMAGE: '图片', AUDIO: '音频', VIDEO: '视频' };
  return labels[normalizeResourceTypeCode(type)] || type || '资源';
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

function resourceLogAction(action?: string) {
  const labels: Record<string, string> = {
    CREATE: '新增资源', UPDATE: '编辑资源', DELETE: '删除资源', APPLY_PUBLIC: '申请公开',
    APPROVE_PUBLIC: '审核通过', REJECT_PUBLIC: '审核驳回', BATCH_UPDATE: '批量修改'
  };
  return labels[String(action || '').toUpperCase()] || action || '-';
}

function findMajorLabel(majorId?: number | null) {
  return majorOptions.value.find((item) => item.value === majorId)?.label || '';
}

async function loadMajorOptions() {
  try {
    majorOptions.value = (await fetchAdminMajors())
      .filter((item) => item.enabled)
      .map((item) => ({ label: item.majorName, value: item.majorId }));
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '专业数据加载失败');
  }
}

function resetSelection() {
  selectedIds.value = [];
}

function openCreatePanel() {
  panelMode.value = 'create';
  editingId.value = null;
  Object.assign(form, createEmptyForm());
  resourceFormVisible.value = true;
}

function closePanel() {
  resourceFormVisible.value = false;
}

function openEditPanel(row: ResourceRow) {
  panelMode.value = 'edit';
  editingId.value = row.resourceId;
  resourceFormVisible.value = true;
  Object.assign(form, {
    resourceName: row.resourceName,
    resourceType: row.resourceType || '',
    majorId: row.majorId ?? null,
    courseName: row.courseName || '',
    uploaderName: row.uploaderName || '',
    coverUrl: row.coverUrl || '',
    coverName: row.coverUrl ? row.coverUrl.split('/').pop() || '-' : '',
    coverSize: '',
    fileUrl: row.fileUrl || '',
    previewUrl: row.previewUrl || '',
    fileName: row.fileName || '',
    fileSize: row.fileSize ? String(row.fileSize) : '',
    fileSizeLabel: row.fileSizeLabel,
    publicStatus: row.publicStatus || 'DRAFT',
    currentVersion: row.currentVersion ? String(row.currentVersion) : '',
    publicVersion: row.publicVersion ? String(row.publicVersion) : ''
  });
}

function openBatchEdit() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择资源');
    return;
  }

  batchForm.majorId = null;
  batchForm.courseName = '';
  batchForm.coverUrl = '';
  batchForm.coverName = '';
  batchForm.coverSize = '';
  batchEditVisible.value = true;
}

function applyFilters() {
  appliedFilters.value = {
    keyword: draft.keyword,
    resourceType: draft.resourceType,
    majorId: draft.majorId,
    courseName: draft.courseName,
    publicStatus: draft.publicStatus,
    uploadDateRange: [...draft.uploadDateRange]
  };
  page.value = 1;
  void loadResources();
}

function resetFilters() {
  draft.keyword = '';
  draft.resourceType = '';
  draft.majorId = null;
  draft.courseName = '';
  draft.publicStatus = '';
  draft.uploadDateRange = [];
  appliedFilters.value = {
    keyword: '',
    resourceType: '',
    majorId: null,
    courseName: '',
    publicStatus: '',
    uploadDateRange: []
  };
  page.value = 1;
  void loadResources();
}

function toggleAllCurrent(value: string | number | boolean) {
  if (!value) {
    selectedIds.value = selectedIds.value.filter((id) => !pagedResources.value.some((item) => item.resourceId === id));
    return;
  }

  const ids = new Set(selectedIds.value);
  pagedResources.value.forEach((item) => ids.add(item.resourceId));
  selectedIds.value = Array.from(ids);
}

function toggleOne(resourceId: number) {
  selectedIds.value = selectedIds.value.includes(resourceId)
    ? selectedIds.value.filter((id) => id !== resourceId)
    : [...selectedIds.value, resourceId];
}

function validateForm(): AdminResourceCommand {
  const resourceName = form.resourceName.trim();
  const majorId = Number(form.majorId);
  const coverUrl = form.coverUrl.trim();
  const fileUrl = form.fileUrl.trim();
  const fileName = form.fileName.trim();
  const fileSize = Number(form.fileSize);
  const currentVersion = form.currentVersion ? Number(form.currentVersion) : undefined;
  const publicVersion = form.publicVersion ? Number(form.publicVersion) : undefined;

  if (!resourceName) {
    throw new Error('请输入资源名称');
  }

  if (!majorId) {
    throw new Error('请选择所属专业');
  }

  if (!coverUrl) {
    throw new Error('请上传封面图');
  }

  if (!fileUrl) {
    throw new Error('请上传资源文件');
  }

  if (!fileName) {
    throw new Error('请上传资源文件');
  }

  if (!Number.isFinite(fileSize) || fileSize <= 0) {
    throw new Error('请输入有效的文件大小');
  }

  return {
    resourceName,
    coverUrl,
    fileUrl,
    previewUrl: form.previewUrl.trim() || undefined,
    fileName,
    fileSize,
    majorId,
    courseName: form.courseName.trim() || undefined,
    uploaderName: form.uploaderName.trim() || undefined,
    publicStatus: form.publicStatus,
    currentVersion,
    publicVersion
  } as AdminResourceCommand & { uploaderName?: string; resourceType?: string; publicStatus?: ResourceStatus; currentVersion?: number; publicVersion?: number };
}

async function saveResource() {
  let payload: AdminResourceCommand & { uploaderName?: string; resourceType?: string; publicStatus?: ResourceStatus; currentVersion?: number; publicVersion?: number };
  try {
    payload = validateForm();
  } catch (error) {
    ElMessage.warning(error instanceof Error ? error.message : '请完善资源信息');
    return;
  }

  saving.value = true;
  try {
    if (panelMode.value === 'edit' && editingId.value) {
      await updateAdminResource(editingId.value, payload);
      ElMessage.success('资源已更新');
    } else {
      await createAdminResource(payload);
      ElMessage.success('资源已新增');
    }
    await loadResources();
    resourceFormVisible.value = false;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function loadResources() {
  loading.value = true;
  try {
    const query: AdminResourceQuery = {
      keyword: appliedFilters.value.keyword.trim() || undefined,
      resourceType: appliedFilters.value.resourceType || undefined,
      majorId: appliedFilters.value.majorId ?? undefined,
      courseName: appliedFilters.value.courseName.trim() || undefined,
      publicStatus: appliedFilters.value.publicStatus || undefined,
      uploadStartDate: appliedFilters.value.uploadDateRange[0],
      uploadEndDate: appliedFilters.value.uploadDateRange[1],
      page: page.value,
      pageSize: pageSize.value
    };
    const result = await fetchAdminResources(query);
    resources.value = result.records.map(mapResourceRow);
    totalCount.value = result.total;
  } catch (error) {
    resources.value = [];
    totalCount.value = 0;
    ElMessage.error(error instanceof Error ? error.message : '个人资源库加载失败');
  } finally {
    loading.value = false;
    resetSelection();
  }
}

async function openDetail(row: ResourceRow) {
  try {
    detailResource.value = mapResourceRow(await fetchAdminResource(row.resourceId));
  } catch {
    detailResource.value = row;
  }
}

async function openPreview(row: ResourceRow) {
  await openDetail(row);
  previewVisible.value = true;
}

async function openLogs(row: ResourceRow) {
  busyId.value = row.resourceId;
  try {
    detailResource.value = row;
    detailLogs.value = await fetchAdminResourceLogs(row.resourceId);
    detailVisible.value = true;
  } catch {
    detailLogs.value = [];
  } finally {
    busyId.value = null;
  }
}

async function applyPublic(row: ResourceRow) {
  try {
    await ElMessageBox.confirm(`确认提交资源「${row.resourceName}」公示申请？`, '公示申请', {
      confirmButtonText: '提交',
      cancelButtonText: '取消',
      type: 'warning'
    });
  } catch {
    return;
  }

  busyId.value = row.resourceId;
  try {
    await submitAdminResourcePublicApplication(row.resourceId);
    row.publicStatus = 'REVIEWING';
    row.statusLabel = statusLabels.REVIEWING;
    row.statusTone = 'reviewing';
    ElMessage.success('公示申请已提交');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '提交失败');
  } finally {
    busyId.value = null;
  }
}

async function deleteResource(row: ResourceRow) {
  try {
    await ElMessageBox.confirm('确定删除吗？若该资源已被同步到了公开库，不会影响公开库继续展示', `删除资源「${row.resourceName}」`, {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    });
  } catch {
    return;
  }

  busyId.value = row.resourceId;
  try {
    await deleteAdminResources([row.resourceId]);
    resources.value = resources.value.filter((item) => item.resourceId !== row.resourceId);
    selectedIds.value = selectedIds.value.filter((id) => id !== row.resourceId);
    ElMessage.success('资源已删除');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '删除失败');
  } finally {
    busyId.value = null;
  }
}

async function batchDeleteResources() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择资源');
    return;
  }

  try {
    await ElMessageBox.confirm('确定删除吗？若资源已被同步到了公开库，不会影响公开库继续展示', `批量删除 ${selectedIds.value.length} 条资源`, {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    });
  } catch {
    return;
  }

  try {
    await deleteAdminResources(selectedIds.value);
    resources.value = resources.value.filter((item) => !selectedIds.value.includes(item.resourceId));
    selectedIds.value = [];
    ElMessage.success('资源已删除');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '批量删除失败');
  }
}

async function saveBatchEdit() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择资源');
    return;
  }

  saving.value = true;
  try {
    await batchUpdateAdminResources({
      resourceIds: selectedIds.value,
      coverUrl: batchForm.coverUrl || undefined,
      majorId: batchForm.majorId ?? undefined,
      courseName: batchForm.courseName.trim() || undefined
    });
    batchEditVisible.value = false;
    ElMessage.success('批量修改成功');
    await loadResources();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '批量修改失败');
  } finally {
    saving.value = false;
  }
}

async function uploadCoverFile(event: Event) {
  const file = firstSelectedFile(event);
  if (!file) {
    return;
  }
  const validation = validateResourceUpload(file, 'cover');
  if (validation) { ElMessage.warning(validation); clearFileInput(event); return; }
  uploadingCover.value = true;
  try {
    const uploaded = await uploadAdminFile(file, 'covers');
    if (!uploaded.fileUrl) {
      throw new Error('上传接口未返回文件地址');
    }
    form.coverUrl = uploaded.fileUrl;
    form.coverName = uploaded.fileName || file.name;
    form.coverSize = formatFileSize(uploaded.fileSize ?? file.size);
    ElMessage.success('封面图已上传');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '封面上传失败');
  } finally {
    uploadingCover.value = false;
    clearFileInput(event);
  }
}

async function uploadResourceFile(event: Event) {
  const file = firstSelectedFile(event);
  if (!file) {
    return;
  }
  const validation = validateResourceUpload(file);
  if (validation) { ElMessage.warning(validation); clearFileInput(event); return; }
  uploadingFile.value = true;
  try {
    const uploaded = await uploadAdminFile(file, 'resources');
    if (!uploaded.fileUrl) {
      throw new Error('上传接口未返回文件地址');
    }
    form.fileUrl = uploaded.fileUrl;
    form.previewUrl = uploaded.fileUrl;
    form.fileName = uploaded.fileName || file.name;
    form.fileSize = String(uploaded.fileSize ?? file.size);
    form.fileSizeLabel = formatFileSize(uploaded.fileSize ?? file.size);
    ElMessage.success('资源文件已上传');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '资源文件上传失败');
  } finally {
    uploadingFile.value = false;
    clearFileInput(event);
  }
}

async function uploadBatchCoverFile(event: Event) {
  const file = firstSelectedFile(event);
  if (!file) {
    return;
  }
  const validation = validateResourceUpload(file, 'cover');
  if (validation) { ElMessage.warning(validation); clearFileInput(event); return; }
  uploadingBatchCover.value = true;
  try {
    const uploaded = await uploadAdminFile(file, 'covers');
    if (!uploaded.fileUrl) {
      throw new Error('上传接口未返回文件地址');
    }
    batchForm.coverUrl = uploaded.fileUrl;
    batchForm.coverName = uploaded.fileName || file.name;
    batchForm.coverSize = formatFileSize(uploaded.fileSize ?? file.size);
    ElMessage.success('批量封面已上传');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '批量封面上传失败');
  } finally {
    uploadingBatchCover.value = false;
    clearFileInput(event);
  }
}

function uploadCoverDrop(event: DragEvent) {
  const file = fileFromDrop(event);
  if (file) void uploadCoverFile({ target: { files: [file] } } as unknown as Event);
}

function uploadResourceDrop(event: DragEvent) {
  const file = fileFromDrop(event);
  if (file) void uploadResourceFile({ target: { files: [file] } } as unknown as Event);
}

function uploadBatchCoverDrop(event: DragEvent) {
  const file = fileFromDrop(event);
  if (file) void uploadBatchCoverFile({ target: { files: [file] } } as unknown as Event);
}

function firstSelectedFile(event: Event) {
  return (event.target as HTMLInputElement | null)?.files?.[0] || null;
}

function clearFileInput(event: Event) {
  const input = event.target as HTMLInputElement | null;
  if (input) {
    input.value = '';
  }
}

function clearCover() {
  form.coverUrl = '';
  form.coverName = '';
  form.coverSize = '';
}

function clearFile() {
  form.fileUrl = '';
  form.previewUrl = '';
  form.fileName = '';
  form.fileSize = '';
  form.fileSizeLabel = '';
}

function downloadResource() {
  if (detailResource.value?.fileUrl) {
    window.open(detailResource.value.fileUrl, '_blank', 'noopener');
    return;
  }

  ElMessage.info('正在下载资源');
}

watch(
  () => form.resourceType,
  (nextType) => {
    if (!form.coverUrl) {
      form.coverUrl = coverForResourceType(nextType);
    }
  }
);

watch([page, pageSize], () => {
  resetSelection();
  void loadResources();
});

onMounted(() => {
  void loadMajorOptions();
  void loadResources();
});
</script>
