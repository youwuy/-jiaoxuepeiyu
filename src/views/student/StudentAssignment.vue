<template>
  <StudentShell eyebrow="课程学习" title="课程作业">
    <section v-loading="loading" class="student-assignment-page">
      <header class="student-assignment-header">
        <el-button :icon="ArrowLeft" title="返回课程学习" aria-label="返回课程学习" @click="goBack" />
        <div>
          <h1>{{ assignment?.assignmentTitle || '课程作业' }}{{ submitted ? ' · 答题报告' : '' }}</h1>
          <p v-if="assignment">
            共 {{ assignment.questions.length }} 题 · 总分 {{ assignment.totalScore }} 分
            <span :class="{ danger: expired }">截止 {{ formatDateTime(answerDeadline) }}</span>
          </p>
        </div>
        <el-button v-if="submitted && canRetry" @click="retryAssignment">再做一次</el-button>
      </header>

      <div v-if="assignment" class="student-assignment-layout">
        <aside class="student-assignment-answer-sheet">
          <strong>答题卡</strong>
          <div>
            <button
              v-for="(question, index) in assignment.questions"
              :key="question.questionId"
              type="button"
              :class="{ answered: isAnswered(question), active: currentIndex === index }"
              @click="selectQuestion(index)"
            >{{ index + 1 }}</button>
          </div>
          <p v-if="!submitted">已答 {{ answeredCount }} 题 · 未答 {{ assignment.questions.length - answeredCount }} 题</p>
          <template v-else>
            <div class="student-assignment-result-score">
              <span>本次得分</span>
              <b>{{ report?.score ?? '-' }}<small>/{{ assignment.totalScore }}</small></b>
            </div>
            <p>提交时间：{{ formatDateTime(report?.submittedAt) }}</p>
            <p v-if="report?.reviewComment">教师评语：{{ report.reviewComment }}</p>
          </template>
        </aside>

        <main v-if="!submitted && currentQuestion" class="student-assignment-question-stage">
          <article class="student-assignment-question">
            <header>
              <div>
                <b>{{ currentIndex + 1 }}</b>
                <span>{{ typeLabel(currentQuestion.questionType) }}</span>
                <em>{{ currentQuestion.score }}分</em>
              </div>
              <strong>{{ currentQuestion.title }}</strong>
            </header>

            <el-radio-group
              v-if="['SINGLE', 'JUDGE'].includes(normalizeType(currentQuestion.questionType))"
              v-model="answers[currentQuestion.questionId]"
              :disabled="answerLocked"
              class="student-assignment-options"
            >
              <el-radio v-for="option in questionOptions(currentQuestion)" :key="option.optionKey" :value="option.optionKey">
                <b>{{ option.optionKey }}</b><span>{{ option.optionText }}</span>
              </el-radio>
            </el-radio-group>

            <el-checkbox-group
              v-else-if="normalizeType(currentQuestion.questionType) === 'MULTIPLE'"
              v-model="multipleAnswers[currentQuestion.questionId]"
              :disabled="answerLocked"
              class="student-assignment-options"
            >
              <el-checkbox v-for="option in questionOptions(currentQuestion)" :key="option.optionKey" :value="option.optionKey">
                <b>{{ option.optionKey }}</b><span>{{ option.optionText }}</span>
              </el-checkbox>
            </el-checkbox-group>

            <div v-else class="student-assignment-text-answer">
              <el-input
                v-model="answers[currentQuestion.questionId]"
                :disabled="answerLocked"
                type="textarea"
                :rows="normalizeType(currentQuestion.questionType) === 'SHORT_ANSWER' ? 8 : 4"
                placeholder="请输入你的答案..."
              />
              <span v-if="normalizeType(currentQuestion.questionType) === 'SHORT_ANSWER'">{{ answers[currentQuestion.questionId]?.length || 0 }} 字</span>
            </div>

            <el-alert v-if="answerLocked" :title="expired ? '作业已超过截止时间，仅可查看已填写内容' : '作业暂未开放答题'" type="warning" :closable="false" />
          </article>

          <footer class="student-assignment-navigation">
            <el-button v-if="currentIndex > 0" :icon="ArrowLeft" @click="selectQuestion(currentIndex - 1)">上一题</el-button>
            <span></span>
            <el-button v-if="currentIndex < assignment.questions.length - 1" type="primary" @click="selectQuestion(currentIndex + 1)">
              下一题<el-icon><ArrowRight /></el-icon>
            </el-button>
            <el-button v-else type="primary" :disabled="answerLocked" :loading="submitting" @click="submitAssignment">提交作业</el-button>
          </footer>
        </main>

        <main v-else-if="submitted" class="student-assignment-report">
          <section class="student-assignment-report-summary">
            <span>提交时间<strong>{{ formatDateTime(report?.submittedAt) }}</strong></span>
            <span>试卷满分<strong>{{ assignment.totalScore }}分</strong></span>
            <span>本次得分<strong>{{ report?.score ?? '-' }}/{{ assignment.totalScore }}</strong></span>
          </section>
          <article v-for="(question, index) in assignment.questions" :id="`assignment-question-${question.questionId}`" :key="question.questionId" class="student-assignment-question report-question">
            <header>
              <div><b>{{ index + 1 }}</b><span>{{ typeLabel(question.questionType) }}</span><em>{{ question.score }}分</em></div>
              <strong>{{ question.title }}</strong>
            </header>
            <div class="student-assignment-report-answer student">
              <span>学生作答</span><p>{{ reportAnswer(question.questionId)?.answerContent || '未作答' }}</p>
            </div>
            <div class="student-assignment-report-answer standard">
              <span>标准答案</span><p>{{ reportAnswer(question.questionId)?.standardAnswer || '-' }}</p>
            </div>
            <strong class="student-assignment-question-score">得分 {{ reportAnswer(question.questionId)?.score ?? 0 }} 分</strong>
          </article>
        </main>

        <el-empty v-else description="该作业暂未配置试题" />
      </div>
      <el-empty v-else-if="!loading" description="作业不存在或暂未开放" />
    </section>

    <el-dialog v-model="successVisible" width="420px" :show-close="false" :close-on-click-modal="false" :close-on-press-escape="false" append-to-body>
      <div class="student-assignment-success">
        <el-icon><CircleCheckFilled /></el-icon>
        <div class="student-assignment-stars">★ ★ ★</div>
        <h2>恭喜你已完成该作业</h2>
        <p>太棒了！你的努力得到了回报，继续保持哦~</p>
        <el-button type="success" @click="viewReport">查看答题报告</el-button>
        <el-button @click="retryAssignment">再做一次</el-button>
      </div>
    </el-dialog>
  </StudentShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeft, ArrowRight, CircleCheckFilled } from '@element-plus/icons-vue';
