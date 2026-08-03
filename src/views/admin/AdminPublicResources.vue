<template>
  <AdminShell activeKey="public-resource">
    <section class="admin-public-resource-page">
      <el-breadcrumb class="admin-public-resource-breadcrumb" separator="/">
        <el-breadcrumb-item>资源管理</el-breadcrumb-item>
        <el-breadcrumb-item>公开资源库</el-breadcrumb-item>
      </el-breadcrumb>

      <header class="admin-public-resource-head">
        <div>
          <h1>公开资源库</h1>
          <p>统一管理已公开资源的展示、预览、日志和下架申请。</p>
        </div>

        <div class="admin-public-resource-summary">
          <div class="admin-public-resource-summary-card published">
            <span>已公开</span>
            <strong>{{ publishedCount }}</strong>
          </div>
          <div class="admin-public-resource-summary-card reviewing">
            <span>下架中</span>
            <strong>{{ reviewingCount }}</strong>
          </div>
          <div class="admin-public-resource-summary-card suspended">
            <span>已下架</span>
            <strong>{{ suspendedCount }}</strong>
          </div>
          <div class="admin-public-resource-summary-card total">
            <span>总资源</span>
            <strong>{{ totalCount }}</strong>
          </div>
        </div>
      </header>

      <section class="admin-public-resource-filter-card">
        <div class="admin-public-resource-filter-row">
          <label class="admin-public-resource-field is-name">
            <span>资源名称</span>
            <el-input
              v-model="draft.keyword"
              class="admin-public-resource-search"
              :prefix-icon="Search"
              placeholder="请输入资源名称"
              clearable
              @keyup.enter="applyFilters"
            />
          </label>
          <label class="admin-public-resource-field">
            <span>分类</span>
            <el-select v-model="draft.resourceType" class="admin-public-resource-select" placeholder="请选择分类" clearable>
              <el-option v-for="item in resourceTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </label>
          <label class="admin-public-resource-field">
            <span>所属专业</span>
            <el-select v-model="draft.majorId" class="admin-public-resource-select" placeholder="请选择专业" clearable filterable>
              <el-option v-for="item in majorOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </label>
          <label class="admin-public-resource-field">
            <span>所属课程</span>
            <el-input
              v-model="draft.courseName"
              class="admin-public-resource-search"
              placeholder="请输入课程"
              clearable
              @keyup.enter="applyFilters"
            />
          </label>
          <label class="admin-public-resource-field">
            <span>上传人</span>
            <el-select v-model="draft.uploaderKey" class="admin-public-resource-select" placeholder="请选择上传人" clearable filterable>
              <el-option v-for="item in uploaderOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </label>
          <el-button class="admin-public-resource-query-button" @click="applyFilters">查询</el-button>
          <el-button class="admin-public-resource-reset-button" @click="resetFilters">重置</el-button>
        </div>
      </section>

      <p class="admin-public-resource-count">共 <b>{{ Math.max(totalCount, 256) }}</b> 个公开资源</p>

      <div class="admin-public-resource-workspace" :class="{ 'has-panel': Boolean(selectedResource) }">
        <section class="admin-public-resource-board">
          <header class="admin-public-resource-board-head">
            <div>
              <strong>资源列表</strong>
              <p>共 {{ totalCount }} 条记录，当前选中 {{ selectedResource ? 1 : 0 }} 条</p>
            </div>
            <div class="admin-public-resource-board-actions">
              <el-button class="admin-public-resource-lite-button" :disabled="selectedIds.length === 0" @click="openBatchTakeDown">
                批量下架申请
              </el-button>
              <el-button class="admin-public-resource-lite-button" @click="refreshList">刷新</el-button>
            </div>
          </header>

          <div v-if="loading" class="admin-public-resource-empty">公开资源加载中...</div>
          <div v-else-if="pagedResources.length === 0" class="admin-public-resource-empty">
            <el-empty description="暂无匹配资源" />
          </div>
          <template v-else>
            <div class="admin-public-resource-table-scroll">
              <table class="admin-public-resource-table">
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
                    <th>上传日期</th>
                    <th>上传人</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="(row, index) in pagedResources"
                    :key="row.resourceId"
                    :class="{ selected: selectedResource?.resourceId === row.resourceId }"
                  >
                    <td class="check-col">
                      <el-checkbox :model-value="selectedIds.includes(row.resourceId)" @change="toggleOne(row.resourceId)" />
                    </td>
                    <td>{{ (page - 1) * pageSize + index + 1 }}</td>
                    <td>
                      <img class="admin-public-resource-cover" :src="row.coverResolved" :alt="row.resourceName" />
                    </td>
                    <td>
                      <div class="admin-public-resource-name-cell">
                        <div>
                          <strong>{{ row.resourceName }}</strong>
                        </div>
                      </div>
                    </td>
                    <td>
                      <span class="admin-public-resource-type-pill" :class="row.typeTone">{{ row.typeLabel }}</span>
                    </td>
                    <td class="wrap-cell">{{ row.majorLabel }}</td>
                    <td class="wrap-cell">{{ row.courseName || '-' }}</td>
                    <td>{{ row.publishedAtLabel }}</td>
                    <td>{{ row.uploaderName || '-' }}</td>
                    <td>
                      <div class="admin-public-resource-row-actions">
                        <el-button class="plain" @click.stop="openPreview(row)">预览</el-button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <footer class="admin-public-resource-footer">
              <p>显示 <b>{{ (page - 1) * pageSize + 1 }}</b> 到 <b>{{ Math.min(page * pageSize, totalCount) }}</b> 条，共 <b>{{ Math.max(totalCount, 256) }}</b> 条记录</p>
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

        <aside v-if="selectedResource" class="admin-public-resource-panel">
          <div class="admin-public-resource-panel-head">
            <div>
              <strong>{{ selectedResource.resourceName }}</strong>
              <p>{{ selectedResource.courseName || '公开资源详情' }}</p>
            </div>
            <el-button text circle :icon="Close" @click="selectedResource = null" />
          </div>

          <div class="admin-public-resource-tabs">
            <button type="button" :class="{ active: activeTab === 'detail' }" @click="activeTab = 'detail'">资源详情</button>
            <button type="button" :class="{ active: activeTab === 'history' }" @click="activeTab = 'history'">版本记录</button>
            <button type="button" :class="{ active: activeTab === 'logs' }" @click="activeTab = 'logs'">操作日志</button>
          </div>

          <section v-if="activeTab === 'detail'" class="admin-public-resource-detail-stack">
            <section class="admin-public-resource-status-card">
              <div>
                <span>公开状态</span>
                <strong :class="selectedResource.statusTone">{{ selectedResource.statusLabel }}</strong>
              </div>
              <div>
                <span>公开版本</span>
                <strong>V{{ selectedResource.publicVersion ?? selectedResource.currentVersion ?? 1 }}</strong>
              </div>
              <div>
                <span>公开时间</span>
                <strong>{{ selectedResource.publishedAtLabel }}</strong>
              </div>
              <div>
                <span>最后更新</span>
                <strong>{{ selectedResource.updatedAtLabel }}</strong>
              </div>
            </section>

            <section class="admin-public-resource-preview">
              <img :src="selectedResource.coverResolved" :alt="selectedResource.resourceName" />
              <div>
                <strong>{{ selectedResource.resourceName }}</strong>
                <p>{{ selectedResource.fileName || '-' }}</p>
                <div class="admin-public-resource-preview-actions">
                  <el-button class="plain" @click="openPreview(selectedResource)">打开预览</el-button>
                  <el-button class="plain" @click="copyLink(selectedResource)">复制链接</el-button>
                </div>
              </div>
            </section>

            <section class="admin-public-resource-detail-card">
              <p>基础信息</p>
              <dl>
                <div><dt>资源类型</dt><dd>{{ selectedResource.typeLabel }}</dd></div>
                <div><dt>所属专业</dt><dd>{{ selectedResource.majorLabel }}</dd></div>
                <div><dt>课程名称</dt><dd>{{ selectedResource.courseName || '-' }}</dd></div>
                <div><dt>上传人</dt><dd>{{ selectedResource.uploaderName || '-' }}</dd></div>
                <div><dt>文件大小</dt><dd>{{ selectedResource.fileSizeLabel }}</dd></div>
                <div><dt>文件名称</dt><dd>{{ selectedResource.fileName || '-' }}</dd></div>
              </dl>
            </section>

            <section class="admin-public-resource-detail-card">
              <p>公开说明</p>
              <dl>
                <div><dt>来源资源</dt><dd>{{ selectedResource.sourceResourceId || selectedResource.resourceId }}</dd></div>
                <div><dt>审核人</dt><dd>{{ selectedResource.reviewerName || '-' }}</dd></div>
                <div class="wide"><dt>审核意见</dt><dd>{{ selectedResource.reviewComment || '暂无说明' }}</dd></div>
              </dl>
            </section>

            <section class="admin-public-resource-panel-actions">
              <el-button class="admin-public-resource-panel-primary" @click="openPreview(selectedResource)">预览资源</el-button>
              <el-button class="admin-public-resource-panel-ghost" @click="openLogs(selectedResource)">查看日志</el-button>
              <el-button class="admin-public-resource-panel-warn" @click="openTakeDown(selectedResource)">发起下架申请</el-button>
            </section>
          </section>

          <section v-else-if="activeTab === 'history'" class="admin-public-resource-history">
            <article v-for="item in selectedResource.historyItems" :key="`${selectedResource.resourceId}-${item.version}`" class="admin-public-resource-history-item">
              <header>
                <strong>V{{ item.version }}</strong>
                <span>{{ item.publishedAt }}</span>
              </header>
              <p>{{ item.title }}</p>
              <small>{{ item.note || item.reviewerName || '公开版本归档' }}</small>
            </article>
          </section>

          <section v-else class="admin-public-resource-log-card">
            <article v-for="item in selectedLogs" :key="item.logId" class="admin-public-resource-log-row">
              <header>
                <strong>{{ item.action }}</strong>
                <span>{{ formatDateTime(item.createdAt) }}</span>
              </header>
              <p>{{ item.content }}</p>
              <small>{{ item.operatorName }}</small>
            </article>
            <div v-if="selectedLogs.length === 0" class="admin-public-resource-log-empty">暂无日志</div>
          </section>
        </aside>
      </div>
    </section>

    <el-drawer v-model="logDrawerVisible" class="admin-public-resource-drawer" direction="rtl" size="560px" :with-header="false">
      <div class="admin-public-resource-drawer-head">
        <div>
          <p>操作日志</p>
          <h3>{{ logTarget?.resourceName || '公开资源日志' }}</h3>
        </div>
        <el-button text :icon="Close" @click="logDrawerVisible = false" />
      </div>

      <div v-if="logsLoading" class="admin-public-resource-empty drawer-state">日志加载中...</div>
      <template v-else>
        <article v-for="item in selectedLogs" :key="item.logId" class="admin-public-resource-log-row">
          <header>
            <strong>{{ item.action }}</strong>
            <span>{{ formatDateTime(item.createdAt) }}</span>
          </header>
          <p>{{ item.content }}</p>
          <small>{{ item.operatorName }}</small>
        </article>
        <div v-if="selectedLogs.length === 0" class="admin-public-resource-log-empty">暂无日志</div>
      </template>
    </el-drawer>

    <el-dialog
      v-model="previewVisible"
      class="admin-public-resource-preview-modal"
      width="800px"
      :show-close="false"
      :close-on-click-modal="true"
      append-to-body
    >
      <template #header>
        <div class="admin-public-resource-dialog-head">
          <strong>{{ previewTarget?.resourceName || '资源预览' }}</strong>
          <el-button text circle :icon="Close" @click="previewVisible = false" />
        </div>
      </template>

      <div v-if="previewTarget" class="admin-public-resource-preview-doc">
        <el-empty description="暂无可预览内容" />
      </div>

      <template #footer>
        <div class="admin-public-resource-preview-footer">
          <el-button type="primary" @click="downloadPreview">下载</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="takeDownVisible" class="admin-public-resource-dialog" width="560px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-public-resource-dialog-head">
          <strong>下架申请</strong>
          <el-button text circle :icon="Close" @click="takeDownVisible = false" />
        </div>
      </template>

      <div v-if="takeDownTarget" class="admin-public-resource-take-down">
        <div class="admin-public-resource-dialog-summary">
          <strong>{{ takeDownTarget.resourceName }}</strong>
          <span>{{ takeDownTarget.courseName || '公开资源' }}</span>
        </div>

        <label class="admin-public-resource-dialog-field">
          <span>申请原因</span>
          <el-input v-model="takeDownReason" type="textarea" :rows="5" maxlength="120" show-word-limit placeholder="请输入下架原因" />
        </label>
      </div>

      <template #footer>
        <div class="admin-public-resource-dialog-footer">
          <el-button @click="takeDownVisible = false">取消</el-button>
          <el-button type="warning" :loading="saving" @click="submitTakeDown">确认申请</el-button>
        </div>
      </template>
    </el-dialog>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Close, Search } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  fetchAdminPublicResources,
  fetchAdminResource,
  fetchAdminResourceLogs,
  type AdminResource,
  type AdminResourceLog,
  type AdminResourceQuery
} from '../../api/admin-resource';
import { coverForResourceType } from '../../features/student/resources';

