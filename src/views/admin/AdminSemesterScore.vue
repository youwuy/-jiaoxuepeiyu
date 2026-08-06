<template>
  <AdminShell activeKey="semester-score">
    <section class="admin-semester-score-page">
      <el-breadcrumb class="admin-semester-score-breadcrumb" separator="/">
        <el-breadcrumb-item>成绩统计</el-breadcrumb-item>
        <el-breadcrumb-item>综合成绩</el-breadcrumb-item>
      </el-breadcrumb>

      <section class="admin-semester-score-filter-card">
        <div class="admin-semester-score-filter-row">
          <label class="admin-semester-score-field">
            <span>学年学期</span>
            <el-select v-model="draft.semesterId" placeholder="请选择学年学期" clearable>
              <el-option v-for="item in semesterOptions" :key="item.semesterId" :label="item.label" :value="item.semesterId" />
            </el-select>
          </label>
          <label class="admin-semester-score-field">
            <span>所属班级</span>
            <el-select v-model="draft.classId" placeholder="请选择班级" clearable>
              <el-option v-for="item in classOptions" :key="item.classId" :label="item.className" :value="item.classId" />
            </el-select>
          </label>
          <label class="admin-semester-score-field">
            <span>课程名称</span>
            <el-input v-model="draft.courseName" placeholder="请输入课程名称" clearable @keyup.enter="applyFilters" />
          </label>
          <label class="admin-semester-score-field">
            <span>学员姓名</span>
            <el-input v-model="draft.studentName" placeholder="请输入学员姓名" clearable @keyup.enter="applyFilters" />
          </label>
          <label class="admin-semester-score-field">
            <span>学号</span>
            <el-input v-model="draft.studentNo" placeholder="请输入学号" clearable @keyup.enter="applyFilters" />
          </label>
          <div class="admin-semester-score-actions-inline">
            <el-button class="admin-semester-score-query" @click="applyFilters">查询</el-button>
            <el-button class="admin-semester-score-reset" @click="resetFilters">重置</el-button>
          </div>
        </div>
      </section>

      <section class="admin-semester-score-summary">
        <article><span>总人数</span><strong>{{ statistics.studentCount }}</strong></article>
        <article><span>优秀人数</span><strong>{{ statistics.excellentCount }}</strong></article>
        <article><span>及格人数</span><strong>{{ statistics.passCount }}</strong></article>
        <article><span>平均分</span><strong>{{ formatScore(statistics.averageScore) }}</strong></article>
        <article><span>最高分</span><strong>{{ formatScore(statistics.maxScore) }}</strong></article>
      </section>

      <section class="admin-semester-score-actions">
        <p>共 <b>{{ total }}</b> 条综合成绩</p>
        <div>
          <el-button class="admin-semester-score-lite" @click="openWeightDialog">成绩权重</el-button>
          <el-button class="admin-semester-score-primary" @click="openExport">导出成绩</el-button>
        </div>
      </section>

      <section class="admin-semester-score-board" v-loading="loading">
        <div class="admin-semester-score-table-scroll">
          <table class="admin-semester-score-table">
            <thead>
              <tr>
                <th>序号</th>
                <th>学员姓名</th>
                <th>学号</th>
                <th>所属班级</th>
                <th>学年学期</th>
                <th>课程名称</th>
                <th>课件学习</th>
                <th>实训练习</th>
                <th>课程作业</th>
                <th>考试</th>
                <th>综合成绩</th>
                <th>等级</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, index) in scores" :key="row.scoreId || `${row.studentNo}-${row.semesterId}`">
                <td>{{ (page - 1) * pageSize + index + 1 }}</td>
                <td><strong>{{ row.studentName }}</strong></td>
                <td>{{ row.studentNo }}</td>
                <td>{{ row.className }}</td>
                <td>{{ row.term }}</td>
                <td>{{ row.courseName }}</td>
                <td>{{ row.coursewareScore }}</td>
                <td>{{ row.trainingScore }}</td>
                <td>{{ row.assignmentScore }}</td>
                <td>{{ row.examScore }}</td>
                <td><span class="admin-semester-score-total" :class="scoreTone(row.totalScore)">{{ row.totalScore }}</span></td>
                <td><span class="admin-semester-score-grade" :class="scoreTone(row.totalScore)">{{ row.grade }}</span></td>
                <td>
                  <div class="admin-semester-score-row-actions">
                    <el-button text @click="openDetail(row)">查看详情</el-button>
                    <el-button text @click="openArchive(row)">学习档案</el-button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <footer class="admin-semester-score-footer">
          <p>显示 <b>{{ pageStart }}</b> 到 <b>{{ pageEnd }}</b> 条，共 <b>{{ total }}</b> 条记录</p>
          <el-pagination v-model:current-page="page" :page-size="pageSize" :total="total" layout="prev, pager, next" background @current-change="loadScores" />
        </footer>
      </section>
    </section>

    <el-dialog v-model="detailVisible" class="admin-semester-score-detail-dialog" width="920px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-semester-score-dialog-head">
          <strong>综合成绩详情</strong>
          <el-button text circle :icon="Close" @click="detailVisible = false" />
        </div>
      </template>
      <section v-if="currentScore" class="admin-semester-score-detail">
        <header>
          <div><span>学员姓名</span><strong>{{ currentScore.studentName }}</strong></div>
          <div><span>学号</span><strong>{{ currentScore.studentNo }}</strong></div>
          <div><span>所属班级</span><strong>{{ currentScore.className }}</strong></div>
          <div><span>综合成绩</span><b :class="scoreTone(currentScore.totalScore)">{{ currentScore.totalScore }}</b></div>
        </header>
        <section class="admin-semester-score-formula">
          <strong>综合成绩计算公式</strong>
          <p>{{ scoreFormula }}</p>
        </section>
        <div class="admin-semester-score-detail-grid">
          <article v-for="part in scoreParts(currentScore)" :key="part.name">
            <span>{{ part.name }}</span>
            <strong>{{ part.score }}</strong>
            <p>权重 {{ part.weight }}%，折算 {{ part.weighted }} 分</p>
            <el-progress :percentage="part.score" :show-text="false" />
          </article>
        </div>
        <section class="admin-semester-score-detail-table">
          <strong>明细记录</strong>
          <table>
            <thead><tr><th>模块</th><th>完成情况</th><th>原始得分</th><th>权重</th><th>折算得分</th></tr></thead>
            <tbody>
              <tr v-for="part in scoreParts(currentScore)" :key="part.name + 'row'"><td>{{ part.name }}</td><td>{{ part.status }}</td><td>{{ part.score }}</td><td>{{ part.weight }}%</td><td>{{ part.weighted }}</td></tr>
            </tbody>
          </table>
        </section>
      </section>
      <template #footer><div class="admin-semester-score-dialog-footer"><el-button @click="detailVisible = false">关闭</el-button><el-button type="primary" @click="openExport">导出详情</el-button></div></template>
    </el-dialog>

    <el-dialog v-model="exportVisible" class="admin-semester-score-export-dialog" width="560px" :show-close="false" append-to-body>
      <template #header><div class="admin-semester-score-dialog-head"><strong>导出成绩</strong><el-button text circle :icon="Close" @click="exportVisible = false" /></div></template>
      <div class="admin-semester-score-export">
        <label><span>导出范围</span><el-radio-group v-model="exportForm.scope"><el-radio label="current">当前筛选结果</el-radio><el-radio label="all">全部成绩</el-radio></el-radio-group></label>
        <label><span>文件格式</span><el-select v-model="exportForm.format"><el-option label="Excel文件" value="xlsx" /><el-option label="CSV文件" value="csv" /></el-select></label>
      </div>
      <template #footer><div class="admin-semester-score-dialog-footer"><el-button @click="exportVisible = false">取消</el-button><el-button type="primary" :loading="exporting" @click="confirmExport">确认导出</el-button></div></template>
    </el-dialog>

    <el-dialog v-model="weightVisible" class="admin-semester-score-export-dialog" width="620px" :show-close="false" append-to-body>
      <template #header><div class="admin-semester-score-dialog-head"><strong>成绩权重</strong><el-button text circle :icon="Close" @click="weightVisible = false" /></div></template>
      <div class="admin-semester-score-weight-list">
        <label v-for="item in weights" :key="item.name"><span>{{ item.name }}</span><el-input-number v-model="item.value" :min="0" :max="100" controls-position="right" /><em>%</em></label>
        <p>合计：<b>{{ weightTotal }}</b>%</p>
      </div>
      <template #footer><div class="admin-semester-score-dialog-footer"><el-button @click="weightVisible = false">取消</el-button><el-button type="primary" :loading="savingWeights" @click="saveWeights">保存</el-button></div></template>
    </el-dialog>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Close } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  exportAdminSemesterScores,
  fetchAdminSemesterScores,
  fetchAdminSemesterScoreStatistics,
  type AdminSemesterScore,
  type AdminSemesterScoreQuery,
  type AdminSemesterScoreStatistics
} from '../../api/admin-semester-score';
import {
  createAdminScoreWeight,
  fetchAdminAcademicYears,
  fetchAdminClasses,
  fetchAdminScoreWeights,
  type AdminClass,
  type AdminScoreWeight
} from '../../api/admin-settings';

