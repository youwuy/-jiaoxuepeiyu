<template>
  <AdminShell activeKey="admin-courses">
    <section class="admin-theory-review-page">
      <header class="admin-theory-review-topbar">
        <div class="admin-theory-review-left">
          <el-button class="admin-theory-review-back" :icon="ArrowLeft" @click="goBack" />
          <el-breadcrumb class="admin-theory-review-breadcrumb" separator="/">
            <el-breadcrumb-item>教学实训</el-breadcrumb-item>
            <el-breadcrumb-item>教学课程</el-breadcrumb-item>
            <el-breadcrumb-item>批改作业</el-breadcrumb-item>
            <el-breadcrumb-item>作业批阅</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>{{ assignmentTitle }}</h1>
        <span></span>
      </header>

      <section class="admin-theory-review-info-card">
        <header>
          <el-icon><DocumentChecked /></el-icon>
          <strong>基本信息</strong>
        </header>
        <div class="admin-theory-review-info-grid">
          <p><span>学生姓名</span><strong>{{ studentName }}</strong></p>
          <p><span>学生学号</span><strong>{{ studentNo }}</strong></p>
          <p><span>所属班级</span><strong>{{ className }}</strong></p>
          <p><span>提交时间</span><strong>{{ submittedAt }}</strong></p>
          <p><span>试卷总分</span><strong>{{ totalPossibleScore }} 分</strong></p>
          <p>
            <span>核算总分</span>
            <b>{{ totalScore }} / {{ totalPossibleScore }}</b>
          </p>
        </div>
      </section>

      <main class="admin-theory-review-content">
        <div v-if="loading" class="admin-course-empty">作业详情加载中...</div>
        <el-empty v-else-if="questionSections.length === 0" description="暂无阅卷数据" />
        <section
          v-else
          v-for="section in questionSections"
          :key="section.key"
          class="admin-theory-review-section"
          :class="section.key"
        >
          <header class="admin-theory-review-section-head">
            <div>
              <span>{{ section.label }}</span>
              <strong>共 {{ section.questions.length }} 题，每题 {{ section.scorePerQuestion }} 分，满分 {{ section.total }} 分</strong>
            </div>
            <b>本类得分 {{ sectionScore(section) }} / {{ section.total }}</b>
          </header>

          <article
            v-for="question in section.questions"
            :key="question.id"
            class="admin-theory-review-question"
            :class="{ subjective: question.type === 'essay' }"
          >
            <h2>{{ question.id }}. {{ question.title }} <span>({{ question.maxScore }}分)</span></h2>

            <ul v-if="question.options?.length" class="admin-theory-review-options">
              <li v-for="option in question.options" :key="option">{{ option }}</li>
            </ul>

            <template v-if="question.type === 'essay'">
              <div class="admin-theory-review-answer-box">
                <strong>学生作答：</strong>
                <p>{{ question.studentAnswerText }}</p>
              </div>
              <div class="admin-theory-review-reference-box">
                <strong>参考答案：</strong>
                <p>{{ question.referenceAnswer }}</p>
              </div>
            </template>

            <div class="admin-theory-review-score-row">
              <p>
                <span>学生作答：</span>
                <b :class="answerTone(question)">{{ displayStudentAnswer(question) }}</b>
              </p>
              <p>
                <span>标准答案：</span>
                <b>{{ question.standardAnswer }}</b>
              </p>
              <p>
                <span>系统判分：</span>
                <strong :class="{ wrong: question.systemScore === 0 }">{{ question.systemScore }}</strong>
              </p>
              <label>
                <span>修正得分：</span>
                <el-input-number
                  v-model="question.score"
                  :min="0"
                  :max="question.maxScore"
                  :step="1"
                  controls-position="right"
                />
                <em>/ {{ question.maxScore }}分</em>
              </label>
              <small v-if="question.type === 'essay' && question.score === 0">
                <el-icon><Warning /></el-icon>
                待评分
              </small>
            </div>
          </article>
        </section>
      </main>

      <footer class="admin-theory-review-comment-bar">
        <div class="admin-theory-review-comment-label">
          <el-icon><ChatLineRound /></el-icon>
          <strong>评语</strong>
        </div>
        <div class="admin-theory-review-comment-input">
          <el-input
            v-model="comment"
            type="textarea"
            maxlength="500"
            show-word-limit
            resize="none"
            placeholder="请输入本次作业整体评语（选填）"
          />
        </div>
        <el-button class="admin-theory-review-save" type="primary" :loading="saving" :disabled="questionSections.length === 0 || !can('update')" @click="saveReview">
          <el-icon><Check /></el-icon>
          保存阅卷结果
        </el-button>
      </footer>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft, ChatLineRound, Check, DocumentChecked, Warning } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';
import {
  fetchAdminAssignmentAttemptDetail,
  reviewAdminAssignmentAttempt,
  type AdminAssignmentAttempt,
  type AdminAssignmentAttemptAnswer
} from '../../api/admin-course';
import { useAdminPermissions } from '../../features/admin/use-admin-permissions';

type QuestionType = 'choice' | 'judge' | 'blank' | 'essay';

interface ReviewQuestion {
  id: number;
  type: QuestionType;
  title: string;
  maxScore: number;
  studentAnswer: string;
  standardAnswer: string;
  systemScore: number;
  score: number;
  options?: string[];
  studentAnswerText?: string;
  referenceAnswer?: string;
}