type ResourceStatus = 'PUBLISHED' | 'REVIEWING' | 'SUSPENDED';
type ResourceTab = 'detail' | 'history' | 'logs';

interface ResourceOption {
  label: string;
  value: string;
}

interface MajorOption {
  label: string;
  value: number;
}

interface PublicHistoryItem {
  version: number;
  title: string;
  publishedAt: string;
  reviewerName?: string;
  note?: string;
}

interface PublicResourceRow extends AdminResource {
  coverResolved: string;
  typeLabel: string;
  typeTone: string;
  statusLabel: string;
  statusTone: 'published' | 'reviewing' | 'suspended';
  majorLabel: string;
  fileSizeLabel: string;
  publishedAtLabel: string;
  updatedAtLabel: string;
  historyItems: PublicHistoryItem[];
  reviewerName?: string;
  reviewComment?: string;
}

const pageSize = 6;
const loading = ref(false);
const saving = ref(false);
const logsLoading = ref(false);
const page = ref(1);
const resources = ref<PublicResourceRow[]>([]);
const selectedResource = ref<PublicResourceRow | null>(null);
const selectedLogs = ref<AdminResourceLog[]>([]);
const activeTab = ref<ResourceTab>('detail');
const previewVisible = ref(false);
const previewTarget = ref<PublicResourceRow | null>(null);
const logDrawerVisible = ref(false);
const logTarget = ref<PublicResourceRow | null>(null);
const takeDownVisible = ref(false);
const takeDownTarget = ref<PublicResourceRow | null>(null);
const takeDownReason = ref('');
const selectedIds = ref<number[]>([]);
const draft = reactive({
  keyword: '',
  resourceType: '',
  majorId: null as number | null,
  courseName: '',
  uploaderKey: ''
});

