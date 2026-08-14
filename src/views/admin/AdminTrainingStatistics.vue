<template>
  <AdminShell activeKey="admin-trainings">
    <section class="admin-training-statistics-page">
      <header class="admin-training-statistics-topbar">
        <el-button
          class="admin-training-statistics-back"
          :icon="ArrowLeft"
          title="返回实训组课列表"
          @click="goBack"
        />
        <el-breadcrumb class="admin-training-statistics-breadcrumb" separator="/">
          <el-breadcrumb-item>教学实训</el-breadcrumb-item>
          <el-breadcrumb-item>实训组课</el-breadcrumb-item>
          <el-breadcrumb-item>成绩统计</el-breadcrumb-item>
        </el-breadcrumb>
      </header>

      <main v-loading="loading" class="admin-training-statistics-shell">
        <section class="admin-training-statistics-summary">
          <article
            v-for="card in summaryCards"
            :key="card.label"
            class="admin-training-statistics-summary-card"
          >
            <span class="admin-training-statistics-summary-icon" :class="card.tone">
              <el-icon><component :is="card.icon" /></el-icon>
            </span>
            <div>
              <p>{{ card.label }}</p>
              <strong>{{ card.value }}</strong>
              <small>{{ card.desc }}</small>
            </div>
          </article>
        </section>

        <section class="admin-training-statistics-filter">
          <span class="admin-training-statistics-filter-label">按班级筛选</span>
          <div class="admin-training-statistics-chip-row">
            <button
              v-for="item in classChips"
              :key="item"
              type="button"
              :class="{ active: activeClass === item }"
              @click="activeClass = item"
            >
              <el-icon v-if="item === '全部班级'"><Check /></el-icon>
              <span>{{ item }}</span>
            </button>
          </div>
        </section>

        <section class="admin-training-statistics-grid top">
          <article class="admin-training-statistics-panel panel-chart">
            <header class="panel-head">
              <div>
                <el-icon><Histogram /></el-icon>
                <strong>各班级参与人数统计</strong>
              </div>
              <div class="panel-legend">
                <span><i class="tone-attend"></i>应参训</span>
                <span><i class="tone-complete"></i>实际参训</span>
              </div>
            </header>
            <div class="class-bar-chart">
              <div class="class-bar-axis">
                <span v-for="tick in classChartTicks" :key="tick">{{ tick }}</span>
              </div>
              <div class="class-bar-plot">
                <div v-for="item in classParticipationData" :key="item.name" class="class-bar-item">
                  <div class="class-bar-group">
                    <i class="tone-attend" :style="{ height: `${item.joinedHeight}%` }">
                      <b>{{ item.joined }}</b>
                    </i>
                    <i class="tone-complete" :style="{ height: `${item.completedHeight}%` }">
                      <b>{{ item.completed }}</b>
                    </i>
                  </div>
                  <span>{{ item.name }}</span>
                </div>
              </div>
            </div>
          </article>

          <article class="admin-training-statistics-panel panel-donut">
            <header class="panel-head">
              <div>
                <el-icon><PieChart /></el-icon>
                <strong>成绩等级占比</strong>
              </div>
            </header>
            <div class="score-donut-layout">
              <div class="score-donut" :style="{ background: donutGradient }">
                <div class="score-donut-core">
                  <strong>{{ participantCount }}</strong>
                  <span>总人数</span>
                </div>
              </div>
              <div class="score-donut-legend">
                <article v-for="item in scoreDistribution" :key="item.name">
                  <i :style="{ background: item.color }"></i>
                  <span>{{ item.name }}</span>
                  <b>{{ item.count }}人（{{ item.percent }}%）</b>
                </article>
              </div>
            </div>
          </article>
        </section>

        <section class="admin-training-statistics-grid middle">
          <article class="admin-training-statistics-panel panel-stack">
            <header class="panel-head">
              <div>
                <el-icon><TrendCharts /></el-icon>
                <strong>个人成绩分布</strong>
              </div>
            </header>
            <div v-if="scoreDistribution.length" class="score-range-chart">
              <div v-for="item in scoreDistribution" :key="item.name" class="score-range-column">
                <b>{{ item.count }} 人</b>
                <i :style="{ height: `${scoreRangeHeight(item.count)}%`, background: item.color }"></i>
                <span>{{ item.name }}</span>
              </div>
            </div>
            <el-empty v-else description="暂无成绩分布数据" />
          </article>

          <article class="admin-training-statistics-panel panel-compare">
            <header class="panel-head">
              <div>
                <el-icon><Histogram /></el-icon>
                <strong>班级平均分对比分析</strong>
              </div>
            </header>
            <div class="compare-chart">
              <div v-for="item in classAverageCompare" :key="item.name" class="compare-row">
                <span>{{ item.name }}</span>
                <div class="compare-bar-track">
                  <i :style="{ width: `${item.percent}%`, background: item.color }"></i>
                </div>
                <b>{{ item.score }}</b>
              </div>
            </div>
          </article>
        </section>

        <section class="admin-training-statistics-grid bottom">
          <article class="admin-training-statistics-panel panel-ranking">
            <header class="panel-head">
              <div>
                <el-icon><Medal /></el-icon>
                <strong>成绩排行榜 Top10</strong>
              </div>
            </header>
            <table class="ranking-table">
              <thead>
                <tr>
                  <th>排名</th>
                  <th>学号</th>
                  <th>姓名</th>
                  <th>班级</th>
                  <th>分数</th>
                  <th>等级</th>
                  <th>用时</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in rankingRows" :key="row.rank">
                  <td>
                    <span class="ranking-badge" :class="rankTone(row.rank)">{{ row.rank }}</span>
                  </td>
                  <td>{{ row.studentNo }}</td>
                  <td><strong>{{ row.name }}</strong></td>
                  <td>{{ row.className }}</td>
                  <td><span class="ranking-score" :class="gradeTone(row.score)">{{ row.score }}</span></td>
                  <td><span class="ranking-grade" :class="gradeTone(row.score)">{{ row.grade }}</span></td>
                  <td>{{ row.duration }}</td>
                </tr>
              </tbody>
            </table>
          </article>

          <article class="admin-training-statistics-panel panel-progress">
            <header class="panel-head">
              <div>
                <el-icon class="weakness-title-icon"><Warning /></el-icon>
                <strong>易错题目 TOP10</strong>
              </div>
            </header>
            <div class="progress-list">
              <article v-for="item in progressRows" :key="item.title">
                <div class="progress-meta">
                  <span class="progress-index" :class="progressTone(item.percent)">{{ item.index }}</span>
                  <div>
                    <strong>{{ item.title }}</strong>
                  <p>{{ item.subtitle }}，错误人数 {{ item.errorCount }}，正确率 {{ item.correctRate }}%</p>
                  </div>
                  <b :class="progressTone(item.percent)">错误率 {{ item.percent }}%</b>
                </div>
                <div class="progress-track">
                  <i :class="progressTone(item.percent)" :style="{ width: `${item.percent}%` }"></i>
                </div>
              </article>
              <el-empty v-if="progressRows.length === 0" description="暂无步骤错误数据" />
            </div>
          </article>
        </section>
      </main>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft, Calendar, Check, Document, Histogram, Medal, PieChart, TrendCharts, User, UserFilled, Warning } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  fetchAdminTraining,
  fetchAdminTrainingOfflineScores,
  fetchAdminTrainingReviews,
  fetchAdminTrainingStatistics,
  fetchAdminTrainingWeakTopics,
  type AdminTraining,
  type AdminTrainingReviewRow,
  type AdminTrainingOfflineScore,
  type AdminTrainingWeakTopic,
  type AdminTrainingStatistics as TrainingStatistics
} from '../../api/admin-training';
import { fetchAdminScoreGradeRules, type AdminScoreGradeRule } from '../../api/admin-settings';

