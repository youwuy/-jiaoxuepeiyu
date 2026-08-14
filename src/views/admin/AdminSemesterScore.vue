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
            <span>学号</span>
            <el-input v-model="draft.studentNo" placeholder="学号搜索" clearable @keyup.enter="applyFilters" />
          </label>
          <label class="admin-semester-score-field">
            <span>学员姓名</span>
            <el-input v-model="draft.studentName" placeholder="姓名搜索" clearable @keyup.enter="applyFilters" />
          </label>
          <div class="admin-semester-score-actions-inline">
            <el-button class="admin-semester-score-query" @click="applyFilters">查询</el-button>
            <el-button class="admin-semester-score-reset" @click="resetFilters">重置</el-button>
            <el-button class="admin-semester-score-reset" :loading="loading" @click="loadPageData">刷新</el-button>
          </div>
        </div>
      </section>

      <section class="admin-semester-score-weight-card">
        <div class="admin-semester-score-weight-line">
          <el-icon><InfoFilled /></el-icon>
          <strong>成绩权重：</strong>
          <span v-for="item in weights" :key="item.name">
            <i :class="weightTone(item.name)"></i>
            {{ item.name }}
            <b>{{ item.value }}%</b>
          </span>
        </div>
        <el-button class="admin-semester-score-primary" :disabled="!can('list')" @click="openOfflineExam">线下考试成绩管理</el-button>
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
                <td>{{ row.coursewareScore }}</td>
                <td>{{ row.trainingScore }}</td>
                <td>{{ row.assignmentScore }}</td>
                <td>{{ row.examScore }}</td>
                <td><span class="admin-semester-score-total" :class="scoreTone(row.totalScore)">{{ row.totalScore }}</span></td>
                <td><span class="admin-semester-score-grade" :class="scoreTone(row.totalScore)">{{ row.grade }}</span></td>
                <td>
                  <div class="admin-semester-score-row-actions">
                    <el-button text :disabled="!can('list')" @click="openDetail(row)">查看详情</el-button>
                    <el-button text :disabled="!canFor('score:archive', 'list')" @click="openArchive(row)">学习档案</el-button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <footer class="admin-semester-score-footer">
          <p>显示 <b>{{ pageStart }}</b> 到 <b>{{ pageEnd }}</b> 条，共 <b>{{ total }}</b> 条记录</p>
          <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :page-sizes="[10, 20, 50, 100]" :total="total" layout="sizes, prev, pager, next" background @current-change="loadScores" @size-change="handlePageSizeChange" />
        </footer>
      </section>
    </section>

    <el-dialog v-model="detailVisible" class="admin-semester-score-detail-dialog" width="800px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-semester-score-dialog-head">
          <strong v-if="currentScore">{{ detailTitle(currentScore) }}</strong>
          <el-button text circle :icon="Close" @click="detailVisible = false" />
        </div>
      </template>
      <section v-if="currentScore" class="admin-semester-score-detail">
        <section class="admin-semester-score-detail-score">
          <span>综合成绩：</span>
          <strong>{{ currentScore.totalScore }}</strong>
          <i></i>
          <p>{{ detailFormula(currentScore) }}</p>
        </section>
        <section class="admin-semester-score-detail-table">
          <table>
            <thead><tr><th>序号</th><th>成绩类型</th><th>成绩权重</th><th>成绩内容</th><th>成绩值</th></tr></thead>
            <tbody>
              <tr v-for="part in scoreParts(currentScore)" :key="part.name + 'row'">
                <td>{{ part.index }}</td>
                <td><strong>{{ part.name }}</strong></td>
                <td><b>{{ part.weight }}%</b></td>
                <td><span v-for="content in part.contents" :key="content">{{ content }}</span></td>
                <td><strong v-for="score in part.scores" :key="`${part.name}-${score}`">{{ score }}</strong></td>
              </tr>
            </tbody>
          </table>
        </section>
      </section>
    </el-dialog>

    <el-dialog v-model="exportVisible" class="admin-semester-score-export-dialog" width="560px" :show-close="false" append-to-body>
      <template #header><div class="admin-semester-score-dialog-head"><strong>导出成绩</strong><el-button text circle :icon="Close" @click="exportVisible = false" /></div></template>
      <div class="admin-semester-score-export">
        <label><span>导出范围</span><el-radio-group v-model="exportForm.scope"><el-radio label="current">当前筛选结果</el-radio><el-radio label="all">全部成绩</el-radio></el-radio-group></label>
        <label><span>文件格式</span><el-select v-model="exportForm.format"><el-option label="Excel文件" value="xlsx" /><el-option label="CSV文件" value="csv" /></el-select></label>
      </div>
      <template #footer><div class="admin-semester-score-dialog-footer"><el-button @click="exportVisible = false">取消</el-button><el-button type="primary" :loading="exporting" :disabled="!can('list')" @click="confirmExport">确认导出</el-button></div></template>
    </el-dialog>

    <el-dialog v-model="weightVisible" class="admin-semester-score-export-dialog" width="620px" :show-close="false" append-to-body>
      <template #header><div class="admin-semester-score-dialog-head"><strong>成绩权重</strong><el-button text circle :icon="Close" @click="weightVisible = false" /></div></template>
      <div class="admin-semester-score-weight-list">
        <label v-for="item in weights" :key="item.name"><span>{{ item.name }}</span><el-input-number v-model="item.value" :min="0" :max="100" :disabled="!canManageScoreWeights" controls-position="right" /><em>%</em></label>
        <p>合计：<b>{{ weightTotal }}</b>%</p>
      </div>
      <template #footer><div class="admin-semester-score-dialog-footer"><el-button @click="weightVisible = false">取消</el-button><el-button type="primary" :loading="savingWeights" :disabled="!canManageScoreWeights" @click="saveWeights">保存</el-button></div></template>
    </el-dialog>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Close, InfoFilled } from '@element-plus/icons-vue';
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
  fetchAdminScoreGradeRules,
  fetchAdminScoreWeights,
  type AdminClass,
  type AdminScoreGradeRule,
  type AdminScoreWeight
} from '../../api/admin-settings';
import { useAdminPermissions } from '../../features/admin/use-admin-permissions';