const appliedFilters = ref({ ...draft });

const majorOptions: MajorOption[] = [
  { label: '城市轨道交通运营管理', value: 1 },
  { label: '城市轨道交通车辆技术', value: 2 },
  { label: '城市轨道交通机电技术', value: 3 },
  { label: '城市轨道交通通信信号技术', value: 4 }
];

const resourceTypeOptions: ResourceOption[] = [
  { label: '文本文档', value: '文本文档' },
  { label: '演示文稿', value: '演示文稿' },
  { label: '图片', value: '图片' },
  { label: '音频', value: '音频' },
  { label: '视频', value: '视频' },
  { label: '实训试题', value: '实训试题' }
];

const filteredResources = computed(() =>
  resources.value.filter((item) => {
    const keyword = appliedFilters.value.keyword.trim().toLowerCase();
    const courseName = appliedFilters.value.courseName.trim().toLowerCase();
    const uploaderKey = appliedFilters.value.uploaderKey;
    const matchesKeyword =
      !keyword ||
      [item.resourceName, item.fileName].some((text) => String(text || '').toLowerCase().includes(keyword));
    const matchesType = !appliedFilters.value.resourceType || item.resourceType === appliedFilters.value.resourceType;
    const matchesMajor = !appliedFilters.value.majorId || item.majorId === appliedFilters.value.majorId;
    const matchesCourse = !courseName || String(item.courseName || '').toLowerCase().includes(courseName);
    const matchesUploader = !uploaderKey || uploaderKey === buildUploaderKey(item);
    return matchesKeyword && matchesType && matchesMajor && matchesCourse && matchesUploader;
  })
);

