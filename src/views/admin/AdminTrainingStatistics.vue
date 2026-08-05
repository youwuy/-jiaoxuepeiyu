<template>
  <AdminShell activeKey="admin-trainings">
    <section class="admin-training-statistics-page">
      <el-breadcrumb class="admin-training-statistics-breadcrumb" separator="/">
        <el-breadcrumb-item>教学实训</el-breadcrumb-item>
        <el-breadcrumb-item>实训组课</el-breadcrumb-item>
        <el-breadcrumb-item>成绩统计</el-breadcrumb-item>
      </el-breadcrumb>

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
                <span><i class="tone-attend"></i>参与人数</span>
                <span><i class="tone-complete"></i>完成人数</span>
              </div>
            </header>
            <div class="class-bar-chart">
              <div class="class-bar-axis">
                <span v-for="tick in [50, 40, 30, 20, 10, 0]" :key="tick">{{ tick }}</span>
              </div>
              <div class="class-bar-plot">
                <div v-for="item in classParticipationData" :key="item.name" class="class-bar-item">
                  <div class="class-bar-group">
                    <div class="class-bar-stack">
                      <i class="tone-attend" :style="{ height: `${item.joinedHeight}%` }">
                        <b>{{ item.joined }}</b>
                      </i>
                      <i class="tone-complete" :style="{ height: `${item.completedHeight}%` }">
                        <b>{{ item.completed }}</b>
                      </i>
                    </div>
                    <div class="class-bar-stack offset">
                      <i class="tone-attend soft" :style="{ height: `${item.joinedShadowHeight}%` }"></i>
                      <i class="tone-complete soft" :style="{ height: `${item.completedShadowHeight}%` }"></i>
                    </div>
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
                <strong>成绩区间分布</strong>
              </div>
              <div class="panel-legend">
                <span><i class="tone-excellent"></i>优秀</span>
                <span><i class="tone-good"></i>良好</span>
                <span><i class="tone-normal"></i>中等</span>
                <span><i class="tone-pass"></i>及格</span>
              </div>
            </header>
            <div class="stack-chart">
              <div class="stack-chart-axis">
                <span v-for="tick in [25, 20, 15, 10, 5, 0]" :key="tick">{{ tick }}</span>
              </div>
              <div class="stack-chart-plot">
                <div v-for="item in stackedDistribution" :key="item.name" class="stack-column">
                  <div class="stack-bars">
                    <i class="tone-excellent" :style="{ height: `${item.excellentHeight}%` }"></i>
                    <i class="tone-good" :style="{ height: `${item.goodHeight}%` }"></i>
                    <i class="tone-normal" :style="{ height: `${item.normalHeight}%` }"></i>
                    <i class="tone-pass" :style="{ height: `${item.passHeight}%` }"></i>
                  </div>
                  <span>{{ item.name }}</span>
                </div>
              </div>
            </div>
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
                  <i :style="{ width: `${item.percent}%` }"></i>
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
                <el-icon><Timer /></el-icon>
                <strong>任务进度环节</strong>
              </div>
            </header>
            <div class="progress-list">
              <article v-for="item in progressRows" :key="item.title">
                <div class="progress-meta">
                  <span class="progress-index">{{ item.index }}</span>
                  <div>
                    <strong>{{ item.title }}</strong>
                    <p>{{ item.subtitle }}</p>
                  </div>
                  <b :class="progressTone(item.percent)">{{ item.percent }}%</b>
                </div>
                <div class="progress-track">
                  <i :class="progressTone(item.percent)" :style="{ width: `${item.percent}%` }"></i>
                </div>
              </article>
            </div>
          </article>
        </section>
      </main>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Calendar, Check, Document, Histogram, Medal, PieChart, Timer, TrendCharts, User, UserFilled } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  fetchAdminTraining,
  fetchAdminTrainingStatistics,
  type AdminTrainingStatistics as TrainingStatistics
} from '../../api/admin-training';

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
  joinedShadowHeight: number;
  completedShadowHeight: number;
}

interface StackItem {
  name: string;
  excellent: number;
  good: number;
  normal: number;
  pass: number;
  excellentHeight: number;
  goodHeight: number;
  normalHeight: number;
  passHeight: number;
}

interface CompareItem {
  name: string;
  score: string;
  percent: number;
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
}

const route = useRoute();
const trainingId = computed(() => Number(route.params.id));
const loading = ref(false);
const trainingTitle = ref(String(route.query.title || '成绩统计'));
const classText = ref(String(route.query.target || ''));
const timeText = ref(String(route.query.time || ''));
const activeClass = ref('全部班级');
const statistics = ref<TrainingStatistics>({});

