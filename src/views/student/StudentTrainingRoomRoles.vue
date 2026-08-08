<template>
  <StudentShell eyebrow="实训中心" title="角色选择">
    <section class="team-roles-page">
      <button class="team-role-back" type="button" @click="router.push({ name: 'student-training-room', params: { roomId: roomId } })">
        <el-icon><ArrowLeft /></el-icon>
        返回组队大厅
      </button>

      <div v-if="loading" class="student-loading">角色信息加载中...</div>
      <template v-else-if="room">
        <header class="team-roles-head">
          <div>
            <p>多人实训 · 进入房间</p>
            <h1>选择实训角色</h1>
            <span>{{ room.trainingName || '多人实训' }} · 房间号 {{ room.roomCode || room.roomId }}</span>
          </div>
          <div class="team-roles-head-state">
            <span class="team-role-view-tag"><el-icon><User /></el-icon>学员视角</span>
            <el-button plain :icon="Refresh" :loading="refreshing" @click="loadRoom">刷新</el-button>
          </div>
        </header>

        <section class="team-roles-layout">
          <article class="team-roles-main-panel">
            <header class="team-roles-panel-title">
              <div>
                <h2>请选择你的实训岗位</h2>
                <p>已选择的角色将由对应成员执行，其他空缺岗位由 AI 补位</p>
              </div>
              <span>{{ claimedRoleCount }} / {{ room.roles?.length || 0 }} 已选择</span>
            </header>
            <div class="team-roles-cards">
              <button
                v-for="role in room.roles || []"
                :key="role.roleId"
                type="button"
                class="team-role-select-card"
                :class="{ selected: role.claimedByStudentId === currentStudentId, occupied: role.claimed && role.claimedByStudentId !== currentStudentId }"
                :disabled="room.roomStatus !== 'WAITING' || Boolean(role.claimed && role.claimedByStudentId !== currentStudentId) || !isCurrentMember"
                @click="selectRole(role.roleId)"
              >
                <span class="team-role-select-icon"><el-icon><User /></el-icon></span>
                <span class="team-role-select-text"><strong>{{ role.roleName }}</strong><small>{{ role.claimed ? (role.claimedByStudentId === currentStudentId ? '当前选择' : '队友已选择') : '空闲岗位' }}</small></span>
                <el-icon v-if="role.claimedByStudentId === currentStudentId" class="team-role-selected-icon"><CircleCheckFilled /></el-icon>
                <span v-else-if="role.claimed" class="team-role-occupied">已占用</span>
                <el-icon v-else class="team-role-arrow"><ArrowRight /></el-icon>
              </button>
              <div v-if="!room.roles?.length" class="team-roles-empty">暂无角色配置</div>
            </div>
          </article>

          <aside class="team-roles-side-panel">
            <header class="team-roles-panel-title compact">
              <div><h2>队伍成员</h2><p>管理员可查看当前角色分配</p></div>
              <span>{{ members.length }} / {{ room.teamSize || members.length }}</span>
            </header>
            <div class="team-roles-member-list">
              <div v-for="member in memberSlots" :key="member.key" class="team-roles-member" :class="{ empty: member.empty }">
                <span class="team-roles-member-avatar">{{ member.empty ? '+' : member.studentName?.slice(0, 1) || '学' }}</span>
                <div><strong>{{ member.empty ? '等待队友加入' : member.studentName || '学员' }}<em v-if="member.owner">房主</em></strong><span>{{ member.empty ? '未加入' : member.roleName || '未选择角色' }}</span></div>
                <el-icon v-if="!member.empty && member.roleName" class="team-roles-member-ok"><CircleCheckFilled /></el-icon>
              </div>
            </div>
            <div class="team-roles-ai-note"><el-icon><InfoFilled /></el-icon><span>开始实训时，未选择的岗位会自动由 AI 扮演。</span></div>
          </aside>
        </section>

        <footer class="team-roles-footer">
          <div><strong>{{ selectedRoleName || '尚未选择角色' }}</strong><span>{{ selectedRoleName ? '你将以此角色进入实训' : '请选择一个空闲岗位' }}</span></div>
          <div class="team-roles-footer-actions">
            <el-button plain @click="router.push({ name: 'student-training-room', params: { roomId } })">返回大厅</el-button>
            <el-button type="primary" :disabled="!selectedRoleName || room.roomStatus !== 'WAITING'" @click="enterRoom">确认进入房间<el-icon><Right /></el-icon></el-button>
          </div>
        </footer>
      </template>
      <div v-else class="team-roles-error">暂时无法获取角色信息，请返回组队大厅重试</div>
    </section>
  </StudentShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft, ArrowRight, CircleCheckFilled, InfoFilled, Refresh, Right, User } from '@element-plus/icons-vue';
