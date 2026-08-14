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
          <el-button class="admin-course-stats-export" :loading="exporting" @click="exportData">
            <el-icon><Download /></el-icon>
            导出数据
          </el-button>
        </header>

        <div v-loading="loading" class="admin-course-stats-table-scroll">
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
              <tr v-for="(student, index) in students" :key="student.studentId || student.studentNo">
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
          <p>显示 {{ pageStart }} 到 {{ pageEnd }} 条，共 {{ total }} 条记录</p>
          <div class="admin-course-stats-pager">
            <el-button :icon="DArrowLeft" :disabled="page === 1" @click="page = 1" />
            <el-button :icon="ArrowLeft" :disabled="page === 1" @click="page = Math.max(1, page - 1)" />
            <el-pagination
              v-model:current-page="page"
              :page-size="pageSize"
              :total="total"
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

      <el-dialog
        v-model="detailVisible"
        class="admin-course-stats-detail-dialog"
        width="820px"
        :show-close="false"
        append-to-body
      >
        <template #header>
          <div class="admin-course-stats-detail-head">
            <div>
              <strong>{{ currentStudent ? `${currentStudent.name}的成绩详情` : '成绩详情' }}</strong>
              <span>{{ courseTitle }}</span>
            </div>
            <el-button text circle :icon="Close" @click="detailVisible = false" />
          </div>
        </template>

        <section v-if="currentStudent" v-loading="detailLoading" class="admin-course-stats-detail-body">
          <div class="admin-course-stats-detail-table-head">
            <span>名称</span>
            <span>类型</span>
            <span>完成情况</span>
            <span>得分</span>
          </div>

          <div class="admin-course-stats-detail-tree">
            <el-empty v-if="!detailLoading && detailTree.length === 0" description="暂无课程学习明细" />
            <section v-for="chapter in detailTree" :key="chapter.id" class="admin-course-stats-detail-chapter">
              <button type="button" class="admin-course-stats-detail-chapter-row" @click="toggleChapter(chapter.id)">
                <el-icon><ArrowDown v-if="expandedChapterIds.includes(chapter.id)" /><ArrowRight v-else /></el-icon>
                <strong>{{ chapter.title }}</strong>
              </button>
              <template v-if="expandedChapterIds.includes(chapter.id)">
                <div v-for="item in chapter.items" :key="item.id" class="admin-course-stats-detail-item-row root-item">
                  <span class="admin-course-stats-detail-item-name">
                    <el-icon :class="item.type"><component :is="item.type === 'assignment' ? EditPen : Document" /></el-icon>
                    {{ item.name }}
                  </span>
                  <span><b :class="item.type">{{ item.typeText }}</b></span>
                  <span><em>{{ item.status }}</em></span>
                  <span class="score">{{ item.score }}</span>
                </div>
                <article v-for="section in chapter.sections" :key="section.id" class="admin-course-stats-detail-section">
                <button
                  type="button"
                  class="admin-course-stats-detail-section-row"
                  :style="{ paddingLeft: `${20 + section.depth * 18}px` }"
                  @click="toggleSection(section.id)"
                >
                  <el-icon><ArrowDown v-if="expandedSectionIds.includes(section.id)" /><ArrowRight v-else /></el-icon>
                  <strong>{{ section.title }}</strong>
                </button>
                <div v-for="item in expandedSectionIds.includes(section.id) ? section.items : []" :key="item.id" class="admin-course-stats-detail-item-row">
                  <span class="admin-course-stats-detail-item-name">
                    <el-icon :class="item.type"><component :is="item.type === 'assignment' ? EditPen : Document" /></el-icon>
                    {{ item.name }}
                  </span>
                  <span><b :class="item.type">{{ item.typeText }}</b></span>
                  <span><em>{{ item.status }}</em></span>
                  <span class="score">{{ item.score }}</span>
                </div>
                </article>
              </template>
            </section>
          </div>
        </section>

        <template #footer>
          <div class="admin-course-stats-detail-footer">
            <el-button type="primary" @click="detailVisible = false">关闭</el-button>
          </div>
        </template>
      </el-dialog>
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
  Close,
  DArrowLeft,
  DArrowRight,
  Download,
  Document,
  EditPen,
  Refresh,
  Search,
  Tickets
} from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  exportAdminCourseStudentStatistics,
  fetchAdminCourseDetail,
  fetchAdminCourseStatistics,
  fetchAdminCourseStudentStatisticsDetail,
  fetchAdminCourseStudentStatistics,
  type AdminCourseStatistics,
  type AdminCourseStudentContentStatistics,
  type AdminCourseStudentStatistics
} from '../../api/admin-course';

