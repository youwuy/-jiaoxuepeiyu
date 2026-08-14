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

      <section v-if="activeArchive" v-loading="detailLoading" class="admin-training-archive-workspace">
        <aside class="admin-training-archive-history">
          <header><strong>实训历史记录</strong><span>共 {{ studentHistory.length }} 条</span></header>
          <div class="admin-training-archive-history-list">
            <button
              v-for="item in studentHistory"
              :key="item.id"
              type="button"
              :class="{ active: item.id === activeArchive.id }"
              @click="selectHistory(item)"
            >
              <strong>{{ item.trainingName }}</strong>
              <span>{{ item.submittedAt }}</span>
              <b>{{ item.personalScore }} 分</b>
            </button>
          </div>
        </aside>

        <div class="admin-training-archive-detail-main">
          <section class="admin-training-archive-student-card">
            <span>学生姓名：<b>{{ activeArchive.studentName }}</b></span>
            <span>学生学号：<b>{{ activeArchive.studentNo }}</b></span>
            <span>所属班级：<b>{{ activeArchive.className }}</b></span>
            <span>提交时间：<b>{{ activeArchive.submittedAt }}</b></span>
          </section>

          <section class="admin-training-archive-title-card">
            <div><h1>{{ activeArchive.detailTitle }}</h1><p>{{ activeArchive.trainingMode }}</p></div>
            <div class="admin-training-archive-summary">
              <span>实训成绩<b>{{ activeArchive.personalScore }} 分</b></span>
              <span>成绩等级<b>{{ archiveGrade }}</b></span>
              <span>训练时长<b>{{ activeArchive.durationSeconds }} 秒</b></span>
              <span>出错步骤<b>{{ archiveErrorCount }} 个</b></span>
            </div>
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
            <tbody ref="archiveStepBody">
              <tr v-if="archiveSteps.length === 0" class="admin-training-archive-empty-row">
                <td colspan="6">暂无实训步骤记录</td>
              </tr>
              <tr v-for="(step, index) in archiveSteps" :key="step.id" :class="{ active: activeStepIndex === index, error: step.isError }">
                <td>{{ index + 1 }}</td>
                <td><button type="button" @click="seekStep(step, index)">{{ step.name }}</button></td>
                <td><span class="admin-training-archive-pill blue">{{ step.expected }}</span></td>
                <td><span class="admin-training-archive-pill" :class="step.isError ? 'red' : 'green'">{{ step.actual }}</span></td>
                <td><b :class="step.isError ? 'fail' : 'pass'">{{ formatStepScore(step) }}</b></td>
                <td>{{ step.seconds }}</td>
              </tr>
            </tbody>
          </table>
        </article>

        <aside class="admin-training-archive-video-card">
          <header><strong><i></i>实训操作视频</strong></header>
          <div v-if="activeArchive?.recordingUrl" class="admin-training-archive-player-shell">
            <video
              ref="archiveVideo"
              class="admin-training-archive-player"
              :src="activeArchive.recordingUrl"
              controls
              preload="metadata"
              @loadedmetadata="syncActiveStep"
              @timeupdate="syncActiveStep"
              @seeked="syncActiveStep"
            />
            <p>{{ activeStepIndex >= 0 ? `当前步骤：${archiveSteps[activeStepIndex]?.name}` : '拖动进度条可同步定位操作步骤' }}</p>
          </div>
          <el-empty v-else description="本次实训未上传录屏，无法播放回放" />
        </aside>
          </section>
        </div>
      </section>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Back, Search } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import { resolvePublicUrl } from '../../api/http';
import { fetchAdminScoreGradeRules, type AdminScoreGradeRule } from '../../api/admin-settings';
import {
  fetchAdminTrainingArchiveDetail,
  fetchAdminTrainingArchives,
  type AdminTrainingArchive,
  type AdminTrainingArchiveStep
} from '../../api/admin-archive';

interface TrainingArchiveRow {
  id: number;
  studentId: number;
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
  id: number;
  name: string;
  expected: string;
  actual: string;
  score: number;
  maxScore: number;
  seconds: number;
  videoStartSecond: number;
  videoEndSecond: number;
  isError: boolean;
}

const page = ref(1);
const pageSize = 10;
const total = ref(0);
const loading = ref(false);
const detailLoading = ref(false);
const viewMode = ref<'list' | 'detail'>('list');
const activeArchive = ref<TrainingArchiveRow | null>(null);
const archiveVideo = ref<HTMLVideoElement>();
const archiveStepBody = ref<HTMLElement>();
const activeStepIndex = ref(-1);
const draft = reactive({ className: '', studentNo: '', studentName: '' });
const applied = ref({ ...draft });

const archives = ref<TrainingArchiveRow[]>([]);
const studentHistory = ref<TrainingArchiveRow[]>([]);
const archiveSteps = ref<ArchiveStep[]>([]);
const gradeRules = ref<AdminScoreGradeRule[]>([]);

