<template>
  <AdminShell activeKey="semester-score">
    <section v-if="viewMode === 'list'" class="admin-semester-score-page">
      <el-breadcrumb class="admin-semester-score-breadcrumb" separator="/">
        <el-breadcrumb-item>成绩统计</el-breadcrumb-item>
        <el-breadcrumb-item>综合成绩</el-breadcrumb-item>
      </el-breadcrumb>

      <section class="admin-semester-score-filter-card">
        <div class="admin-semester-score-filter-row">
          <el-select v-model="draft.term" placeholder="2024-2025年上学期">
            <el-option v-for="item in termOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="draft.className" placeholder="请选择班级" clearable>
            <el-option v-for="item in classOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-input v-model="draft.studentNo" :prefix-icon="Search" placeholder="学号搜索" clearable @keyup.enter="applyFilters" />
          <el-input v-model="draft.studentName" :prefix-icon="Search" placeholder="姓名搜索" clearable @keyup.enter="applyFilters" />
          <el-button class="admin-semester-score-query" @click="applyFilters">查询</el-button>
          <el-button class="admin-semester-score-reset" @click="resetFilters">重置</el-button>
        </div>
      </section>

      <section class="admin-semester-score-weight-card">
        <div class="admin-semester-score-weight-line">
          <el-icon><InfoFilled /></el-icon>
          <strong>成绩权重：</strong>
          <span v-for="item in weightLegend" :key="item.name">
            <i :style="{ background: item.color }"></i>
            {{ item.name }}
            <b>{{ item.value }}%</b>
          </span>
        </div>
        <el-button class="admin-semester-score-primary" @click="openOfflinePage">线下考试成绩管理</el-button>
      </section>

      <section class="admin-semester-score-board">
        <div class="admin-semester-score-table-scroll">
          <table class="admin-semester-score-table">
            <thead>
              <tr>
                <th>序号</th>
                <th>姓名</th>
                <th>学号</th>
                <th>班级</th>
                <th>所属学年学期</th>
                <th>课件学习</th>
                <th>实训练习</th>
                <th>课程作业</th>
                <th>考试</th>
                <th>综合成绩</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, index) in pagedScores" :key="row.studentNo" @click="openDetail(row)">
                <td>{{ (page - 1) * pageSize + index + 1 }}</td>
                <td><strong>{{ row.studentName }}</strong></td>
                <td>{{ row.studentNo }}</td>
                <td>{{ row.className }}</td>
                <td><b>{{ row.term }}</b></td>
                <td>{{ row.coursewareScore }}</td>
                <td>{{ row.trainingScore }}</td>
                <td>{{ row.assignmentScore }}</td>
                <td>{{ row.examScore }}</td>
                <td><strong class="admin-semester-score-total">{{ row.totalScore }}</strong></td>
              </tr>
            </tbody>
          </table>
        </div>
        <footer class="admin-semester-score-footer">
          <p>共 <b>{{ totalCount }}</b> 条记录</p>
          <el-pagination v-model:current-page="page" :page-size="pageSize" :total="totalCount" layout="prev, pager, next" background />
        </footer>
      </section>
    </section>

    <section v-else class="admin-semester-score-offline-page">
      <el-breadcrumb class="admin-semester-score-breadcrumb" separator="/">
        <el-breadcrumb-item>成绩统计</el-breadcrumb-item>
        <el-breadcrumb-item>
          <button type="button" @click="backToList">综合成绩</button>
        </el-breadcrumb-item>
        <el-breadcrumb-item>线下成绩管理</el-breadcrumb-item>
      </el-breadcrumb>

      <el-button class="admin-semester-score-upload" @click="openImportDialog">
        <el-icon><UploadFilled /></el-icon>
        上传线下考试成绩
      </el-button>

      <section class="admin-semester-score-offline-board">
        <table class="admin-semester-score-offline-table">
          <thead>
            <tr>
              <th>序号</th>
              <th>考试名称</th>
              <th>考试起止时间</th>
              <th>所属学年学期</th>
              <th>人数</th>
              <th>上传人</th>
              <th>上传时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in offlineExams" :key="item.name">
              <td>{{ index + 1 }}</td>
              <td><strong>{{ item.name }}</strong></td>
              <td>{{ item.timeRange }}</td>
              <td>{{ item.term }}</td>
              <td>{{ item.count }}</td>
              <td>{{ item.uploader }}</td>
              <td>{{ item.uploadedAt }}</td>
              <td><el-button class="admin-semester-score-edit-link" @click="openEditDialog(item)">编辑成绩</el-button></td>
            </tr>
          </tbody>
        </table>
      </section>

      <footer class="admin-semester-score-offline-footer">
        <p>共 <b>56</b> 条记录</p>
        <el-pagination v-model:current-page="offlinePage" :page-size="6" :total="56" layout="prev, pager, next" background />
      </footer>
    </section>

    <el-dialog v-model="detailVisible" class="admin-semester-score-detail-dialog" width="800px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-semester-score-dialog-head">
          <strong>{{ detailTitle }}</strong>
          <button type="button" @click="detailVisible = false"><el-icon><Close /></el-icon></button>
        </div>
      </template>
      <section v-if="currentScore" class="admin-semester-score-detail">
        <div class="admin-semester-score-detail-score">
          <span>综合成绩：</span>
          <strong>{{ currentScore.totalScore }}</strong>
          <i></i>
          <p>计算公式：课件学习×20%＋实训练习×35%＋课程作业×15%＋考试×30%</p>
        </div>
        <table>
          <thead>
            <tr>
              <th>序号</th>
              <th>成绩类型</th>
              <th>成绩权重</th>
              <th>成绩内容</th>
              <th>成绩值</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in detailRows" :key="item.content">
              <td>{{ index + 1 }}</td>
              <td>{{ item.type }}</td>
              <td><b>{{ item.weight }}%</b></td>
              <td>{{ item.content }}</td>
              <td><strong>{{ item.score }}</strong></td>
            </tr>
          </tbody>
        </table>
      </section>
    </el-dialog>

    <el-dialog v-model="importVisible" class="admin-semester-score-import-dialog" width="560px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-semester-score-dialog-head">
          <strong>导入线下考试成绩</strong>
          <button type="button" @click="importVisible = false"><el-icon><Close /></el-icon></button>
        </div>
      </template>
      <section class="admin-semester-score-import-form">
        <label>
          <span>考试名称 <b>*</b></span>
          <el-input v-model="importForm.name" placeholder="请输入考试名称" maxlength="20" />
          <em>最多输入20个字</em>
        </label>
        <label>
          <span>考试起止时间 <b>*</b></span>
          <div class="admin-semester-score-date-row">
            <el-input v-model="importForm.startTime" :suffix-icon="Calendar" placeholder="开始时间" />
            <i>至</i>
            <el-input v-model="importForm.endTime" :suffix-icon="Calendar" placeholder="结束时间" />
          </div>
        </label>
        <label>
          <span>所属学年学期 <b>*</b></span>
          <el-select v-model="importForm.term">
            <el-option v-for="item in termOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </label>
        <div class="admin-semester-score-upload-box">
          <el-icon><UploadFilled /></el-icon>
          <strong>点击上传或拖拽文件到此处</strong>
          <span>仅支持 .xlsx 格式，文件大小不超过10MB</span>
        </div>
        <div class="admin-semester-score-template-row">
          <button type="button">下载导入模板</button>
          <span>请按模板格式填写成绩数据后上传</span>
        </div>
      </section>
      <template #footer>
        <div class="admin-semester-score-dialog-footer">
          <el-button @click="importVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmImport">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" class="admin-semester-score-edit-dialog" width="724px" :show-close="false" append-to-body>
      <template #header>
        <div class="admin-semester-score-dialog-head">
          <strong>{{ editingExam?.name || '编辑成绩' }}-编辑成绩</strong>
          <button type="button" @click="editVisible = false"><el-icon><Close /></el-icon></button>
        </div>
      </template>
      <section class="admin-semester-score-edit-body">
        <div class="admin-semester-score-tip">
          <el-icon><InfoFilled /></el-icon>
          在成绩文本框中直接修改分数，修改完成后点击“确定”保存。
        </div>
        <table>
          <thead>
            <tr>
              <th>序号</th>
              <th>学号</th>
              <th>姓名</th>
              <th>成绩</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, index) in editableScores" :key="row.studentNo">
              <td>{{ index + 1 }}</td>
              <td>{{ row.studentNo }}</td>
              <td><strong>{{ row.studentName }}</strong></td>
              <td><el-input v-model="row.score" /></td>
            </tr>
          </tbody>
        </table>
        <footer>
          <p>共 <b>128</b> 条记录</p>
          <el-pagination v-model:current-page="editPage" :page-size="8" :total="128" layout="prev, pager, next" background />
        </footer>
      </section>
      <template #footer>
        <div class="admin-semester-score-dialog-footer">
          <el-button @click="editVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEditScores">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Calendar, Close, InfoFilled, Search, UploadFilled } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';

