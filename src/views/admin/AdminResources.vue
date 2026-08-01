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
            <el-button class="admin-resource-query-button" @click="applyFilters">查询</el-button>
            <el-button class="admin-resource-reset-button" @click="resetFilters">重置</el-button>
          </div>
        </div>
      </section>

      <section class="admin-resource-actions-row">
        <el-button class="admin-resource-primary-button" type="primary" @click="openCreatePanel">
          <el-icon><Plus /></el-icon>
          上传资源
        </el-button>
        <el-button class="admin-resource-lite-button" @click="openBatchEdit">
          <el-icon><Setting /></el-icon>
          批量设置
        </el-button>
        <el-button class="admin-resource-lite-button danger" @click="batchDeleteResources">
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
                  <td>{{ row.updatedAtLabel }}</td>
                  <td>{{ row.uploaderName || '-' }}</td>
                  <td>
                    <div class="admin-resource-row-actions design">
                      <el-button class="plain" @click="openPreview(row)">预览</el-button>
                      <el-button class="plain" @click="openEditPanel(row)">编辑</el-button>
                      <el-button class="plain" :loading="busyId === row.resourceId" @click="deleteResource(row)">删除</el-button>
                      <el-button v-if="row.publicStatus !== 'PUBLISHED'" class="warn" @click="applyPublic(row)">申请公开</el-button>
                      <el-button v-else-if="row.publicStatus === 'PUBLISHED'" class="warn" @click="applyPublic(row)">申请公开最新版</el-button>
                      <el-button class="log" @click="openLogs(row)">操作日志</el-button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <footer class="admin-resource-footer design">
            <p>显示 <b>{{ (page - 1) * pageSize + 1 }}</b> 到 <b>{{ Math.min(page * pageSize, totalCount) }}</b> 条，共 <b>{{ totalCount }}</b> 条记录</p>
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

      <el-dialog v-model="resourceFormVisible" class="admin-resource-design-dialog" width="600px" :show-close="false" append-to-body>
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

          <label class="admin-resource-modal-field">
            <span>封面图 <b>*</b></span>
            <div v-if="panelMode === 'edit' && form.coverUrl" class="admin-resource-file-card cover">
              <img :src="form.coverUrl" alt="封面图" />
              <div>
                <strong>{{ form.coverName || 'resource-cover.jpg' }}</strong>
                <p>{{ form.coverSize || '2.4 MB' }}</p>
              </div>
              <el-button text circle :icon="Delete" @click="clearCover" />
            </div>
            <button v-else type="button" class="admin-resource-upload-drop cover" @click="mockSelectCover">
              <el-icon><Picture /></el-icon>
              <strong>点击或拖拽上传封面图</strong>
              <span>支持 JPG、PNG 格式，大小不超过 5MB</span>
            </button>
          </label>

          <label class="admin-resource-modal-field">
            <span>资源内容 <b>*</b></span>
            <div v-if="panelMode === 'edit' && form.fileName" class="admin-resource-file-card content">
              <span class="admin-resource-file-icon">
                <el-icon><Document /></el-icon>
              </span>
              <div>
                <strong>{{ form.fileName }}</strong>
                <p>{{ form.fileSizeLabel || form.fileSize || '18.5 MB' }} <i></i> 上传完成 <em>✓</em></p>
              </div>
              <el-button text circle :icon="Delete" @click="clearFile" />
            </div>
            <button v-else type="button" class="admin-resource-upload-drop content" @click="mockSelectFile">
              <el-icon><UploadFilled /></el-icon>
              <strong>点击或拖拽上传资源文件</strong>
              <span>支持 PDF、Word、PPT、视频等多种格式，大小不超过 200MB</span>
            </button>
          </label>

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
          <div class="admin-resource-preview-document">
            <h2>第一章 转向架结构与原理</h2>
            <p>§ 1.1 转向架的组成与分类</p>
            <article v-for="item in previewSections" :key="item.index" :class="item.tone">
              <span>{{ item.index }}</span>
              <div>
                <strong>{{ item.title }}</strong>
                <p>{{ item.content }}</p>
              </div>
            </article>
          </div>
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
          <article v-for="item in detailLogs" :key="item.logId" class="admin-resource-log-row">
            <header>
              <strong>{{ item.action }}</strong>
              <span>{{ formatDateTime(item.createdAt) }}</span>
            </header>
            <p>{{ item.content }}</p>
            <small>{{ item.operatorName }}</small>
          </article>
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
            <button type="button" class="admin-resource-upload-drop cover" @click="mockBatchCover">
              <el-icon><Picture /></el-icon>
              <strong>点击或拖拽上传封面图</strong>
              <span>支持 JPG、PNG 格式，大小不超过 5MB</span>
            </button>
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

