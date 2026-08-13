<template>
  <StudentShell eyebrow="实训中心" title="组队大厅">
    <section class="training-lobby-page">
      <nav class="training-lobby-breadcrumb" aria-label="面包屑导航">
        <button type="button" @click="router.push('/student/training')">实训中心</button>
        <el-icon><ArrowRight /></el-icon>
        <span>组队大厅</span>
      </nav>

      <section class="training-lobby-subject">
        <div class="training-lobby-title-row">
          <h1>{{ trainingTitle }}</h1>
          <span>当前实训题</span>
        </div>
        <p>选择实训房间加入队伍，开始任务演练</p>
      </section>

      <div class="training-lobby-create-row">
        <el-button type="primary" :loading="creating" @click="createRoom">创建房间</el-button>
      </div>

      <div v-if="loading" class="training-lobby-loading">房间加载中...</div>

      <section v-else-if="rooms.length" class="training-lobby-room-grid">
        <article
          v-for="room in rooms"
          :key="room.roomId"
          class="training-lobby-room-card"
          :class="{ 'is-started': isStarted(room) }"
        >
          <header>
            <h2>{{ roomName(room) }}</h2>
            <span class="training-lobby-room-state">
              <i></i>
              {{ isStarted(room) ? '已开始' : '未开始' }}
            </span>
          </header>

          <p>{{ memberSummary(room) }}</p>

          <el-button
            :disabled="isStarted(room)"
            :loading="joiningRoomId === room.roomId"
            @click="enterRoom(room)"
          >
            {{ isStarted(room) ? '已开始' : '进入房间' }}
            <el-icon v-if="!isStarted(room)"><Right /></el-icon>
          </el-button>
        </article>
      </section>

      <div v-else class="training-lobby-empty">
        <strong>暂无房间</strong>
        <span>请点击上方创建房间按钮，去创建一个吧</span>
      </div>
    </section>
  </StudentShell>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { ArrowRight, Right } from '@element-plus/icons-vue';
import { useRoute, useRouter } from 'vue-router';
import {
  createTrainingRoom,
  fetchStudentTrainings,
  fetchTrainingRooms,
  fetchTrainingRoom,
  joinTrainingRoom,
  type TrainingRoom
} from '../../api/student';
import StudentShell from '../../components/student/StudentShell.vue';

const route = useRoute();
const router = useRouter();
const rooms = ref<TrainingRoom[]>([]);
const fallbackTitle = ref('实训任务');
const loading = ref(true);
const creating = ref(false);
const joiningRoomId = ref<number>();
let pollTimer: number | undefined;

const trainingId = computed(() => Number(route.params.trainingId));
const topicId = computed(() => Number(route.query.topicId));
const activeRoomId = computed(() => Number(route.query.activeRoomId || 0));
const trainingTitle = computed(() => String(route.query.title || fallbackTitle.value));

function isStarted(room: TrainingRoom) {
  return room.roomStatus === 'STARTED';
}

function ownerName(room: TrainingRoom) {
  return room.members?.find((member) => member.owner)?.studentName || '学员';
}

function roomName(room: TrainingRoom) {
  return `${ownerName(room)}创建的房间`;
}

function memberSummary(room: TrainingRoom) {
  const names = room.members?.map((member) => member.studentName || '学员').join(', ') || '-';
  const remaining = Math.max((room.teamSize || room.roles?.length || room.members?.length || 0) - (room.members?.length || 0), 0);
  return `已加入：${names}  |  还需${remaining}人`;
}

function currentStudentId() {
  const storedStudent = localStorage.getItem('jiaoxuepeiyu_student_user');
  try {
    const user = storedStudent ? JSON.parse(storedStudent) as { id?: number; userId?: number; studentId?: number } : {};
    return Number(user.id ?? user.userId ?? user.studentId ?? 0);
  } catch {
    return 0;
  }
}

async function loadTrainingTitle() {
  if (route.query.title || !Number.isFinite(trainingId.value)) {
    return;
  }

  try {
    const list = await fetchStudentTrainings();
    fallbackTitle.value = list.find((item) => item.id === trainingId.value)?.title || fallbackTitle.value;
  } catch {}
}

async function loadRooms(showLoading = false) {
  if (!Number.isFinite(trainingId.value)) {
    return;
  }

  if (showLoading) {
    loading.value = true;
  }

  try {
    rooms.value = await fetchTrainingRooms(trainingId.value, topicId.value);
    if (activeRoomId.value && !rooms.value.some((room) => room.roomId === activeRoomId.value)) {
      rooms.value.unshift(await fetchTrainingRoom(activeRoomId.value));
    }
  } catch {
    if (activeRoomId.value) {
      try {
        rooms.value = [await fetchTrainingRoom(activeRoomId.value)];
      } catch {
        rooms.value = [];
      }
    }
  } finally {
    loading.value = false;
  }
}

async function createRoom() {
  if (activeRoomId.value) {
    ElMessage.warning('您已在其他实训房间中，请退出后方可创建新房间');
    return;
  }

  creating.value = true;
  try {
    const room = await createTrainingRoom(trainingId.value, topicId.value);
    await router.push({ name: 'student-training-room-roles', params: { roomId: room.roomId } });
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '创建房间失败');
  } finally {
    creating.value = false;
  }
}

