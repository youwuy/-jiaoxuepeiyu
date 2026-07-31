<template>
  <AdminShell activeKey="admin-courses">
    <section class="admin-course-reviews-page">
      <header class="admin-course-reviews-topbar">
        <div class="admin-course-reviews-left">
          <el-button class="admin-course-reviews-back" :icon="ArrowLeft" @click="goBack" />
          <el-breadcrumb class="admin-course-reviews-breadcrumb" separator="/">
            <el-breadcrumb-item>教学实训</el-breadcrumb-item>
            <el-breadcrumb-item>教学课程</el-breadcrumb-item>
            <el-breadcrumb-item>批改作业</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>{{ courseTitle }} - 批改作业</h1>
        <span></span>
      </header>

      <section class="admin-course-reviews-filter-card">
        <div class="admin-course-reviews-filter-row">
          <label class="admin-course-reviews-field">
            <span>学员姓名</span>
            <el-input v-model="filters.studentName" placeholder="请输入学员姓名" clearable />
          </label>
          <label class="admin-course-reviews-field">
            <span>学员学号</span>
            <el-input v-model="filters.studentNo" placeholder="请输入学员学号" clearable />
          </label>
          <label class="admin-course-reviews-field">
            <span>所属班级</span>
            <el-select v-model="filters.className" placeholder="请选择所属班级" clearable>
              <el-option v-for="item in classOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </label>
          <label class="admin-course-reviews-field assignment">
            <span>作业名称</span>
            <el-input v-model="filters.assignmentName" placeholder="请输入作业名称" clearable />
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
            <strong>作业列表</strong>
          </div>
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

        <div class="admin-course-reviews-table-scroll">
          <table class="admin-course-reviews-table">
            <thead>
              <tr>
                <th>序号</th>
                <th>学员姓名</th>
                <th>学号</th>
                <th>所属班级</th>
                <th>作业名称</th>
                <th>是否提交</th>
                <th>最后一次提交时间</th>
                <th>是否批阅</th>
                <th>作业得分</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, index) in pagedRows" :key="item.id">
                <td>{{ (page - 1) * pageSize + index + 1 }}</td>
                <td class="admin-course-reviews-name">{{ item.studentName }}</td>
                <td>{{ item.studentNo }}</td>
                <td>{{ item.className }}</td>
                <td>{{ item.assignmentName }}</td>
                <td>
                  <span class="admin-course-reviews-tag" :class="item.submitted ? 'submitted' : 'not-submitted'">
                    {{ item.submitted ? '已提交' : '未提交' }}
                  </span>
                </td>
                <td>{{ item.submittedAt || '-' }}</td>
                <td>
                  <span v-if="item.submitted" class="admin-course-reviews-tag" :class="item.reviewed ? 'reviewed' : 'pending'">
                    {{ item.reviewed ? '已批阅' : '未批阅' }}
                  </span>
                  <span v-else>-</span>
                </td>
                <td>{{ item.score ?? '-' }}</td>
                <td>
                  <span v-if="!item.submitted" class="admin-course-reviews-none">-</span>
                  <el-button
                    v-else-if="item.reviewed"
                    class="admin-course-reviews-action view"
                    @click="openReview(item)"
                  >
                    <el-icon><View /></el-icon>
                    查看批阅
                  </el-button>
                  <el-button v-else class="admin-course-reviews-action edit" @click="openReview(item)">
                    <el-icon><EditPen /></el-icon>
                    批阅
                  </el-button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <footer class="admin-course-reviews-pagination">
          <p>显示 {{ pageStart }} 到 {{ pageEnd }} 条，共 {{ filteredRows.length }} 条记录</p>
          <div class="admin-course-reviews-pager">
            <el-button :icon="DArrowLeft" :disabled="page === 1" @click="page = 1" />
            <el-button :icon="ArrowLeft" :disabled="page === 1" @click="page = Math.max(1, page - 1)" />
            <el-pagination
              v-model:current-page="page"
              :page-size="pageSize"
              :total="filteredRows.length"
              layout="pager"
              background
            />
            <el-button :icon="ArrowRight" :disabled="page === pageCount" @click="page = Math.min(pageCount, page + 1)" />
            <el-button :icon="DArrowRight" :disabled="page === pageCount" @click="page = pageCount" />
            <span>每页</span>
            <el-select v-model="pageSize" class="admin-course-reviews-size">
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
import { mockAdminCourses } from '../../features/admin/courses';

type ReviewTabKey = 'all' | 'pending' | 'reviewed' | 'notSubmitted';

interface AssignmentReviewRow {
  id: number;
  studentName: string;
  studentNo: string;
  className: string;
  assignmentName: string;
  submitted: boolean;
  submittedAt?: string;
  reviewed: boolean;
  score?: number;
}

const route = useRoute();
const router = useRouter();
const courseId = computed(() => Number(route.params.id));
const currentCourse = computed(() => mockAdminCourses.find((item) => item.courseId === courseId.value));
const courseTitle = computed(() => (route.query.title as string) || currentCourse.value?.courseName || '城市轨道交通信号系统原理');

const page = ref(1);
const pageSize = ref(10);
const activeTab = ref<ReviewTabKey>('all');
const filters = reactive({
  studentName: '',
  studentNo: '',
  className: '',
  assignmentName: ''
});
const appliedFilters = ref({ ...filters });

