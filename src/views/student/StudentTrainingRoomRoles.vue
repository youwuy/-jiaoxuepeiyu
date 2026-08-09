<template>
  <StudentShell eyebrow="实训中心" title="角色选择">
    <section class="role-select-page">
      <nav class="role-select-breadcrumb" aria-label="面包屑导航">
        <button type="button" @click="router.push('/student/training')">实训中心</button>
        <el-icon><ArrowRight /></el-icon>
        <button type="button" @click="returnLobby">组队大厅</button>
        <el-icon><ArrowRight /></el-icon>
        <span>角色选择</span>
      </nav>

      <div v-if="loading" class="student-loading">角色信息加载中...</div>

      <template v-else-if="room">
        <header class="role-room-summary">
          <h1>{{ roomTitle }}</h1>
          <div class="role-room-status">
            <span><i></i>{{ roomStatusText }}</span>
            <small>{{ room.roomStatus === 'WAITING' ? '等待队员加入' : '实训进行中' }}</small>
          </div>
        </header>

        <div class="role-select-heading">
          <h2><el-icon><User /></el-icon>选择你的角色</h2>
          <p>每个角色仅限一人选择，选定后不可更改</p>
        </div>

        <section class="role-card-grid">
          <article
            v-for="(role, index) in room.roles || []"
            :key="role.roleId"
            class="role-choice-card"
            :class="[
              `is-tone-${index % 3}`,
              {
                'is-occupied': isOccupiedByOther(role),
                'is-selected': role.claimedByStudentId === currentStudentId,
                'is-locked': Boolean(selectedRoleName && role.claimedByStudentId !== currentStudentId)
              }
            ]"
          >
            <span class="role-card-accent"></span>
            <div class="role-card-body">
              <span class="role-card-avatar">
                <span><el-icon><User /></el-icon></span>
                <i><el-icon><CircleCheckFilled /></el-icon></i>
              </span>
              <h3>{{ role.roleName }}</h3>

              <div v-if="isOccupiedByOther(role)" class="role-card-occupied-text">
                已被{{ roleOwnerName(role.claimedByStudentId) }}选择
              </div>
              <el-button
                v-else
                :disabled="!canSelectRole(role)"
                :loading="claimingRoleId === role.roleId"
                @click="selectRole(role.roleId)"
              >
                {{ role.claimedByStudentId === currentStudentId ? '已选择' : '选择此角色' }}
              </el-button>
            </div>
          </article>

          <div v-if="!room.roles?.length" class="role-card-empty">暂无可选择的角色</div>
        </section>

        <footer class="role-select-footer">
          <div class="role-select-tip" :class="{ 'is-ready': Boolean(selectedRoleName) }">
            <span><el-icon><WarningFilled /></el-icon></span>
            <strong>{{ selectedRoleName ? `已选择角色：${selectedRoleName}` : '请先选择一个角色' }}</strong>
          </div>
          <div class="role-select-actions">
            <el-button @click="returnLobby">返回</el-button>
            <el-button
              v-if="isOwner"
              type="primary"
              :disabled="!selectedRoleName || room.roomStatus !== 'WAITING'"
              :loading="starting"
              @click="startRoom"
            >
              开始实训
            </el-button>
            <el-button v-else type="primary" disabled>等待房主开始</el-button>
          </div>
        </footer>
      </template>

      <div v-else class="role-select-error">暂时无法获取角色信息，请返回组队大厅重试</div>
    </section>
  </StudentShell>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { ArrowRight, CircleCheckFilled, User, WarningFilled } from '@element-plus/icons-vue';
import { useRoute, useRouter } from 'vue-router';
import {
  claimTrainingRoomRole,
  fetchTrainingRoom,
  startTrainingRoom,
  type TrainingRoom,
  type TrainingRoomRole
} from '../../api/student';
import StudentShell from '../../components/student/StudentShell.vue';

const route = useRoute();
const router = useRouter();
const roomId = Number(route.params.roomId);
const room = ref<TrainingRoom>();
const loading = ref(true);
const claimingRoleId = ref<number>();
const starting = ref(false);
let pollTimer: number | undefined;

const storedStudent = localStorage.getItem('jiaoxuepeiyu_student_user');
let currentStudentId = 0;
try {
  const user = storedStudent ? JSON.parse(storedStudent) as { id?: number; userId?: number; studentId?: number } : {};
  currentStudentId = Number(user.id ?? user.userId ?? user.studentId ?? 0);
} catch {
  currentStudentId = 0;
}

const members = computed(() => room.value?.members || []);
const isOwner = computed(() => room.value?.ownerStudentId === currentStudentId || members.value.some((member) => member.owner && member.studentId === currentStudentId));
const selectedRoleName = computed(() => room.value?.roles?.find((role) => role.claimedByStudentId === currentStudentId)?.roleName);
const roomOwnerName = computed(() => members.value.find((member) => member.owner)?.studentName || '学员');
const roomTitle = computed(() => `${room.value?.trainingName || '多人实训'}-${roomOwnerName.value}创建的房间`);
const roomStatusText = computed(() => room.value?.roomStatus === 'STARTED' ? '已开始' : '未开始');