interface ReviewSection {
  key: string;
  label: string;
  scorePerQuestion: number;
  total: number;
  questions: ReviewQuestion[];
}

const route = useRoute();
const router = useRouter();
const { can } = useAdminPermissions('teaching:course');
const courseId = computed(() => Number(route.params.id));
const attemptId = computed(() => Number(route.params.reviewId));
const attempt = ref<AdminAssignmentAttempt | null>(null);
const loading = ref(false);
const saving = ref(false);
const assignmentTitle = computed(() => attempt.value?.assignmentTitle || (route.query.assignment as string) || '作业批阅');
const studentName = computed(() => attempt.value?.studentName || (route.query.studentName as string) || '--');
const studentNo = computed(() => attempt.value?.studentNo || (route.query.studentNo as string) || '--');
const className = computed(() => attempt.value?.className || (route.query.className as string) || '--');
const submittedAt = computed(() => formatDateTime(attempt.value?.submittedAt) || (route.query.submittedAt as string) || '--');
const comment = ref('');
const questionSections = ref<ReviewSection[]>([]);

const totalScore = computed(() => questionSections.value.reduce((sum, section) => sum + sectionScore(section), 0));
const totalPossibleScore = computed(() => questionSections.value.reduce((sum, section) => sum + section.total, 0));

function sectionScore(section: ReviewSection) {
  return section.questions.reduce((sum, question) => sum + Number(question.score || 0), 0);
}

function displayStudentAnswer(question: ReviewQuestion) {
  return question.type === 'essay' ? '待评分' : question.studentAnswer;
}

function answerTone(question: ReviewQuestion) {
  if (question.type === 'essay') {
    return 'pending';
  }
  return question.studentAnswer === question.standardAnswer ? 'right' : 'wrong';
}

function goBack() {
  router.push({
    path: `/admin/courses/${courseId.value}/reviews`,
    query: { title: route.query.courseTitle as string }
  });
}

async function saveReview() {
  if (questionSections.value.length === 0) {
    ElMessage.warning('暂无可保存的阅卷数据');
    return;
  }
  saving.value = true;
  try {
    await reviewAdminAssignmentAttempt(attemptId.value, {
      reviewComment: comment.value.trim() || undefined,
      answers: questionSections.value.flatMap((section) =>
        section.questions.map((question) => ({
          questionId: question.id,
          score: Number(question.score || 0)
        }))
      )
    });
    ElMessage.success('阅卷结果已保存');
    goBack();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '阅卷结果保存失败');
  } finally {
    saving.value = false;
  }
}

function formatDateTime(value?: string) {
  if (!value) {
    return '';
  }

  const normalized = value.includes('T') ? value.replace('T', ' ') : value;
  return normalized.slice(0, 16);
}

function normalizeQuestionType(value?: string): QuestionType {
  const type = (value || '').toUpperCase();
  if (type.includes('JUDGE') || type.includes('TRUE_FALSE')) {
    return 'judge';
  }
  if (type.includes('BLANK')) {
    return 'blank';
  }
  if (type.includes('ESSAY') || type.includes('SUBJECTIVE')) {
    return 'essay';
  }
  return 'choice';
}

function sectionMeta(type: QuestionType) {
  if (type === 'judge') {
    return { key: 'judge', label: '判断题' };
  }
  if (type === 'blank') {
    return { key: 'blank', label: '填空题' };
  }
  if (type === 'essay') {
    return { key: 'essay', label: '简答题' };
  }
  return { key: 'single', label: '选择题' };
}

function mapAnswerQuestion(answer: AdminAssignmentAttemptAnswer): ReviewQuestion {
  const type = normalizeQuestionType(answer.questionType);
  const score = Number(answer.score ?? 0);
  return {
    id: answer.questionId,
    type,
    title: answer.title || '未命名题目',
    maxScore: Number(answer.questionScore ?? 0),
    studentAnswer: answer.answerContent || '',
    standardAnswer: answer.standardAnswer || '',
    systemScore: score,
    score,
    studentAnswerText: answer.answerContent || '',
    referenceAnswer: answer.standardAnswer || ''
  };
}

function buildSections(answers: AdminAssignmentAttemptAnswer[]) {
  const grouped = new Map<string, ReviewSection>();
  answers.forEach((answer) => {
    const question = mapAnswerQuestion(answer);
    const meta = sectionMeta(question.type);
    const existing =
      grouped.get(meta.key) ||
      ({
        key: meta.key,
        label: meta.label,
        scorePerQuestion: question.maxScore,
        total: 0,
        questions: []
      } satisfies ReviewSection);
    existing.questions.push(question);
    existing.total += question.maxScore;
    existing.scorePerQuestion = existing.questions.length === 1 ? question.maxScore : existing.scorePerQuestion;
    grouped.set(meta.key, existing);
  });

  return Array.from(grouped.values());
}

async function loadAttemptDetail() {
  loading.value = true;
  try {
    const detail = await fetchAdminAssignmentAttemptDetail(attemptId.value);
    attempt.value = detail;
    comment.value = detail.reviewComment || '';
    if (detail.answers?.length) {
      questionSections.value = buildSections(detail.answers);
    }
  } catch (error) {
    ElMessage.warning(error instanceof Error ? error.message : '作业详情接口暂不可用');
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadAttemptDetail();
});
</script>
