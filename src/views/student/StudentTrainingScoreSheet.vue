<template>
  <StudentShell eyebrow="实训中心" title="成绩单">
    <section class="score-sheet-page">
      <button class="score-sheet-back" type="button" @click="backToTraining">
        <el-icon><ArrowLeft /></el-icon>
        返回实训中心
      </button>

      <div v-if="loading" class="student-loading">成绩单加载中...</div>

      <template v-else-if="scoreSheet">
        <header class="score-sheet-header">
          <div>
            <p>实训中心 / 成绩单</p>
            <h1>{{ scoreSheet.title || '实训成绩单' }}</h1>
            <span>提交时间：{{ scoreSheet.finishedAt || '-' }}</span>
          </div>
          <el-button class="score-ranking-button" type="primary" plain @click="rankingVisible = true">
            <el-icon><Trophy /></el-icon>
            查看成绩排名
          </el-button>
        </header>

        <section class="score-sheet-overview">
          <div class="score-sheet-student">
            <span class="score-sheet-avatar"><el-icon><User /></el-icon></span>
            <div><span>学员</span><strong>{{ scoreSheet.studentName || '-' }}</strong><small>{{ scoreSheet.className || '-' }}</small></div>
          </div>
          <div class="score-sheet-score"><span>实训得分</span><strong>{{ scoreSheet.score ?? 0 }}</strong><em>分</em></div>
          <div class="score-sheet-overview-item"><span>实训模式</span><strong>{{ scoreSheet.mode || '-' }}</strong></div>
          <div class="score-sheet-overview-item"><span>实训用时</span><strong>{{ scoreSheet.duration || '-' }}</strong></div>
        </section>

        <section class="score-sheet-content">
          <div class="score-sheet-panel">
            <header class="score-sheet-panel-header"><div><h2>实训步骤成绩</h2><p>查看本次实训的操作记录和步骤得分</p></div><span>{{ scoreSheet.steps.length }} 个步骤</span></header>
            <el-table :data="scoreSheet.steps" class="score-sheet-table">
              <el-table-column type="index" label="序号" width="72" />
              <el-table-column prop="name" label="实训步骤" min-width="170" />
              <el-table-column prop="expected" label="标准操作" min-width="220" />
              <el-table-column prop="actual" label="实际操作" min-width="220" />
              <el-table-column prop="score" label="得分" width="90">
                <template #default="scope"><strong class="step-score">{{ scope.row.score ?? 0 }}</strong></template>
              </el-table-column>
            </el-table>
            <div v-if="scoreSheet.recordingUrl" class="score-sheet-recording"><el-button type="primary" @click="openRecording">查看实训录屏</el-button></div>
            <el-empty v-if="!scoreSheet.steps.length" description="暂无步骤成绩" />
          </div>

          <aside class="score-sheet-side">
            <div class="score-sheet-side-card"><h2>成绩说明</h2><p>本次成绩由实训过程中的各步骤操作结果综合计算。</p><div class="score-sheet-rule"><el-icon><CircleCheckFilled /></el-icon><span>成绩已提交</span></div></div>
            <div class="score-sheet-side-card score-sheet-rank-card"><span class="rank-card-icon"><el-icon><Trophy /></el-icon></span><div><h2>成绩排名</h2><p>无论本次得分多少，均可查看成绩排名。</p></div><el-button text type="primary" @click="rankingVisible = true">查看排名<el-icon><ArrowRight /></el-icon></el-button></div>
          </aside>
        </section>
      </template>

      <div v-else class="score-sheet-error">暂时无法获取成绩单，请返回实训中心重试</div>
    </section>

    <el-dialog v-model="rankingVisible" width="520px" title="成绩排名" align-center>
      <div class="ranking-dialog-content">
        <span class="ranking-dialog-icon"><el-icon><Trophy /></el-icon></span>
        <h2>本次成绩排名</h2>
        <strong>待公布</strong>
        <p>成绩排名将在本次实训全部结束后统一生成，请稍后再查看。</p>
      </div>
      <template #footer><el-button type="primary" @click="rankingVisible = false">知道了</el-button></template>
    </el-dialog>
  </StudentShell>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft, ArrowRight, CircleCheckFilled, Trophy, User } from '@element-plus/icons-vue';
import { fetchStudentTrainingScoreSheet } from '../../api/student';
import type { TrainingArchiveDetail } from '../../features/student/profile';
import StudentShell from '../../components/student/StudentShell.vue';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const rankingVisible = ref(false);
const scoreSheet = ref<TrainingArchiveDetail>();

async function loadScoreSheet() {
  const attemptId = Number(route.params.attemptId);
  if (!Number.isFinite(attemptId)) return;
  loading.value = true;
  try {
    scoreSheet.value = await fetchStudentTrainingScoreSheet(attemptId);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '成绩单加载失败');
  } finally {
    loading.value = false;
  }
}

function backToTraining() {
  void router.push('/student/training');
}

function openRecording() {
  if (scoreSheet.value?.recordingUrl) {
    window.open(scoreSheet.value.recordingUrl, '_blank', 'noopener');
  }
}