interface SummaryCard {
  label: string;
  value: string;
  desc: string;
  icon: typeof Document;
  tone: string;
}

interface ScoreBucket {
  name: string;
  count: number;
  percent: number;
  color: string;
}

interface ChartClassItem {
  name: string;
  joined: number;
  completed: number;
  joinedHeight: number;
  completedHeight: number;
}

interface CompareItem {
  name: string;
  score: string;
  percent: number;
  color: string;
}

interface RankingRow {
  rank: number;
  studentNo: string;
  name: string;
  className: string;
  score: number;
  grade: string;
  duration: string;
}

interface ProgressRow {
  index: number;
  title: string;
  subtitle: string;
  percent: number;
  errorCount: number;
  correctRate: number;
}

const route = useRoute();
const router = useRouter();
const trainingId = computed(() => Number(route.params.id));
const loading = ref(false);
const trainingTitle = ref(String(route.query.title || '成绩统计'));
const classText = ref(String(route.query.target || ''));
const timeText = ref(String(route.query.time || ''));
const activeClass = ref('全部班级');
const statistics = ref<TrainingStatistics>({});
const trainingDetail = ref<AdminTraining>();
const reviewRows = ref<AdminTrainingReviewRow[]>([]);
const offlineScores = ref<AdminTrainingOfflineScore[]>([]);
const weakTopics = ref<AdminTrainingWeakTopic[]>([]);
const gradeRules = ref<AdminScoreGradeRule[]>([]);