import StudentShell from '../../components/student/StudentShell.vue';
import {
  fetchStudentAssignment,
  fetchStudentAssignmentReport,
  retryStudentAssignment,
  saveStudentAssignmentAnswers,
  submitStudentAssignment,
  type StudentAssignmentDetail,
  type StudentAssignmentOption,
  type StudentAssignmentQuestion,
  type StudentAssignmentReport
} from '../../api/student';

const route = useRoute();
const router = useRouter();
const assignmentId = computed(() => Number(route.params.id));
const courseId = computed(() => Number(route.query.courseId));
const assignment = ref<StudentAssignmentDetail | null>(null);
const report = ref<StudentAssignmentReport | null>(null);
const loading = ref(false);
const submitting = ref(false);
const successVisible = ref(false);
const currentIndex = ref(0);
const answers = reactive<Record<number, string>>({});
const multipleAnswers = reactive<Record<number, string[]>>({});
const currentQuestion = computed(() => assignment.value?.questions[currentIndex.value]);
const submitted = computed(() => ['SUBMITTED', 'REVIEWED'].includes(String(assignment.value?.status || '').toUpperCase()));
const answerDeadline = computed(() => assignment.value?.answerEndTime || assignment.value?.deadline);
const expired = computed(() => Boolean(answerDeadline.value && Date.now() > new Date(answerDeadline.value).getTime()));
const notOpen = computed(() => Boolean(assignment.value?.answerStartTime && Date.now() < new Date(assignment.value.answerStartTime).getTime()));
const answerLocked = computed(() => expired.value || notOpen.value);
const canRetry = computed(() => !answerLocked.value);
const answeredCount = computed(() => assignment.value?.questions.filter(isAnswered).length ?? 0);

