<template>
  <StudentShell eyebrow="实训中心" title="组队大厅">
    <section class="team-room-page">
      <button class="team-room-back" type="button" @click="router.push('/student/training')">
        <el-icon><ArrowLeft /></el-icon>
        返回实训中心
      </button>

      <div v-if="loading" class="student-loading">组队房间加载中...</div>

      <template v-else-if="room">
        <header class="team-room-header">
          <div>
            <p class="team-room-eyebrow">多人实训 · 组队大厅</p>
            <h1>{{ room.trainingName || '多人实训房间' }}</h1>
            <p class="team-room-subtitle">邀请同学加入房间，选择角色后开始实训</p>
          </div>
          <div class="team-room-header-actions">
            <span class="team-room-status" :class="`is-${roomStatusClass}`">
              <i></i>{{ roomStatusText }}
            </span>
            <el-button v-if="!isCurrentMember" type="primary" :loading="joining" @click="joinRoom">加入房间</el-button>
            <el-button plain :icon="Refresh" :loading="refreshing" @click="loadRoom">刷新房间</el-button>
          </div>
        </header>

        <section class="team-room-codebar">
          <div class="team-room-code-label">
            <span class="team-room-code-icon"><el-icon><Key /></el-icon></span>
            <div>
              <span>房间号</span>
              <strong>{{ room.roomCode || room.roomId }}</strong>
            </div>
          </div>
          <div class="team-room-code-actions">
            <span>将房间号发送给队友</span>
            <el-button text :icon="CopyDocument" @click="copyRoomCode">复制房间号</el-button>
          </div>
        </section>

        <div class="team-room-grid">
          <section class="team-room-panel team-room-members">
            <header class="team-room-panel-header">
              <div>
                <h2>组队成员</h2>
                <p>已加入 {{ members.length }} / {{ room.teamSize || members.length }} 人</p>
              </div>
              <el-tag type="info" effect="plain">等待队友</el-tag>
            </header>

            <div class="team-member-list">
              <article v-for="member in memberSlots" :key="member.key" class="team-member-item" :class="{ empty: member.empty }">
                <template v-if="!member.empty">
                  <span class="team-member-avatar">{{ member.studentName?.slice(0, 1) || '学' }}</span>
                  <div class="team-member-info">
                    <strong>{{ member.studentName || '学员' }}<em v-if="member.owner">房主</em></strong>
                    <span>{{ member.roleName || '尚未选择角色' }}</span>
                  </div>
                  <el-icon v-if="member.roleName" class="team-member-check"><CircleCheckFilled /></el-icon>
                </template>
                <template v-else>
                  <span class="team-member-avatar is-empty"><el-icon><Plus /></el-icon></span>
                  <div class="team-member-info"><strong>等待队友加入</strong><span>把房间号分享给队友</span></div>
                </template>
              </article>
            </div>
          </section>

          <section class="team-room-panel team-room-roles">
            <header class="team-room-panel-header">
              <div>
                <h2>实训角色</h2>
                <p>进入房间后选择本次实训岗位</p>
              </div>
            </header>
            <div class="team-role-grid">
              <button
                v-for="role in room.roles || []"
                :key="role.roleId"
                type="button"
                class="team-role-item"
                :class="{ claimed: role.claimed, mine: role.claimedByStudentId === currentStudentId }"
                @click="openRoleSelection"
              >
                <span class="team-role-icon"><el-icon><User /></el-icon></span>
                <strong>{{ role.roleName }}</strong>
                <span v-if="role.claimedByStudentId === currentStudentId" class="team-role-state">已选择</span>
                <span v-else-if="role.claimed" class="team-role-state">已被选择</span>
                <span v-else class="team-role-state">可选择</span>
              </button>
              <div v-if="!room.roles?.length" class="team-room-empty">暂无可选择的角色</div>
            </div>
            <div class="team-room-role-entry">
              <el-button type="primary" :disabled="room.roomStatus !== 'WAITING' || !isCurrentMember" @click="openRoleSelection">
                进入房间选择角色
                <el-icon><Right /></el-icon>
              </el-button>
            </div>
          </section>
        </div>

        <footer class="team-room-footer">
          <div class="team-room-hint"><el-icon><InfoFilled /></el-icon>未选择角色的成员将在开始后由 AI 扮演</div>
          <div class="team-room-footer-actions">
            <el-button plain @click="leaveRoom">退出房间</el-button>
            <el-button type="primary" :disabled="!isOwner || room.roomStatus !== 'WAITING'" @click="openStartDialog">
              开始实训
              <el-icon><Right /></el-icon>
            </el-button>
          </div>
        </footer>
      </template>

      <div v-else class="team-room-error">暂时无法获取房间信息，请返回实训中心重试</div>
    </section>

    <el-dialog
      v-model="startDialogVisible"
      class="training-start-dialog"
      width="520px"
      :show-close="false"
      destroy-on-close
      align-center
    >
      <div class="training-start-modal">
        <button class="training-start-close" type="button" aria-label="关闭弹窗" @click="startDialogVisible = false">
          <el-icon><Close /></el-icon>
        </button>

        <div class="training-start-icon">
          <el-icon><VideoPlay /></el-icon>
        </div>
        <h2>开始实训</h2>
        <p class="training-start-description">确认开始本次多人实训吗？开始后房间将进入实训状态。</p>

        <div class="training-start-summary">
          <div class="training-start-summary-row">
            <span>实训名称</span>
            <strong>{{ room?.trainingName || '多人实训' }}</strong>
          </div>
          <div class="training-start-summary-row">
            <span>房间号</span>
            <strong>{{ room?.roomCode || room?.roomId }}</strong>
          </div>
          <div class="training-start-summary-row">
            <span>参与人数</span>
            <strong>{{ members.length }} / {{ room?.teamSize || members.length }} 人</strong>
          </div>
        </div>

        <div class="training-start-notice">
          <el-icon><InfoFilled /></el-icon>
          <span>未选择角色的成员将在实训开始后由 AI 扮演，请确认队伍信息无误。</span>
        </div>

        <div class="training-start-actions">
          <el-button plain @click="startDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="starting" @click="startRoom">
            确认开始
            <el-icon><Right /></el-icon>
          </el-button>
        </div>
      </div>
    </el-dialog>
  </StudentShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  ArrowLeft,
  CircleCheckFilled,
  Close,
  CopyDocument,
  InfoFilled,
  Key,
  Plus,
  Refresh,
  Right,
  User,
  VideoPlay
} from '@element-plus/icons-vue';
import {
  fetchTrainingRoom,
  joinTrainingRoom,
  leaveTrainingRoom,
  startTrainingRoom,
  type TrainingRoom,
  type TrainingRoomMember
} from '../../api/student';
import StudentShell from '../../components/student/StudentShell.vue';