const participantCount = computed(() => activeClass.value === '全部班级'
  ? numberValue(statistics.value.participantCount)
  : new Set(filteredReviewRows.value.map((item) => item.studentId)).size);
const completedCount = computed(() => submittedStudentRows.value.length);
const averageScore = computed(() => {
  const scores = submittedScores.value;
  return scores.length ? scores.reduce((sum, score) => sum + score, 0) / scores.length : 0;
});
const notCompletedCount = computed(() => Math.max(participantCount.value - completedCount.value, 0));
const classLabels = computed(() => {
  const parsed = [...new Set(reviewRows.value.map((item) => item.className).filter(Boolean) as string[])];
  if (parsed.length > 0) {
    return parsed;
  }
  return [];
});
const classChips = computed(() => ['全部班级', ...classLabels.value]);
const filteredReviewRows = computed(() => activeClass.value === '全部班级'
  ? reviewRows.value
  : reviewRows.value.filter((item) => item.className === activeClass.value));
const submittedStudentRows = computed(() => {
  const rows = new Map<number, AdminTrainingReviewRow & { totalScore: number; totalDuration: number }>();
  filteredReviewRows.value.filter((item) => item.attemptId).forEach((item) => {
    const score = Number(item.manualScore ?? item.systemScore ?? 0);
    const current = rows.get(item.studentId);
    if (!current) {
      rows.set(item.studentId, { ...item, totalScore: score, totalDuration: Number(item.durationSeconds || 0) });
      return;
    }
    current.totalScore += score;
    current.totalDuration += Number(item.durationSeconds || 0);
  });
  offlineScores.value
    .filter((item) => activeClass.value === '全部班级' || item.className === activeClass.value)
    .forEach((item) => {
      if (rows.has(item.studentId)) return;
      rows.set(item.studentId, {
        studentId: item.studentId,
        studentName: item.studentName,
        studentNo: item.studentNo,
        className: item.className,
        topicId: 0,
        totalScore: Number(item.totalScore || 0),
        totalDuration: 0
      });
    });
  return [...rows.values()];
});
const courseMaxScore = computed(() => [...new Map(reviewRows.value.map((item) => [item.topicId, Number(item.maxScore || 0)])).values()]
  .reduce((sum, score) => sum + score, 0) || 100);
const submittedScores = computed(() => submittedStudentRows.value
  .map((item) => Number(item.totalScore))
  .filter((score) => Number.isFinite(score)));

const summaryCards = computed<SummaryCard[]>(() => [
  {
    label: '实训课程名',
    value: trainingTitle.value || '-',
    desc: trainingDetail.value?.trainingType === 'EXAM' ? '实训考试' : '实训练习',
    icon: Document,
    tone: 'calendar'
  },
  {
    label: '组卷方式',
    value: paperModeText(trainingDetail.value?.paperMode),
    desc: trainingDetail.value?.paperName || '未关联理论试卷',
    icon: Document,
    tone: 'purple'
  },
  {
    label: '参与班级',
    value: classText.value || '-',
    desc: `${classLabels.value.length} 个班级`,
    icon: User,
    tone: 'green'
  },
  {
    label: '实训题目总数',
    value: `${trainingDetail.value?.topicCount || 0} 题`,
    desc: trainingDetail.value?.trainingMode === 'TEAM' ? '协同实训' : '单人实训',
    icon: Document,
    tone: 'blue'
  },
  {
    label: '实训课程起止时间',
    value: formatTimeRange(timeText.value) || '-',
    desc: '考试时段',
    icon: Calendar,
    tone: 'green'
  },
  {
    label: '应参加人数',
    value: `${participantCount.value} 人`,
    desc: '计划参训',
    icon: User,
    tone: 'orange'
  },
  {
    label: '实际参训人数',
    value: `${completedCount.value} 人`,
    desc: '实际参训',
    icon: UserFilled,
    tone: 'blue'
  },
  {
    label: '未参训人数',
    value: `${notCompletedCount.value} 人`,
    desc: '缺考人数',
    icon: User,
    tone: 'red'
  },
  {
    label: '平均分',
    value: `${averageScore.value.toFixed(1)} /${courseMaxScore.value}`,
    desc: '成绩均值',
    icon: Histogram,
    tone: 'pink'
  }
]);

