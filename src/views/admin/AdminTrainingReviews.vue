<template>
  <AdminShell activeKey="admin-trainings">
    <section class="admin-course-reviews-page admin-training-reviews-page">
      <header class="admin-course-reviews-topbar">
        <div class="admin-course-reviews-left">
          <el-button class="admin-course-reviews-back" :icon="ArrowLeft" @click="goBack" />
          <el-breadcrumb class="admin-course-reviews-breadcrumb" separator="/">
            <el-breadcrumb-item>教学实训</el-breadcrumb-item>
            <el-breadcrumb-item>实训组课</el-breadcrumb-item>
            <el-breadcrumb-item>阅卷</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>{{ trainingTitle }} - 阅卷</h1>
        <span></span>
      </header>

      <div class="training-review-workbench">
      <aside class="training-review-topics">
        <strong>实训任务</strong>
        <button v-for="topic in topics" :key="topic.id" :class="{ active: activeTopicId === topic.id }" @click="selectTopic(topic.id)">
          <span>{{ topic.name }}</span><small>{{ topic.mode }}</small>
        </button>
      </aside>
      <div class="training-review-main">
      <section class="admin-course-reviews-filter-card">
        <div class="admin-course-reviews-filter-row">
          <label class="admin-course-reviews-field">
            <span>学员姓名</span>
            <el-input v-model="filterDraft.studentName" placeholder="请输入学员姓名" clearable @keyup.enter="applyFilters" />
          </label>
          <label class="admin-course-reviews-field">
            <span>学员学号</span>
            <el-input v-model="filterDraft.studentNo" placeholder="请输入学员学号" clearable @keyup.enter="applyFilters" />
          </label>
          <label class="admin-course-reviews-field">
            <span>所属班级</span>
            <el-select v-model="filterDraft.classNames" multiple collapse-tags placeholder="请选择所属班级" clearable>
              <el-option v-for="name in classOptions" :key="name" :label="name" :value="name" />
            </el-select>
          </label>
          <div class="admin-course-reviews-buttons">
            <el-button type="primary" class="admin-course-reviews-query" @click="applyFilters">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button class="admin-course-reviews-reset" @click="resetFilters">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </div>
        </div>
      </section>

      <section class="admin-course-reviews-table-card">
        <header class="admin-course-reviews-table-head">
          <div>
            <el-icon><Tickets /></el-icon>
            <strong>实训课批阅</strong>
          </div>
          <el-button @click="exportRows">导出数据</el-button>
        </header>

        <div class="admin-course-reviews-tabs">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            type="button"
            :class="{ active: activeTab === tab.key }"
            @click="setTab(tab.key)"
          >
            <span>{{ tab.label }}</span>
            <b :class="tab.tone">{{ tab.count }}</b>
          </button>
        </div>

        <div v-if="loading" class="admin-course-empty">阅卷列表加载中...</div>
        <div v-else-if="pagedRows.length === 0" class="admin-course-empty">
          <el-empty description="暂无阅卷记录" />
        </div>
        <div v-else class="admin-course-reviews-table-scroll">
          <table class="admin-course-reviews-table">
            <thead>
              <tr>
                <th>序号</th>
                <th>学员姓名</th>
                <th>学号</th>
                <th>所属班级</th>
                <th>实训任务</th>
                <th>是否提交</th>
                <th>提交次数</th>
                <th>最后一次提交时间</th>
                <th>是否批阅</th>
                <th>个人得分</th>
                <th>同组队员成绩</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, index) in pagedRows" :key="item.id">
                <td>{{ (page - 1) * pageSize + index + 1 }}</td>
                <td class="admin-course-reviews-name">{{ item.studentName }}</td>
                <td>{{ item.studentNo }}</td>
                <td>{{ item.className }}</td>
                <td>{{ item.taskName }}</td>
                <td>
                  <span class="admin-course-reviews-tag" :class="item.submitted ? 'submitted' : 'not-submitted'">
                    {{ item.submitted ? '已提交' : '未提交' }}
                  </span>
                </td>
                <td>{{ item.submitCount }}</td>
                <td>{{ item.submittedAt || '-' }}</td>
                <td>
                  <span v-if="item.submitted" class="admin-course-reviews-tag" :class="item.reviewed ? 'reviewed' : 'pending'">
                    {{ item.reviewed ? '已批阅' : '未批阅' }}
                  </span>
                  <span v-else>-</span>
                </td>
                <td>{{ item.score ?? '-' }}</td>
                <td>{{ item.teammateScores }}</td>
                <td>
                  <span v-if="!item.submitted" class="admin-course-reviews-none">-</span>
                  <el-button
                    v-else-if="item.reviewed"
                    class="admin-course-reviews-action view"
                    :disabled="!can('list')"
                    @click="openReview(item)"
                  >
                    <el-icon><View /></el-icon>
                    查看批阅
                  </el-button>
                  <el-button v-else class="admin-course-reviews-action edit" :disabled="!can('update')" @click="openReview(item)">
                    <el-icon><EditPen /></el-icon>
                    批阅
                  </el-button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <footer class="admin-course-reviews-pagination">
          <p>显示 {{ pageStart }} 到 {{ pageEnd }} 条，共 {{ total }} 条记录</p>
          <div class="admin-course-reviews-pager">
            <el-button :icon="DArrowLeft" :disabled="page === 1" @click="goToPage(1)" />
            <el-button :icon="ArrowLeft" :disabled="page === 1" @click="goToPage(Math.max(1, page - 1))" />
            <el-pagination
              v-model:current-page="page"
              :page-size="pageSize"
              :total="total"
              layout="pager"
              background
              @current-change="goToPage"
            />
            <el-button :icon="ArrowRight" :disabled="page === pageCount" @click="goToPage(Math.min(pageCount, page + 1))" />
            <el-button :icon="DArrowRight" :disabled="page === pageCount" @click="goToPage(pageCount)" />
            <span>每页</span>
            <el-select v-model="pageSize" class="admin-course-reviews-size">
              <el-option :label="10" :value="10" />
              <el-option :label="20" :value="20" />
            </el-select>
            <span>条</span>
          </div>
        </footer>
      </section>
      </div></div>

      <el-dialog v-model="reviewVisible" width="1100px" :title="reviewReadonly ? '查看批阅' : '批阅实训'" append-to-body>
        <div v-if="reviewTarget" class="training-review-dialog">
          <div class="review-student-meta">
            <span>学员姓名<strong>{{ reviewTarget.studentName }}</strong></span>
            <span>学号<strong>{{ reviewTarget.studentNo }}</strong></span>
            <span>所属班级<strong>{{ reviewTarget.className }}</strong></span>
            <span>提交时间<strong>{{ formatDateTime(selectedAttempt?.submittedAt) }}</strong></span>
          </div>
          <div class="review-attempt-list">
            <button v-for="attempt in attempts" :key="attempt.attemptId" :class="{ active: selectedAttempt?.attemptId === attempt.attemptId }" @click="selectAttempt(attempt)">
              <span>{{ formatDateTime(attempt.submittedAt) }}</span>
              <strong>
                {{ attempt.manualScore ?? attempt.systemScore ?? 0 }} 分
                <em v-if="attempt.attemptId === reviewTarget.id">（最终成绩）</em>
              </strong>
              <small>{{ attempt.reviewedAt ? '已批阅' : '待批阅' }}</small>
            </button>
          </div>
          <div v-if="attemptDetail" class="review-detail-grid">
            <section>
              <table class="review-step-table">
                <thead><tr><th>序号</th><th>步骤名称</th><th>正确结果</th><th>实际操作</th><th>得分</th><th>用时(秒)</th></tr></thead>
                <tbody><tr v-for="(step, index) in attemptDetail.steps || []" :key="step.stepId" @click="seekVideo(step.videoStartSecond)">
                  <td>{{ index + 1 }}</td><td class="step-link">{{ step.stepName }}</td><td>{{ step.standardOperation || '-' }}</td>
                  <td :class="{ error: Number(step.score || 0) < Number(step.maxScore || 0) }">{{ step.actualOperation || '-' }}</td>
                  <td>{{ step.score ?? 0 }}</td><td>{{ step.durationSeconds ?? 0 }}</td>
                </tr></tbody>
              </table>
              <div class="review-score-row"><span>系统核算个人得分：{{ selectedAttempt?.systemScore ?? 0 }} / {{ attemptMaxScore }}</span>
                <span v-if="reviewReadonly">人工修正总分：{{ manualScore }} / {{ attemptMaxScore }}</span>
                <label v-else>人工修正总分 <el-input-number v-model="manualScore" :min="0" :max="attemptMaxScore" :controls="false" /></label>
              </div>
            </section>
            <section class="review-video-panel">
              <video v-if="attemptDetail.recordingUrl" ref="reviewVideo" :src="resolvePublicUrl(attemptDetail.recordingUrl)" controls />
              <div v-else>本实训未开启操作视频录制，无法播放回放</div>
            </section>
          </div>
          <label class="review-comment">
            <span>实训评语</span>
            <p v-if="reviewReadonly" class="review-comment-readonly">{{ reviewComment || '暂无教师实训评语' }}</p>
            <el-input v-else v-model="reviewComment" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="请输入本次实训作业整体评语（选填，最多500字）" />
          </label>
        </div>
        <template #footer><el-button @click="reviewVisible = false">关闭</el-button><el-button v-if="!reviewReadonly" type="primary" :disabled="!can('update')" @click="saveReview">保存批阅结果</el-button></template>
      </el-dialog>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  ArrowLeft,
  ArrowRight,
  DArrowLeft,
  DArrowRight,
  EditPen,
  Refresh,
  Search,
  Tickets,
  View
} from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import { resolvePublicUrl } from '../../api/http';
import { fetchAdminTrainingArchiveDetail, type AdminTrainingArchiveDetail } from '../../api/admin-archive';
import {
  fetchAdminTraining,
  fetchAdminTrainingReviewAttempts,
  fetchAdminTrainingReviews,
  reviewAdminTrainingAttempt,
  type AdminTrainingReviewAttempt,
  type AdminTrainingReviewRow
} from '../../api/admin-training';
import { useAdminPermissions } from '../../features/admin/use-admin-permissions';

