<template>
  <AdminShell activeKey="admin-trainings">
    <section class="admin-training-monitor-page">
      <header class="admin-training-monitor-topbar">
        <div class="admin-training-monitor-left">
          <el-button class="admin-training-monitor-back" :icon="ArrowLeft" @click="goBack" />
          <el-breadcrumb class="admin-training-monitor-breadcrumb" separator="/">
            <el-breadcrumb-item>教学实训</el-breadcrumb-item>
            <el-breadcrumb-item>实训组课</el-breadcrumb-item>
            <el-breadcrumb-item>监考</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>{{ trainingTitle }} - 监考详情</h1>
        <span></span>
      </header>

      <main v-loading="loading" class="admin-training-monitor-content">
        <header class="admin-training-monitor-heading">
          <div>
            <h2>{{ trainingTitle }}</h2>
            <p>{{ monitorRange }} / {{ monitorRoom }} / {{ monitorClass }}</p>
          </div>
          <div class="monitor-refresh-actions">
            <span>每 5 秒自动刷新</span>
            <el-button :icon="Refresh" @click="loadMonitor()">刷新</el-button>
            <el-tag type="success" effect="dark">实训中</el-tag>
          </div>
        </header>

        <section class="monitor-camera-panel">
          <div class="panel-heading camera-heading"><h3>教室全景监控 <small>LIVE 实时直播</small></h3></div>
          <div
            v-if="!loading && cameras.length"
            class="monitor-grid"
            :class="{ 'is-single': cameras.length === 1 }"
            :style="{ '--camera-count': cameras.length }"
          >
            <article v-for="camera in cameras" :key="camera.id" class="camera-card" @click="openCamera(camera)">
              <div class="camera-screen">
                <video v-if="isCameraPlayable(camera)" :src="camera.streamUrl" autoplay muted playsinline />
                <span class="live-dot">{{ camera.online ? '在线' : '离线' }}</span>
                <span class="camera-channel">NVR {{ camera.channel }}</span>
                <template v-if="!isCameraPlayable(camera)">
                  <el-icon><Monitor /></el-icon>
                  <strong>{{ camera.name }}</strong>
                  <p>{{ cameraPlaceholder(camera) }}</p>
                </template>
              </div>
              <footer><span>{{ camera.location }}</span><time>{{ snapshotTime }}</time></footer>
            </article>
          </div>
          <el-empty v-else-if="!loading" description="暂无监控摄像头信息" />
        </section>

        <section class="monitor-student-panel">
          <div class="panel-heading">
            <h3>学员实训实况</h3>
          </div>
          <div v-if="students.length" class="admin-training-monitor-table-scroll">
            <table class="admin-training-monitor-table">
              <colgroup>
                <col class="admin-training-monitor-col-name" />
                <col class="admin-training-monitor-col-student-no" />
                <col class="admin-training-monitor-col-topic" />
                <col class="admin-training-monitor-col-mode" />
              </colgroup>
              <thead>
                <tr>
                  <th>学员姓名</th>
                  <th>学号</th>
                  <th>当前实训题</th>
                  <th>模式</th>
                  <th>个人成绩</th>
                  <th>实训进度</th>
                  <th>当前角色</th>
                  <th>所在房间</th>
                  <th>队员姓名</th>
                  <th>整队成绩</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="student in students" :key="student.id">
                  <td>{{ student.name }}</td>
                  <td>{{ student.studentNo }}</td>
                  <td>{{ student.topic }}</td>
                  <td>{{ student.mode }}</td>
                  <td>{{ student.score }}</td>
                  <td>{{ student.progress }}</td>
                  <td>{{ student.role }}</td>
                  <td>{{ student.room }}</td>
                  <td>
                    <span class="student-teammates">{{ student.teammates }}</span>
                  </td>
                  <td>{{ student.teamScore }}</td>
                  <td class="student-actions">
                    <el-button link type="primary" :disabled="!can('list')" @click="openStudentMonitor(student)">查看监控</el-button>
                    <el-button v-if="student.mode === '多人实训' && student.roomId" link type="danger" :disabled="!can('update')" @click="dissolveRoom(student)">解散房间</el-button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <el-empty v-else description="暂无参训学员信息" />
        </section>
      </main>

      <el-dialog
        v-model="studentMonitorVisible"
        class="admin-training-student-monitor-dialog"
        width="960px"
        :show-close="false"
        append-to-body
      >
        <template #header>
          <div class="admin-training-student-monitor-head">
            <div>
              <span>学员监控</span>
              <strong>查看学员桌面</strong>
            </div>
            <el-button text circle :icon="Close" @click="studentMonitorVisible = false" />
          </div>
        </template>

        <div v-if="students.length" class="admin-training-student-monitor-body">
          <section v-if="studentMonitorTarget" class="admin-training-student-desktop">
            <header>
              <div>
                <strong>{{ studentMonitorTarget.name }}</strong>
                <span>{{ studentMonitorTarget.studentNo }} / {{ studentMonitorTarget.room }}</span>
              </div>
              <el-tag size="small" :type="studentMonitorTarget.online ? 'success' : 'info'">
                {{ studentMonitorTarget.online ? '在线' : '离线' }}
              </el-tag>
            </header>
            <div class="admin-training-student-desktop-screen">
              <img
                v-if="desktopAvailable(studentMonitorTarget)"
                :src="desktopImageUrl(studentMonitorTarget)"
                :alt="`${studentMonitorTarget.name}桌面监控`"
                @error="markDesktopUnavailable(studentMonitorTarget)"
              />
              <template v-else>
                <el-icon><Monitor /></el-icon>
                <strong>{{ studentMonitorTarget.name }} 的桌面画面</strong>
                <span>学员桌面未上线，暂无监控画面</span>
              </template>
            </div>
            <footer class="student-monitor-info">
              <span><small>学员姓名</small>{{ studentMonitorTarget.name }}</span>
              <span><small>学号</small>{{ studentMonitorTarget.studentNo }}</span>
              <span><small>所在班级</small>{{ studentMonitorTarget.className }}</span>
              <span><small>当前进度</small>{{ studentMonitorTarget.progress }}</span>
            </footer>
          </section>
        </div>
        <el-empty v-else description="暂无可查看的学员桌面" />

        <template #footer>
          <div class="admin-training-student-monitor-footer">
            <el-button v-if="hasPreviousStudent" @click="switchStudent(-1)">上一个</el-button>
            <el-button v-if="hasNextStudent" @click="switchStudent(1)">下一个</el-button>
            <el-button @click="studentMonitorVisible = false">关闭</el-button>
          </div>
        </template>
      </el-dialog>

      <el-dialog v-model="cameraVisible" width="900px" title="摄像头实时画面" append-to-body>
        <div v-if="cameraTarget" class="camera-preview">
          <video v-if="cameraPlayable" :src="cameraTarget.streamUrl" controls autoplay muted playsinline />
          <div v-else class="camera-preview-empty">
            <Monitor />
            <strong>{{ cameraTarget.online ? '当前 RTSP 地址需经流媒体网关转换后才能在浏览器播放' : '设备离线，暂无实时视频流' }}</strong>
          </div>
        </div>
      </el-dialog>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeft, Close, Monitor, Refresh } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import { resolvePublicUrl } from '../../api/http';