const scoreDistribution = computed<ScoreBucket[]>(() => {
  const colors = ['#ef4444', '#f59e0b', '#3b82f6', '#8b5cf6', '#14b8a6', '#64748b'];
  const base = gradeRules.value.map((rule, index) => ({
    name: `${rule.gradeName} (${formatScore(rule.minScore)}%-${formatScore(rule.maxScore)}%)`,
    color: colors[index % colors.length],
    match: (score: number) => {
      const percent = courseMaxScore.value ? score * 100 / courseMaxScore.value : 0;
      return percent >= rule.minScore && percent <= rule.maxScore;
    }
  }));
  const total = submittedScores.value.length;
  return base.map((item) => {
    const count = submittedScores.value.filter(item.match).length;
    return {
    name: item.name,
    count,
    percent: total ? Number(((count / total) * 100).toFixed(1)) : 0,
    color: item.color
    };
  });
});

const donutGradient = computed(() => {
  const values = scoreDistribution.value;
  if (!values.length || !submittedScores.value.length) return '#e5e7eb';
  const total = values.reduce((sum, item) => sum + item.percent, 0) || 100;
  let start = 0;
  const segments = values.map((item) => {
    const end = start + (item.percent / total) * 100;
    const segment = `${item.color} ${start}% ${end}%`;
    start = end;
    return segment;
  });
  return `conic-gradient(${segments.join(', ')})`;
});

const classParticipationData = computed<ChartClassItem[]>(() => {
  const labels = activeClass.value === '全部班级' ? classLabels.value : [activeClass.value];
  const joinedCounts = labels.map((label) => new Set(filteredReviewRows.value.filter((item) => item.className === label).map((item) => item.studentId)).size);
  const completedCounts = labels.map((label) => submittedStudentRows.value.filter((item) => item.className === label).length);
  return labels.map((label, index) => {
    const joined = joinedCounts[index] ?? 0;
    const completed = Math.min(completedCounts[index] ?? 0, joined);
    return {
      name: label,
      joined,
      completed,
      joinedHeight: (joined / classChartMax.value) * 100,
      completedHeight: (completed / classChartMax.value) * 100
    };
  });
});

const classChartMax = computed(() => {
  const classCount = Math.max(classLabels.value.length, 1);
  const largestAverage = Math.ceil(participantCount.value / classCount);
  return Math.max(50, Math.ceil(largestAverage / 10) * 10);
});

const classChartTicks = computed(() =>
  Array.from({ length: 6 }, (_, index) => Math.round(classChartMax.value - (classChartMax.value / 5) * index))
);

const classAverageCompare = computed<CompareItem[]>(() =>
  (activeClass.value === '全部班级' ? classLabels.value : [activeClass.value]).map((label, index) => {
    const colors = ['#6d5efc', '#3b82f6', '#37c793', '#f59e0b', '#ef4444'];
    const values = submittedStudentRows.value.filter((item) => item.className === label).map((item) => Number(item.totalScore));
    const score = values.length ? values.reduce((sum, value) => sum + value, 0) / values.length : 0;
    return {
      name: label,
      score: score.toFixed(1),
      percent: Math.max(0, Math.min(100, score / courseMaxScore.value * 100)),
      color: colors[index % colors.length]
    };
  })
);

const rankingRows = computed<RankingRow[]>(() => [...submittedStudentRows.value].sort((a, b) => {
  const scoreDiff = Number(b.totalScore) - Number(a.totalScore);
  return scoreDiff || Number(a.totalDuration ?? Number.MAX_SAFE_INTEGER) - Number(b.totalDuration ?? Number.MAX_SAFE_INTEGER);
}).slice(0, 10).map((item, index) => {
  const score = Number(item.totalScore);
  return { rank: index + 1, studentNo: item.studentNo || '-', name: item.studentName || '-', className: item.className || '-', score, grade: gradeName(score), duration: formatDuration(item.totalDuration) };
}));

const progressRows = computed<ProgressRow[]>(() => weakTopics.value.map((item, index) => ({
  index: index + 1,
  title: item.topicName || '未命名实训题',
  subtitle: `共 ${numberValue(item.submittedStudentCount)} 人提交`,
  percent: Number((100 - numberValue(item.correctRate)).toFixed(1)),
  errorCount: numberValue(item.errorStudentCount),
  correctRate: numberValue(item.correctRate)
})));

function numberValue(value: number | string | undefined | null) {
  const parsed = Number(value ?? 0);
  return Number.isFinite(parsed) ? parsed : 0;
}

function formatScore(value: number) {
  return Number.isInteger(value) ? String(value) : value.toFixed(1);
}

