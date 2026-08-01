<template>
  <AdminShell activeKey="personal-resource">
    <section class="admin-resource-page">
      <el-breadcrumb class="admin-resource-breadcrumb" separator="/">
        <el-breadcrumb-item>资源管理</el-breadcrumb-item>
        <el-breadcrumb-item>个人资源库</el-breadcrumb-item>
      </el-breadcrumb>

      <header class="admin-resource-head">
        <div>
          <h1>个人资源库</h1>
          <p>统一维护个人资源的类型、专业、课程归属和公示状态</p>
        </div>
      </header>

      <section class="admin-resource-filter-card">
        <div class="admin-resource-filter-row">
          <el-input
            v-model="draft.keyword"
            class="admin-resource-search"
            :prefix-icon="Search"
            placeholder="搜索资源名称、课程或上传人"
            clearable
            @keyup.enter="applyFilters"
          />
          <el-select v-model="draft.resourceType" class="admin-resource-select" placeholder="资源类型" clearable>
            <el-option v-for="item in resourceTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-model="draft.majorId" class="admin-resource-select" placeholder="所属专业" clearable filterable>
            <el-option v-for="item in majorOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-model="draft.publicStatus" class="admin-resource-select" placeholder="公示状态" clearable>
            <el-option v-for="item in publicStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-button class="admin-resource-query-button" @click="applyFilters">查询</el-button>
          <el-button class="admin-resource-reset-button" @click="resetFilters">重置</el-button>
        </div>
      </section>

      <div class="admin-resource-layout" :class="{ 'has-panel': panelVisible }">
        <section class="admin-resource-board">
          <header class="admin-resource-board-head">
            <div>
              <strong>资源列表</strong>
              <p>共 {{ totalCount }} 条资源，当前已选 {{ selectedIds.length }} 条</p>
            </div>
            <div class="admin-resource-board-actions">
              <el-button class="admin-resource-lite-button" @click="openBatchEdit">批量修改</el-button>
              <el-button class="admin-resource-lite-button danger" @click="batchDeleteResources">批量删除</el-button>
              <el-button class="admin-resource-primary-button" type="primary" @click="openCreatePanel">
                <el-icon><Plus /></el-icon>
                新增资源
              </el-button>
            </div>
          </header>

          <div v-if="loading" class="admin-resource-empty">资源加载中...</div>
          <div v-else-if="pagedResources.length === 0" class="admin-resource-empty">
            <el-empty description="暂无匹配资源" />
          </div>
          <template v-else>
            <div class="admin-resource-table-scroll">
              <table class="admin-resource-table">
                <thead>
                  <tr>
                    <th class="check-col">
                      <el-checkbox :model-value="allCurrentSelected" :indeterminate="partCurrentSelected" @change="toggleAllCurrent" />
                    </th>
                    <th>资源名称</th>
                    <th>类型</th>
                    <th>所属专业</th>
                    <th>课程名称</th>
                    <th>上传人</th>
                    <th>公示状态</th>
                    <th>版本</th>
                    <th>文件大小</th>
                    <th>更新时间</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="row in pagedResources" :key="row.resourceId" :class="{ selected: selectedIds.includes(row.resourceId) }">
                    <td class="check-col">
                      <el-checkbox :model-value="selectedIds.includes(row.resourceId)" @change="toggleOne(row.resourceId)" />
                    </td>
                    <td>
                      <div class="admin-resource-name-cell">
                        <img :src="row.coverResolved" :alt="row.resourceName" />
                        <div>
                          <strong>{{ row.resourceName }}</strong>
                          <span>{{ row.fileName || row.previewUrl || row.fileUrl || '-' }}</span>
                        </div>
                      </div>
                    </td>
                    <td>
                      <span class="admin-resource-type-pill" :class="row.typeTone">{{ row.typeLabel }}</span>
                    </td>
                    <td class="wrap-cell">{{ row.majorLabel }}</td>
                    <td class="wrap-cell">{{ row.courseName || '-' }}</td>
                    <td>{{ row.uploaderName || '-' }}</td>
                    <td>
                      <span class="admin-resource-status" :class="row.statusTone">
                        <i></i>
                        {{ row.statusLabel }}
                      </span>
                    </td>
                    <td class="admin-resource-version">
                      <strong>V{{ row.currentVersion ?? 1 }}</strong>
                      <span>公示 V{{ row.publicVersion ?? row.currentVersion ?? 1 }}</span>
                    </td>
                    <td>{{ row.fileSizeLabel }}</td>
                    <td>{{ row.updatedAtLabel }}</td>
                    <td>
                      <div class="admin-resource-row-actions">
                        <el-button class="plain" @click="openDetail(row)">查看</el-button>
                        <el-button class="edit" @click="openEditPanel(row)">编辑</el-button>
                        <el-button class="plain" @click="openLogs(row)">日志</el-button>
                        <el-button class="warn" @click="applyPublic(row)">公示申请</el-button>
                        <el-button class="danger" :loading="busyId === row.resourceId" @click="deleteResource(row)">删除</el-button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <footer class="admin-resource-footer">
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

        <aside v-if="panelVisible" class="admin-resource-panel">
          <div class="admin-resource-panel-head">
            <div>
              <strong>{{ panelTitle }}</strong>
              <p>维护资源元数据、文件信息和公示状态</p>
            </div>
            <el-button text circle :icon="Close" @click="closePanel" />
          </div>

          <div class="admin-resource-form">
            <label class="admin-resource-field wide">
              <span>资源名称 <b>*</b></span>
              <el-input v-model="form.resourceName" maxlength="40" placeholder="请输入资源名称" />
            </label>

            <div class="admin-resource-form-grid">
              <label class="admin-resource-field">
                <span>资源类型 <b>*</b></span>
                <el-select v-model="form.resourceType" placeholder="请选择资源类型">
                  <el-option v-for="item in resourceTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </label>
              <label class="admin-resource-field">
                <span>所属专业 <b>*</b></span>
                <el-select v-model="form.majorId" placeholder="请选择专业" filterable>
                  <el-option v-for="item in majorOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </label>
              <label class="admin-resource-field">
                <span>课程名称</span>
                <el-input v-model="form.courseName" maxlength="40" placeholder="请输入课程名称" />
              </label>
              <label class="admin-resource-field">
                <span>上传人</span>
                <el-input v-model="form.uploaderName" maxlength="20" placeholder="请输入上传人" />
              </label>
              <label class="admin-resource-field">
                <span>文件名称 <b>*</b></span>
                <el-input v-model="form.fileName" maxlength="80" placeholder="请输入文件名称" />
              </label>
              <label class="admin-resource-field">
                <span>文件大小(KB) <b>*</b></span>
                <el-input v-model="form.fileSize" type="number" min="0" placeholder="请输入文件大小" />
              </label>
              <label class="admin-resource-field wide">
                <span>封面地址</span>
                <el-input v-model="form.coverUrl" placeholder="请输入封面地址" />
              </label>
              <label class="admin-resource-field wide">
                <span>文件地址 <b>*</b></span>
                <el-input v-model="form.fileUrl" placeholder="请输入文件地址" />
              </label>
              <label class="admin-resource-field wide">
                <span>预览地址</span>
                <el-input v-model="form.previewUrl" placeholder="请输入预览地址" />
              </label>
              <label class="admin-resource-field">
                <span>公示状态</span>
                <el-select v-model="form.publicStatus" placeholder="请选择状态">
                  <el-option v-for="item in publicStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </label>
              <label class="admin-resource-field">
                <span>当前版本</span>
                <el-input v-model="form.currentVersion" type="number" min="1" />
              </label>
              <label class="admin-resource-field">
                <span>公示版本</span>
                <el-input v-model="form.publicVersion" type="number" min="1" />
              </label>
            </div>
          </div>

          <div class="admin-resource-panel-footer">
            <el-button class="admin-resource-panel-cancel" @click="closePanel">取消</el-button>
            <el-button class="admin-resource-panel-confirm" type="primary" :loading="saving" @click="saveResource">确定</el-button>
          </div>
        </aside>
      </div>
    </section>

    <el-drawer v-model="detailVisible" class="admin-resource-drawer" direction="rtl" size="720px" :with-header="false">
      <div class="admin-drawer-head">
        <div>
          <p>资源详情</p>
          <h3>{{ detailResource?.resourceName || '资源详情' }}</h3>
        </div>
        <el-button text :icon="Close" @click="detailVisible = false" />
      </div>

      <div v-if="detailLoading" class="admin-resource-empty drawer-state">详情加载中...</div>
      <template v-else-if="detailResource">
        <section class="admin-resource-detail-summary">
          <div>
            <span>公示状态</span>
            <strong>{{ detailResource.statusLabel }}</strong>
          </div>
          <div>
            <span>资源类型</span>
            <strong>{{ detailResource.typeLabel }}</strong>
          </div>
          <div>
            <span>所属专业</span>
            <strong>{{ detailResource.majorLabel }}</strong>
          </div>
          <div>
            <span>更新时间</span>
            <strong>{{ detailResource.updatedAtLabel }}</strong>
          </div>
        </section>

        <section class="admin-resource-detail-grid">
          <div class="admin-resource-detail-card">
            <p>基础信息</p>
            <dl>
              <div><dt>课程名称</dt><dd>{{ detailResource.courseName || '-' }}</dd></div>
              <div><dt>上传人</dt><dd>{{ detailResource.uploaderName || '-' }}</dd></div>
              <div><dt>文件名称</dt><dd>{{ detailResource.fileName || '-' }}</dd></div>
              <div><dt>文件大小</dt><dd>{{ detailResource.fileSizeLabel }}</dd></div>
              <div><dt>当前版本</dt><dd>V{{ detailResource.currentVersion ?? 1 }}</dd></div>
              <div><dt>公示版本</dt><dd>V{{ detailResource.publicVersion ?? detailResource.currentVersion ?? 1 }}</dd></div>
            </dl>
          </div>

          <div class="admin-resource-detail-card">
            <p>封面与文件</p>
            <div class="admin-resource-preview-box">
              <img :src="detailResource.coverResolved" :alt="detailResource.resourceName" />
              <div>
                <strong>{{ detailResource.resourceName }}</strong>
                <p>{{ detailResource.fileName || '-' }}</p>
                <div class="admin-resource-detail-actions">
                  <el-button class="plain" @click="previewResource(detailResource)">预览</el-button>
                  <el-button class="plain" @click="copyFileLink(detailResource)">复制链接</el-button>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section class="admin-resource-detail-card admin-resource-log-card">
          <p>操作日志</p>
          <div v-if="detailLogs.length === 0" class="admin-resource-log-empty">暂无日志</div>
          <article v-for="item in detailLogs" :key="item.logId" class="admin-resource-log-row">
            <header>
              <strong>{{ item.action }}</strong>
              <span>{{ formatDateTime(item.createdAt) }}</span>
            </header>
            <p>{{ item.content }}</p>
            <small>{{ item.operatorName }}</small>
          </article>
        </section>
      </template>
    </el-drawer>

    <el-dialog v-model="batchEditVisible" class="admin-resource-dialog" width="560px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-resource-dialog-head">
          <strong>批量修改资源</strong>
          <el-button text circle :icon="Close" @click="batchEditVisible = false" />
        </div>
      </template>

      <div class="admin-resource-batch-grid">
        <label>
          <span>所属专业</span>
          <el-select v-model="batchForm.majorId" placeholder="请选择专业" filterable>
            <el-option v-for="item in majorOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </label>
        <label class="wide">
          <span>课程名称</span>
          <el-input v-model="batchForm.courseName" placeholder="请输入课程名称" />
        </label>
      </div>

      <template #footer>
        <div class="admin-resource-dialog-footer">
          <el-button @click="batchEditVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveBatchEdit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Close, Plus, Search } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  batchUpdateAdminResources,
  createAdminResource,
  deleteAdminResources,
  fetchAdminResource,
  fetchAdminResourceLogs,
  fetchAdminResources,
  submitAdminResourcePublicApplication,
  updateAdminResource,
  type AdminResource,
  type AdminResourceCommand,
  type AdminResourceLog,
  type AdminResourceQuery
} from '../../api/admin-resource';
import { coverForResourceType } from '../../features/student/resources';