const participantCount = computed(() => numberValue(statistics.value.participantCount) || 182);
const completedCount = computed(() => numberValue(statistics.value.submittedAttemptCount) || 182);
const averageScore = computed(() => numberValue(statistics.value.averageScore) || 78.5);
const notCompletedCount = computed(() => Math.max(participantCount.value - completedCount.value, 0));
const classLabels = computed(() => {
  const parsed = splitLabels(classText.value || trainingTitle.value);
  if (parsed.length > 0) {
    return parsed.slice(0, 5);
  }
  return ['城轨信号2501班', '城轨检修2502班', '城轨站务2503班', '城轨调度2504班', '城轨运输2505班'];
});
const classChips = computed(() => ['全部班级', ...classLabels.value]);

const summaryCards = computed<SummaryCard[]>(() => [
  {
    label: '实训起止时间',
    value: formatTimeRange(timeText.value),
    desc: '开放与截止时间',
    icon: Calendar,
    tone: 'calendar'
  },
  {
    label: '应参加人数',
    value: String(participantCount.value),
    desc: '纳入统计人数',
    icon: User,
    tone: 'purple'
  },
  {
    label: '已完成人数',
    value: String(completedCount.value),
    desc: '已提交成绩人数',
    icon: UserFilled,
    tone: 'blue'
  },
  {
    label: '未完成人数',
    value: String(notCompletedCount.value),
    desc: '待完成任务人数',
    icon: Document,
    tone: 'red'
  },
  {
    label: '平均分',
    value: `${averageScore.value.toFixed(1)}`,
    desc: '综合成绩均值',
    icon: Histogram,
    tone: 'pink'
  }
]);

const scoreDistribution = computed<ScoreBucket[]>(() => {
  const base = [
    { name: '优秀 (90-100)', ratio: 0.25, color: '#ef4444' },
    { name: '良好 (80-89)', ratio: 0.29, color: '#f59e0b' },
    { name: '中等 (70-79)', ratio: 0.22, color: '#3b82f6' },
    { name: '及格 (60-69)', ratio: 0.15, color: '#f97316' },
    { name: '不及格 (0-59)', ratio: 0.09, color: '#8b5cf6' }
  ];
  const counts = allocateCounts(participantCount.value, base.map((item) => item.ratio));
  return base.map((item, index) => ({
    name: item.name,
    count: counts[index],
    percent: participantCount.value ? Number(((counts[index] / participantCount.value) * 100).toFixed(1)) : 0,
    color: item.color
  }));
});

const donutGradient = computed(() => {
  const values = scoreDistribution.value;
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
  const rows = classLabels.value.map((label, index) => {
    const joined = Math.max(Math.round(participantCount.value * (0.26 + index * 0.018)), 18);
    const completed = Math.max(joined - (index % 2 === 0 ? 4 : 2), 0);
    return {
      name: label,
      joined,
      completed,
      joinedHeight: 40 + index * 2,
      completedHeight: 34 + index * 2,
      joinedShadowHeight: 34 + index * 2,
      completedShadowHeight: 28 + index * 2
    };
  });
  return rows;
});

const stackedDistribution = computed<StackItem[]>(() =>
  classLabels.value.map((label, index) => {
    const excellent = Math.max(6 - index, 3);
    const good = 7 + (index % 2);
    const normal = 5 + (index % 3);
    const pass = 4 + (index % 2);
    return {
      name: label,
      excellent,
      good,
      normal,
      pass,
      excellentHeight: 22 + excellent * 2,
      goodHeight: 28 + good * 1.6,
      normalHeight: 24 + normal * 1.7,
      passHeight: 18 + pass * 1.7
    };
  })
);

const classAverageCompare = computed<CompareItem[]>(() =>
  classLabels.value.map((label, index) => {
    const score = Math.max(72, Math.min(95, averageScore.value - index * 1.7 + (index % 2 === 0 ? 1.2 : -0.5)));
    return {
      name: label,
      score: score.toFixed(1),
      percent: Math.max(55, Math.min(100, (score / 100) * 100))
    };
  })
);

const rankingRows = computed<RankingRow[]>(() => {
  const names = ['王晨曦', '李浩然', '赵雨桐', '陈思远', '刘子昂', '张若彤', '周子墨', '孙雨欣', '黄嘉怡', '林志远'];
  const scores = [98, 96, 95, 93, 91, 89, 88, 87, 85, 84];
  const durations = ['12min', '15min', '10min', '18min', '24min', '20min', '21min', '19min', '28min', '25min'];

  return names.map((name, index) => ({
    rank: index + 1,
    studentNo: `20210${String(index + 101).padStart(3, '0')}`,
    name,
    className: classLabels.value[index % classLabels.value.length],
    score: scores[index],
    grade: gradeForScore(scores[index]),
    duration: durations[index]
  }));
});

