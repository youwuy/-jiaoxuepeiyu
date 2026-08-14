<template>
  <AdminShell activeKey="admin-trainings">
    <section class="admin-training-exam-page">
      <header class="admin-training-exam-topbar">
        <div class="admin-training-exam-heading">
          <el-button class="admin-training-exam-back" :icon="ArrowLeft" title="返回实训组课" @click="goBack" />
          <div>
            <h1>{{ trainingTitle }} - 考试准备</h1>
            <p>请等待全部学员进入指定实训试题、自动被分配随机角色，确认人员全部就绪后，点击开始考试。</p>
          </div>
        </div>
        <el-button class="admin-training-exam-start" type="primary" :loading="starting" :disabled="examStarted || !can('enable')" @click="startExam">
          {{ examStarted ? '考试已开始' : '开始考试' }}
        </el-button>
      </header>

      <main v-loading="loading" class="admin-training-exam-content">
        <header class="admin-training-exam-section-head">
          <el-icon><Monitor /></el-icon>
          <strong>学员实况</strong>
        </header>

        <section class="admin-training-exam-table-card">
          <div class="admin-training-exam-table-scroll">
            <table class="admin-training-exam-table">
              <colgroup>
                <col class="col-name" />
                <col class="col-student-no" />
                <col class="col-topic" />
                <col class="col-mode" />
                <col class="col-role" />
              </colgroup>
              <thead>
                <tr>
                  <th>学员姓名</th>
                  <th>学号</th>
                  <th>当前实训题</th>
                  <th>模式</th>
                  <th>系统分配角色</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="student in students" :key="student.id">
                  <td class="student-name">{{ student.name }}</td>
                  <td>{{ student.studentNo }}</td>
                  <td>{{ student.topic }}</td>
                  <td>{{ student.mode }}</td>
                  <td>{{ student.role }}</td>
                </tr>
              </tbody>
            </table>
            <el-empty v-if="!loading && students.length === 0" description="暂无学员实况数据" />
          </div>
        </section>
      </main>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeft, Monitor } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  fetchAdminTraining,
  fetchAdminTrainingMonitor,
  startAdminTrainingExam,
  type AdminTrainingStudentState
} from '../../api/admin-training';
import { useAdminPermissions } from '../../features/admin/use-admin-permissions';

interface ExamStudentRow {
  id: number;
  name: string;
  studentNo: string;
  topic: string;
  mode: string;
  role: string;
}

const route = useRoute();
const router = useRouter();
const { can } = useAdminPermissions('teaching:training');
const trainingId = computed(() => Number(route.params.id));
const trainingTitle = ref(String(route.query.title || '期末考试'));
const trainingTime = ref(String(route.query.time || ''));
const trainingRoom = ref(String(route.query.room || ''));
const loading = ref(false);
const starting = ref(false);
const examStarted = ref(false);
const students = ref<ExamStudentRow[]>([]);
let refreshTimer: number | undefined;

function goBack() {
  router.push('/admin/training');
}

async function startExam() {
  try {
    await ElMessageBox.confirm(
      '是否确认开始考试？开始后已有角色的学员将自动进入实训场景中，其他学员无法进入',
      '开始考试',
      { type: 'warning', confirmButtonText: '开始考试', cancelButtonText: '取消' }
    );
    starting.value = true;
    await startAdminTrainingExam(trainingId.value);
    examStarted.value = true;
    ElMessage.success('考试已开始');
    await router.push({
      name: 'admin-training-monitor',
      params: { id: trainingId.value },
      query: { title: trainingTitle.value, time: trainingTime.value, room: trainingRoom.value }
    });
  } catch (error) {
    if (error === 'cancel' || error === 'close') return;
    ElMessage.error(error instanceof Error ? error.message : '开始考试失败');
  } finally {
    starting.value = false;
  }
}

function mapStudent(item: AdminTrainingStudentState, index: number): ExamStudentRow {
  return {
    id: item.studentId || index + 1,
    name: item.studentName || '-',
    studentNo: item.studentNo || '-',
    topic: item.currentTopicName || '-',
    mode: item.currentTopicName ? '多人实训' : '-',
    role: item.roleName || '-'
  };
}