const route = useRoute();
const router = useRouter();
const room = ref<TrainingRoom>();
const loading = ref(false);
const refreshing = ref(false);
const joining = ref(false);
const starting = ref(false);
const startDialogVisible = ref(false);
const storedStudent = localStorage.getItem('jiaoxuepeiyu_student_user');
let currentStudentId = 0;
try {
  const user = storedStudent ? JSON.parse(storedStudent) as { id?: number; userId?: number; studentId?: number } : {};
  currentStudentId = Number(user.id ?? user.userId ?? user.studentId ?? 0);
} catch {
  currentStudentId = 0;
}

const members = computed(() => room.value?.members || []);
const memberSlots = computed<(Partial<TrainingRoomMember> & { key: string; empty?: boolean })[]>(() => {
  const size = Math.max(room.value?.teamSize || 0, members.value.length, 1);
  return Array.from({ length: size }, (_, index) => members.value[index] ? { ...members.value[index], key: `member-${index}` } : { key: `empty-${index}`, empty: true });
});
const isOwner = computed(() => Boolean(members.value.find((member) => member.owner)?.studentId === currentStudentId || room.value?.ownerStudentId === currentStudentId));
const isCurrentMember = computed(() => currentStudentId > 0 && members.value.some((member) => member.studentId === currentStudentId));
const roomStatusClass = computed(() => (room.value?.roomStatus || 'WAITING').toLowerCase());
const roomStatusText = computed(() => ({ WAITING: '等待组队', STARTED: '实训进行中', DISSOLVED: '房间已解散' }[room.value?.roomStatus || 'WAITING'] || '等待组队'));

