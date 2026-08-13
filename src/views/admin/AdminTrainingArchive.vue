<template>
  <AdminShell activeKey="training-archive">
    <section v-if="viewMode === 'list'" class="admin-training-archive-page">
      <el-breadcrumb class="admin-training-archive-breadcrumb" separator="/">
        <el-breadcrumb-item>成绩统计</el-breadcrumb-item>
        <el-breadcrumb-item>实训档案</el-breadcrumb-item>
      </el-breadcrumb>

      <section class="admin-training-archive-filter-card">
        <el-select v-model="draft.className" placeholder="请选择班级" clearable>
          <el-option v-for="item in classOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-input v-model="draft.studentNo" :prefix-icon="Search" placeholder="学号搜索" clearable @keyup.enter="applyFilters" />
        <el-input v-model="draft.studentName" :prefix-icon="Search" placeholder="姓名搜索" clearable @keyup.enter="applyFilters" />
        <el-button class="admin-training-archive-query" @click="applyFilters">查询</el-button>
        <el-button class="admin-training-archive-reset" @click="resetFilters">重置</el-button>
      </section>

      <section v-loading="loading" class="admin-training-archive-board">
        <table class="admin-training-archive-table">
          <thead>
            <tr>
              <th>序号</th>
              <th>班级</th>
              <th>学号</th>
              <th>姓名</th>
              <th>实训名称</th>
              <th>实训模式</th>
              <th>角色</th>
              <th>提交时间</th>
              <th>提交类型</th>
              <th>时长（秒）</th>
              <th>个人得分</th>
              <th>整队总分</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, index) in pagedArchives" :key="row.id">
              <td>{{ (page - 1) * pageSize + index + 1 }}</td>
              <td>{{ row.className }}</td>
              <td>{{ row.studentNo }}</td>
              <td><strong>{{ row.studentName }}</strong></td>
              <td>{{ row.trainingName }}</td>
              <td>{{ row.trainingMode }}</td>
              <td>{{ row.roleName }}</td>
              <td>{{ row.submittedAt }}</td>
              <td>{{ row.submitType }}</td>
              <td>{{ row.durationSeconds }}</td>
              <td><b>{{ row.personalScore }}</b></td>
              <td><b>{{ row.teamScore }}</b></td>
              <td><el-button text class="admin-training-archive-detail-button" @click="openDetail(row)">查看详情</el-button></td>
            </tr>
          </tbody>
        </table>

        <footer class="admin-training-archive-footer">
          <span></span>
          <el-pagination v-model:current-page="page" :page-size="pageSize" :total="total" layout="prev, pager, next" background @current-change="loadArchives" />
        </footer>
      </section>
    </section>

    <section v-else class="admin-training-archive-detail-page">
      <header class="admin-training-archive-detail-top">
        <button type="button" class="admin-training-archive-back" @click="backToList">
          <el-icon><Back /></el-icon>
        </button>
        <el-breadcrumb class="admin-training-archive-detail-breadcrumb" separator="/">
          <el-breadcrumb-item>成绩统计</el-breadcrumb-item>
          <el-breadcrumb-item>实训档案</el-breadcrumb-item>
          <el-breadcrumb-item>查看详情</el-breadcrumb-item>
        </el-breadcrumb>
      </header>

      <section v-if="activeArchive" class="admin-training-archive-student-card">
        <span>学生姓名：<b>{{ activeArchive.studentName }}</b></span>
        <span>学生学号：<b>{{ activeArchive.studentNo }}</b></span>
        <span>所属班级：<b>{{ activeArchive.className }}</b></span>
        <span>提交时间：<b>{{ activeArchive.submittedAt }}</b></span>
      </section>

      <section v-if="activeArchive" class="admin-training-archive-title-card">
        <h1>{{ activeArchive.detailTitle }}</h1>
        <p>{{ activeArchive.trainingMode }}</p>
      </section>

      <section class="admin-training-archive-detail-grid">
        <article class="admin-training-archive-step-card">
          <header>
            <strong><i></i>实训步骤详情</strong>
            <span>点击步骤名可以看对应操作视频</span>
          </header>
          <table>
            <thead>
              <tr>
                <th>序号</th>
                <th>步骤名称</th>
                <th>正确结果</th>
                <th>实际操作</th>
                <th>得分</th>
                <th>用时(秒)</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(step, index) in archiveSteps" :key="step.name">
                <td>{{ index + 1 }}</td>
                <td><button type="button" @click="openStepVideo(step)">{{ step.name }}</button></td>
                <td><span class="admin-training-archive-pill blue">{{ step.expected }}</span></td>
                <td><span class="admin-training-archive-pill" :class="step.score > 0 ? 'green' : 'red'">{{ step.actual }}</span></td>
                <td><b :class="step.score > 0 ? 'pass' : 'fail'">{{ step.score }}</b></td>
                <td>{{ step.seconds }}</td>
              </tr>
            </tbody>
          </table>
        </article>

        <aside class="admin-training-archive-video-card">
          <header><strong><i></i>实训操作视频</strong></header>
          <button v-if="activeArchive?.recordingUrl" type="button" class="admin-training-archive-video" @click="openVideoPreview">
            <span class="play"></span>
            <span class="track"><b></b></span>
            <em class="time start">00:00</em>
            <em class="time end">{{ formatDuration(activeArchive?.durationSeconds) }}</em>
            <span class="controls">
              <i class="pause"></i>
              <i class="volume"></i>
              <i class="bar"></i>
              <i class="screen"></i>
            </span>
          </button>
          <el-empty v-else description="暂无实训操作视频" />
        </aside>
      </section>

      <el-dialog v-model="videoVisible" class="admin-training-archive-video-dialog" width="780px" :show-close="false" append-to-body>
        <template #header>
          <div class="admin-training-archive-dialog-head">
            <strong>{{ previewTitle }}</strong>
            <el-button text circle :icon="Close" @click="videoVisible = false" />
          </div>
        </template>
        <button type="button" class="admin-training-archive-video is-dialog" @click="videoVisible = false">
          <span class="play"></span>
          <span class="track"><b></b></span>
          <em class="time start">00:00</em>
          <em class="time end">{{ formatDuration(activeArchive?.durationSeconds) }}</em>
          <span class="controls">
            <i class="pause"></i>
            <i class="volume"></i>
            <i class="bar"></i>
            <i class="screen"></i>
          </span>
        </button>
      </el-dialog>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Back, Close, Search } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  fetchAdminTrainingArchiveDetail,
  fetchAdminTrainingArchives,
  type AdminTrainingArchive,
  type AdminTrainingArchiveStep
} from '../../api/admin-archive';

