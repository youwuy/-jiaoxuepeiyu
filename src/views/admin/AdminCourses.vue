<template>
  <AdminShell activeKey="admin-courses">
    <section class="admin-course-page">
      <el-breadcrumb class="admin-course-breadcrumb" separator="/">
        <el-breadcrumb-item>教学实训</el-breadcrumb-item>
        <el-breadcrumb-item>教学课程</el-breadcrumb-item>
      </el-breadcrumb>

      <section class="admin-course-filter-card">
        <div class="admin-course-filter-row">
          <el-input
            v-model="draft.keyword"
            class="admin-course-search"
            :prefix-icon="Search"
            placeholder="搜索课程名称"
            clearable
            @keyup.enter="applyFilters"
          />
          <el-select v-model="draft.className" class="admin-course-select" placeholder="授课班级" clearable>
            <el-option v-for="item in classOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="draft.termLabel" class="admin-course-select" placeholder="教学时间" clearable>
            <el-option v-for="item in termOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="draft.publishStatus" class="admin-course-select" placeholder="课程状态" clearable>
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="未发布" value="DRAFT" />
          </el-select>
          <el-button class="admin-course-query-button" @click="applyFilters">查询</el-button>
          <el-button class="admin-course-reset-button" @click="resetFilters">重置</el-button>
        </div>

        <div class="admin-course-actions-row">
          <el-button class="admin-course-add-button" type="primary" @click="handleCreateCourse">
            <el-icon><Plus /></el-icon>
            新增课程
          </el-button>
        </div>
      </section>

      <section class="admin-course-table-card">
        <div v-if="loading" class="admin-course-empty">课程加载中...</div>
        <div v-else-if="pagedCourses.length === 0" class="admin-course-empty">
          <el-empty description="暂无匹配课程" />
        </div>
        <template v-else>
          <div class="admin-course-table-scroll">
            <table class="admin-course-table">
              <thead>
                <tr>
                  <th>课程名称</th>
                  <th>学年学期</th>
                  <th>教学起止时间</th>
                  <th>授课班级</th>
                  <th>教学团队</th>
                  <th>待批改作业</th>
                  <th>课程状态</th>
                  <th>创建时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="course in pagedCourses" :key="course.id">
                  <td class="admin-course-name-cell">
                    <strong>{{ course.title }}</strong>
                  </td>
                  <td>{{ course.termLabel }}</td>
                  <td>{{ course.periodLabel }}</td>
                  <td class="admin-course-multiline">{{ course.classLabel }}</td>
                  <td>{{ course.teacherLabel }}</td>
                  <td class="admin-course-pending" :class="{ hot: Number(course.pendingReviewLabel) > 0 }">
                    {{ course.pendingReviewLabel }}
                  </td>
                  <td>
                    <span class="admin-course-status" :class="course.statusTone">
                      <i class="dot"></i>
                      {{ course.statusLabel }}
                    </span>
                  </td>
                  <td>{{ course.createdAtLabel }}</td>
                  <td class="admin-course-ops">
                    <template v-if="course.statusTone === 'published'">
                      <el-button class="admin-op-button warning" :loading="busyId === course.id" @click="cancelPublish(course)">
                        取消发布
                      </el-button>
                      <el-button class="admin-op-button primary" @click="openDetail(course)">查看</el-button>
                      <el-button class="admin-op-button primary" @click="handleReview(course)">批改作业</el-button>
                      <el-button class="admin-op-button primary" @click="openStatistics(course)">成绩统计</el-button>
                      <el-button class="admin-op-button gray" @click="copyCourse(course)">复制</el-button>
                      <el-dropdown trigger="click">
                        <el-button class="admin-op-button more">
                          更多
                          <el-icon><ArrowDown /></el-icon>
                        </el-button>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item @click="openLogs(course)">操作日志</el-dropdown-item>
                            <el-dropdown-item @click="openDetail(course)">课程详情</el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </template>

                    <template v-else>
                      <el-button class="admin-op-button light" @click="openDetail(course)">编辑</el-button>
                      <el-button class="admin-op-button danger" :loading="busyId === course.id" @click="deleteCourse(course)">
                        删除
                      </el-button>
                      <el-button class="admin-op-button success" :loading="busyId === course.id" @click="publishCourse(course)">
                        发布
                      </el-button>
                      <el-button class="admin-op-button gray" @click="copyCourse(course)">复制</el-button>
                      <el-button class="admin-op-button muted" @click="openLogs(course)">操作日志</el-button>
                    </template>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <footer class="admin-course-footer">
            <p>共 {{ filteredCourses.length }} 条记录</p>
            <el-pagination
              v-model:current-page="page"
              :page-count="2"
              layout="prev, pager, next"
              background
            />
          </footer>
        </template>
      </section>
    </section>

    <el-drawer
      v-model="detailVisible"
      class="admin-course-drawer"
      direction="rtl"
      size="720px"
      :with-header="false"
    >
      <div class="admin-drawer-head">
        <div>
          <p>课程详情</p>
          <h3>{{ selectedCourse?.title || '课程详情' }}</h3>
        </div>
        <el-button text :icon="Close" @click="detailVisible = false" />
      </div>

      <div v-if="detailLoading" class="admin-course-empty drawer-state">详情加载中...</div>

      <template v-else-if="selectedCourse">
        <section class="admin-detail-summary">
          <div>
            <span>课程状态</span>
            <strong>{{ selectedCourse.statusLabel }}</strong>
          </div>
          <div>
            <span>学年学期</span>
            <strong>{{ selectedCourse.termLabel }}</strong>
          </div>
          <div>
            <span>教学时间</span>
            <strong>{{ selectedCourse.periodLabel }}</strong>
          </div>
          <div>
            <span>教学团队</span>
            <strong>{{ selectedCourse.teacherLabel }}</strong>
          </div>
        </section>

        <section class="admin-detail-grid">
          <div class="admin-detail-card">
            <p>课程概况</p>
            <dl>
              <div><dt>授课班级</dt><dd>{{ selectedCourse.classLabel }}</dd></div>
              <div><dt>教学模式</dt><dd>{{ selectedDetail?.learningMode || '-' }}</dd></div>
              <div><dt>作业规则</dt><dd>{{ selectedDetail?.assignmentCompletionRule || '-' }}</dd></div>
              <div><dt>课件总数</dt><dd>{{ selectedDetail?.coursewareCount ?? selectedCourse.contentCount }}</dd></div>
              <div><dt>作业总数</dt><dd>{{ selectedDetail?.assignmentCount ?? 0 }}</dd></div>
              <div><dt>创建时间</dt><dd>{{ selectedCourse.createdAtLabel }}</dd></div>
            </dl>
          </div>

          <div class="admin-detail-card admin-detail-outline">
            <p>章节结构</p>
            <div v-if="(selectedDetail?.chapters?.length ?? 0) === 0" class="admin-course-empty inline">
              暂无章节
            </div>
            <div v-else class="admin-outline-list">
              <article v-for="chapter in selectedDetail?.chapters || []" :key="chapter.chapterId" class="admin-outline-item">
                <header>
                  <strong>{{ chapter.chapterTitle }}</strong>
                  <span>{{ chapter.contents?.length || 0 }} 项</span>
                </header>
                <ul>
                  <li v-for="content in chapter.contents || []" :key="content.contentId">
                    <span>{{ content.itemType === 'ASSIGNMENT' ? '作业' : '课件' }}</span>
                    <b>{{ formatContentTitle(content) }}</b>
                  </li>
                </ul>
              </article>
            </div>
          </div>
        </section>
      </template>
    </el-drawer>

    <el-drawer
      v-model="logsVisible"
      class="admin-course-drawer"
      direction="rtl"
      size="560px"
      :with-header="false"
    >
      <div class="admin-drawer-head">
        <div>
          <p>操作日志</p>
          <h3>{{ selectedCourse?.title || '课程日志' }}</h3>
        </div>
        <el-button text :icon="Close" @click="logsVisible = false" />
      </div>

      <div v-if="logsLoading" class="admin-course-empty drawer-state">日志加载中...</div>
      <div v-else class="admin-log-list">
        <article v-for="log in logs" :key="log.logId" class="admin-log-item">
          <header>
            <strong>{{ log.action }}</strong>
            <span>{{ formatDateTime(log.createdAt) }}</span>
          </header>
          <p>{{ log.content }}</p>
          <small>{{ log.operatorName }}</small>
        </article>
      </div>
    </el-drawer>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowDown, Close, Plus, Search } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  copyAdminCourse,
  deleteAdminCourse,
  fetchAdminCourseDetail,
  fetchAdminCourseLogs,
  fetchAdminCourses,
  publishAdminCourse,
  cancelPublishAdminCourse
} from '../../api/admin-course';
import {
  buildAdminCourseViews,
  formatCourseContentTitle as formatContentTitle,
  mockAdminCourses,
  type AdminCourseRecord,
  type AdminCourseView
} from '../../features/admin/courses';

