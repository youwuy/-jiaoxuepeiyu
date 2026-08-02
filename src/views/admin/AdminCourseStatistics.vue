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

      <section class="admin-course-stats-summary">
        <article>
          <span>课程人数</span>
          <strong>{{ courseStats.studentCount }}</strong>
        </article>
        <article>
          <span>已完成</span>
          <strong>{{ courseStats.completedCount }}</strong>
        </article>
        <article>
          <span>学习中</span>
          <strong>{{ courseStats.studyingCount }}</strong>
        </article>
        <article>
          <span>未开始</span>
          <strong>{{ courseStats.notStartedCount }}</strong>
        </article>
        <article>
          <span>待批改</span>
          <strong>{{ courseStats.pendingReviewCount }}</strong>
        </article>
        <article>
          <span>平均分</span>
          <strong>{{ formatScore(courseStats.averageScore) }}</strong>
        </article>
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
import { computed, onMounted, reactive, ref, watch } from 'vue';
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
import { fetchAdminCourseStatistics, type AdminCourseStatistics } from '../../api/admin-course';
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
const courseStats = ref<AdminCourseStatistics>({
  courseId: courseId.value,
  studentCount: 0,
  completedCount: 0,
  studyingCount: 0,
  notStartedCount: 0,
  pendingReviewCount: 0,
  averageScore: 0
});

const students = ref<StudentScoreRow[]>([]);

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

async function loadCourseStatistics() {
  try {
    courseStats.value = await fetchAdminCourseStatistics(courseId.value);
  } catch (error) {
    ElMessage.warning(error instanceof Error ? error.message : '课程统计接口暂不可用');
  }
}

onMounted(() => {
  void loadCourseStatistics();
});
</script>