import {
  dissolveAdminTrainingRoom,
  fetchAdminTraining,
  fetchAdminTrainingMonitor,
  type AdminTrainingCameraState,
  type AdminTrainingStudentState
} from '../../api/admin-training';
import { useAdminPermissions } from '../../features/admin/use-admin-permissions';

interface MonitorCamera {
  id: string;
  name: string;
  location: string;
  online: boolean;
  streamUrl?: string;
  channel: string;
}

interface MonitorStudent {
  id: number;
  name: string;
  studentNo: string;
  topic: string;
  mode: string;
  score: string;
  progress: string;
  role: string;
  room: string;
  roomId?: number;
  teammates: string;
  teamScore: string;
  className: string;
  online: boolean;
  desktopStreamUrl?: string;
}

const route = useRoute();
const router = useRouter();
const { can } = useAdminPermissions('teaching:training');
const trainingId = computed(() => Number(route.params.id));
const trainingTitle = ref(String(route.query.title || '实时监考'));
const monitorRange = ref(String(route.query.time || '未配置开放时间').replace(/\n/g, ' '));
const monitorClass = ref(String(route.query.className || '未配置班级'));
const monitorRoom = ref(String(route.query.room || '未配置教室'));
const loading = ref(false);
const cameras = ref<MonitorCamera[]>([]);
const students = ref<MonitorStudent[]>([]);
const studentMonitorVisible = ref(false);
const studentMonitorTarget = ref<MonitorStudent>();
const cameraVisible = ref(false);
const cameraTarget = ref<MonitorCamera>();
const snapshotTime = ref('-');
const unavailableDesktopIds = ref<number[]>([]);
const currentStudentIndex = computed(() => students.value.findIndex((student) => student.id === studentMonitorTarget.value?.id));
const hasPreviousStudent = computed(() => currentStudentIndex.value > 0);
const hasNextStudent = computed(() => currentStudentIndex.value >= 0 && currentStudentIndex.value < students.value.length - 1);
const cameraPlayable = computed(() => Boolean(cameraTarget.value && isCameraPlayable(cameraTarget.value)));
let refreshTimer: ReturnType<typeof setInterval> | undefined;

