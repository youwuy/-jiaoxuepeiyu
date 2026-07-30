<template>
  <StudentShell eyebrow="个人中心" title="学习复盘">
    <section class="profile-grid">
      <article class="profile-card profile-identity">
        <div class="avatar">张</div>
        <h2>张同学</h2>
        <p>城轨运营 2401 班 · 学号 A20260001</p>
        <el-button>修改密码</el-button>
      </article>

      <article class="profile-card score-card">
        <p>综合成绩</p>
        <strong>{{ weightedScore }}</strong>
        <div v-for="item in scoreParts" :key="item.label" class="score-row">
          <span>{{ item.label }} · 权重 {{ Math.round(item.weight * 100) }}%</span>
          <el-progress :percentage="item.score" :stroke-width="7" />
        </div>
      </article>
    </section>

    <section class="profile-columns">
      <article class="profile-card">
        <div class="section-head">
          <h2>消息通知</h2>
          <el-tag type="danger">{{ unreadCount }} 条未读</el-tag>
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
          <el-button text type="primary">查看全部</el-button>
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
import { computed } from 'vue';
import StudentShell from '../../components/student/StudentShell.vue';
import {
  calculateWeightedScore,
  mockArchives,
  mockMessages,
  mockScoreParts,
  summarizeUnreadMessages
} from '../../features/student/profile';

const scoreParts = mockScoreParts;
const messages = mockMessages;
const archives = mockArchives;
const weightedScore = computed(() => calculateWeightedScore(scoreParts));
const unreadCount = computed(() => summarizeUnreadMessages(messages));
</script>
