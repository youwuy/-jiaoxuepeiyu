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

          <strong class="training-topic-count">{{ training.topicCount ?? training.steps?.length ?? 0 }} 题</strong>

          <span class="training-term">{{ training.term }}</span>

          <span class="training-state-pill" :class="`is-${training.status}`">{{ statusText[training.status] }}</span>

          <span v-if="training.status === 'available'" class="training-countdown is-hot">
            <el-icon><Clock /></el-icon>
            {{ training.countdown || '-' }}
          </span>
          <span v-else-if="training.status === 'notStarted'" class="training-countdown is-blue">
            <el-icon><Clock /></el-icon>
            {{ training.countdown || '-' }}
          </span>
          <span v-else-if="training.bestScore !== undefined" class="training-score-pill">
            <el-icon><Trophy /></el-icon>
            {{ training.bestScore }}分
          </span>
          <span v-else class="training-countdown is-muted">未提交</span>
        </header>

        <div v-if="expandedIds.includes(training.id)" class="training-step-list">
          <div v-for="(step, index) in training.steps ?? []" :key="step.id" class="training-step-row">
            <span class="training-step-indent" aria-hidden="true"></span>
            <span class="training-step-title-cell">
              <span class="training-step-index">{{ index + 1 }}</span>
              <span class="training-step-title">{{ step.title }}</span>
            </span>
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
              <el-icon>
                <Document v-if="training.status === 'completed' || step.action === 'score'" />
                <Right v-else />
              </el-icon>
              {{ actionText(training, step) }}
            </el-button>
          </div>
        </div>
      </article>
    </section>

  </StudentShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { ArrowDown, ArrowRight, Calendar, Clock, Document, Right, Search, Trophy, User } from '@element-plus/icons-vue';
import {
  createTrainingRoom,
  createUeLaunchSession,
  fetchStudentTrainings,
  fetchTrainingRoom
} from '../../api/student';
import type { UeLaunchSession } from '../../api/student';
import { resolveApiBaseUrl } from '../../api/http';
import StudentShell from '../../components/student/StudentShell.vue';
import {
  filterTrainings,
  type StudentTraining,
  type StudentTrainingStep,
  type TrainingModeFilter,
  type TrainingStatus
} from '../../features/student/training';

const mode = ref<TrainingModeFilter>('all');
const router = useRouter();
const status = ref<TrainingStatus | 'all'>('all');
const keyword = ref('');
const loading = ref(false);
const actionLoadingIds = ref<number[]>([]);
const trainings = ref<StudentTraining[]>([]);
const expandedIds = ref<number[]>([]);
let trainingRequestId = 0;
const modeOptions = [
  { label: '全部', value: 'all' },
  { label: '单人实训', value: 'single' },
  { label: '多人实训', value: 'team' }
];

const statusText: Record<TrainingStatus, string> = {
  available: '进行中',
  notStarted: '未开始',
  completed: '已结束'
};

const visibleTrainings = computed(() =>
  filterTrainings(trainings.value, {
    mode: mode.value,
    status: status.value,
    keyword: keyword.value
  })
);

async function loadTrainings() {
  const requestId = ++trainingRequestId;
  loading.value = true;

  try {
    const remoteTrainings = await fetchStudentTrainings({
      mode: mode.value === 'all' ? undefined : mode.value,
      keyword: keyword.value.trim() || undefined
    });

    if (requestId !== trainingRequestId) {
      return;
    }

    trainings.value = remoteTrainings;
    expandedIds.value = trainings.value.map((item) => item.id);
  } catch (error) {
    if (requestId === trainingRequestId) {
      trainings.value = [];
      expandedIds.value = [];
      ElMessage.error(error instanceof Error ? error.message : '实训列表加载失败');
    }
  } finally {
    if (requestId === trainingRequestId) {
      loading.value = false;
    }
  }
}

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
    if (training.latestAttemptId) {
      await router.push({ name: 'student-training-score-sheet', params: { attemptId: training.latestAttemptId }, query: { trainingId: training.id } });
    } else {
      ElMessage.warning('暂无可查看的成绩单');
    }
    return;
  }

  actionLoadingIds.value = [...actionLoadingIds.value, step.id];
  try {
    const roomId = training.activeRoomId;
    if (step.mode === 'team' || step.action === 'team') {
      const room = training.activeRoomId
        ? await fetchTrainingRoom(training.activeRoomId)
        : await createTrainingRoom(training.id);
      await router.push({ name: 'student-training-room', params: { roomId: room.roomId } });
      return;
    }

    const session = await createUeLaunchSession(training.id);
    launchUeApplication({ ...session, roomId: session.roomId || roomId });
    ElMessage.success(`正在启动三维实训：${step.title}`);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '实训接口调用失败');
  } finally {
    actionLoadingIds.value = actionLoadingIds.value.filter((id) => id !== step.id);
  }
}

function launchUeApplication(session: UeLaunchSession) {
  const query = new URLSearchParams({
    protocolVersion: '1',
    apiBase: resolveApiBaseUrl(),
    trainingId: String(session.trainingId),
    studentId: String(session.studentId),
    launchToken: session.launchToken
  });
  if (session.roomId) {
    query.set('roomId', String(session.roomId));
  }

  const scheme = String(import.meta.env.VITE_UE_PROTOCOL || 'jiaoyu-ue').replace(/[^a-z0-9+.-]/gi, '');
  const launcher = document.createElement('iframe');
  launcher.hidden = true;
  launcher.src = `${scheme}://launch?${query.toString()}`;
  document.body.appendChild(launcher);
  window.setTimeout(() => launcher.remove(), 2000);
}

watch([mode, keyword], () => {
  void loadTrainings();
});

onMounted(() => {
  void loadTrainings();
});
</script>
