<template>
  <StudentShell eyebrow="实训中心" title="开始实训">
    <section class="training-start-page" :class="{ 'is-running': phase === 'running' }">
      <div v-if="phase === 'loading'" class="training-start-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在准备实训环境</span>
      </div>

      <template v-else>
        <header class="training-start-header">
          <button class="training-start-back" type="button" @click="backToTraining">
            <el-icon><ArrowLeft /></el-icon>
            返回实训中心
          </button>
          <span class="training-start-status"><i></i>{{ phase === 'countdown' ? '即将开始' : '实训进行中' }}</span>
        </header>

        <main v-if="phase === 'countdown'" class="training-countdown-stage">
          <div class="training-countdown-orbit training-countdown-orbit-left"></div>
          <div class="training-countdown-orbit training-countdown-orbit-right"></div>
          <div class="training-countdown-content">
            <p class="training-start-eyebrow">{{ task.trainingName || room?.trainingName || '多人实训' }}</p>
            <h1>准备进入实训</h1>
            <p class="training-countdown-caption">请确认你的实训角色，系统即将为你加载实训场景</p>

            <div class="training-role-summary">
              <span class="training-role-avatar"><el-icon><UserFilled /></el-icon></span>
              <div>
                <span>本次实训角色</span>
                <strong>{{ task.roleName || currentRole || '待分配角色' }}</strong>
              </div>
            </div>

            <div class="training-countdown-number" :key="countdown">{{ countdown }}</div>
            <p class="training-countdown-tip">{{ countdown > 0 ? '即将开始，请做好准备' : '正在进入实训' }}</p>

            <div v-if="aiRoleNames.length" class="training-ai-roles">
              <el-icon><Cpu /></el-icon>
              <span>AI 扮演：{{ aiRoleNames.join('、') }}</span>
            </div>
          </div>
        </main>

        <main v-else class="training-workspace">
          <header class="training-workspace-title">
            <div>
              <p class="training-start-eyebrow">{{ task.trainingName || room?.trainingName || '多人实训' }}</p>
              <h1>实训进行中</h1>
            </div>
            <div class="training-workspace-role">
              <span class="training-role-avatar small"><el-icon><UserFilled /></el-icon></span>
              <div><span>当前角色</span><strong>{{ task.roleName || currentRole || '待分配角色' }}</strong></div>
            </div>
          </header>

          <section class="training-workspace-content">
            <div class="training-workspace-placeholder">
              <span class="training-workspace-icon"><el-icon><Monitor /></el-icon></span>
              <h2>实训场景已准备就绪</h2>
              <p>请打开本地三维实训程序开始操作，系统会同步记录你的实训过程。</p>
              <el-button type="primary" :loading="launchLoading" @click="notifyLaunch">进入三维实训</el-button>
            </div>
            <aside class="training-workspace-info">
              <h2>实训信息</h2>
              <dl>
                <div><dt>房间号</dt><dd>{{ task.roomCode || room?.roomCode || room?.roomId || '-' }}</dd></div>
                <div><dt>参与人数</dt><dd>{{ room?.members?.length || task.teamSize || '-' }} / {{ room?.teamSize || task.teamSize || '-' }} 人</dd></div>
                <div><dt>角色</dt><dd>{{ task.roleName || currentRole || '待分配角色' }}</dd></div>
              </dl>
              <div v-if="aiRoleNames.length" class="training-workspace-ai"><el-icon><Cpu /></el-icon><span>AI 扮演：{{ aiRoleNames.join('、') }}</span></div>
            </aside>
          </section>
        </main>
      </template>
    </section>
  </StudentShell>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft, Cpu, Loading, Monitor, UserFilled } from '@element-plus/icons-vue';
import {
  createUeLaunchSession,
  fetchStudentTrainingTask,
  fetchTrainingRoom,
  type StudentTrainingTask,
  type TrainingRoom,
  type UeLaunchSession
} from '../../api/student';
import { resolveApiBaseUrl } from '../../api/http';
import StudentShell from '../../components/student/StudentShell.vue';

type StartPhase = 'loading' | 'countdown' | 'running';

const route = useRoute();
const router = useRouter();
const phase = ref<StartPhase>('loading');
const countdown = ref(3);
const room = ref<TrainingRoom>();
const task = ref<StudentTrainingTask>({ trainingId: Number(route.query.trainingId) || 0 });
const launchLoading = ref(false);
let countdownTimer: number | undefined;

const currentStudentId = computed(() => {
  const storedStudent = localStorage.getItem('jiaoxuepeiyu_student_user');
  try {
    const user = storedStudent ? JSON.parse(storedStudent) as { id?: number; userId?: number; studentId?: number } : {};
    return Number(user.id ?? user.userId ?? user.studentId ?? 0);
  } catch {
    return 0;
  }
});
const currentRole = computed(() => room.value?.members?.find((member) => member.studentId === currentStudentId.value)?.roleName);
const aiRoleNames = computed(() => task.value.aiRoleNames || room.value?.roles?.filter((role) => !role.claimed).map((role) => role.roleName) || []);