type ResourceStatus = 'DRAFT' | 'REVIEWING' | 'PUBLISHED';
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
  statusTone: 'draft' | 'reviewing' | 'published';
  majorLabel: string;
  fileSizeLabel: string;
  updatedAtLabel: string;
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
  publicStatus: ResourceStatus;
  currentVersion: string;
  publicVersion: string;
}

const pageSize = 8;
const loading = ref(false);
const saving = ref(false);
const detailLoading = ref(false);
const page = ref(1);
const resources = ref<ResourceRow[]>([]);
const detailResource = ref<ResourceRow | null>(null);
const detailLogs = ref<AdminResourceLog[]>([]);
const detailVisible = ref(false);
const panelVisible = ref(true);
const panelMode = ref<PanelMode>('create');
const editingId = ref<number | null>(null);
const busyId = ref<number | null>(null);
const batchEditVisible = ref(false);
const selectedIds = ref<number[]>([]);

const draft = reactive({
  keyword: '',
  resourceType: '',
  majorId: null as number | null,
  publicStatus: '' as '' | ResourceStatus
});

const appliedFilters = ref({ ...draft });
const form = reactive<ResourceForm>(createEmptyForm());
const batchForm = reactive({
  majorId: null as number | null,
  courseName: ''
});

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

const publicStatusOptions: ResourceOption[] = [
  { label: '未公示', value: 'DRAFT' },
  { label: '审核中', value: 'REVIEWING' },
  { label: '已公示', value: 'PUBLISHED' }
];

