<template>
  <AdminShell activeKey="admin-courses">
    <section class="admin-course-stats-page">
      <header class="admin-course-stats-topbar">
        <div class="admin-course-stats-left">
          <el-button class="admin-course-stats-back" :icon="ArrowLeft" @click="goBack" />
          <el-breadcrumb class="admin-course-stats-breadcrumb" separator="/">
            <el-breadcrumb-item>教学实训</el-breadcrumb-item>
            <el-breadcrumb-item>教学课程</el-breadcrumb-item>
            <el-breadcrumb-item>成绩统计</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>{{ courseTitle }} - 成绩统计</h1>
        <span class="admin-course-stats-spacer"></span>
      </header>

      <section class="admin-course-stats-filter-card">
        <div class="admin-course-stats-filter-row">
          <label class="admin-course-stats-field">
            <span>学员姓名</span>
            <el-input v-model="filters.studentName" placeholder="请输入学员姓名" clearable />
          </label>
          <label class="admin-course-stats-field">
            <span>学员学号</span>
            <el-input v-model="filters.studentNo" placeholder="请输入学员学号" clearable />
          </label>
          <label class="admin-course-stats-field">
            <span>所属班级</span>
            <el-select v-model="filters.className" placeholder="请选择所属班级" clearable>
              <el-option v-for="item in classOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </label>
          <div class="admin-course-stats-buttons">
            <el-button type="primary" class="admin-course-stats-query" @click="applyFilters">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button class="admin-course-stats-reset" @click="resetFilters">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </div>
        </div>
      </section>

      <section class="admin-course-stats-table-card">
        <header class="admin-course-stats-table-head">
          <div>
            <el-icon><Tickets /></el-icon>
            <strong>成绩列表</strong>
          </div>
          <el-button class="admin-course-stats-export" @click="exportData">
            <el-icon><Download /></el-icon>
            导出数据
          </el-button>
        </header>

        <div class="admin-course-stats-table-scroll">
          <table class="admin-course-stats-table">
            <thead>
              <tr>
                <th>序号</th>
                <th>学员姓名</th>
                <th>学员学号</th>
                <th>所属班级</th>
                <th>学习进度</th>
                <th>进度得分</th>
                <th>作业提交数量</th>
                <th>作业合计得分</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(student, index) in pagedStudents" :key="student.studentNo">
                <td>{{ (page - 1) * pageSize + index + 1 }}</td>
                <td class="admin-course-stats-name">{{ student.name }}</td>
                <td>{{ student.studentNo }}</td>
                <td>{{ student.className }}</td>
                <td class="admin-course-stats-progress">{{ student.progress }}%</td>
                <td>{{ formatScore(student.progressScore) }}</td>
                <td>{{ student.assignmentCount }}</td>
                <td>{{ student.assignmentScore }}</td>
                <td>
                  <el-button link type="primary" class="admin-course-stats-link" @click="showDetail(student)">
                    查看详情
                  </el-button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <footer class="admin-course-stats-pagination">
          <p>显示 {{ pageStart }} 到 {{ pageEnd }} 条，共 {{ filteredStudents.length }} 条记录</p>
          <div class="admin-course-stats-pager">
            <el-button :icon="DArrowLeft" :disabled="page === 1" @click="page = 1" />
            <el-button :icon="ArrowLeft" :disabled="page === 1" @click="page = Math.max(1, page - 1)" />
            <el-pagination
              v-model:current-page="page"
              :page-size="pageSize"
              :total="filteredStudents.length"
              layout="pager"
              background
            />
            <el-button :icon="ArrowRight" :disabled="page === pageCount" @click="page = Math.min(pageCount, page + 1)" />
            <el-button :icon="DArrowRight" :disabled="page === pageCount" @click="page = pageCount" />
            <span>每页</span>
            <el-select v-model="pageSize" class="admin-course-stats-size">
              <el-option :label="10" :value="10" />
              <el-option :label="20" :value="20" />
            </el-select>
            <span>条</span>
          </div>
        </footer>
      </section>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  ArrowLeft,
  ArrowRight,
  DArrowLeft,
  DArrowRight,
  Download,
  Refresh,
  Search,
  Tickets
} from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import { mockAdminCourses } from '../../features/admin/courses';

interface StudentScoreRow {
  name: string;
  studentNo: string;
  className: string;
  progress: number;
  progressScore: number;
  assignmentCount: number;
  assignmentScore: number;
}

