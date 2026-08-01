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
            <el-select v-model="draft.term" placeholder="请选择学年学期" clearable>
              <el-option v-for="item in termOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </label>
          <label class="admin-semester-score-field">
            <span>所属班级</span>
            <el-select v-model="draft.className" placeholder="请选择班级" clearable>
              <el-option v-for="item in classOptions" :key="item" :label="item" :value="item" />
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
          <el-button class="admin-semester-score-query" @click="applyFilters">查询</el-button>
          <el-button class="admin-semester-score-reset" @click="resetFilters">重置</el-button>
        </div>
      </section>

      <section class="admin-semester-score-summary">
        <article><span>总人数</span><strong>{{ filteredScores.length }}</strong></article>
        <article><span>优秀人数</span><strong>{{ excellentCount }}</strong></article>
        <article><span>及格人数</span><strong>{{ passCount }}</strong></article>
        <article><span>平均分</span><strong>{{ averageScore }}</strong></article>
        <article><span>最高分</span><strong>{{ maxScore }}</strong></article>
      </section>

      <section class="admin-semester-score-actions">
        <p>共 <b>{{ filteredScores.length }}</b> 条综合成绩</p>
        <div>
          <el-button class="admin-semester-score-lite" @click="openWeightDialog">成绩权重</el-button>
          <el-button class="admin-semester-score-primary" @click="openExport">导出成绩</el-button>
        </div>
      </section>

      <section class="admin-semester-score-board">
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
              <tr v-for="(row, index) in pagedScores" :key="row.studentNo + row.courseName">
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
          <p>显示 <b>{{ pageStart }}</b> 到 <b>{{ pageEnd }}</b> 条，共 <b>{{ filteredScores.length }}</b> 条记录</p>
          <el-pagination v-model:current-page="page" :page-size="pageSize" :total="filteredScores.length" layout="prev, pager, next" background />
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
          <p>课件学习进度得分 × 30% + 实训练习得分 × 30% + 课程作业得分 × 30% + 考试得分 × 10%</p>
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
      <template #footer><div class="admin-semester-score-dialog-footer"><el-button @click="exportVisible = false">取消</el-button><el-button type="primary" @click="confirmExport">确认导出</el-button></div></template>
    </el-dialog>

    <el-dialog v-model="weightVisible" class="admin-semester-score-export-dialog" width="620px" :show-close="false" append-to-body>
      <template #header><div class="admin-semester-score-dialog-head"><strong>成绩权重</strong><el-button text circle :icon="Close" @click="weightVisible = false" /></div></template>
      <div class="admin-semester-score-weight-list">
        <label v-for="item in weights" :key="item.name"><span>{{ item.name }}</span><el-input-number v-model="item.value" :min="0" :max="100" controls-position="right" /><em>%</em></label>
        <p>合计：<b>{{ weightTotal }}</b>%</p>
      </div>
      <template #footer><div class="admin-semester-score-dialog-footer"><el-button @click="weightVisible = false">取消</el-button><el-button type="primary" @click="saveWeights">保存</el-button></div></template>
    </el-dialog>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Close } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';

interface SemesterScoreRow {
  studentName: string;
  studentNo: string;
  className: string;
  term: string;
  courseName: string;
  coursewareScore: number;
  trainingScore: number;
  assignmentScore: number;
  examScore: number;
  totalScore: number;
  grade: string;
}

const page = ref(1);
const pageSize = 10;
const detailVisible = ref(false);
const exportVisible = ref(false);
const weightVisible = ref(false);
const currentScore = ref<SemesterScoreRow | null>(null);
const draft = reactive({ term: '', className: '', courseName: '', studentName: '', studentNo: '' });
const applied = ref({ ...draft });
const exportForm = reactive({ scope: 'current', format: 'xlsx' });
const weights = reactive([
  { name: '课件学习', value: 30 },
  { name: '实训练习', value: 30 },
  { name: '课程作业', value: 30 },
  { name: '考试', value: 10 }
]);

