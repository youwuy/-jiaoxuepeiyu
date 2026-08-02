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
            <el-select v-model="filters.classId" placeholder="请选择所属班级" filterable clearable>
              <el-option v-for="item in classOptions" :key="item.classId" :label="item.className" :value="item.classId" />
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

        <div v-if="loading" class="admin-course-empty">作业列表加载中...</div>
        <div v-else-if="pagedRows.length === 0" class="admin-course-empty">
          <el-empty description="暂无匹配作业" />
        </div>
        <div v-else class="admin-course-reviews-table-scroll">
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
          <p>显示 {{ pageStart }} 到 {{ pageEnd }} 条，共 {{ total }} 条记录</p>
          <div class="admin-course-reviews-pager">
            <el-button :icon="DArrowLeft" :disabled="page === 1" @click="goToPage(1)" />
            <el-button :icon="ArrowLeft" :disabled="page === 1" @click="goToPage(Math.max(1, page - 1))" />
            <el-pagination
              v-model:current-page="page"
              :page-size="pageSize"
              :total="total"
              layout="pager"
              background
              @current-change="loadRows"
            />
            <el-button :icon="ArrowRight" :disabled="page === pageCount" @click="goToPage(Math.min(pageCount, page + 1))" />
            <el-button :icon="DArrowRight" :disabled="page === pageCount" @click="goToPage(pageCount)" />
            <span>每页</span>
            <el-select v-model="pageSize" class="admin-course-reviews-size" @change="handlePageSizeChange">
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
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
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
import {
  fetchAdminAssignmentAttempts,
  fetchAdminClasses,
  type AdminAssignmentAttempt,
  type AdminClassOption
} from '../../api/admin-course';
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
const total = ref(0);
const loading = ref(false);
const activeTab = ref<ReviewTabKey>('all');
const filters = reactive({
  studentName: '',
  studentNo: '',
  classId: undefined as number | undefined,
  assignmentName: ''
});
const appliedFilters = ref({ ...filters });
let requestId = 0;

const classOptions = ref<AdminClassOption[]>([]);
const rows = ref<AssignmentReviewRow[]>([]);

const tabs = computed(() => [
  { key: 'all' as const, label: '全部', count: total.value, tone: 'all' },
  {
    key: 'pending' as const,
    label: '待批阅',
    count: rows.value.filter((item) => item.submitted && !item.reviewed).length,
    tone: 'pending'
  },
  {
    key: 'reviewed' as const,
    label: '已批阅',
    count: rows.value.filter((item) => item.submitted && item.reviewed).length,
    tone: 'reviewed'
  },
  {
    key: 'notSubmitted' as const,
    label: '未提交',
    count: rows.value.filter((item) => !item.submitted).length,
    tone: 'notSubmitted'
  }
]);
const pageCount = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)));
const pagedRows = computed(() => rows.value);
const pageStart = computed(() => (total.value === 0 ? 0 : (page.value - 1) * pageSize.value + 1));
const pageEnd = computed(() => Math.min(page.value * pageSize.value, total.value));

function formatDateTime(value?: string) {
  if (!value) {
    return undefined;
  }

  const normalized = value.includes('T') ? value.replace('T', ' ') : value;
  return normalized.slice(0, 16);
}

function statusForTab(tab: ReviewTabKey) {
  if (tab === 'pending') {
    return 'SUBMITTED';
  }
  if (tab === 'reviewed') {
    return 'REVIEWED';
  }
  return undefined;
}

function mapAttemptRow(item: AdminAssignmentAttempt): AssignmentReviewRow {
  const reviewed = (item.status || '').toUpperCase() === 'REVIEWED';
  return {
    id: item.attemptId,
    studentName: item.studentName || '-',
    studentNo: item.studentNo || '-',
    className: item.className || '-',
    assignmentName: item.assignmentTitle || '-',
    submitted: true,
    submittedAt: formatDateTime(item.submittedAt),
    reviewed,
    score: item.score
  };
}

function setTab(tab: ReviewTabKey) {
  activeTab.value = tab;
  page.value = 1;
  void loadRows();
}

function applyFilters() {
  appliedFilters.value = { ...filters };
  page.value = 1;
  void loadRows();
}

function resetFilters() {
  filters.studentName = '';
  filters.studentNo = '';
  filters.classId = undefined;
  filters.assignmentName = '';
  appliedFilters.value = { ...filters };
  page.value = 1;
  void loadRows();
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

function queryKeyword() {
  return [appliedFilters.value.studentName, appliedFilters.value.studentNo, appliedFilters.value.assignmentName]
    .map((item) => item.trim())
    .filter(Boolean)
    .join(' ');
}

async function loadRows() {
  const currentRequest = ++requestId;
  loading.value = true;

  try {
    if (activeTab.value === 'notSubmitted') {
      rows.value = [];
      total.value = 0;
      return;
    }

    const result = await fetchAdminAssignmentAttempts({
      courseId: courseId.value,
      classId: appliedFilters.value.classId,
      status: statusForTab(activeTab.value),
      keyword: queryKeyword() || undefined,
      page: page.value,
      pageSize: pageSize.value
    });
    if (currentRequest !== requestId) {
      return;
    }

    rows.value = result.records.map(mapAttemptRow);
    total.value = result.total;
  } catch (error) {
    if (currentRequest === requestId) {
      rows.value = [];
      total.value = 0;
      ElMessage.error(error instanceof Error ? error.message : '作业批阅列表加载失败');
    }
  } finally {
    if (currentRequest === requestId) {
      loading.value = false;
    }
  }
}

async function loadClassOptions() {
  try {
    classOptions.value = (await fetchAdminClasses()).filter((item) => item.enabled !== false);
  } catch {
    classOptions.value = [];
  }
}

function handlePageSizeChange() {
  page.value = 1;
  void loadRows();
}

function goToPage(nextPage: number) {
  page.value = nextPage;
  void loadRows();
}

onMounted(() => {
  void loadClassOptions();
  void loadRows();
});
</script>