type ReviewTabKey = 'all' | 'pending' | 'reviewed' | 'notSubmitted';

interface TrainingReviewRow {
  id: number;
  studentId: number;
  topicId: number;
  studentName: string;
  studentNo: string;
  className: string;
  taskName: string;
  maxScore: number;
  submitted: boolean;
  submittedAt?: string;
  reviewed: boolean;
  score?: number;
  submitCount: number;
  teammateScores: string;
}

interface ReviewTopic { id: number; name: string; mode: string }

const route = useRoute();
const router = useRouter();
const { can } = useAdminPermissions('teaching:training');
const trainingId = computed(() => Number(route.params.id));
const trainingTitle = ref(String(route.query.title || '实训组课'));
const page = ref(1);
const pageSize = ref(10);
const activeTab = ref<ReviewTabKey>('all');
const loading = ref(false);
const filterDraft = reactive({
  studentName: '',
  studentNo: '',
  classNames: [] as string[]
});
const appliedFilters = reactive({
  studentName: '',
  studentNo: '',
  classNames: [] as string[]
});
const rows = ref<TrainingReviewRow[]>([]);
const topics = ref<ReviewTopic[]>([]);
const activeTopicId = ref(0);
const reviewVisible = ref(false);
const reviewTarget = ref<TrainingReviewRow>();
const attempts = ref<AdminTrainingReviewAttempt[]>([]);
const selectedAttempt = ref<AdminTrainingReviewAttempt>();
const attemptDetail = ref<AdminTrainingArchiveDetail>();
const manualScore = ref(0);
const reviewComment = ref('');
const reviewReadonly = ref(false);
const reviewVideo = ref<HTMLVideoElement>();
const classOptions = computed(() => [...new Set(rows.value.map((item) => item.className).filter((name) => name && name !== '-'))]);
const attemptMaxScore = computed(() => Number(selectedAttempt.value?.maxScore || reviewTarget.value?.maxScore || 100));

