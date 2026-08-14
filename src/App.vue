<template>
  <RouterView />
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, watch } from 'vue';
import { useRoute } from 'vue-router';
import { sendOnlineHeartbeat } from './api/auth';
import type { AuthPortal } from './api/http';

const route = useRoute();
let heartbeatTimer: ReturnType<typeof setInterval> | undefined;

const activePortal = computed<AuthPortal | null>(() => {
  const path = route.path;
  if (path.startsWith('/admin/') && path !== '/admin/login') return 'admin';
  if (path.startsWith('/student/') && path !== '/student/login') return 'student';
  return null;
});

function stopHeartbeat() {
  if (heartbeatTimer) clearInterval(heartbeatTimer);
  heartbeatTimer = undefined;
}

function reportHeartbeat(portal: AuthPortal) {
  void sendOnlineHeartbeat(portal).catch(() => undefined);
}

watch(activePortal, (portal) => {
  stopHeartbeat();
  if (!portal) return;
  reportHeartbeat(portal);
  heartbeatTimer = setInterval(() => reportHeartbeat(portal), 30_000);
}, { immediate: true });

onBeforeUnmount(stopHeartbeat);
</script>