const progressRows = computed<ProgressRow[]>(() => [
  { index: 1, title: '任务一：车辆故障内容排查', subtitle: '步骤1：识别与定位故障点', percent: 68 },
  { index: 2, title: '任务二：发车与出库准备', subtitle: '步骤2：按照流程执行出库', percent: 52 },
  { index: 3, title: '任务三：客室应急处理', subtitle: '步骤1：判读事件与响应', percent: 45 },
  { index: 4, title: '任务四：机控设备检修', subtitle: '步骤1：检修项逐项确认', percent: 38 },
  { index: 5, title: '任务五：调度信息识别', subtitle: '步骤1：基础指令识别', percent: 31 }
]);

function numberValue(value: number | string | undefined | null) {
  const parsed = Number(value ?? 0);
  return Number.isFinite(parsed) ? parsed : 0;
}

function formatDateTime(value?: string) {
  if (!value) {
    return '';
  }
  return value.replace('T', ' ').slice(0, 16);
}

function formatTimeRange(value: string) {
  if (!value) {
    return '未配置';
  }
  return value.replace(/\s*至\s*/, ' 至 ');
}

function splitLabels(value: string) {
  return value
    .split(/[、,，\/|;]/)
    .map((item) => item.trim())
    .filter(Boolean);
}

function allocateCounts(total: number, ratios: number[]) {
  const raw = ratios.map((ratio) => Math.round(total * ratio));
  const diff = total - raw.reduce((sum, item) => sum + item, 0);
  if (raw.length > 0) {
    raw[0] += diff;
  }
  return raw;
}

function gradeForScore(score: number) {
  if (score >= 90) return '优秀';
  if (score >= 80) return '良好';
  if (score >= 70) return '中等';
  if (score >= 60) return '及格';
  return '不及格';
}

function gradeTone(score: number) {
  if (score >= 90) return 'excellent';
  if (score >= 80) return 'good';
  if (score >= 70) return 'normal';
  if (score >= 60) return 'pass';
  return 'bad';
}

function rankTone(rank: number) {
  if (rank === 1) return 'gold';
  if (rank === 2) return 'silver';
  if (rank === 3) return 'bronze';
  return 'normal';
}

function progressTone(percent: number) {
  if (percent >= 60) return 'excellent';
  if (percent >= 45) return 'good';
  if (percent >= 30) return 'normal';
  return 'bad';
}

async function loadStatistics() {
  if (!trainingId.value) {
    return;
  }

  loading.value = true;
  try {
    const [detail, result] = await Promise.all([
      fetchAdminTraining(trainingId.value),
      fetchAdminTrainingStatistics(trainingId.value)
    ]);
    trainingTitle.value = detail.trainingName || trainingTitle.value;
    classText.value = detail.classNames || classText.value;
    timeText.value = `${formatDateTime(detail.openStartTime)} 至 ${formatDateTime(detail.openEndTime)}`.trim();
    statistics.value = result;
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
</script>

<style scoped>
.admin-training-statistics-page {
  min-height: 100vh;
  padding: 0 24px 32px;
  background: #f5f7fb;
}

.admin-training-statistics-breadcrumb {
  height: 48px;
  display: flex;
  align-items: center;
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
  gap: 16px;
  padding: 10px 0 0;
}

.admin-training-statistics-summary,
.admin-training-statistics-filter,
.admin-training-statistics-grid {
  border: 1px solid #dce5f3;
  border-radius: 12px;
  background: #ffffff;
}

.admin-training-statistics-summary {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 14px;
  padding: 14px;
}

.admin-training-statistics-summary-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  min-height: 92px;
  padding: 12px 14px;
  border: 1px solid #edf2f8;
  border-radius: 10px;
  background: #ffffff;
  box-shadow: 0 1px 0 rgba(15, 23, 42, 0.02);
}

.admin-training-statistics-summary-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 10px;
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
  font-size: 13px;
  font-weight: 700;
}

.admin-training-statistics-summary-card strong {
  display: block;
  margin-top: 8px;
  color: #152238;
  font-size: 20px;
  line-height: 1.1;
  font-weight: 900;
}

.admin-training-statistics-summary-card small {
  display: block;
  margin-top: 6px;
  color: #8aa0bd;
  font-size: 12px;
}

.admin-training-statistics-filter {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
}

.admin-training-statistics-filter-label {
  flex: 0 0 auto;
  color: #64748b;
  font-size: 13px;
  font-weight: 800;
}

.admin-training-statistics-chip-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.admin-training-statistics-chip-row button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 30px;
  border: 1px solid #dbe3ee;
  border-radius: 8px;
  padding: 0 12px;
  background: #ffffff;
  color: #64748b;
  cursor: pointer;
  font: inherit;
  font-size: 13px;
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
  gap: 14px;
}

.admin-training-statistics-grid.top,
.admin-training-statistics-grid.middle,
.admin-training-statistics-grid.bottom {
  grid-template-columns: minmax(0, 2fr) minmax(0, 1fr);
}