const pageSize = 5;
const router = useRouter();
const page = ref(1);
const busyId = ref<number | null>(null);
const loading = ref(false);
const detailLoading = ref(false);
const logsLoading = ref(false);

const courses = ref<AdminCourseRecord[]>(mockAdminCourses);
const selectedCourse = ref<AdminCourseView | null>(null);
const selectedDetail = ref<AdminCourseRecord | null>(null);
const detailVisible = ref(false);
const logsVisible = ref(false);
const logs = ref<
  {
    logId: number;
    courseId: number;
    operatorName: string;
    action: string;
    content: string;
    createdAt: string;
  }[]
>([]);

const draft = reactive({
  keyword: '',
  className: '',
  termLabel: '',
  publishStatus: ''
});

const appliedFilters = ref({ ...draft });
let requestId = 0;

const courseViews = computed(() => buildAdminCourseViews(courses.value));

const classOptions = computed(() =>
  Array.from(
    new Set(
      courseViews.value
        .flatMap((course) => course.classLabel.split('\n'))
        .map((item) => item.trim())
        .filter((item) => Boolean(item) && item !== '-')
    )
  )
);

const termOptions = computed(() =>
  Array.from(new Set(courseViews.value.map((course) => course.termLabel).filter((item) => item && item !== '-')))
);

