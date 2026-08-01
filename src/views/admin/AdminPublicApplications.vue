<template>
  <AdminShell activeKey="public-application">
    <section class="admin-public-page">
      <el-breadcrumb class="admin-public-breadcrumb" separator="/">
        <el-breadcrumb-item>资源管理</el-breadcrumb-item>
        <el-breadcrumb-item>资源公开申请</el-breadcrumb-item>
      </el-breadcrumb>

      <header class="admin-public-head">
        <div>
          <h1>资源公开申请</h1>
          <p>统一查看资源申请、审核意见和公开状态，支持快速通过或驳回。</p>
        </div>

        <div class="admin-public-summary">
          <div class="admin-public-summary-card pending">
            <span>待审核</span>
            <strong>{{ pendingCount }}</strong>
          </div>
          <div class="admin-public-summary-card approved">
            <span>已通过</span>
            <strong>{{ approvedCount }}</strong>
          </div>
          <div class="admin-public-summary-card rejected">
            <span>已驳回</span>
            <strong>{{ rejectedCount }}</strong>
          </div>
          <div class="admin-public-summary-card total">
            <span>总申请</span>
            <strong>{{ totalCount }}</strong>
          </div>
        </div>
      </header>

      <section class="admin-public-filter-card">
        <div class="admin-public-filter-row">
          <el-input
            v-model="draft.keyword"
            class="admin-public-search"
            :prefix-icon="Search"
            placeholder="搜索资源名称、申请人或课程"
            clearable
            @keyup.enter="applyFilters"
          />
          <el-select v-model="draft.resourceType" class="admin-public-select" placeholder="资源类型" clearable>
            <el-option v-for="item in resourceTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-model="draft.majorId" class="admin-public-select" placeholder="所属专业" clearable filterable>
            <el-option v-for="item in majorOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-model="draft.publicStatus" class="admin-public-select" placeholder="审核状态" clearable>
            <el-option v-for="item in publicStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-date-picker
            v-model="draft.appliedRange"
            class="admin-public-range"
            type="daterange"
            range-separator="至"
            start-placeholder="申请开始"
            end-placeholder="申请结束"
            value-format="YYYY-MM-DD"
            unlink-panels
          />
          <el-button class="admin-public-query-button" @click="applyFilters">查询</el-button>
          <el-button class="admin-public-reset-button" @click="resetFilters">重置</el-button>
        </div>
      </section>

      <div class="admin-public-workspace" :class="{ 'has-detail': Boolean(selectedApplication) }">
        <section class="admin-public-board">
          <header class="admin-public-board-head">
            <div>
              <strong>申请列表</strong>
              <p>共 {{ totalCount }} 条申请，当前待审 {{ pendingCount }} 条，已选 {{ selectedIds.length }} 条</p>
            </div>
            <div class="admin-public-board-actions">
              <el-button class="admin-public-lite-button" :disabled="selectedIds.length === 0" @click="openBatchReview('APPROVED')">
                批量通过
              </el-button>
              <el-button class="admin-public-lite-button" :disabled="selectedIds.length === 0" @click="openBatchReview('REJECTED')">
                批量驳回
              </el-button>
              <el-button class="admin-public-lite-button" @click="refreshList">刷新</el-button>
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
                    <th class="check-col">
                      <el-checkbox :model-value="allCurrentSelected" :indeterminate="partCurrentSelected" @change="toggleAllCurrent" />
                    </th>
                    <th>资源名称</th>
                    <th>资源类型</th>
                    <th>版本</th>
                    <th>所属专业</th>
                    <th>申请人</th>
                    <th>申请时间</th>
                    <th>审核状态</th>
                    <th>审核人</th>
                    <th>审核时间</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="row in pagedApplications"
                    :key="row.applicationId"
                    :class="{ selected: selectedApplication?.applicationId === row.applicationId }"
                    @click="selectApplication(row)"
                  >
                    <td class="check-col">
                      <el-checkbox :model-value="selectedIds.includes(row.applicationId)" @change="toggleOne(row.applicationId)" />
                    </td>
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
                    <td class="admin-public-version">
                      <strong>V{{ row.resourceVersion ?? row.currentVersion ?? 1 }}</strong>
                      <span>公开 V{{ row.publicVersion ?? row.resourceVersion ?? row.currentVersion ?? 1 }}</span>
                    </td>
                    <td class="wrap-cell">{{ row.majorLabel }}</td>
                    <td>{{ row.applicantName || '-' }}</td>
                    <td>{{ row.appliedAtLabel }}</td>
                    <td>
                      <span class="admin-public-status" :class="row.statusTone">
                        <i />
                        {{ row.statusLabel }}
                      </span>
                    </td>
                    <td>{{ row.reviewerName || '-' }}</td>
                    <td>{{ row.reviewedAtLabel }}</td>
                    <td>
                      <div class="admin-public-row-actions">
                        <el-button class="plain" @click.stop="selectApplication(row)">查看</el-button>
                        <el-button class="plain" @click.stop="openLogs(row)">记录</el-button>
                        <el-button
                          v-if="row.statusTone === 'pending'"
                          class="approve"
                          :loading="busyId === row.applicationId"
                          @click.stop="openReview(row, 'APPROVED')"
                        >
                          通过
                        </el-button>
                        <el-button
                          v-if="row.statusTone === 'pending'"
                          class="reject"
                          :loading="busyId === row.applicationId"
                          @click.stop="openReview(row, 'REJECTED')"
                        >
                          驳回
                        </el-button>
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

        <aside v-if="selectedApplication" class="admin-public-panel">
          <div class="admin-public-panel-head">
            <div>
              <strong>申请详情</strong>
              <p>{{ selectedApplication.resourceName }}</p>
            </div>
            <el-button text circle :icon="Close" @click="selectedApplication = null" />
          </div>

          <section class="admin-public-status-card">
            <div>
              <span>审核状态</span>
              <strong :class="selectedApplication.statusTone">{{ selectedApplication.statusLabel }}</strong>
            </div>
            <div>
              <span>申请人</span>
              <strong>{{ selectedApplication.applicantName || '-' }}</strong>
            </div>
            <div>
              <span>申请时间</span>
              <strong>{{ selectedApplication.appliedAtLabel }}</strong>
            </div>
            <div>
              <span>审核人</span>
              <strong>{{ selectedApplication.reviewerName || '-' }}</strong>
            </div>
          </section>

          <section class="admin-public-preview">
            <img :src="selectedApplication.coverResolved" :alt="selectedApplication.resourceName" />
            <div>
              <strong>{{ selectedApplication.resourceName }}</strong>
              <p>{{ selectedApplication.fileName || '-' }}</p>
              <div class="admin-public-preview-actions">
                <el-button class="plain" @click="openPreview(selectedApplication)">打开预览</el-button>
                <el-button class="plain" @click="copyFileLink(selectedApplication)">复制链接</el-button>
              </div>
            </div>
          </section>

          <section class="admin-public-detail-grid">
            <div class="admin-public-detail-card">
              <p>资源快照</p>
              <dl>
                <div><dt>资源类型</dt><dd>{{ selectedApplication.typeLabel }}</dd></div>
                <div><dt>所属专业</dt><dd>{{ selectedApplication.majorLabel }}</dd></div>
                <div><dt>课程名称</dt><dd>{{ selectedApplication.courseName || '-' }}</dd></div>
                <div><dt>文件大小</dt><dd>{{ selectedApplication.fileSizeLabel }}</dd></div>
                <div><dt>申请版本</dt><dd>V{{ selectedApplication.resourceVersion ?? 1 }}</dd></div>
                <div><dt>公开版本</dt><dd>V{{ selectedApplication.publicVersion ?? selectedApplication.resourceVersion ?? 1 }}</dd></div>
              </dl>
            </div>

            <div class="admin-public-detail-card">
              <p>审核信息</p>
              <dl>
                <div><dt>审核状态</dt><dd>{{ selectedApplication.statusLabel }}</dd></div>
                <div><dt>审核人</dt><dd>{{ selectedApplication.reviewerName || '-' }}</dd></div>
                <div class="wide"><dt>审核意见</dt><dd>{{ selectedApplication.reviewComment || '-' }}</dd></div>
              </dl>
            </div>
          </section>

          <section class="admin-public-log-card">
            <p>处理动作</p>
                      <div class="admin-public-panel-actions">
                        <el-button
                          v-if="selectedApplication.statusTone === 'pending'"
                          class="admin-public-panel-approve"
                          :loading="busyId === selectedApplication.applicationId"
                @click="openReview(selectedApplication, 'APPROVED')"
              >
                通过公开
              </el-button>
              <el-button
                v-if="selectedApplication.statusTone === 'pending'"
                class="admin-public-panel-reject"
                :loading="busyId === selectedApplication.applicationId"
                @click="openReview(selectedApplication, 'REJECTED')"
              >
                驳回申请
              </el-button>
              <el-button class="admin-public-panel-ghost" @click="selectApplication(selectedApplication)">保持选中</el-button>
            </div>
          </section>

          <section class="admin-public-timeline-card">
            <p>处理时间线</p>
            <article class="admin-public-timeline-item">
              <header>
                <strong>提交申请</strong>
                <span>{{ selectedApplication.appliedAtLabel }}</span>
              </header>
              <p>申请人 {{ selectedApplication.applicantName || '-' }} 提交了资源公开申请。</p>
            </article>
            <article class="admin-public-timeline-item">
              <header>
                <strong>当前状态</strong>
                <span>{{ selectedApplication.reviewedAtLabel || '-' }}</span>
              </header>
              <p>
                {{
                  selectedApplication.statusTone === 'pending'
                    ? '当前还在等待审核。'
                    : selectedApplication.statusTone === 'approved'
                      ? '申请已通过，资源已进入公开库。'
                      : '申请已驳回，可重新整理后再次提交。'
                }}
              </p>
            </article>
          </section>
        </aside>
      </div>
    </section>

    <el-dialog v-model="reviewVisible" class="admin-public-dialog" width="560px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-public-dialog-head">
          <strong>{{ reviewMode === 'APPROVED' ? '通过公开申请' : '驳回公开申请' }}</strong>
          <el-button text circle :icon="Close" @click="reviewVisible = false" />
        </div>
      </template>

      <div v-if="reviewTarget" class="admin-public-dialog-body">
        <div class="admin-public-dialog-summary">
          <strong>{{ reviewTarget.resourceName }}</strong>
          <span>{{ reviewTarget.applicantName || '-' }} · {{ reviewTarget.appliedAtLabel }}</span>
        </div>

        <label class="admin-public-dialog-field">
          <span>{{ reviewMode === 'APPROVED' ? '通过说明' : '驳回原因' }}</span>
          <el-input
            v-model="reviewComment"
            type="textarea"
            :rows="5"
            maxlength="120"
            show-word-limit
            :placeholder="reviewMode === 'APPROVED' ? '可填写通过说明' : '请填写驳回原因'"
          />
        </label>
      </div>

      <template #footer>
        <div class="admin-public-dialog-footer">
          <el-button @click="reviewVisible = false">取消</el-button>
          <el-button :type="reviewMode === 'APPROVED' ? 'primary' : 'danger'" :loading="saving" @click="submitReview">
            {{ reviewMode === 'APPROVED' ? '确认通过' : '确认驳回' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-drawer v-model="logDrawerVisible" class="admin-public-log-drawer" direction="rtl" size="560px" :with-header="false">
      <div class="admin-public-resource-drawer-head">
        <div>
          <p>申请记录</p>
          <h3>{{ logTarget?.resourceName || '资源申请记录' }}</h3>
        </div>
        <el-button text :icon="Close" @click="logDrawerVisible = false" />
      </div>

      <div v-if="logsLoading" class="admin-public-resource-empty drawer-state">记录加载中...</div>
      <template v-else>
        <article v-for="item in selectedLogs" :key="item.logId" class="admin-public-resource-log-row">
          <header>
            <strong>{{ item.action }}</strong>
            <span>{{ formatDateTime(item.createdAt) }}</span>
          </header>
          <p>{{ item.content }}</p>
          <small>{{ item.operatorName }}</small>
        </article>
        <div v-if="selectedLogs.length === 0" class="admin-public-resource-log-empty">暂无记录</div>
      </template>
    </el-drawer>

    <el-dialog v-model="previewVisible" class="admin-public-dialog" width="680px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-public-dialog-head">
          <strong>资源预览</strong>
          <el-button text circle :icon="Close" @click="previewVisible = false" />
        </div>
      </template>

      <div v-if="previewTarget" class="admin-public-preview-dialog">
        <img :src="previewTarget.coverResolved" :alt="previewTarget.resourceName" />
        <div>
          <strong>{{ previewTarget.resourceName }}</strong>
          <p>{{ previewTarget.fileName || previewTarget.fileUrl || '-' }}</p>
          <div class="admin-public-preview-dialog-actions">
            <el-button class="plain" @click="openExternalPreview(previewTarget)">新窗口打开</el-button>
            <el-button class="plain" @click="copyFileLink(previewTarget)">复制链接</el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="batchReviewVisible" class="admin-public-dialog" width="560px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-public-dialog-head">
          <strong>{{ batchReviewMode === 'APPROVED' ? '批量通过' : '批量驳回' }}</strong>
          <el-button text circle :icon="Close" @click="batchReviewVisible = false" />
        </div>
      </template>

      <div class="admin-public-dialog-summary">
        <strong>已选 {{ selectedIds.length }} 条申请</strong>
        <span>{{ batchReviewMode === 'APPROVED' ? '将统一通过并进入公开流程' : '将统一驳回并保留历史记录' }}</span>
      </div>

      <label class="admin-public-dialog-field">
        <span>{{ batchReviewMode === 'APPROVED' ? '通过说明' : '驳回原因' }}</span>
        <el-input
          v-model="batchReviewComment"
          type="textarea"
          :rows="5"
          maxlength="120"
          show-word-limit
          :placeholder="batchReviewMode === 'APPROVED' ? '可填写统一通过说明' : '请填写统一驳回原因'"
        />
      </label>

      <template #footer>
        <div class="admin-public-dialog-footer">
          <el-button @click="batchReviewVisible = false">取消</el-button>
          <el-button :type="batchReviewMode === 'APPROVED' ? 'primary' : 'danger'" :loading="saving" @click="submitBatchReview">
            {{ batchReviewMode === 'APPROVED' ? '确认通过' : '确认驳回' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Close, Search } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  approveAdminPublicApplication,
  fetchAdminPublicApplication,
  fetchAdminPublicApplications,
  fetchAdminResourceLogs,
  rejectAdminPublicApplication,
  type AdminPublicApplication,
  type AdminResourceLog,
  type AdminResourceQuery
} from '../../api/admin-resource';
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
const reviewVisible = ref(false);
const reviewMode = ref<ReviewMode>('APPROVED');
const reviewTarget = ref<PublicApplicationRow | null>(null);
const reviewComment = ref('');
const logDrawerVisible = ref(false);
const logTarget = ref<PublicApplicationRow | null>(null);
const logsLoading = ref(false);
const selectedLogs = ref<AdminResourceLog[]>([]);
const previewVisible = ref(false);
const previewTarget = ref<PublicApplicationRow | null>(null);
const selectedIds = ref<number[]>([]);
const batchReviewVisible = ref(false);
const batchReviewMode = ref<ReviewMode>('APPROVED');
const batchReviewComment = ref('');

const draft = reactive({
  keyword: '',
  resourceType: '',
  majorId: null as number | null,
  publicStatus: '' as '' | ApplicationStatus,
  appliedRange: [] as string[]
});

const appliedFilters = ref({ ...draft, appliedRange: [] as string[] });

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
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
];

const filteredApplications = computed(() =>
  applications.value.filter((item) => {
    const keyword = appliedFilters.value.keyword.trim().toLowerCase();
    const matchesKeyword =
      !keyword ||
      [item.resourceName, item.courseName, item.applicantName, item.reviewerName, item.fileName, item.majorLabel].some((text) =>
        String(text || '').toLowerCase().includes(keyword)
      );
    const matchesType = !appliedFilters.value.resourceType || item.resourceType === appliedFilters.value.resourceType;
    const matchesMajor = !appliedFilters.value.majorId || item.majorId === appliedFilters.value.majorId;
    const matchesStatus = !appliedFilters.value.publicStatus || item.publicStatus === appliedFilters.value.publicStatus;
    const [startDate, endDate] = appliedFilters.value.appliedRange;
    const applyDate = item.appliedAtLabel.slice(0, 10);
    const matchesDate = (!startDate || applyDate >= startDate) && (!endDate || applyDate <= endDate);
    return matchesKeyword && matchesType && matchesMajor && matchesStatus && matchesDate;
  })
);

const totalCount = computed(() => filteredApplications.value.length);
const pendingCount = computed(() => filteredApplications.value.filter((item) => item.statusTone === 'pending').length);
const approvedCount = computed(() => filteredApplications.value.filter((item) => item.statusTone === 'approved').length);
const rejectedCount = computed(() => filteredApplications.value.filter((item) => item.statusTone === 'rejected').length);
const pagedApplications = computed(() => filteredApplications.value.slice((page.value - 1) * pageSize, page.value * pageSize));
const allCurrentSelected = computed(() => pagedApplications.value.length > 0 && pagedApplications.value.every((item) => selectedIds.value.includes(item.applicationId)));
const partCurrentSelected = computed(() => selectedIds.value.length > 0 && !allCurrentSelected.value);

function mockApplications(): AdminPublicApplication[] {
  return [
    {
      applicationId: 8101,
      publicResourceId: 9101,
      resourceId: 5101,
      resourceVersion: 3,
      resourceName: '城轨运营基础教学课件',
      resourceType: '演示文稿',
      coverUrl: coverForResourceType('演示文稿'),
      fileUrl: 'https://example.com/resource/5101',
      previewUrl: 'https://example.com/resource/5101/preview',
      fileName: '城轨运营基础教学课件-v3.pptx',
      fileSize: 18600,
      majorId: 1,
      majorName: '城市轨道交通运营管理',
      courseName: '城市轨道交通概论',
      applicantName: '王老师',
      reviewerName: '周老师',
      reviewComment: '内容完整，适合公开。',
      publicStatus: 'APPROVED',
      appliedAt: '2025-03-18 10:20',
      reviewedAt: '2025-03-18 15:20'
    },
    {
      applicationId: 8102,
      publicResourceId: 9102,
      resourceId: 5102,
      resourceVersion: 1,
      resourceName: 'CBTC系统原理讲解视频',
      resourceType: '视频',
      coverUrl: coverForResourceType('视频'),
      fileUrl: 'https://example.com/resource/5102',
      previewUrl: 'https://example.com/resource/5102/preview',
      fileName: 'CBTC系统原理讲解视频.mp4',
      fileSize: 246000,
      majorId: 4,
      majorName: '城市轨道交通通信信号技术',
      courseName: '城市轨道交通信号系统',
      applicantName: '李老师',
      publicStatus: 'PENDING',
      appliedAt: '2025-03-20 14:05'
    },
    {
      applicationId: 8103,
      publicResourceId: 9103,
      resourceId: 5103,
      resourceVersion: 2,
      resourceName: '车辆构造高清图集',
      resourceType: '图片',
      coverUrl: coverForResourceType('图片'),
      fileUrl: 'https://example.com/resource/5103',
      previewUrl: 'https://example.com/resource/5103/preview',
      fileName: '车辆构造高清图集.zip',
      fileSize: 32400,
      majorId: 2,
      majorName: '城市轨道交通车辆技术',
      courseName: '城轨车辆构造',
      applicantName: '赵老师',
      reviewerName: '陈老师',
      reviewComment: '图片清晰，但说明文档还要补充。',
      publicStatus: 'REJECTED',
      appliedAt: '2025-03-21 09:48',
      reviewedAt: '2025-03-21 11:10'
    },
    {
      applicationId: 8104,
      publicResourceId: 9104,
      resourceId: 5104,
      resourceVersion: 1,
      resourceName: '车站运营管理标准手册',
      resourceType: '文本文档',
      coverUrl: coverForResourceType('文本文档'),
      fileUrl: 'https://example.com/resource/5104',
      previewUrl: 'https://example.com/resource/5104/preview',
      fileName: '车站运营管理标准手册.pdf',
      fileSize: 9800,
      majorId: 1,
      majorName: '城市轨道交通运营管理',
      courseName: '车站运营管理',
      applicantName: '王老师',
      publicStatus: 'PENDING',
      appliedAt: '2025-03-16 11:20'
    },
    {
      applicationId: 8105,
      publicResourceId: 9105,
      resourceId: 5105,
      resourceVersion: 4,
      resourceName: '供电系统故障案例分析',
      resourceType: '音频',
      coverUrl: coverForResourceType('音频'),
      fileUrl: 'https://example.com/resource/5105',
      previewUrl: 'https://example.com/resource/5105/preview',
      fileName: '供电系统故障案例分析.mp3',
      fileSize: 86200,
      majorId: 3,
      majorName: '城市轨道交通机电技术',
      courseName: '城轨供电系统',
      applicantName: '陈老师',
      reviewerName: '李老师',
      reviewComment: '通过，内容与课程匹配。',
      publicStatus: 'APPROVED',
      appliedAt: '2025-03-15 16:10',
      reviewedAt: '2025-03-16 09:12'
    },
    {
      applicationId: 8106,
      publicResourceId: 9106,
      resourceId: 5106,
      resourceVersion: 1,
      resourceName: '车站值班员实训试题库',
      resourceType: '实训试题',
      coverUrl: coverForResourceType('实训试题'),
      fileUrl: 'https://example.com/resource/5106',
      previewUrl: 'https://example.com/resource/5106/preview',
      fileName: '车站值班员实训试题库.xlsx',
      fileSize: 12500,
      majorId: 4,
      majorName: '城市轨道交通通信信号技术',
      courseName: '信号设备维护',
      applicantName: '刘老师',
      publicStatus: 'PENDING',
      appliedAt: '2025-03-13 18:15'
    }
  ];
}

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
    return '已通过';
  }
  if (status === 'REJECTED') {
    return '已驳回';
  }
  return '待审核';
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