async function loadRoom() {
  const roomId = Number(route.params.roomId);
  if (!Number.isFinite(roomId)) {
    return;
  }

  loading.value = !room.value;
  refreshing.value = Boolean(room.value);
  try {
    room.value = await fetchTrainingRoom(roomId);
    if (room.value.roomStatus === 'STARTED') {
      await router.replace({ name: 'student-training-start', params: { roomId: room.value.roomId }, query: { trainingId: room.value.trainingId } });
      return;
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '房间信息加载失败');
  } finally {
    loading.value = false;
    refreshing.value = false;
  }
}

function openRoleSelection() {
  if (room.value) {
    void router.push({ name: 'student-training-room-roles', params: { roomId: room.value.roomId } });
  }
}

async function joinRoom() {
  if (!room.value) return;
  joining.value = true;
  try {
    room.value = await joinTrainingRoom(room.value.roomId);
    ElMessage.success('已加入房间');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加入房间失败');
  } finally {
    joining.value = false;
  }
}

async function startRoom() {
  if (!room.value) return;
  starting.value = true;
  try {
    room.value = await startTrainingRoom(room.value.roomId);
    startDialogVisible.value = false;
    await router.push({ name: 'student-training-start', params: { roomId: room.value.roomId }, query: { trainingId: room.value.trainingId } });
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '开始实训失败');
  } finally {
    starting.value = false;
  }
}

function openStartDialog() {
  if (!room.value || !isOwner.value || room.value.roomStatus !== 'WAITING') {
    return;
  }

  startDialogVisible.value = true;
}

async function leaveRoom() {
  if (!room.value) return;
  try {
    await ElMessageBox.confirm('退出后将无法继续使用当前房间，确定退出吗？', '退出房间', { type: 'warning', confirmButtonText: '确定退出', cancelButtonText: '取消' });
    await leaveTrainingRoom(room.value.roomId);
    ElMessage.success('已退出房间');
    await router.push('/student/training');
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error instanceof Error ? error.message : '退出房间失败');
    }
  }
}

async function copyRoomCode() {
  const code = String(room.value?.roomCode || room.value?.roomId || '');
  try {
    await navigator.clipboard.writeText(code);
    ElMessage.success('房间号已复制');
  } catch {
    ElMessage.warning('复制失败，请手动记录房间号');
  }
}

onMounted(() => {
  void loadRoom();
});
</script>