function goBack() {
  router.push('/admin/training');
}

function mapCamera(item: AdminTrainingCameraState, index: number): MonitorCamera {
  return {
    id: `${item.cameraId || 'camera'}-${index}`,
    name: item.cameraName || `摄像头${item.cameraId || index + 1}`,
    location: item.classroomName || '-',
    online: item.cameraStatus === 'ONLINE',
    streamUrl: resolvePublicUrl(item.streamUrl),
    channel: item.nvrChannel || String(index + 1)
  };
}

function mapStudent(item: AdminTrainingStudentState, index: number): MonitorStudent {
  return {
    id: item.studentId || index + 1,
    name: item.studentName || '-',
    studentNo: item.studentNo || '-',
    topic: item.currentTopicName || '-',
    mode: item.trainingMode === 'SINGLE' ? '单人实训' : '多人实训',
    score: item.score == null ? '-' : String(item.score),
    progress: `${item.submittedTopicCount || 0} / ${item.totalTopicCount || 0}`,
    role: item.roleName || '-',
    room: item.roomCode || (item.roomId ? `房间 ${item.roomId}` : '-'),
    roomId: item.roomId,
    teammates: item.roomId ? (item.teammateNames || '-') : '-',
    teamScore: item.roomId && item.teamScore != null ? String(item.teamScore) : '-',
    className: item.className || '-',
    online: item.deskStatus === 'ONLINE',
    desktopStreamUrl: item.desktopStreamUrl
  };
}

function openStudentMonitor(student: MonitorStudent) {
  studentMonitorTarget.value = student;
  studentMonitorVisible.value = true;
}

function switchStudent(offset: number) {
  const target = students.value[currentStudentIndex.value + offset];
  if (target) studentMonitorTarget.value = target;
}

function openCamera(camera: MonitorCamera) {
  cameraTarget.value = camera;
  cameraVisible.value = true;
}

function isCameraPlayable(camera: MonitorCamera) {
  return Boolean(camera.online && camera.streamUrl && !camera.streamUrl.toLowerCase().startsWith('rtsp://'));
}

function cameraPlaceholder(camera: MonitorCamera) {
  if (!camera.online) return '设备离线，暂无实时视频流';
  if (camera.streamUrl?.toLowerCase().startsWith('rtsp://')) return 'RTSP 流需经流媒体网关转换';
  return '暂无可播放的实时视频流';
}

function desktopAvailable(student: MonitorStudent) {
  return student.online && Boolean(student.desktopStreamUrl) && !unavailableDesktopIds.value.includes(student.id);
}

function markDesktopUnavailable(student: MonitorStudent) {
  if (!unavailableDesktopIds.value.includes(student.id)) {
    unavailableDesktopIds.value.push(student.id);
  }
}

function desktopImageUrl(student: MonitorStudent) {
  const url = resolvePublicUrl(student.desktopStreamUrl);
  if (!url) return '';
  const separator = url.includes('?') ? '&' : '?';
  return `${url}${separator}snapshot=${encodeURIComponent(snapshotTime.value)}`;
}

async function dissolveRoom(student: MonitorStudent) {
  if (!student.roomId) return;
  try {
    await ElMessageBox.confirm('解散后，学员将退出实训，需要重新组队才能实训', '解散房间', {
      type: 'warning',
      confirmButtonText: '确认解散',
      cancelButtonText: '取消'
    });
    await dissolveAdminTrainingRoom(trainingId.value, student.roomId);
    ElMessage.success('房间已解散');
    await loadMonitor(false);
  } catch (error) {
    if (error === 'cancel' || error === 'close') return;
    ElMessage.error(error instanceof Error ? error.message : '解散房间失败');
  }
}