const route = useRoute();
const router = useRouter();
const courseId = computed(() => Number(route.params.id));
const currentCourse = computed(() => mockAdminCourses.find((item) => item.courseId === courseId.value));
const courseTitle = computed(() => (route.query.title as string) || currentCourse.value?.courseName || '城市轨道交通信号系统原理');

const page = ref(1);
const pageSize = ref(10);
const filters = reactive({
  studentName: '',
  studentNo: '',
  className: ''
});
const appliedFilters = ref({ ...filters });

const students = ref<StudentScoreRow[]>([
  { name: '张明远', studentNo: '2024CGXH001', className: '信号1班', progress: 92, progressScore: 9.2, assignmentCount: 8, assignmentScore: 75 },
  { name: '李晓婷', studentNo: '2024CGXH002', className: '信号1班', progress: 85, progressScore: 8.5, assignmentCount: 7, assignmentScore: 68 },
  { name: '王志强', studentNo: '2024CGXH003', className: '信号2班', progress: 78, progressScore: 7.8, assignmentCount: 6, assignmentScore: 54 },
  { name: '赵雨涵', studentNo: '2024CGXH004', className: '信号1班', progress: 96, progressScore: 9.6, assignmentCount: 8, assignmentScore: 79 },
  { name: '陈浩然', studentNo: '2024CGXH005', className: '信号2班', progress: 45, progressScore: 4.5, assignmentCount: 3, assignmentScore: 0 },
  { name: '刘思琪', studentNo: '2024CGXH006', className: '信号1班', progress: 88, progressScore: 8.8, assignmentCount: 7, assignmentScore: 0 },
  { name: '周子轩', studentNo: '2024CGXH007', className: '信号2班', progress: 62, progressScore: 6.2, assignmentCount: 5, assignmentScore: 39 },
  { name: '吴嘉豪', studentNo: '2024CGXH008', className: '信号1班', progress: 100, progressScore: 10, assignmentCount: 8, assignmentScore: 80 },
  { name: '孙悦然', studentNo: '2024CGXH009', className: '信号3班', progress: 70, progressScore: 7, assignmentCount: 5, assignmentScore: 43 },
  { name: '黄俊杰', studentNo: '2024CGXH010', className: '信号3班', progress: 35, progressScore: 3.5, assignmentCount: 2, assignmentScore: 12 },
  { name: '马欣怡', studentNo: '2024CGXH011', className: '信号1班', progress: 82, progressScore: 8.2, assignmentCount: 7, assignmentScore: 66 },
  { name: '朱博文', studentNo: '2024CGXH012', className: '信号2班', progress: 58, progressScore: 5.8, assignmentCount: 4, assignmentScore: 31 }
]);

const classOptions = computed(() => Array.from(new Set(students.value.map((item) => item.className))));
const pageCount = computed(() => Math.max(1, Math.ceil(filteredStudents.value.length / pageSize.value)));
const filteredStudents = computed(() => {
  const name = normalize(appliedFilters.value.studentName);
  const no = normalize(appliedFilters.value.studentNo);
  return students.value.filter((item) => {
    const matchesName = !name || normalize(item.name).includes(name);
    const matchesNo = !no || normalize(item.studentNo).includes(no);
    const matchesClass = !appliedFilters.value.className || item.className === appliedFilters.value.className;
    return matchesName && matchesNo && matchesClass;
  });
});
const pagedStudents = computed(() => {
  const start = (page.value - 1) * pageSize.value;
  return filteredStudents.value.slice(start, start + pageSize.value);
});
const pageStart = computed(() => (filteredStudents.value.length === 0 ? 0 : (page.value - 1) * pageSize.value + 1));
const pageEnd = computed(() => Math.min(page.value * pageSize.value, filteredStudents.value.length));

watch(pageSize, () => {
  page.value = 1;
});

watch(filteredStudents, () => {
  if (page.value > pageCount.value) {
    page.value = pageCount.value;
  }
});

function normalize(value: string) {
  return value.replace(/\s+/g, '').toLowerCase();
}

function formatScore(value: number) {
  return Number.isInteger(value) ? String(value) : value.toFixed(1);
}

function applyFilters() {
  appliedFilters.value = { ...filters };
  page.value = 1;
}

function resetFilters() {
  filters.studentName = '';
  filters.studentNo = '';
  filters.className = '';
  appliedFilters.value = { ...filters };
  page.value = 1;
}

function goBack() {
  router.push('/admin/courses');
}

function exportData() {
  ElMessage.success('成绩数据导出任务已创建');
}

function showDetail(student: StudentScoreRow) {
  ElMessage.info(`${student.name} 的成绩详情待接入接口`);
}
</script>