function paperModeText(value?: string) {
  if (value === 'MANUAL') return '手动组卷';
  if (value === 'AUTO') return '自动组卷';
  return '不组卷';
}

function scoreRangeHeight(count: number) {
  const maximum = Math.max(...scoreDistribution.value.map((item) => item.count), 1);
  return Math.max(count > 0 ? 8 : 0, count * 100 / maximum);
}

function gradeName(score: number) {
  const percent = courseMaxScore.value ? score * 100 / courseMaxScore.value : 0;
  return gradeRules.value.find((rule) => percent >= rule.minScore && percent <= rule.maxScore)?.gradeName || '-';
}

function goBack() {
  router.push('/admin/training');
}

function formatDateTime(value?: string) {
  if (!value) {
    return '';
  }
  return value.replace('T', ' ').slice(0, 16);
}

function formatTimeRange(value: string) {
  if (!value) {
    return '';
  }
  return value.replace(/\s*至\s*/, ' 至 ');
}

function formatDuration(seconds?: number) {
  if (seconds === undefined || seconds === null) return '-';
  const total = Math.max(0, Number(seconds));
  const minutes = Math.floor(total / 60);
  const remain = Math.floor(total % 60);
  return `${minutes}:${String(remain).padStart(2, '0')}`;
}

function gradeTone(score: number) {
  const name = gradeName(score);
  if (name.includes('优秀')) return 'excellent';
  if (name.includes('良好')) return 'good';
  if (name.includes('不及格')) return 'bad';
  if (name.includes('及格')) return 'pass';
  if (name !== '-') return 'normal';
  return 'bad';
}

function rankTone(rank: number) {
  if (rank === 1) return 'gold';
  if (rank === 2) return 'silver';
  if (rank === 3) return 'bronze';
  return 'normal';
}

function progressTone(percent: number) {
  if (percent >= 60) return 'danger';
  if (percent >= 45) return 'warning';
  return 'muted';
}

async function loadStatistics() {
  if (!trainingId.value) {
    return;
  }

  loading.value = true;
  try {
    const detail = await fetchAdminTraining(trainingId.value);
    if (!detail.openEndTime || Date.now() < new Date(detail.openEndTime).getTime()) {
      ElMessage.warning('实训结束后才可查看成绩统计');
      await router.replace('/admin/training');
      return;
    }
    const [result, rules, reviews, offline, topics] = await Promise.all([
      fetchAdminTrainingStatistics(trainingId.value),
      fetchAdminScoreGradeRules(),
      fetchAdminTrainingReviews(trainingId.value),
      fetchAdminTrainingOfflineScores(trainingId.value),
      fetchAdminTrainingWeakTopics(trainingId.value, activeClass.value === '全部班级' ? undefined : activeClass.value)
    ]);
    trainingDetail.value = detail;
    trainingTitle.value = detail.trainingName || trainingTitle.value;
    classText.value = detail.classNames || classText.value;
    timeText.value = `${formatDateTime(detail.openStartTime)} 至 ${formatDateTime(detail.openEndTime)}`.trim();
    statistics.value = result;
    gradeRules.value = [...rules].sort((left, right) => right.maxScore - left.maxScore || right.minScore - left.minScore);
    reviewRows.value = reviews;
    offlineScores.value = offline;
    weakTopics.value = topics;
  } catch (error) {
    statistics.value = {};
    ElMessage.error(error instanceof Error ? error.message : '成绩统计加载失败');
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadStatistics();
});

let refreshTimer: ReturnType<typeof setInterval> | undefined;
onMounted(() => {
  refreshTimer = setInterval(() => void loadStatistics(), 60_000);
});

onBeforeUnmount(() => {
  if (refreshTimer) clearInterval(refreshTimer);
});

watch(activeClass, async (value) => {
  if (!trainingId.value) return;
  try {
    weakTopics.value = await fetchAdminTrainingWeakTopics(trainingId.value, value === '全部班级' ? undefined : value);
  } catch (error) {
    weakTopics.value = [];
    ElMessage.error(error instanceof Error ? error.message : '易错题目加载失败');
  }
});
</script>

<style scoped>
.admin-training-statistics-page {
  min-height: 100vh;
  padding: 0 10px 20px;
  background: #f5f7fb;
}

.admin-training-statistics-topbar {
  display: flex;
  align-items: center;
  gap: 18px;
  min-height: 68px;
}

.admin-training-statistics-back.el-button {
  width: 44px;
  height: 44px;
  border: 1px solid #dce5f1;
  border-radius: 9px;
  background: #ffffff;
  color: #53657f;
  font-size: 18px;
}

