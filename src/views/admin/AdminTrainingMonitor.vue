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
        <h1>实时监考</h1>
        <span></span>
      </header>

      <main v-loading="loading" class="admin-training-monitor-content">
        <header class="admin-training-monitor-heading">
          <div>
            <h2>{{ trainingTitle }}</h2>
            <p>{{ monitorRange }} / {{ monitorRoom }} / {{ cameras.length }} 间</p>
          </div>
          <el-button class="admin-training-monitor-back-list" @click="goBack">返回实训组课</el-button>
        </header>

        <div v-if="!loading && cameras.length" class="monitor-grid">
          <article v-for="camera in cameras" :key="camera.id" class="camera-card">
            <div class="camera-screen">
              <span class="live-dot">直播</span>
              <strong>{{ camera.name }}</strong>
              <p>RTSP 可配置接入</p>
            </div>
            <footer>
              <span>{{ camera.location }}</span>
              <el-tag size="small" :type="camera.online ? 'success' : 'info'">
                {{ camera.online ? '在线' : '离线' }}
              </el-tag>
            </footer>
          </article>
        </div>
        <div v-else-if="!loading" class="admin-training-monitor-empty">
          <el-empty description="暂无监控摄像头信息" />
        </div>

        <section class="monitor-student-panel">
          <div class="panel-heading">
            <h3>学员监控</h3>
            <el-button :icon="Monitor" type="primary" plain @click="openStudentMonitor">查看学员桌面</el-button>
          </div>
          <div v-if="students.length" class="admin-training-monitor-table-scroll">
            <table class="admin-training-monitor-table">
              <colgroup>
                <col class="admin-training-monitor-col-name" />
                <col class="admin-training-monitor-col-student-no" />
                <col class="admin-training-monitor-col-topic" />
                <col class="admin-training-monitor-col-mode" />
                <col class="admin-training-monitor-col-room" />
                <col class="admin-training-monitor-col-ip" />
                <col class="admin-training-monitor-col-status" />
              </colgroup>
              <thead>
                <tr>
                  <th>学员姓名</th>
                  <th>学号</th>
                  <th>当前实训题</th>
                  <th>模式</th>
                  <th>所在房间</th>
                  <th>IP</th>
                  <th>在线状态</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="student in students" :key="student.id">
                  <td>{{ student.name }}</td>
                  <td>{{ student.studentNo }}</td>
                  <td>{{ student.topic }}</td>
                  <td>{{ student.mode }}</td>
                  <td>{{ student.room }}</td>
                  <td>{{ student.ip }}</td>
                  <td>
                    <span class="admin-training-monitor-status" :class="{ offline: !student.online }">
                      {{ student.online ? '在线' : '离线' }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <el-empty v-else description="暂无在线学员信息" />
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
          <aside class="admin-training-student-monitor-list">
            <strong>学员列表</strong>
            <button
              v-for="student in students"
              :key="student.id"
              type="button"
              :class="{ active: studentMonitorTarget?.id === student.id }"
              @click="selectStudent(student)"
            >
              <span>
                <b>{{ student.name }}</b>
                <small>{{ student.studentNo }}</small>
              </span>
              <i :class="{ online: student.online }">{{ student.online ? '在线' : '离线' }}</i>
            </button>
          </aside>

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
              <el-icon><Monitor /></el-icon>
              <strong>{{ studentMonitorTarget.name }} 的桌面画面</strong>
              <span>{{ studentMonitorTarget.online ? '等待桌面流接入' : '学员当前不在线' }}</span>
            </div>
            <footer>
              <span>当前实训题：{{ studentMonitorTarget.topic }}</span>
              <span>IP：{{ studentMonitorTarget.ip }}</span>
            </footer>
          </section>
        </div>
        <el-empty v-else description="暂无可查看的学员桌面" />

        <template #footer>
          <div class="admin-training-student-monitor-footer">
            <el-button @click="studentMonitorVisible = false">关闭</el-button>
          </div>
        </template>
      </el-dialog>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft, Close, Monitor } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  fetchAdminTraining,
  fetchAdminTrainingMonitor,
  type AdminTrainingCameraState,
  type AdminTrainingStudentState
} from '../../api/admin-training';