onMounted(() => { void loadScoreSheet(); });
onBeforeUnmount(() => { rankingVisible.value = false; });
</script>

<style scoped>
.score-sheet-page { min-height: calc(100vh - 120px); padding-bottom: 40px; }
.score-sheet-back { display: inline-flex; align-items: center; gap: 7px; margin: 4px 0 22px; padding: 4px 0; border: 0; background: transparent; color: #64748b; cursor: pointer; font-size: 14px; }.score-sheet-back:hover { color: #2563eb; }
.score-sheet-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; margin-bottom: 22px; }.score-sheet-header p { margin: 0 0 8px; color: #2563eb; font-size: 13px; font-weight: 700; }.score-sheet-header h1 { margin: 0; color: #0f172a; font-size: 26px; line-height: 34px; }.score-sheet-header span { display: block; margin-top: 8px; color: #94a3b8; font-size: 13px; }.score-ranking-button { flex: 0 0 auto; }
.score-sheet-overview { display: grid; grid-template-columns: minmax(220px, 1.5fr) repeat(3, minmax(130px, .7fr)); align-items: center; gap: 18px; margin-bottom: 18px; padding: 22px 24px; border: 1px solid #dbeafe; border-radius: 10px; background: #f8fbff; }.score-sheet-student { display: flex; align-items: center; gap: 12px; }.score-sheet-avatar { display: grid; width: 44px; height: 44px; place-items: center; border-radius: 10px; background: #dbeafe; color: #2563eb; font-size: 21px; }.score-sheet-student div, .score-sheet-overview-item, .score-sheet-score { display: grid; gap: 5px; }.score-sheet-student span, .score-sheet-overview-item span, .score-sheet-score span { color: #94a3b8; font-size: 12px; }.score-sheet-student strong { color: #334155; font-size: 15px; }.score-sheet-student small { color: #64748b; font-size: 12px; }.score-sheet-score { grid-template-columns: auto auto; align-items: end; gap: 0 5px; }.score-sheet-score span { grid-column: 1 / -1; }.score-sheet-score strong { color: #2563eb; font-size: 32px; line-height: 34px; }.score-sheet-score em { padding-bottom: 3px; color: #2563eb; font-size: 13px; font-style: normal; }.score-sheet-overview-item strong { color: #334155; font-size: 14px; }
.score-sheet-content { display: grid; grid-template-columns: minmax(0, 1fr) 250px; gap: 18px; }.score-sheet-panel, .score-sheet-side-card { border: 1px solid #edf1f5; border-radius: 10px; background: #ffffff; }.score-sheet-panel { overflow: hidden; }.score-sheet-panel-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 14px; padding: 22px 24px 16px; border-bottom: 1px solid #f1f5f9; }.score-sheet-panel-header h2, .score-sheet-side-card h2 { margin: 0; color: #1e293b; font-size: 16px; }.score-sheet-panel-header p { margin: 7px 0 0; color: #94a3b8; font-size: 13px; }.score-sheet-panel-header > span { color: #64748b; font-size: 13px; }.score-sheet-table { width: 100%; }.step-score { color: #2563eb; }.score-sheet-recording { padding: 0 24px 22px; }.score-sheet-side { display: grid; align-content: start; gap: 18px; }.score-sheet-side-card { padding: 20px; }.score-sheet-side-card > p { margin: 12px 0 18px; color: #94a3b8; font-size: 13px; line-height: 21px; }.score-sheet-rule { display: flex; align-items: center; gap: 7px; color: #16a34a; font-size: 13px; }.rank-card-icon { display: grid; width: 38px; height: 38px; margin-bottom: 14px; place-items: center; border-radius: 9px; background: #fff7ed; color: #f59e0b; font-size: 19px; }.rank-card p { margin-bottom: 15px; }.rank-card .el-button { padding: 0; }.score-sheet-error { display: grid; min-height: 360px; place-items: center; color: #94a3b8; }.ranking-dialog-content { display: grid; justify-items: center; padding: 18px 24px 24px; text-align: center; }.ranking-dialog-icon { display: grid; width: 58px; height: 58px; place-items: center; border-radius: 15px; background: #fff7ed; color: #f59e0b; font-size: 28px; }.ranking-dialog-content h2 { margin: 16px 0 7px; color: #1e293b; font-size: 18px; }.ranking-dialog-content strong { color: #2563eb; font-size: 24px; }.ranking-dialog-content p { margin: 12px 0 0; color: #94a3b8; font-size: 13px; line-height: 21px; }
@media (max-width: 900px) { .score-sheet-overview { grid-template-columns: repeat(2, minmax(0, 1fr)); }.score-sheet-content { grid-template-columns: 1fr; }.score-sheet-side { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 600px) { .score-sheet-header { align-items: stretch; flex-direction: column; }.score-ranking-button { align-self: flex-start; }.score-sheet-overview, .score-sheet-side { grid-template-columns: 1fr; }.score-sheet-overview { padding: 18px; }.score-sheet-panel-header { padding-left: 18px; padding-right: 18px; }.score-sheet-table { min-width: 780px; }.score-sheet-panel { overflow-x: auto; }.score-sheet-recording { padding-left: 18px; padding-right: 18px; } }
</style>