.admin-training-statistics-breadcrumb {
  color: #94a3b8;
  font-size: 14px;
  font-weight: 600;
}

.admin-training-statistics-breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #2563eb;
  font-weight: 900;
}

.admin-training-statistics-shell {
  display: grid;
  gap: 0;
  padding: 0;
  border: 2px dashed #6d5efc;
  background: #ffffff;
}

.admin-training-statistics-summary,
.admin-training-statistics-filter,
.admin-training-statistics-grid {
  border: 0;
  border-bottom: 2px dashed #6d5efc;
  border-radius: 0;
  background: #ffffff;
}

.admin-training-statistics-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 16px;
  padding: 14px 16px;
}

.admin-training-statistics-summary-card {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  min-height: 72px;
  padding: 12px 14px;
  border: 1px solid #edf2f8;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 1px 0 rgba(15, 23, 42, 0.02);
}

.admin-training-statistics-summary-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 9px;
  font-size: 18px;
}

.admin-training-statistics-summary-icon.calendar {
  background: #eff6ff;
  color: #2563eb;
}

.admin-training-statistics-summary-icon.purple {
  background: #f5f3ff;
  color: #8b5cf6;
}

.admin-training-statistics-summary-icon.green {
  background: #ecfdf5;
  color: #10b981;
}

.admin-training-statistics-summary-icon.orange {
  background: #fff7ed;
  color: #f97316;
}

.admin-training-statistics-summary-icon.blue {
  background: #eef6ff;
  color: #3b82f6;
}

.admin-training-statistics-summary-icon.red {
  background: #fef2f2;
  color: #ef4444;
}

.admin-training-statistics-summary-icon.pink {
  background: #fdf2f8;
  color: #ec4899;
}

.admin-training-statistics-summary-card p {
  margin: 0;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
}

.admin-training-statistics-summary-card strong {
  display: block;
  margin-top: 5px;
  color: #152238;
  font-size: 15px;
  line-height: 1.1;
  font-weight: 900;
}

.admin-training-statistics-summary-card small {
  display: block;
  margin-top: 3px;
  color: #8aa0bd;
  font-size: 11px;
}

.admin-training-statistics-filter {
  display: flex;
  align-items: center;
  min-height: 42px;
  gap: 12px;
  padding: 6px 16px;
}

.admin-training-statistics-filter-label {
  flex: 0 0 auto;
  color: #64748b;
  font-size: 12px;
  font-weight: 800;
}

.admin-training-statistics-chip-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.admin-training-statistics-chip-row button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 28px;
  border: 1px solid #dbe3ee;
  border-radius: 6px;
  padding: 0 14px;
  background: #ffffff;
  color: #64748b;
  cursor: pointer;
  font: inherit;
  font-size: 12px;
  font-weight: 800;
}

.admin-training-statistics-chip-row button.active {
  border-color: #3b82f6;
  background: #3b82f6;
  color: #ffffff;
}

.admin-training-statistics-chip-row button .el-icon {
  font-size: 12px;
}

.admin-training-statistics-grid {
  display: grid;
  gap: 0;
}

.admin-training-statistics-grid.top,
.admin-training-statistics-grid.middle,
.admin-training-statistics-grid.bottom {
  grid-template-columns: minmax(0, 2.05fr) minmax(0, 0.95fr);
}

.admin-training-statistics-grid.bottom {
  grid-template-columns: minmax(0, 2.05fr) minmax(0, 0.95fr);
  border-bottom: 0;
}

.admin-training-statistics-panel {
  min-height: 286px;
  border: 0;
  border-right: 1px solid #edf2f8;
  border-radius: 0;
  background: #ffffff;
}

.admin-training-statistics-panel:last-child {
  border-right: 0;
}

.admin-training-statistics-grid.bottom .admin-training-statistics-panel {
  min-height: 412px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px 8px;
}

.panel-head > div {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #1e293b;
  font-size: 13px;
  font-weight: 900;
}

.panel-head .el-icon {
  font-size: 16px;
  color: #3b82f6;
}

.panel-legend {
  display: inline-flex;
  align-items: center;
  gap: 14px;
  color: #64748b;
  font-size: 11px;
  font-weight: 700;
}

.panel-legend span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.panel-legend i {
  width: 8px;
  height: 8px;
  border-radius: 2px;
}

.tone-attend {
  background: #6d5efc;
}

.tone-complete {
  background: #37c793;
}

.tone-excellent {
  background: #ef4444;
}

