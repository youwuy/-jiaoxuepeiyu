<template>
  <AdminShell activeKey="public-resource">
    <section class="admin-public-resource-page">
      <el-breadcrumb class="admin-public-resource-breadcrumb" separator="/">
        <el-breadcrumb-item>资源管理</el-breadcrumb-item>
        <el-breadcrumb-item>公开资源库</el-breadcrumb-item>
      </el-breadcrumb>

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

      <p class="admin-public-resource-count">共 <b>{{ totalCount }}</b> 个公开资源</p>

      <div class="admin-public-resource-workspace">
        <section class="admin-public-resource-board">
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
              <p>显示 <b>{{ (page - 1) * pageSize + 1 }}</b> 到 <b>{{ Math.min(page * pageSize, totalCount) }}</b> 条，共 <b>{{ totalCount }}</b> 条记录</p>
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

      <div v-if="previewTarget" class="admin-public-resource-preview-doc"><AdminResourcePreview :resource="previewTarget" /></div>

      <template #footer>
        <div class="admin-public-resource-preview-footer">
          <el-button type="primary" @click="downloadPreview">下载</el-button>
        </div>
      </template>
    </el-dialog>

  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Close, Search } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import AdminResourcePreview from '../../components/admin/AdminResourcePreview.vue';
import {
  fetchAdminPublicResources,
  type AdminResource,
  type AdminResourceQuery
} from '../../api/admin-resource';
import { resolvePublicUrl } from '../../api/http';
import { coverForResourceType } from '../../features/student/resources';
import { fetchAdminMajors, fetchAdminTeachers } from '../../api/admin-course';

type ResourceStatus = 'PUBLISHED' | 'REVIEWING' | 'SUSPENDED';

interface ResourceOption {
  label: string;
  value: string;
}

interface MajorOption {
  label: string;
  value: number;
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
}

const pageSize = 6;
const loading = ref(false);
const page = ref(1);
const resources = ref<PublicResourceRow[]>([]);
const previewVisible = ref(false);
const previewTarget = ref<PublicResourceRow | null>(null);
const selectedIds = ref<number[]>([]);
const draft = reactive({
  keyword: '',
  resourceType: '',
  majorId: null as number | null,
  courseName: '',
  uploaderKey: ''
});

const appliedFilters = ref({ ...draft });

const majorOptions = ref<MajorOption[]>([]);
const uploaderOptions = ref<ResourceOption[]>([]);

const resourceTypeOptions: ResourceOption[] = [
  { label: '文本文档', value: 'DOCUMENT' },
  { label: '演示文稿', value: 'PRESENTATION' },
  { label: '图片', value: 'IMAGE' },
  { label: '音频', value: 'AUDIO' },
  { label: '视频', value: 'VIDEO' }
];

const totalCount = ref(0);
const pagedResources = computed(() => resources.value);
const allCurrentSelected = computed(() => pagedResources.value.length > 0 && pagedResources.value.every((item) => selectedIds.value.includes(item.resourceId)));
const partCurrentSelected = computed(() => selectedIds.value.length > 0 && !allCurrentSelected.value);

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

function mapResourceRow(resource: AdminResource): PublicResourceRow {
  const typeLabel = normalizeResourceType(resource.resourceType);
  const status = normalizeStatus(resource.publicStatus);
  const majorLabel = resource.majorName || findMajorLabel(resource.majorId) || '-';
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
    statusLabel: statusLabel(status),
    statusTone: statusTone(status),
    majorLabel,
    fileSizeLabel: formatFileSize(resource.fileSize),
    publishedAtLabel: formatDateTime(resource.createdAt || resource.updatedAt),
    updatedAtLabel: formatDateTime(resource.updatedAt || resource.createdAt),
    publicStatus: status
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
    page: page.value,
    pageSize
  };
}

async function loadResources() {
  loading.value = true;
  try {
    const result = await fetchAdminPublicResources(buildQuery());
    const mapped = result.records.map(mapResourceRow);
    resources.value = mapped;
    totalCount.value = result.total;
  } catch (error) {
    resources.value = [];
    totalCount.value = 0;
    ElMessage.error(error instanceof Error ? error.message : '公开资源库加载失败');
  } finally {
    loading.value = false;
    selectedIds.value = [];
  }
}

async function loadFilterOptions() {
  try {
    const [majors, teachers] = await Promise.all([fetchAdminMajors(), fetchAdminTeachers()]);
    majorOptions.value = majors.map((item) => ({
      label: `${item.majorName}${item.enabled === false ? '（已禁用）' : ''}`,
      value: item.majorId
    }));
    uploaderOptions.value = teachers
      .filter((item) => item.enabled !== false)
      .map((item) => ({ label: item.realName, value: `id:${item.userId}` }));
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '筛选项加载失败');
  }
}

function openPreview(row: PublicResourceRow) {
  previewTarget.value = row;
  previewVisible.value = true;
}

function downloadPreview() {
  const url = previewTarget.value?.fileUrl || previewTarget.value?.previewUrl;
  if (url) {
    window.open(url, '_blank', 'noopener');
    return;
  }
  ElMessage.info('正在下载资源');
}

watch(page, () => {
  selectedIds.value = [];
  void loadResources();
});

onMounted(() => {
  void loadFilterOptions();
  void loadResources();
});
</script>
