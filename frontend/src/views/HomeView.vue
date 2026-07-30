<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { http } from '../api/http';

const backendStatus = ref('检测中');

onMounted(async () => {
  try {
    const { data } = await http.get('/health');
    backendStatus.value = `${data.status} - ${data.service}`;
  } catch (error) {
    backendStatus.value = '后台未启动或接口不可达';
  }
});
</script>

<template>
  <main class="shell">
    <section class="panel">
      <div>
        <p class="eyebrow">Web教辅系统</p>
        <h1>项目开发环境已初始化</h1>
        <p class="summary">前端 Vue 3 + Vite，后台 Java 8 + Spring Boot，数据库 MySQL 5.7.42。</p>
      </div>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="后台接口">{{ backendStatus }}</el-descriptions-item>
        <el-descriptions-item label="占工分支">zhan/frontend-test</el-descriptions-item>
        <el-descriptions-item label="陈工分支">chen/backend</el-descriptions-item>
      </el-descriptions>
    </section>
  </main>
</template>