const classOptions = computed(() => Array.from(new Set(archives.value.map((item) => item.className))));
const pagedArchives = computed(() => archives.value);
const archiveErrorCount = computed(() => archiveSteps.value.filter((step) => step.isError).length);
const archiveGrade = computed(() => {
  const total = archiveSteps.value.reduce((sum, step) => sum + step.maxScore, 0);
  const score = Number(activeArchive.value?.personalScore || 0);
  if (total <= 0 || gradeRules.value.length === 0) return '-';
  const percentage = (score / total) * 100;
  return gradeRules.value.find((rule) => percentage >= rule.minScore && percentage <= rule.maxScore)?.gradeName || '-';
});

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
  viewMode.value = 'detail';
  activeArchive.value = row;
  studentHistory.value = [row];
  try {
    studentHistory.value = await loadStudentHistory(row.studentId);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '实训历史记录加载失败');
  }
  await selectHistory(row);
}

async function loadStudentHistory(studentId: number) {
  const records: TrainingArchiveRow[] = [];
  let historyPage = 1;
  let historyTotal = 0;
  do {
    const result = await fetchAdminTrainingArchives({ studentId, page: historyPage, pageSize: 100 });
    const pageRecords = result.records.map(mapArchive);
    records.push(...pageRecords);
    historyTotal = result.total;
    historyPage += 1;
    if (pageRecords.length === 0) break;
  } while (records.length < historyTotal);
  return records;
}

async function selectHistory(row: TrainingArchiveRow) {
  archiveVideo.value?.pause();
  detailLoading.value = true;
  try {
    const detail = await fetchAdminTrainingArchiveDetail(row.id);
    activeArchive.value = mapArchive(detail);
    archiveSteps.value = (detail.steps || []).map(mapStep);
    activeStepIndex.value = -1;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '实训档案详情加载失败');
  } finally {
    detailLoading.value = false;
  }
}

async function loadGradeRules() {
  try {
    gradeRules.value = await fetchAdminScoreGradeRules();
  } catch (error) {
    gradeRules.value = [];
    ElMessage.error(error instanceof Error ? error.message : '成绩等级配置加载失败');
  }
}

function backToList() {
  archiveVideo.value?.pause();
  viewMode.value = 'list';
}

async function seekStep(step: ArchiveStep, index: number) {
  if (!activeArchive.value?.recordingUrl) {
    ElMessage.info('暂无该步骤的操作视频');
    return;
  }
  const video = archiveVideo.value;
  if (!video) return;
  video.currentTime = Math.max(0, step.videoStartSecond);
  setActiveStep(index);
  try {
    await video.play();
  } catch {
    ElMessage.info('浏览器已阻止自动播放，请点击播放器开始播放');
  }
}

function syncActiveStep() {
  const video = archiveVideo.value;
  if (!video || !archiveSteps.value.length) return;
  const currentTime = video.currentTime;
  let matchedIndex = -1;
  archiveSteps.value.forEach((step, index) => {
    const nextStart = archiveSteps.value[index + 1]?.videoStartSecond;
    const end = step.videoEndSecond > step.videoStartSecond
      ? step.videoEndSecond
      : (nextStart ?? Number.POSITIVE_INFINITY);
    if (currentTime >= step.videoStartSecond && currentTime < end) matchedIndex = index;
  });
  if (matchedIndex < 0) {
    archiveSteps.value.forEach((step, index) => {
      if (currentTime >= step.videoStartSecond) matchedIndex = index;
    });
  }
  if (matchedIndex >= 0 && matchedIndex !== activeStepIndex.value) setActiveStep(matchedIndex);
}

function setActiveStep(index: number) {
  activeStepIndex.value = index;
  requestAnimationFrame(() => {
    archiveStepBody.value?.querySelectorAll('tr')[index]?.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
  });
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
    studentId: Number(item.studentId || 0),
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
    recordingUrl: resolvePublicUrl((item as { recordingUrl?: string }).recordingUrl)
  };
}

function mapStep(step: AdminTrainingArchiveStep): ArchiveStep {
  const score = Number(step.score || 0);
  const maxScore = Number(step.maxScore || 0);
  return {
    id: Number(step.stepId || 0),
    name: step.stepName || '-',
    expected: step.standardOperation || '-',
    actual: step.actualOperation || '-',
    score,
    maxScore,
    seconds: Number(step.durationSeconds || 0),
    videoStartSecond: Number(step.videoStartSecond || 0),
    videoEndSecond: Number(step.videoEndSecond || 0),
    isError: maxScore > 0 ? score < maxScore : false
  };
}

function formatStepScore(step: ArchiveStep) {
  return step.maxScore > 0 ? `${formatScore(step.score)} / ${formatScore(step.maxScore)}` : formatScore(step.score);
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

onMounted(() => {
  void loadArchives();
  void loadGradeRules();
});
</script>