interface SemesterScoreRow {
  scoreId: number;
  semesterId?: number;
  classId?: number;
  studentId?: number;
  studentName: string;
  studentNo: string;
  className: string;
  term: string;
  courseName: string;
  coursewareScore: number;
  trainingScore: number;
  assignmentScore: number;
  examScore: number;
  coursewareWeight: number;
  trainingPracticeWeight: number;
  assignmentWeight: number;
  examWeight: number;
  totalScore: number;
  grade: string;
}

interface SemesterOption {
  semesterId: number;
  label: string;
  current: boolean;
}

interface ScoreFilters {
  semesterId: number | null;
  classId: number | null;
  courseName: string;
  studentName: string;
  studentNo: string;
}

const router = useRouter();
const page = ref(1);
const pageSize = 10;
const total = ref(0);
const loading = ref(false);
const exporting = ref(false);
const savingWeights = ref(false);
const detailVisible = ref(false);
const exportVisible = ref(false);
const weightVisible = ref(false);
const currentScore = ref<SemesterScoreRow | null>(null);
const draft = reactive<ScoreFilters>({ semesterId: null, classId: null, courseName: '', studentName: '', studentNo: '' });
const applied = ref<ScoreFilters>({ ...draft });
const exportForm = reactive({ scope: 'current', format: 'xlsx' });
const semesterOptions = ref<SemesterOption[]>([]);
const classOptions = ref<AdminClass[]>([]);
const weights = reactive([
  { name: '课件学习', value: 30 },
  { name: '实训练习', value: 30 },
  { name: '课程作业', value: 30 },
  { name: '考试', value: 10 }
]);
const statistics = reactive<AdminSemesterScoreStatistics>({
  studentCount: 0,
  averageScore: 0,
  maxScore: 0,
  minScore: 0,
  excellentCount: 0,
  passCount: 0
});

