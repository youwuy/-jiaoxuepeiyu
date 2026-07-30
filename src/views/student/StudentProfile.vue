<template>
  <StudentShell eyebrow="个人中心" title="学习复盘">
    <section class="profile-hero">
      <article class="profile-card profile-identity">
        <div class="avatar">张</div>
        <div>
          <h2>{{ student.name }}</h2>
          <p>{{ student.className }} · 学号 {{ student.studentId }}</p>
        </div>
        <el-button class="profile-edit">修改密码</el-button>
      </article>

      <article class="profile-score-summary">
        <span>综合成绩</span>
        <strong>{{ weightedScore }}</strong>
      </article>
      <article class="profile-score-summary">
        <span>未读消息</span>
        <strong>{{ unreadCount }}</strong>
      </article>
      <article class="profile-score-summary">
        <span>实训档案</span>
        <strong>{{ archives.length }}</strong>
      </article>
    </section>

    <section class="profile-columns">
      <article class="profile-card score-card">
        <div class="section-head">
          <h2>成绩构成</h2>
          <span class="resource-pill">实时统计</span>
        </div>
        <div v-for="item in scoreParts" :key="item.label" class="score-row">
          <span>{{ item.label }} · 权重 {{ Math.round(item.weight * 100) }}%</span>
          <el-progress :percentage="item.score" :stroke-width="7" />
        </div>
      </article>

      <article class="profile-card">
        <div class="section-head">
          <h2>消息通知</h2>
          <span class="course-status-pill is-notStarted">{{ unreadCount }} 条未读</span>
        </div>
        <button v-for="message in messages" :key="message.id" class="message-row" :class="{ unread: message.unread }">
          <span>{{ message.type }}</span>
          <strong>{{ message.title }}</strong>
          <em>{{ message.time }}</em>
        </button>
      </article>

      <article class="profile-card">
        <div class="section-head">
          <h2>实训档案</h2>
          <el-button class="resource-action" text type="primary">查看全部</el-button>
        </div>
        <div v-for="archive in archives" :key="archive.id" class="archive-row">
          <div>
            <strong>{{ archive.title }}</strong>
            <span>{{ archive.finishedAt }} · 用时 {{ archive.duration }}</span>
          </div>
          <b>{{ archive.score }} 分</b>
        </div>
      </article>
    </section>
  </StudentShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { fetchStudentProfile } from '../../api/student';
import StudentShell from '../../components/student/StudentShell.vue';
import {
  calculateWeightedScore,
  mockArchives,
  mockMessages,
  mockScoreParts,
  summarizeUnreadMessages,
  type ScorePart,
  type StudentMessage,
  type TrainingArchive
} from '../../features/student/profile';

const student = ref({
  name: '张林林',
  className: '城轨运营 2401 班',
  studentId: 'A20260001'
});
const scoreParts = ref<ScorePart[]>(mockScoreParts);
const messages = ref<StudentMessage[]>(mockMessages);
const archives = ref<TrainingArchive[]>(mockArchives);
const weightedScore = computed(() => calculateWeightedScore(scoreParts.value));
const unreadCount = computed(() => summarizeUnreadMessages(messages.value));

onMounted(async () => {
  try {
    const profile = await fetchStudentProfile();
    student.value = profile.student ?? student.value;
    scoreParts.value = profile.scoreParts ?? scoreParts.value;
    messages.value = profile.messages ?? messages.value;
    archives.value = profile.archives ?? archives.value;
  } catch {
    ElMessage.warning('后端个人中心接口暂不可用，已展示本地示例数据');
  }
});
</script>
