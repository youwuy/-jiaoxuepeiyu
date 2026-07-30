<template>
  <StudentShell eyebrow="资源学习" title="公开资源库">
    <section class="student-board-toolbar">
      <div class="student-filter-tabs">
        <button
          v-for="item in categories"
          :key="item"
          :class="{ active: category === item }"
          @click="category = item"
        >
          {{ item }}
        </button>
      </div>
      <div class="student-board-actions">
        <el-select v-model="type" class="compact-select" placeholder="类型">
          <el-option v-for="item in types" :key="item" :label="item" :value="item" />
        </el-select>
        <el-input v-model="keyword" class="module-search" :prefix-icon="Search" placeholder="搜索资源名称" clearable />
      </div>
    </section>

    <div v-if="loading" class="student-loading">资源加载中...</div>

    <section v-else class="resource-list">
      <article v-for="resource in visibleResources" :key="resource.id" class="resource-row">
        <div class="resource-icon">
          <el-icon>
            <VideoPlay v-if="resource.type === '视频'" />
            <Picture v-else-if="resource.type === '图片'" />
            <Document v-else />
          </el-icon>
        </div>
        <div>
          <h2>{{ resource.title }}</h2>
          <p>{{ resource.category }} · {{ resource.courseName || '全员公开' }}</p>
        </div>
        <span class="resource-pill">{{ resource.type }}</span>
        <span>
          <el-icon><Folder /></el-icon>
          {{ resource.size }}
        </span>
        <span>
          <el-icon><Calendar /></el-icon>
          {{ resource.updatedAt }}
        </span>
        <el-button class="resource-action" text type="primary">预览</el-button>
      </article>
    </section>
  </StudentShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Calendar, Document, Folder, Picture, Search, VideoPlay } from '@element-plus/icons-vue';
import { fetchStudentResources } from '../../api/student';
import StudentShell from '../../components/student/StudentShell.vue';
import { filterResources, mockResources } from '../../features/student/resources';

const keyword = ref('');
const category = ref('全部');
const type = ref('全部');
const loading = ref(false);
const resources = ref(mockResources);
const categories = ['全部', '课程资源', '实训资源', '公开资料'];
const types = ['全部', 'PPT', '视频', '图片', 'PDF'];

const visibleResources = computed(() =>
  filterResources(resources.value, {
    keyword: keyword.value,
    category: category.value,
    type: type.value
  })
);

onMounted(async () => {
  loading.value = true;
  try {
    resources.value = await fetchStudentResources();
  } catch {
    ElMessage.warning('后端资源接口暂不可用，已展示本地示例数据');
  } finally {
    loading.value = false;
  }
});
</script>