import { claimTrainingRoomRole, fetchTrainingRoom, type TrainingRoom, type TrainingRoomMember } from '../../api/student';
import StudentShell from '../../components/student/StudentShell.vue';

const route = useRoute();
const router = useRouter();
const roomId = Number(route.params.roomId);
const room = ref<TrainingRoom>();
const loading = ref(false);
const refreshing = ref(false);
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
const isCurrentMember = computed(() => currentStudentId > 0 && members.value.some((member) => member.studentId === currentStudentId));
const selectedRoleName = computed(() => room.value?.roles?.find((role) => role.claimedByStudentId === currentStudentId)?.roleName);
const claimedRoleCount = computed(() => room.value?.roles?.filter((role) => role.claimed)?.length || 0);

async function loadRoom() {
  if (!Number.isFinite(roomId)) return;
  loading.value = !room.value;
  refreshing.value = Boolean(room.value);
  try {
    room.value = await fetchTrainingRoom(roomId);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '角色信息加载失败');
  } finally {
    loading.value = false;
    refreshing.value = false;
  }
}

async function selectRole(roleId: number) {
  if (!room.value || !isCurrentMember.value) return;
  try {
    room.value = await claimTrainingRoomRole(room.value.roomId, roleId);
    ElMessage.success('角色选择成功');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '角色选择失败');
  }
}

function enterRoom() {
  ElMessage.success(`已选择${selectedRoleName.value}，等待房主开始实训`);
  void router.push({ name: 'student-training-room', params: { roomId } });
}

onMounted(() => {
  void loadRoom();
});
</script>