interface SemesterScoreRow {
  studentName: string;
  studentNo: string;
  className: string;
  term: string;
  coursewareScore: number;
  trainingScore: number;
  assignmentScore: number;
  examScore: number;
  totalScore: number;
}

interface OfflineExam {
  name: string;
  timeRange: string;
  term: string;
  count: number;
  uploader: string;
  uploadedAt: string;
}

interface EditableScore {
  studentName: string;
  studentNo: string;
  score: string;
}

const page = ref(1);
const pageSize = 5;
const totalCount = 128;
const viewMode = ref<'list' | 'offline'>('list');
const detailVisible = ref(false);
const importVisible = ref(false);
const editVisible = ref(false);
const offlinePage = ref(1);
const editPage = ref(1);
const currentScore = ref<SemesterScoreRow | null>(null);
const editingExam = ref<OfflineExam | null>(null);
const draft = reactive({ term: '2024-2025年上学期', className: '', studentName: '', studentNo: '' });
const applied = ref({ ...draft });
const importForm = reactive({ name: '', startTime: '', endTime: '', term: '2024-2025年上学期' });

const weightLegend = [
  { name: '课件学习', value: 20, color: '#3b82f6' },
  { name: '实训练习', value: 35, color: '#10b981' },
  { name: '课程作业', value: 15, color: '#f59e0b' },
  { name: '考试', value: 30, color: '#ef4444' }
];

