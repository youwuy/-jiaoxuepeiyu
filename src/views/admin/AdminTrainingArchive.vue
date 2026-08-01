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

      <section class="admin-training-archive-board">
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
          <el-pagination v-model:current-page="page" :page-size="pageSize" :total="filteredArchives.length" layout="prev, pager, next" background />
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
          <button type="button" class="admin-training-archive-video" @click="openVideoPreview">
            <span class="play"></span>
            <span class="track"><b></b></span>
            <em class="time start">04:28</em>
            <em class="time end">12:48</em>
            <span class="controls">
              <i class="pause"></i>
              <i class="volume"></i>
              <i class="bar"></i>
              <i class="screen"></i>
            </span>
          </button>
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
          <em class="time start">04:28</em>
          <em class="time end">12:48</em>
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
import { computed, reactive, ref } from 'vue';
import { Back, Close, Search } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';

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
const viewMode = ref<'list' | 'detail'>('list');
const activeArchive = ref<TrainingArchiveRow | null>(null);
const videoVisible = ref(false);
const previewTitle = ref('实训操作视频');
const draft = reactive({ className: '', studentNo: '', studentName: '' });
const applied = ref({ ...draft });

const archives = ref<TrainingArchiveRow[]>([
  { id: 1, className: '城轨运营2401班', studentNo: 'S20240301', studentName: '王成祥', trainingName: 'CBTC信号系统操作实训', detailTitle: 'CBTC信号系统操作实训（乘客角色）', trainingMode: '多人实训', roleName: '乘客', submittedAt: '2025-01-15 09:00', submitType: '正常提交', durationSeconds: 12, personalScore: '8', teamScore: '21' },
  { id: 2, className: '城轨运营2401班', studentNo: 'S20240456', studentName: '陈松', trainingName: '轨道交通信号设备维护', detailTitle: '轨道交通信号设备维护（站台员角色）', trainingMode: '多人实训', roleName: '站台员', submittedAt: '2025-01-10 14:13', submitType: '异常退出', durationSeconds: 90, personalScore: '9', teamScore: '32' },
  { id: 3, className: '城轨运营2402班', studentNo: 'S20240322', studentName: '赵立申', trainingName: '列车自动控制系统调试', detailTitle: '列车自动控制系统调试（司机角色）', trainingMode: '多人实训', roleName: '司机', submittedAt: '2025-01-08 08:30', submitType: '正常提交', durationSeconds: 18, personalScore: '6', teamScore: '19' },
  { id: 4, className: '城轨信号2401班', studentNo: 'S20240501', studentName: '周莹莹', trainingName: '车站联锁设备故障排查', detailTitle: '车站联锁设备故障排查（运营人员角色）', trainingMode: '多人实训', roleName: '运营人员', submittedAt: '2025-01-05 13:00', submitType: '房间解散', durationSeconds: 15, personalScore: '3', teamScore: '28' },
  { id: 5, className: '城轨车辆2401班', studentNo: 'S20240610', studentName: '吴石磊', trainingName: '轨道电路检测与维修', detailTitle: '轨道电路检测与维修', trainingMode: '单人实训', roleName: '-', submittedAt: '2025-01-03 10:09', submitType: '正常提交', durationSeconds: 60, personalScore: '4', teamScore: '-' }
]);

const archiveSteps: ArchiveStep[] = [
  { name: '穿戴安全防护用品', expected: '按十字对角顺序初步护入所有', actual: '按十字对角顺序初步护入所有', score: 1, seconds: 45 },
  { name: '检查工具准备情况', expected: '按十字对角顺序初步护入所有', actual: '按十字对角顺序初步护入所有', score: 1, seconds: 32 },
  { name: '确认信号机断电状态', expected: '按十字对角顺序初步护入所有', actual: '按十字对角顺序初步护入所有', score: 0, seconds: 18 },
  { name: '拆卸信号机外壳', expected: '按十字对角顺序初步护入所有', actual: '按十字对角顺序初步护入所有', score: 1, seconds: 56 },
  { name: '检查内部接线端子', expected: '按十字对角顺序初步护入所有', actual: '按十字对角顺序初步护入所有', score: 1, seconds: 78 },
  { name: '清洁透镜组表面', expected: '按十字对角顺序初步护入所有', actual: '按十字对角顺序初步护入所有', score: 0, seconds: 22 },
  { name: '检测灯泡工作状态', expected: '按十字对角顺序初步护入所有', actual: '按十字对角顺序初步护入所有', score: 1, seconds: 41 },
  { name: '测量电路电压参数', expected: '按十字对角顺序初步护入所有', actual: '按十字对角顺序初步护入所有', score: 1, seconds: 63 },
  { name: '调整灯丝转换继电器', expected: '按十字对角顺序初步护入所有', actual: '按十字对角顺序初步护入所有', score: 0, seconds: 15 }
];

const classOptions = computed(() => Array.from(new Set(archives.value.map((item) => item.className))));
const filteredArchives = computed(() => archives.value.filter((item) => (!applied.value.className || item.className === applied.value.className) && (!applied.value.studentNo || item.studentNo.includes(applied.value.studentNo)) && (!applied.value.studentName || item.studentName.includes(applied.value.studentName))));
const pagedArchives = computed(() => filteredArchives.value.slice((page.value - 1) * pageSize, page.value * pageSize));

function applyFilters() {
  applied.value = { ...draft };
  page.value = 1;
}

function resetFilters() {
  Object.assign(draft, { className: '', studentNo: '', studentName: '' });
  applyFilters();
}

function openDetail(row: TrainingArchiveRow) {
  activeArchive.value = row;
  viewMode.value = 'detail';
}

function backToList() {
  viewMode.value = 'list';
}

function openVideoPreview() {
  previewTitle.value = '实训操作视频';
  videoVisible.value = true;
}

function openStepVideo(step: ArchiveStep) {
  previewTitle.value = `${step.name} - 操作视频`;
  videoVisible.value = true;
}
</script>
