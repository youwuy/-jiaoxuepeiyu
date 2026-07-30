<template>
  <StudentShell eyebrow="实训中心" title="实训中心">
    <section class="training-filterbar">
      <el-segmented v-model="mode" class="training-segmented" :options="modeOptions" />
      <el-input v-model="keyword" class="training-search" :prefix-icon="Search" placeholder="搜索实训名称" clearable />
      <el-select v-model="status" class="training-status-select" placeholder="全部状态">
        <el-option label="全部状态" value="all" />
        <el-option label="进行中" value="available" />
        <el-option label="未开始" value="notStarted" />
        <el-option label="已结束" value="completed" />
      </el-select>
    </section>

    <div v-if="loading" class="student-loading">实训加载中...</div>

    <section v-else class="training-list-panel">
      <article v-for="training in visibleTrainings" :key="training.id" class="training-list-card" :class="{ collapsed: !expandedIds.includes(training.id) }">
        <header class="training-parent-row">
          <button class="training-expand" :aria-label="`${training.title}展开状态`" @click="toggleTraining(training.id)">
            <el-icon>
              <ArrowDown v-if="expandedIds.includes(training.id)" />
              <ArrowRight v-else />
            </el-icon>
          </button>

          <div class="training-title-cell">
            <strong>{{ training.title }}</strong>
          </div>

          <div class="training-date-cell">
            <el-icon><Calendar /></el-icon>
            <span>{{ training.startAt }} ~ {{ training.deadline }}</span>
          </div>

          <span class="training-type-pill" :class="training.category === 'exam' ? 'is-exam' : 'is-practice'">
            {{ training.category === 'exam' ? '考试' : '练习' }}
          </span>

          <strong class="training-topic-count">{{ training.topicCount ?? training.steps?.length ?? 1 }} 题</strong>

          <span class="training-term">{{ training.term }}</span>

          <span class="training-state-pill" :class="`is-${training.status}`">{{ statusText[training.status] }}</span>

          <span v-if="training.status === 'available'" class="training-countdown is-hot">
            <el-icon><Clock /></el-icon>
            {{ training.countdown ?? '剩3天6小时' }}
          </span>
          <span v-else-if="training.status === 'notStarted'" class="training-countdown is-blue">
            <el-icon><Clock /></el-icon>
            {{ training.countdown ?? '待开放' }}
          </span>
          <span v-else-if="training.bestScore !== undefined" class="training-score-pill">
            <el-icon><Trophy /></el-icon>
            {{ training.bestScore }}分
          </span>
          <span v-else class="training-countdown is-muted">未提交</span>
        </header>

        <div v-if="expandedIds.includes(training.id)" class="training-step-list">
          <div v-for="(step, index) in training.steps ?? []" :key="step.id" class="training-step-row">
            <span class="training-step-index">{{ index + 1 }}</span>
            <span class="training-step-title">{{ step.title }}</span>
            <span class="training-mode-pill" :class="step.mode === 'team' ? 'is-team' : 'is-single'">
              <el-icon><User /></el-icon>
              {{ step.mode === 'team' ? '多人实训' : '单人实训' }}
            </span>
            <el-button
              class="training-row-action"
              :class="{ 'is-score': training.status === 'completed', 'is-disabled': training.status === 'notStarted' }"
              :disabled="training.status === 'notStarted'"
              :loading="actionLoadingIds.includes(step.id)"
              @click="handleTrainingAction(training, step)"
            >
              <el-icon><Document /></el-icon>
              {{ actionText(training, step) }}
            </el-button>
          </div>
        </div>
      </article>
    </section>
  </StudentShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { ArrowDown, ArrowRight, Calendar, Clock, Document, Search, Trophy, User } from '@element-plus/icons-vue';
import {
  createTrainingRoom,
  fetchStudentTrainings,
  fetchTrainingAppInstallation,
  fetchTrainingRoom,
  startTrainingRoom
} from '../../api/student';
import StudentShell from '../../components/student/StudentShell.vue';
import {
  filterTrainings,
  mockTrainings,
  type StudentTraining,
  type StudentTrainingStep,
  type TrainingModeFilter,
  type TrainingStatus
} from '../../features/student/training';

const mode = ref<TrainingModeFilter>('all');
const status = ref<TrainingStatus | 'all'>('all');
const keyword = ref('');
const loading = ref(false);
const actionLoadingIds = ref<number[]>([]);
const trainings = ref(mockTrainings);
const expandedIds = ref<number[]>(mockTrainings.map((item) => item.id));
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

function toggleTraining(id: number) {
  expandedIds.value = expandedIds.value.includes(id)
    ? expandedIds.value.filter((item) => item !== id)
    : [...expandedIds.value, id];
}

function actionText(training: StudentTraining, step: StudentTrainingStep) {
  if (training.status === 'completed' || step.action === 'score') {
    return '查看成绩单';
  }

  if (step.action === 'team' || step.mode === 'team') {
    return '组队实训';
  }

  if (step.action === 'retry') {
    return '再次实训';
  }

  return '开始实训';
}

async function handleTrainingAction(training: StudentTraining, step: StudentTrainingStep) {
  if (training.status === 'notStarted') {
    return;
  }

  if (training.status === 'completed' || step.action === 'score') {
    ElMessage.info(`正在查看成绩单：${step.title}`);
    return;
  }

  actionLoadingIds.value = [...actionLoadingIds.value, step.id];
  try {
    if (step.mode === 'team' || step.action === 'team') {
      const room = training.activeRoomId
        ? await fetchTrainingRoom(training.activeRoomId)
        : await createTrainingRoom(training.id);
      ElMessage.success(`组队房间已准备：${room.roomCode || room.roomId}`);
      return;
    }

    const installation = await fetchTrainingAppInstallation();
    if (installation.installed === false) {
      ElMessage.warning(installation.message || '实训应用未安装，请先安装后再开始实训');
      return;
    }

    if (training.activeRoomId) {
      await startTrainingRoom(training.activeRoomId);
    }
    ElMessage.success(`开始实训：${step.title}`);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '实训接口调用失败');
  } finally {
    actionLoadingIds.value = actionLoadingIds.value.filter((id) => id !== step.id);
  }
}

onMounted(async () => {
  loading.value = true;
  try {
    const remoteTrainings = await fetchStudentTrainings();
    trainings.value = remoteTrainings;
    expandedIds.value = remoteTrainings.map((item) => item.id);
  } catch {
    trainings.value = mockTrainings;
    expandedIds.value = mockTrainings.map((item) => item.id);
  } finally {
    loading.value = false;
  }
});
</script>