const scores = ref<SemesterScoreRow[]>([]);

const pageStart = computed(() => total.value ? (page.value - 1) * pageSize + 1 : 0);
const pageEnd = computed(() => Math.min(page.value * pageSize, total.value));
const weightTotal = computed(() => weights.reduce((sum, item) => sum + Number(item.value || 0), 0));
const scoreFormula = computed(() => `课件学习进度得分 × ${weights[0].value}% + 实训练习得分 × ${weights[1].value}% + 课程作业得分 × ${weights[2].value}% + 考试得分 × ${weights[3].value}%`);

function currentQuery(includePage = true): AdminSemesterScoreQuery {
  const keyword = [applied.value.courseName, applied.value.studentName, applied.value.studentNo]
    .map((item) => item.trim())
    .filter(Boolean)
    .join(' ');

  return {
    semesterId: applied.value.semesterId,
    classId: applied.value.classId,
    keyword: keyword || undefined,
    page: includePage ? page.value : undefined,
    pageSize: includePage ? pageSize : undefined
  };
}

function mapScore(score: AdminSemesterScore): SemesterScoreRow {
  const totalScore = numberValue(score.comprehensiveScore);
  return {
    scoreId: score.scoreId,
    semesterId: score.semesterId,
    classId: score.classId,
    studentId: score.studentId,
    studentName: score.studentName || '-',
    studentNo: score.studentNo || '-',
    className: score.className || '-',
    term: score.academicTerm || '-',
    courseName: '综合成绩',
    coursewareScore: numberValue(score.coursewareLearningScore),
    trainingScore: numberValue(score.trainingPracticeScore),
    assignmentScore: numberValue(score.courseAssignmentScore),
    examScore: numberValue(score.examScore),
    coursewareWeight: score.coursewareWeight ?? weights[0].value,
    trainingPracticeWeight: score.trainingPracticeWeight ?? weights[1].value,
    assignmentWeight: score.assignmentWeight ?? weights[2].value,
    examWeight: score.examWeight ?? weights[3].value,
    totalScore,
    grade: gradeForScore(totalScore)
  };
}