async function loadExamPreparation(silent = false) {
  if (!trainingId.value) {
    ElMessage.error('实训课编号无效');
    goBack();
    return;
  }

  if (!silent) loading.value = true;
  try {
    const [detail, snapshot] = await Promise.all([
      fetchAdminTraining(trainingId.value),
      fetchAdminTrainingMonitor(trainingId.value)
    ]);
    trainingTitle.value = detail.trainingName || trainingTitle.value;
    trainingTime.value = [detail.openStartTime, detail.openEndTime].filter(Boolean).join(' 至 ') || trainingTime.value;
    examStarted.value = Boolean(detail.examStartedAt);
    students.value = (snapshot.students || []).map(mapStudent);
  } catch (error) {
    if (!silent) {
      students.value = [];
      ElMessage.error(error instanceof Error ? error.message : '考试准备信息加载失败');
    }
  } finally {
    if (!silent) loading.value = false;
  }
}

onMounted(() => {
  void loadExamPreparation();
  refreshTimer = window.setInterval(() => {
    void loadExamPreparation(true);
  }, 5000);
});

onBeforeUnmount(() => {
  if (refreshTimer !== undefined) {
    window.clearInterval(refreshTimer);
  }
});
</script>

<style scoped>
.admin-training-exam-page {
  min-height: 100vh;
  padding: 0 24px 28px;
  background: #f5f7fb;
}

.admin-training-exam-topbar {
  display: flex;
  min-height: 82px;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 0 24px;
  border-bottom: 1px solid #e8edf5;
  background: #ffffff;
}

.admin-training-exam-heading {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 16px;
}

.admin-training-exam-back.el-button {
  width: 40px;
  height: 40px;
  flex: 0 0 auto;
  border: 0;
  border-radius: 8px;
  background: #f3f7fc;
  color: #53657f;
  font-size: 18px;
}

.admin-training-exam-heading h1 {
  margin: 0;
  color: #17233d;
  font-size: 17px;
  font-weight: 900;
}

.admin-training-exam-heading p {
  margin: 5px 0 0;
  color: #94a3b8;
  font-size: 12px;
}

.admin-training-exam-start.el-button {
  width: 100px;
  height: 44px;
  flex: 0 0 auto;
  border-radius: 8px;
  background: #3b82f6;
  font-weight: 800;
}

.admin-training-exam-content {
  padding: 24px;
  background: #ffffff;
}

.admin-training-exam-section-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
  color: #17233d;
  font-size: 14px;
}

.admin-training-exam-section-head .el-icon {
  color: #3478f6;
  font-size: 16px;
}

.admin-training-exam-table-card {
  border: 1px solid #dfe7f1;
  border-radius: 10px;
  overflow: hidden;
}

.admin-training-exam-table-scroll {
  max-height: calc(100vh - 230px);
  min-height: 560px;
  overflow: auto;
}

.admin-training-exam-table {
  width: 100%;
  min-width: 1080px;
  border-collapse: collapse;
  table-layout: fixed;
}

.admin-training-exam-table th {
  position: sticky;
  top: 0;
  z-index: 1;
  height: 48px;
  padding: 0 14px;
  background: #f6f8fb;
  color: #52657d;
  font-size: 12px;
  font-weight: 900;
  text-align: left;
}

.admin-training-exam-table td {
  height: 56px;
  padding: 0 14px;
  border-bottom: 1px solid #edf1f6;
  color: #52657d;
  font-size: 13px;
}

.admin-training-exam-table .student-name {
  color: #17233d;
  font-weight: 800;
}

.admin-training-exam-table .col-name {
  width: 96px;
}

.admin-training-exam-table .col-student-no {
  width: 130px;
}

.admin-training-exam-table .col-topic {
  width: 45%;
}

.admin-training-exam-table .col-mode {
  width: 96px;
}

.admin-training-exam-table .col-role {
  width: 106px;
}

@media (max-width: 760px) {
  .admin-training-exam-page {
    padding: 0 12px 20px;
  }

  .admin-training-exam-topbar {
    align-items: flex-start;
    flex-direction: column;
    padding: 16px;
  }

  .admin-training-exam-start.el-button {
    align-self: flex-end;
  }

  .admin-training-exam-content {
    padding: 16px;
  }

  .admin-training-exam-table-scroll {
    min-height: 480px;
  }
}
</style>