.tone-good {
  background: #f59e0b;
}

.tone-normal {
  background: #3b82f6;
}

.tone-pass {
  background: #f97316;
}

.tone-bad {
  background: #8b5cf6;
}

.soft {
  opacity: 0.85;
}

.class-bar-chart,
.stack-chart {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  gap: 6px;
  padding: 0 28px 16px 12px;
}

.class-bar-axis,
.stack-chart-axis {
  display: grid;
  align-content: end;
  padding-bottom: 30px;
  color: #94a3b8;
  font-size: 11px;
  text-align: right;
}

.class-bar-axis span,
.stack-chart-axis span {
  height: 35px;
}

.class-bar-plot,
.stack-chart-plot {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  align-items: end;
  gap: 14px;
  min-height: 220px;
}

.class-bar-plot {
  position: relative;
  padding: 0 8px;
}

.class-bar-plot::before {
  position: absolute;
  top: 0;
  right: 8px;
  bottom: 30px;
  left: 8px;
  border-bottom: 1px solid #cbd5e1;
  background-image: repeating-linear-gradient(
    to bottom,
    #edf1f6 0,
    #edf1f6 1px,
    transparent 1px,
    transparent 20%
  );
  content: '';
  pointer-events: none;
}

.class-bar-item,
.stack-column {
  position: relative;
  z-index: 1;
  display: grid;
  align-content: end;
  gap: 9px;
  min-height: 220px;
}

.class-bar-group {
  display: flex;
  align-items: end;
  justify-content: center;
  gap: 4px;
  height: 188px;
}

.class-bar-group i {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: min(34px, 42%);
  min-height: 3px;
  border-radius: 2px 2px 0 0;
}

.class-bar-group i b {
  color: #ffffff;
  font-size: 11px;
  font-weight: 900;
}

.class-bar-item span,
.stack-column span {
  color: #475569;
  font-size: 11px;
  font-weight: 700;
  text-align: center;
}

.stack-bars {
  display: flex;
  flex-direction: column-reverse;
  justify-content: flex-start;
  height: 188px;
  border-radius: 4px 4px 0 0;
  overflow: hidden;
}

.score-range-chart {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(84px, 1fr));
  align-items: end;
  gap: 18px;
  min-height: 236px;
  padding: 20px 28px 22px;
  border-bottom: 1px solid #cbd5e1;
  background-image: repeating-linear-gradient(to bottom, #edf1f6 0, #edf1f6 1px, transparent 1px, transparent 20%);
}

.score-range-column {
  display: grid;
  grid-template-rows: 20px 170px auto;
  align-items: end;
  gap: 8px;
  min-width: 0;
  text-align: center;
}

.score-range-column b {
  color: #475569;
  font-size: 12px;
}

.score-range-column i {
  width: min(56px, 75%);
  min-height: 0;
  margin: 0 auto;
  border-radius: 3px 3px 0 0;
}

.score-range-column span {
  color: #64748b;
  font-size: 11px;
  line-height: 1.4;
  overflow-wrap: anywhere;
}

.stack-bars i {
  display: block;
  min-height: 16px;
}

.score-donut-layout {
  display: grid;
  grid-template-columns: 186px minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  padding: 20px 20px 18px;
}

.score-donut {
  position: relative;
  width: 144px;
  height: 144px;
  margin: 0 auto;
  border-radius: 50%;
}

.score-donut::before {
  content: '';
  position: absolute;
  inset: 22px;
  border-radius: 50%;
  background: #ffffff;
}

.score-donut-core {
  position: absolute;
  inset: 22px;
  display: grid;
  place-items: center;
  text-align: center;
}

.score-donut-core strong {
  color: #17233d;
  font-size: 24px;
  line-height: 1;
  font-weight: 900;
}

.score-donut-core span {
  color: #94a3b8;
  font-size: 11px;
  font-weight: 700;
}

.score-donut-legend {
  display: grid;
  gap: 8px;
}

.score-donut-legend article {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #475569;
  font-size: 11px;
}

.score-donut-legend i {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.score-donut-legend span {
  flex: 0 0 auto;
}

.score-donut-legend b {
  margin-left: auto;
  color: #94a3b8;
  font-weight: 700;
}

.compare-chart {
  display: grid;
  gap: 14px;
  padding: 30px 28px 18px 20px;
}

.compare-row {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr) 40px;
  align-items: center;
  gap: 10px;
}

.compare-row span {
  color: #64748b;
  font-size: 11px;
  font-weight: 700;
  white-space: nowrap;
}