const matchedRows = computed(() =>
  rows.value.filter((item) => {
    const keywordMatched =
      (!appliedFilters.studentName || item.studentName.includes(appliedFilters.studentName)) &&
      (!appliedFilters.studentNo || item.studentNo.includes(appliedFilters.studentNo)) &&
      (!appliedFilters.classNames.length || appliedFilters.classNames.includes(item.className)) &&
      (!activeTopicId.value || item.topicId === activeTopicId.value);
    if (!keywordMatched) {
      return false;
    }
    if (activeTab.value === 'pending') {
      return item.submitted && !item.reviewed;
    }
    if (activeTab.value === 'reviewed') {
      return item.submitted && item.reviewed;
    }
    if (activeTab.value === 'notSubmitted') {
      return !item.submitted;
    }
    return true;
  })
);

const filterBaseRows = computed(() =>
  rows.value.filter((item) => {
    return (
      (!appliedFilters.studentName || item.studentName.includes(appliedFilters.studentName)) &&
      (!appliedFilters.studentNo || item.studentNo.includes(appliedFilters.studentNo)) &&
      (!appliedFilters.classNames.length || appliedFilters.classNames.includes(item.className)) &&
      (!activeTopicId.value || item.topicId === activeTopicId.value)
    );
  })
);

const tabs = computed(() => [
  { key: 'all' as const, label: '全部', count: filterBaseRows.value.length, tone: 'all' },
  {
    key: 'pending' as const,
    label: '待批阅',
    count: filterBaseRows.value.filter((item) => item.submitted && !item.reviewed).length,
    tone: 'pending'
  },
  {
    key: 'reviewed' as const,
    label: '已批阅',
    count: filterBaseRows.value.filter((item) => item.submitted && item.reviewed).length,
    tone: 'reviewed'
  },
  {
    key: 'notSubmitted' as const,
    label: '未提交',
    count: filterBaseRows.value.filter((item) => !item.submitted).length,
    tone: 'notSubmitted'
  }
]);