interface StudentScoreRow {
  studentId: number;
  name: string;
  studentNo: string;
  className: string;
  progress: number;
  progressScore: number;
  assignmentCount: number;
  assignmentScore: number;
}

interface DetailItem {
  id: number;
  name: string;
  type: 'courseware' | 'assignment';
  typeText: '课件' | '作业';
  status: string;
  score: string;
}

interface DetailSection {
  id: number;
  title: string;
  depth: number;
  items: DetailItem[];
}

interface DetailChapter extends DetailSection {
  sections: DetailSection[];
}

const route = useRoute();
const router = useRouter();
const courseId = computed(() => Number(route.params.id));
const courseTitle = ref((route.query.title as string) || '教学课程');

const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);
const detailLoading = ref(false);
const exporting = ref(false);
const initialStudentNo = String(route.query.studentNo || '').trim();
const filters = reactive({
  studentName: '',
  studentNo: initialStudentNo,
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
const detailVisible = ref(false);
const currentStudent = ref<StudentScoreRow>();
const detailAutoOpened = ref(false);
const detailRows = ref<AdminCourseStudentContentStatistics[]>([]);
const expandedChapterIds = ref<number[]>([]);
const expandedSectionIds = ref<number[]>([]);

const detailTree = computed(() => {
  return buildDetailTree(detailRows.value);
});

const classOptions = computed(() => Array.from(new Set(students.value.map((item) => item.className))));
const pageCount = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)));
const pageStart = computed(() => (total.value === 0 ? 0 : (page.value - 1) * pageSize.value + 1));
const pageEnd = computed(() => Math.min(page.value * pageSize.value, total.value));

watch(pageSize, () => {
  page.value = 1;
  void loadStudents();
});

watch(page, () => {
  void loadStudents();
});

function formatScore(value: number) {
  return Number.isInteger(value) ? String(value) : value.toFixed(1);
}

function applyFilters() {
  appliedFilters.value = { ...filters };
  page.value = 1;
  void loadStudents();
}

function resetFilters() {
  filters.studentName = '';
  filters.studentNo = '';
  filters.className = '';
  appliedFilters.value = { ...filters };
  page.value = 1;
  void loadStudents();
}

function goBack() {
  router.push('/admin/courses');
}

async function exportData() {
  exporting.value = true;
  try {
    await exportAdminCourseStudentStatistics(courseId.value, currentQuery(false));
    ElMessage.success('成绩文件已导出');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '成绩导出失败');
  } finally {
    exporting.value = false;
  }
}

async function showDetail(student: StudentScoreRow) {
  currentStudent.value = student;
  detailVisible.value = true;
  detailRows.value = [];
  detailLoading.value = true;
  try {
    detailRows.value = await fetchAdminCourseStudentStatisticsDetail(courseId.value, student.studentId);
    const tree = buildDetailTree(detailRows.value);
    expandedChapterIds.value = tree.map((chapter) => chapter.id);
    expandedSectionIds.value = tree.flatMap((chapter) => chapter.sections.map((section) => section.id));
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '课程学员成绩详情加载失败');
  } finally {
    detailLoading.value = false;
  }
}

async function loadCourseStatistics() {
  try {
    courseStats.value = await fetchAdminCourseStatistics(courseId.value);
  } catch (error) {
    ElMessage.warning(error instanceof Error ? error.message : '课程统计接口暂不可用');
  }
}

async function loadCourseDetail() {
  if (!courseId.value) {
    return;
  }
  try {
    const detail = await fetchAdminCourseDetail(courseId.value);
    courseTitle.value = detail.courseName || courseTitle.value;
  } catch {
    // The statistics page can still render when the detail endpoint is unavailable.
  }
}

