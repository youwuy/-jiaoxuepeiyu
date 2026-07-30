<template>
  <StudentShell eyebrow="课程学习" title="课程详情">
    <section class="learn-workbench">
      <aside class="course-sidebar">
        <button class="back-link" @click="router.push('/student/courses')">
          <el-icon><ArrowLeft /></el-icon>
          返回课程列表
        </button>

        <div class="course-title-block">
          <h1>{{ course.name }}</h1>
          <div class="course-progress-strip">
            <span :style="{ width: `${progress}%` }"></span>
          </div>
          <strong>{{ progress }}%</strong>
        </div>

        <el-scrollbar class="catalog-scroll">
          <section v-for="(chapter, chapterIndex) in course.chapters" :key="chapter.id" class="catalog-section">
            <div class="chapter-row" :class="`is-${chapter.status}`">
              <span class="chapter-caret">
                <el-icon><ArrowDown v-if="chapter.status !== 'notStarted'" /><ArrowRight v-else /></el-icon>
              </span>
              <span class="chapter-index" :class="`is-${chapter.status}`">
                <CircleCheckFilled v-if="chapter.status === 'completed'" />
                <template v-else>{{ chapterIndex + 1 }}</template>
              </span>
              <strong>{{ chapter.title }}</strong>
              <em>{{ chapterStatusText[chapter.status] }}</em>
            </div>

            <button
              v-for="item in chapter.items"
              :key="item.id"
              class="catalog-item"
              :class="itemClasses(item)"
              @click="selectItem(item)"
            >
              <span class="catalog-item-state">
                <el-icon>
                  <CircleCheckFilled v-if="item.status === 'completed'" />
                  <CircleCheck v-else-if="selectedItem?.id === item.id" />
                  <CircleClose v-else-if="item.status === 'locked'" />
                  <Clock v-else />
                </el-icon>
              </span>
              <el-icon class="catalog-item-type">
                <Document v-if="item.type === 'courseware'" />
                <DocumentChecked v-else />
              </el-icon>
              <span>{{ item.title }}</span>
              <strong v-if="item.score !== undefined">{{ item.score }}分</strong>
              <strong v-else-if="item.deadline" class="is-danger">截止 {{ item.deadline.slice(5) }}</strong>
              <strong v-else>{{ item.type === 'courseware' ? '课件' : '作业' }}</strong>
            </button>
          </section>
        </el-scrollbar>
      </aside>

      <main class="learn-main">
        <section v-if="selectedItem" class="courseware-panel">
          <header class="learn-header">
            <div class="learn-file-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div>
              <h2>{{ selectedItem.title }}</h2>
              <p>
                课件类型：{{ selectedItem.resourceType ?? itemTypeText[selectedItem.type] }}
                <span>最低学习时长：{{ selectedItem.minDurationMinutes ?? 15 }}分钟</span>
              </p>
            </div>
            <div class="learn-timer">
              <el-icon><Timer /></el-icon>
              <strong>已学习 {{ formatLearnedTime(selectedItem) }}</strong>
            </div>
          </header>

          <div v-if="selectedItem.type === 'courseware'" class="preview-window">
            <img :src="stationPreview" :alt="selectedItem.title" />
          </div>

          <div v-else class="assignment-panel">
            <el-tag type="warning" effect="light">课程作业</el-tag>
            <h3>{{ selectedItem.title }}</h3>
            <p>截止时间 {{ selectedItem.deadline }}，完成后可在目录中查看得分和学习记录。</p>
            <el-button type="primary" :disabled="selectedItem.status === 'locked'">进入作业</el-button>
          </div>
        </section>
      </main>
    </section>
  </StudentShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  ArrowDown,
  ArrowLeft,
  ArrowRight,
  CircleCheck,
  CircleCheckFilled,
  CircleClose,
  Clock,
  Document,
  DocumentChecked,
  Timer
} from '@element-plus/icons-vue';
import StudentShell from '../../components/student/StudentShell.vue';
import stationPreview from '../../assets/course-station-preview.png';
import { fetchStudentCourse, updateCoursewareProgress } from '../../api/student';
import {
  calculateCourseProgress,
  mockStudentCourses,
  type CourseCatalogItem,
  type CourseChapter,
  type CourseItemType
} from '../../features/student/courses';

const route = useRoute();
const router = useRouter();
const courseId = computed(() => Number(route.params.id));
const course = ref(mockStudentCourses.find((item) => item.id === courseId.value) ?? mockStudentCourses[0]);
const progress = computed(() => calculateCourseProgress(course.value));
const selectedItem = ref<CourseCatalogItem>();
const progressSyncingIds = ref<string[]>([]);

const chapterStatusText: Record<CourseChapter['status'], string> = {
  completed: '已完成',
  learning: '',
  notStarted: '未开始'
};

const itemTypeText: Record<CourseItemType, string> = {
  courseware: 'PPT文档',
  assignment: '在线作业'
};

onMounted(async () => {
  try {
    course.value = await fetchStudentCourse(courseId.value);
  } catch {
    ElMessage.warning('后端课程详情接口暂不可用，已展示本地示例数据');
  }
});

watch(
  course,
  (nextCourse) => {
    selectedItem.value =
      nextCourse.chapters.flatMap((chapter) => chapter.items).find((item) => item.status === 'current') ??
      nextCourse.chapters.flatMap((chapter) => chapter.items).find((item) => item.status !== 'locked') ??
      nextCourse.chapters[0]?.items[0];
  },
  { immediate: true }
);

function itemClasses(item: CourseCatalogItem) {
  return {
    active: selectedItem.value?.id === item.id,
    completed: item.status === 'completed',
    locked: item.status === 'locked',
    pending: item.status === 'pending'
  };
}

function selectItem(item: CourseCatalogItem) {
  if (item.status === 'locked') {
    ElMessage.warning('请先完成前置课件或作业');
    return;
  }

  selectedItem.value = item;

  if (item.type === 'courseware') {
    syncCoursewareProgress(item);
  }
}

async function syncCoursewareProgress(item: CourseCatalogItem) {
  const contentId = Number(item.id);
  if (!Number.isFinite(contentId) || progressSyncingIds.value.includes(item.id)) {
    return;
  }

  progressSyncingIds.value = [...progressSyncingIds.value, item.id];
  try {
    const requiredSeconds = (item.minDurationMinutes ?? 0) * 60;
    const studiedSeconds = Math.max(item.learnedSeconds ?? 0, Math.min(requiredSeconds, 60));
    await updateCoursewareProgress(courseId.value, contentId, studiedSeconds, item.status === 'completed');
  } catch {
    ElMessage.warning('学习进度暂未同步，稍后会继续使用本地展示');
  } finally {
    progressSyncingIds.value = progressSyncingIds.value.filter((id) => id !== item.id);
  }
}

function formatLearnedTime(item: CourseCatalogItem): string {
  const totalSeconds = item.learnedSeconds ?? (item.durationMinutes ?? 0) * 60;
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;

  if (seconds <= 0) {
    return `${minutes}分钟`;
  }

  return `${minutes}分${seconds}秒`;
}
</script>
