<template>
  <section class="student-page">
    <header class="student-topbar">
      <div>
        <p>学员端</p>
        <h1>我的课程</h1>
      </div>
      <div class="student-userbar">
        <el-badge is-dot>
          <el-button :icon="Bell" circle aria-label="消息通知" />
        </el-badge>
        <el-dropdown>
          <button class="user-trigger">张同学</button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <main class="student-main">
      <section class="course-toolbar">
        <div>
          <h2>课程学习</h2>
          <p>按开放时间和学习状态查看当前课程安排</p>
        </div>
        <el-input
          v-model="keyword"
          class="course-search"
          :prefix-icon="Search"
          size="large"
          placeholder="搜索课程名称"
          clearable
          @blur="keyword = keyword.trim()"
          @keyup.enter="keyword = keyword.trim()"
        />
      </section>

      <el-empty v-if="visibleCourses.length === 0" description="暂未找到课程" />

      <section v-else class="course-grid">
        <article v-for="course in visibleCourses" :key="course.id" class="course-card">
          <div class="course-card-head">
            <el-tag :type="statusMeta[course.status].tagType" effect="light">
              {{ statusMeta[course.status].label }}
            </el-tag>
            <span>{{ course.term }}</span>
          </div>

          <div class="course-progress-line">
            <span>已学 {{ course.progress }}%</span>
            <el-progress :percentage="course.progress" :stroke-width="8" :show-text="false" />
          </div>

          <h3>{{ course.name }}</h3>

          <dl class="course-facts">
            <div>
              <dt>课件</dt>
              <dd>{{ course.resourceCount }} 个</dd>
            </div>
            <div>
              <dt>作业</dt>
              <dd>{{ course.assignmentCount }} 次</dd>
            </div>
            <div>
              <dt>授课教师</dt>
              <dd>{{ course.teachers.join('、') }}</dd>
            </div>
          </dl>

          <p class="course-period">
            <el-icon><Calendar /></el-icon>
            {{ formatOpenPeriod(course) }}
          </p>

          <el-button
            class="course-action"
            :type="course.status === 'notStarted' ? 'info' : 'primary'"
            :disabled="course.status === 'notStarted'"
            @click="openCourse(course)"
          >
            {{ statusMeta[course.status].action }}
          </el-button>
        </article>
      </section>
    </main>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Bell, Calendar, Search } from '@element-plus/icons-vue';
import {
  buildCourseViews,
  filterCoursesByKeyword,
  formatOpenPeriod,
  mockStudentCourses,
  type CourseStatus,
  type StudentCourseView
} from '../../features/student/courses';

const router = useRouter();
const keyword = ref('');
const currentTime = new Date('2026-07-30T08:00:00');

const statusMeta: Record<CourseStatus, { label: string; action: string; tagType: 'success' | 'info' | 'warning' }> = {
  learning: {
    label: '学习中',
    action: '进入学习',
    tagType: 'success'
  },
  notStarted: {
    label: '未开始',
    action: '暂未开放',
    tagType: 'info'
  },
  completed: {
    label: '已结束',
    action: '查看详情',
    tagType: 'warning'
  }
};

const courseViews = computed(() => buildCourseViews(mockStudentCourses, currentTime));
const visibleCourses = computed(() => filterCoursesByKeyword(courseViews.value, keyword.value));

function openCourse(course: StudentCourseView) {
  if (course.status === 'notStarted') {
    return;
  }

  router.push(`/student/courses/${course.id}/learn`);
}
</script>