const scores = ref<SemesterScoreRow[]>([
  { studentName: '王成祥', studentNo: 'S20240301', className: '城轨运营2401班', term: '2024-2025年上学期', coursewareScore: 88, trainingScore: 92, assignmentScore: 85, examScore: 90, totalScore: 89.8 },
  { studentName: '陈松', studentNo: 'S20240306', className: '城轨运营2401班', term: '2024-2025年上学期', coursewareScore: 75, trainingScore: 78, assignmentScore: 80, examScore: 72, totalScore: 75.9 },
  { studentName: '赵立申', studentNo: 'S20240322', className: '城轨运营2402班', term: '2024-2025年上学期', coursewareScore: 90, trainingScore: 85, assignmentScore: 88, examScore: 82, totalScore: 85.6 },
  { studentName: '周莹莹', studentNo: 'S20240501', className: '城轨信号2401班', term: '2024-2025年上学期', coursewareScore: 82, trainingScore: 76, assignmentScore: 70, examScore: 68, totalScore: 73.9 },
  { studentName: '吴石磊', studentNo: 'S20240610', className: '城轨车辆2401班', term: '2024-2025年上学期', coursewareScore: 95, trainingScore: 91, assignmentScore: 93, examScore: 88, totalScore: 91.2 }
]);

const offlineExams = ref<OfflineExam[]>([
  { name: '城市轨道交通行车组织期末考试', timeRange: '2025-01-15 09:00 ~ 2025-01-15 11:00', term: '2024-2025年 上学期', count: 128, uploader: '张建国', uploadedAt: '2025-01-16 14:30:22' },
  { name: '城市轨道交通信号系统期中考试', timeRange: '2025-01-08 14:00 ~ 2025-01-08 16:00', term: '2024-2025年 上学期', count: 96, uploader: '李明辉', uploadedAt: '2025-01-09 10:15:08' },
  { name: '城轨车辆构造与检修期末考试', timeRange: '2025-01-20 09:00 ~ 2025-01-20 11:30', term: '2024-2025年 上学期', count: 112, uploader: '王思远', uploadedAt: '2025-01-21 08:42:35' },
  { name: '城市轨道交通运营安全期中考试', timeRange: '2024-12-10 14:00 ~ 2024-12-10 16:00', term: '2024-2025年 上学期', count: 85, uploader: '赵志强', uploadedAt: '2024-12-11 16:20:47' },
  { name: '城轨供电系统运行与维护期末考试', timeRange: '2025-01-18 09:00 ~ 2025-01-18 11:00', term: '2024-2025年 上学期', count: 76, uploader: '张建国', uploadedAt: '2025-01-19 11:55:13' },
  { name: '城市轨道交通法规与标准期末考试', timeRange: '2025-01-22 14:00 ~ 2025-01-22 16:00', term: '2024-2025年 上学期', count: 104, uploader: '李明辉', uploadedAt: '2025-01-23 09:30:51' }
]);