const totalCount = computed(() => filteredResources.value.length);
const filteredResources = computed(() =>
  resources.value.filter((item) => {
    const keyword = appliedFilters.value.keyword.trim().toLowerCase();
    const matchesKeyword =
      !keyword ||
      [item.resourceName, item.courseName, item.uploaderName, item.fileName, item.majorLabel].some((text) => String(text || '').toLowerCase().includes(keyword));
    const matchesType = !appliedFilters.value.resourceType || item.resourceType === appliedFilters.value.resourceType;
    const matchesMajor = !appliedFilters.value.majorId || item.majorId === appliedFilters.value.majorId;
    const matchesStatus = !appliedFilters.value.publicStatus || item.publicStatus === appliedFilters.value.publicStatus;
    return matchesKeyword && matchesType && matchesMajor && matchesStatus;
  })
);
const pagedResources = computed(() => filteredResources.value.slice((page.value - 1) * pageSize, page.value * pageSize));
const panelTitle = computed(() => (panelMode.value === 'edit' ? '编辑资源' : '新增资源'));
const allCurrentSelected = computed(() => pagedResources.value.length > 0 && pagedResources.value.every((item) => selectedIds.value.includes(item.resourceId)));
const partCurrentSelected = computed(() => selectedIds.value.length > 0 && !allCurrentSelected.value);

