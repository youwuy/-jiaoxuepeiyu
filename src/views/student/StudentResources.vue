<template>
  <StudentShell eyebrow="资源学习" title="公开资源库">
    <section class="resource-filter-panel">
      <el-input v-model="keyword" class="resource-search" :suffix-icon="Search" placeholder="搜索资源名称" clearable />

      <div class="resource-filter-row">
        <span class="resource-filter-label">资源类型:</span>
        <div class="resource-chip-group" aria-label="资源类型筛选">
          <button
            v-for="item in types"
            :key="item"
            class="resource-chip"
            :class="{ active: type === item }"
            @click="selectType(item)"
          >
            {{ item }}
          </button>
        </div>
      </div>

      <div class="resource-filter-row">
        <span class="resource-filter-label">所属专业:</span>
        <div class="resource-chip-group" aria-label="所属专业筛选">
          <button
            v-for="item in categories"
            :key="item"
            class="resource-chip"
            :class="{ active: category === item }"
            @click="selectCategory(item)"
          >
            {{ item }}
          </button>
        </div>
      </div>
    </section>

    <div v-if="loading" class="student-loading">资源加载中...</div>

    <template v-else>
      <section class="resource-card-grid">
        <article v-for="resource in pageResources" :key="resource.id" class="resource-card" @click="previewResource(resource)">
          <div class="resource-cover">
            <img :src="resource.coverUrl" :alt="resource.title" />
            <span class="resource-type-badge" :class="typeClass(resource.type)">
              <el-icon>
                <VideoPlay v-if="resource.type === '视频'" />
                <Picture v-else-if="resource.type === '图像' || resource.type === '图片'" />
                <Headset v-else-if="resource.type === '音频'" />
                <Document v-else />
              </el-icon>
              {{ resource.type }}
            </span>
          </div>

          <div class="resource-card-body">
            <h2>{{ resource.title }}</h2>
            <p>{{ resource.category }}</p>

            <div class="resource-card-meta">
              <span>
                <el-icon><User /></el-icon>
                {{ resource.author || '任课教师' }}
              </span>
              <time>{{ resource.updatedAt }}</time>
            </div>
          </div>
        </article>
      </section>

      <footer class="resource-pagination">
        <p>显示 <strong>{{ resourceRangeText }}</strong> 条，共 <strong>{{ totalResourceCount }}</strong> 条资源</p>
        <div class="resource-page-controls" aria-label="资源分页">
          <button class="resource-page-button muted" type="button" aria-label="上一页">
            <el-icon><ArrowLeft /></el-icon>
          </button>
          <button class="resource-page-button active" type="button">1</button>
          <button class="resource-page-button" type="button">2</button>
          <button class="resource-page-button" type="button">3</button>
          <span class="resource-page-ellipsis">...</span>
          <button class="resource-page-button" type="button">16</button>
          <button class="resource-page-button muted" type="button" aria-label="下一页">
            <el-icon><ArrowRight /></el-icon>
          </button>
        </div>
      </footer>
    </template>
  </StudentShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { ArrowLeft, ArrowRight, Document, Headset, Picture, Search, User, VideoPlay } from '@element-plus/icons-vue';
import { fetchStudentResources } from '../../api/student';
import StudentShell from '../../components/student/StudentShell.vue';
import { filterResources, type StudentResource } from '../../features/student/resources';

const keyword = ref('');
const category = ref('全部');
const type = ref('全部');
const loading = ref(false);
const resources = ref<StudentResource[]>([]);
const page = ref(1);
const pageSize = 8;
const categories = ['全部', '城市轨道交通运营管理', '城市轨道交通车辆技术', '城市轨道交通机电技术', '城市轨道交通通信信号技术'];
const types = ['全部', '文本文档', '演示文稿', '图像', '音频', '视频'];
let resourceRequestId = 0;

const visibleResources = computed(() =>
  filterResources(resources.value, {
    keyword: keyword.value,
    category: category.value,
    type: type.value
  })
);

const pageResources = computed(() => {
  const start = (page.value - 1) * pageSize;
  return visibleResources.value.slice(start, start + pageSize);
});

const totalResourceCount = computed(() => visibleResources.value.length);
const resourceRangeText = computed(() => {
  if (totalResourceCount.value === 0) {
    return '0-0';
  }

  const start = (page.value - 1) * pageSize + 1;
  const end = Math.min(page.value * pageSize, totalResourceCount.value);
  return `${start}-${end}`;
});

async function loadResources() {
  const requestId = ++resourceRequestId;
  loading.value = true;

  try {
    const remoteResources = await fetchStudentResources({
      keyword: keyword.value.trim() || undefined,
      resourceType: type.value === '全部' ? undefined : type.value
    });

    if (requestId !== resourceRequestId) {
      return;
    }

    resources.value = remoteResources;
  } catch (error) {
    if (requestId === resourceRequestId) {
      resources.value = [];
      ElMessage.error(error instanceof Error ? error.message : '学习资源加载失败');
    }
  } finally {
    if (requestId === resourceRequestId) {
      loading.value = false;
    }
  }
}

function selectType(value: string) {
  type.value = value;
}

function selectCategory(value: string) {
  category.value = value;
}

function typeClass(value: string) {
  if (value === '视频') {
    return 'is-video';
  }

  if (value === '图像' || value === '图片') {
    return 'is-image';
  }

  if (value === '音频') {
    return 'is-audio';
  }

  if (value === '实训试题') {
    return 'is-exam';
  }

  if (value === '演示文稿') {
    return 'is-presentation';
  }

  return 'is-document';
}

function previewResource(resource: StudentResource) {
  const targetUrl = resource.previewUrl || resource.fileUrl;
  if (targetUrl) {
    window.open(targetUrl, '_blank', 'noopener');
    return;
  }

  ElMessage.warning(`当前资源未配置预览链接：${resource.title}`);
}

watch([keyword, category, type], () => {
  page.value = 1;
});

watch([keyword, type], () => {
  void loadResources();
});

onMounted(() => {
  void loadResources();
});
</script>