async function enterRoom(room: TrainingRoom) {
  if (isStarted(room)) {
    return;
  }

  joiningRoomId.value = room.roomId;
  try {
    const isMember = room.members?.some((member) => member.studentId === currentStudentId());
    if (!isMember) {
      await joinTrainingRoom(room.roomId);
    }
    await router.push({ name: 'student-training-room-roles', params: { roomId: room.roomId } });
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '进入房间失败');
  } finally {
    joiningRoomId.value = undefined;
  }
}

onMounted(async () => {
  await Promise.all([loadTrainingTitle(), loadRooms(true)]);
  pollTimer = window.setInterval(() => {
    void loadRooms();
  }, 3000);
});

onBeforeUnmount(() => {
  if (pollTimer !== undefined) {
    window.clearInterval(pollTimer);
  }
});
</script>

<style scoped>
.training-lobby-page {
  min-height: calc(100vh - 127px);
  color: #1e293b;
}

.training-lobby-breadcrumb {
  display: flex;
  align-items: center;
  gap: 4px;
  height: 18px;
  color: #64748b;
  font-size: 13px;
}

.training-lobby-breadcrumb button {
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  cursor: pointer;
  font: inherit;
}

.training-lobby-breadcrumb button:hover {
  color: #2563eb;
}

.training-lobby-breadcrumb .el-icon {
  color: #9ca3af;
  font-size: 12px;
}

.training-lobby-subject {
  min-height: 104px;
  margin-top: 24px;
  padding: 24px;
  box-sizing: border-box;
  border: 1px solid #f3f4f6;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.training-lobby-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.training-lobby-title-row h1 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
  font-weight: 700;
  line-height: 28px;
  letter-spacing: 0;
}

.training-lobby-title-row span {
  padding: 2px 10px;
  border-radius: 6px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 12px;
  line-height: 20px;
}

.training-lobby-subject p {
  margin: 4px 0 0;
  color: #94a3b8;
  font-size: 14px;
  line-height: 20px;
}

.training-lobby-create-row {
  display: flex;
  align-items: center;
  height: 72px;
}

.training-lobby-create-row .el-button {
  width: 99px;
  height: 40px;
  margin: 0;
  border-radius: 8px;
  background: #3b82f6;
  font-size: 14px;
}

.training-lobby-room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, 260px);
  gap: 16px;
}

.training-lobby-room-card {
  width: 260px;
  height: 170px;
  box-sizing: border-box;
  border: 1px solid #f3f4f6;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.training-lobby-room-card header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 20px 20px 12px;
}

.training-lobby-room-card h2 {
  overflow: hidden;
  margin: 0;
  color: #1e293b;
  font-size: 15px;
  font-weight: 600;
  line-height: 22px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.training-lobby-room-state {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 6px;
  background: #f0fdf4;
  color: #16a34a;
  font-size: 11px;
  line-height: 18px;
}

.training-lobby-room-state i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #22c55e;
}

.training-lobby-room-card > p {
  overflow: hidden;
  margin: 8px 20px 7px;
  color: #64748b;
  font-size: 12px;
  line-height: 20px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.training-lobby-room-card > .el-button {
  width: 220px;
  height: 40px;
  margin: 0 20px;
  border-color: #3b82f6;
  border-radius: 8px;
  background: #ffffff;
  color: #3b82f6;
  font-size: 14px;
  font-weight: 600;
}

.training-lobby-room-card > .el-button .el-icon {
  margin-left: 6px;
  font-size: 16px;
}

.training-lobby-room-card.is-started {
  background: #f9fafb;
  opacity: 0.6;
}

.training-lobby-room-card.is-started h2 {
  color: #94a3b8;
}

.training-lobby-room-card.is-started .training-lobby-room-state {
  background: #e5e7eb;
  color: #94a3b8;
}

.training-lobby-room-card.is-started .training-lobby-room-state i {
  background: #9ca3af;
}

.training-lobby-room-card.is-started > .el-button {
  border-color: #e5e7eb;
  background: #e5e7eb;
  color: #94a3b8;
}

.training-lobby-loading {
  display: grid;
  width: 260px;
  height: 170px;
  place-items: center;
  border: 1px dashed #e5e7eb;
  border-radius: 12px;
  color: #94a3b8;
  font-size: 13px;
}

.training-lobby-empty {
  display: flex;
  width: 100%;
  height: 165px;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 20px;
  overflow: hidden;
  text-align: center;
}

.training-lobby-empty strong {
  color: #64748b;
  font-size: 18px;
  font-weight: 600;
  line-height: 1.6;
}

.training-lobby-empty span {
  color: #94a3b8;
  font-size: 14px;
  line-height: 1.6;
}

@media (max-width: 640px) {
  .training-lobby-title-row {
    align-items: flex-start;
    flex-direction: column;
    gap: 6px;
  }

  .training-lobby-room-grid {
    grid-template-columns: 1fr;
  }

  .training-lobby-room-card,
  .training-lobby-loading {
    width: 100%;
  }

  .training-lobby-room-card > .el-button {
    width: calc(100% - 40px);
  }
}
</style>