const filteredCourses = computed(() => {
  const keyword = normalizeText(appliedFilters.value.keyword);
  return courseViews.value.filter((course) => {
    const matchesKeyword =
      !keyword ||
      [course.title, course.termLabel, course.classLabel, course.teacherLabel, course.statusLabel]
        .join(' ')
        .toLowerCase()
        .includes(keyword);
    const matchesClass = !appliedFilters.value.className || course.classLabel.includes(appliedFilters.value.className);
    const matchesTerm = !appliedFilters.value.termLabel || course.termLabel === appliedFilters.value.termLabel;
    const matchesStatus =
      !appliedFilters.value.publishStatus || course.publishStatus?.toUpperCase() === appliedFilters.value.publishStatus;

    return matchesKeyword && matchesClass && matchesTerm && matchesStatus;
  });
});

const pagedCourses = computed(() => {
  const start = (page.value - 1) * pageSize;
  return filteredCourses.value.slice(start, start + pageSize);
});

function normalizeText(value: string) {
  return value.replace(/\s+/g, '').toLowerCase();
}

function formatDateTime(value?: string) {
  if (!value) {
    return '-';
  }

  const normalized = value.includes('T') ? value.replace('T', ' ') : value;
  return normalized.slice(0, 16);
}

async function loadCourses() {
  const currentRequest = ++requestId;
  loading.value = true;

  try {
    const result = await fetchAdminCourses({ page: 1, pageSize: 100 });
    if (currentRequest !== requestId) {
      return;
    }

    courses.value = result.records.length > 0 ? result.records : mockAdminCourses;
  } catch {
    if (currentRequest === requestId) {
      courses.value = mockAdminCourses;
    }
  } finally {
    if (currentRequest === requestId) {
      loading.value = false;
    }
  }
}