function roleOwnerName(studentId?: number) {
  return members.value.find((member) => member.studentId === studentId)?.studentName || '其他学员';
}

function isOccupiedByOther(role: TrainingRoomRole) {
  return Boolean(role.claimed && role.claimedByStudentId !== currentStudentId);
}

function canSelectRole(role: TrainingRoomRole) {
  if (room.value?.roomStatus !== 'WAITING' || isOccupiedByOther(role)) {
    return false;
  }

  return !selectedRoleName.value || role.claimedByStudentId === currentStudentId;
}

async function loadRoom(showLoading = false) {
  if (!Number.isFinite(roomId)) {
    loading.value = false;
    return;
  }

  if (showLoading) {
    loading.value = true;
  }

  try {
    room.value = await fetchTrainingRoom(roomId);
    if (room.value.roomStatus === 'STARTED') {
      await router.replace({
        name: 'student-training-start',
        params: { roomId: room.value.roomId },
        query: { trainingId: room.value.trainingId }
      });
    }
  } catch (error) {
    if (showLoading) {
      ElMessage.error(error instanceof Error ? error.message : '角色信息加载失败');
    }
  } finally {
    loading.value = false;
  }
}

async function selectRole(roleId: number) {
  const targetRole = room.value?.roles?.find((role) => role.roleId === roleId);
  if (!room.value || !targetRole || !canSelectRole(targetRole)) {
    return;
  }

  claimingRoleId.value = roleId;
  try {
    room.value = await claimTrainingRoomRole(room.value.roomId, roleId);
    ElMessage.success('角色选择成功');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '角色选择失败');
  } finally {
    claimingRoleId.value = undefined;
  }
}

async function startRoom() {
  if (!room.value || !isOwner.value || !selectedRoleName.value) {
    return;
  }

  starting.value = true;
  try {
    room.value = await startTrainingRoom(room.value.roomId);
    await router.push({
      name: 'student-training-start',
      params: { roomId: room.value.roomId },
      query: { trainingId: room.value.trainingId }
    });
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '开始实训失败');
  } finally {
    starting.value = false;
  }
}

function returnLobby() {
  if (!room.value) {
    void router.push('/student/training');
    return;
  }

  void router.push({
    name: 'student-training-lobby',
    params: { trainingId: room.value.trainingId },
    query: {
      title: room.value.trainingName,
      activeRoomId: String(room.value.roomId)
    }
  });
}

onMounted(async () => {
  await loadRoom(true);
  pollTimer = window.setInterval(() => {
    void loadRoom();
  }, 3000);
});

onBeforeUnmount(() => {
  if (pollTimer !== undefined) {
    window.clearInterval(pollTimer);
  }
});
</script>

<style scoped>
.role-select-page {
  min-height: calc(100vh - 127px);
  color: #1e293b;
}

.role-select-breadcrumb {
  display: flex;
  align-items: center;
  gap: 4px;
  height: 18px;
  color: #64748b;
  font-size: 13px;
}

.role-select-breadcrumb button {
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  cursor: pointer;
  font: inherit;
}

.role-select-breadcrumb button:hover {
  color: #2563eb;
}

.role-select-breadcrumb .el-icon {
  color: #9ca3af;
  font-size: 12px;
}

.role-room-summary {
  min-height: 102px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-top: 24px;
  padding: 24px;
  box-sizing: border-box;
  border: 1px solid #f3f4f6;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.role-room-summary h1 {
  overflow: hidden;
  margin: 0;
  color: #0f172a;
  font-size: 20px;
  font-weight: 700;
  line-height: 28px;
  letter-spacing: 0;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.role-room-status {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 12px;
}

.role-room-status > span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 2px 8px;
  border-radius: 6px;
  background: #f0fdf4;
  color: #16a34a;
  font-size: 11px;
  line-height: 18px;
}

.role-room-status i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #22c55e;
}

.role-room-status small {
  color: #94a3b8;
  font-size: 12px;
}

.role-select-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-top: 32px;
}

.role-select-heading h2 {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: #1e293b;
  font-size: 18px;
  line-height: 26px;
}

.role-select-heading h2 .el-icon {
  color: #2563eb;
  font-size: 20px;
}

.role-select-heading p {
  margin: 0;
  color: #94a3b8;
  font-size: 13px;
}

.role-card-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
  min-height: 292px;
  margin-top: 16px;
}