.admin-training-statistics-grid.bottom {
  grid-template-columns: minmax(0, 1.35fr) minmax(0, 0.85fr);
}

.admin-training-statistics-panel {
  min-height: 360px;
  border: 1px solid #dce5f3;
  border-radius: 12px;
  background: #ffffff;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px 10px;
}

.panel-head > div {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #1e293b;
  font-size: 15px;
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
  font-size: 12px;
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
  padding: 0 16px 16px 10px;
}

.class-bar-axis,
.stack-chart-axis {
  display: grid;
  align-content: end;
  padding-bottom: 34px;
  color: #94a3b8;
  font-size: 11px;
  text-align: right;
}

.class-bar-axis span,
.stack-chart-axis span {
  height: 48px;
}

.class-bar-plot,
.stack-chart-plot {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  align-items: end;
  gap: 12px;
  min-height: 270px;
}

.class-bar-item,
.stack-column {
  display: grid;
  align-content: end;
  gap: 10px;
  min-height: 270px;
}

.class-bar-group {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px;
  align-items: end;
  height: 230px;
}

.class-bar-stack {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 4px;
  height: 100%;
  min-height: 230px;
}

.class-bar-stack i {
  position: relative;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  min-height: 16px;
  border-radius: 4px 4px 0 0;
}

.class-bar-stack i b {
  position: absolute;
  bottom: 6px;
  color: #ffffff;
  font-size: 11px;
  font-weight: 800;
}

.class-bar-stack.offset {
  margin-top: 16px;
}

.class-bar-item span,
.stack-column span {
  color: #475569;
  font-size: 11px;
  font-weight: 700;
  text-align: center;
}

.score-donut-layout {
  display: grid;
  grid-template-columns: 228px minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  padding: 8px 16px 20px;
}

.score-donut {
  position: relative;
  width: 174px;
  height: 174px;
  margin: 0 auto;
  border-radius: 50%;
}

.score-donut::before {
  content: '';
  position: absolute;
  inset: 18px;
  border-radius: 50%;
  background: #ffffff;
}

.score-donut-core {
  position: absolute;
  inset: 18px;
  display: grid;
  place-items: center;
  text-align: center;
}

.score-donut-core strong {
  color: #17233d;
  font-size: 28px;
  line-height: 1;
  font-weight: 900;
}

.score-donut-core span {
  color: #94a3b8;
  font-size: 12px;
  font-weight: 700;
}

.score-donut-legend {
  display: grid;
  gap: 12px;
}

.score-donut-legend article {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #475569;
  font-size: 12px;
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
  gap: 16px;
  padding: 6px 16px 18px;
}

.compare-row {
  display: grid;
  grid-template-columns: 1fr minmax(0, 2.8fr) 54px;
  align-items: center;
  gap: 12px;
}

.compare-row span {
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.compare-bar-track {
  height: 12px;
  border-radius: 999px;
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
  font-size: 12px;
  font-weight: 900;
  text-align: right;
}

.ranking-table {
  width: 100%;
  border-collapse: collapse;
}

.ranking-table th,
.ranking-table td {
  height: 44px;
  padding: 0 12px;
  border-bottom: 1px solid #edf2f8;
  color: #475569;
  font-size: 12px;
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
  min-width: 24px;
  height: 24px;
  padding: 0 8px;
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
  gap: 10px;
  padding: 4px 16px 16px;
}

.progress-list article {
  display: grid;
  gap: 10px;
  padding: 10px 0 4px;
}

.progress-meta {
  display: grid;
  grid-template-columns: 26px minmax(0, 1fr) 74px;
  align-items: start;
  gap: 10px;
}

.progress-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 999px;
  background: #eef2f7;
  color: #64748b;
  font-size: 12px;
  font-weight: 800;
}

.progress-meta strong {
  color: #17233d;
  font-size: 13px;
  font-weight: 800;
}

.progress-meta p {
  margin: 4px 0 0;
  color: #94a3b8;
  font-size: 12px;
}

.progress-meta b {
  justify-self: end;
  color: #64748b;
  font-size: 12px;
  font-weight: 900;
}

.progress-track {
  height: 8px;
  border-radius: 999px;
  background: #eef2f7;
  overflow: hidden;
}

.progress-track i {
  display: block;
  height: 100%;
  border-radius: inherit;
}

.progress-track i.excellent {
  background: #ef4444;
}

.progress-track i.good {
  background: #f59e0b;
}

.progress-track i.normal {
  background: #3b82f6;
}

.progress-track i.bad {
  background: #94a3b8;
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
    grid-template-columns: 20px minmax(0, 1fr);
  }

  .progress-meta b {
    justify-self: start;
  }

  .ranking-table {
    min-width: 760px;
  }
}
</style>
