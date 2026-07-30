<template>
  <StudentShell eyebrow="实训中心" title="我的实训">
    <section class="student-board-toolbar">
      <el-segmented v-model="mode" class="student-segmented" :options="modeOptions" />
      <div class="student-board-actions">
        <el-select v-model="status" class="compact-select" placeholder="状态">
          <el-option label="全部状态" value="all" />
          <el-option label="可进入" value="available" />
          <el-option label="未开始" value="notStarted" />
          <el-option label="已完成" value="completed" />
        </el-select>
        <el-input v-model="keyword" class="module-search" :prefix-icon="Search" placeholder="搜索实训名称" clearable />
      </div>
    </section>

    <div v-if="loading" class="student-loading">实训加载中...</div>

    <section v-else class="training-grid">
      <article v-for="training in visibleTrainings" :key="training.id" class="training-card">
        <div class="training-card-head">
          <span class="course-status-pill" :class="training.status === 'available' ? 'is-learning' : training.status === 'notStarted' ? 'is-notStarted' : 'is-completed'">
            {{ statusText[training.status] }}
          </span>
          <span class="course-term-pill">
            {{ training.mode === 'team' ? '多人实训' : '单人实训' }}
          </span>
        </div>
        <h2>{{ training.title }}</h2>
        <div class="course-meta-row">
          <span>
            <el-icon><Collection /></el-icon>
            {{ training.courseName }}
          </span>
          <span>
            <el-icon><Clock /></el-icon>
            {{ training.deadline }}
          </span>
          <span>
            <el-icon><Trophy /></el-icon>
            最高成绩：{{ training.bestScore ?? '-' }}
          </span>
        </div>
        <p v-if="training.roles?.length" class="course-period">
          <el-icon><User /></el-icon>
          角色：{{ training.roles.join('、') }}
        </p>
        <p v-else class="course-period">
          <el-icon><User /></el-icon>
          练习次数：{{ training.attempts || '-' }}
        </p>
        <el-button
          class="course-action"
          :class="{ 'is-muted': training.status === 'notStarted', 'is-plain': training.status === 'completed' }"
          :type="training.status === 'available' ? 'primary' : 'default'"
          :disabled="training.status === 'notStarted'"
        >
          {{ training.status === 'completed' ? '再次实训' : '开始实训' }}
        </el-button>
      </article>
    </section>
  </StudentShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Clock, Collection, Search, Trophy, User } from '@element-plus/icons-vue';
import { fetchStudentTrainings } from '../../api/student';
import StudentShell from '../../components/student/StudentShell.vue';
import { filterTrainings, mockTrainings, type TrainingModeFilter, type TrainingStatus } from '../../features/student/training';

const mode = ref<TrainingModeFilter>('all');
const status = ref<TrainingStatus | 'all'>('all');
const keyword = ref('');
const loading = ref(false);
const trainings = ref(mockTrainings);
const modeOptions = [
  { label: '全部', value: 'all' },
  { label: '单人实训', value: 'single' },
  { label: '多人实训', value: 'team' }
];

const statusText: Record<TrainingStatus, string> = {
  available: '可进入',
  notStarted: '未开始',
  completed: '已完成'
};

const visibleTrainings = computed(() =>
  filterTrainings(trainings.value, {
    mode: mode.value,
    status: status.value,
    keyword: keyword.value
  })
);

onMounted(async () => {
  loading.value = true;
  try {
    trainings.value = await fetchStudentTrainings();
  } catch {
    ElMessage.warning('后端实训接口暂不可用，已展示本地示例数据');
  } finally {
    loading.value = false;
  }
});
</script>
