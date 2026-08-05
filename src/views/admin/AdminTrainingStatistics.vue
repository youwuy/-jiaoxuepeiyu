<template>
  <AdminShell activeKey="admin-trainings">
    <section class="admin-training-statistics-page">
      <header class="admin-training-statistics-topbar">
        <div class="admin-training-statistics-left">
          <el-button class="admin-training-statistics-back" :icon="ArrowLeft" @click="goBack" />
          <el-breadcrumb class="admin-training-statistics-breadcrumb" separator="/">
            <el-breadcrumb-item>教学实训</el-breadcrumb-item>
            <el-breadcrumb-item>实训组课</el-breadcrumb-item>
            <el-breadcrumb-item>成绩统计</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>成绩统计</h1>
        <span></span>
      </header>

      <main v-loading="loading" class="admin-training-statistics-content">
        <header class="admin-training-statistics-heading">
          <div>
            <span>成绩统计</span>
            <h2>{{ trainingTitle }}</h2>
          </div>
          <el-button class="admin-training-statistics-back-list" @click="goBack">返回实训组课</el-button>
        </header>

        <section class="admin-training-statistics-summary">
          <article>
            <span>应参加</span>
            <strong>{{ statistics.participantCount || 0 }}</strong>
          </article>
          <article>
            <span>已完成</span>
            <strong>{{ statistics.submittedAttemptCount || 0 }}</strong>
          </article>
          <article>
            <span>平均分</span>
            <strong>{{ formatScore(statistics.averageScore) }}</strong>
          </article>
          <article>
            <span>通过率</span>
            <strong>{{ passRate }}</strong>
          </article>
        </section>

        <section class="admin-training-statistics-table-card">
          <table v-if="hasStatistics" class="admin-training-statistics-table">
            <thead>
              <tr>
                <th>班级</th>
                <th>人数</th>
                <th>完成人数</th>
                <th>平均分</th>
                <th>通过率</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>{{ className }}</td>
                <td>{{ statistics.participantCount || 0 }}</td>
                <td>{{ statistics.submittedAttemptCount || 0 }}</td>
                <td>{{ formatScore(statistics.averageScore) }}</td>
                <td>{{ passRate }}</td>
              </tr>
            </tbody>
          </table>
          <el-empty v-else-if="!loading" description="暂无成绩统计数据" />
        </section>
      </main>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  fetchAdminTraining,
  fetchAdminTrainingStatistics,
  type AdminTrainingStatistics as TrainingStatistics
} from '../../api/admin-training';

const route = useRoute();
const router = useRouter();
const trainingId = computed(() => Number(route.params.id));
const trainingTitle = ref(String(route.query.title || '实训组课'));
const className = ref(String(route.query.target || '暂无班级'));
const loading = ref(false);
const statistics = ref<TrainingStatistics>({});

const hasStatistics = computed(() => Object.keys(statistics.value).length > 0);

const passRate = computed(() => {
  const participantCount = Number(statistics.value.participantCount || 0);
  const completedCount = Number(statistics.value.submittedAttemptCount || 0);
  if (!participantCount) {
    return '0%';
  }
  return `${Math.round((completedCount / participantCount) * 100)}%`;
});

function goBack() {
  router.push('/admin/training');
}

function formatScore(value?: number) {
  if (value === undefined || value === null || Number.isNaN(Number(value))) {
    return '-';
  }
  return Number(value).toFixed(1).replace(/\.0$/, '');
}

async function loadStatistics() {
  if (!trainingId.value) {
    return;
  }

  loading.value = true;
  try {
    const [detail, result] = await Promise.all([
      fetchAdminTraining(trainingId.value),
      fetchAdminTrainingStatistics(trainingId.value)
    ]);
    trainingTitle.value = detail.trainingName || trainingTitle.value;
    className.value = detail.classNames || className.value;
    statistics.value = result;
  } catch (error) {
    statistics.value = {};
    ElMessage.error(error instanceof Error ? error.message : '成绩统计加载失败');
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadStatistics();
});
</script>