interface SemesterScoreRow {
  scoreId: number;
  semesterId?: number;
  classId?: number;
  studentId?: number;
  studentName: string;
  studentNo: string;
  className: string;
  term: string;
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
  studentName: string;
  studentNo: string;
}

const router = useRouter();
const { can, canFor } = useAdminPermissions('score:semester');
const canManageScoreWeights = computed(() => canFor('config:score-weight', 'create') || canFor('system:settings', 'create'));
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);
const exporting = ref(false);
const savingWeights = ref(false);
const detailVisible = ref(false);
const exportVisible = ref(false);
const weightVisible = ref(false);
const currentScore = ref<SemesterScoreRow | null>(null);
const draft = reactive<ScoreFilters>({ semesterId: null, classId: null, studentName: '', studentNo: '' });
const applied = ref<ScoreFilters>({ ...draft });
const exportForm = reactive({ scope: 'current', format: 'xlsx' });
const semesterOptions = ref<SemesterOption[]>([]);
const classOptions = ref<AdminClass[]>([]);
const gradeRules = ref<AdminScoreGradeRule[]>([]);
const weights = reactive([
  { name: '课件学习', value: 0 },
  { name: '实训练习', value: 0 },
  { name: '课程作业', value: 0 },
  { name: '考试', value: 0 }
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

const pageStart = computed(() => total.value ? (page.value - 1) * pageSize.value + 1 : 0);
const pageEnd = computed(() => Math.min(page.value * pageSize.value, total.value));
const weightTotal = computed(() => weights.reduce((sum, item) => sum + Number(item.value || 0), 0));

function currentQuery(includePage = true): AdminSemesterScoreQuery {
  return {
    semesterId: applied.value.semesterId,
    classId: applied.value.classId,
    studentName: applied.value.studentName.trim() || undefined,
    studentNo: applied.value.studentNo.trim() || undefined,
    page: includePage ? page.value : undefined,
    pageSize: includePage ? pageSize.value : undefined
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
  return gradeRules.value.find((rule) => score >= rule.minScore && score <= rule.maxScore)?.gradeName || '-';
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

async function loadGradeRules() {
  try {
    gradeRules.value = await fetchAdminScoreGradeRules();
  } catch (error) {
    gradeRules.value = [];
    ElMessage.error(error instanceof Error ? error.message : '成绩等级加载失败');
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
  Object.assign(draft, { semesterId: null, classId: null, studentName: '', studentNo: '' });
  const currentSemester = semesterOptions.value.find((item) => item.current);
  draft.semesterId = currentSemester?.semesterId ?? null;
  applyFilters();
}

function handlePageSizeChange() {
  page.value = 1;
  void loadScores();
}

function scoreTone(score: number) { if (score >= 85) return 'excellent'; if (score >= 75) return 'good'; if (score >= 60) return 'normal'; return 'bad'; }
function openDetail(row: SemesterScoreRow) { currentScore.value = row; detailVisible.value = true; }
function openArchive(row: SemesterScoreRow) {
  router.push({ path: '/admin/training-archive', query: { keyword: row.studentNo } });
}
function openOfflineExam() { router.push('/admin/semester-score/offline'); }
function weightTone(name: string) {
  if (name.includes('实训')) return 'training';
  if (name.includes('作业')) return 'assignment';
  if (name.includes('考试')) return 'exam';
  return 'courseware';
}
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

function detailTitle(row: SemesterScoreRow) {
  return `${row.studentName}（${row.studentNo}）${row.term}综合成绩详情`;
}

function detailFormula(row: SemesterScoreRow) {
  return `计算公式：课件学习×${row.coursewareWeight}% + 实训练习×${row.trainingPracticeWeight}% + 课程作业×${row.assignmentWeight}% + 考试×${row.examWeight}%`;
}

function scoreParts(row: SemesterScoreRow) {
  return [
    {
      index: 1,
      name: '课件学习',
      weight: row.coursewareWeight,
      contents: ['本学期课件学习汇总'],
      scores: [row.coursewareScore]
    },
    { index: 2, name: '实训练习', weight: row.trainingPracticeWeight, contents: ['本学期实训练习汇总'], scores: [row.trainingScore] },
    { index: 3, name: '课程作业', weight: row.assignmentWeight, contents: ['本学期课程作业汇总'], scores: [row.assignmentScore] },
    { index: 4, name: '考试', weight: row.examWeight, contents: ['本学期考试成绩汇总'], scores: [row.examScore] }
  ];
}

onMounted(async () => {
  await Promise.all([loadOptions(), loadWeights(), loadGradeRules()]);
  await loadPageData();
});
</script>

<style scoped>
.admin-semester-score-filter-row {
  grid-template-columns: 160px 150px minmax(220px, 1fr) 150px auto;
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