function createEmptyForm(): ResourceForm {
  return {
    resourceName: '',
    resourceType: resourceTypeOptions[0]?.value ?? '文本文档',
    majorId: majorOptions[0]?.value ?? null,
    courseName: '',
    uploaderName: '',
    coverUrl: '',
    fileUrl: '',
    previewUrl: '',
    fileName: '',
    fileSize: '',
    publicStatus: 'DRAFT',
    currentVersion: '1',
    publicVersion: '1'
  };
}

function mockResources(): AdminResource[] {
  return [
    {
      resourceId: 5001,
      resourceName: '城轨运营基础教学课件',
      resourceType: '演示文稿',
      coverUrl: coverForResourceType('演示文稿'),
      fileUrl: 'https://example.com/resource/5001',
      previewUrl: 'https://example.com/resource/5001/preview',
      fileName: '城轨运营基础教学课件.pptx',
      fileSize: 18600,
      majorId: 1,
      majorName: '城市轨道交通运营管理',
      courseName: '城市轨道交通概论',
      uploaderName: '王老师',
      publicStatus: 'PUBLISHED',
      currentVersion: 2,
      publicVersion: 2,
      updatedAt: '2025-03-18 10:20'
    },
    {
      resourceId: 5002,
      resourceName: 'CBTC系统原理讲解视频',
      resourceType: '视频',
      coverUrl: coverForResourceType('视频'),
      fileUrl: 'https://example.com/resource/5002',
      previewUrl: 'https://example.com/resource/5002/preview',
      fileName: 'CBTC系统原理讲解视频.mp4',
      fileSize: 246000,
      majorId: 4,
      majorName: '城市轨道交通通信信号技术',
      courseName: '城市轨道交通信号系统',
      uploaderName: '李老师',
      publicStatus: 'REVIEWING',
      currentVersion: 1,
      publicVersion: 1,
      updatedAt: '2025-03-20 14:05'
    },
    {
      resourceId: 5003,
      resourceName: '车辆构造高清图集',
      resourceType: '图片',
      coverUrl: coverForResourceType('图片'),
      fileUrl: 'https://example.com/resource/5003',
      previewUrl: 'https://example.com/resource/5003/preview',
      fileName: '车辆构造高清图集.zip',
      fileSize: 32400,
      majorId: 2,
      majorName: '城市轨道交通车辆技术',
      courseName: '城轨车辆构造',
      uploaderName: '赵老师',
      publicStatus: 'DRAFT',
      currentVersion: 1,
      publicVersion: 1,
      updatedAt: '2025-03-21 09:48'
    },
    {
      resourceId: 5004,
      resourceName: '车站运营管理标准手册',
      resourceType: '文本文档',
      coverUrl: coverForResourceType('文本文档'),
      fileUrl: 'https://example.com/resource/5004',
      previewUrl: 'https://example.com/resource/5004/preview',
      fileName: '车站运营管理标准手册.pdf',
      fileSize: 9800,
      majorId: 1,
      majorName: '城市轨道交通运营管理',
      courseName: '车站运营管理',
      uploaderName: '王老师',
      publicStatus: 'REVIEWING',
      currentVersion: 3,
      publicVersion: 2,
      updatedAt: '2025-03-16 11:20'
    },
    {
      resourceId: 5005,
      resourceName: '供电系统故障案例分析',
      resourceType: '音频',
      coverUrl: coverForResourceType('音频'),
      fileUrl: 'https://example.com/resource/5005',
      previewUrl: 'https://example.com/resource/5005/preview',
      fileName: '供电系统故障案例分析.mp3',
      fileSize: 86200,
      majorId: 3,
      majorName: '城市轨道交通机电技术',
      courseName: '城轨供电系统',
      uploaderName: '陈老师',
      publicStatus: 'PUBLISHED',
      currentVersion: 2,
      publicVersion: 2,
      updatedAt: '2025-03-15 16:10'
    },
    {
      resourceId: 5006,
      resourceName: '车站值班员实训试题库',
      resourceType: '实训试题',
      coverUrl: coverForResourceType('实训试题'),
      fileUrl: 'https://example.com/resource/5006',
      previewUrl: 'https://example.com/resource/5006/preview',
      fileName: '车站值班员实训试题库.xlsx',
      fileSize: 12500,
      majorId: 4,
      majorName: '城市轨道交通通信信号技术',
      courseName: '信号设备维护',
      uploaderName: '刘老师',
      publicStatus: 'DRAFT',
      currentVersion: 1,
      publicVersion: 1,
      updatedAt: '2025-03-13 18:15'
    }
  ];
}

