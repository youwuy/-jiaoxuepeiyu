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
          <p><span>试卷总分</span><strong>100 分</strong></p>
          <p>
            <span>核算总分</span>
            <b>{{ totalScore }} / 100</b>
          </p>
        </div>
      </section>

      <main class="admin-theory-review-content">
        <section
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
        <el-button class="admin-theory-review-save" type="primary" @click="saveReview">
          <el-icon><Check /></el-icon>
          保存阅卷结果
        </el-button>
      </footer>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft, ChatLineRound, Check, DocumentChecked, Warning } from '@element-plus/icons-vue';
import AdminShell from '../../components/admin/AdminShell.vue';

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
const courseId = computed(() => Number(route.params.id));
const assignmentTitle = computed(() => (route.query.assignment as string) || '第1.1节作业');
const studentName = computed(() => (route.query.studentName as string) || '张明远');
const studentNo = computed(() => (route.query.studentNo as string) || '2023XH9201');
const className = computed(() => (route.query.className as string) || '城轨1班');
const submittedAt = computed(() => (route.query.submittedAt as string) || '2025-04-10 14:32');
const comment = ref('');

const questionSections = reactive<ReviewSection[]>([
  {
    key: 'single',
    label: '单选题',
    scorePerQuestion: 5,
    total: 25,
    questions: [
      {
        id: 1,
        type: 'choice',
        title: '城市轨道交通信号系统中，联锁设备的核心功能是什么？',
        maxScore: 5,
        studentAnswer: 'B',
        standardAnswer: 'B',
        systemScore: 5,
        score: 5,
        options: ['A. 控制列车运行速度', 'B. 保证列车运行安全，防止冲突', 'C. 管理车站票务流量', 'D. 调度车辆维修计划']
      },
      {
        id: 2,
        type: 'choice',
        title: 'ATS系统的主要功能不包括以下哪一项？',
        maxScore: 5,
        studentAnswer: 'A',
        standardAnswer: 'C',
        systemScore: 0,
        score: 0,
        options: ['A. 列车运行监控', 'B. 列车自动调度', 'C. 轨道电路检测', 'D. 运行图管理']
      },
      {
        id: 3,
        type: 'choice',
        title: '信号机显示红灯时，表示什么意思？',
        maxScore: 5,
        studentAnswer: 'C',
        standardAnswer: 'C',
        systemScore: 5,
        score: 5,
        options: ['A. 允许通过', 'B. 减速运行', 'C. 禁止越过', 'D. 准备停车']
      },
      {
        id: 4,
        type: 'choice',
        title: '城市轨道交通中，ATP系统的全称是什么？',
        maxScore: 5,
        studentAnswer: 'A',
        standardAnswer: 'A',
        systemScore: 5,
        score: 5,
        options: ['A. Automatic Train Protection', 'B. Automatic Train Operation', 'C. Automatic Train Supervision', 'D. Automatic Train Control']
      },
      {
        id: 5,
        type: 'choice',
        title: '道岔转换设备属于信号系统的哪个子系统？',
        maxScore: 5,
        studentAnswer: 'C',
        standardAnswer: 'C',
        systemScore: 5,
        score: 5,
        options: ['A. ATS子系统', 'B. ATP子系统', 'C. 联锁子系统', 'D. ATO子系统']
      }
    ]
  },
  {
    key: 'multiple',
    label: '多选题',
    scorePerQuestion: 8,
    total: 24,
    questions: [
      {
        id: 6,
        type: 'choice',
        title: '以下哪些属于城市轨道交通信号系统的组成部分？',
        maxScore: 8,
        studentAnswer: 'AB',
        standardAnswer: 'ABD',
        systemScore: 0,
        score: 0,
        options: ['A. 联锁设备', 'B. 闭塞设备', 'C. 售票系统', 'D. ATS系统']
      },
      {
        id: 7,
        type: 'choice',
        title: '联锁设备需要检查的基本联锁条件包括？',
        maxScore: 8,
        studentAnswer: 'ABC',
        standardAnswer: 'ABC',
        systemScore: 8,
        score: 8,
        options: ['A. 进路空闲', 'B. 道岔位置正确', 'C. 敌对进路未建立', 'D. 列车速度达标']
      },
      {
        id: 8,
        type: 'choice',
        title: 'ATP系统可以实现的功能包括？',
        maxScore: 8,
        studentAnswer: 'ABD',
        standardAnswer: 'ABD',
        systemScore: 8,
        score: 8,
        options: ['A. 超速防护', 'B. 列车定位', 'C. 自动开关车门', 'D. 安全间隔监控']
      }
    ]
  },
  {
    key: 'judge',
    label: '判断题',
    scorePerQuestion: 3,
    total: 15,
    questions: [
      { id: 9, type: 'judge', title: '联锁设备是保证车站列车或调车作业安全的信号设备。', maxScore: 3, studentAnswer: '正确', standardAnswer: '正确', systemScore: 3, score: 3 },
      { id: 10, type: 'judge', title: 'ATS系统可以直接控制列车运行速度。', maxScore: 3, studentAnswer: '错误', standardAnswer: '错误', systemScore: 3, score: 3 },
      { id: 11, type: 'judge', title: '轨道电路仅用于检测列车占用，不具备传递信息功能。', maxScore: 3, studentAnswer: '正确', standardAnswer: '错误', systemScore: 0, score: 0 },
      { id: 12, type: 'judge', title: 'CBTC系统是基于通信的列车控制系统。', maxScore: 3, studentAnswer: '正确', standardAnswer: '正确', systemScore: 3, score: 3 },
      { id: 13, type: 'judge', title: '信号机黄灯表示允许列车按规定速度通过。', maxScore: 3, studentAnswer: '错误', standardAnswer: '错误', systemScore: 3, score: 3 }
    ]
  },
  {
    key: 'blank',
    label: '填空题',
    scorePerQuestion: 4,
    total: 12,
    questions: [
      { id: 14, type: 'blank', title: '城市轨道交通信号系统的核心目标是保障列车运行______和提高运输效率。', maxScore: 4, studentAnswer: '安全', standardAnswer: '安全', systemScore: 4, score: 4 },
      { id: 15, type: 'blank', title: 'CBTC系统的中文全称是______列车控制系统。', maxScore: 4, studentAnswer: '基于通信的', standardAnswer: '基于通信的', systemScore: 4, score: 4 },
      { id: 16, type: 'blank', title: '道岔的位置和区段状态由______设备进行检测和锁闭。', maxScore: 4, studentAnswer: '联锁', standardAnswer: '转辙', systemScore: 0, score: 0 }
    ]
  },
  {
    key: 'essay',
    label: '简答题',
    scorePerQuestion: 12,
    total: 24,
    questions: [
      {
        id: 17,
        type: 'essay',
        title: '请简述城市轨道交通信号系统中联锁设备的主要功能及其工作原理。',
        maxScore: 12,
        studentAnswer: '待评分',
        standardAnswer: '参考答案',
        systemScore: 0,
        score: 0,
        studentAnswerText:
          '联锁设备是城市轨道交通信号系统的核心组成部分，主要功能包括：进路排列与进路安全、防止敌对进路同时建立、对道岔进行位置检测和锁闭、与信号机联动。工作原理是通过采集轨道区段、道岔位置和信号显示状态，依据联锁逻辑运算，控制信号机和道岔设备，保证列车运行的安全防护。',
        referenceAnswer:
          '联锁设备主要功能包括进路建立与锁闭、道岔控制与位置检测、信号机控制、敌对进路检查、进路解锁等。工作原理是通过采集现场设备状态信息，根据联锁计算机逻辑判断，满足条件后开放信号、锁闭进路，确保列车运行安全。'
      },
      {
        id: 18,
        type: 'essay',
        title: '请分析ATP系统与ATO系统在城市轨道交通中的分工与协作关系。',
        maxScore: 12,
        studentAnswer: '待评分',
        standardAnswer: '参考答案',
        systemScore: 0,
        score: 0,
        studentAnswerText:
          'ATP系统主要负责列车运行的安全防护，包括测速防护、列车定位、安全间隔监控等功能，确保列车在任何情况下都不会发生碰撞或超速事故。ATO系统则负责列车的自动驾驶，包括自动启动、自动加速、自动减速、精确停车等功能。两者分工明确，ATP保障安全底线，ATO在ATP的安全防护下实现自动化运行。',
        referenceAnswer:
          'ATP负责安全防护，包括速度监督、移动授权、列车间隔控制等，是安全底线；ATO负责自动运行，包括牵引、制动和精准停车。二者协同工作时，ATO发出驾驶指令，ATP持续监督并在超限时采取制动，保证自动运行既高效又安全。'
      }
    ]
  }
]);

const totalScore = computed(() => questionSections.reduce((sum, section) => sum + sectionScore(section), 0));

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

function saveReview() {
  ElMessage.success('阅卷结果已保存');
  goBack();
}
</script>