const scores = ref<SemesterScoreRow[]>([
  { studentName: '张明远', studentNo: '2024CGXH001', className: '信号1班', term: '2024-2025学年 下学期', courseName: '城市轨道交通信号系统', coursewareScore: 96, trainingScore: 92, assignmentScore: 90, examScore: 88, totalScore: 92.1, grade: '优秀' },
  { studentName: '李晓婷', studentNo: '2024CGXH002', className: '信号1班', term: '2024-2025学年 下学期', courseName: '城市轨道交通信号系统', coursewareScore: 90, trainingScore: 85, assignmentScore: 86, examScore: 82, totalScore: 86.7, grade: '优秀' },
  { studentName: '王志强', studentNo: '2024CGXH003', className: '信号2班', term: '2024-2025学年 下学期', courseName: '城市轨道交通信号系统', coursewareScore: 84, trainingScore: 78, assignmentScore: 80, examScore: 76, totalScore: 80.2, grade: '良好' },
  { studentName: '赵雨涵', studentNo: '2024CGXH004', className: '信号1班', term: '2024-2025学年 下学期', courseName: '车站运营管理', coursewareScore: 98, trainingScore: 95, assignmentScore: 94, examScore: 92, totalScore: 95.1, grade: '优秀' },
  { studentName: '陈浩然', studentNo: '2024CGXH005', className: '信号2班', term: '2024-2025学年 下学期', courseName: '城轨车辆构造', coursewareScore: 65, trainingScore: 58, assignmentScore: 62, examScore: 60, totalScore: 61.7, grade: '中等' },
  { studentName: '刘思琪', studentNo: '2024CGXH006', className: '信号1班', term: '2024-2025学年 下学期', courseName: '城市轨道交通信号系统', coursewareScore: 88, trainingScore: 90, assignmentScore: 84, examScore: 80, totalScore: 86.6, grade: '优秀' },
  { studentName: '周子轩', studentNo: '2024CGXH007', className: '信号2班', term: '2024-2025学年 下学期', courseName: '城轨供电系统', coursewareScore: 76, trainingScore: 72, assignmentScore: 70, examScore: 68, totalScore: 72.0, grade: '中等' },
  { studentName: '吴嘉豪', studentNo: '2024CGXH008', className: '信号1班', term: '2024-2025学年 下学期', courseName: '城市轨道交通信号系统', coursewareScore: 100, trainingScore: 96, assignmentScore: 98, examScore: 95, totalScore: 97.8, grade: '优秀' },
  { studentName: '孙悦然', studentNo: '2024CGXH009', className: '信号3班', term: '2024-2025学年 下学期', courseName: '行车组织', coursewareScore: 82, trainingScore: 76, assignmentScore: 78, examScore: 74, totalScore: 78.4, grade: '良好' },
  { studentName: '黄俊杰', studentNo: '2024CGXH010', className: '信号3班', term: '2024-2025学年 下学期', courseName: '信号设备维护', coursewareScore: 58, trainingScore: 55, assignmentScore: 60, examScore: 52, totalScore: 57.2, grade: '较差' },
  { studentName: '马欣怡', studentNo: '2024CGXH011', className: '信号1班', term: '2024-2025学年 下学期', courseName: '城市轨道交通信号系统', coursewareScore: 89, trainingScore: 84, assignmentScore: 88, examScore: 86, totalScore: 86.9, grade: '优秀' },
  { studentName: '朱博文', studentNo: '2024CGXH012', className: '信号2班', term: '2024-2025学年 下学期', courseName: '信号系统原理', coursewareScore: 74, trainingScore: 70, assignmentScore: 69, examScore: 72, totalScore: 71.2, grade: '中等' }
]);

const termOptions = computed(() => Array.from(new Set(scores.value.map((item) => item.term))));
const classOptions = computed(() => Array.from(new Set(scores.value.map((item) => item.className))));
const filteredScores = computed(() => scores.value.filter((item) => (!applied.value.term || item.term === applied.value.term) && (!applied.value.className || item.className === applied.value.className) && (!applied.value.courseName || item.courseName.includes(applied.value.courseName)) && (!applied.value.studentName || item.studentName.includes(applied.value.studentName)) && (!applied.value.studentNo || item.studentNo.includes(applied.value.studentNo))));
const pagedScores = computed(() => filteredScores.value.slice((page.value - 1) * pageSize, page.value * pageSize));
const pageStart = computed(() => filteredScores.value.length ? (page.value - 1) * pageSize + 1 : 0);
const pageEnd = computed(() => Math.min(page.value * pageSize, filteredScores.value.length));
const excellentCount = computed(() => filteredScores.value.filter((item) => item.totalScore >= 85).length);
const passCount = computed(() => filteredScores.value.filter((item) => item.totalScore >= 60).length);
const averageScore = computed(() => filteredScores.value.length ? (filteredScores.value.reduce((sum, item) => sum + item.totalScore, 0) / filteredScores.value.length).toFixed(1) : '0');
const maxScore = computed(() => filteredScores.value.length ? Math.max(...filteredScores.value.map((item) => item.totalScore)).toFixed(1) : '0');
const weightTotal = computed(() => weights.reduce((sum, item) => sum + Number(item.value || 0), 0));

function applyFilters() { applied.value = { ...draft }; page.value = 1; }
function resetFilters() { Object.assign(draft, { term: '', className: '', courseName: '', studentName: '', studentNo: '' }); applyFilters(); }
function scoreTone(score: number) { if (score >= 85) return 'excellent'; if (score >= 75) return 'good'; if (score >= 60) return 'normal'; return 'bad'; }
function openDetail(row: SemesterScoreRow) { currentScore.value = row; detailVisible.value = true; }
function openArchive(row: SemesterScoreRow) { ElMessage.info(`${row.studentName} 的学习档案入口已预留`); }
function openExport() { exportVisible.value = true; }
function openWeightDialog() { weightVisible.value = true; }
function confirmExport() { exportVisible.value = false; ElMessage.success('成绩导出任务已创建'); }
function saveWeights() { weightVisible.value = false; ElMessage.success('成绩权重已保存'); }
function scoreParts(row: SemesterScoreRow) {
  return [
    { name: '课件学习', score: row.coursewareScore, weight: 30, weighted: (row.coursewareScore * 0.3).toFixed(1), status: '已完成' },
    { name: '实训练习', score: row.trainingScore, weight: 30, weighted: (row.trainingScore * 0.3).toFixed(1), status: '已完成' },
    { name: '课程作业', score: row.assignmentScore, weight: 30, weighted: (row.assignmentScore * 0.3).toFixed(1), status: '已批阅' },
    { name: '考试', score: row.examScore, weight: 10, weighted: (row.examScore * 0.1).toFixed(1), status: '已完成' }
  ];
}
</script>