async function loadStartData() {
  const roomId = Number(route.params.roomId);
  if (!Number.isFinite(roomId)) {
    phase.value = 'countdown';
    beginCountdown();
    return;
  }

  try {
    room.value = await fetchTrainingRoom(roomId);
    const trainingId = Number(route.query.trainingId) || room.value.trainingId;
    const topicId = room.value.topicId || Number(route.query.topicId);
    if (Number.isFinite(trainingId) && trainingId > 0 && Number.isFinite(topicId) && topicId > 0) {
      try {
        task.value = await fetchStudentTrainingTask(trainingId, topicId);
      } catch {
        task.value = { trainingId, trainingName: room.value.trainingName, roomId: room.value.roomId, roomCode: room.value.roomCode, teamSize: room.value.teamSize };
      }
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '实训信息加载失败');
  } finally {
    phase.value = 'countdown';
    beginCountdown();
  }
}

function beginCountdown() {
  window.clearInterval(countdownTimer);
  countdownTimer = window.setInterval(() => {
    if (countdown.value <= 1) {
      window.clearInterval(countdownTimer);
      countdown.value = 0;
      window.setTimeout(() => { phase.value = 'running'; }, 450);
      return;
    }
    countdown.value -= 1;
  }, 1000);
}

function backToTraining() {
  void router.push('/student/training');
}

async function notifyLaunch() {
  if (launchLoading.value) {
    return;
  }

  const trainingId = Number(route.query.trainingId) || room.value?.trainingId || task.value.trainingId;
  if (!Number.isFinite(trainingId) || trainingId <= 0) {
    ElMessage.error('实训信息不完整，无法启动三维实训');
    return;
  }

  launchLoading.value = true;
  try {
    const topicId = room.value?.topicId || task.value.topicId || Number(route.query.topicId);
    if (!Number.isFinite(topicId) || topicId <= 0) {
      ElMessage.error('实训题信息不完整，无法启动三维实训');
      return;
    }
    const session = await createUeLaunchSession(trainingId, topicId);
    launchUeApplication({
      ...session,
      roomId: session.roomId || room.value?.roomId || Number(route.params.roomId) || undefined
    });
    ElMessage.success('正在启动三维实训');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '三维实训启动失败');
  } finally {
    launchLoading.value = false;
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
  if (session.topicId) {
    query.set('topicId', String(session.topicId));
  }

  const scheme = String(import.meta.env.VITE_UE_PROTOCOL || 'jiaoyu-ue').replace(/[^a-z0-9+.-]/gi, '');
  const launcher = document.createElement('iframe');
  launcher.hidden = true;
  launcher.src = `${scheme}://launch?${query.toString()}`;
  document.body.appendChild(launcher);
  window.setTimeout(() => launcher.remove(), 2000);
}

onMounted(() => { void loadStartData(); });
onBeforeUnmount(() => { window.clearInterval(countdownTimer); });
</script>

