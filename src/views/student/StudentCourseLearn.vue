<template>
  <section class="learn-page">
    <aside class="course-sidebar">
      <button class="back-link" @click="router.push('/student/courses')">
        <el-icon><ArrowLeft /></el-icon>
        我的课程
      </button>

      <div class="course-title-block">
        <h1>{{ course.name }}</h1>
        <p>整体学习进度 {{ progress }}%</p>
        <el-progress :percentage="progress" :stroke-width="8" :show-text="false" />
      </div>

      <el-scrollbar class="catalog-scroll">
        <section v-for="chapter in course.chapters" :key="chapter.id" class="catalog-section">
          <div class="chapter-row">
            <span>{{ chapter.title }}</span>
            <el-tag size="small" :type="chapterTagType(chapter.status)">
              {{ chapterStatusText[chapter.status] }}
            </el-tag>
          </div>

          <button
            v-for="item in chapter.items"
            :key="item.id"
            class="catalog-item"
            :class="{ active: selectedItem?.id === item.id, locked: item.status === 'locked' }"
            @click="selectItem(item)"
          >
            <el-icon>
              <Lock v-if="item.status === 'locked'" />
              <CircleCheck v-else-if="item.status === 'completed'" />
              <VideoPlay v-else-if="item.type === 'courseware'" />
              <DocumentChecked v-else />
            </el-icon>
            <span>{{ item.title }}</span>
            <strong v-if="item.score !== undefined">{{ item.score }} 分</strong>
            <strong v-else-if="item.deadline">截止 {{ item.deadline.slice(5) }}</strong>
          </button>
        </section>
      </el-scrollbar>
    </aside>

    <main class="learn-main">
      <header class="learn-header">
        <div>
          <p>{{ selectedItem?.type === 'courseware' ? '课件学习' : '课程作业' }}</p>
          <h2>{{ selectedItem?.title }}</h2>
        </div>
        <div class="learn-timer">
          <span>已学习时长</span>
          <strong>{{ selectedItem?.durationMinutes ?? 0 }} 分钟</strong>
        </div>
      </header>

      <section v-if="selectedItem?.type === 'courseware'" class="courseware-panel">
        <div class="ware-meta">
          <el-tag>课件</el-tag>
          <span>最低学习时长 {{ selectedItem.minDurationMinutes }} 分钟</span>
          <span>{{ selectedItem.openStart }} 至 {{ selectedItem.openEnd }} 开放</span>
        </div>
        <div class="preview-window">
          <el-icon><Reading /></el-icon>
          <h3>{{ selectedItem.title }}</h3>
          <p>这里展示课件预览内容。后续接入资源预览接口后，可加载 PDF、视频、PPT、图片等资源。</p>
        </div>
      </section>

      <section v-else class="assignment-panel">
        <div>
          <el-tag type="danger">作业</el-tag>
          <span>截止时间 {{ selectedItem?.deadline }}</span>
        </div>
        <h3>{{ selectedItem?.title }}</h3>
        <p>作业入口已占位，后续将继续拆分理论题作答、实训题作业和答题报告页面。</p>
        <el-button type="primary" :disabled="selectedItem?.status === 'locked'">进入作业</el-button>
      </section>
    </main>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  ArrowLeft,
  CircleCheck,
  DocumentChecked,
  Lock,
  Reading,
  VideoPlay
} from '@element-plus/icons-vue';
import {
  calculateCourseProgress,
  mockStudentCourses,
  type CourseCatalogItem,
  type CourseChapter
} from '../../features/student/courses';

const route = useRoute();
const router = useRouter();
const courseId = Number(route.params.id);
const course = computed(() => mockStudentCourses.find((item) => item.id === courseId) ?? mockStudentCourses[0]);
const progress = computed(() => calculateCourseProgress(course.value));

const selectedItem = ref<CourseCatalogItem | undefined>(
  course.value.chapters.flatMap((chapter) => chapter.items).find((item) => item.status === 'current') ??
    course.value.chapters[0]?.items[0]
);

const chapterStatusText: Record<CourseChapter['status'], string> = {
  completed: '已完成',
  learning: '学习中',
  notStarted: '未开始'
};

function chapterTagType(status: CourseChapter['status']) {
  if (status === 'completed') {
    return 'success';
  }

  if (status === 'learning') {
    return 'primary';
  }

  return 'info';
}

function selectItem(item: CourseCatalogItem) {
  if (item.status === 'locked') {
    ElMessage.warning('请先完成前置课件或作业');
    return;
  }

  selectedItem.value = item;
}
</script>