async function loadMonitor(showLoading = true) {
  if (!trainingId.value) {
    return;
  }

  if (showLoading) loading.value = true;
  try {
    const detail = await fetchAdminTraining(trainingId.value);
    trainingTitle.value = detail.trainingName || trainingTitle.value;
    monitorRange.value = `${formatDateTime(detail.openStartTime)} 至 ${formatDateTime(detail.openEndTime)}`;
    monitorClass.value = detail.classNames || monitorClass.value;
    monitorRoom.value = detail.classroomName || monitorRoom.value;
  } catch {
    trainingTitle.value = trainingTitle.value || '实时监考';
  }

  try {
    const snapshot = await fetchAdminTrainingMonitor(trainingId.value);
    unavailableDesktopIds.value = [];
    cameras.value = (snapshot.cameras || []).slice(0, 4).map(mapCamera);
    students.value = (snapshot.students || []).map(mapStudent);
    snapshotTime.value = formatDateTime(snapshot.generatedAt);
    if (studentMonitorTarget.value) {
      studentMonitorTarget.value = students.value.find((student) => student.id === studentMonitorTarget.value?.id);
    }
    monitorRoom.value = monitorRoom.value || '未配置教室';
  } catch (error) {
    cameras.value = [];
    students.value = [];
    ElMessage.error(error instanceof Error ? error.message : '实训监控加载失败');
  } finally {
    if (showLoading) loading.value = false;
  }
}

function formatDateTime(value?: string) {
  if (!value) {
    return '未配置';
  }
  return value.replace('T', ' ').slice(0, 16);
}

onMounted(() => {
  void loadMonitor();
  refreshTimer = setInterval(() => void loadMonitor(false), 5000);
});

onBeforeUnmount(() => {
  if (refreshTimer) clearInterval(refreshTimer);
});
</script>

<style scoped>
.admin-training-monitor-page {
  min-height: 100vh;
  padding: 0 24px 28px;
  background: #f5f7fb;
}

.admin-training-monitor-topbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);
  align-items: center;
  min-height: 68px;
}

.admin-training-monitor-left {
  display: flex;
  align-items: center;
  gap: 18px;
}

.admin-training-monitor-back.el-button {
  width: 44px;
  height: 44px;
  border: 1px solid #dce5f1;
  border-radius: 9px;
  background: #ffffff;
  color: #53657f;
  font-size: 18px;
}

.admin-training-monitor-breadcrumb {
  font-size: 13px;
}

.admin-training-monitor-breadcrumb .el-breadcrumb__inner {
  color: #6c7d96;
}

.admin-training-monitor-breadcrumb .el-breadcrumb__item:last-child .el-breadcrumb__inner {
  color: #2f7cf6;
  font-weight: 800;
}

.admin-training-monitor-topbar h1 {
  margin: 0;
  color: #111827;
  font-size: 17px;
  font-weight: 900;
  text-align: center;
}

.admin-training-monitor-content {
  min-height: calc(100vh - 96px);
  border: 1px solid #e5ebf3;
  border-radius: 10px;
  padding: 30px;
  background: #ffffff;
}

.admin-training-monitor-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}

.admin-training-monitor-heading h2 {
  margin: 0;
  color: #0f172a;
  font-size: 26px;
  line-height: 34px;
}

.admin-training-monitor-heading p {
  margin: 8px 0 0;
  color: #647895;
  font-size: 16px;
}

.monitor-refresh-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #7b8da7;
  font-size: 13px;
}

.admin-training-monitor-back-list.el-button {
  height: 44px;
  padding: 0 16px;
  border-color: #d9e4f0;
  border-radius: 8px;
  color: #53657f;
  font-size: 16px;
  font-weight: 800;
}

.admin-training-monitor-page .monitor-grid {
  grid-template-columns: repeat(var(--camera-count), minmax(260px, 1fr));
  gap: 18px;
  overflow-x: auto;
}

.admin-training-monitor-page .monitor-grid.is-single {
  grid-template-columns: minmax(420px, 720px);
  justify-content: center;
}

.camera-heading {
  border: 0 !important;
  padding: 0 !important;
}

.camera-heading small {
  margin-left: 8px;
  color: #ef4444;
  font-size: 12px;
}

.camera-card {
  cursor: pointer;
}

.camera-channel {
  position: absolute;
  top: 12px;
  right: 12px;
  color: #fff;
  font-size: 12px;
}

.admin-training-monitor-page .camera-screen {
  min-height: 242px;
}