interface MonitorCamera {
  id: string;
  name: string;
  location: string;
  online: boolean;
  streamUrl?: string;
}

interface MonitorStudent {
  id: number;
  name: string;
  studentNo: string;
  topic: string;
  mode: string;
  room: string;
  ip: string;
  online: boolean;
}

const route = useRoute();
const router = useRouter();
const trainingId = computed(() => Number(route.params.id));
const trainingTitle = ref(String(route.query.title || '实时监考'));
const monitorRange = ref(String(route.query.time || '未配置开放时间').replace(/\n/g, ' '));
const monitorRoom = ref(String(route.query.room || '未配置教室'));
const loading = ref(false);
const cameras = ref<MonitorCamera[]>([]);
const students = ref<MonitorStudent[]>([]);
const studentMonitorVisible = ref(false);
const studentMonitorTarget = ref<MonitorStudent>();

function goBack() {
  router.push('/admin/training');
}

function mapCamera(item: AdminTrainingCameraState, index: number): MonitorCamera {
  return {
    id: `${item.cameraId || 'camera'}-${index}`,
    name: item.cameraName || `摄像头${item.cameraId || index + 1}`,
    location: item.classroomName || '-',
    online: item.cameraStatus !== 'OFFLINE',
    streamUrl: item.streamUrl
  };
}

function mapStudent(item: AdminTrainingStudentState, index: number): MonitorStudent {
  return {
    id: item.studentId || index + 1,
    name: item.studentName || '-',
    studentNo: item.studentNo || '-',
    topic: item.roleName || '-',
    mode: item.roomStatus === 'SINGLE' ? '单人实训' : '协同实训',
    room: item.roomId ? `房间 ${item.roomId}` : '-',
    ip: item.clientIp || '-',
    online: item.deskStatus !== 'OFFLINE'
  };
}

function selectStudent(student: MonitorStudent) {
  studentMonitorTarget.value = student;
}

function openStudentMonitor() {
  studentMonitorTarget.value = students.value.find((student) => student.online) || students.value[0];
  studentMonitorVisible.value = true;
}

async function loadMonitor() {
  if (!trainingId.value) {
    return;
  }

  loading.value = true;
  try {
    const detail = await fetchAdminTraining(trainingId.value);
    trainingTitle.value = detail.trainingName || trainingTitle.value;
    monitorRange.value = `${formatDateTime(detail.openStartTime)} 至 ${formatDateTime(detail.openEndTime)}`;
    monitorRoom.value = detail.roomCount ? `${detail.roomCount} 间实训教室` : monitorRoom.value;
  } catch {
    trainingTitle.value = trainingTitle.value || '实时监考';
  }

  try {
    const snapshot = await fetchAdminTrainingMonitor(trainingId.value);
    cameras.value = (snapshot.cameras || []).map(mapCamera);
    students.value = (snapshot.students || []).map(mapStudent);
    monitorRoom.value = monitorRoom.value || '未配置教室';
  } catch (error) {
    cameras.value = [];
    students.value = [];
    ElMessage.error(error instanceof Error ? error.message : '实训监控加载失败');
  } finally {
    loading.value = false;
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
  gap: 18px;
}

.admin-training-monitor-page .camera-screen {
  min-height: 242px;
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
  min-width: 900px;
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

.admin-training-monitor-col-ip {
  width: 12%;
}

.admin-training-monitor-col-status {
  width: 11%;
}

.admin-training-monitor-status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  height: 24px;
  border: 1px solid #d9edcb;
  border-radius: 5px;
  padding: 0 8px;
  background: #f0f9e9;
  color: #67c23a;
  font-size: 13px;
  font-weight: 700;
}

.admin-training-monitor-status.offline {
  border-color: #e1e7ef;
  background: #f5f7fa;
  color: #909399;
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
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 16px;
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

  .admin-training-student-monitor-body {
    grid-template-columns: 1fr;
  }

  .admin-training-student-monitor-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .admin-training-student-monitor-list > strong {
    grid-column: 1 / -1;
  }
}

@media (max-width: 640px) {
  .admin-training-monitor-page {
    padding: 0 12px 20px;
  }

  .admin-training-monitor-heading {
    flex-direction: column;
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
