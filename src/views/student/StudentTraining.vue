<template>
  <StudentShell eyebrow="实训中心" title="我的实训">
    <section class="module-toolbar">
      <el-segmented v-model="mode" :options="modeOptions" />
      <el-select v-model="status" class="compact-select" placeholder="状态">
        <el-option label="全部状态" value="all" />
        <el-option label="可进入" value="available" />
        <el-option label="未开始" value="notStarted" />
        <el-option label="已完成" value="completed" />
      </el-select>
      <el-input v-model="keyword" class="module-search" :prefix-icon="Search" placeholder="搜索实训名称" clearable />
    </section>

    <section class="training-grid">
      <article v-for="training in visibleTrainings" :key="training.id" class="training-card">
        <div>
          <el-tag :type="training.mode === 'team' ? 'warning' : 'success'">
            {{ training.mode === 'team' ? '多人实训' : '单人实训' }}
          </el-tag>
          <el-tag effect="plain">{{ statusText[training.status] }}</el-tag>
        </div>
        <h2>{{ training.title }}</h2>
        <p>{{ training.courseName }}</p>
        <dl>
          <div>
            <dt>截止时间</dt>
            <dd>{{ training.deadline }}</dd>
          </div>
          <div>
            <dt>练习次数</dt>
            <dd>{{ training.attempts || '-' }}</dd>
          </div>
          <div>
            <dt>最高成绩</dt>
            <dd>{{ training.bestScore ?? '-' }}</dd>
          </div>
        </dl>
        <p v-if="training.roles?.length" class="role-line">角色：{{ training.roles.join('、') }}</p>
        <el-button type="primary" :disabled="training.status === 'notStarted'">
          {{ training.status === 'completed' ? '再次实训' : '开始实训' }}
        </el-button>
      </article>
    </section>
  </StudentShell>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { Search } from '@element-plus/icons-vue';
import StudentShell from '../../components/student/StudentShell.vue';
import { filterTrainings, mockTrainings, type TrainingModeFilter, type TrainingStatus } from '../../features/student/training';

const mode = ref<TrainingModeFilter>('all');
const status = ref<TrainingStatus | 'all'>('all');
const keyword = ref('');
const modeOptions = [
  { label: '全部', value: 'all' },
  { label: '单人实训', value: 'single' },
  { label: '多人实训', value: 'team' }
];

const statusText: Record<TrainingStatus, string> = {
  available: '可进入',
  notStarted: '未开始',
  completed: '已完成'
};

const visibleTrainings = computed(() =>
  filterTrainings(mockTrainings, {
    mode: mode.value,
    status: status.value,
    keyword: keyword.value
  })
);
</script>