.admin-training-monitor-page .camera-screen video {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  background: #0f172a;
}

.admin-training-monitor-page .camera-screen .el-icon {
  width: 42px;
  height: 42px;
}

.admin-training-monitor-page .monitor-student-panel {
  margin-top: 30px;
  border-color: #dfe7f1;
  border-radius: 9px;
}

.admin-training-monitor-empty {
  display: grid;
  min-height: 366px;
  place-items: center;
  border: 1px dashed #dce5f1;
  border-radius: 8px;
  background: #fcfdff;
}

.admin-training-monitor-empty :deep(.el-empty) {
  padding: 0;
}

.admin-training-monitor-empty :deep(.el-empty__image) {
  width: 160px;
}

.admin-training-monitor-empty :deep(.el-empty__description) {
  margin-top: 14px;
  color: #9aa9bc;
  font-size: 16px;
}

.admin-training-monitor-page .panel-heading {
  min-height: 70px;
  padding: 0 16px;
  border-bottom-color: #dfe7f1;
}

.admin-training-monitor-page .panel-heading h3 {
  color: #0f172a;
  font-size: 20px;
}

.admin-training-monitor-page .panel-heading .el-button {
  height: 40px;
  border-color: #90b9ff;
  border-radius: 6px;
  color: #367df5;
  font-size: 16px;
  font-weight: 800;
}

.admin-training-monitor-table-scroll {
  overflow-x: auto;
}

.admin-training-monitor-table {
  width: 100%;
  min-width: 1900px;
  border-collapse: collapse;
  table-layout: fixed;
}

.admin-training-monitor-table th,
.admin-training-monitor-table td {
  height: 56px;
  padding: 0 16px;
  border-bottom: 1px solid #e5ebf3;
  color: #53657d;
  font-size: 16px;
  text-align: left;
  white-space: nowrap;
}

.admin-training-monitor-table th {
  height: 58px;
  color: #93a0b1;
  font-weight: 900;
}

.admin-training-monitor-table td {
  font-weight: 500;
}

.admin-training-monitor-col-name {
  width: 15%;
}

.admin-training-monitor-col-student-no {
  width: 18%;
}

.admin-training-monitor-col-topic {
  width: 22%;
}

.admin-training-monitor-col-mode {
  width: 10%;
}

.admin-training-monitor-col-room {
  width: 12%;
}

.admin-training-student-monitor-dialog.el-dialog {
  max-width: calc(100vw - 32px);
  border-radius: 8px;
}

.admin-training-student-monitor-dialog .el-dialog__header {
  margin: 0;
  padding: 20px 24px 14px;
}

.admin-training-student-monitor-dialog .el-dialog__body {
  padding: 16px 24px 0;
}

.admin-training-student-monitor-dialog .el-dialog__footer {
  padding: 16px 24px 22px;
}

.admin-training-student-monitor-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.admin-training-student-monitor-head > div {
  display: grid;
  gap: 5px;
}

.admin-training-student-monitor-head span {
  color: #6c7d96;
  font-size: 13px;
  font-weight: 700;
}

.admin-training-student-monitor-head strong {
  color: #111827;
  font-size: 20px;
  font-weight: 900;
}

.admin-training-student-monitor-body {
  min-height: 440px;
}

.admin-training-student-monitor-list {
  display: grid;
  align-content: start;
  gap: 8px;
  border: 1px solid #e5ebf3;
  border-radius: 8px;
  padding: 14px;
  background: #f8fafc;
}

.admin-training-student-monitor-list > strong {
  color: #263445;
  font-size: 14px;
}

.admin-training-student-monitor-list button {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  min-width: 0;
  border: 1px solid #e5ebf3;
  border-radius: 7px;
  padding: 10px;
  background: #ffffff;
  color: #52657d;
  text-align: left;
  cursor: pointer;
}

.admin-training-student-monitor-list button.active {
  border-color: #367df5;
  background: #edf5ff;
}

.admin-training-student-monitor-list button > span {
  display: grid;
  min-width: 0;
  gap: 4px;
}

.admin-training-student-monitor-list button b,
.admin-training-student-monitor-list button small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.admin-training-student-monitor-list button b {
  color: #263445;
  font-size: 13px;
}

.admin-training-student-monitor-list button small {
  color: #8a9ab0;
  font-size: 11px;
}