const total = computed(() => matchedRows.value.length);
const pageCount = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)));
const pagedRows = computed(() => {
  const start = (page.value - 1) * pageSize.value;
  return matchedRows.value.slice(start, start + pageSize.value);
});
const pageStart = computed(() => (total.value === 0 ? 0 : (page.value - 1) * pageSize.value + 1));
const pageEnd = computed(() => Math.min(page.value * pageSize.value, total.value));

function goBack() {
  router.push('/admin/training');
}

function setTab(tab: ReviewTabKey) {
  activeTab.value = tab;
  page.value = 1;
}

function applyFilters() {
  appliedFilters.studentName = filterDraft.studentName.trim();
  appliedFilters.studentNo = filterDraft.studentNo.trim();
  appliedFilters.classNames = [...filterDraft.classNames];
  page.value = 1;
}

function resetFilters() {
  filterDraft.studentName = '';
  filterDraft.studentNo = '';
  filterDraft.classNames = [];
  appliedFilters.studentName = '';
  appliedFilters.studentNo = '';
  appliedFilters.classNames = [];
  page.value = 1;
}

function goToPage(nextPage: number) {
  page.value = Math.min(Math.max(1, nextPage), pageCount.value);
}

function selectTopic(topicId: number) {
  activeTopicId.value = topicId;
  page.value = 1;
}

async function openReview(row: TrainingReviewRow) {
  reviewTarget.value = row;
  attempts.value = [];
  selectedAttempt.value = undefined;
  attemptDetail.value = undefined;
  manualScore.value = 0;
  reviewComment.value = '';
  reviewReadonly.value = row.reviewed;
  try {
    attempts.value = await fetchAdminTrainingReviewAttempts(trainingId.value, row.studentId, row.topicId);
    reviewVisible.value = true;
    const finalAttempt = attempts.value.find((attempt) => attempt.attemptId === row.id) || attempts.value[0];
    if (finalAttempt) await selectAttempt(finalAttempt);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '提交记录加载失败');
  }
}