function mapApplicationRow(application: AdminPublicApplication): PublicApplicationRow {
  const typeLabel = normalizeResourceType(application.resourceType);
  const status = normalizeStatus(application.publicStatus);
  const majorLabel = application.majorName || findMajorLabel(application.majorId);
  return {
    ...application,
    resourceType: typeLabel,
    coverUrl: application.coverUrl || coverForResourceType(typeLabel),
    coverResolved: application.coverUrl || coverForResourceType(typeLabel),
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

function mergeSelectedIds(ids: number[]) {
  selectedIds.value = Array.from(new Set([...selectedIds.value, ...ids]));
}

function createEmptyFilters() {
  return {
    keyword: '',
    resourceType: '',
    majorId: null as number | null,
    publicStatus: '' as '' | ApplicationStatus,
    appliedRange: [] as string[]
  };
}

function selectApplication(application: PublicApplicationRow) {
  selectedApplication.value = application;
  void loadApplicationDetail(application);
}

function openReview(application: PublicApplicationRow, mode: ReviewMode) {
  reviewTarget.value = application;
  reviewMode.value = mode;
  reviewComment.value = '';
  reviewVisible.value = true;
}

function openBatchReview(mode: ReviewMode) {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择申请');
    return;
  }
  batchReviewMode.value = mode;
  batchReviewComment.value = '';
  batchReviewVisible.value = true;
}

function toggleOne(applicationId: number) {
  selectedIds.value = selectedIds.value.includes(applicationId)
    ? selectedIds.value.filter((id) => id !== applicationId)
    : [...selectedIds.value, applicationId];
}

function toggleAllCurrent(value: string | number | boolean) {
  if (!value) {
    selectedIds.value = selectedIds.value.filter((id) => !pagedApplications.value.some((item) => item.applicationId === id));
    return;
  }
  mergeSelectedIds(pagedApplications.value.map((item) => item.applicationId));
}

function applyFilters() {
  appliedFilters.value = {
    keyword: draft.keyword,
    resourceType: draft.resourceType,
    majorId: draft.majorId,
    publicStatus: draft.publicStatus,
    appliedRange: [...draft.appliedRange]
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
    resourceType: appliedFilters.value.resourceType || undefined,
    majorId: appliedFilters.value.majorId ?? undefined,
    publicStatus: appliedFilters.value.publicStatus || undefined,
    uploadStartDate: appliedFilters.value.appliedRange[0],
    uploadEndDate: appliedFilters.value.appliedRange[1],
    page: 1,
    pageSize: 999
  };
}

async function loadApplications() {
  loading.value = true;
  try {
    const result = await fetchAdminPublicApplications(buildQuery());
    const mapped = result.records.map(mapApplicationRow);
    applications.value = mapped.length > 0 ? mapped : mockApplications().map(mapApplicationRow);
    if (!selectedApplication.value || !applications.value.some((item) => item.applicationId === selectedApplication.value?.applicationId)) {
      selectedApplication.value = applications.value[0] ?? null;
    }
    selectedIds.value = selectedIds.value.filter((id) => applications.value.some((item) => item.applicationId === id));
    if (selectedApplication.value) {
      void loadApplicationDetail(selectedApplication.value);
    }
  } catch {
    applications.value = mockApplications().map(mapApplicationRow);
    if (!selectedApplication.value || !applications.value.some((item) => item.applicationId === selectedApplication.value?.applicationId)) {
      selectedApplication.value = applications.value[0] ?? null;
    }
    selectedIds.value = selectedIds.value.filter((id) => applications.value.some((item) => item.applicationId === id));
    if (selectedApplication.value) {
      void loadApplicationDetail(selectedApplication.value);
    }
  } finally {
    loading.value = false;
  }
}

async function refreshList() {
  await loadApplications();
}

async function loadApplicationDetail(application: PublicApplicationRow) {
  try {
    const detail = await fetchAdminPublicApplication(application.applicationId);
    selectedApplication.value = mapApplicationRow(detail);
  } catch {
    selectedApplication.value = application;
  }
}

async function openLogs(application: PublicApplicationRow) {
  logTarget.value = application;
  logDrawerVisible.value = true;
  logsLoading.value = true;
  try {
    selectedLogs.value = await fetchAdminResourceLogs(application.resourceId);
  } catch {
    selectedLogs.value = [];
  } finally {
    logsLoading.value = false;
  }
}

function openPreview(application: PublicApplicationRow) {
  previewTarget.value = application;
  previewVisible.value = true;
}

function openExternalPreview(application: PublicApplicationRow) {
  const url = application.previewUrl || application.fileUrl;
  if (url) {
    window.open(url, '_blank', 'noopener');
    return;
  }
  ElMessage.info(`正在打开资源：${application.resourceName}`);
}

function copyFileLink(application: PublicApplicationRow) {
  const url = application.fileUrl || application.previewUrl;
  if (!url) {
    ElMessage.warning('暂无可复制链接');
    return;
  }
  void navigator.clipboard?.writeText(url);
  ElMessage.success('链接已复制');
}

async function submitBatchReview() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择申请');
    return;
  }

  if (batchReviewMode.value === 'REJECTED' && !batchReviewComment.value.trim()) {
    ElMessage.warning('请填写驳回原因');
    return;
  }

  try {
    await ElMessageBox.confirm(
      batchReviewMode.value === 'APPROVED'
        ? `确认批量通过 ${selectedIds.value.length} 条公开申请？`
        : `确认批量驳回 ${selectedIds.value.length} 条公开申请？`,
      batchReviewMode.value === 'APPROVED' ? '批量通过' : '批量驳回',
      {
        confirmButtonText: batchReviewMode.value === 'APPROVED' ? '通过' : '驳回',
        cancelButtonText: '取消',
        type: batchReviewMode.value === 'APPROVED' ? 'success' : 'warning'
      }
    );
  } catch {
    return;
  }

  saving.value = true;
  try {
    const targets = applications.value.filter((item) => selectedIds.value.includes(item.applicationId));
    await Promise.all(
      targets.map((item) =>
        batchReviewMode.value === 'APPROVED'
          ? approveAdminPublicApplication(item.applicationId, {
              reviewComment: batchReviewComment.value.trim() || undefined
            })
          : rejectAdminPublicApplication(item.applicationId, {
              reviewComment: batchReviewComment.value.trim()
            })
      )
    );
    ElMessage.success(batchReviewMode.value === 'APPROVED' ? '批量通过成功' : '批量驳回成功');
    batchReviewVisible.value = false;
    selectedIds.value = [];
    await loadApplications();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '批量审核失败');
  } finally {
    saving.value = false;
  }
}

async function submitReview() {
  if (!reviewTarget.value) {
    return;
  }

  const application = reviewTarget.value;
  if (reviewMode.value === 'REJECTED' && !reviewComment.value.trim()) {
    ElMessage.warning('请填写驳回原因');
    return;
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
    return;
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
    reviewVisible.value = false;
    await loadApplications();
    const nextStatus = reviewMode.value;
    selectedApplication.value = {
      ...application,
      publicStatus: nextStatus,
      statusTone: statusTone(nextStatus),
      statusLabel: statusLabel(nextStatus),
      reviewerName: application.reviewerName || '当前管理员',
      reviewedAtLabel: formatDateTime(new Date().toISOString()),
      reviewComment: reviewComment.value.trim() || application.reviewComment || ''
    };
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '审核失败');
  } finally {
    busyId.value = null;
    saving.value = false;
  }
}

onMounted(() => {
  void loadApplications();
});
</script>