.admin-training-student-monitor-list button i {
  flex: 0 0 auto;
  color: #94a3b8;
  font-size: 11px;
  font-style: normal;
}

.admin-training-student-monitor-list button i.online {
  color: #10a866;
}

.admin-training-student-desktop {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  overflow: hidden;
  border: 1px solid #e5ebf3;
  border-radius: 8px;
  background: #ffffff;
}

.admin-training-student-desktop > header,
.admin-training-student-desktop > footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
}

.admin-training-student-desktop > header {
  border-bottom: 1px solid #e5ebf3;
}

.admin-training-student-desktop > header > div {
  display: grid;
  gap: 4px;
}

.admin-training-student-desktop > header strong {
  color: #263445;
  font-size: 14px;
}

.admin-training-student-desktop > header span,
.admin-training-student-desktop > footer {
  color: #8a9ab0;
  font-size: 12px;
}

.admin-training-student-desktop-screen {
  display: grid;
  align-content: center;
  justify-items: center;
  gap: 10px;
  min-height: 300px;
  background:
    linear-gradient(135deg, rgba(15, 23, 42, 0.96), rgba(30, 64, 175, 0.84)),
    repeating-linear-gradient(90deg, rgba(255, 255, 255, 0.08) 0 1px, transparent 1px 32px);
  color: #ffffff;
  text-align: center;
}

.admin-training-student-desktop-screen .el-icon {
  font-size: 42px;
  opacity: 0.88;
}

.admin-training-student-desktop-screen strong {
  font-size: 16px;
}

.admin-training-student-desktop-screen span {
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
}

.admin-training-student-desktop-screen img {
  width: 100%;
  height: 100%;
  min-height: 300px;
  object-fit: contain;
}

.student-monitor-info {
  display: grid !important;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.student-monitor-info span {
  display: grid;
  min-width: 0;
  gap: 4px;
  overflow: hidden;
  color: #334155;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.student-monitor-info small {
  color: #94a3b8;
}

.student-actions {
  width: 180px;
}

.student-teammates {
  display: inline-block;
  overflow: hidden;
  max-width: 170px;
  text-overflow: ellipsis;
  vertical-align: middle;
}

.camera-preview video {
  display: block;
  width: 100%;
  max-height: 70vh;
  background: #0f172a;
}

.camera-preview-empty {
  display: grid;
  min-height: 420px;
  place-items: center;
  gap: 12px;
  padding: 40px;
  background: #0f172a;
  color: #fff;
  text-align: center;
}

.camera-preview-empty svg {
  width: 52px;
  height: 52px;
}

.admin-training-student-monitor-footer {
  display: flex;
  justify-content: flex-end;
}

.admin-training-student-monitor-footer .el-button {
  min-width: 80px;
  height: 36px;
  border-radius: 7px;
  font-weight: 800;
}

@media (max-width: 980px) {
  .admin-training-monitor-topbar {
    grid-template-columns: 1fr;
    align-items: flex-start;
    gap: 10px;
    padding: 12px 0;
  }

  .admin-training-monitor-topbar h1 {
    order: -1;
    text-align: left;
  }

  .admin-training-monitor-content {
    padding: 22px;
  }

  .student-monitor-info { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

@media (max-width: 640px) {
  .admin-training-monitor-page {
    padding: 0 12px 20px;
  }

  .admin-training-monitor-heading {
    flex-direction: column;
  }

  .monitor-refresh-actions {
    flex-wrap: wrap;
  }

  .admin-training-monitor-page .monitor-grid.is-single {
    grid-template-columns: minmax(280px, 1fr);
  }

  .admin-training-monitor-content {
    padding: 16px;
  }

  .admin-training-monitor-back-list.el-button {
    width: 100%;
  }

  .admin-training-monitor-empty {
    min-height: 280px;
  }

  .admin-training-monitor-page .panel-heading {
    min-height: 62px;
  }

  .admin-training-student-monitor-dialog.el-dialog {
    width: calc(100vw - 20px) !important;
  }

  .admin-training-student-monitor-dialog .el-dialog__header,
  .admin-training-student-monitor-dialog .el-dialog__body,
  .admin-training-student-monitor-dialog .el-dialog__footer {
    padding-right: 16px;
    padding-left: 16px;
  }

  .admin-training-student-monitor-list {
    grid-template-columns: 1fr;
  }

  .admin-training-student-monitor-list > strong {
    grid-column: auto;
  }

  .admin-training-student-desktop > footer {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
