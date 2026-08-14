<template>
  <section class="student-shell">
    <header class="student-topbar">
      <div class="student-brand">
        <span class="student-brand-icon">
          <el-icon><Collection /></el-icon>
        </span>
        <strong>城轨实训教学系统</strong>
      </div>
      <nav class="student-nav">
        <RouterLink v-for="item in navItems" :key="item.path" :to="item.path">
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>
      <div class="student-userbar">
        <el-badge is-dot>
          <el-button :icon="Bell" circle aria-label="消息通知" />
        </el-badge>
        <el-dropdown @command="handleUserCommand">
          <button class="user-trigger">
            {{ displayName }}
            <el-icon><ArrowDown /></el-icon>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout" :disabled="loggingOut">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <main class="student-main" :aria-label="`${eyebrow}-${title}`">
      <slot />
    </main>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowDown, Bell, Collection, DataAnalysis, Files, Monitor } from '@element-plus/icons-vue';
import { getAuthUser } from '../../api/http';
import { logout } from '../../api/auth';

interface StoredStudentUser {
  realName?: string;
  username?: string;
}

defineProps<{
  eyebrow: string;
  title: string;
}>();

const router = useRouter();
const loggingOut = ref(false);

const navItems = [
  { label: '课程学习', path: '/student/courses', icon: Collection },
  { label: '实训中心', path: '/student/training', icon: Monitor },
  { label: '资源学习', path: '/student/resources', icon: Files },
  { label: '个人中心', path: '/student/profile', icon: DataAnalysis }
];

const displayName = computed(() => {
  const user = getAuthUser<StoredStudentUser>('student');
  return user?.realName || user?.username || '学员';
});

async function handleUserCommand(command: string) {
  if (command !== 'logout' || loggingOut.value) {
    return;
  }
  loggingOut.value = true;
  try {
    await logout('student');
  } catch {
    // Local session is cleared even when the server session has already expired.
  } finally {
    loggingOut.value = false;
    await router.replace('/student/login');
  }
}
</script>