.role-choice-card {
  position: relative;
  overflow: hidden;
  min-width: 0;
  height: 292px;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.role-choice-card.is-tone-1 {
  border: 2px solid #60a5fa;
  box-shadow: 0 4px 24px rgba(59, 130, 246, 0.15);
}

.role-card-accent {
  position: absolute;
  top: 0;
  right: 0;
  left: 0;
  height: 6px;
  background: linear-gradient(90deg, #3b82f6, #6366f1);
}

.role-choice-card.is-tone-2 .role-card-accent {
  background: linear-gradient(90deg, #10b981, #14b8a6);
}

.role-choice-card.is-occupied,
.role-choice-card.is-locked {
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  box-shadow: none;
}

.role-choice-card.is-occupied .role-card-accent,
.role-choice-card.is-locked .role-card-accent {
  background: #d1d5db;
}

.role-card-body {
  display: flex;
  height: 100%;
  align-items: center;
  flex-direction: column;
  padding: 32px 24px 24px;
  box-sizing: border-box;
}

.role-card-avatar {
  position: relative;
  display: grid;
  width: 96px;
  height: 96px;
  place-items: center;
  border-radius: 50%;
  background: #eff6ff;
}

.role-card-avatar > span {
  display: grid;
  width: 80px;
  height: 80px;
  place-items: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  color: #3b82f6;
  font-size: 38px;
}

.is-tone-2 .role-card-avatar {
  background: #ecfdf5;
}

.is-tone-2 .role-card-avatar > span {
  background: linear-gradient(135deg, #d1fae5, #a7f3d0);
  color: #10b981;
}

.role-card-avatar > i {
  position: absolute;
  right: 0;
  bottom: 0;
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  border: 3px solid #ffffff;
  border-radius: 50%;
  background: #3b82f6;
  color: #ffffff;
  font-size: 17px;
}

.is-tone-2 .role-card-avatar > i {
  background: #10b981;
}

.role-choice-card.is-occupied .role-card-avatar,
.role-choice-card.is-locked .role-card-avatar {
  background: #e5e7eb;
}

.role-choice-card.is-occupied .role-card-avatar > span,
.role-choice-card.is-locked .role-card-avatar > span {
  background: #d1d5db;
  color: #9ca3af;
}

.role-choice-card.is-occupied .role-card-avatar > i,
.role-choice-card.is-locked .role-card-avatar > i {
  background: #9ca3af;
}

.role-choice-card h3 {
  margin: 16px 0 20px;
  color: #1e293b;
  font-size: 20px;
  font-weight: 700;
  line-height: 28px;
}

.role-choice-card.is-occupied h3,
.role-choice-card.is-locked h3 {
  color: #94a3b8;
}

.role-card-occupied-text,
.role-choice-card .el-button {
  width: 100%;
  height: 48px;
  box-sizing: border-box;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
}

.role-card-occupied-text {
  display: grid;
  place-items: center;
  padding: 0 16px;
  background: #f3f4f6;
  color: #94a3b8;
  font-size: 14px;
}

.role-choice-card .el-button {
  border: 0;
  background: linear-gradient(90deg, #3b82f6, #2563eb);
  color: #ffffff;
}

.role-choice-card.is-tone-2 .el-button {
  background: linear-gradient(90deg, #10b981, #059669);
}

.role-choice-card.is-selected .el-button,
.role-choice-card.is-locked .el-button {
  background: #e5e7eb;
  color: #94a3b8;
}

.role-card-empty,
.role-select-error {
  grid-column: 1 / -1;
  display: grid;
  min-height: 220px;
  place-items: center;
  color: #94a3b8;
  font-size: 14px;
}

.role-select-footer {
  display: flex;
  min-height: 84px;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-top: 32px;
  padding: 20px;
  box-sizing: border-box;
  border: 1px solid #f3f4f6;
  border-radius: 12px;
  background: #ffffff;
}

.role-select-tip {
  display: flex;
  align-items: center;
  gap: 12px;
}

.role-select-tip > span {
  display: grid;
  width: 40px;
  height: 40px;
  flex: 0 0 40px;
  place-items: center;
  border-radius: 10px;
  background: #fffbeb;
  color: #f59e0b;
  font-size: 20px;
}

.role-select-tip.is-ready > span {
  background: #ecfdf5;
  color: #10b981;
}

.role-select-tip strong {
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
}

.role-select-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.role-select-actions .el-button {
  height: 44px;
  margin: 0;
  padding: 0 24px;
  border-color: #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
  color: #64748b;
}

.role-select-actions .el-button--primary {
  min-width: 112px;
  padding: 0 32px;
  border-color: #2563eb;
  background: #2563eb;
  color: #ffffff;
}

.role-select-actions .el-button--primary.is-disabled {
  border-color: #d1d5db;
  background: #d1d5db;
  color: #9ca3af;
}

@media (max-width: 960px) {
  .role-card-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .role-room-summary,
  .role-select-heading,
  .role-select-footer {
    align-items: flex-start;
    flex-direction: column;
  }

  .role-room-summary h1 {
    white-space: normal;
  }

  .role-card-grid {
    grid-template-columns: 1fr;
  }

  .role-select-actions {
    width: 100%;
  }

  .role-select-actions .el-button {
    flex: 1;
  }
}
</style>
