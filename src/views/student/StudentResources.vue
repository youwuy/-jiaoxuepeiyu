<template>
  <StudentShell eyebrow="资源学习" title="公开资源库">
    <section class="module-toolbar">
      <el-input v-model="keyword" class="module-search" :prefix-icon="Search" placeholder="搜索资源名称" clearable />
      <el-select v-model="category" class="compact-select" placeholder="分类">
        <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
      </el-select>
      <el-select v-model="type" class="compact-select" placeholder="类型">
        <el-option v-for="item in types" :key="item" :label="item" :value="item" />
      </el-select>
    </section>

    <section class="resource-list">
      <article v-for="resource in visibleResources" :key="resource.id" class="resource-row">
        <div class="resource-icon">{{ resource.type.slice(0, 1) }}</div>
        <div>
          <h2>{{ resource.title }}</h2>
          <p>{{ resource.category }} · {{ resource.courseName || '全员公开' }}</p>
        </div>
        <span>{{ resource.type }}</span>
        <span>{{ resource.size }}</span>
        <span>{{ resource.updatedAt }}</span>
        <el-button text type="primary">预览</el-button>
      </article>
    </section>
  </StudentShell>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { Search } from '@element-plus/icons-vue';
import StudentShell from '../../components/student/StudentShell.vue';
import { filterResources, mockResources } from '../../features/student/resources';

const keyword = ref('');
const category = ref('全部');
const type = ref('全部');
const categories = ['全部', '课程资源', '实训资源', '公开资料'];
const types = ['全部', 'PPT', '视频', '图片', 'PDF'];

const visibleResources = computed(() =>
  filterResources(mockResources, {
    keyword: keyword.value,
    category: category.value,
    type: type.value
  })
);
</script>