onMounted(() => loadAssignment(true));

async function loadAssignment(autoRetryExam: boolean) {
  if (!Number.isFinite(assignmentId.value) || assignmentId.value <= 0) {
    ElMessage.error('作业参数不正确');
    return;
  }
  loading.value = true;
  try {
    let detail = await fetchStudentAssignment(assignmentId.value);
    const isSubmitted = ['SUBMITTED', 'REVIEWED'].includes(String(detail.status || '').toUpperCase());
    if (autoRetryExam && isSubmitted && String(detail.publishMode || '').toUpperCase() === 'EXAM' && isWithinWindow(detail)) {
      await retryStudentAssignment(assignmentId.value);
      detail = await fetchStudentAssignment(assignmentId.value);
    }
    assignment.value = detail;
    initializeAnswers(detail.questions);
    currentIndex.value = 0;
    report.value = ['SUBMITTED', 'REVIEWED'].includes(String(detail.status || '').toUpperCase())
      ? await fetchStudentAssignmentReport(assignmentId.value)
      : null;
  } catch (error) {
    assignment.value = null;
    ElMessage.error(error instanceof Error ? error.message : '作业加载失败');
  } finally {
    loading.value = false;
  }
}

function initializeAnswers(questions: StudentAssignmentQuestion[]) {
  Object.keys(answers).forEach((key) => delete answers[Number(key)]);
  Object.keys(multipleAnswers).forEach((key) => delete multipleAnswers[Number(key)]);
  questions.forEach((question) => {
    const content = question.answerContent || '';
    if (normalizeType(question.questionType) === 'MULTIPLE') {
      multipleAnswers[question.questionId] = content.split(',').map((item) => item.trim()).filter(Boolean);
    } else {
      answers[question.questionId] = content;
    }
  });
}

function normalizeType(type?: string) {
  const value = String(type || '').toUpperCase();
  if (value === 'FILL') return 'FILL_BLANK';
  if (value === 'SHORT') return 'SHORT_ANSWER';
  return value;
}

function typeLabel(type?: string) {
  return ({ SINGLE: '单选题', MULTIPLE: '多选题', JUDGE: '判断题', FILL: '填空题', FILL_BLANK: '填空题', SHORT: '简答题', SHORT_ANSWER: '简答题' } as Record<string, string>)[String(type || '').toUpperCase()] || '问答题';
}

function questionOptions(question: StudentAssignmentQuestion): StudentAssignmentOption[] {
  if (normalizeType(question.questionType) === 'JUDGE') return [{ optionKey: 'TRUE', optionText: '正确' }, { optionKey: 'FALSE', optionText: '错误' }];
  if (!question.options) return [];
  try {
    const parsed = JSON.parse(question.options) as Array<{ optionKey?: string; optionText?: string; key?: string; text?: string }>;
    return Array.isArray(parsed) ? parsed.map((item) => ({ optionKey: item.optionKey || item.key || '', optionText: item.optionText || item.text || '' })).filter((item) => item.optionKey) : [];
  } catch {
    return [];
  }
}

function isAnswered(question: StudentAssignmentQuestion) {
  return normalizeType(question.questionType) === 'MULTIPLE'
    ? (multipleAnswers[question.questionId]?.length ?? 0) > 0
    : Boolean(answers[question.questionId]?.trim());
}

function answerPayload() {
  return (assignment.value?.questions ?? []).map((question) => ({
    questionId: question.questionId,
    answerContent: normalizeType(question.questionType) === 'MULTIPLE'
      ? [...(multipleAnswers[question.questionId] ?? [])].sort().join(',')
      : (answers[question.questionId] || '').trim()
  }));
}