<style scoped>
.training-start-page { min-height: calc(100vh - 120px); padding-bottom: 32px; color: #0f172a; }
.training-start-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.training-start-back { display: inline-flex; align-items: center; gap: 7px; padding: 4px 0; border: 0; background: transparent; color: #64748b; cursor: pointer; font-size: 14px; }
.training-start-back:hover { color: #2563eb; }
.training-start-status { display: inline-flex; align-items: center; gap: 7px; padding: 7px 12px; border-radius: 7px; background: #eff6ff; color: #2563eb; font-size: 13px; font-weight: 700; }
.training-start-status i { width: 7px; height: 7px; border-radius: 50%; background: currentColor; }
.training-start-loading { display: grid; min-height: 520px; place-content: center; justify-items: center; gap: 12px; color: #64748b; font-size: 14px; }
.training-start-loading .el-icon { color: #2563eb; font-size: 26px; }
.training-countdown-stage { position: relative; display: grid; min-height: 610px; overflow: hidden; place-items: center; border-radius: 14px; background: linear-gradient(135deg, #0f274e 0%, #123c76 52%, #1769aa 100%); }
.training-countdown-stage::before { position: absolute; inset: 0; background: linear-gradient(120deg, rgba(255, 255, 255, .08), transparent 35%, rgba(56, 189, 248, .14)); content: ''; }
.training-countdown-content { position: relative; z-index: 1; display: grid; justify-items: center; width: min(100%, 560px); padding: 56px 24px; color: #ffffff; text-align: center; }
.training-start-eyebrow { margin: 0 0 10px; color: #60a5fa; font-size: 13px; font-weight: 700; }
.training-countdown-content .training-start-eyebrow { color: #bfdbfe; }
.training-countdown-content h1, .training-workspace-title h1 { margin: 0; font-size: 30px; line-height: 42px; }
.training-countdown-caption { margin: 10px 0 0; color: rgba(255, 255, 255, .7); font-size: 14px; }
.training-role-summary { display: flex; align-items: center; gap: 12px; min-width: 230px; margin-top: 38px; padding: 12px 18px; border: 1px solid rgba(191, 219, 254, .35); border-radius: 9px; background: rgba(15, 23, 42, .18); text-align: left; }
.training-role-avatar { display: grid; width: 42px; height: 42px; place-items: center; border-radius: 10px; background: #2563eb; color: #ffffff; font-size: 19px; }
.training-role-avatar.small { width: 36px; height: 36px; font-size: 16px; }
.training-role-summary div, .training-workspace-role div { display: grid; gap: 4px; }
.training-role-summary span, .training-workspace-role span { color: #bfdbfe; font-size: 12px; }
.training-role-summary strong, .training-workspace-role strong { color: #ffffff; font-size: 14px; }
.training-countdown-number { display: grid; width: 168px; height: 168px; margin-top: 32px; place-items: center; border: 1px solid rgba(191, 219, 254, .5); border-radius: 50%; background: rgba(15, 23, 42, .2); box-shadow: 0 0 0 16px rgba(147, 197, 253, .08), 0 0 0 32px rgba(147, 197, 253, .04); color: #ffffff; font-size: 76px; font-weight: 700; line-height: 1; }
.training-countdown-tip { margin: 27px 0 0; color: rgba(255, 255, 255, .8); font-size: 14px; }
.training-ai-roles, .training-workspace-ai { display: flex; align-items: center; gap: 7px; color: #fde68a; font-size: 12px; }
.training-ai-roles { margin-top: 22px; }
.training-countdown-orbit { position: absolute; width: 320px; height: 320px; border: 1px solid rgba(191, 219, 254, .16); border-radius: 50%; }
.training-countdown-orbit::after { position: absolute; width: 8px; height: 8px; border-radius: 50%; background: #93c5fd; box-shadow: 0 0 18px #bfdbfe; content: ''; }
.training-countdown-orbit-left { top: -120px; left: -100px; }.training-countdown-orbit-left::after { right: 40px; bottom: 54px; }
.training-countdown-orbit-right { right: -90px; bottom: -150px; }.training-countdown-orbit-right::after { top: 50px; left: 38px; }
.training-workspace { min-height: 610px; padding: 30px; border: 1px solid #e6edf5; border-radius: 14px; background: #f8fbff; }
.training-workspace-title { display: flex; align-items: center; justify-content: space-between; gap: 20px; padding-bottom: 24px; border-bottom: 1px solid #e5edf5; }
.training-workspace-title h1 { color: #0f172a; font-size: 26px; }
.training-workspace-role { display: flex; align-items: center; gap: 10px; padding: 9px 13px; border: 1px solid #dbeafe; border-radius: 9px; background: #ffffff; }
.training-workspace-role span { color: #94a3b8; }.training-workspace-role strong { color: #334155; }
.training-workspace-content { display: grid; grid-template-columns: minmax(0, 1fr) 280px; gap: 18px; margin-top: 22px; }
.training-workspace-placeholder, .training-workspace-info { border: 1px solid #e5edf5; border-radius: 10px; background: #ffffff; }
.training-workspace-placeholder { display: grid; min-height: 450px; place-content: center; justify-items: center; padding: 30px; text-align: center; }
.training-workspace-icon { display: grid; width: 62px; height: 62px; place-items: center; border-radius: 15px; background: #eff6ff; color: #2563eb; font-size: 28px; }
.training-workspace-placeholder h2 { margin: 18px 0 7px; color: #1e293b; font-size: 19px; }.training-workspace-placeholder p { max-width: 400px; margin: 0 0 22px; color: #94a3b8; font-size: 13px; line-height: 22px; }
.training-workspace-info { align-self: start; padding: 20px; }.training-workspace-info h2 { margin: 0 0 18px; color: #1e293b; font-size: 16px; }.training-workspace-info dl { display: grid; gap: 15px; margin: 0; }.training-workspace-info dl div { display: flex; justify-content: space-between; gap: 15px; }.training-workspace-info dt { color: #94a3b8; font-size: 13px; }.training-workspace-info dd { margin: 0; color: #334155; font-size: 13px; text-align: right; }.training-workspace-ai { margin-top: 22px; padding-top: 15px; border-top: 1px solid #eef2f7; color: #c2410c; line-height: 18px; }
@media (max-width: 760px) { .training-countdown-stage { min-height: 560px; }.training-workspace { padding: 20px; }.training-workspace-content { grid-template-columns: 1fr; }.training-workspace-info { order: -1; }.training-workspace-title { align-items: stretch; flex-direction: column; }.training-start-header { align-items: stretch; flex-direction: column; gap: 12px; }.training-start-status { align-self: flex-start; } }
@media (max-width: 420px) { .training-countdown-content h1 { font-size: 25px; }.training-countdown-number { width: 140px; height: 140px; font-size: 64px; }.training-role-summary { margin-top: 28px; } }
</style>