const draft = reactive({
  keyword: '',
  resourceType: '',
  majorId: null as number | null,
  courseName: '',
  publicStatus: '' as '' | ResourceStatus,
  uploadDateRange: [] as string[]
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

const appliedFilters = ref({ ...draft });
const form = reactive<ResourceForm>(createEmptyForm());
const batchForm = reactive({
  majorId: null as number | null,
  courseName: ''
});

const totalCount = computed(() => filteredResources.value.length);
const filteredResources = computed(() =>
  resources.value.filter((item) => {
    const keyword = appliedFilters.value.keyword.trim().toLowerCase();
    const matchesKeyword =
      !keyword ||
      [item.resourceName, item.courseName, item.uploaderName, item.fileName, item.majorLabel].some((text) => String(text || '').toLowerCase().includes(keyword));
    const matchesType = !appliedFilters.value.resourceType || item.resourceType === appliedFilters.value.resourceType;
    const matchesMajor = !appliedFilters.value.majorId || item.majorId === appliedFilters.value.majorId;
    const matchesCourse = !appliedFilters.value.courseName || String(item.courseName || '').includes(appliedFilters.value.courseName);
    const matchesStatus = !appliedFilters.value.publicStatus || item.publicStatus === appliedFilters.value.publicStatus;
    return matchesKeyword && matchesType && matchesMajor && matchesCourse && matchesStatus;
  })
);
const pagedResources = computed(() => filteredResources.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value));
const allCurrentSelected = computed(() => pagedResources.value.length > 0 && pagedResources.value.every((item) => selectedIds.value.includes(item.resourceId)));
const partCurrentSelected = computed(() => selectedIds.value.length > 0 && !allCurrentSelected.value);
const previewSections = [
  {
    index: 1,
    tone: 'blue',
    title: '转向架定义',
    content: '转向架是城轨车辆走行部的重要组成部分，连接车体与轨道，承载并传递车辆载荷，保证车辆沿轨道安全运行。'
  },
  {
    index: 2,
    tone: 'green',
    title: '转向架主要部件',
    content: '主要包括构架、轮对、轴箱、弹簧装置、减振器及牵引装置等部件，各部件协同工作确保运行品质。'
  },
  {
    index: 3,
    tone: 'orange',
    title: '转向架检修流程',
    content: '外观检查、尺寸测量、探伤检测、组装调试四步流程，严格执行检修规程。'
  }
];

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
    fileSizeLabel: '',
    coverName: '',
    coverSize: '',
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
    resourceType: row.resourceType || '文本文档',
    majorId: row.majorId ?? majorOptions[0]?.value ?? null,
    courseName: row.courseName || '',
    uploaderName: row.uploaderName || '',
    coverUrl: row.coverUrl || '',
    coverName: row.coverUrl ? 'math-cover.jpg' : '',
    coverSize: row.coverUrl ? '2.4 MB' : '',
    fileUrl: row.fileUrl || '',
    previewUrl: row.previewUrl || '',
    fileName: row.fileName || '',
    fileSize: row.fileSize ? String(row.fileSize) : '',
    fileSizeLabel: row.fileSizeLabel,
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

function mockSelectCover() {
  form.coverUrl = coverForResourceType(form.resourceType);
  form.coverName = 'resource-cover.jpg';
  form.coverSize = '2.4 MB';
  ElMessage.success('封面图已选择');
}

function mockSelectFile() {
  form.fileName = form.resourceName ? `${form.resourceName}.pdf` : '资源文件.pdf';
  form.fileUrl = form.fileUrl || 'https://example.com/resource/uploaded';
  form.previewUrl = form.previewUrl || form.fileUrl;
  form.fileSize = '18500';
  form.fileSizeLabel = '18.5 MB';
  ElMessage.success('资源文件已选择');
}

function mockBatchCover() {
  ElMessage.success('批量封面已选择');
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

onMounted(() => {
  void loadResources();
});
</script>