<style scoped>
.admin-training-statistics-page {
  min-height: 100vh;
  padding: 0 24px 28px;
  background: #f5f7fb;
}

.admin-training-statistics-topbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);
  align-items: center;
  min-height: 68px;
}

.admin-training-statistics-left {
  display: flex;
  align-items: center;
  gap: 18px;
}

.admin-training-statistics-back.el-button {
  width: 36px;
  height: 36px;
  border: 1px solid #dce5f1;
  border-radius: 8px;
  background: #ffffff;
  color: #53657f;
  font-size: 16px;
}

.admin-training-statistics-breadcrumb {
  font-size: 13px;
}

.admin-training-statistics-breadcrumb :deep(.el-breadcrumb__inner) {
  color: #6c7d96;
}

.admin-training-statistics-breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #2f7cf6;
  font-weight: 800;
}

.admin-training-statistics-topbar h1 {
  margin: 0;
  color: #111827;
  font-size: 17px;
  font-weight: 900;
  text-align: center;
}

.admin-training-statistics-content {
  min-height: calc(100vh - 96px);
  border: 1px solid #e5ebf3;
  border-radius: 10px;
  padding: 24px;
  background: #ffffff;
}

.admin-training-statistics-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 24px;
  border-bottom: 1px solid #edf2f8;
}

.admin-training-statistics-heading > div {
  display: grid;
  gap: 8px;
}

.admin-training-statistics-heading span {
  color: #6c7d96;
  font-size: 14px;
  font-weight: 700;
}

.admin-training-statistics-heading h2 {
  margin: 0;
  color: #17233d;
  font-size: 24px;
  line-height: 32px;
  font-weight: 900;
}

.admin-training-statistics-back-list.el-button {
  height: 36px;
  border-color: #d9e4f0;
  border-radius: 7px;
  color: #53657f;
  font-weight: 800;
}

.admin-training-statistics-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  padding: 24px 0 28px;
}

.admin-training-statistics-summary article {
  display: grid;
  gap: 12px;
  min-height: 106px;
  border: 1px solid #dfe7f1;
  border-radius: 9px;
  padding: 20px;
  background: #f8fafc;
}

.admin-training-statistics-summary span {
  color: #6d7e97;
  font-size: 14px;
}

.admin-training-statistics-summary strong {
  color: #17233d;
  font-size: 22px;
  line-height: 28px;
  font-weight: 900;
}

.admin-training-statistics-table-card {
  overflow-x: auto;
}

.admin-training-statistics-table {
  width: 100%;
  min-width: 680px;
  border-collapse: collapse;
  table-layout: fixed;
}

.admin-training-statistics-table th,
.admin-training-statistics-table td {
  height: 50px;
  padding: 0 14px;
  border-bottom: 1px solid #e5ebf3;
  color: #626d7e;
  font-size: 14px;
  text-align: left;
}

.admin-training-statistics-table th {
  color: #8b929d;
  font-weight: 900;
}

.admin-training-statistics-table td {
  color: #606b7b;
  font-weight: 500;
}

.admin-training-statistics-table th:first-child,
.admin-training-statistics-table td:first-child {
  width: 42%;
}

.admin-training-statistics-table th:not(:first-child),
.admin-training-statistics-table td:not(:first-child) {
  width: 14.5%;
}

.admin-training-statistics-table-card :deep(.el-empty) {
  min-height: 180px;
}

@media (max-width: 980px) {
  .admin-training-statistics-topbar {
    grid-template-columns: 1fr;
    align-items: flex-start;
    gap: 10px;
    padding: 12px 0;
  }

  .admin-training-statistics-topbar h1 {
    order: -1;
    text-align: left;
  }

  .admin-training-statistics-content {
    padding: 18px;
  }

  .admin-training-statistics-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .admin-training-statistics-page {
    padding: 0 12px 20px;
  }

  .admin-training-statistics-heading {
    flex-direction: column;
  }

  .admin-training-statistics-back-list.el-button {
    width: 100%;
  }

  .admin-training-statistics-summary {
    grid-template-columns: 1fr;
  }
}
</style>