function applyFilters() {
  appliedFilters.value = { ...draft };
  page.value = 1;
}

function resetFilters() {
  draft.keyword = '';
  draft.className = '';
  draft.termLabel = '';
  draft.publishStatus = '';
  appliedFilters.value = { ...draft };
  page.value = 1;
}

function handleCreateCourse() {
  router.push('/admin/courses/new');
}

function setBusy(courseId: number | null) {
  busyId.value = courseId;
}

async function publishCourse(course: AdminCourseView) {
  setBusy(course.id);
  try {
    await publishAdminCourse(course.id);
    mutateCourse(course.id, { publishStatus: 'PUBLISHED' });
    ElMessage.success('课程已发布');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '发布失败');
  } finally {
    setBusy(null);
  }
}

async function cancelPublish(course: AdminCourseView) {
  setBusy(course.id);
  try {
    await cancelPublishAdminCourse(course.id);
    mutateCourse(course.id, { publishStatus: 'DRAFT' });
    ElMessage.success('已取消发布');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '取消发布失败');
  } finally {
    setBusy(null);
  }
}

async function deleteCourse(course: AdminCourseView) {
  try {
    await ElMessageBox.confirm(`确认删除课程「${course.title}」？`, '删除课程', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    });
  } catch {
    return;
  }

  setBusy(course.id);
  try {
    await deleteAdminCourse(course.id);
    courses.value = courses.value.filter((item) => item.courseId !== course.id);
    ElMessage.success('课程已删除');
    page.value = 1;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '删除失败');
  } finally {
    setBusy(null);
  }
}

async function copyCourse(course: AdminCourseView) {
  setBusy(course.id);
  try {
    const result = await copyAdminCourse(course.id);
    const copyId = result.courseId || Date.now();
    courses.value = [
      {
        ...course,
        courseId: copyId,
        courseName: `${course.title}（副本）`,
        publishStatus: 'DRAFT',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      },
      ...courses.value
    ];
    ElMessage.success('课程已复制');
    page.value = 1;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '复制失败');
  } finally {
    setBusy(null);
  }
}

async function openDetail(course: AdminCourseView) {
  selectedCourse.value = course;
  detailVisible.value = true;
  detailLoading.value = true;

  try {
    selectedDetail.value = await fetchAdminCourseDetail(course.id);
  } catch {
    selectedDetail.value = courses.value.find((item) => item.courseId === course.id) ?? null;
  } finally {
    detailLoading.value = false;
  }
}

function openStatistics(course: AdminCourseView) {
  router.push({
    path: `/admin/courses/${course.id}/statistics`,
    query: { title: course.title }
  });
}

async function openLogs(course: AdminCourseView) {
  selectedCourse.value = course;
  logsVisible.value = true;
  logsLoading.value = true;

  try {
    logs.value = await fetchAdminCourseLogs(course.id);
  } catch {
    logs.value = [];
  } finally {
    logsLoading.value = false;
  }
}

function handleReview(course: AdminCourseView) {
  router.push({
    path: `/admin/courses/${course.id}/reviews`,
    query: { title: course.title }
  });
}

function mutateCourse(courseId: number, patch: Partial<AdminCourseRecord>) {
  courses.value = courses.value.map((course) => (course.courseId === courseId ? { ...course, ...patch } : course));
}

onMounted(() => {
  void loadCourses();
});
</script>
