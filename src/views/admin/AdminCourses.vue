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
          <el-select v-model="draft.classId" class="admin-course-select" placeholder="授课班级" filterable clearable>
            <el-option v-for="item in classOptions" :key="item.classId" :label="item.className" :value="item.classId" />
          </el-select>
          <div class="admin-course-date-range">
            <el-date-picker
              v-model="draft.teachingStartDate"
              type="date"
              placeholder="教学开始日期"
              value-format="YYYY-MM-DD"
              clearable
            />
            <span>至</span>
            <el-date-picker
              v-model="draft.teachingEndDate"
              type="date"
              placeholder="教学结束日期"
              value-format="YYYY-MM-DD"
              clearable
            />
          </div>
          <el-select v-model="draft.publishStatus" class="admin-course-select" placeholder="课程状态" clearable>
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="未发布" value="NOT_PUBLISHED" />
          </el-select>
          <el-button class="admin-course-query-button" @click="applyFilters">查询</el-button>
          <el-button class="admin-course-reset-button" @click="resetFilters">重置</el-button>
        </div>

        <div class="admin-course-actions-row">
          <el-button class="admin-course-add-button" type="primary" :disabled="!can('create')" @click="handleCreateCourse">
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
                      <el-button class="admin-op-button warning" :disabled="!can('disable')" :loading="busyId === course.id" @click="cancelPublish(course)">
                        取消发布
                      </el-button>
                      <el-button class="admin-op-button primary" :disabled="!can('list')" @click="openDetail(course)">查看</el-button>
                      <el-button class="admin-op-button primary" :disabled="!can('update')" @click="handleReview(course)">批改作业</el-button>
                      <el-button class="admin-op-button primary" :disabled="!can('list')" @click="openStatistics(course)">成绩统计</el-button>
                      <el-button class="admin-op-button gray" :disabled="!can('create')" @click="copyCourse(course)">复制</el-button>
                      <el-button class="admin-op-button light" :disabled="!can('update')" @click="handleEditCourse(course)">编辑</el-button>
                      <el-button class="admin-op-button danger" :disabled="!can('delete')" :loading="busyId === course.id" @click="deleteCourse(course)">
                        删除
                      </el-button>
                      <el-dropdown trigger="click">
                        <el-button class="admin-op-button more">
                          更多
                          <el-icon><ArrowDown /></el-icon>
                        </el-button>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item :disabled="!can('list')" @click="openLogs(course)">操作日志</el-dropdown-item>
                            <el-dropdown-item :disabled="!can('list')" @click="openDetail(course)">课程详情</el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </template>

                    <template v-else>
                      <el-button class="admin-op-button light" :disabled="!can('update')" @click="handleEditCourse(course)">编辑</el-button>
                      <el-button class="admin-op-button danger" :disabled="!can('delete')" :loading="busyId === course.id" @click="deleteCourse(course)">
                        删除
                      </el-button>
                      <el-button class="admin-op-button success" :disabled="!can('enable')" :loading="busyId === course.id" @click="publishCourse(course)">
                        发布
                      </el-button>
                      <el-button class="admin-op-button gray" :disabled="!can('create')" @click="copyCourse(course)">复制</el-button>
                      <el-button class="admin-op-button muted" :disabled="!can('list')" @click="openLogs(course)">操作日志</el-button>
                    </template>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <footer class="admin-course-footer">
            <p>共 {{ total }} 条记录</p>
            <el-pagination
              v-model:current-page="page"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              background
              @current-change="loadCourses"
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

    <el-dialog
      v-model="logsVisible"
      class="admin-course-log-dialog"
      width="760px"
      :show-close="false"
      destroy-on-close
    >
      <div class="admin-drawer-head">
        <div>
          <p>操作日志</p>
          <h3>{{ selectedCourse?.title || '课程日志' }}</h3>
        </div>
        <el-button text :icon="Close" @click="logsVisible = false" />
      </div>

      <div v-if="logsLoading" class="admin-course-empty drawer-state">日志加载中...</div>
      <el-empty v-else-if="logs.length === 0" description="暂无操作日志" />
      <div v-else class="admin-course-log-table-wrap">
        <table class="admin-course-log-table">
          <thead>
            <tr>
              <th>序号</th>
              <th>操作人</th>
              <th>操作时间</th>
              <th>操作内容</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(log, index) in logs" :key="log.logId">
              <td>{{ index + 1 }}</td>
              <td>{{ log.operatorName || '-' }}</td>
              <td>{{ formatLogDateTime(log.createdAt) }}</td>
              <td>{{ formatLogAction(log.action) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <template #footer>
        <el-button @click="logsVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowDown, Close, Plus, Search } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  deleteAdminCourse,
  fetchAdminClasses,
  fetchAdminCourseDetail,
  fetchAdminCourseLogs,
  fetchAdminCourses,
  publishAdminCourse,
  cancelPublishAdminCourse
} from '../../api/admin-course';
import {
  buildAdminCourseViews,
  formatCourseContentTitle as formatContentTitle,
  mapAdminCourseView,
  type AdminCourseRecord,
  type AdminCourseView
} from '../../features/admin/courses';
import type { AdminClassOption } from '../../api/admin-course';
import { useAdminPermissions } from '../../features/admin/use-admin-permissions';

const pageSize = 5;
const router = useRouter();
const { can } = useAdminPermissions('teaching:course');
const page = ref(1);
const total = ref(0);
const busyId = ref<number | null>(null);
const loading = ref(false);
const detailLoading = ref(false);
const logsLoading = ref(false);

const courses = ref<AdminCourseRecord[]>([]);
const classOptions = ref<AdminClassOption[]>([]);
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
  classId: undefined as number | undefined,
  teachingStartDate: '',
  teachingEndDate: '',
  publishStatus: ''
});