async function selectAttempt(attempt: AdminTrainingReviewAttempt) {
  try {
    const detail = await fetchAdminTrainingArchiveDetail(attempt.attemptId);
    selectedAttempt.value = attempt;
    reviewReadonly.value = Boolean(attempt.reviewedAt);
    manualScore.value = Number(attempt.manualScore ?? attempt.systemScore ?? 0);
    reviewComment.value = attempt.reviewComment || '';
    attemptDetail.value = detail;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '提交详情加载失败');
  }
}

function seekVideo(second = 0) {
  if (!reviewVideo.value) return;
  reviewVideo.value.currentTime = second;
  void reviewVideo.value.play();
}

async function saveReview() {
  if (!selectedAttempt.value || manualScore.value < 0 || manualScore.value > attemptMaxScore.value) {
    ElMessage.warning('请输入合适的分数');
    return;
  }
  try {
    await reviewAdminTrainingAttempt(trainingId.value, selectedAttempt.value.attemptId, {
      manualScore: manualScore.value,
      comment: reviewComment.value.trim() || undefined
    });
    ElMessage.success('批阅结果已保存');
    reviewVisible.value = false;
    await loadReviews();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '批阅保存失败');
  }
}

function exportRows() {
  const lines = [['学员姓名', '学号', '所属班级', '实训任务', '提交次数', '提交时间', '批阅状态', '个人得分']];
  matchedRows.value.forEach((row) => lines.push([
    row.studentName, row.studentNo, row.className, row.taskName, String(row.submitCount), row.submittedAt || '',
    row.submitted ? (row.reviewed ? '已批阅' : '待批阅') : '未提交', String(row.score ?? '')
  ]));
  const csv = '\ufeff' + lines.map((line) => line.map((cell) => `"${cell.replace(/"/g, '""')}"`).join(',')).join('\n');
  const link = document.createElement('a');
  link.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }));
  link.download = `${trainingTitle.value}-阅卷数据.csv`;
  link.click();
  URL.revokeObjectURL(link.href);
}

async function loadTrainingTitle() {
  if (!trainingId.value) {
    return false;
  }
  try {
    const detail = await fetchAdminTraining(trainingId.value);
    trainingTitle.value = detail.trainingName || trainingTitle.value;
    if (!detail.openEndTime || Date.now() < new Date(detail.openEndTime).getTime()) {
      ElMessage.warning('实训结束后才可进入阅卷');
      await router.replace('/admin/training');
      return false;
    }
    return true;
  } catch {
    return true;
  }
}

function mapReviewRow(item: AdminTrainingReviewRow): TrainingReviewRow {
  return {
    id: item.attemptId || Number(`${item.studentId}${item.topicId}`),
    studentId: item.studentId,
    topicId: item.topicId,
    studentName: item.studentName || '-',
    studentNo: item.studentNo || '-',
    className: item.className || '-',
    taskName: item.topicName || '-',
    submitted: Boolean(item.attemptId),
    submittedAt: item.submittedAt ? formatDateTime(item.submittedAt) : undefined,
    reviewed: Boolean(item.reviewedAt),
    maxScore: Number(item.maxScore || 100),
    score: item.attemptId ? Number(item.manualScore ?? item.systemScore ?? 0) : undefined,
    submitCount: Number(item.submitCount || 0),
    teammateScores: item.trainingMode === 'TEAM' ? (item.teammateScores || '-') : '-'
  };
}

async function loadReviews() {
  if (!trainingId.value) return;
  loading.value = true;
  try {
    const data = await fetchAdminTrainingReviews(trainingId.value);
    rows.value = data.map(mapReviewRow);
    const unique = new Map<number, ReviewTopic>();
    data.forEach((item) => unique.set(item.topicId, {
      id: item.topicId,
      name: item.topicName || `实训任务${item.topicId}`,
      mode: item.trainingMode === 'TEAM' ? '多人实训' : '单人实训'
    }));
    topics.value = [...unique.values()];
    if (!activeTopicId.value && topics.value[0]) activeTopicId.value = topics.value[0].id;
  } catch (error) {
    rows.value = [];
    ElMessage.error(error instanceof Error ? error.message : '阅卷列表加载失败');
  } finally {
    loading.value = false;
  }
}