function mapResourceRow(resource: AdminResource): ResourceRow {
  const typeLabel = resource.resourceType || '资源';
  const status = normalizeStatus(resource.publicStatus);
  const majorLabel = resource.majorName || findMajorLabel(resource.majorId) || '-';
  const fileSizeLabel = formatFileSize(resource.fileSize);
  const updatedAtLabel = formatDateTime(resource.updatedAt);
  return {
    ...resource,
    resourceType: typeLabel,
    coverUrl: resource.coverUrl || coverForResourceType(typeLabel),
    coverResolved: resource.coverUrl || coverForResourceType(typeLabel),
    typeLabel,
    typeTone: typeTone(typeLabel),
    statusLabel: statusLabels[status],
    statusTone: statusTone(status),
    majorLabel,
    fileSizeLabel,
    updatedAtLabel,
    publicStatus: status
  };
}

function normalizeStatus(value?: string): ResourceStatus {
  const upper = String(value || '').toUpperCase();
  if (upper === 'PUBLISHED' || upper === 'PUBLIC' || upper === '已公示') {
    return 'PUBLISHED';
  }

  if (upper === 'REVIEWING' || upper === 'REVIEW' || upper === '审核中' || upper === 'APPROVING') {
    return 'REVIEWING';
  }

  return 'DRAFT';
}

function statusTone(status: ResourceStatus): 'draft' | 'reviewing' | 'published' {
  if (status === 'PUBLISHED') {
    return 'published';
  }

  if (status === 'REVIEWING') {
    return 'reviewing';
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
  DRAFT: '未公示',
  REVIEWING: '审核中',
  PUBLISHED: '已公示'
};

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
  return majorOptions.find((item) => item.value === majorId)?.label || '';
}

function resetSelection() {
  selectedIds.value = [];
}

function openCreatePanel() {
  panelMode.value = 'create';
  editingId.value = null;
  Object.assign(form, createEmptyForm());
  form.coverUrl = coverForResourceType(form.resourceType);
  panelVisible.value = true;
}

function closePanel() {
  panelVisible.value = false;
}