const appliedFilters = ref({ ...draft });
let requestId = 0;

const courseViews = computed(() => buildAdminCourseViews(courses.value));

const pagedCourses = computed(() => courseViews.value);

function formatLogDateTime(value?: string) {
  if (!value) {
    return '-';
  }
  return value.replace('T', ' ').slice(0, 19);
}

function formatLogAction(action?: string) {
  const labels: Record<string, string> = {
    CREATE: '新增',
    COPY: '新增',
    UPDATE: '编辑',
    PUBLISH: '发布',
    CANCEL_PUBLISH: '取消发布',
    ENABLE: '启用',
    DISABLE: '禁用'
  };
  return labels[String(action || '').toUpperCase()] || '编辑';
}

async function loadCourses() {
  const currentRequest = ++requestId;
  loading.value = true;

  try {
    const result = await fetchAdminCourses({
      keyword: appliedFilters.value.keyword.trim() || undefined,
      classId: appliedFilters.value.classId,
      teachingStartTime: appliedFilters.value.teachingStartDate
        ? `${appliedFilters.value.teachingStartDate}T00:00:00`
        : undefined,
      teachingEndTime: appliedFilters.value.teachingEndDate
        ? `${appliedFilters.value.teachingEndDate}T23:59:59`
        : undefined,
      publishStatus: appliedFilters.value.publishStatus || undefined,
      page: page.value,
      pageSize
    });
    if (currentRequest !== requestId) {
      return;
    }

    courses.value = result.records;
    total.value = result.total;
  } catch (error) {
    if (currentRequest === requestId) {
      courses.value = [];
      total.value = 0;
      ElMessage.error(error instanceof Error ? error.message : '教学课程列表加载失败');
    }
  } finally {
    if (currentRequest === requestId) {
      loading.value = false;
    }
  }
}

function applyFilters() {
  if (
    draft.teachingStartDate &&
    draft.teachingEndDate &&
    draft.teachingEndDate < draft.teachingStartDate
  ) {
    ElMessage.warning('教学结束日期不能早于开始日期');
    return;
  }
  appliedFilters.value = { ...draft };
  page.value = 1;
  void loadCourses();
}

function resetFilters() {
  draft.keyword = '';
  draft.classId = undefined;
  draft.teachingStartDate = '';
  draft.teachingEndDate = '';
  draft.publishStatus = '';
  appliedFilters.value = { ...draft };
  page.value = 1;
  void loadCourses();
}

function handleCreateCourse() {
  router.push('/admin/courses/new');
}

async function handleEditCourse(course: AdminCourseView) {
  if (course.statusTone === 'published') {
    try {
      await ElMessageBox.confirm(
        `确定要编辑「${course.title}」吗？若已存在学习数据则会自动删除，请谨慎操作`,
        '编辑课程',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      );
    } catch {
      return;
    }
  }
  router.push(`/admin/courses/${course.id}/edit`);
}

function setBusy(courseId: number | null) {
  busyId.value = courseId;
}

async function publishCourse(course: AdminCourseView) {
  try {
    await ElMessageBox.confirm(`确定要发布「${course.title}」吗？发布后学员可以看到该课程`, '发布课程', {
      confirmButtonText: '确定发布',
      cancelButtonText: '取消',
      type: 'warning'
    });
  } catch {
    return;
  }

  setBusy(course.id);
  try {
    const detail = await fetchAdminCourseDetail(course.id);
    const detailView = mapAdminCourseView(detail);
    if (detailView.contentCount <= 0) {
      ElMessage.warning('请先在课程中添加课件或作业内容后再发布');
      mutateCourse(course.id, detail);
      return;
    }

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
  try {
    await ElMessageBox.confirm(`确定要取消发布「${course.title}」吗？取消后学员将无法继续访问该课程`, '取消发布', {
      confirmButtonText: '确定取消发布',
      cancelButtonText: '取消',
      type: 'warning'
    });
  } catch {
    return;
  }

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
    await ElMessageBox.confirm(`确定要删除「${course.title}」吗？删除后课程及学习数据将无法恢复，请谨慎操作`, '删除课程', {
      confirmButtonText: '确定',
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
    total.value = Math.max(0, total.value - 1);
    ElMessage.success('课程已删除');
    page.value = 1;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '删除失败');
  } finally {
    setBusy(null);
  }
}

function copyCourse(course: AdminCourseView) {
  router.push({ path: '/admin/courses/new', query: { copyFrom: String(course.id) } });
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

async function loadFilterOptions() {
  try {
    classOptions.value = (await fetchAdminClasses()).filter((item) => item.enabled !== false);
  } catch {
    classOptions.value = [];
  }
}

onMounted(() => {
  void loadFilterOptions();
  void loadCourses();
});
</script>