function formatDateTime(value?: string) {
  return value ? value.replace('T', ' ').slice(0, 16) : '-';
}

onMounted(async () => {
  if (await loadTrainingTitle()) {
    await loadReviews();
  }
});
</script>

<style scoped>
.admin-training-reviews-page {
  min-width: 0;
}

.training-review-workbench {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 16px;
}

.training-review-topics {
  display: grid;
  align-content: start;
  gap: 8px;
  border: 1px solid #e5ebf3;
  border-radius: 8px;
  padding: 16px;
  background: #fff;
}

.training-review-topics > strong { margin-bottom: 6px; color: #0f172a; }
.training-review-topics button {
  display: grid;
  gap: 5px;
  border: 1px solid transparent;
  border-radius: 6px;
  padding: 12px;
  background: #f8fafc;
  color: #475569;
  text-align: left;
  cursor: pointer;
}
.training-review-topics button.active { border-color: #93c5fd; background: #eff6ff; color: #2563eb; }
.training-review-topics small { color: #94a3b8; }
.training-review-main { min-width: 0; }
.admin-course-reviews-table { min-width: 1320px; }

.review-student-meta {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}
.review-student-meta span { display: grid; gap: 5px; border: 1px solid #e5e7eb; padding: 12px; color: #94a3b8; }
.review-student-meta strong { overflow: hidden; color: #334155; text-overflow: ellipsis; white-space: nowrap; }
.review-attempt-list { display: flex; gap: 8px; overflow-x: auto; margin-bottom: 16px; }
.review-attempt-list button { display: grid; flex: 0 0 190px; gap: 4px; border: 1px solid #e2e8f0; padding: 10px; background: #fff; text-align: left; cursor: pointer; }
.review-attempt-list button.active { border-color: #3b82f6; background: #eff6ff; }
.review-attempt-list small { color: #64748b; }
.review-attempt-list em { color: #2563eb; font-size: 11px; font-style: normal; }
.review-detail-grid { display: grid; grid-template-columns: minmax(0, 1.5fr) minmax(280px, 1fr); gap: 16px; }
.review-step-table { width: 100%; border-collapse: collapse; }
.review-step-table th, .review-step-table td { border: 1px solid #e5e7eb; padding: 9px; text-align: left; }
.review-step-table th { background: #f8fafc; color: #64748b; }
.review-step-table .step-link { color: #2563eb; cursor: pointer; }
.review-step-table .error { color: #dc2626; }
.review-score-row { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-top: 14px; }
.review-score-row label { display: flex; align-items: center; gap: 8px; }
.review-video-panel { display: grid; min-height: 320px; place-items: center; background: #0f172a; color: #cbd5e1; text-align: center; }
.review-video-panel video { width: 100%; max-height: 420px; }
.review-comment { display: grid; gap: 8px; margin-top: 16px; }
.review-comment-readonly { min-height: 88px; margin: 0; border: 1px solid #e5e7eb; padding: 14px 16px; background: #f8fafc; color: #475569; line-height: 24px; white-space: pre-wrap; }

.admin-training-reviews-page .admin-course-reviews-table-card {
  overflow: hidden;
}

@media (max-width: 980px) {
  .training-review-workbench { grid-template-columns: 1fr; }
  .training-review-topics { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .training-review-topics > strong { grid-column: 1 / -1; }
  .review-detail-grid { grid-template-columns: 1fr; }
  .review-student-meta { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .admin-training-reviews-page .admin-course-reviews-topbar {
    grid-template-columns: 1fr;
    align-items: flex-start;
    gap: 10px;
    padding: 12px 0;
  }

  .admin-training-reviews-page .admin-course-reviews-topbar h1 {
    order: -1;
    text-align: left;
  }

  .admin-training-reviews-page .admin-course-reviews-filter-card {
    padding-top: 20px;
  }

  .admin-training-reviews-page .admin-course-reviews-field {
    max-width: none;
  }

  .admin-training-reviews-page .admin-course-reviews-buttons {
    width: 100%;
  }
}
</style>