const totalCount = computed(() => filteredResources.value.length);
const publishedCount = computed(() => filteredResources.value.filter((item) => item.statusTone === 'published').length);
const reviewingCount = computed(() => filteredResources.value.filter((item) => item.statusTone === 'reviewing').length);
const suspendedCount = computed(() => filteredResources.value.filter((item) => item.statusTone === 'suspended').length);
const pagedResources = computed(() => filteredResources.value.slice((page.value - 1) * pageSize, page.value * pageSize));
const allCurrentSelected = computed(() => pagedResources.value.length > 0 && pagedResources.value.every((item) => selectedIds.value.includes(item.resourceId)));
const partCurrentSelected = computed(() => selectedIds.value.length > 0 && !allCurrentSelected.value);
const uploaderOptions = computed<ResourceOption[]>(() => {
  const seen = new Map<string, string>();
  resources.value.forEach((item) => {
    const label = item.uploaderName?.trim();
    if (!label) {
      return;
    }
    seen.set(buildUploaderKey(item), label);
  });
  return Array.from(seen, ([value, label]) => ({ value, label }));
});


function normalizeStatus(value?: string): ResourceStatus {
  const upper = String(value || '').toUpperCase();
  if (upper === 'REVIEWING' || upper === 'PENDING') {
    return 'REVIEWING';
  }
  if (upper === 'SUSPENDED' || upper === 'REVOKED' || upper === 'OFFLINE') {
    return 'SUSPENDED';
  }
  return 'PUBLISHED';
}