const editableScores = ref<EditableScore[]>([
  { studentName: '王成祥', studentNo: 'S20240301', score: '89.8' },
  { studentName: '陈松', studentNo: 'S20240456', score: '75.9' },
  { studentName: '赵立申', studentNo: 'S20240322', score: '85.6' },
  { studentName: '周莹莹', studentNo: 'S20240501', score: '73.9' },
  { studentName: '吴石磊', studentNo: 'S20240610', score: '91.2' },
  { studentName: '刘思雨', studentNo: 'S20240721', score: '82.4' },
  { studentName: '张浩然', studentNo: 'S20240809', score: '78.5' },
  { studentName: '李雨桐', studentNo: 'S20241033', score: '88.0' }
]);

const termOptions = computed(() => Array.from(new Set(scores.value.map((item) => item.term))));
const classOptions = computed(() => Array.from(new Set(scores.value.map((item) => item.className))));
const filteredScores = computed(() => scores.value.filter((item) => (!applied.value.term || item.term === applied.value.term) && (!applied.value.className || item.className === applied.value.className) && (!applied.value.studentName || item.studentName.includes(applied.value.studentName)) && (!applied.value.studentNo || item.studentNo.includes(applied.value.studentNo))));
const pagedScores = computed(() => filteredScores.value.slice(0, pageSize));
const detailTitle = computed(() => currentScore.value ? `${currentScore.value.studentName}（${currentScore.value.studentNo}） 2024-2025年 上学期综合成绩详情` : '综合成绩详情');
const detailRows = computed(() => [
  { type: '课件学习', weight: 20, content: '城市轨道交通行车组织基础理论', score: 20 },
  { type: '课件学习', weight: 20, content: '城市轨道交通信号系统原理', score: 35 },
  { type: '课件学习', weight: 20, content: '城轨车辆构造与检修技术', score: 30 },
  { type: '实训练习', weight: 35, content: '行车组织方案实训练习', score: currentScore.value?.trainingScore || 92 },
  { type: '课程作业', weight: 15, content: '行车组织方案设计作业', score: currentScore.value?.assignmentScore || 83 },
  { type: '考试', weight: 30, content: '城市轨道交通运营安全期末考试', score: currentScore.value?.examScore || 90 }
]);

/** 应用综合成绩查询条件。 */
function applyFilters() {
  applied.value = { ...draft };
  page.value = 1;
}

/** 重置综合成绩查询条件。 */
function resetFilters() {
  Object.assign(draft, { term: '2024-2025年上学期', className: '', studentName: '', studentNo: '' });
  applyFilters();
}

/** 打开综合成绩详情弹窗。 */
function openDetail(row: SemesterScoreRow) {
  currentScore.value = row;
  detailVisible.value = true;
}

/** 进入线下成绩管理二级页面。 */
function openOfflinePage() {
  viewMode.value = 'offline';
}

/** 返回综合成绩列表。 */
function backToList() {
  viewMode.value = 'list';
}

/** 打开导入线下考试成绩弹窗。 */
function openImportDialog() {
  importVisible.value = true;
}

/** 打开编辑成绩弹窗。 */
function openEditDialog(item: OfflineExam) {
  editingExam.value = item;
  editVisible.value = true;
}

/** 确认导入线下考试成绩。 */
function confirmImport() {
  importVisible.value = false;
  ElMessage.success('线下考试成绩已导入');
}

/** 保存编辑后的成绩。 */
function saveEditScores() {
  editVisible.value = false;
  ElMessage.success('成绩已保存');
}
</script>