<style scoped>
.team-roles-page { min-height: calc(100vh - 120px); padding-bottom: 40px; }
.team-role-back { display: inline-flex; align-items: center; gap: 8px; margin: 4px 0 22px; border: 0; background: transparent; color: #64748b; cursor: pointer; font-size: 14px; }
.team-role-back:hover { color: #2563eb; }
.team-roles-head { display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; margin-bottom: 24px; }
.team-roles-head p { margin: 0 0 8px; color: #2563eb; font-size: 13px; font-weight: 700; }
.team-roles-head h1 { margin: 0; color: #0f172a; font-size: 26px; line-height: 34px; }
.team-roles-head > div:first-child > span { display: block; margin-top: 8px; color: #94a3b8; font-size: 14px; }
.team-roles-head-state, .team-roles-footer-actions { display: flex; align-items: center; gap: 12px; }
.team-role-view-tag { display: inline-flex; align-items: center; gap: 6px; min-height: 30px; padding: 0 11px; border-radius: 7px; background: #eff6ff; color: #2563eb; font-size: 13px; font-weight: 700; }
.team-roles-layout { display: grid; grid-template-columns: minmax(0, 1.35fr) minmax(300px, .65fr); gap: 18px; }
.team-roles-main-panel, .team-roles-side-panel { min-height: 410px; border: 1px solid #edf1f5; border-radius: 10px; background: #ffffff; box-shadow: 0 2px 8px rgba(16, 28, 54, .025); }
.team-roles-panel-title { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; padding: 22px 24px 18px; border-bottom: 1px solid #f1f5f9; }
.team-roles-panel-title.compact { padding-bottom: 16px; }
.team-roles-panel-title h2 { margin: 0; color: #1e293b; font-size: 16px; }
.team-roles-panel-title p { margin: 7px 0 0; color: #94a3b8; font-size: 13px; }
.team-roles-panel-title > span { flex: 0 0 auto; color: #64748b; font-size: 13px; }
.team-roles-cards { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; padding: 22px 24px 26px; }
.team-role-select-card { display: flex; align-items: center; gap: 12px; min-height: 90px; padding: 14px; border: 1px solid #dbeafe; border-radius: 9px; background: #f8fbff; color: #334155; cursor: pointer; text-align: left; }
.team-role-select-card:hover:not(:disabled), .team-role-select-card.selected { border-color: #3b82f6; background: #eff6ff; }
.team-role-select-card.occupied { border-color: #e2e8f0; background: #f8fafc; color: #94a3b8; cursor: not-allowed; }
.team-role-select-icon { width: 42px; height: 42px; display: grid; flex: 0 0 42px; place-items: center; border-radius: 10px; background: #dbeafe; color: #2563eb; font-size: 20px; }
.team-role-select-text { display: grid; min-width: 0; gap: 6px; }
.team-role-select-text strong { overflow: hidden; font-size: 14px; text-overflow: ellipsis; white-space: nowrap; }
.team-role-select-text small { color: #94a3b8; font-size: 12px; }
.team-role-selected-icon { margin-left: auto; color: #2563eb; font-size: 20px; }
.team-role-occupied { margin-left: auto; color: #94a3b8; font-size: 12px; white-space: nowrap; }
.team-role-arrow { margin-left: auto; color: #93c5fd; }
.team-roles-empty, .team-roles-error { grid-column: 1 / -1; min-height: 180px; display: grid; place-items: center; color: #94a3b8; font-size: 14px; }
.team-roles-member-list { display: grid; gap: 10px; padding: 18px 20px; }
.team-roles-member { display: flex; align-items: center; gap: 10px; min-height: 58px; padding: 8px 10px; border: 1px solid #edf1f5; border-radius: 8px; background: #fafbfc; }
.team-roles-member.empty { border-style: dashed; background: #ffffff; }
.team-roles-member-avatar { width: 34px; height: 34px; display: grid; flex: 0 0 34px; place-items: center; border-radius: 8px; background: #dbeafe; color: #2563eb; font-size: 14px; font-weight: 800; }
.team-roles-member.empty .team-roles-member-avatar { background: #f1f5f9; color: #94a3b8; }
.team-roles-member > div { display: grid; min-width: 0; gap: 4px; }
.team-roles-member strong { overflow: hidden; color: #334155; font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
.team-roles-member strong em { margin-left: 6px; color: #ea580c; font-size: 10px; font-style: normal; }
.team-roles-member > div span { color: #94a3b8; font-size: 12px; }
.team-roles-member-ok { margin-left: auto; color: #22c55e; }
.team-roles-ai-note { display: flex; align-items: flex-start; gap: 8px; margin: 0 20px 20px; padding: 11px 12px; border-radius: 7px; background: #fff7ed; color: #c2410c; font-size: 12px; line-height: 18px; }
.team-roles-footer { display: flex; align-items: center; justify-content: space-between; gap: 18px; margin-top: 18px; padding: 18px 24px; border: 1px solid #edf1f5; border-radius: 10px; background: #ffffff; }
.team-roles-footer > div:first-child { display: grid; gap: 6px; }
.team-roles-footer strong { color: #334155; font-size: 14px; }
.team-roles-footer > div:first-child span { color: #94a3b8; font-size: 12px; }
@media (max-width: 900px) { .team-roles-layout { grid-template-columns: 1fr; } }
@media (max-width: 640px) { .team-roles-head, .team-roles-footer { align-items: stretch; flex-direction: column; } .team-roles-head-state, .team-roles-footer-actions { justify-content: space-between; } .team-roles-cards { grid-template-columns: 1fr; } .team-roles-head h1 { font-size: 22px; } }
</style>