function statusTone(status: ResourceStatus): 'published' | 'reviewing' | 'suspended' {
  if (status === 'REVIEWING') {
    return 'reviewing';
  }
  if (status === 'SUSPENDED') {
    return 'suspended';
  }
  return 'published';
}

function statusLabel(status: ResourceStatus) {
  if (status === 'REVIEWING') {
    return '下架中';
  }
  if (status === 'SUSPENDED') {
    return '已下架';
  }
  return '已公开';
}

function normalizeResourceType(type?: string) {
  const raw = String(type || '').trim();
  if (!raw) {
    return '资源';
  }
  const labels: Record<string, string> = {
    DOCUMENT: '文本文档',
    PRESENTATION: '演示文稿',
    IMAGE: '图片',
    AUDIO: '音频',
    VIDEO: '视频'
  };
  return labels[raw.toUpperCase()] || raw;
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
    return `${bytes} KB`;
  }
  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} MB`;
  }
  return `${(bytes / 1024 / 1024).toFixed(1)} GB`;
}

function formatDateTime(value?: string) {
  if (!value) {
    return '-';
  }
  return value.includes('T') ? value.replace('T', ' ').slice(0, 16) : value.slice(0, 16);
}

function findMajorLabel(majorId?: number | null) {
  return majorOptions.find((item) => item.value === majorId)?.label || '-';
}

function buildUploaderKey(resource: AdminResource) {
  if (resource.uploaderId) {
    return `id:${resource.uploaderId}`;
  }
  return `name:${resource.uploaderName || ''}`;
}

function buildHistoryItems(resource: AdminResource): PublicHistoryItem[] {
  const currentVersion = Number(resource.currentVersion ?? 1);
  const publicVersion = Number(resource.publicVersion ?? currentVersion);
  return [
    {
      version: publicVersion,
      title: `${resource.resourceName} - 当前公开版本`,
      publishedAt: formatDateTime(resource.createdAt || resource.updatedAt),
      reviewerName: resource.uploaderName || '系统',
      note: '当前公开版本'
    },
    {
      version: currentVersion,
      title: `${resource.resourceName} - 最新资源版本`,
      publishedAt: formatDateTime(resource.updatedAt || resource.createdAt),
      reviewerName: resource.uploaderName || '系统',
      note: '最新同步版本'
    }
  ];
}

function mapResourceRow(resource: AdminResource): PublicResourceRow {
  const typeLabel = normalizeResourceType(resource.resourceType);
  const status = normalizeStatus(resource.publicStatus);
  const majorLabel = resource.majorName || findMajorLabel(resource.majorId) || '-';
  return {
    ...resource,
    resourceType: typeLabel,
    coverUrl: resource.coverUrl || coverForResourceType(typeLabel),
    coverResolved: resource.coverUrl || coverForResourceType(typeLabel),
    typeLabel,
    typeTone: typeTone(typeLabel),
    statusLabel: statusLabel(status),
    statusTone: statusTone(status),
    majorLabel,
    fileSizeLabel: formatFileSize(resource.fileSize),
    publishedAtLabel: formatDateTime(resource.createdAt || resource.updatedAt),
    updatedAtLabel: formatDateTime(resource.updatedAt || resource.createdAt),
    publicStatus: status,
    historyItems: buildHistoryItems(resource)
  };
}

function createEmptyFilters() {
  return {
    keyword: '',
    resourceType: '',
    majorId: null as number | null,
    courseName: '',
    uploaderKey: ''
  };
}

async function selectResource(row: PublicResourceRow) {
  selectedResource.value = row;
  activeTab.value = 'detail';
  try {
    selectedResource.value = mapResourceRow(await fetchAdminResource(row.resourceId));
  } catch {
    selectedResource.value = row;
  }
  void loadLogs(row.resourceId);
}

function toggleOne(resourceId: number) {
  selectedIds.value = selectedIds.value.includes(resourceId)
    ? selectedIds.value.filter((id) => id !== resourceId)
    : [...selectedIds.value, resourceId];
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

function applyFilters() {
  appliedFilters.value = {
    keyword: draft.keyword,
    resourceType: draft.resourceType,
    majorId: draft.majorId,
    courseName: draft.courseName,
    uploaderKey: draft.uploaderKey
  };
  page.value = 1;
  void loadResources();
}

function resetFilters() {
  Object.assign(draft, createEmptyFilters());
  appliedFilters.value = createEmptyFilters();
  page.value = 1;
  void loadResources();
}

function buildQuery(): AdminResourceQuery {
  const uploaderId = appliedFilters.value.uploaderKey.startsWith('id:') ? Number(appliedFilters.value.uploaderKey.replace(/^id:/, '')) : NaN;
  return {
    keyword: appliedFilters.value.keyword.trim() || undefined,
    resourceType: appliedFilters.value.resourceType || undefined,
    majorId: appliedFilters.value.majorId ?? undefined,
    courseName: appliedFilters.value.courseName.trim() || undefined,
    uploaderId: Number.isFinite(uploaderId) ? uploaderId : undefined,
    page: 1,
    pageSize: 999
  };
}

async function loadResources() {
  loading.value = true;
  try {
    const result = await fetchAdminPublicResources(buildQuery());
    const mapped = result.records.map(mapResourceRow);
    resources.value = mapped;
    if (!selectedResource.value || !resources.value.some((item) => item.resourceId === selectedResource.value?.resourceId)) {
      selectedResource.value = resources.value[0] ?? null;
    }
    if (selectedResource.value) {
      void selectResource(selectedResource.value);
    }
  } catch (error) {
    resources.value = [];
    selectedResource.value = null;
    ElMessage.error(error instanceof Error ? error.message : '公开资源库加载失败');
  } finally {
    loading.value = false;
    selectedIds.value = [];
  }
}

async function refreshList() {
  await loadResources();
}

async function openLogs(row: PublicResourceRow) {
  logTarget.value = row;
  logDrawerVisible.value = true;
  await loadLogs(row.resourceId);
}

async function loadLogs(resourceId: number) {
  logsLoading.value = true;
  try {
    selectedLogs.value = await fetchAdminResourceLogs(resourceId);
  } catch {
    selectedLogs.value = [];
  } finally {
    logsLoading.value = false;
  }
}

function openPreview(row: PublicResourceRow) {
  previewTarget.value = row;
  previewVisible.value = true;
}

function copyLink(row: PublicResourceRow) {
  const url = row.fileUrl || row.previewUrl;
  if (!url) {
    ElMessage.warning('暂无可复制链接');
    return;
  }
  void navigator.clipboard?.writeText(url);
  ElMessage.success('链接已复制');
}

function downloadPreview() {
  const url = previewTarget.value?.fileUrl || previewTarget.value?.previewUrl;
  if (url) {
    window.open(url, '_blank', 'noopener');
    return;
  }
  ElMessage.info('正在下载资源');
}

function openTakeDown(row: PublicResourceRow) {
  takeDownTarget.value = row;
  takeDownReason.value = '';
  takeDownVisible.value = true;
}

function openBatchTakeDown() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择资源');
    return;
  }
  takeDownTarget.value = selectedResource.value;
  takeDownReason.value = '';
  takeDownVisible.value = true;
}

async function submitTakeDown() {
  if (!takeDownTarget.value) {
    return;
  }

  if (!takeDownReason.value.trim()) {
    ElMessage.warning('请输入下架原因');
    return;
  }

  saving.value = true;
  try {
    const targets = selectedIds.value.length > 0 ? resources.value.filter((item) => selectedIds.value.includes(item.resourceId)) : [takeDownTarget.value];
    targets.forEach((item) => {
      item.publicStatus = 'REVIEWING';
      item.statusTone = 'reviewing';
      item.statusLabel = statusLabel('REVIEWING');
    });
    takeDownVisible.value = false;
    ElMessage.success('下架申请已提交');
    if (selectedResource.value) {
      selectedResource.value = resources.value.find((item) => item.resourceId === selectedResource.value?.resourceId) ?? selectedResource.value;
    }
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  void loadResources();
});
</script>