function numberValue(value: number | string | undefined | null): number {
  const number = Number(value ?? 0);
  return Number.isFinite(number) ? number : 0;
}

function gradeForScore(score: number) {
  if (score >= 90) return '优秀';
  if (score >= 80) return '良好';
  if (score >= 60) return '及格';
  return '不及格';
}

function formatScore(value: number | string | undefined | null) {
  return numberValue(value).toFixed(1);
}

async function loadOptions() {
  try {
    const [years, classes] = await Promise.all([fetchAdminAcademicYears(), fetchAdminClasses()]);
    semesterOptions.value = years.flatMap((year) =>
      year.semesters.map((semester) => ({
        semesterId: semester.semesterId,
        label: `${year.yearName} ${semester.semesterName}`,
        current: semester.current
      }))
    );
    classOptions.value = classes;
    if (!draft.semesterId) {
      const currentSemester = semesterOptions.value.find((item) => item.current);
      draft.semesterId = currentSemester?.semesterId ?? null;
      applied.value.semesterId = draft.semesterId;
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '筛选选项加载失败');
  }
}

async function loadWeights() {
  try {
    const rows = await fetchAdminScoreWeights();
    const latest = [...rows].sort((left, right) => Number(right.weightId || 0) - Number(left.weightId || 0))[0];
    applyWeightRow(latest);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '成绩权重加载失败');
  }
}

function applyWeightRow(row?: AdminScoreWeight) {
  if (!row) {
    return;
  }
  weights[0].value = row.coursewareWeight;
  weights[1].value = row.trainingPracticeWeight;
  weights[2].value = row.assignmentWeight;
  weights[3].value = row.examWeight;
}

async function loadStatistics() {
  try {
    Object.assign(statistics, await fetchAdminSemesterScoreStatistics(currentQuery(false)));
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '综合成绩统计加载失败');
  }
}

async function loadScores() {
  loading.value = true;
  try {
    const result = await fetchAdminSemesterScores(currentQuery());
    scores.value = (result.records || []).map(mapScore);
    total.value = result.total || 0;
  } catch (error) {
    scores.value = [];
    total.value = 0;
    ElMessage.error(error instanceof Error ? error.message : '综合成绩加载失败');
  } finally {
    loading.value = false;
  }
}

async function loadPageData() {
  await Promise.all([loadScores(), loadStatistics()]);
}