<style scoped>
.team-room-page { min-height: calc(100vh - 120px); padding-bottom: 40px; }
.team-room-back { display: inline-flex; align-items: center; gap: 8px; margin: 4px 0 22px; border: 0; background: transparent; color: #64748b; cursor: pointer; font-size: 14px; }
.team-room-back:hover { color: #2563eb; }
.team-room-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; margin-bottom: 24px; }
.team-room-eyebrow { margin: 0 0 8px; color: #2563eb; font-size: 13px; font-weight: 700; }
.team-room-header h1 { margin: 0; color: #0f172a; font-size: 26px; line-height: 34px; }
.team-room-subtitle { margin: 8px 0 0; color: #94a3b8; font-size: 14px; }
.team-room-header-actions, .team-room-code-actions, .team-room-footer-actions { display: flex; align-items: center; gap: 12px; }
.team-room-status { display: inline-flex; align-items: center; gap: 7px; min-height: 30px; padding: 0 12px; border-radius: 7px; background: #eff6ff; color: #2563eb; font-size: 13px; font-weight: 700; }
.team-room-status i { width: 7px; height: 7px; border-radius: 50%; background: currentColor; }
.team-room-status.is-started { background: #ecfdf5; color: #059669; }
.team-room-status.is-dissolved { background: #f1f5f9; color: #64748b; }
.team-room-codebar { display: flex; align-items: center; justify-content: space-between; min-height: 84px; margin-bottom: 18px; padding: 18px 24px; border: 1px solid #dbeafe; border-radius: 10px; background: #f8fbff; }
.team-room-code-label { display: flex; align-items: center; gap: 14px; }
.team-room-code-icon { width: 42px; height: 42px; display: grid; place-items: center; border-radius: 10px; background: #dbeafe; color: #2563eb; font-size: 20px; }
.team-room-code-label div { display: grid; gap: 4px; }
.team-room-code-label span { color: #64748b; font-size: 13px; }
.team-room-code-label strong { color: #1e3a8a; font-size: 22px; letter-spacing: 1px; }
.team-room-code-actions span { color: #94a3b8; font-size: 13px; }
.team-room-grid { display: grid; grid-template-columns: minmax(300px, .92fr) minmax(420px, 1.08fr); gap: 18px; }
.team-room-panel { min-height: 360px; border: 1px solid #edf1f5; border-radius: 10px; background: #ffffff; box-shadow: 0 2px 8px rgba(16, 28, 54, .025); }
.team-room-panel-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; padding: 22px 24px 16px; border-bottom: 1px solid #f1f5f9; }
.team-room-panel-header h2 { margin: 0; color: #1e293b; font-size: 16px; }
.team-room-panel-header p { margin: 7px 0 0; color: #94a3b8; font-size: 13px; }
.team-member-list { display: grid; gap: 10px; padding: 18px 24px 24px; }
.team-member-item { display: flex; align-items: center; gap: 12px; min-height: 62px; padding: 10px 12px; border: 1px solid #edf1f5; border-radius: 8px; background: #fafbfc; }
.team-member-item.empty { border-style: dashed; background: #ffffff; }
.team-member-avatar { width: 36px; height: 36px; display: grid; flex: 0 0 36px; place-items: center; border-radius: 9px; background: #dbeafe; color: #2563eb; font-size: 15px; font-weight: 800; }
.team-member-avatar.is-empty { background: #f1f5f9; color: #94a3b8; }
.team-member-info { display: grid; min-width: 0; gap: 5px; }
.team-member-info strong { overflow: hidden; color: #334155; font-size: 14px; text-overflow: ellipsis; white-space: nowrap; }
.team-member-info strong em { margin-left: 8px; padding: 2px 5px; border-radius: 4px; background: #fff7ed; color: #ea580c; font-size: 11px; font-style: normal; font-weight: 700; }
.team-member-info span { color: #94a3b8; font-size: 12px; }
.team-member-check { margin-left: auto; color: #22c55e; font-size: 18px; }
.team-role-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; padding: 18px 24px 24px; }
.team-role-item { display: grid; grid-template-columns: 36px minmax(0, 1fr); align-items: center; column-gap: 10px; min-height: 74px; padding: 12px; border: 1px solid #dbeafe; border-radius: 8px; background: #f8fbff; color: #334155; cursor: pointer; text-align: left; }
.team-role-item:hover:not(:disabled), .team-role-item.mine { border-color: #60a5fa; background: #eff6ff; }
.team-role-item.claimed:not(.mine) { border-color: #e2e8f0; background: #f8fafc; color: #94a3b8; cursor: not-allowed; }
.team-role-icon { width: 36px; height: 36px; display: grid; grid-row: span 2; place-items: center; border-radius: 9px; background: #dbeafe; color: #2563eb; }
.team-role-item strong { overflow: hidden; font-size: 14px; text-overflow: ellipsis; white-space: nowrap; }
.team-role-state { color: #94a3b8; font-size: 12px; }
.team-role-item.mine .team-role-state { color: #2563eb; }
.team-room-role-entry { display: flex; justify-content: flex-end; padding: 0 24px 22px; }
.team-room-empty, .team-room-error { grid-column: 1 / -1; padding: 42px 10px; color: #94a3b8; font-size: 14px; text-align: center; }
.team-room-footer { display: flex; align-items: center; justify-content: space-between; gap: 18px; margin-top: 18px; padding: 18px 24px; border: 1px solid #edf1f5; border-radius: 10px; background: #ffffff; }
.team-room-hint { display: inline-flex; align-items: center; gap: 7px; color: #94a3b8; font-size: 13px; }
.team-room-hint .el-icon { color: #60a5fa; }
.team-room-error { min-height: 260px; display: grid; place-items: center; border: 1px solid #edf1f5; border-radius: 10px; background: #ffffff; }
.training-start-dialog :deep(.el-dialog) { overflow: hidden; border-radius: 12px; }
.training-start-dialog :deep(.el-dialog__header), .training-start-dialog :deep(.el-dialog__body) { padding: 0; }
.training-start-modal { position: relative; padding: 34px 36px 30px; background: #ffffff; }
.training-start-close { position: absolute; top: 18px; right: 18px; display: grid; width: 30px; height: 30px; place-items: center; border: 0; border-radius: 6px; background: transparent; color: #94a3b8; cursor: pointer; font-size: 18px; }
.training-start-close:hover { background: #f1f5f9; color: #475569; }
.training-start-icon { display: grid; width: 52px; height: 52px; margin: 0 auto 16px; place-items: center; border-radius: 14px; background: #eaf2ff; color: #2563eb; font-size: 26px; }
.training-start-modal h2 { margin: 0; color: #1e293b; font-size: 20px; line-height: 28px; text-align: center; }
.training-start-description { margin: 9px 0 22px; color: #94a3b8; font-size: 13px; line-height: 21px; text-align: center; }
.training-start-summary { overflow: hidden; border: 1px solid #edf1f5; border-radius: 8px; background: #fafbfc; }
.training-start-summary-row { display: flex; align-items: center; justify-content: space-between; gap: 20px; min-height: 44px; padding: 0 16px; border-bottom: 1px solid #edf1f5; }
.training-start-summary-row:last-child { border-bottom: 0; }
.training-start-summary-row span { color: #94a3b8; font-size: 13px; }
.training-start-summary-row strong { overflow: hidden; color: #334155; font-size: 13px; font-weight: 600; text-align: right; text-overflow: ellipsis; white-space: nowrap; }
.training-start-notice { display: flex; align-items: flex-start; gap: 8px; margin-top: 14px; padding: 11px 12px; border-radius: 6px; background: #f8fbff; color: #64748b; font-size: 12px; line-height: 19px; }
.training-start-notice .el-icon { flex: 0 0 auto; margin-top: 2px; color: #60a5fa; }
.training-start-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 24px; }
@media (max-width: 640px) {
  .training-start-dialog { width: calc(100% - 28px) !important; }
  .training-start-modal { padding: 30px 20px 22px; }
}
@media (max-width: 900px) { .team-room-grid { grid-template-columns: 1fr; } }
@media (max-width: 640px) { .team-room-header, .team-room-codebar, .team-room-footer { align-items: stretch; flex-direction: column; } .team-room-header-actions, .team-room-code-actions, .team-room-footer-actions { justify-content: space-between; } .team-room-code-actions { align-items: center; } .team-role-grid { grid-template-columns: 1fr; } .team-room-header h1 { font-size: 22px; } }
</style>