const rows = ref<AssignmentReviewRow[]>([
  { id: 1, studentName: '张明远', studentNo: '2024CGXH001', className: '信号1班', assignmentName: '1.1节作业', submitted: true, submittedAt: '2025-04-10 14:32', reviewed: true, score: 92 },
  { id: 2, studentName: '李晓婷', studentNo: '2024CGXH002', className: '信号1班', assignmentName: '1.3节作业', submitted: true, submittedAt: '2025-04-11 09:15', reviewed: false, score: 92 },
  { id: 3, studentName: '王志强', studentNo: '2024CGXH003', className: '信号2班', assignmentName: '1.1节作业', submitted: false, reviewed: false },
  { id: 4, studentName: '赵雨涵', studentNo: '2024CGXH004', className: '信号1班', assignmentName: '1.3节作业', submitted: true, submittedAt: '2025-04-09 16:48', reviewed: true, score: 88 },
  { id: 5, studentName: '陈浩然', studentNo: '2024CGXH005', className: '信号2班', assignmentName: '1.1节作业', submitted: true, submittedAt: '2025-04-12 10:20', reviewed: false, score: 92 },
  { id: 6, studentName: '刘思琪', studentNo: '2024CGXH006', className: '信号1班', assignmentName: '1.3节作业', submitted: false, reviewed: false },
  { id: 7, studentName: '周子轩', studentNo: '2024CGXH007', className: '信号2班', assignmentName: '1.1节作业', submitted: true, submittedAt: '2025-04-08 11:05', reviewed: true, score: 75 },
  { id: 8, studentName: '吴嘉豪', studentNo: '2024CGXH008', className: '信号1班', assignmentName: '1.3节作业', submitted: true, submittedAt: '2025-04-13 08:42', reviewed: false, score: 92 },
  { id: 9, studentName: '孙悦然', studentNo: '2024CGXH009', className: '信号3班', assignmentName: '1.1节作业', submitted: false, reviewed: false },
  { id: 10, studentName: '黄俊杰', studentNo: '2024CGXH010', className: '信号3班', assignmentName: '1.3节作业', submitted: true, submittedAt: '2025-04-07 15:30', reviewed: true, score: 68 },
  { id: 11, studentName: '马欣怡', studentNo: '2024CGXH011', className: '信号1班', assignmentName: '1.1节作业', submitted: true, submittedAt: '2025-04-12 18:10', reviewed: false, score: 90 },
  { id: 12, studentName: '朱博文', studentNo: '2024CGXH012', className: '信号2班', assignmentName: '1.3节作业', submitted: true, submittedAt: '2025-04-12 19:45', reviewed: true, score: 84 }
]);

const classOptions = computed(() => Array.from(new Set(rows.value.map((item) => item.className))));
const baseFilteredRows = computed(() => {
  const name = normalize(appliedFilters.value.studentName);
  const no = normalize(appliedFilters.value.studentNo);
  const assignment = normalize(appliedFilters.value.assignmentName);
  return rows.value.filter((item) => {
    const matchesName = !name || normalize(item.studentName).includes(name);
    const matchesNo = !no || normalize(item.studentNo).includes(no);
    const matchesClass = !appliedFilters.value.className || item.className === appliedFilters.value.className;
    const matchesAssignment = !assignment || normalize(item.assignmentName).includes(assignment);
    return matchesName && matchesNo && matchesClass && matchesAssignment;
  });
});
const filteredRows = computed(() => {
  if (activeTab.value === 'pending') {
    return baseFilteredRows.value.filter((item) => item.submitted && !item.reviewed);
  }
  if (activeTab.value === 'reviewed') {
    return baseFilteredRows.value.filter((item) => item.submitted && item.reviewed);
  }
  if (activeTab.value === 'notSubmitted') {
    return baseFilteredRows.value.filter((item) => !item.submitted);
  }
  return baseFilteredRows.value;
});
const tabs = computed(() => [
  { key: 'all' as const, label: '全部', count: baseFilteredRows.value.length, tone: 'all' },
  {
    key: 'pending' as const,
    label: '待批阅',
    count: baseFilteredRows.value.filter((item) => item.submitted && !item.reviewed).length,
    tone: 'pending'
  },
  {
    key: 'reviewed' as const,
    label: '已批阅',
    count: baseFilteredRows.value.filter((item) => item.submitted && item.reviewed).length,
    tone: 'reviewed'
  },
  {
    key: 'notSubmitted' as const,
    label: '未提交',
    count: baseFilteredRows.value.filter((item) => !item.submitted).length,
    tone: 'notSubmitted'
  }
]);
const pageCount = computed(() => Math.max(1, Math.ceil(filteredRows.value.length / pageSize.value)));
const pagedRows = computed(() => {
  const start = (page.value - 1) * pageSize.value;
  return filteredRows.value.slice(start, start + pageSize.value);
});
const pageStart = computed(() => (filteredRows.value.length === 0 ? 0 : (page.value - 1) * pageSize.value + 1));
const pageEnd = computed(() => Math.min(page.value * pageSize.value, filteredRows.value.length));

watch(pageSize, () => {
  page.value = 1;
});

watch(filteredRows, () => {
  if (page.value > pageCount.value) {
    page.value = pageCount.value;
  }
});

function normalize(value?: string) {
  return (value || '').replace(/\s+/g, '').toLowerCase();
}

function setTab(tab: ReviewTabKey) {
  activeTab.value = tab;
  page.value = 1;
}

function applyFilters() {
  appliedFilters.value = { ...filters };
  page.value = 1;
}

function resetFilters() {
  filters.studentName = '';
  filters.studentNo = '';
  filters.className = '';
  filters.assignmentName = '';
  appliedFilters.value = { ...filters };
  page.value = 1;
}

function goBack() {
  router.push('/admin/courses');
}

function openReview(row: AssignmentReviewRow) {
  router.push({
    path: `/admin/courses/${courseId.value}/reviews/${row.id}/theory`,
    query: {
      courseTitle: courseTitle.value,
      assignment: row.assignmentName,
      studentName: row.studentName,
      studentNo: row.studentNo,
      className: row.className,
      submittedAt: row.submittedAt || ''
    }
  });
}
</script>