async function loadStudents() {
  if (!courseId.value) {
    return;
  }
  loading.value = true;
  try {
    const result = await fetchAdminCourseStudentStatistics(courseId.value, currentQuery(true));
    students.value = result.records.map(mapStudent);
    total.value = result.total;
    if (page.value > pageCount.value) {
      page.value = pageCount.value;
    }
    openInitialDetail();
  } catch (error) {
    students.value = [];
    total.value = 0;
    ElMessage.error(error instanceof Error ? error.message : '课程学员成绩接口暂不可用');
  } finally {
    loading.value = false;
  }
}

function openInitialDetail() {
  if (!initialStudentNo || detailAutoOpened.value) {
    return;
  }

  const matched = students.value.find((student) => student.studentNo === initialStudentNo);
  if (matched) {
    void showDetail(matched);
    detailAutoOpened.value = true;
  }
}

function currentQuery(withPage: boolean) {
  return {
    studentName: appliedFilters.value.studentName.trim() || undefined,
    studentNo: appliedFilters.value.studentNo.trim() || undefined,
    className: appliedFilters.value.className.trim() || undefined,
    page: withPage ? page.value : undefined,
    pageSize: withPage ? pageSize.value : undefined
  };
}

function mapStudent(row: AdminCourseStudentStatistics): StudentScoreRow {
  return {
    studentId: row.studentId,
    name: row.studentName || '-',
    studentNo: row.studentNo || '-',
    className: row.className || '-',
    progress: Number(row.progressPercent || 0),
    progressScore: Number(row.progressScore || 0),
    assignmentCount: Number(row.assignmentCount || 0),
    assignmentScore: Number(row.assignmentScore || 0)
  };
}

function buildDetailTree(rows: AdminCourseStudentContentStatistics[]): DetailChapter[] {
  const nodes = new Map<number, DetailChapter & { parentId?: number; sortOrder: number; children: DetailSection[] }>();
  for (const row of rows) {
    if (!nodes.has(row.chapterId)) {
      nodes.set(row.chapterId, {
        id: row.chapterId,
        parentId: row.parentChapterId,
        title: row.chapterTitle || '未命名章节',
        depth: 0,
        sortOrder: Number(row.chapterSortOrder || 0),
        items: [],
        sections: [],
        children: []
      });
    }
    if (row.contentId) {
      const type = row.itemType === 'ASSIGNMENT' ? 'assignment' : 'courseware';
      nodes.get(row.chapterId)?.items.push({
        id: row.contentId,
        name: row.contentTitle || '未命名内容',
        type,
        typeText: type === 'assignment' ? '作业' : '课件',
        status: completionStatusText(row.completionStatus),
        score: type === 'assignment' && row.score !== undefined && row.score !== null ? formatScore(Number(row.score)) : '-'
      });
    }
  }

  const roots: Array<DetailChapter & { parentId?: number; sortOrder: number; children: DetailSection[] }> = [];
  for (const node of nodes.values()) {
    const parent = node.parentId ? nodes.get(node.parentId) : undefined;
    if (parent) parent.children.push(node);
    else roots.push(node);
  }
  const byOrder = (left: { sortOrder?: number; id: number }, right: { sortOrder?: number; id: number }) =>
    Number(left.sortOrder || 0) - Number(right.sortOrder || 0) || left.id - right.id;
  roots.sort(byOrder);
  const flatten = (node: DetailChapter & { children: DetailSection[] }, depth = 1): DetailSection[] =>
    node.children.sort(byOrder).flatMap((child) => [
      { id: child.id, title: child.title, depth, items: child.items },
      ...flatten(child as DetailChapter & { children: DetailSection[] }, depth + 1)
    ]);
  return roots.map((root) => ({
    id: root.id,
    title: root.title,
    depth: 0,
    items: root.items,
    sections: flatten(root)
  }));
}

function completionStatusText(value?: string) {
  if (value === 'COMPLETED') return '已完成';
  if (value === 'IN_PROGRESS') return '进行中';
  return '未完成';
}

function toggleChapter(id: number) {
  expandedChapterIds.value = expandedChapterIds.value.includes(id)
    ? expandedChapterIds.value.filter((item) => item !== id)
    : [...expandedChapterIds.value, id];
}

function toggleSection(id: number) {
  expandedSectionIds.value = expandedSectionIds.value.includes(id)
    ? expandedSectionIds.value.filter((item) => item !== id)
    : [...expandedSectionIds.value, id];
}

onMounted(() => {
  void loadCourseDetail();
  void loadCourseStatistics();
  void loadStudents();
});
</script>