async function submitAssignment() {
  const unanswered = (assignment.value?.questions.length ?? 0) - answeredCount.value;
  const message = unanswered > 0
    ? `当前存在 ${unanswered} 道未作答题目，确认提交将按空白计分，是否提交？`
    : '提交后将生成本次答题报告，是否确认提交？';
  try {
    await ElMessageBox.confirm(message, '提交作业', { type: 'warning', confirmButtonText: '确认提交', cancelButtonText: '继续检查' });
  } catch {
    return;
  }
  submitting.value = true;
  try {
    await saveStudentAssignmentAnswers(assignmentId.value, answerPayload());
    await submitStudentAssignment(assignmentId.value);
    successVisible.value = true;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '提交失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
}

async function viewReport() {
  successVisible.value = false;
  await loadAssignment(false);
}

async function retryAssignment() {
  try {
    await retryStudentAssignment(assignmentId.value);
    successVisible.value = false;
    await loadAssignment(false);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '暂时无法重新作答');
  }
}

function selectQuestion(index: number) {
  currentIndex.value = Math.min(Math.max(index, 0), Math.max((assignment.value?.questions.length ?? 1) - 1, 0));
  if (submitted.value) document.getElementById(`assignment-question-${assignment.value?.questions[currentIndex.value]?.questionId}`)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function reportAnswer(questionId: number) {
  return report.value?.answers?.find((item) => item.questionId === questionId);
}

function isWithinWindow(detail: StudentAssignmentDetail) {
  const now = Date.now();
  const startsAt = detail.answerStartTime ? new Date(detail.answerStartTime).getTime() : null;
  const endsAt = detail.answerEndTime || detail.deadline ? new Date(detail.answerEndTime || detail.deadline || '').getTime() : null;
  return (!startsAt || now >= startsAt) && (!endsAt || now <= endsAt);
}

function goBack() {
  router.push(Number.isFinite(courseId.value) && courseId.value > 0 ? `/student/courses/${courseId.value}/learn` : '/student/courses');
}

function formatDateTime(value?: string) {
  return value ? value.replace('T', ' ').slice(0, 19) : '-';
}
</script>

<style scoped>
.student-assignment-page { min-height: calc(100vh - 72px); padding: 28px 36px 48px; background: #f5f7fa; color: #1f2d3d; }
.student-assignment-header { display: grid; grid-template-columns: 40px minmax(0, 1fr) auto; align-items: center; gap: 14px; max-width: 1180px; margin: 0 auto 20px; }
.student-assignment-header h1 { margin: 0 0 6px; font-size: 22px; }
.student-assignment-header p { margin: 0; color: #8291a8; font-size: 13px; }
.student-assignment-header p span { margin-left: 20px; }.student-assignment-header p span.danger { color: #ef4444; }
.student-assignment-layout { display: grid; grid-template-columns: 220px minmax(0, 1fr); align-items: start; gap: 20px; max-width: 1180px; margin: 0 auto; }
.student-assignment-answer-sheet { position: sticky; top: 20px; padding: 20px; border: 1px solid #e1e7ef; border-radius: 8px; background: #fff; }
.student-assignment-answer-sheet > strong { display: block; margin-bottom: 16px; }.student-assignment-answer-sheet > div:first-of-type { display: grid; grid-template-columns: repeat(5, 30px); gap: 8px; }
.student-assignment-answer-sheet button { width: 30px; height: 30px; border: 1px solid #d5deea; border-radius: 4px; background: #fff; color: #718096; cursor: pointer; }
.student-assignment-answer-sheet button.answered { border-color: #4d8dff; background: #4d8dff; color: #fff; }.student-assignment-answer-sheet button.active { box-shadow: 0 0 0 2px #1d4ed8 inset; }
.student-assignment-answer-sheet > p { margin: 16px 0 0; color: #8291a8; font-size: 13px; line-height: 1.7; }
.student-assignment-result-score { margin-top: 20px; padding-top: 18px; border-top: 1px solid #edf0f5; }.student-assignment-result-score span { display: block; color: #8291a8; font-size: 13px; }
.student-assignment-result-score b { display: block; margin-top: 4px; color: #2563eb; font-size: 28px; }.student-assignment-result-score small { font-size: 14px; }
.student-assignment-question-stage, .student-assignment-report { display: grid; gap: 14px; }.student-assignment-question { scroll-margin-top: 18px; min-height: 420px; padding: 28px 32px; border: 1px solid #e1e7ef; border-radius: 8px; background: #fff; }
.student-assignment-question > header { margin-bottom: 24px; }.student-assignment-question > header div { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
.student-assignment-question > header div > b { display: grid; place-items: center; width: 26px; height: 26px; border-radius: 4px; background: #edf4ff; color: #2563eb; }
.student-assignment-question > header div > span { color: #2563eb; font-size: 13px; }.student-assignment-question > header div > em { color: #8a98ad; font-size: 13px; font-style: normal; }
.student-assignment-question > header > strong { display: block; font-size: 16px; line-height: 1.8; }.student-assignment-options { display: grid; gap: 12px; align-items: start; }
.student-assignment-options :deep(.el-radio), .student-assignment-options :deep(.el-checkbox) { height: auto; margin: 0; padding: 12px 14px; border: 1px solid #e7ebf1; border-radius: 6px; white-space: normal; }
.student-assignment-options :deep(.el-radio.is-checked), .student-assignment-options :deep(.el-checkbox.is-checked) { border-color: #4d8dff; background: #edf4ff; }
.student-assignment-options :deep(.el-radio__label), .student-assignment-options :deep(.el-checkbox__label) { display: flex; gap: 10px; line-height: 1.6; }.student-assignment-options b { color: #2563eb; }
.student-assignment-text-answer { position: relative; }.student-assignment-text-answer > span { position: absolute; right: 12px; bottom: 8px; color: #94a3b8; font-size: 12px; }
.student-assignment-question :deep(.el-alert) { margin-top: 18px; }.student-assignment-navigation { display: grid; grid-template-columns: auto 1fr auto; padding: 4px 0; }
.student-assignment-report-summary { display: grid; grid-template-columns: repeat(3, 1fr); padding: 20px 28px; border: 1px solid #e1e7ef; border-radius: 8px; background: #fff; }
.student-assignment-report-summary span { color: #8291a8; font-size: 13px; }.student-assignment-report-summary strong { display: block; margin-top: 6px; color: #1f2d3d; font-size: 16px; }
.report-question { min-height: 0; }.student-assignment-report-answer { display: grid; grid-template-columns: 80px 1fr; gap: 12px; margin-top: 10px; padding: 12px 14px; border-radius: 6px; }
.student-assignment-report-answer.student { background: #fff4f4; }.student-assignment-report-answer.standard { background: #effaf4; }.student-assignment-report-answer span { font-weight: 700; }.student-assignment-report-answer p { margin: 0; white-space: pre-wrap; }
.student-assignment-question-score { display: block; margin-top: 14px; color: #2563eb; text-align: right; }.student-assignment-success { display: grid; justify-items: center; padding: 14px 20px 8px; text-align: center; }
.student-assignment-success > .el-icon { color: #10b981; font-size: 64px; }.student-assignment-stars { margin-top: 10px; color: #f59e0b; font-size: 22px; }.student-assignment-success h2 { margin: 16px 0 6px; }.student-assignment-success p { margin: 0 0 20px; color: #94a3b8; }
.student-assignment-success .el-button { width: 100%; margin: 8px 0 0; }
@media (max-width: 760px) { .student-assignment-page { padding: 20px 14px 36px; }.student-assignment-layout { grid-template-columns: 1fr; }.student-assignment-answer-sheet { position: static; }.student-assignment-header { grid-template-columns: 40px minmax(0, 1fr); }.student-assignment-header > .el-button:last-child { grid-column: 2; justify-self: start; }.student-assignment-question { min-height: 360px; padding: 20px 16px; }.student-assignment-report-summary { grid-template-columns: 1fr; gap: 14px; } }
</style>