function openEditPanel(row: ResourceRow) {
  panelMode.value = 'edit';
  editingId.value = row.resourceId;
  panelVisible.value = true;
  Object.assign(form, {
    resourceName: row.resourceName,
    resourceType: row.resourceType || '文本文档',
    majorId: row.majorId ?? majorOptions[0]?.value ?? null,
    courseName: row.courseName || '',
    uploaderName: row.uploaderName || '',
    coverUrl: row.coverUrl || '',
    fileUrl: row.fileUrl || '',
    previewUrl: row.previewUrl || '',
    fileName: row.fileName || '',
    fileSize: row.fileSize ? String(row.fileSize) : '',
    publicStatus: row.publicStatus || 'DRAFT',
    currentVersion: String(row.currentVersion ?? 1),
    publicVersion: String(row.publicVersion ?? row.currentVersion ?? 1)
  });
}

function openBatchEdit() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择资源');
    return;
  }

  batchForm.majorId = null;
  batchForm.courseName = '';
  batchEditVisible.value = true;
}

function applyFilters() {
  appliedFilters.value = {
    keyword: draft.keyword,
    resourceType: draft.resourceType,
    majorId: draft.majorId,
    publicStatus: draft.publicStatus
  };
  page.value = 1;
  void loadResources();
}

function resetFilters() {
  draft.keyword = '';
  draft.resourceType = '';
  draft.majorId = null;
  draft.publicStatus = '';
  appliedFilters.value = {
    keyword: '',
    resourceType: '',
    majorId: null,
    publicStatus: ''
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
  const resourceType = form.resourceType.trim();
  const majorId = Number(form.majorId);
  const fileUrl = form.fileUrl.trim();
  const fileName = form.fileName.trim();
  const fileSize = Number(form.fileSize);
  const currentVersion = Number(form.currentVersion || 1);
  const publicVersion = Number(form.publicVersion || currentVersion || 1);

  if (!resourceName) {
    throw new Error('请输入资源名称');
  }

  if (!resourceType) {
    throw new Error('请选择资源类型');
  }

  if (!majorId) {
    throw new Error('请选择所属专业');
  }

  if (!fileUrl) {
    throw new Error('请输入文件地址');
  }

  if (!fileName) {
    throw new Error('请输入文件名称');
  }

  if (!Number.isFinite(fileSize) || fileSize <= 0) {
    throw new Error('请输入有效的文件大小');
  }

  return {
    resourceName,
    coverUrl: form.coverUrl.trim() || coverForResourceType(resourceType),
    fileUrl,
    previewUrl: form.previewUrl.trim() || undefined,
    fileName,
    fileSize,
    majorId,
    courseName: form.courseName.trim() || undefined,
    uploaderName: form.uploaderName.trim() || undefined,
    resourceType,
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
    panelVisible.value = true;
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
      publicStatus: appliedFilters.value.publicStatus || undefined,
      page: 1,
      pageSize: 999
    };
    const result = await fetchAdminResources(query);
    resources.value = result.records.map(mapResourceRow);
  } catch {
    resources.value = mockResources().map(mapResourceRow);
  } finally {
    loading.value = false;
    resetSelection();
  }
}

async function openDetail(row: ResourceRow) {
  detailVisible.value = true;
  detailLoading.value = true;
  try {
    detailResource.value = mapResourceRow(await fetchAdminResource(row.resourceId));
  } catch {
    detailResource.value = row;
  }

  try {
    detailLogs.value = await fetchAdminResourceLogs(row.resourceId);
  } catch {
    detailLogs.value = [];
  } finally {
    detailLoading.value = false;
  }
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
    await ElMessageBox.confirm(`确认删除资源「${row.resourceName}」？`, '删除资源', {
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
    await ElMessageBox.confirm(`确认删除已选 ${selectedIds.value.length} 条资源？`, '批量删除', {
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

function previewResource(row: ResourceRow) {
  const url = row.previewUrl || row.fileUrl;
  if (url) {
    window.open(url, '_blank', 'noopener');
    return;
  }

  ElMessage.info(`正在打开资源：${row.resourceName}`);
}

function copyFileLink(row: ResourceRow) {
  const url = row.fileUrl || row.previewUrl;
  if (!url) {
    ElMessage.warning('暂无可复制链接');
    return;
  }

  void navigator.clipboard?.writeText(url);
  ElMessage.success('链接已复制');
}

watch(
  () => form.resourceType,
  (nextType) => {
    if (!form.coverUrl) {
      form.coverUrl = coverForResourceType(nextType);
    }
  }
);

onMounted(() => {
  void loadResources();
});
</script>