interface TrainingArchiveRow {
  id: number;
  className: string;
  studentNo: string;
  studentName: string;
  trainingName: string;
  detailTitle: string;
  trainingMode: string;
  roleName: string;
  submittedAt: string;
  submitType: string;
  durationSeconds: number;
  personalScore: string;
  teamScore: string;
  recordingUrl?: string;
}

interface ArchiveStep {
  name: string;
  expected: string;
  actual: string;
  score: number;
  seconds: number;
}

const page = ref(1);
const pageSize = 10;
const total = ref(0);
const loading = ref(false);
const viewMode = ref<'list' | 'detail'>('list');
const activeArchive = ref<TrainingArchiveRow | null>(null);
const videoVisible = ref(false);
const previewTitle = ref('实训操作视频');
const draft = reactive({ className: '', studentNo: '', studentName: '' });
const applied = ref({ ...draft });

const archives = ref<TrainingArchiveRow[]>([]);
const archiveSteps = ref<ArchiveStep[]>([]);

const classOptions = computed(() => Array.from(new Set(archives.value.map((item) => item.className))));
const pagedArchives = computed(() => archives.value);

function applyFilters() {
  applied.value = { ...draft };
  page.value = 1;
  void loadArchives();
}

function resetFilters() {
  Object.assign(draft, { className: '', studentNo: '', studentName: '' });
  applyFilters();
}

async function openDetail(row: TrainingArchiveRow) {
  try {
    const detail = await fetchAdminTrainingArchiveDetail(row.id);
    activeArchive.value = mapArchive(detail);
    archiveSteps.value = (detail.steps || []).map(mapStep);
  } catch (error) {
    activeArchive.value = row;
    archiveSteps.value = [];
    ElMessage.error(error instanceof Error ? error.message : '实训档案详情加载失败');
  }
  viewMode.value = 'detail';
}

function backToList() {
  viewMode.value = 'list';
}

function openVideoPreview() {
  const recordingUrl = activeArchive.value?.recordingUrl;
  if (!recordingUrl) {
    return;
  }
  window.open(recordingUrl, '_blank', 'noopener');
}

function openStepVideo(step: ArchiveStep) {
  if (!activeArchive.value?.recordingUrl) {
    ElMessage.info('暂无该步骤的操作视频');
    return;
  }
  previewTitle.value = `${step.name} - 操作视频`;
  videoVisible.value = true;
}

async function loadArchives() {
  loading.value = true;
  try {
    const keyword = [applied.value.className, applied.value.studentNo, applied.value.studentName]
      .map((item) => item.trim())
      .filter(Boolean)
      .join(' ');
    const result = await fetchAdminTrainingArchives({
      keyword: keyword || undefined,
      page: page.value,
      pageSize
    });
    archives.value = result.records.map(mapArchive);
    total.value = result.total;
  } catch (error) {
    archives.value = [];
    total.value = 0;
    ElMessage.error(error instanceof Error ? error.message : '实训档案加载失败');
  } finally {
    loading.value = false;
  }
}

function mapArchive(item: AdminTrainingArchive): TrainingArchiveRow {
  const title = item.trainingName || '-';
  return {
    id: item.archiveId,
    className: item.className || '-',
    studentNo: item.studentNo || '-',
    studentName: item.studentName || '-',
    trainingName: title,
    detailTitle: item.roleName ? `${title}（${item.roleName}）` : title,
    trainingMode: item.trainingMode || '-',
    roleName: item.roleName || '-',
    submittedAt: formatDateTime(item.submittedAt),
    submitType: item.submitType || '-',
    durationSeconds: Number(item.durationSeconds || 0),
    personalScore: formatScore(item.personalScore),
    teamScore: item.teamScore === undefined || item.teamScore === null ? '-' : formatScore(item.teamScore),
    recordingUrl: (item as { recordingUrl?: string }).recordingUrl
  };
}

function mapStep(step: AdminTrainingArchiveStep): ArchiveStep {
  return {
    name: step.stepName || '-',
    expected: step.standardOperation || '-',
    actual: step.actualOperation || '-',
    score: Number(step.score || 0),
    seconds: Number(step.durationSeconds || 0)
  };
}

function formatDateTime(value?: string) {
  if (!value) {
    return '-';
  }
  return value.includes('T') ? value.replace('T', ' ').slice(0, 16) : value.slice(0, 16);
}

function formatScore(value?: number | string) {
  const score = Number(value || 0);
  return Number.isInteger(score) ? String(score) : score.toFixed(1);
}

function formatDuration(value?: number) {
  const totalSeconds = Math.max(0, Math.round(Number(value || 0)));
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
}

onMounted(() => {
  void loadArchives();
});
</script>