.compare-bar-track {
  height: 16px;
  border-radius: 2px;
  background: #eef2f7;
  overflow: hidden;
}

.compare-bar-track i {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: #6d5efc;
}

.compare-row b {
  color: #ef4444;
  font-size: 11px;
  font-weight: 900;
  text-align: right;
}

.ranking-table {
  width: 100%;
  border-collapse: collapse;
}

.ranking-table th,
.ranking-table td {
  height: 34px;
  padding: 0 18px;
  border-bottom: 1px solid #edf2f8;
  color: #475569;
  font-size: 11px;
  text-align: left;
  white-space: nowrap;
}

.ranking-table th {
  color: #94a3b8;
  font-weight: 900;
}

.ranking-table td strong {
  color: #17233d;
}

.ranking-badge,
.ranking-score,
.ranking-grade {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 7px;
  border-radius: 999px;
  font-weight: 900;
}

.ranking-badge.gold {
  background: #fef3c7;
  color: #d97706;
}

.ranking-badge.silver {
  background: #e5eefc;
  color: #2563eb;
}

.ranking-badge.bronze {
  background: #fdebd4;
  color: #ea580c;
}

.ranking-badge.normal {
  background: #f5f7fb;
  color: #94a3b8;
}

.ranking-score.excellent,
.ranking-grade.excellent {
  background: #dcfce7;
  color: #059669;
}

.ranking-score.good,
.ranking-grade.good {
  background: #eff6ff;
  color: #2563eb;
}

.ranking-score.normal,
.ranking-grade.normal {
  background: #fef3c7;
  color: #d97706;
}

.ranking-score.pass,
.ranking-grade.pass {
  background: #ffedd5;
  color: #ea580c;
}

.ranking-score.bad,
.ranking-grade.bad {
  background: #fee2e2;
  color: #ef4444;
}

.progress-list {
  display: grid;
  gap: 22px;
  padding: 22px 20px 26px;
}

.progress-list article {
  display: grid;
  gap: 10px;
}

.progress-meta {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
}

.progress-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 9px;
  font-size: 16px;
  font-weight: 900;
}

.progress-index.danger {
  background: #fff1f2;
  color: #ef4444;
}

.progress-index.warning {
  background: #fff7ed;
  color: #f59e0b;
}

.progress-index.muted {
  background: #f1f3f6;
  color: #6b7280;
}

.progress-meta strong {
  color: #17233d;
  font-size: 14px;
  font-weight: 900;
}

.progress-meta p {
  margin: 6px 0 0;
  color: #94a3b8;
  font-size: 12px;
}

.progress-meta b {
  justify-self: end;
  padding: 5px 9px;
  border-radius: 7px;
  font-size: 12px;
  font-weight: 900;
  white-space: nowrap;
}

.progress-track {
  height: 9px;
  margin-left: 54px;
  border-radius: 999px;
  background: #f0f2f5;
  overflow: hidden;
}

.progress-track i {
  display: block;
  height: 100%;
  border-radius: inherit;
}

.progress-meta b.danger {
  background: #fff1f2;
  color: #ef4444;
}

.progress-meta b.warning {
  background: #fff7ed;
  color: #f59e0b;
}

.progress-meta b.muted {
  background: #f1f3f6;
  color: #6b7280;
}

.progress-track i.danger {
  background: #ef4444;
}

.progress-track i.warning {
  background: #f59e0b;
}

.progress-track i.muted {
  background: #6b7280;
}

.panel-progress .weakness-title-icon {
  color: #ef4444;
}

@media (max-width: 1280px) {
  .admin-training-statistics-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .admin-training-statistics-grid.top,
  .admin-training-statistics-grid.middle,
  .admin-training-statistics-grid.bottom {
    grid-template-columns: 1fr;
  }

  .class-bar-plot,
  .stack-chart-plot {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .score-donut-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .admin-training-statistics-page {
    padding: 0 12px 20px;
  }

  .admin-training-statistics-summary {
    grid-template-columns: 1fr;
  }

  .admin-training-statistics-filter {
    align-items: flex-start;
    flex-direction: column;
  }

  .class-bar-plot,
  .stack-chart-plot {
    grid-template-columns: 1fr;
  }

  .panel-legend {
    flex-wrap: wrap;
    gap: 8px 12px;
  }

  .compare-row {
    grid-template-columns: 1fr;
  }

  .progress-meta {
    grid-template-columns: 42px minmax(0, 1fr);
  }

  .progress-meta b {
    grid-column: 2;
    justify-self: start;
  }

  .ranking-table {
    min-width: 760px;
  }
}
</style>