function applyFilters() {
  applied.value = { ...draft };
  page.value = 1;
  loadPageData();
}

function resetFilters() {
  Object.assign(draft, { semesterId: null, classId: null, courseName: '', studentName: '', studentNo: '' });
  const currentSemester = semesterOptions.value.find((item) => item.current);
  draft.semesterId = currentSemester?.semesterId ?? null;
  applyFilters();
}

function scoreTone(score: number) { if (score >= 85) return 'excellent'; if (score >= 75) return 'good'; if (score >= 60) return 'normal'; return 'bad'; }
function openDetail(row: SemesterScoreRow) { currentScore.value = row; detailVisible.value = true; }
function openArchive(row: SemesterScoreRow) {
  router.push({ path: '/admin/training-archive', query: { keyword: row.studentNo } });
}
function openExport() { exportVisible.value = true; }
function openWeightDialog() { weightVisible.value = true; }
async function confirmExport() {
  exporting.value = true;
  try {
    await exportAdminSemesterScores(exportForm.scope === 'all' ? {} : currentQuery(false));
    exportVisible.value = false;
    ElMessage.success('成绩文件已导出');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '成绩导出失败');
  } finally {
    exporting.value = false;
  }
}

async function saveWeights() {
  if (weightTotal.value !== 100) {
    ElMessage.warning('成绩权重合计必须为100%');
    return;
  }
  if (!draft.semesterId && !applied.value.semesterId) {
    ElMessage.warning('请选择学年学期');
    return;
  }

  savingWeights.value = true;
  try {
    await createAdminScoreWeight({
      semesterId: (draft.semesterId || applied.value.semesterId) as number,
      coursewareWeight: Number(weights[0].value || 0),
      trainingPracticeWeight: Number(weights[1].value || 0),
      assignmentWeight: Number(weights[2].value || 0),
      examWeight: Number(weights[3].value || 0)
    });
    weightVisible.value = false;
    await loadWeights();
    await loadPageData();
    ElMessage.success('成绩权重已保存');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '成绩权重保存失败');
  } finally {
    savingWeights.value = false;
  }
}

function scoreParts(row: SemesterScoreRow) {
  return [
    { name: '课件学习', score: row.coursewareScore, weight: row.coursewareWeight, weighted: (row.coursewareScore * row.coursewareWeight / 100).toFixed(1), status: '已完成' },
    { name: '实训练习', score: row.trainingScore, weight: row.trainingPracticeWeight, weighted: (row.trainingScore * row.trainingPracticeWeight / 100).toFixed(1), status: '已完成' },
    { name: '课程作业', score: row.assignmentScore, weight: row.assignmentWeight, weighted: (row.assignmentScore * row.assignmentWeight / 100).toFixed(1), status: '已批阅' },
    { name: '考试', score: row.examScore, weight: row.examWeight, weighted: (row.examScore * row.examWeight / 100).toFixed(1), status: '已完成' }
  ];
}

onMounted(async () => {
  await Promise.all([loadOptions(), loadWeights()]);
  await loadPageData();
});
</script>

<style scoped>
.admin-semester-score-filter-row {
  grid-template-columns: 160px 150px minmax(220px, 1fr) minmax(220px, 1fr) 150px auto;
  column-gap: 14px;
  align-items: end;
}

.admin-semester-score-actions-inline {
  display: flex;
  align-items: flex-end;
  align-self: end;
  justify-self: start;
  gap: 12px;
  margin-left: 0;
  padding-bottom: 0;
  height: 42px;
}

.admin-semester-score-query.el-button,
.admin-semester-score-reset.el-button {
  min-width: 60px;
  height: 42px;
  flex: 0 0 auto;
}

@media (max-width: 1280px) {
  .admin-semester-score-filter-row {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .admin-semester-score-actions-inline {
    grid-column: 1 / -1;
    margin-left: 0;
    padding-bottom: 0;
    justify-content: flex-start;
  }
}
</style>
