<template>
  <StudentShell eyebrow="课程学习" title="课程学习">
    <section class="course-toolbar">
      <el-input
        v-model="keyword"
        class="course-search"
        :prefix-icon="Search"
        placeholder="搜索课程名称"
        clearable
        @blur="keyword = keyword.trim()"
        @keyup.enter="keyword = keyword.trim()"
      />
    </section>

    <div v-if="loading" class="student-loading">课程加载中...</div>

    <el-empty v-else-if="visibleCourses.length === 0" description="暂未找到课程" />

    <section v-else class="course-grid">
      <article
        v-for="course in visibleCourses"
        :key="course.id"
        class="course-card"
        :class="{ 'is-clickable': course.status !== 'notStarted' }"
        :tabindex="course.status === 'notStarted' ? -1 : 0"
        :aria-disabled="course.status === 'notStarted'"
        role="button"
        @click="openCourse(course)"
        @keyup.enter="openCourse(course)"
        @keyup.space.prevent="openCourse(course)"
      >
        <div class="course-card-head">
          <div class="course-labels">
            <span class="course-status-pill" :class="`is-${course.status}`">
              {{ statusMeta[course.status].label }}
            </span>
            <span class="course-term-pill">{{ course.term }}</span>
          </div>
          <span class="course-progress-text">已学{{ course.progress }}%</span>
        </div>

        <h3>{{ course.name }}</h3>

        <div class="course-meta-row">
          <span>
            <el-icon><Document /></el-icon>
            {{ course.resourceCount }} 个课件
          </span>
          <span>
            <el-icon><DocumentChecked /></el-icon>
            {{ course.assignmentCount }} 次作业
          </span>
          <span>
            <el-icon><User /></el-icon>
            教师：{{ course.teachers.join('、') }}
          </span>
        </div>

        <p class="course-period">
          <el-icon><Calendar /></el-icon>
          {{ formatOpenPeriod(course) }}
        </p>

        <el-button
          class="course-action"
          :class="{ 'is-muted': course.status === 'notStarted', 'is-plain': course.status === 'completed' }"
          :type="course.status === 'learning' ? 'primary' : 'default'"
          :disabled="course.status === 'notStarted'"
          @click.stop="openCourse(course)"
        >
          {{ statusMeta[course.status].action }}
        </el-button>
      </article>
    </section>
  </StudentShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Calendar, Document, DocumentChecked, Search, User } from '@element-plus/icons-vue';
import { fetchStudentCourses } from '../../api/student';
import StudentShell from '../../components/student/StudentShell.vue';
import {
  buildCourseViews,
  filterCoursesByKeyword,
  formatOpenPeriod,
  type CourseStatus,
  type StudentCourse,
  type StudentCourseView
} from '../../features/student/courses';

const router = useRouter();
const keyword = ref('');
const loading = ref(false);
const currentTime = new Date();
const courses = ref<StudentCourse[]>([]);

const statusMeta: Record<CourseStatus, { label: string; action: string }> = {
  learning: {
    label: '学习中',
    action: '进入学习'
  },
  notStarted: {
    label: '未开始',
    action: '暂未开放'
  },
  completed: {
    label: '已结束',
    action: '查看详情'
  }
};

const courseViews = computed(() => buildCourseViews(courses.value, currentTime));
const visibleCourses = computed(() => filterCoursesByKeyword(courseViews.value, keyword.value));

onMounted(async () => {
  loading.value = true;
  try {
    courses.value = await fetchStudentCourses();
  } catch (error) {
    courses.value = [];
    ElMessage.error(error instanceof Error ? error.message : '课程列表加载失败');
  } finally {
    loading.value = false;
  }
});

function openCourse(course: StudentCourseView) {
  if (course.status === 'notStarted') {
    return;
  }

  router.push(`/student/courses/${course.id}/learn`);
}
</script>
