<template>
  <section class="admin-shared-resource-preview">
    <template v-if="source && !failed">
      <div v-if="kind === 'image'" class="admin-shared-resource-preview-stage">
        <img :src="source" :alt="resource?.resourceName || '资源预览'" :style="{ transform: `scale(${zoom})` }" @error="failed = true" />
        <div class="admin-shared-resource-preview-tools">
          <el-button :icon="ZoomOut" circle aria-label="缩小" :disabled="zoom <= 0.5" @click="zoom = Math.max(0.5, zoom - 0.1)" />
          <span>{{ Math.round(zoom * 100) }}%</span>
          <el-button :icon="ZoomIn" circle aria-label="放大" :disabled="zoom >= 2" @click="zoom = Math.min(2, zoom + 0.1)" />
        </div>
      </div>
      <div v-else-if="kind === 'video' || kind === 'audio'" class="admin-shared-resource-preview-stage media">
        <video v-if="kind === 'video'" ref="media" :src="source" controls @error="failed = true" />
        <audio v-else ref="media" :src="source" controls @error="failed = true" />
        <label class="admin-shared-resource-preview-rate">
          <span>播放速度</span>
          <el-slider v-model="rate" :min="0.5" :max="2" :step="0.1" @input="applyRate" />
          <b>{{ rate.toFixed(1) }}x</b>
        </label>
      </div>
      <iframe v-else-if="kind === 'frame'" :src="source" :title="resource?.resourceName || '资源预览'" @load="failed = false" @error="failed = true" />
      <div v-else class="admin-shared-resource-preview-fallback">
        <el-icon><Document /></el-icon><strong>{{ resource?.fileName || resource?.resourceName }}</strong>
        <p>当前文件类型不支持在线预览，请下载后查看。</p>
      </div>
    </template>
    <div v-else-if="failed" class="admin-shared-resource-preview-fallback error">
      <el-icon><Warning /></el-icon><strong>资源加载失败，请稍后重试</strong>
      <el-button @click="retry">重新加载</el-button>
    </div>
    <el-empty v-else description="暂无可预览内容" />
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { Document, Warning, ZoomIn, ZoomOut } from '@element-plus/icons-vue';
import { resourcePreviewKind, resourcePreviewSource, type PreviewableResource } from '../../features/resources/preview';

const props = defineProps<{ resource?: PreviewableResource | null }>();
const failed = ref(false);
const zoom = ref(1);
const rate = ref(1);
const media = ref<HTMLMediaElement | null>(null);
const source = computed(() => resourcePreviewSource(props.resource));
const kind = computed(() => resourcePreviewKind(props.resource));

function applyRate(value: number | number[]) {
  if (media.value) media.value.playbackRate = Number(value);
}

function retry() {
  failed.value = false;
  if (media.value) media.value.load();
}

watch(() => props.resource, () => { failed.value = false; zoom.value = 1; rate.value = 1; });
</script>
